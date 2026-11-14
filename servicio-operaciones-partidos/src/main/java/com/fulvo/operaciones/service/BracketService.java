package com.fulvo.operaciones.service;

import com.fulvo.common.exception.ConflictoEstadoException;
import com.fulvo.operaciones.models.EstadoPartido;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.repository.PartidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BracketService {

    private final PartidoRepository partidoRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void promoverGanador(Partido partido) {
        if (!esLlaves(partido)) {
            return;
        }
        Long ganador = partido.getGanadorEquipoId();
        if (partido.getEstado() != EstadoPartido.FINALIZADO || ganador == null
                || (!ganador.equals(partido.getEquipoLocalId())
                    && !ganador.equals(partido.getEquipoVisitanteId()))) {
            throw new ConflictoEstadoException("La llave no tiene un resultado válido para promover al ganador");
        }
        propagarResolucion(partido);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void procesarCancelacion(Partido partido) {
        if (esLlaves(partido)) {
            if (partido.getEstado() != EstadoPartido.CANCELADO
                    || partido.getGanadorEquipoId() != null) {
                throw new ConflictoEstadoException("La llave debe estar cancelada sin ganador");
            }
            propagarResolucion(partido);
        }
    }

    private boolean esLlaves(Partido partido) {
        return "LLAVES".equalsIgnoreCase(partido.getFormatoTorneo());
    }

    private void propagarResolucion(Partido partido) {
        List<Long> faseActual = partidoRepository.findIdsLlavesPorFaseOrdenados(
                partido.getTorneoId(), partido.getFechaOFase());
        int indice = faseActual.indexOf(partido.getId());
        if (indice < 0) {
            throw new ConflictoEstadoException("El partido no pertenece a la fase del bracket");
        }
        List<Long> faseSiguiente = partidoRepository.findIdsLlavesPorFaseOrdenados(
                partido.getTorneoId(), partido.getFechaOFase() + 1);

        if (faseActual.size() == 1 && faseSiguiente.isEmpty()
                && !partidoRepository.existsByTorneoIdAndFormatoTorneoAndFechaOFaseGreaterThan(
                        partido.getTorneoId(), "LLAVES", partido.getFechaOFase())) {
            return;
        }
        if (faseActual.size() % 2 != 0 || faseSiguiente.size() != faseActual.size() / 2) {
            throw new ConflictoEstadoException("El bracket está incompleto");
        }

        Partido destino = partidoRepository.findByIdWithLock(faseSiguiente.get(indice / 2))
                .orElseThrow(() -> new ConflictoEstadoException("No existe el partido de destino"));
        if (destino.getEstado() != EstadoPartido.PROGRAMADO
                && destino.getEstado() != EstadoPartido.REPROGRAMADO) {
            throw new ConflictoEstadoException("El partido de destino ya no admite nuevos participantes");
        }

        boolean local = indice % 2 == 0;
        Long ganador = partido.getGanadorEquipoId();
        Long ocupante = local ? destino.getEquipoLocalId() : destino.getEquipoVisitanteId();
        Long rival = local ? destino.getEquipoVisitanteId() : destino.getEquipoLocalId();
        if ((ocupante != null && !ocupante.equals(ganador))
                || (ganador != null && Objects.equals(rival, ganador))) {
            throw new ConflictoEstadoException("El resultado entra en conflicto con el partido de destino");
        }
        if (local) {
            destino.setEquipoLocalId(ganador);
        } else {
            destino.setEquipoVisitanteId(ganador);
        }

        Partido vecino = partidoRepository.findById(faseActual.get(indice ^ 1))
                .orElseThrow(() -> new ConflictoEstadoException("No existe la llave vecina"));
        boolean vecinoResuelto = vecino.getEstado() == EstadoPartido.FINALIZADO
                || vecino.getEstado() == EstadoPartido.CANCELADO;
        if (vecinoResuelto && (partido.getGanadorEquipoId() == null
                || vecino.getGanadorEquipoId() == null)) {
            Long clasificado = ganador != null ? ganador : vecino.getGanadorEquipoId();
            destino.setGanadorEquipoId(clasificado);
            destino.setEstado(clasificado == null
                    ? EstadoPartido.CANCELADO : EstadoPartido.FINALIZADO);
            partidoRepository.save(destino);
            propagarResolucion(destino);
        } else {
            partidoRepository.save(destino);
        }
    }
}
