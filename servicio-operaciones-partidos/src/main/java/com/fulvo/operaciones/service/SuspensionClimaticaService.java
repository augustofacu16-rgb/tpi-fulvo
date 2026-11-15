package com.fulvo.operaciones.service;

import com.fulvo.common.dto.SuspensionPartidoDTO;
import com.fulvo.operaciones.client.TorneosApiClient;
import com.fulvo.operaciones.models.EstadoPartido;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.repository.PartidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SuspensionClimaticaService {
    private final PartidoRepository partidoRepository;
    private final TorneosApiClient torneosApiClient;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean suspenderSiVigente(Partido evaluado, String motivo) {
        Partido actual = partidoRepository.findByIdWithLock(evaluado.getId()).orElse(null);
        LocalDateTime ahora = LocalDateTime.now();
        if (actual == null
                || (actual.getEstado() != EstadoPartido.PROGRAMADO && actual.getEstado() != EstadoPartido.REPROGRAMADO)
                || actual.getFechaHora() == null || actual.getFechaHora().isBefore(ahora)
                || actual.getFechaHora().isAfter(ahora.plusHours(24))
                || !Objects.equals(actual.getFechaHora(), evaluado.getFechaHora())
                || actual.getSede() == null
                || !Objects.equals(actual.getSede().getId(), evaluado.getSede().getId())
                || !Objects.equals(actual.getSede().getLatitud(), evaluado.getSede().getLatitud())
                || !Objects.equals(actual.getSede().getLongitud(), evaluado.getSede().getLongitud())) {
            return false;
        }
        actual.suspenderPorClima();
        partidoRepository.flush();
        torneosApiClient.notificarSuspensionPartido(actual.getTorneoId(),
                new SuspensionPartidoDTO(actual.getId(), actual.getEquipoLocalId(),
                        actual.getEquipoVisitanteId(), motivo == null ? "Clima adverso" : motivo,
                        actual.getFechaHora()));
        return true;
    }
}
