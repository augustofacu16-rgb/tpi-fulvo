package com.fulvo.operaciones.service;

import com.fulvo.operaciones.dto.GoleadorResponse;
import com.fulvo.operaciones.dto.PosicionResponse;
import com.fulvo.operaciones.models.ActuacionJugador;
import com.fulvo.operaciones.models.EstadoPartido;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.models.TablaPosicion;
import com.fulvo.operaciones.models.TipoActuacion;
import com.fulvo.operaciones.repository.PartidoRepository;
import com.fulvo.operaciones.repository.TablaPosicionRepository;
import com.fulvo.operaciones.repository.ActuacionJugadorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TablaPosicionService {

    private final PartidoRepository partidoRepository;
    private final TablaPosicionRepository tablaPosicionRepository;
    private final ActuacionJugadorRepository actuacionJugadorRepository;

    @Transactional
    public void recalcular(Long torneoId) {
        List<Partido> partidosComputables = partidoRepository
                .findAllByTorneoIdAndEstadoIn(torneoId,
                        List.of(EstadoPartido.FINALIZADO, EstadoPartido.CANCELADO));

        Map<Long, TablaPosicion> posicionesPorEquipo = new LinkedHashMap<>();
        for (TablaPosicion posicion : tablaPosicionRepository.findAllByTorneoId(torneoId)) {
            posicion.reiniciarEstadisticas();
            posicionesPorEquipo.put(posicion.getEquipoId(), posicion);
        }

        for (Partido partido : partidosComputables) {
            boolean canceladoLiga = partido.getEstado() == EstadoPartido.CANCELADO
                    && "LIGA".equalsIgnoreCase(partido.getFormatoTorneo());
            if (partido.getEquipoLocalId() == null
                    || partido.getEquipoVisitanteId() == null
                    || (partido.getEstado() == EstadoPartido.CANCELADO && !canceladoLiga)
                    || (!canceladoLiga && (partido.getGolesLocal() == null
                        || partido.getGolesVisitante() == null))) {
                continue;
            }

            TablaPosicion local = posicionesPorEquipo.computeIfAbsent(
                    partido.getEquipoLocalId(), equipoId -> new TablaPosicion(torneoId, equipoId));
            TablaPosicion visitante = posicionesPorEquipo.computeIfAbsent(
                    partido.getEquipoVisitanteId(), equipoId -> new TablaPosicion(torneoId, equipoId));

            if (canceladoLiga) {
                local.registrarDerrotaPorIncidentes();
                visitante.registrarDerrotaPorIncidentes();
                continue;
            }

            int golesLocal = partido.getGolesLocal();
            int golesVisitante = partido.getGolesVisitante();
            if (partido.getGanadorEquipoId() == null) {
                local.registrarEmpate(golesLocal, golesVisitante);
                visitante.registrarEmpate(golesVisitante, golesLocal);
            } else if (partido.getGanadorEquipoId().equals(partido.getEquipoLocalId())) {
                local.registrarVictoria(golesLocal, golesVisitante);
                visitante.registrarDerrota(golesVisitante, golesLocal);
            } else {
                visitante.registrarVictoria(golesVisitante, golesLocal);
                local.registrarDerrota(golesLocal, golesVisitante);
            }
        }
        tablaPosicionRepository.saveAll(posicionesPorEquipo.values());
    }

    @Transactional
    public List<PosicionResponse> obtenerPosiciones(Long torneoId) {
        return tablaPosicionRepository
                .findAllByTorneoIdOrderByPuntosDescDiferenciaGolDescGolesAFavorDesc(
                        torneoId
                )
                .stream()
                .map(tabla -> new PosicionResponse(
                        tabla.getEquipoId(),
                        tabla.getPartidosJugados(),
                        tabla.getPartidosGanados(),
                        tabla.getPartidosEmpatados(),
                        tabla.getPartidosPerdidos(),
                        tabla.getGolesAFavor(),
                        tabla.getGolesEnContra(),
                        tabla.getDiferenciaGol(),
                        tabla.getPuntos()
                ))
                .toList();
    }

    @Transactional
    public List<GoleadorResponse> obtenerGoleadores(Long torneoId) {
        Map<String, Integer> golesPorJugador = new LinkedHashMap<>();

        List<ActuacionJugador> goles = actuacionJugadorRepository
                .findAllByTipoAndPartido_TorneoIdAndPartido_Estado(
                        TipoActuacion.GOL,
                        torneoId,
                        EstadoPartido.FINALIZADO
                );

        for (ActuacionJugador gol : goles) {
            String clave = gol.getEquipoId() + ":" + gol.getDorsalJugador();
            golesPorJugador.merge(clave, 1, Integer::sum);
        }

        return golesPorJugador.entrySet()
                .stream()
                .map(entry -> {
                    String[] partes = entry.getKey().split(":");
                    return new GoleadorResponse(
                            Long.valueOf(partes[0]),
                            Integer.valueOf(partes[1]),
                            entry.getValue()
                    );
                })
                .sorted((primero, segundo) ->
                        segundo.goles().compareTo(primero.goles()))
                .toList();
    }

}
