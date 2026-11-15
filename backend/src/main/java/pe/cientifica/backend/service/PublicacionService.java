package pe.cientifica.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.cientifica.backend.dto.PublicacionResponse;
import pe.cientifica.backend.entity.Publicacion;
import pe.cientifica.backend.repository.PublicacionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de consulta de publicaciones: listado con filtros y detalle por id.
 *
 * <p>Los métodos son {@code @Transactional(readOnly = true)} porque
 * {@code open-in-view=false} y las relaciones LAZY deben leerse dentro del service.</p>
 */
@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;

    public PublicacionService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    /**
     * Lista las publicaciones, de la más reciente a la más antigua, con filtros opcionales.
     *
     * @param sede      id de la sede (opcional)
     * @param categoria id de la categoría (opcional)
     * @param desde     fecha inicial, inclusive (opcional)
     * @param hasta     fecha final, inclusive (opcional)
     * @param q         texto a buscar en título y descripción (opcional)
     */
    @Transactional(readOnly = true)
    public List<PublicacionResponse> listar(
            Integer sede,
            Integer categoria,
            LocalDate desde,
            LocalDate hasta,
            String q) {

        LocalDateTime fechaDesde =
                desde == null ? null : desde.atStartOfDay();

        // Incluye todo el día indicado en "hasta".
        LocalDateTime fechaHasta =
                hasta == null ? null : hasta.plusDays(1).atStartOfDay();

        return publicacionRepository.buscar(
                        sede, categoria, fechaDesde, fechaHasta, q)
                .stream()
                .map(PublicacionResponse::desde)
                .toList();
    }

    /**
     * Devuelve el detalle de una publicación.
     *
     * @throws ResponseStatusException 404 si no existe
     */
    @Transactional(readOnly = true)
    public PublicacionResponse obtenerPorId(Integer id) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Publicación no encontrada"
                ));

        return PublicacionResponse.desde(publicacion);
    }
}