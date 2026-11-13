package com.fulvo.operaciones.service;

import com.fulvo.common.dto.CrucePartidoDTO;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.repository.PartidoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class PartidoLoteService {

    private final PartidoRepository partidoRepository;

    @Transactional
    public void procesarLote(List<CrucePartidoDTO> cruces) {
        List<Partido> partidos = cruces.stream()
                .map(this::convertirAPartido)
                .toList();

        partidoRepository.saveAll(partidos);

        log.info("Lote procesado con {} partidos", partidos.size());
    }

    private Partido convertirAPartido(CrucePartidoDTO cruce) {
        return new Partido(
                cruce.getTorneoId(),
                cruce.getFormatoTorneo(),
                cruce.getFechaOFase(),
                cruce.getEquipoLocalId(),
                cruce.getEquipoVisitanteId(),
                cruce.getFechaHora()
        );
    }

}
