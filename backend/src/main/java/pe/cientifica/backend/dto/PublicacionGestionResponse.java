package pe.cientifica.backend.dto;

import pe.cientifica.backend.entity.EstadoPublicacion;
import pe.cientifica.backend.entity.Publicacion;

import java.time.LocalDateTime;

/**
 * Respuesta de los endpoints de escritura (crear, editar, cambiar estado y "mías").
 *
 * <p>Tiene exactamente los mismos campos JSON que {@link PublicacionResponse}
 * (respuesta de lectura), para que el frontend reciba una sola forma.
 * Del autor solo se expone el nombre: nunca el correo ni la contraseña.</p>
 *
 * <p>Se debe llamar a {@link #desde(Publicacion)} dentro de una transacción
 * (con {@code open-in-view=false} las relaciones LAZY no se pueden leer fuera del service).</p>
 */
public record PublicacionGestionResponse(
        Integer id,
        String titulo,
        String descripcion,
        String imagen,
        LocalDateTime fechaPublicacion,
        EstadoPublicacion estado,
        Integer categoriaId,
        String categoriaNombre,
        Integer sedeId,
        String sedeNombre,
        String autorNombre
) {

    /** Convierte una entidad en su respuesta pública. */
    public static PublicacionGestionResponse desde(Publicacion p) {
        return new PublicacionGestionResponse(
                p.getId(),
                p.getTitulo(),
                p.getDescripcion(),
                p.getImagen(),
                p.getFechaPublicacion(),
                p.getEstado(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre(),
                p.getSede().getId(),
                p.getSede().getNombre(),
                p.getUsuario().getNombre() + " " + p.getUsuario().getApellido()
        );
    }
}