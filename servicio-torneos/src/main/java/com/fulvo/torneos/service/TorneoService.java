package com.fulvo.torneos.service;

import com.fulvo.common.exception.BusinessRuleException;
import com.fulvo.common.exception.RecursoNoEncontradoException;
import com.fulvo.torneos.domain.EstadoEquipo;
import com.fulvo.torneos.domain.EstadoTorneo;
import com.fulvo.torneos.domain.FormatoTorneo;
import com.fulvo.torneos.domain.Torneo;
import com.fulvo.torneos.dto.CrearTorneoDTO;
import com.fulvo.torneos.dto.TorneoResponseDTO;
import com.fulvo.torneos.repository.EquipoRepository;
import com.fulvo.torneos.repository.TorneoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TorneoService {

    private final TorneoRepository torneoRepository;
    private final EquipoRepository equipoRepository;

    @Transactional
    public TorneoResponseDTO crearTorneo(CrearTorneoDTO dto) {
        log.info("Creando nuevo torneo: '{}', formato: {}, cupo: {}", dto.getNombre(), dto.getFormato(), dto.getCupoEquipos());

        validarCupoPorFormato(dto.getFormato(), dto.getCupoEquipos());

        Torneo torneo = Torneo.builder()
                .nombre(dto.getNombre().trim())
                .formato(dto.getFormato())
                .cupoEquipos(dto.getCupoEquipos())
                .estado(EstadoTorneo.ABIERTO_INSCRIPCION)
                .fechaInicio(dto.getFechaInicio())
                .build();

        Torneo guardado = torneoRepository.save(torneo);
        log.info("Torneo creado exitosamente con ID: {}", guardado.getId());

        return mapToResponseDTO(guardado, 0);
    }

    @Transactional(readOnly = true)
    public List<TorneoResponseDTO> listarTorneos() {
        return torneoRepository.findAllConConteoEquipos();
    }

    @Transactional(readOnly = true)
    public TorneoResponseDTO obtenerTorneoPorId(Long id) {
        return torneoRepository.findTorneoDTOById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado con ID: " + id));
    }

    private void validarCupoPorFormato(FormatoTorneo formato, Integer cupo) {
        if (formato == FormatoTorneo.LIGA) {
            if (cupo != 10 && cupo != 20) {
                throw new BusinessRuleException("El cupo de equipos para Liga debe ser exactamente 10 o 20");
            }
        } else if (formato == FormatoTorneo.LLAVES) {
            if (cupo != 8 && cupo != 16) {
                throw new BusinessRuleException("El cupo de equipos para Llaves debe ser exactamente 8 o 16");
            }
        }
    }

    private TorneoResponseDTO mapToResponseDTO(Torneo torneo, int equiposInscriptos) {
        return TorneoResponseDTO.builder()
                .id(torneo.getId())
                .nombre(torneo.getNombre())
                .formato(torneo.getFormato())
                .cupoEquipos(torneo.getCupoEquipos())
                .equiposInscriptos(equiposInscriptos)
                .estado(torneo.getEstado())
                .fechaInicio(torneo.getFechaInicio())
                .fechaCreacion(torneo.getFechaCreacion())
                .build();
    }
}
