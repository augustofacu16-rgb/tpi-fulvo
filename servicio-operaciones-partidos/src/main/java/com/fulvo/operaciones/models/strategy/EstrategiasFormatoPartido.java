package com.fulvo.operaciones.models.strategy;

public final class EstrategiasFormatoPartido {

    private static final EstrategiaFormatoPartido LIGA = new EstrategiaLiga();
    private static final EstrategiaFormatoPartido LLAVES = new EstrategiaLlaves();

    private EstrategiasFormatoPartido() {
    }

    public static EstrategiaFormatoPartido para(String formatoTorneo) {
        return "LLAVES".equalsIgnoreCase(formatoTorneo) ? LLAVES : LIGA;
    }
}
