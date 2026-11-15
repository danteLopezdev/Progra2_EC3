package pe.cientifica.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import pe.cientifica.backend.entity.EstadoReporte;

/**
 * Cuerpo de {@code PATCH /api/admin/reportes/{id}}.
 * Solo se aceptan {@code REVISADO} o {@code DESCARTADO}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReporteEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoReporte estado
) {
}
