package pe.cientifica.backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.cientifica.backend.entity.EstadoReporte;
import pe.cientifica.backend.entity.Publicacion;
import pe.cientifica.backend.entity.Reporte;
import pe.cientifica.backend.entity.Usuario;

import java.util.List;

/**
 * Repository de reportes de moderación.
 */
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    /** Indica si el usuario ya reportó esa publicación (regla: un reporte por usuario y publicación). */
    boolean existsByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    /** Todos los reportes, del más reciente al más antiguo. */
    @EntityGraph(attributePaths = {"usuario", "publicacion", "publicacion.usuario"})
    List<Reporte> findAllByOrderByFechaReporteDesc();

    /** Reportes con un estado dado, del más reciente al más antiguo. */
    @EntityGraph(attributePaths = {"usuario", "publicacion", "publicacion.usuario"})
    List<Reporte> findByEstadoOrderByFechaReporteDesc(EstadoReporte estado);
}
