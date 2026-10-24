package pe.cientifica.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /api/publicaciones/{id}/reportes}.
 * El usuario sale del token y el estado inicial ({@code PENDIENTE}) lo pone el servidor.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReporteRequest(

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
        String motivo
) {
}
