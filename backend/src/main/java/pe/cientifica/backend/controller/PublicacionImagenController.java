package pe.cientifica.backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.cientifica.backend.dto.PublicacionResponse;
import pe.cientifica.backend.service.ImagenStorageService;
import pe.cientifica.backend.service.PublicacionImagenService;

import java.time.Duration;

/** Subida, eliminación y descarga de imágenes de publicaciones. */
@RestController
public class PublicacionImagenController {

    private final PublicacionImagenService service;
    private final ImagenStorageService storage;

    public PublicacionImagenController(PublicacionImagenService service, ImagenStorageService storage) {
        this.service = service;
        this.storage = storage;
    }

    /** Sube la imagen (campo multipart "archivo"). 200 con la publicación; 400, 403 o 404. */
    @PostMapping(path = "/api/publicaciones/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PublicacionResponse subir(@PathVariable Integer id,
                                     @RequestParam("archivo") MultipartFile archivo,
                                     Authentication authentication) {
        return service.subir(id, archivo, authentication);
    }

    /** Quita la imagen de una publicación propia. 204; 403 o 404. */
    @DeleteMapping("/api/publicaciones/{id}/imagen")
    public ResponseEntity<Void> quitar(@PathVariable Integer id, Authentication authentication) {
        service.quitar(id, authentication);
        return ResponseEntity.noContent().build();
    }

    /** Descarga pública de una imagen por su nombre. 404 si el nombre es inválido o no existe. */
    @GetMapping("/api/imagenes/{nombre}")
    public ResponseEntity<Resource> ver(@PathVariable String nombre) {
        Resource recurso = storage.cargar(nombre);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ImagenStorageService.tipoContenido(nombre)))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(recurso);
    }
}