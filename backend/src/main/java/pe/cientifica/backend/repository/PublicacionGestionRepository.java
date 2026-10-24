package pe.cientifica.backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.cientifica.backend.entity.Publicacion;
import pe.cientifica.backend.entity.Usuario;

import java.util.List;

/**
 * Repository de escritura de publicaciones (zona de Lutber).
 *
 * <p>Es independiente de {@code PublicacionRepository} (zona de Dostin) para que
 * ambos puedan trabajar sin pisarse en el mismo archivo. Al final se pueden unificar.</p>
 */
public interface PublicacionGestionRepository extends JpaRepository<Publicacion, Integer> {

    /**
     * Publicaciones de un autor, de la más reciente a la más antigua.
     * El {@code EntityGraph} carga sede, categoría y autor en una sola consulta.
     */
    @EntityGraph(attributePaths = {"sede", "categoria", "usuario"})
    List<Publicacion> findByUsuarioOrderByFechaPublicacionDesc(Usuario usuario);
}
