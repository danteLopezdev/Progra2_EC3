package pe.cientifica.backend.dto;

import pe.cientifica.backend.entity.Publicacion;

import java.time.LocalDateTime;

/**
 * Respuesta única de publicaciones: la usan los endpoints de lectura y de escritura.
 * Del autor solo se expone el nombre: nunca el correo ni la contraseña.
 *
 * <p>Se debe llamar a {@link #desde(Publicacion)} dentro de una transacción
 * (con {@code open-in-view=false} las relaciones LAZY no se pueden leer fuera del service).</p>
 */
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

    /** Convierte una entidad en su respuesta pública. */
    public static PublicacionResponse desde(Publicacion p) {
        return new PublicacionResponse(
                p.getId(),
                p.getTitulo(),
                p.getDescripcion(),
                p.getImagen(),
                p.getFechaPublicacion(),
                p.getEstado().name(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre(),
                p.getSede().getId(),
                p.getSede().getNombre(),
                p.getUsuario().getNombre() + " " + p.getUsuario().getApellido()
        );
    }
}