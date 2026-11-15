package com.fulvo.operaciones.service;

import com.fulvo.common.dto.ClimaPronosticoDTO;
import com.fulvo.operaciones.client.ClimaApiClient;
import com.fulvo.operaciones.dto.SchedulerClimaResponse;
import com.fulvo.operaciones.models.EstadoPartido;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.repository.PartidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClimaSchedulerService {
    private final PartidoRepository partidoRepository;
    private final ClimaApiClient climaApiClient;
    private final SuspensionClimaticaService suspensionClimaticaService;

    // No mantener una transacción ni entidades administradas durante la consulta HTTP.
    public SchedulerClimaResponse evaluarPartidosProximas24Horas() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Partido> candidatos = partidoRepository.findPartidosEnVentanaClimatica(
                List.of(EstadoPartido.PROGRAMADO, EstadoPartido.REPROGRAMADO), ahora, ahora.plusHours(24));
        List<Long> suspendidos = new ArrayList<>();
        for (Partido candidato : candidatos) {
            try {
                ClimaPronosticoDTO pronostico = climaApiClient.consultarPronostico(
                        candidato.getSede().getLatitud(), candidato.getSede().getLongitud(),
                        candidato.getFechaHora());
                if (pronostico != null && Boolean.TRUE.equals(pronostico.getCondicionAdversa())
                        && suspensionClimaticaService.suspenderSiVigente(candidato, pronostico.getDescripcion())) {
                    suspendidos.add(candidato.getId());
                }
            } catch (RuntimeException ex) {
                // El servicio transaccional ya revirtió este partido; los demás pueden continuar.
                log.error("No se pudo evaluar/sincronizar el partido {}. Se reintentará si sigue en ventana",
                        candidato.getId(), ex);
            }
        }
        return new SchedulerClimaResponse(ahora, candidatos.size(), suspendidos.size(), suspendidos);
    }
}
