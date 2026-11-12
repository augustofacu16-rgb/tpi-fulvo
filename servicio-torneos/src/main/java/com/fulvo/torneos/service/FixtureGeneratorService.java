package com.fulvo.torneos.service;

import com.fulvo.common.dto.CrucePartidoDTO;
import com.fulvo.torneos.domain.Equipo;
import com.fulvo.torneos.domain.FormatoTorneo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio de generación determinística de fixtures (Round Robin para Liga y Árbol para Llaves) (Subsección 2.2.2).
 */
@Slf4j
@Service
public class FixtureGeneratorService {

    /**
     * Genera el fixture completo según el formato del certamen.
     *
     * @param torneoId ID del torneo.
     * @param formato FormatoTorneo (LIGA o LLAVES).
     * @param equipos Lista de equipos admitidos (en estado ACTIVO).
     * @return Lista determinística de CrucePartidoDTO listos para despachar a Operaciones.
     */
    public List<CrucePartidoDTO> generarFixture(Long torneoId, FormatoTorneo formato, List<Equipo> equipos) {
        // Orden determinístico por ID para que la generación sea repetible
        List<Equipo> ordenados = new ArrayList<>(equipos);
        ordenados.sort(Comparator.comparing(Equipo::getId));

        if (formato == FormatoTorneo.LIGA) {
            return generarRoundRobinLiga(torneoId, ordenados);
        } else if (formato == FormatoTorneo.LLAVES) {
            return generarArbolLlaves(torneoId, ordenados);
        } else {
            throw new IllegalArgumentException("Formato de torneo no soportado: " + formato);
        }
    }

    /**
     * Algoritmo Round Robin (Berger tables) para certámenes en formato LIGA.
     * Para N equipos (N par: 10 o 20):
     * - Total de fechas: N - 1 (9 fechas para 10 equipos; 19 fechas para 20 equipos).
     * - Partidos por fecha: N / 2 (5 partidos por fecha para 10 equipos; 10 para 20 equipos).
     * - Total de partidos: N * (N - 1) / 2 (45 partidos para 10 equipos; 190 para 20 equipos).
     */
    private List<CrucePartidoDTO> generarRoundRobinLiga(Long torneoId, List<Equipo> equipos) {
        int n = equipos.size();
        int totalFechas = n - 1;
        int partidosPorFecha = n / 2;

        List<CrucePartidoDTO> cruces = new ArrayList<>();
        List<Long> equipoIds = new ArrayList<>(equipos.stream().map(Equipo::getId).toList());

        for (int fecha = 1; fecha <= totalFechas; fecha++) {
            for (int partido = 0; partido < partidosPorFecha; partido++) {
                int localIdx = partido;
                int visitanteIdx = n - 1 - partido;

                Long localId = equipoIds.get(localIdx);
                Long visitanteId = equipoIds.get(visitanteIdx);

                // Alternancia de localía según Tablas de Berger para distribuir equilibradamente
                // la condición de local y visitante entre todos los equipos a lo largo de las fechas:
                // - En el partido 0 (donde interviene el pivote fijo), se invierte en fechas pares.
                // - En los demás partidos de la jornada, se invierte en índices impares de partido.
                boolean invertirLocalia = (partido == 0) ? (fecha % 2 == 0) : (partido % 2 == 1);
                if (invertirLocalia) {
                    Long temp = localId;
                    localId = visitanteId;
                    visitanteId = temp;
                }

                cruces.add(CrucePartidoDTO.builder()
                        .torneoId(torneoId)
                        .formatoTorneo("LIGA")
                        .fechaOFase(fecha)
                        .equipoLocalId(localId)
                        .equipoVisitanteId(visitanteId)
                        .estado("PROGRAMADO")
                        .build());
            }

            // Rotación circular en sentido horario manteniendo fijo el elemento en la posición 0:
            // Elemento 0 fijo; el último elemento se mueve a la posición 1, desplazando el resto a la derecha.
            Long ultimo = equipoIds.remove(n - 1);
            equipoIds.add(1, ultimo);
        }

        log.info("Fixture Round Robin generado para Torneo {} (LIGA): {} fechas, {} partidos en total",
                torneoId, totalFechas, cruces.size());
        return cruces;
    }

    /**
     * Generación de árbol de emparejamiento para certámenes en formato LLAVES (eliminación directa).
     * Para 8 equipos (3 fases):
     * - Fase 1 (Cuartos de final): 4 partidos con cruces emparejados (1 vs 8, 2 vs 7, 3 vs 6, 4 vs 5).
     * - Fase 2 (Semifinales): 2 partidos con equipos en null (a definirse por ganadores de fase 1).
     * - Fase 3 (Final): 1 partido con equipos en null (a definirse por ganadores de fase 2).
     *
     * Para 16 equipos (4 fases):
     * - Fase 1 (Octavos de final): 8 partidos (1 vs 16, 2 vs 15, ...).
     * - Fase 2 (Cuartos de final): 4 partidos con equipos en null.
     * - Fase 3 (Semifinales): 2 partidos con equipos en null.
     * - Fase 4 (Final): 1 partido con equipos en null.
     */
    private List<CrucePartidoDTO> generarArbolLlaves(Long torneoId, List<Equipo> equipos) {
        int n = equipos.size(); // 8 o 16
        List<CrucePartidoDTO> cruces = new ArrayList<>();

        // Fase 1: Cruces iniciales con equipos conocidos emparejados por seeding (1 vs n, 2 vs n-1, ...)
        int partidosFase1 = n / 2;
        for (int i = 0; i < partidosFase1; i++) {
            Long localId = equipos.get(i).getId();
            Long visitanteId = equipos.get(n - 1 - i).getId();

            cruces.add(CrucePartidoDTO.builder()
                    .torneoId(torneoId)
                    .formatoTorneo("LLAVES")
                    .fechaOFase(1)
                    .equipoLocalId(localId)
                    .equipoVisitanteId(visitanteId)
                    .estado("PROGRAMADO")
                    .build());
        }

        // Fases posteriores: Partidos en el bracket con equipos en null
        int faseActual = 2;
        int partidosEnFase = partidosFase1 / 2;
        while (partidosEnFase >= 1) {
            for (int i = 0; i < partidosEnFase; i++) {
                cruces.add(CrucePartidoDTO.builder()
                        .torneoId(torneoId)
                        .formatoTorneo("LLAVES")
                        .fechaOFase(faseActual)
                        .equipoLocalId(null)
                        .equipoVisitanteId(null)
                        .estado("PROGRAMADO")
                        .build());
            }
            faseActual++;
            partidosEnFase /= 2;
        }

        log.info("Árbol de Llaves generado para Torneo {} (LLAVES): {} fases, {} partidos en total",
                torneoId, faseActual - 1, cruces.size());
        return cruces;
    }
}
