package com.fulvo.operaciones.service;

import com.fulvo.common.exception.ConflictoEstadoException;
import com.fulvo.common.exception.RecursoNoEncontradoException;
import com.fulvo.operaciones.dto.AsignarPartidoRequest;
import com.fulvo.operaciones.dto.ReprogramarPartidoRequest;
import com.fulvo.operaciones.exception.ConflictoHorarioException;
import com.fulvo.operaciones.models.Arbitro;
import com.fulvo.operaciones.models.EstadoPartido;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.models.Sede;
import com.fulvo.operaciones.repository.ArbitroRepository;
import com.fulvo.operaciones.repository.PartidoRepository;
import com.fulvo.operaciones.repository.SedeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final SedeRepository sedeRepository;
    private final ArbitroRepository arbitroRepository;

    @Transactional
    public Partido asignar(Long partidoId, AsignarPartidoRequest request) {
        // 1. Bloqueo pesimista del partido para serializar modificaciones concurrentes sobre el mismo encuentro
        Partido partido = partidoRepository.findByIdWithLock(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Partido", partidoId));

        // 2. Orden determinístico de adquisición de bloqueos (Partido -> Sede -> Arbitro)
        // para prevenir deadlocks en concurrencia entre encuentros distintos.
        Sede sede = sedeRepository.findByIdWithLock(request.sedeId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Sede", request.sedeId()));

        Arbitro arbitro = arbitroRepository.findByIdWithLock(request.arbitroDni())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Arbitro", request.arbitroDni()));

        validarDisponibilidadHoraria(partidoId, sede.getId(), arbitro.getDni(), request.fechaHora());

        Arbitro arbitroAnterior = partido.getArbitro();
        if (arbitroAnterior != null && !arbitroAnterior.getDni().equals(arbitro.getDni())) {
            arbitroAnterior.setAsignadoPartido(false);
        }

        partido.setSede(sede);
        partido.setArbitro(arbitro);
        partido.setFechaHora(request.fechaHora());
        arbitro.setAsignadoPartido(true);

        return partidoRepository.save(partido);
    }

    /**
     * Reprograma un partido suspendido asignando una nueva sede y fecha/hora,
     * validando disponibilidad horaria de la sede y del árbitro (si tuviera uno)
     * y transicionando el encuentro al estado REPROGRAMADO (FN-08, RN-07).
     */
    @Transactional
    public Partido reprogramar(Long partidoId, ReprogramarPartidoRequest request) {
        Partido partido = partidoRepository.findByIdWithLock(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido", partidoId));

        if (partido.getEstado() != EstadoPartido.SUSPENDIDO) {
            throw new ConflictoEstadoException(
                    "Solo se pueden reprogramar partidos en estado SUSPENDIDO (estado actual: "
                            + partido.getEstado() + ")"
            );
        }

        Sede nuevaSede = sedeRepository.findByIdWithLock(request.nuevaSedeId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede", request.nuevaSedeId()));

        String arbitroDni = null;
        if (partido.getArbitro() != null) {
            Arbitro arbitro = arbitroRepository.findByIdWithLock(partido.getArbitro().getDni())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Arbitro", partido.getArbitro().getDni()));
            arbitroDni = arbitro.getDni();
        }

        validarDisponibilidadHoraria(partidoId, nuevaSede.getId(), arbitroDni, request.nuevaFechaHora());

        partido.setSede(nuevaSede);
        partido.setFechaHora(request.nuevaFechaHora());
        partido.setEstado(EstadoPartido.REPROGRAMADO);

        return partidoRepository.save(partido);
    }

    private void validarDisponibilidadHoraria(Long partidoId, Long sedeId, String arbitroDni, LocalDateTime fechaHora) {
        LocalDateTime desde = fechaHora.minusHours(2);
        LocalDateTime hasta = fechaHora.plusHours(2);

        if (partidoRepository.countSedeConflictos(sedeId, desde, hasta, partidoId) > 0) {
            throw new ConflictoHorarioException(
                    "La sede ya está asignada a otro partido en la ventana horaria indicada"
            );
        }

        if (arbitroDni != null && partidoRepository.countArbitroConflictos(arbitroDni, desde, hasta, partidoId) > 0) {
            throw new ConflictoHorarioException(
                    "El árbitro ya está asignado a otro partido en la ventana horaria indicada"
            );
        }
    }
}
