package pe.cientifica.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.cientifica.backend.dto.ReporteRequest;
import pe.cientifica.backend.dto.ReporteResponse;
import pe.cientifica.backend.service.ReporteService;

/**
 * Permite a un usuario autenticado reportar una publicación (reporte de moderación).
 */
@RestController
@RequestMapping("/api/publicaciones")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    /**
     * Reporta una publicación.
     * Recibe: {"motivo": "..."}. El usuario sale del token y el estado inicial es PENDIENTE.
     * Devuelve: 201 con el reporte; 400 si el motivo es inválido; 404 si la publicación no existe;
     * 409 si el usuario ya la reportó.
     */
    @PostMapping("/{id}/reportes")
    public ResponseEntity<ReporteResponse> reportar(@PathVariable Integer id,
                                                    @Valid @RequestBody ReporteRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.reportar(id, request.motivo(), authentication));
    }
}
