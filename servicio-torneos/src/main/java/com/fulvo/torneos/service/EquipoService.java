package com.fulvo.torneos.service;

import com.fulvo.common.exception.BusinessRuleException;
import com.fulvo.common.exception.ConflictoEstadoException;
import com.fulvo.common.exception.RecursoNoEncontradoException;
import com.fulvo.torneos.domain.Equipo;
import com.fulvo.torneos.domain.EstadoEquipo;
import com.fulvo.torneos.domain.EstadoTorneo;
import com.fulvo.torneos.domain.Jugador;
import com.fulvo.torneos.domain.PosicionJugador;
import com.fulvo.torneos.domain.Torneo;
import com.fulvo.torneos.dto.BajaEquipoResponseDTO;
import com.fulvo.torneos.dto.JugadorInscripcionDTO;
import com.fulvo.torneos.dto.ReemplazoJugadorDTO;
import com.fulvo.torneos.dto.ReemplazoJugadorResponseDTO;
import com.fulvo.torneos.repository.EquipoRepository;
import com.fulvo.torneos.repository.JugadorRepository;
import com.fulvo.torneos.repository.TorneoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestión del plantel y de la permanencia de un equipo dentro del certamen (Subsección 3.4):
 * reemplazos de jugadores (FN-11) y bajas / descalificaciones (FN-13, RN-04).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EquipoService {

    /** Máximo de reemplazos acumulados por equipo durante la competencia. */
    public static final int MAX_REEMPLAZOS = 5;

    public static final String TIPO_BAJA_VOLUNTARIA = "BAJA_VOLUNTARIA";
    public static final String TIPO_DESCALIFICACION = "DESCALIFICACION";

    private final EquipoRepository equipoRepository;
    private final JugadorRepository jugadorRepository;
    private final TorneoRepository torneoRepository;
    private final TorneoNotificacionService torneoNotificacionService;

    /**
     * Reemplaza un jugador del plantel (Subsección 3.4.1, FN-11, RN-05/RN-11).
     * El jugador saliente (identificado por su ID autogenerado) pasa a activo = false
     * y el nuevo jugador se incorpora al plantel.
     */
    @Transactional
    public ReemplazoJugadorResponseDTO reemplazarJugador(Long equipoId, ReemplazoJugadorDTO dto) {
        // Bloqueo pesimista: dos reemplazos simultáneos no pueden superar el límite de 5 cambios
        Equipo equipo = equipoRepository.findByIdWithLock(equipoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado con ID: " + equipoId));

        if (equipo.getEstado() != EstadoEquipo.ACTIVO) {
            throw new ConflictoEstadoException("El equipo no está activo en el certamen (estado actual: " + equipo.getEstado() + ")");
        }

        Torneo torneo = equipo.getTorneo();
        if (torneo.getEstado() != EstadoTorneo.INICIADO) {
            throw new ConflictoEstadoException("Los reemplazos de jugadores solo se permiten con el torneo INICIADO (estado actual: "
                    + torneo.getEstado() + ")");
        }

        // RN-05 / RN-11: máximo 5 reemplazos acumulados
        if (equipo.getCambiosRealizados() >= MAX_REEMPLAZOS) {
            throw new BusinessRuleException("El equipo ya utilizó los " + MAX_REEMPLAZOS
                    + " reemplazos reglamentarios; no se admiten más cambios (RN-11)", "RN-11");
        }

        Long salienteId = dto.getJugadorSalienteId();
        JugadorInscripcionDTO entranteDto = dto.getNuevoJugador();
        String dniEntrante = entranteDto.getDni().trim();

        List<Jugador> vigentes = jugadorRepository.findByEquipoId(equipoId).stream()
                .filter(j -> Boolean.TRUE.equals(j.getActivo()))
                .toList();

        Jugador saliente = vigentes.stream()
                .filter(j -> j.getId().equals(salienteId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El jugador con ID " + salienteId + " no integra el plantel vigente del equipo " + equipoId));

        String dniSaliente = saliente.getDni();

        // RN-11: prohibición taxativa de sustituir futbolistas suspendidos
        if (Boolean.TRUE.equals(saliente.getSuspendido())) {
            throw new BusinessRuleException("No se puede sustituir a un futbolista suspendido (ID " + salienteId + ", DNI " + dniSaliente + ") (RN-11)", "RN-11");
        }

        if (dniSaliente.equals(dniEntrante)) {
            throw new BusinessRuleException("El jugador entrante no puede tener el mismo DNI que el saliente");
        }

        // RN-02: el entrante no puede estar vigente en otro equipo (ni en este) del mismo certamen
        if (jugadorRepository.existsByTorneoIdAndDniIn(torneo.getId(), List.of(dniEntrante))) {
            throw new ConflictoEstadoException("El jugador entrante (DNI " + dniEntrante
                    + ") ya se encuentra inscripto en un equipo de este torneo (RN-02)");
        }

        // El dorsal no puede repetirse entre los jugadores vigentes (el del saliente queda libre)
        boolean dorsalOcupado = vigentes.stream()
                .filter(j -> !j.getId().equals(saliente.getId()))
                .anyMatch(j -> j.getDorsal().equals(entranteDto.getDorsal()));
        if (dorsalOcupado) {
            throw new BusinessRuleException("El dorsal " + entranteDto.getDorsal() + " ya está asignado a otro jugador del plantel");
        }

        // RN-02: el plantel debe conservar al menos un arquero
        if (saliente.getPosicion() == PosicionJugador.ARQUERO && entranteDto.getPosicion() != PosicionJugador.ARQUERO) {
            boolean quedaOtroArquero = vigentes.stream()
                    .filter(j -> !j.getId().equals(saliente.getId()))
                    .anyMatch(j -> j.getPosicion() == PosicionJugador.ARQUERO);
            if (!quedaOtroArquero) {
                throw new BusinessRuleException("El plantel debe conservar al menos un ARQUERO: el reemplazo del único arquero debe ser otro arquero (RN-02)");
            }
        }

        // Baja lógica del saliente e incorporación del entrante (hereda la capitanía para mantener exactamente 1 capitán)
        saliente.setActivo(false);
        jugadorRepository.save(saliente);

        Jugador entrante = Jugador.builder()
                .equipo(equipo)
                .dni(dniEntrante)
                .nombre(entranteDto.getNombre().trim())
                .apellido(entranteDto.getApellido().trim())
                .dorsal(entranteDto.getDorsal())
                .posicion(entranteDto.getPosicion())
                .esCapitan(Boolean.TRUE.equals(saliente.getEsCapitan()))
                .activo(true)
                .suspendido(false)
                .build();
        Jugador entranteGuardado = jugadorRepository.save(entrante);

        equipo.setCambiosRealizados(equipo.getCambiosRealizados() + 1);
        equipoRepository.save(equipo);

        log.info("Reemplazo en equipo {}: sale jugador ID {} (DNI {}), entra jugador ID {} (DNI {}) ({}/{} cambios utilizados)",
                equipoId, salienteId, dniSaliente, entranteGuardado.getId(), dniEntrante, equipo.getCambiosRealizados(), MAX_REEMPLAZOS);

        return ReemplazoJugadorResponseDTO.builder()
                .equipoId(equipo.getId())
                .equipoNombre(equipo.getNombre())
                .jugadorSalienteId(saliente.getId())
                .dniSaliente(dniSaliente)
                .nuevoJugadorId(entranteGuardado.getId())
                .dniEntrante(dniEntrante)
                .cambiosRealizados(equipo.getCambiosRealizados())
                .cambiosRestantes(MAX_REEMPLAZOS - equipo.getCambiosRealizados())
                .build();
    }

    /**
     * Baja voluntaria o descalificación de un equipo (Subsección 3.4.2, FN-13, RN-04).
     * Permitida en cualquier fase del torneo mientras este no haya finalizado ni sido cancelado.
     *
     * @param tipo BAJA_VOLUNTARIA (por defecto) o DESCALIFICACION.
     */
    @Transactional
    public BajaEquipoResponseDTO darDeBajaEquipo(Long torneoId, Long equipoId, String tipo, String motivo) {
        boolean descalificacion = parsearTipoBaja(tipo);

        // Se bloquea primero el torneo (mismo orden que admisiones y fixture) y luego el equipo
        Torneo torneo = torneoRepository.findByIdWithLock(torneoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado con ID: " + torneoId));

        Equipo equipo = equipoRepository.findByIdWithLock(equipoId)
                .filter(e -> e.getTorneo().getId().equals(torneoId))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El equipo " + equipoId + " no pertenece al torneo " + torneoId));

        if (torneo.getEstado() == EstadoTorneo.FINALIZADO || torneo.getEstado() == EstadoTorneo.CANCELADO) {
            throw new ConflictoEstadoException("No se puede dar de baja a un equipo de un torneo " + torneo.getEstado());
        }
        if (equipo.getEstado() != EstadoEquipo.ACTIVO) {
            throw new ConflictoEstadoException("El equipo ya no está activo en el certamen (estado actual: " + equipo.getEstado() + ")");
        }

        equipo.setEstado(descalificacion ? EstadoEquipo.DESCALIFICADO : EstadoEquipo.DADO_DE_BAJA);
        equipoRepository.save(equipo);

        // Con la inscripción cerrada, la baja deja el cupo incompleto: se reabre para poder admitir un reemplazante
        if (torneo.getEstado() == EstadoTorneo.CERRADO_INSCRIPCION) {
            torneo.setEstado(EstadoTorneo.ABIERTO_INSCRIPCION);
            torneoRepository.save(torneo);
            log.info("Torneo {} reabierto a ABIERTO_INSCRIPCION: la baja del equipo {} dejó el cupo incompleto", torneoId, equipoId);
        }

        log.info("Equipo {} ('{}') del torneo {} pasó a {} (motivo: {})",
                equipoId, equipo.getNombre(), torneoId, equipo.getEstado(), motivo);

        // RN-11: notificación asíncrona al delegado; un fallo de correo no revierte la baja
        torneoNotificacionService.despacharBajaEquipo(
                torneoNotificacionService.resolverEmailDestinatario(equipo.getDelegadoUsuarioId()),
                equipo.getNombre(),
                torneo.getNombre(),
                descalificacion,
                motivo);

        return BajaEquipoResponseDTO.builder()
                .equipoId(equipo.getId())
                .equipoNombre(equipo.getNombre())
                .torneoId(torneo.getId())
                .estadoEquipo(equipo.getEstado())
                .estadoTorneo(torneo.getEstado())
                .motivo(motivo)
                .build();
    }

    /** @return true si el tipo es DESCALIFICACION; false si es BAJA_VOLUNTARIA. */
    private boolean parsearTipoBaja(String tipo) {
        String normalizado = tipo == null ? TIPO_BAJA_VOLUNTARIA : tipo.trim().toUpperCase();
        if (normalizado.equals(TIPO_DESCALIFICACION)) {
            return true;
        }
        if (normalizado.equals(TIPO_BAJA_VOLUNTARIA)) {
            return false;
        }
        throw new BusinessRuleException("Tipo de baja no válido: '" + tipo + "'. Valores admitidos: "
                + TIPO_BAJA_VOLUNTARIA + ", " + TIPO_DESCALIFICACION);
    }
}
