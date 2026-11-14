package com.fulvo.operaciones.models.strategy;

import com.fulvo.common.exception.EmpateNoPermitidoException;
import com.fulvo.common.exception.ConflictoEstadoException;
import com.fulvo.operaciones.models.ResultadoPartido;

public final class EstrategiaLlaves implements EstrategiaFormatoPartido {

    @Override
    public void validarCancelacion(Long localId, Long visitanteId) {
        if (localId == null || visitanteId == null) {
            throw new ConflictoEstadoException("La cancelación por incidentes requiere ambos equipos");
        }
    }

    @Override
    public void validarResultado(ResultadoPartido resultado) {
        if (resultado == null) {
            throw new IllegalArgumentException("El resultado es obligatorio");
        }
        if ((resultado.penalesLocal() == null) != (resultado.penalesVisitante() == null)) {
            throw new IllegalArgumentException("Debe indicar los penales de ambos equipos o de ninguno");
        }
        if (resultado.golesLocal().equals(resultado.golesVisitante())) {
            if (resultado.penalesLocal() == null
                    || resultado.penalesLocal().equals(resultado.penalesVisitante())) {
                throw new EmpateNoPermitidoException(
                        "Un partido de llaves empatado requiere penales con un ganador");
            }
        } else if (resultado.penalesLocal() != null) {
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
        return resultado.penalesLocal() > resultado.penalesVisitante() ? localId : visitanteId;
    }
}
