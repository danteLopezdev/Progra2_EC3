
package pe.cientifica.backend.dto;

import java.time.LocalDateTime;

public record PublicacionResponse(
        Integer id,
        String titulo,
        String descripcion,
        String imagen,
        LocalDateTime fechaPublicacion,
        String estado,
        Integer categoriaId,
        String categoriaNombre,
        Integer sedeId,
        String sedeNombre,
        String autorNombre
) {
}
