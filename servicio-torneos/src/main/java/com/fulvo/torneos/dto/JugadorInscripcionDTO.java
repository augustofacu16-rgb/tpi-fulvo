package com.fulvo.torneos.dto;

import com.fulvo.torneos.models.PosicionJugador;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Schema(description = "Datos de un jugador integrante de la lista de buena fe de un equipo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JugadorInscripcionDTO {

    @Schema(description = "DNI del jugador (debe ser único por torneo, RN-02)", example = "30111001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El DNI del jugador es obligatorio")
    private String dni;

    @Schema(description = "Nombre de pila del futbolista", example = "Lionel", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre del jugador es obligatorio")
    private String nombre;

    @Schema(description = "Apellido del futbolista", example = "Messi", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El apellido del jugador es obligatorio")
    private String apellido;

    @Schema(description = "Posición en el campo (ARQUERO, DEFENSOR, MEDIOCAMPISTA, DELANTERO)", example = "DELANTERO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La posición del jugador es obligatoria")
    private PosicionJugador posicion;

    @Schema(description = "Número de dorsal en la camiseta (único dentro del equipo)", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El dorsal del jugador es obligatorio")
    private Integer dorsal;

    @Schema(description = "Indica si el futbolista es designado capitán (debe haber exactamente 1 por plantel)", example = "true")
    @Builder.Default
    private Boolean esCapitan = false;
}
