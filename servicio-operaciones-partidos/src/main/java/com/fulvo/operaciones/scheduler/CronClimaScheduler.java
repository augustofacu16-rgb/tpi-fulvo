package com.fulvo.operaciones.scheduler;

import com.fulvo.operaciones.service.ClimaSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Proceso automático desatendido (@Scheduled) que ejecuta periódicamente la
 * verificación climática de partidos a 24 horas vista (FN-08, RN-07).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CronClimaScheduler {

    private final ClimaSchedulerService climaSchedulerService;

    @Scheduled(fixedRateString = "${clima.scheduler.fixed-rate-ms:3600000}")
    public void verificarClimaPartidosProximos() {
        log.debug("Ejecutando tarea programada CronClimaScheduler");
        climaSchedulerService.evaluarPartidosProximas24Horas();
    }
}
