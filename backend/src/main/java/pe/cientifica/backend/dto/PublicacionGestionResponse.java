package pe.cientifica.backend.dto;

import pe.cientifica.backend.entity.EstadoPublicacion;
import pe.cientifica.backend.entity.Publicacion;

import java.time.LocalDateTime;

/**
 * Respuesta de los endpoints de escritura (crear, editar, cambiar estado y "mías").
 *
 * <p>Tiene la misma forma JSON que la respuesta de lectura acordada en el contrato de la API.
 * Del autor solo se expone el nombre: nunca el correo ni la contraseña.</p>
 *
 * <p>Se debe llamar a {@link #desde(Publicacion)} dentro de una transacción
 * (con {@code open-in-view=false} las relaciones LAZY no se pueden leer fuera del service).</p>
 */
public record PublicacionGestionResponse(
        Integer idPublicacion,
        String titulo,
        String descripcion,
        EstadoPublicacion estado,
        LocalDateTime fechaCreacion,
        Referencia sede,
        Referencia categoria,
        String autor
) {

    /** Par {@code id + nombre} usado para sede y categoría. */
    public record Referencia(Integer id, String nombre) {
    }

    /** Convierte una entidad en su respuesta pública. */
    public static PublicacionGestionResponse desde(Publicacion p) {
        return new PublicacionGestionResponse(
                p.getId(),
                p.getTitulo(),
                p.getDescripcion(),
                p.getEstado(),
                p.getFechaPublicacion(),
                new Referencia(p.getSede().getId(), p.getSede().getNombre()),
                new Referencia(p.getCategoria().getId(), p.getCategoria().getNombre()),
                p.getUsuario().getNombre() + " " + p.getUsuario().getApellido()
        );
    }
}
