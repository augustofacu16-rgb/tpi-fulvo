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

        Map<Long, Estadisticas> estadisticasPorEquipo = new LinkedHashMap<>();

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

            Estadisticas local = estadisticasPorEquipo.computeIfAbsent(
                    partido.getEquipoLocalId(),
                    ignored -> new Estadisticas()
            );
            Estadisticas visitante = estadisticasPorEquipo.computeIfAbsent(
                    partido.getEquipoVisitanteId(),
                    ignored -> new Estadisticas()
            );

            local.partidosJugados++;
            visitante.partidosJugados++;
            if (canceladoLiga) {
                local.partidosPerdidos++;
                visitante.partidosPerdidos++;
                continue;
            }

            int golesLocal = valorOCero(partido.getGolesLocal());
            int golesVisitante = valorOCero(partido.getGolesVisitante());
            local.golesAFavor += golesLocal;
            local.golesEnContra += golesVisitante;
            visitante.golesAFavor += golesVisitante;
            visitante.golesEnContra += golesLocal;

            if (partido.getGanadorEquipoId() == null) {
                local.partidosEmpatados++;
                visitante.partidosEmpatados++;
                local.puntos++;
                visitante.puntos++;
            } else if (partido.getGanadorEquipoId().equals(partido.getEquipoLocalId())) {
                local.partidosGanados++;
                visitante.partidosPerdidos++;
                local.puntos += 3;
            } else {
                visitante.partidosGanados++;
                local.partidosPerdidos++;
                visitante.puntos += 3;
            }
        }

        for (TablaPosicion tabla : tablaPosicionRepository.findAllByTorneoId(torneoId)) {
            resetear(tabla);
        }

        for (Map.Entry<Long, Estadisticas> entry : estadisticasPorEquipo.entrySet()) {
            TablaPosicion tabla = tablaPosicionRepository
                    .findByTorneoIdAndEquipoId(torneoId, entry.getKey())
                    .orElseGet(() -> new TablaPosicion(torneoId, entry.getKey()));

            aplicar(tabla, entry.getValue());
            tablaPosicionRepository.save(tabla);
        }
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

    private int valorOCero(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private void resetear(TablaPosicion tabla) {
        tabla.setPartidosJugados(0);
        tabla.setPartidosGanados(0);
        tabla.setPartidosEmpatados(0);
        tabla.setPartidosPerdidos(0);
        tabla.setGolesAFavor(0);
        tabla.setGolesEnContra(0);
        tabla.setDiferenciaGol(0);
        tabla.setPuntos(0);
    }

    private void aplicar(TablaPosicion tabla, Estadisticas estadisticas) {
        tabla.setPartidosJugados(estadisticas.partidosJugados);
        tabla.setPartidosGanados(estadisticas.partidosGanados);
        tabla.setPartidosEmpatados(estadisticas.partidosEmpatados);
        tabla.setPartidosPerdidos(estadisticas.partidosPerdidos);
        tabla.setGolesAFavor(estadisticas.golesAFavor);
        tabla.setGolesEnContra(estadisticas.golesEnContra);
        tabla.setDiferenciaGol(
                estadisticas.golesAFavor - estadisticas.golesEnContra
        );
        tabla.setPuntos(estadisticas.puntos);
    }

    private static class Estadisticas {
        int partidosJugados;
        int partidosGanados;
        int partidosEmpatados;
        int partidosPerdidos;
        int golesAFavor;
        int golesEnContra;
        int puntos;
    }
}
