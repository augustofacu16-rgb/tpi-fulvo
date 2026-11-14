package com.fulvo.torneos.service;

import com.fulvo.common.dto.CrucePartidoDTO;
import com.fulvo.common.exception.BusinessRuleException;
import com.fulvo.common.exception.ConflictoEstadoException;
import com.fulvo.common.exception.RecursoNoEncontradoException;
import com.fulvo.torneos.client.OperacionesApiClient;
import com.fulvo.torneos.domain.Equipo;
import com.fulvo.torneos.domain.EstadoEquipo;
import com.fulvo.torneos.domain.EstadoTorneo;
import com.fulvo.torneos.domain.FormatoTorneo;
import com.fulvo.torneos.domain.Torneo;
import com.fulvo.torneos.dto.CambiarEstadoTorneoDTO;
import com.fulvo.torneos.dto.CrearTorneoDTO;
import com.fulvo.torneos.dto.FixtureGeneradoResponseDTO;
import com.fulvo.torneos.dto.TorneoResponseDTO;
import com.fulvo.torneos.repository.EquipoRepository;
import com.fulvo.torneos.repository.TorneoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fulvo.common.dto.JugadorPlantelDTO;
import com.fulvo.torneos.repository.JugadorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TorneoService {

    private final TorneoRepository torneoRepository;
    private final EquipoRepository equipoRepository;
    private final FixtureGeneratorService fixtureGeneratorService;
    private final OperacionesApiClient operacionesApiClient;
    private final JugadorRepository jugadorRepository;

    @Transactional
    public TorneoResponseDTO crearTorneo(CrearTorneoDTO dto) {
        log.info("Creando nuevo torneo: '{}', formato: {}, cupo: {}", dto.getNombre(), dto.getFormato(), dto.getCupoEquipos());

        validarCupoPorFormato(dto.getFormato(), dto.getCupoEquipos());

        Torneo torneo = Torneo.builder()
                .nombre(dto.getNombre().trim())
                .formato(dto.getFormato())
                .cupoEquipos(dto.getCupoEquipos())
                .estado(EstadoTorneo.ABIERTO_INSCRIPCION)
                .fechaInicio(dto.getFechaInicio())
                .build();

        Torneo guardado = torneoRepository.save(torneo);
        log.info("Torneo creado exitosamente con ID: {}", guardado.getId());

        return mapToResponseDTO(guardado, 0);
    }

    @Transactional(readOnly = true)
    public List<TorneoResponseDTO> listarTorneos() {
        return torneoRepository.findAllConConteoEquipos();
    }

    @Transactional(readOnly = true)
    public TorneoResponseDTO obtenerTorneoPorId(Long id) {
        return torneoRepository.findTorneoDTOById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado con ID: " + id));
    }

    /**
     * Cierra la inscripción o cancela el torneo (Subsección 2.2.1, FN-06, RN-01, RN-04).
     * Si el nuevo estado es CERRADO_INSCRIPCION (o INSCRIPCION_CERRADA), valida que el cupo esté al 100%.
     * Si no alcanzó el 100%, rechaza con 409 Conflict.
     */
    @Transactional
    public TorneoResponseDTO cambiarEstado(Long id, CambiarEstadoTorneoDTO dto) {
        Torneo torneo = torneoRepository.findByIdWithLock(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado con ID: " + id));

        String estadoNormalizado = dto.getEstado().trim().toUpperCase();

        if (estadoNormalizado.equals("CERRADO_INSCRIPCION") || estadoNormalizado.equals("INSCRIPCION_CERRADA")) {
            if (torneo.getEstado() != EstadoTorneo.ABIERTO_INSCRIPCION) {
                throw new ConflictoEstadoException("Solo se puede cerrar la inscripción de un torneo en estado ABIERTO_INSCRIPCION (estado actual: " + torneo.getEstado() + ")");
            }

            long equiposAdmitidos = equipoRepository.countByTorneoIdAndEstado(torneo.getId(), EstadoEquipo.ACTIVO);
            if (equiposAdmitidos < torneo.getCupoEquipos()) {
                log.warn("Intento de cerrar inscripción con cupo incompleto en Torneo {}: {}/{} equipos admitidos",
                        torneo.getId(), equiposAdmitidos, torneo.getCupoEquipos());
                throw new ConflictoEstadoException("No se puede cerrar la inscripción sin cubrir el 100% del cupo (admitidos: "
                        + equiposAdmitidos + " de " + torneo.getCupoEquipos() + ") (RN-01)");
            }

            torneo.setEstado(EstadoTorneo.CERRADO_INSCRIPCION);
            log.info("Inscripción cerrada al 100% para Torneo {}", torneo.getId());

        } else if (estadoNormalizado.equals("CANCELADO")) {
            // RN-04: El organizador puede cancelar el torneo en cualquier momento
            torneo.setEstado(EstadoTorneo.CANCELADO);
            log.info("Torneo {} cancelado por el organizador (RN-04)", torneo.getId());
        } else {
            throw new BusinessRuleException("Transición de estado no permitida a: " + dto.getEstado());
        }

        Torneo actualizado = torneoRepository.save(torneo);
        long inscriptos = equipoRepository.countByTorneoIdAndEstado(actualizado.getId(), EstadoEquipo.ACTIVO);
        return mapToResponseDTO(actualizado, (int) inscriptos);
    }

    /**
     * Genera el fixture determinístico y lo despacha a Operaciones (Subsección 2.2.2, 2.2.3).
     * Transiciona el torneo a INICIADO.
     */
    @Transactional
    public FixtureGeneradoResponseDTO generarFixture(Long id) {
        Torneo torneo = torneoRepository.findByIdWithLock(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado con ID: " + id));

        if (torneo.getEstado() == EstadoTorneo.INICIADO) {
            throw new ConflictoEstadoException("El fixture ya ha sido generado previamente para este torneo");
        }

        if (torneo.getEstado() == EstadoTorneo.CANCELADO || torneo.getEstado() == EstadoTorneo.FINALIZADO) {
            throw new ConflictoEstadoException("No se puede generar fixture para un torneo en estado " + torneo.getEstado());
        }

        // Obtener directamente desde la BD solo los equipos admitidos (ACTIVO) y ordenados por ID ascendente
        List<Equipo> equipos = equipoRepository.findByTorneoIdAndEstadoOrderByIdAsc(torneo.getId(), EstadoEquipo.ACTIVO);

        if (equipos.size() < torneo.getCupoEquipos()) {
            throw new ConflictoEstadoException("No se puede generar el fixture: el torneo aún no cuenta con el 100% del cupo de equipos admitidos ("
                    + equipos.size() + "/" + torneo.getCupoEquipos() + ")");
        }

        // Generar los cruces determinísticos
        List<CrucePartidoDTO> cruces = fixtureGeneratorService.generarFixture(torneo.getId(), torneo.getFormato(), equipos);

        // Despachar lote vía RestClient a servicio-operaciones-partidos (POST /v1/partidos/lote)
        operacionesApiClient.despacharLotePartidos(cruces);

        // Transición de estado a INICIADO
        torneo.setEstado(EstadoTorneo.INICIADO);
        torneoRepository.save(torneo);
        log.info("Torneo {} transicionado a INICIADO tras despacho exitoso de {} cruces a Operaciones",
                torneo.getId(), cruces.size());

        int totalFechasOFases = cruces.stream().mapToInt(CrucePartidoDTO::getFechaOFase).max().orElse(1);

        return FixtureGeneradoResponseDTO.builder()
                .torneoId(torneo.getId())
                .formato(torneo.getFormato().name())
                .totalFechasOFases(totalFechasOFases)
                .totalPartidos(cruces.size())
                .partidos(cruces)
                .build();
    }

    private void validarCupoPorFormato(FormatoTorneo formato, Integer cupo) {
        if (formato == FormatoTorneo.LIGA) {
            if (cupo != 10 && cupo != 20) {
                throw new BusinessRuleException("El cupo de equipos para Liga debe ser exactamente 10 o 20");
            }
        } else if (formato == FormatoTorneo.LLAVES) {
            if (cupo != 8 && cupo != 16) {
                throw new BusinessRuleException("El cupo de equipos para Llaves debe ser exactamente 8 o 16");
            }
        }
    }

    private TorneoResponseDTO mapToResponseDTO(Torneo torneo, int equiposInscriptos) {
        return TorneoResponseDTO.builder()
                .id(torneo.getId())
                .nombre(torneo.getNombre())
                .formato(torneo.getFormato())
                .cupoEquipos(torneo.getCupoEquipos())
                .equiposInscriptos(equiposInscriptos)
                .estado(torneo.getEstado())
                .fechaInicio(torneo.getFechaInicio())
                .fechaCreacion(torneo.getFechaCreacion())
                .build();
    }
    public List<JugadorPlantelDTO> obtenerJugadoresPlantel(Long torneoId) {
        return jugadorRepository.findActivosByTorneoId(torneoId).stream()
                .map(j -> new JugadorPlantelDTO(
                        j.getEquipo().getId(),
                        j.getDorsal(),
                        j.getNombre(),
                        j.getApellido()
                ))
                .toList();
    }
}
