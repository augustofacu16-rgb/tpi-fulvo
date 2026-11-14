package com.fulvo.torneos.dto;

import com.fulvo.common.dto.CrucePartidoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Respuesta de generación del fixture determinístico de cruces de partidos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixtureGeneradoResponseDTO {

    @Schema(description = "Identificador del torneo iniciado", example = "1")
    private Long torneoId;

    @Schema(description = "Formato del certamen (LIGA o LLAVES)", example = "LIGA")
    private String formato;

    @Schema(description = "Cantidad total de jornadas de liga o fases de copa", example = "9")
    private Integer totalFechasOFases;

    @Schema(description = "Número total de cruces generados determinísticamente", example = "45")
    private Integer totalPartidos;

    @Schema(description = "Listado completo de cruces despachados a Operaciones")
    private List<CrucePartidoDTO> partidos;
}
