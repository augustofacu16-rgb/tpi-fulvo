package com.fulvo.torneos.dto;

import com.fulvo.common.dto.CrucePartidoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixtureGeneradoResponseDTO {

    private Long torneoId;
    private String formato;
    private Integer totalFechasOFases;
    private Integer totalPartidos;
    private List<CrucePartidoDTO> partidos;
}
