package com.fulvo.torneos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulvo.common.exception.BusinessRuleException;
import com.fulvo.common.exception.ConflictoEstadoException;
import com.fulvo.common.exception.RecursoNoEncontradoException;
import com.fulvo.torneos.domain.*;
import com.fulvo.torneos.dto.CrearSolicitudInscripcionDTO;
import com.fulvo.torneos.dto.JugadorInscripcionDTO;
import com.fulvo.torneos.dto.ResolverAdmisionDTO;
import com.fulvo.torneos.dto.SolicitudInscripcionResponseDTO;
import com.fulvo.torneos.repository.EquipoRepository;
import com.fulvo.torneos.repository.JugadorRepository;
import com.fulvo.torneos.repository.SolicitudInscripcionRepository;
import com.fulvo.torneos.repository.TorneoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudInscripcionService {

    private final SolicitudInscripcionRepository solicitudRepository;
    private final TorneoRepository torneoRepository;
    private final EquipoRepository equipoRepository;
    private final JugadorRepository jugadorRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SolicitudInscripcionResponseDTO postularEquipo(Long torneoId, String fanaticoUsuarioId, CrearSolicitudInscripcionDTO dto) {
        log.info("Procesando postulación de equipo '{}' para torneo ID {} por usuario {}",
                dto.getNombreEquipo(), torneoId, fanaticoUsuarioId);

        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado con ID: " + torneoId));

        if (torneo.getEstado() != EstadoTorneo.ABIERTO_INSCRIPCION) {
            throw new ConflictoEstadoException("El torneo no se encuentra en estado ABIERTO_INSCRIPCION");
        }

        // RN-03: Límite de postulación del fanático (solo una solicitud activa por torneo)
        boolean yaTieneSolicitud = solicitudRepository.existsByTorneoIdAndFanaticoUsuarioIdAndEstadoIn(
                torneoId,
                fanaticoUsuarioId,
                List.of(EstadoSolicitudInscripcion.PENDIENTE, EstadoSolicitudInscripcion.ACEPTADA)
        );
        if (yaTieneSolicitud) {
            throw new ConflictoEstadoException("El usuario ya cuenta con una solicitud de inscripción activa para este torneo");
        }

        // Validar nombre de equipo único en el torneo
        if (equipoRepository.existsByTorneoIdAndNombreIgnoreCase(torneoId, dto.getNombreEquipo().trim())) {
            throw new ConflictoEstadoException("Ya existe un equipo admitido con ese nombre en este torneo");
        }

        // RN-02: Conformación de Plantel (15 a 22 jugadores, 1 arquero mínimo, exactamente 1 capitán)
        validarPlantelBuenaFe(torneoId, dto.getJugadores());

        String datosPlantelJson;
        try {
            datosPlantelJson = objectMapper.writeValueAsString(dto.getJugadores());
        } catch (JsonProcessingException e) {
            log.error("Error al serializar plantel a JSON", e);
            throw new BusinessRuleException("Error al procesar los datos del plantel");
        }

        SolicitudInscripcion solicitud = SolicitudInscripcion.builder()
                .torneo(torneo)
                .fanaticoUsuarioId(fanaticoUsuarioId)
                .nombreEquipo(dto.getNombreEquipo().trim())
                .estado(EstadoSolicitudInscripcion.PENDIENTE)
                .datosPlantelJson(datosPlantelJson)
                .build();

        SolicitudInscripcion guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud de inscripción guardada con ID {}", guardada.getId());

        return mapToResponseDTO(guardada, null);
    }

    @Transactional
    public SolicitudInscripcionResponseDTO resolverAdmision(Long solicitudId, ResolverAdmisionDTO dto) {
        log.info("Resolviendo admisión para solicitud ID {}: {}", solicitudId, dto.getEstado());

        SolicitudInscripcion solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud de inscripción no encontrada con ID: " + solicitudId));

        if (solicitud.getEstado() != EstadoSolicitudInscripcion.PENDIENTE) {
            throw new ConflictoEstadoException("La solicitud ya ha sido resuelta previamente (estado actual: " + solicitud.getEstado() + ")");
        }

        Long equipoIdGenerado = null;
        if (dto.getEstado() == EstadoSolicitudInscripcion.ACEPTADA) {
            // Bloqueo pesimista (SELECT ... FOR UPDATE) para serializar la admisión y evitar race condition de cupo
            Torneo torneo = torneoRepository.findByIdWithLock(solicitud.getTorneo().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado"));
            // Verificar cupo disponible del torneo
            long equiposActivos = equipoRepository.countByTorneoIdAndEstado(torneo.getId(), EstadoEquipo.ACTIVO);
            if (equiposActivos >= torneo.getCupoEquipos()) {
                throw new ConflictoEstadoException("No es posible admitir el equipo: el cupo de " + torneo.getCupoEquipos() + " equipos ya está completo");
            }

            // Deserializar plantel y volver a validar no-duplicidad de DNIs al momento de la admisión definitiva
            List<JugadorInscripcionDTO> listaJugadores = deserializarPlantel(solicitud.getDatosPlantelJson());
            validarDnisNoInscriptosEnTorneo(torneo.getId(), listaJugadores);

            // Crear el Equipo formalmente en estado ACTIVO
            Equipo equipo = Equipo.builder()
                    .torneo(torneo)
                    .solicitud(solicitud)
                    .nombre(solicitud.getNombreEquipo())
                    .delegadoUsuarioId(solicitud.getFanaticoUsuarioId())
                    .estado(EstadoEquipo.ACTIVO)
                    .cambiosRealizados(0)
                    .build();

            // Asociar los jugadores del plantel
            for (JugadorInscripcionDTO jDto : listaJugadores) {
                Jugador jugador = Jugador.builder()
                        .dni(jDto.getDni().trim())
                        .nombre(jDto.getNombre().trim())
                        .apellido(jDto.getApellido().trim())
                        .dorsal(jDto.getDorsal())
                        .posicion(jDto.getPosicion())
                        .esCapitan(Boolean.TRUE.equals(jDto.getEsCapitan()))
                        .activo(true)
                        .suspendido(false)
                        .build();
                equipo.agregarJugador(jugador);
            }

            Equipo equipoGuardado = equipoRepository.save(equipo);
            equipoIdGenerado = equipoGuardado.getId();

            solicitud.setEstado(EstadoSolicitudInscripcion.ACEPTADA);
            solicitud.setMotivoRechazo(null);
            log.info("Equipo '{}' admitido con ID {}", equipoGuardado.getNombre(), equipoIdGenerado);

        } else if (dto.getEstado() == EstadoSolicitudInscripcion.RECHAZADA) {
            solicitud.setEstado(EstadoSolicitudInscripcion.RECHAZADA);
            solicitud.setMotivoRechazo(dto.getMotivoRechazo() != null ? dto.getMotivoRechazo().trim() : "Rechazada por la administración");
            log.info("Solicitud ID {} rechazada con motivo: {}", solicitudId, solicitud.getMotivoRechazo());
        } else {
            throw new BusinessRuleException("El estado de resolución debe ser ACEPTADA o RECHAZADA");
        }

        SolicitudInscripcion actualizada = solicitudRepository.save(solicitud);
        return mapToResponseDTO(actualizada, equipoIdGenerado);
    }

    private void validarPlantelBuenaFe(Long torneoId, List<JugadorInscripcionDTO> jugadores) {
        if (jugadores == null || jugadores.size() < 15 || jugadores.size() > 22) {
            throw new BusinessRuleException("El plantel de buena fe debe tener entre 15 y 22 jugadores (cantidad recibida: "
                    + (jugadores != null ? jugadores.size() : 0) + ")");
        }

        Set<String> dnis = new HashSet<>();
        Set<Integer> dorsales = new HashSet<>();
        int arqueros = 0;
        int capitanes = 0;

        for (JugadorInscripcionDTO j : jugadores) {
            String dni = j.getDni().trim();
            if (!dnis.add(dni)) {
                throw new BusinessRuleException("DNI duplicado dentro del mismo plantel: " + dni);
            }
            if (!dorsales.add(j.getDorsal())) {
                throw new BusinessRuleException("Dorsal duplicado dentro del mismo plantel: " + j.getDorsal());
            }
            if (j.getPosicion() == PosicionJugador.ARQUERO) {
                arqueros++;
            }
            if (Boolean.TRUE.equals(j.getEsCapitan())) {
                capitanes++;
            }
        }

        if (arqueros < 1) {
            throw new BusinessRuleException("El plantel debe contar con al menos un jugador en la posición ARQUERO");
        }
        if (capitanes != 1) {
            throw new BusinessRuleException("El plantel debe tener exactamente un capitán designado (capitanes encontrados: " + capitanes + ")");
        }

        validarDnisNoInscriptosEnTorneo(torneoId, jugadores);
    }

    private void validarDnisNoInscriptosEnTorneo(Long torneoId, List<JugadorInscripcionDTO> jugadores) {
        List<String> dnis = jugadores.stream().map(j -> j.getDni().trim()).toList();
        boolean existeDniDuplicado = jugadorRepository.existsByTorneoIdAndDniIn(torneoId, dnis);
        if (existeDniDuplicado) {
            throw new ConflictoEstadoException("Uno o más jugadores ya se encuentran inscriptos en otro equipo del mismo torneo (RN-02)");
        }
    }

    private List<JugadorInscripcionDTO> deserializarPlantel(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<JugadorInscripcionDTO>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error al deserializar plantel desde JSON", e);
            throw new BusinessRuleException("Error al reconstruir el plantel de la solicitud");
        }
    }

    private SolicitudInscripcionResponseDTO mapToResponseDTO(SolicitudInscripcion s, Long equipoId) {
        return SolicitudInscripcionResponseDTO.builder()
                .id(s.getId())
                .torneoId(s.getTorneo().getId())
                .fanaticoUsuarioId(s.getFanaticoUsuarioId())
                .nombreEquipo(s.getNombreEquipo())
                .estado(s.getEstado())
                .fechaSolicitud(s.getFechaSolicitud())
                .motivoRechazo(s.getMotivoRechazo())
                .equipoId(equipoId)
                .build();
    }
}
