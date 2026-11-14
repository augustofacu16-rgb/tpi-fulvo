package com.fulvo.torneos.service;

import com.fulvo.common.dto.ReprogramacionPartidoDTO;
import com.fulvo.common.dto.SuspensionPartidoDTO;
import com.fulvo.common.exception.RecursoNoEncontradoException;
import com.fulvo.torneos.domain.Equipo;
import com.fulvo.torneos.domain.EstadoNovedadPartido;
import com.fulvo.torneos.domain.NovedadCronograma;
import com.fulvo.torneos.domain.Torneo;
import com.fulvo.torneos.dto.CronogramaTorneoResponseDTO;
import com.fulvo.torneos.dto.NovedadCronogramaResponseDTO;
import com.fulvo.torneos.dto.PartidoPendienteDTO;
import com.fulvo.torneos.repository.EquipoRepository;
import com.fulvo.torneos.repository.NovedadCronogramaRepository;
import com.fulvo.torneos.repository.TorneoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de dominio responsable de procesar el impacto de suspensiones y reprogramaciones
 * sobre el calendario del torneo, garantizando consistencia y trazabilidad de partidos pendientes (Subsección 4.5).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CronogramaTorneoService {

    private final TorneoRepository torneoRepository;
    private final EquipoRepository equipoRepository;
    private final NovedadCronogramaRepository novedadCronogramaRepository;
    private final TorneoNotificacionService torneoNotificacionService;

    /**
     * Procesa la suspensión de un partido en el certamen y alerta a los delegados (RN-07, RN-11, RN-12).
     */
    @Transactional
    public NovedadCronogramaResponseDTO registrarSuspension(Long torneoId, SuspensionPartidoDTO dto) {
        log.info("Procesando suspensión del partido #{} en torneo #{} (local={}, visitante={})",
                dto.partidoId(), torneoId, dto.equipoLocalId(), dto.equipoVisitanteId());

        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado con ID: " + torneoId));

        Equipo local = equipoRepository.findById(dto.equipoLocalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo local no encontrado con ID: " + dto.equipoLocalId()));

        Equipo visitante = equipoRepository.findById(dto.equipoVisitanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo visitante no encontrado con ID: " + dto.equipoVisitanteId()));

        NovedadCronograma novedad = novedadCronogramaRepository.findByTorneoIdAndPartidoId(torneoId, dto.partidoId())
                .orElseGet(() -> NovedadCronograma.builder()
                        .torneoId(torneoId)
                        .partidoId(dto.partidoId())
                        .equipoLocalId(dto.equipoLocalId())
                        .equipoVisitanteId(dto.equipoVisitanteId())
                        .fechaOriginal(dto.fechaOriginal())
                        .build());

        novedad.setEstadoPartido(EstadoNovedadPartido.SUSPENDIDO);
        novedad.setMotivo(dto.motivo());
        if (dto.fechaOriginal() != null) {
            novedad.setFechaOriginal(dto.fechaOriginal());
        }
        novedad.setFechaRegistro(LocalDateTime.now());
        novedad.setNotificadoDelegados(true);

        NovedadCronograma persistida = novedadCronogramaRepository.save(novedad);
        log.info("Novedad de suspensión guardada con éxito con ID: {}", persistida.getId());

        // Despacho de notificaciones por correo a los delegados de ambos equipos (RN-11, RN-12)
        despacharNotificacionesSuspension(torneo, local, visitante, dto);

        return mapToResponseDTO(persistida, local.getNombre(), visitante.getNombre());
    }

    /**
     * Procesa la reprogramación de un partido suspendido y notifica la nueva fecha/sede a delegados (Subsección 4.5).
     */
    @Transactional
    public NovedadCronogramaResponseDTO registrarReprogramacion(Long torneoId, ReprogramacionPartidoDTO dto) {
        log.info("Procesando reprogramación del partido #{} en torneo #{} a nueva fecha: {}",
                dto.partidoId(), torneoId, dto.nuevaFechaHora());

        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado con ID: " + torneoId));

        Equipo local = equipoRepository.findById(dto.equipoLocalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo local no encontrado con ID: " + dto.equipoLocalId()));

        Equipo visitante = equipoRepository.findById(dto.equipoVisitanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo visitante no encontrado con ID: " + dto.equipoVisitanteId()));

        NovedadCronograma novedad = novedadCronogramaRepository.findByTorneoIdAndPartidoId(torneoId, dto.partidoId())
                .orElseGet(() -> NovedadCronograma.builder()
                        .torneoId(torneoId)
                        .partidoId(dto.partidoId())
                        .equipoLocalId(dto.equipoLocalId())
                        .equipoVisitanteId(dto.equipoVisitanteId())
                        .build());

        novedad.setEstadoPartido(EstadoNovedadPartido.REPROGRAMADO);
        novedad.setNuevaFechaHora(dto.nuevaFechaHora());
        novedad.setNuevaSedeId(dto.nuevaSedeId());
        novedad.setMotivo(dto.motivo());
        novedad.setFechaRegistro(LocalDateTime.now());
        novedad.setNotificadoDelegados(true);

        NovedadCronograma persistida = novedadCronogramaRepository.save(novedad);
        log.info("Novedad de reprogramación guardada con éxito con ID: {}", persistida.getId());

        // Despacho de notificaciones a los delegados de ambos equipos
        despacharNotificacionesReprogramacion(torneo, local, visitante, dto);

        return mapToResponseDTO(persistida, local.getNombre(), visitante.getNombre());
    }

    /**
     * Consulta el cronograma consolidado del torneo reflejando partidos afectados (Subsección 4.5).
     */
    @Transactional(readOnly = true)
    public CronogramaTorneoResponseDTO obtenerCronogramaTorneo(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado con ID: " + torneoId));

        List<NovedadCronograma> novedades = novedadCronogramaRepository.findByTorneoIdOrderByFechaRegistroDesc(torneoId);
        Map<Long, String> nombresEquipos = obtenerMapaNombresEquipos(torneoId);

        List<NovedadCronogramaResponseDTO> novedadesDTO = novedades.stream()
                .map(n -> mapToResponseDTO(n,
                        nombresEquipos.getOrDefault(n.getEquipoLocalId(), "Equipo #" + n.getEquipoLocalId()),
                        nombresEquipos.getOrDefault(n.getEquipoVisitanteId(), "Equipo #" + n.getEquipoVisitanteId())))
                .toList();

        int suspendidos = (int) novedades.stream()
                .filter(n -> n.getEstadoPartido() == EstadoNovedadPartido.SUSPENDIDO)
                .count();

        int reprogramados = (int) novedades.stream()
                .filter(n -> n.getEstadoPartido() == EstadoNovedadPartido.REPROGRAMADO)
                .count();

        return CronogramaTorneoResponseDTO.builder()
                .torneoId(torneo.getId())
                .nombreTorneo(torneo.getNombre())
                .estadoTorneo(torneo.getEstado().name())
                .totalPartidosAfectados(novedades.size())
                .partidosSuspendidos(suspendidos)
                .partidosReprogramados(reprogramados)
                .novedades(novedadesDTO)
                .build();
    }

    /**
     * Retorna los partidos pendientes o pospuestos, opcionalmente filtrados para un equipo/delegado (Subsección 4.5).
     */
    @Transactional(readOnly = true)
    public List<PartidoPendienteDTO> obtenerPartidosPendientes(Long torneoId, Long equipoId) {
        if (!torneoRepository.existsById(torneoId)) {
            throw new RecursoNoEncontradoException("Torneo no encontrado con ID: " + torneoId);
        }

        List<NovedadCronograma> novedades;
        if (equipoId != null) {
            novedades = novedadCronogramaRepository.findByTorneoIdAndEquipoId(torneoId, equipoId);
        } else {
            novedades = novedadCronogramaRepository.findByTorneoIdOrderByFechaRegistroDesc(torneoId);
        }

        Map<Long, String> nombresEquipos = obtenerMapaNombresEquipos(torneoId);

        return novedades.stream()
                .map(n -> PartidoPendienteDTO.builder()
                        .partidoId(n.getPartidoId())
                        .equipoLocalId(n.getEquipoLocalId())
                        .nombreEquipoLocal(nombresEquipos.getOrDefault(n.getEquipoLocalId(), "Equipo #" + n.getEquipoLocalId()))
                        .equipoVisitanteId(n.getEquipoVisitanteId())
                        .nombreEquipoVisitante(nombresEquipos.getOrDefault(n.getEquipoVisitanteId(), "Equipo #" + n.getEquipoVisitanteId()))
                        .estadoPartido(n.getEstadoPartido().name())
                        .motivo(n.getMotivo())
                        .fechaOriginal(n.getFechaOriginal())
                        .nuevaFechaHora(n.getNuevaFechaHora())
                        .nuevaSedeId(n.getNuevaSedeId())
                        .requiereReprogramacion(n.getEstadoPartido() == EstadoNovedadPartido.SUSPENDIDO)
                        .build())
                .toList();
    }

    private void despacharNotificacionesSuspension(Torneo torneo, Equipo local, Equipo visitante, SuspensionPartidoDTO dto) {
        String emailLocal = torneoNotificacionService.resolverEmailDestinatario(local.getDelegadoUsuarioId());
        torneoNotificacionService.despacharSuspensionPartido(
                emailLocal, local.getNombre(), visitante.getNombre(), torneo.getNombre(),
                dto.partidoId(), dto.motivo(), dto.fechaOriginal());

        String emailVisitante = torneoNotificacionService.resolverEmailDestinatario(visitante.getDelegadoUsuarioId());
        torneoNotificacionService.despacharSuspensionPartido(
                emailVisitante, visitante.getNombre(), local.getNombre(), torneo.getNombre(),
                dto.partidoId(), dto.motivo(), dto.fechaOriginal());
    }

    private void despacharNotificacionesReprogramacion(Torneo torneo, Equipo local, Equipo visitante, ReprogramacionPartidoDTO dto) {
        String emailLocal = torneoNotificacionService.resolverEmailDestinatario(local.getDelegadoUsuarioId());
        torneoNotificacionService.despacharReprogramacionPartido(
                emailLocal, local.getNombre(), visitante.getNombre(), torneo.getNombre(),
                dto.partidoId(), dto.motivo(), dto.nuevaFechaHora(), dto.nuevaSedeId());

        String emailVisitante = torneoNotificacionService.resolverEmailDestinatario(visitante.getDelegadoUsuarioId());
        torneoNotificacionService.despacharReprogramacionPartido(
                emailVisitante, visitante.getNombre(), local.getNombre(), torneo.getNombre(),
                dto.partidoId(), dto.motivo(), dto.nuevaFechaHora(), dto.nuevaSedeId());
    }

    private Map<Long, String> obtenerMapaNombresEquipos(Long torneoId) {
        return equipoRepository.findByTorneoId(torneoId).stream()
                .collect(Collectors.toMap(Equipo::getId, Equipo::getNombre, (e1, e2) -> e1));
    }

    private NovedadCronogramaResponseDTO mapToResponseDTO(NovedadCronograma entity, String nombreLocal, String nombreVisitante) {
        return NovedadCronogramaResponseDTO.builder()
                .id(entity.getId())
                .torneoId(entity.getTorneoId())
                .partidoId(entity.getPartidoId())
                .equipoLocalId(entity.getEquipoLocalId())
                .nombreEquipoLocal(nombreLocal)
                .equipoVisitanteId(entity.getEquipoVisitanteId())
                .nombreEquipoVisitante(nombreVisitante)
                .estadoPartido(entity.getEstadoPartido().name())
                .motivo(entity.getMotivo())
                .fechaOriginal(entity.getFechaOriginal())
                .nuevaFechaHora(entity.getNuevaFechaHora())
                .nuevaSedeId(entity.getNuevaSedeId())
                .fechaRegistro(entity.getFechaRegistro())
                .notificadoDelegados(entity.isNotificadoDelegados())
                .build();
    }
}
