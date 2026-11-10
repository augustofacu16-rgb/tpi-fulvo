package com.fulvo.torneos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilitación de procesamiento asíncrono para el despacho no bloqueante de notificaciones.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
