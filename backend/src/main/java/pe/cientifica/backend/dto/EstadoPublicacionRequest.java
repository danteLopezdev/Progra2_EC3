package pe.cientifica.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import pe.cientifica.backend.entity.EstadoPublicacion;

/**
 * Cuerpo de {@code PATCH /api/publicaciones/{id}/estado}.
 * Ejemplo: {@code { "estado": "ATENDIDA" }}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EstadoPublicacionRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoPublicacion estado
) {
}
