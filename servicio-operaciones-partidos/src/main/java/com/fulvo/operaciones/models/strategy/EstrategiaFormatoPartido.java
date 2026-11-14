package com.fulvo.operaciones.models.strategy;

import com.fulvo.operaciones.models.ResultadoPartido;

public interface EstrategiaFormatoPartido {

    void validarResultado(ResultadoPartido resultado);

    Long calcularGanador(ResultadoPartido resultado, Long localId, Long visitanteId);

    void validarCancelacion(Long localId, Long visitanteId);
}
