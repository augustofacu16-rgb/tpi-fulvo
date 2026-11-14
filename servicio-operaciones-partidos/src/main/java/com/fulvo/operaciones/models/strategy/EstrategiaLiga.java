package com.fulvo.operaciones.models.strategy;

import com.fulvo.operaciones.models.ResultadoPartido;

public final class EstrategiaLiga implements EstrategiaFormatoPartido {

    @Override
    public void validarCancelacion(Long localId, Long visitanteId) {
    }

    @Override
    public void validarResultado(ResultadoPartido resultado) {
        if (resultado == null) {
            throw new IllegalArgumentException("El resultado es obligatorio");
        }
        if ((resultado.penalesLocal() == null) != (resultado.penalesVisitante() == null)) {
            throw new IllegalArgumentException("Debe indicar los penales de ambos equipos o de ninguno");
        }
        if (resultado.penalesLocal() != null) {
            throw new IllegalArgumentException("Los penales solo corresponden a un empate en formato LLAVES");
        }
    }

    @Override
    public Long calcularGanador(ResultadoPartido resultado, Long localId, Long visitanteId) {
        validarResultado(resultado);
        if (resultado.golesLocal() > resultado.golesVisitante()) {
            return localId;
        }
        if (resultado.golesVisitante() > resultado.golesLocal()) {
            return visitanteId;
        }
        return null;
    }
}
