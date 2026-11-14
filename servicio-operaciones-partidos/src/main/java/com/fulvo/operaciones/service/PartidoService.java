package com.fulvo.operaciones.service;

import com.fulvo.common.exception.ArbitroNoAsignadoException;
import com.fulvo.operaciones.client.TorneosApiClient;
import com.fulvo.common.exception.RecursoNoEncontradoException;
import com.fulvo.operaciones.dto.AsignarPartidoRequest;
import com.fulvo.operaciones.dto.ActuacionJugadorRequest;
import com.fulvo.operaciones.dto.RegistrarActaRequest;
import com.fulvo.operaciones.exception.ConflictoHorarioException;
import com.fulvo.operaciones.models.ActuacionJugador;
import com.fulvo.operaciones.models.Arbitro;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.models.ResultadoPartido;
import com.fulvo.operaciones.models.Sede;
import com.fulvo.operaciones.repository.ArbitroRepository;
import com.fulvo.operaciones.repository.ActuacionJugadorRepository;
import com.fulvo.operaciones.repository.PartidoRepository;
import com.fulvo.operaciones.repository.SedeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final SedeRepository sedeRepository;
    private final ArbitroRepository arbitroRepository;
    private final ActuacionJugadorRepository actuacionJugadorRepository;
    private final TablaPosicionService tablaPosicionService;
    private final BracketService bracketService;
    private final TorneosApiClient torneosApiClient;

    @Transactional
    public Partido iniciarPartido(Long partidoId, Jwt jwt) {
        Partido partido = partidoRepository.findByIdWithLock(partidoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Partido", partidoId));

        validarArbitro(partido, jwt);
        partido.iniciar();

        return partidoRepository.save(partido);
    }

    @Transactional
    public Partido cancelarPartido(Long partidoId, Jwt jwt) {
        Partido partido = partidoRepository.findByIdWithLock(partidoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Partido", partidoId));

        validarArbitro(partido, jwt);

        partido.cancelarPorIncidentes();

        Partido partidoCancelado = partidoRepository.save(partido);
        bracketService.procesarCancelacion(partidoCancelado);
        tablaPosicionService.recalcular(partido.getTorneoId());
        partidoRepository.flush();
        if ("LLAVES".equalsIgnoreCase(partido.getFormatoTorneo())) {
            torneosApiClient.descalificarPorIncidentes(partido.getTorneoId(),
                    partido.getEquipoLocalId(), partido.getEquipoVisitanteId());
        }

        return partidoCancelado;
    }


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

    @Transactional
    public Partido registrarActa(Long partidoId, RegistrarActaRequest request, Jwt jwt) {
        Partido partido = partidoRepository.findByIdWithLock(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Partido", partidoId));

        validarArbitro(partido, jwt);

        ResultadoPartido resultado = new ResultadoPartido(
                request.golesLocal(), request.golesVisitante(),
                request.penalesLocal(), request.penalesVisitante());
        partido.validarFinalizacion(resultado);

        int golesLocalRegistrados = 0;
        int golesVisitanteRegistrados = 0;

        for (ActuacionJugadorRequest actuacion : request.actuaciones()) {
            ActuacionJugador actuacionJugador = new ActuacionJugador(
                    partido, actuacion.equipoId(), actuacion.dorsalJugador(), actuacion.tipo());

            if (actuacion.tipo() == com.fulvo.operaciones.models.TipoActuacion.GOL) {
                if (actuacion.equipoId().equals(partido.getEquipoLocalId())) {
                    golesLocalRegistrados++;
                } else {
                    golesVisitanteRegistrados++;
                }
            }

            actuacionJugadorRepository.save(actuacionJugador);
        }

        if (golesLocalRegistrados != request.golesLocal()
                || golesVisitanteRegistrados != request.golesVisitante()) {
            throw new IllegalArgumentException(
                    "La cantidad de actuaciones de tipo GOL no coincide con el resultado"
            );
        }

        partido.finalizar(resultado);

        Partido partidoFinalizado = partidoRepository.save(partido);
        bracketService.promoverGanador(partidoFinalizado);
        tablaPosicionService.recalcular(partido.getTorneoId());
        return partidoFinalizado;
    }

    private void validarArbitro(Partido partido, Jwt jwt) {
        if (partido.getArbitro() == null) {
            throw new ArbitroNoAsignadoException(
                    "El partido no tiene un árbitro asignado"
            );
        }

        if (!jwt.getSubject().equals(partido.obtenerArbitroUsuarioId())) {
            throw new ArbitroNoAsignadoException(
                    "El usuario autenticado no es el árbitro asignado"
            );
        }
    }

}
