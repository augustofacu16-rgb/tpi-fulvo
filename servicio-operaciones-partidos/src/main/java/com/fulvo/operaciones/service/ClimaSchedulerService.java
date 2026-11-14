package com.fulvo.operaciones.service;

import com.fulvo.common.dto.ClimaPronosticoDTO;
import com.fulvo.common.dto.NotificacionEmailDTO;
import com.fulvo.operaciones.client.ClimaApiClient;
import com.fulvo.operaciones.client.NotificacionesApiClient;
import com.fulvo.operaciones.dto.SchedulerClimaResponse;
import com.fulvo.operaciones.models.EstadoPartido;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.models.Sede;
import com.fulvo.operaciones.repository.PartidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de evaluar el pronóstico meteorológico para los partidos
 * programados dentro de las próximas 24 horas, suspender automáticamente aquellos
 * afectados por clima severo y notificar al delegado (FN-08, RN-07, RN-12).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClimaSchedulerService {

    private static final String TIPO_EVENTO_SUSPENSION = "PARTIDO_SUSPENDIDO";
    private static final String ASUNTO_ALERTA = "Alerta Meteorológica: Partido Suspendido por Tormenta";
    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PartidoRepository partidoRepository;
    private final ClimaApiClient climaApiClient;
    private final NotificacionesApiClient notificacionesApiClient;

    @Value("${notificaciones.delegado-fallback-email:delegado1@fulvo.com}")
    private String delegadoFallbackEmail;

    /**
     * Evalúa los partidos dentro de la ventana de 24 horas y suspende los que
     * presenten condición climática adversa según servicio-clima.
     *
     * @return Resumen con la cantidad de partidos evaluados y suspendidos.
     */
    @Transactional
    public SchedulerClimaResponse evaluarPartidosProximas24Horas() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite24h = ahora.plusHours(24);

        List<EstadoPartido> estadosEvaluables = List.of(
                EstadoPartido.PROGRAMADO,
                EstadoPartido.REPROGRAMADO
        );

        List<Partido> candidatos = partidoRepository.findPartidosEnVentanaClimatica(
                estadosEvaluables, ahora, limite24h
        );

        log.info("Scheduler climático iniciado: {} partido(s) en ventana de 24h ({} a {})",
                candidatos.size(), ahora, limite24h);

        List<Long> idsSuspendidos = new ArrayList<>();

        for (Partido partido : candidatos) {
            if (evaluarYSuspenderSiCorresponde(partido)) {
                idsSuspendidos.add(partido.getId());
            }
        }

        log.info("Scheduler climático finalizado: evaluados={}, suspendidos={}",
                candidatos.size(), idsSuspendidos.size());

        return new SchedulerClimaResponse(
                ahora,
                candidatos.size(),
                idsSuspendidos.size(),
                idsSuspendidos
        );
    }

    private boolean evaluarYSuspenderSiCorresponde(Partido partido) {
        Sede sede = partido.getSede();
        try {
            ClimaPronosticoDTO pronostico = climaApiClient.consultarPronostico(
                    sede.getLatitud(),
                    sede.getLongitud(),
                    partido.getFechaHora()
            );

            if (pronostico == null || !Boolean.TRUE.equals(pronostico.getCondicionAdversa())) {
                log.debug("Partido {} en sede '{}' sin condición climática adversa",
                        partido.getId(), sede.getNombre());
                return false;
            }

            partido.suspenderPorClima();
            partidoRepository.save(partido);

            log.warn("Partido {} suspendido preventivamente por clima adverso en sede '{}' (precipitación={}mm, desc='{}')",
                    partido.getId(), sede.getNombre(),
                    pronostico.getPrecipitacionMm(), pronostico.getDescripcion());

            notificarSuspension(partido, sede, pronostico);
            return true;
        } catch (Exception ex) {
            // Un fallo al consultar clima para un partido no debe abortar la evaluación del resto
            log.error("Error al evaluar el clima del partido {} en sede '{}': {}",
                    partido.getId(), sede.getNombre(), ex.getMessage(), ex);
            return false;
        }
    }

    private void notificarSuspension(Partido partido, Sede sede, ClimaPronosticoDTO pronostico) {
        String descripcionClima = pronostico.getDescripcion() != null
                ? pronostico.getDescripcion()
                : "Tormenta / precipitación severa";
        String precipitacion = pronostico.getPrecipitacionMm() != null
                ? String.format("%.1f mm", pronostico.getPrecipitacionMm())
                : "N/D";

        String cuerpo = String.format("""
                <p>Estimado/a delegado/a,</p>
                <p>Le informamos que el partido <strong>#%d</strong> programado para el <strong>%s</strong>
                en la sede <strong>%s</strong> (%s) ha sido <strong>SUSPENDIDO preventivamente</strong>
                debido a condiciones meteorológicas adversas.</p>
                <ul>
                    <li><strong>Condición prevista:</strong> %s</li>
                    <li><strong>Precipitación estimada:</strong> %s</li>
                </ul>
                <p>El organizador procederá a reprogramar el encuentro a la brevedad.</p>
                """,
                partido.getId(),
                partido.getFechaHora().format(FECHA_FORMATTER),
                sede.getNombre(),
                sede.getDireccion(),
                descripcionClima,
                precipitacion
        );

        NotificacionEmailDTO notificacion = NotificacionEmailDTO.builder()
                .destinatario(delegadoFallbackEmail)
                .asunto(ASUNTO_ALERTA)
                .cuerpo(cuerpo)
                .tipoEvento(TIPO_EVENTO_SUSPENSION)
                .fechaHora(LocalDateTime.now())
                .build();

        notificacionesApiClient.enviarNotificacion(notificacion);
    }
}
