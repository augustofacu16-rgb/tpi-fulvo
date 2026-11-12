package com.fulvo.operaciones.service;

import com.fulvo.common.exception.RecursoNoEncontradoException;
import com.fulvo.operaciones.dto.AsignarPartidoRequest;
import com.fulvo.operaciones.exception.ConflictoHorarioException;
import com.fulvo.operaciones.models.Arbitro;
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
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Partido", partidoId));

        Sede sede = sedeRepository.findById(request.sedeId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Sede", request.sedeId()));

        Arbitro arbitro = arbitroRepository.findById(request.arbitroDni())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Arbitro", request.arbitroDni()));

        LocalDateTime desde = request.fechaHora().minusHours(2);
        LocalDateTime hasta = request.fechaHora().plusHours(2);

        boolean sedeOcupada = partidoRepository
                .countSedeConflictos(
                        sede.getId(), desde, hasta, partidoId) > 0;

        if (sedeOcupada) {
            throw new ConflictoHorarioException(
                    "La sede ya está asignada a otro partido en la ventana horaria indicada"
            );
        }

        boolean arbitroOcupado = partidoRepository
                .countArbitroConflictos(
                        arbitro.getDni(), desde, hasta, partidoId) > 0;

        if (arbitroOcupado) {
            throw new ConflictoHorarioException(
                    "El árbitro ya está asignado a otro partido en la ventana horaria indicada"
            );
        }

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
}
