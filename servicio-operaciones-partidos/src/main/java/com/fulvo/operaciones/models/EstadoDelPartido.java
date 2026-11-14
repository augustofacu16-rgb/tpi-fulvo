package com.fulvo.operaciones.models;

import com.fulvo.common.exception.ConflictoEstadoException;

public abstract class EstadoDelPartido {

    private static final EstadoDelPartido PROGRAMADO = new Programado();
    private static final EstadoDelPartido EN_CURSO = new EnCurso();
    private static final EstadoDelPartido REPROGRAMADO = new Reprogramado();
    private static final EstadoDelPartido SIN_OPERACIONES = new SinOperaciones();

    static EstadoDelPartido para(EstadoPartido estado) {
        if (estado == null) {
            throw new ConflictoEstadoException("El partido no tiene un estado definido");
        }
        return switch (estado) {
            case PROGRAMADO -> PROGRAMADO;
            case EN_CURSO -> EN_CURSO;
            case REPROGRAMADO -> REPROGRAMADO;
            case FINALIZADO, CANCELADO, SUSPENDIDO -> SIN_OPERACIONES;
        };
    }

    public void iniciar(Partido partido) {
        throw new ConflictoEstadoException("El partido no se encuentra en estado PROGRAMADO");
    }

    public void cancelarPorIncidentes(Partido partido) {
        throw new ConflictoEstadoException("Solo se pueden cancelar partidos EN_CURSO");
    }

    public void validarFinalizacion(Partido partido, ResultadoPartido resultado) {
        throw new ConflictoEstadoException("Solo se pueden registrar actas de partidos EN_CURSO");
    }

    public void finalizar(Partido partido, ResultadoPartido resultado) {
        throw new ConflictoEstadoException("Solo se pueden registrar actas de partidos EN_CURSO");
    }

    public void incorporarClasificado(Partido partido, Long equipoId, boolean comoLocal) {
        validarFormatoDeLlaves(partido);
        throw new ConflictoEstadoException(
                "El partido de destino ya no admite nuevos participantes");
    }

    public void resolverAvanceSinRival(Partido partido) {
        validarFormatoDeLlaves(partido);
        throw new ConflictoEstadoException(
                "El partido de destino ya no admite nuevos participantes");
    }

    private abstract static class ReceptorDeClasificados extends EstadoDelPartido {
        @Override
        public void incorporarClasificado(Partido partido, Long equipoId, boolean comoLocal) {
            validarFormatoDeLlaves(partido);
            Long ocupante = comoLocal ? partido.getEquipoLocalId() : partido.getEquipoVisitanteId();
            Long rival = comoLocal ? partido.getEquipoVisitanteId() : partido.getEquipoLocalId();
            if ((ocupante != null && !ocupante.equals(equipoId))
                    || (equipoId != null && equipoId.equals(rival))) {
                throw new ConflictoEstadoException(
                        "El resultado entra en conflicto con el partido de destino");
            }
            if (comoLocal) {
                partido.setEquipoLocalId(equipoId);
            } else {
                partido.setEquipoVisitanteId(equipoId);
            }
        }

        @Override
        public void resolverAvanceSinRival(Partido partido) {
            validarFormatoDeLlaves(partido);
            if (partido.getEquipoLocalId() != null && partido.getEquipoVisitanteId() != null) {
                throw new ConflictoEstadoException(
                        "Un partido con ambos equipos debe resolverse mediante un acta");
            }
            Long ganador = partido.getEquipoLocalId() != null
                    ? partido.getEquipoLocalId() : partido.getEquipoVisitanteId();
            partido.setGanadorEquipoId(ganador);
            partido.transicionarA(ganador == null
                    ? EstadoPartido.CANCELADO : EstadoPartido.FINALIZADO);
        }
    }

    private static final class Programado extends ReceptorDeClasificados {
        @Override
        public void iniciar(Partido partido) {
            if (partido.getEquipoLocalId() == null || partido.getEquipoVisitanteId() == null) {
                throw new ConflictoEstadoException("El partido todavía no tiene ambos equipos definidos");
            }
            partido.transicionarA(EstadoPartido.EN_CURSO);
        }
    }

    private static final class EnCurso extends EstadoDelPartido {
        @Override
        public void cancelarPorIncidentes(Partido partido) {
            partido.estrategiaFormato().validarCancelacion(
                    partido.getEquipoLocalId(), partido.getEquipoVisitanteId());
            partido.setGanadorEquipoId(null);
            partido.transicionarA(EstadoPartido.CANCELADO);
        }

        @Override
        public void finalizar(Partido partido, ResultadoPartido resultado) {
            validarFinalizacion(partido, resultado);
            Long ganador = partido.estrategiaFormato().calcularGanador(
                    resultado, partido.getEquipoLocalId(), partido.getEquipoVisitanteId());
            partido.registrarResultado(resultado, ganador);
            partido.transicionarA(EstadoPartido.FINALIZADO);
        }

        @Override
        public void validarFinalizacion(Partido partido, ResultadoPartido resultado) {
            partido.estrategiaFormato().validarResultado(resultado);
        }
    }

    private static void validarFormatoDeLlaves(Partido partido) {
        if (!"LLAVES".equalsIgnoreCase(partido.getFormatoTorneo())) {
            throw new ConflictoEstadoException("Solo los partidos LLAVES reciben clasificados");
        }
    }

    private static final class Reprogramado extends ReceptorDeClasificados {
    }

    private static final class SinOperaciones extends EstadoDelPartido {
    }
}
