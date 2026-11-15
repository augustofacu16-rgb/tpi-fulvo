package com.fulvo.operaciones.controller;

import com.fulvo.operaciones.dto.AsignarPartidoRequest;
import com.fulvo.operaciones.dto.AsignarPartidoResponse;
import com.fulvo.operaciones.dto.ReprogramarPartidoRequest;
import com.fulvo.operaciones.dto.ReprogramarPartidoResponse;
import com.fulvo.operaciones.dto.SchedulerClimaResponse;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.service.ClimaSchedulerService;
import com.fulvo.operaciones.service.PartidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fulvo.operaciones.dto.EstadoPartidoResponse;
import com.fulvo.operaciones.dto.ActaResponse;
import com.fulvo.operaciones.dto.RegistrarActaRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/v1/partidos")
@RequiredArgsConstructor
@Tag(name = "Partidos", description = "Asignación de recursos, arbitraje y contingencias climáticas de partidos")
@SecurityRequirement(name = "bearerAuth")
public class PartidoController {

    private final PartidoService partidoService;
    private final ClimaSchedulerService climaSchedulerService;

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Iniciar un partido",
            description = "Transiciona PROGRAMADO o REPROGRAMADO a EN_CURSO. Requiere ambos equipos definidos. El sub del JWT debe coincidir con el usuario del árbitro asignado. A través del Gateway requiere rol árbitro. No recibe body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operación realizada; ver el detalle de la respuesta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EstadoPartidoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o vencido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "No hay árbitro asignado, el JWT no corresponde a ese árbitro o el Gateway rechaza el rol",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Partido inexistente",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Estado incompatible o equipos todavía sin definir",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EstadoPartidoResponse> iniciarPartido(
            @Parameter(description = "Identificador del partido", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        Partido partido = partidoService.iniciarPartido(id, jwt);

        EstadoPartidoResponse response = new EstadoPartidoResponse(
                partido.getId(),
                partido.getEstado()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelacion")
    @Operation(summary = "Cancelar por incidentes",
            description = "Cancela un partido EN_CURSO. Requiere el JWT del árbitro asignado y rol árbitro en el Gateway. En Liga ambos equipos suman un partido jugado y perdido, sin puntos. En LLAVES se descalifica a ambos equipos mediante Torneos y se resuelven los avances sin rival. No recibe body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operación realizada; ver el detalle de la respuesta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EstadoPartidoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o vencido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "No hay árbitro asignado, el usuario no es el árbitro del partido o rol insuficiente en Gateway",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Partido inexistente",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Estado incompatible, participantes incompletos en LLAVES o conflicto de bracket",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "502", description = "No se pudo confirmar la descalificación en Torneos; se revierte la cancelación local",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EstadoPartidoResponse> cancelarPartido(
            @Parameter(description = "Identificador del partido", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        Partido partido = partidoService.cancelarPartido(id, jwt);

        EstadoPartidoResponse response = new EstadoPartidoResponse(
                partido.getId(),
                partido.getEstado()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/asignaciones")
    @Operation(summary = "Asignar sede, árbitro y horario",
            description = "Asigna o reemplaza la sede, el árbitro y la fecha del partido. Requiere rol organizador a través del Gateway. Rechaza otra asignación de la misma sede o árbitro a menos de dos horas del horario solicitado; exactamente dos horas de separación se permiten. Conserva el estado actual del partido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operación realizada; ver el detalle de la respuesta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AsignarPartidoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o vencido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Campos obligatorios ausentes o body inválido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente en el API Gateway",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Partido, sede o árbitro inexistente",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "La sede o el árbitro tienen otro partido en la ventana horaria",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<AsignarPartidoResponse> asignar(
            @Parameter(description = "Identificador del partido", example = "1") @PathVariable Long id,
            @Valid @RequestBody AsignarPartidoRequest request) {

        Partido partido = partidoService.asignar(id, request);

        AsignarPartidoResponse response = new AsignarPartidoResponse(
                partido.getId(),
                request.sedeId(),
                request.arbitroDni(),
                partido.getFechaHora(),
                partido.getEstado()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/scheduler-clima/ejecutar")
    @Operation(summary = "Ejecutar la evaluación climática",
            description = "Dispara manualmente la misma evaluación que ejecuta el scheduler automático. Considera partidos PROGRAMADO o REPROGRAMADO en las próximas 24 horas, con sede y coordenadas. Ante clima adverso vuelve a validar los datos bajo bloqueo, suspende y sincroniza con Torneos, que notifica a los delegados. Si Clima o la sincronización fallan para un partido, continúa con los demás y no lo cuenta como suspendido. Un 200 indica que terminó la evaluación, no que todas las consultas externas fueron exitosas. Requiere organizador en el Gateway. No recibe body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operación realizada; ver el detalle de la respuesta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SchedulerClimaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o vencido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente en el API Gateway",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<SchedulerClimaResponse> ejecutarSchedulerClima() {
        return ResponseEntity.ok(climaSchedulerService.evaluarPartidosProximas24Horas());
    }

    @PostMapping("/{id}/reprogramaciones")
    @Operation(summary = "Reprogramar un partido suspendido",
            description = "Transiciona SUSPENDIDO a REPROGRAMADO con sede y fecha futura. Conserva el árbitro asignado y comprueba disponibilidad de sede y árbitro, si lo tiene, con separación mínima de dos horas. Sincroniza la fecha y sede con Torneos. Requiere organizador a través del Gateway.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operación realizada; ver el detalle de la respuesta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReprogramarPartidoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o vencido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Sede inválida, fecha no futura o campos obligatorios ausentes",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente en el API Gateway",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Partido, sede o árbitro inexistente",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "El partido no está SUSPENDIDO o existe superposición horaria",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "502", description = "Falló la sincronización con Torneos; se conservan el estado, sede y fecha locales anteriores",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ReprogramarPartidoResponse> reprogramar(
            @Parameter(description = "Identificador del partido suspendido", example = "1") @PathVariable Long id,
            @Valid @RequestBody ReprogramarPartidoRequest request) {

        Partido partido = partidoService.reprogramar(id, request);

        ReprogramarPartidoResponse response = new ReprogramarPartidoResponse(
                partido.getId(),
                partido.getSede().getId(),
                partido.getSede().getNombre(),
                partido.getFechaHora(),
                partido.getEstado()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/actas")
    @Operation(summary = "Registrar el acta y finalizar el partido",
            description = "Requiere un partido EN_CURSO y el JWT del árbitro asignado, con rol árbitro en Gateway. Las actuaciones GOL de cada equipo deben coincidir con sus goles y todas las actuaciones deben pertenecer a un equipo del partido. En Liga se permite empate y no se admiten penales. En LLAVES un empate requiere penales con ganador; fuera de un empate no se admiten penales. Finaliza el partido, promueve al ganador cuando corresponde y recalcula posiciones en una transacción local.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operación realizada; ver el detalle de la respuesta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o vencido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Resultado, penales o actuaciones inválidos; goles registrados incompatibles con el marcador",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "No hay árbitro asignado, el JWT pertenece a otro usuario o rol insuficiente en Gateway",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Partido inexistente",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "El partido no está EN_CURSO o la promoción entra en conflicto con el bracket",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ActaResponse> registrarActa(
            @Parameter(description = "Identificador del partido en curso", example = "1") @PathVariable Long id,
            @Valid @RequestBody RegistrarActaRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        Partido partido = partidoService.registrarActa(id, request, jwt);

        ActaResponse response = new ActaResponse(
                partido.getId(),
                partido.getEstado(),
                partido.getGolesLocal(),
                partido.getGolesVisitante(),
                partido.getPenalesLocal(),
                partido.getPenalesVisitante(),
                partido.getGanadorEquipoId()
        );

        return ResponseEntity.ok(response);
    }
}
