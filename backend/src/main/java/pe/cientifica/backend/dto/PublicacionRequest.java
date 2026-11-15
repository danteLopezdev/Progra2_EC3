package pe.cientifica.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo para crear o editar una publicación.
 *
 * <p>No incluye autor, estado ni fecha: esos datos los decide el servidor.
 * La tabla {@code publicacion} no tiene columna de precio, por eso no se recibe.
 * Si el cliente los envía en el JSON, Jackson los ignora.</p>
 *
 * <p>Los largos máximos deben coincidir con los de {@code docs/publicaciones.sql}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PublicacionRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
        String descripcion,

        @NotNull(message = "La sede es obligatoria")
        Integer idSede,

        @NotNull(message = "La categoría es obligatoria")
        Integer idCategoria
) {
}
