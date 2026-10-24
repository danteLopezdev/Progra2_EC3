package pe.cientifica.backend.dto;

import pe.cientifica.backend.entity.EstadoPublicacion;
import pe.cientifica.backend.entity.EstadoReporte;
import pe.cientifica.backend.entity.Reporte;

import java.time.LocalDateTime;

/**
 * Reporte de moderación tal como se muestra al administrador.
 *
 * <p>Solo se exponen nombres de personas, nunca correos ni contraseñas.
 * Se debe llamar a {@link #desde(Reporte)} dentro de una transacción.</p>
 */
public record ReporteResponse(
        Integer idReporte,
        String motivo,
        EstadoReporte estado,
        LocalDateTime fechaReporte,
        String reportadoPor,
        PublicacionReportada publicacion
) {

    /** Datos mínimos de la publicación reportada. */
    public record PublicacionReportada(
            Integer idPublicacion,
            String titulo,
            EstadoPublicacion estado,
            String autor
    ) {
    }

    /** Convierte una entidad en su respuesta. */
    public static ReporteResponse desde(Reporte r) {
        var p = r.getPublicacion();
        return new ReporteResponse(
                r.getId(),
                r.getMotivo(),
                r.getEstado(),
                r.getFechaReporte(),
                r.getUsuario().getNombre() + " " + r.getUsuario().getApellido(),
                new PublicacionReportada(
                        p.getId(),
                        p.getTitulo(),
                        p.getEstado(),
                        p.getUsuario().getNombre() + " " + p.getUsuario().getApellido()
                )
        );
    }
}
