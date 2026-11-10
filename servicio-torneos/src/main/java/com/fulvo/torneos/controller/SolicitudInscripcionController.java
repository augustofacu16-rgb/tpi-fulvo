package com.fulvo.torneos.controller;

import com.fulvo.torneos.dto.CrearSolicitudInscripcionDTO;
import com.fulvo.torneos.dto.ResolverAdmisionDTO;
import com.fulvo.torneos.dto.SolicitudInscripcionResponseDTO;
import com.fulvo.torneos.service.SolicitudInscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SolicitudInscripcionController {

    private final SolicitudInscripcionService solicitudService;

    /**
     * Endpoint FN-04: Postulación de equipo con lista de buena fe (15-22 jugadores).
     * Devuelve HTTP 202 Accepted según especificación de la cátedra.
     */
    @PostMapping("/v1/torneos/{id}/solicitudes-inscripcion")
    public ResponseEntity<SolicitudInscripcionResponseDTO> postularEquipo(
            @PathVariable("id") Long torneoId,
            @RequestHeader(value = "X-User-Id", defaultValue = "fanatico-default-id") String fanaticoUsuarioId,
            @Valid @RequestBody CrearSolicitudInscripcionDTO dto) {

        SolicitudInscripcionResponseDTO response = solicitudService.postularEquipo(torneoId, fanaticoUsuarioId, dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Endpoint FN-05: Admisión o rechazo administrativo de solicitudes.
     * Devuelve HTTP 200 OK.
     */
    @PatchMapping("/v1/solicitudes-inscripcion/{id}/admision")
    public ResponseEntity<SolicitudInscripcionResponseDTO> resolverAdmision(
            @PathVariable("id") Long solicitudId,
            @Valid @RequestBody ResolverAdmisionDTO dto) {

        SolicitudInscripcionResponseDTO response = solicitudService.resolverAdmision(solicitudId, dto);
        return ResponseEntity.ok(response);
    }
}
