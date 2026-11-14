package com.fulvo.operaciones.service;

import com.fulvo.common.exception.ArbitroNoAsignadoException;
import com.fulvo.operaciones.client.TorneosApiClient;
import com.fulvo.common.exception.ConflictoEstadoException;
import com.fulvo.common.exception.EmpateNoPermitidoException;
import com.fulvo.common.exception.RecursoNoEncontradoException;
import com.fulvo.operaciones.dto.AsignarPartidoRequest;
import com.fulvo.operaciones.dto.ActuacionJugadorRequest;
import com.fulvo.operaciones.dto.RegistrarActaRequest;
import com.fulvo.operaciones.exception.ConflictoHorarioException;
import com.fulvo.operaciones.models.ActuacionJugador;
import com.fulvo.operaciones.models.Arbitro;
import com.fulvo.operaciones.models.EstadoPartido;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.models.Sede;
import com.fulvo.operaciones.repository.ArbitroRepository;
import com.fulvo.operaciones.repository.ActuacionJugadorRepository;
import com.fulvo.operaciones.repository.PartidoRepository;
import com.fulvo.operaciones.repository.SedeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fulvo.operaciones.dto.EstadoPartidoResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;

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

        if (partido.getArbitro() == null) {
            throw new ArbitroNoAsignadoException(
                    "El partido no tiene un árbitro asignado"
            );
        }

        String usuarioAutenticado = jwt.getSubject();
        String usuarioDelArbitro = partido.obtenerArbitroUsuarioId();

        if (!usuarioAutenticado.equals(usuarioDelArbitro)) {
            throw new ArbitroNoAsignadoException(
                    "El usuario autenticado no es el árbitro asignado"
            );
        }

        if (partido.getEstado() != EstadoPartido.PROGRAMADO) {
            throw new ConflictoEstadoException(
                    "El partido no se encuentra en estado PROGRAMADO"
            );
        }

        if (partido.getEquipoLocalId() == null || partido.getEquipoVisitanteId() == null) {
            throw new ConflictoEstadoException("El partido todavía no tiene ambos equipos definidos");
        }
        partido.setEstado(EstadoPartido.EN_CURSO);

        return partidoRepository.save(partido);
    }

    @Transactional
    public Partido cancelarPartido(Long partidoId, Jwt jwt) {
        Partido partido = partidoRepository.findByIdWithLock(partidoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Partido", partidoId));

        validarArbitro(partido, jwt);

        if (partido.getEstado() != EstadoPartido.EN_CURSO) {
            throw new ConflictoEstadoException(
                    "Solo se pueden cancelar partidos EN_CURSO"
            );
        }

        partido.setEstado(EstadoPartido.CANCELADO);
        partido.setGanadorEquipoId(null);

        Partido partidoCancelado = partidoRepository.save(partido);
        bracketService.procesarCancelacion(partidoCancelado);
        tablaPosicionService.recalcular(partido.getTorneoId());
        partidoRepository.flush();
        if ("LLAVES".equalsIgnoreCase(partido.getFormatoTorneo())) {
            if (partido.getEquipoLocalId() == null || partido.getEquipoVisitanteId() == null) {
                throw new ConflictoEstadoException("La cancelación por incidentes requiere ambos equipos");
            }
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

        if (partido.getEstado() != EstadoPartido.EN_CURSO) {
            throw new ConflictoEstadoException(
                    "Solo se pueden registrar actas de partidos EN_CURSO"
            );
        }

        validarPenales(request, partido);

        int golesLocalRegistrados = 0;
        int golesVisitanteRegistrados = 0;

        for (ActuacionJugadorRequest actuacion : request.actuaciones()) {
            validarEquipoDelPartido(partido, actuacion.equipoId());

            if (actuacion.tipo() == com.fulvo.operaciones.models.TipoActuacion.GOL) {
                if (actuacion.equipoId().equals(partido.getEquipoLocalId())) {
                    golesLocalRegistrados++;
                } else {
                    golesVisitanteRegistrados++;
                }
            }

            actuacionJugadorRepository.save(new ActuacionJugador(
                    partido,
                    actuacion.equipoId(),
                    actuacion.dorsalJugador(),
                    actuacion.tipo()
            ));
        }

        if (golesLocalRegistrados != request.golesLocal()
                || golesVisitanteRegistrados != request.golesVisitante()) {
            throw new IllegalArgumentException(
                    "La cantidad de actuaciones de tipo GOL no coincide con el resultado"
            );
        }

        partido.setGolesLocal(request.golesLocal());
        partido.setGolesVisitante(request.golesVisitante());
        partido.setPenalesLocal(request.penalesLocal());
        partido.setPenalesVisitante(request.penalesVisitante());
        partido.setGanadorEquipoId(calcularGanador(partido, request));
        partido.setEstado(EstadoPartido.FINALIZADO);

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

    private void validarEquipoDelPartido(Partido partido, Long equipoId) {
        if (!equipoId.equals(partido.getEquipoLocalId())
                && !equipoId.equals(partido.getEquipoVisitanteId())) {
            throw new IllegalArgumentException(
                    "La actuación pertenece a un equipo que no participa del partido"
            );
        }
    }

    private void validarPenales(RegistrarActaRequest request, Partido partido) {
        boolean soloUnPenal = (request.penalesLocal() == null)
                != (request.penalesVisitante() == null);

        if (soloUnPenal) {
            throw new IllegalArgumentException(
                    "Debe indicar los penales de ambos equipos o de ninguno"
            );
        }

        boolean empate = request.golesLocal().equals(request.golesVisitante());
        boolean esLlaves = "LLAVES".equalsIgnoreCase(partido.getFormatoTorneo());

        if (esLlaves && empate) {
            if (request.penalesLocal() == null
                    || request.penalesVisitante() == null
                    || request.penalesLocal().equals(request.penalesVisitante())) {
                throw new EmpateNoPermitidoException(
                        "Un partido de llaves empatado requiere penales con un ganador"
                );
            }
        } else if (request.penalesLocal() != null
                || request.penalesVisitante() != null) {
            throw new IllegalArgumentException(
                    "Los penales solo corresponden a un empate en formato LLAVES"
            );
        }
    }

    private Long calcularGanador(Partido partido, RegistrarActaRequest request) {
        if (request.golesLocal() > request.golesVisitante()) {
            return partido.getEquipoLocalId();
        }

        if (request.golesVisitante() > request.golesLocal()) {
            return partido.getEquipoVisitanteId();
        }

        if (request.penalesLocal() != null
                && request.penalesVisitante() != null) {
            if (request.penalesLocal() > request.penalesVisitante()) {
                return partido.getEquipoLocalId();
            }

            if (request.penalesVisitante() > request.penalesLocal()) {
                return partido.getEquipoVisitanteId();
            }
        }

        return null;
    }
}
