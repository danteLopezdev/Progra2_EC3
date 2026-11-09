
package pe.cientifica.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.cientifica.backend.entity.Publicacion;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicacionRepository
        extends JpaRepository<Publicacion, Integer> {

    @Query(value = """
        SELECT p.*
        FROM publicacion p
        WHERE (:sede IS NULL OR p.id_sede = :sede)
          AND (:categoria IS NULL OR p.id_categoria = :categoria)
          AND (:desde IS NULL OR p.fecha_publicacion >= :desde)
          AND (:hasta IS NULL OR p.fecha_publicacion < :hasta)
          AND (
              :q IS NULL
              OR TRIM(:q) = ''
              OR MATCH(p.titulo, p.descripcion)
                 AGAINST (:q IN NATURAL LANGUAGE MODE)
          )
        ORDER BY p.fecha_publicacion DESC
        """, nativeQuery = true)
    List<Publicacion> buscar(
            @Param("sede") Integer sede,
            @Param("categoria") Integer categoria,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("q") String q
    );
}
