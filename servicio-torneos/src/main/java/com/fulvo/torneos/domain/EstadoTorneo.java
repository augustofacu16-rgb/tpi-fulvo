package com.fulvo.torneos.domain;

/**
 * Estados del ciclo de vida del Torneo (Máquina de estados oficial).
 */
public enum EstadoTorneo {
    ABIERTO_INSCRIPCION,
    CERRADO_INSCRIPCION,
    INICIADO,
    FINALIZADO,
    CANCELADO
}
