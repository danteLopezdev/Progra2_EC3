package pe.cientifica.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.cientifica.backend.dto.ReporteEstadoRequest;
import pe.cientifica.backend.dto.ReporteResponse;
import pe.cientifica.backend.service.PublicacionGestionService;
import pe.cientifica.backend.service.ReporteService;

import java.util.List;

/**
 * Moderación del administrador. Todas las rutas bajo {@code /api/admin/**}
 * quedan restringidas al rol ADMINISTRADOR por SecurityConfig
 * (401 sin token, 403 con token de estudiante).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ReporteService reporteService;
    private final PublicacionGestionService publicacionService;

    public AdminController(ReporteService reporteService, PublicacionGestionService publicacionService) {
        this.reporteService = reporteService;
        this.publicacionService = publicacionService;
    }

    /**
     * Lista los reportes de moderación.
     * Recibe: parámetro opcional {@code estado} (PENDIENTE, REVISADO o DESCARTADO).
     * Devuelve: 200 con la lista (más recientes primero); 400 si el estado es inválido.
     */
    @GetMapping("/reportes")
    public List<ReporteResponse> reportes(@RequestParam(required = false) String estado) {
        return reporteService.listar(estado);
    }

    /**
     * Marca un reporte como REVISADO o DESCARTADO.
     * Recibe: {"estado": "REVISADO" | "DESCARTADO"}.
     * Devuelve: 200 con el reporte; 400 si el estado no es válido; 404 si no existe.
     */
    @PatchMapping("/reportes/{id}")
    public ReporteResponse actualizarReporte(@PathVariable Integer id,
                                             @Valid @RequestBody ReporteEstadoRequest request) {
        return reporteService.actualizarEstado(id, request.estado());
    }

    /**
     * Retira (elimina) una publicación; sus reportes se borran en cascada.
     * Devuelve: 204; 404 si no existe.
     */
    @DeleteMapping("/publicaciones/{id}")
    public ResponseEntity<Void> retirarPublicacion(@PathVariable Integer id) {
        publicacionService.retirar(id);
        return ResponseEntity.noContent().build();
    }
}
