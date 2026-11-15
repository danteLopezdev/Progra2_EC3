package pe.cientifica.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.cientifica.backend.dto.EstadoPublicacionRequest;
import pe.cientifica.backend.dto.PublicacionGestionResponse;
import pe.cientifica.backend.dto.PublicacionRequest;
import pe.cientifica.backend.service.PublicacionGestionService;

import java.util.List;

/**
 * Endpoints de escritura de publicaciones. Todos requieren token (ruta no pública en SecurityConfig).
 * El autor siempre es el usuario del token.
 */
@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionGestionController {

    private final PublicacionGestionService service;

    public PublicacionGestionController(PublicacionGestionService service) {
        this.service = service;
    }

    /**
     * Crea una publicación.
     * Recibe: titulo, descripcion, idSede, idCategoria.
     * Devuelve: 201 con la publicación creada; 400 si hay datos inválidos; 404 si sede o categoría no existen.
     */
    @PostMapping
    public ResponseEntity<PublicacionGestionResponse> crear(@Valid @RequestBody PublicacionRequest request,
                                                            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request, authentication));
    }

    /**
     * Edita una publicación propia.
     * Devuelve: 200 con la publicación; 403 si no es el autor; 404 si no existe.
     */
    @PutMapping("/{id}")
    public PublicacionGestionResponse editar(@PathVariable Integer id,
                                             @Valid @RequestBody PublicacionRequest request,
                                             Authentication authentication) {
        return service.editar(id, request, authentication);
    }

    /**
     * Elimina una publicación propia.
     * Devuelve: 204; 403 si no es el autor; 404 si no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, Authentication authentication) {
        service.eliminar(id, authentication);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cambia el estado de una publicación propia.
     * Recibe: {"estado": "VIGENTE" | "ATENDIDA" | "FINALIZADA"}.
     * Devuelve: 200 con la publicación; 400 si el estado es inválido; 403 si no es el autor; 404 si no existe.
     */
    @PatchMapping("/{id}/estado")
    public PublicacionGestionResponse cambiarEstado(@PathVariable Integer id,
                                                    @Valid @RequestBody EstadoPublicacionRequest request,
                                                    Authentication authentication) {
        return service.cambiarEstado(id, request.estado(), authentication);
    }

    /**
     * Lista las publicaciones del usuario autenticado ("Mis publicaciones").
     * Devuelve: 200 con la lista, de la más reciente a la más antigua.
     */
    @GetMapping("/mias")
    public List<PublicacionGestionResponse> mias(Authentication authentication) {
        return service.listarMias(authentication);
    }
}
