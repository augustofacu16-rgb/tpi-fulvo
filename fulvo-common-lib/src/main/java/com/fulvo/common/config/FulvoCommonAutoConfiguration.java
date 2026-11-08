package com.fulvo.common.config;

import com.fulvo.common.exception.GlobalExceptionHandler;
import com.fulvo.common.filter.CorrelationIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Autoconfiguración de Spring Boot 3 para fulvo-common-lib.
 * Permite que cualquier microservicio que agregue la dependencia registre de forma transparente
 * el manejador de excepciones RFC 7807 y el filtro de correlación MDC sin requerir configuración manual.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class FulvoCommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
