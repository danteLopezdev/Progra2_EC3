
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

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;

    public PublicacionService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    

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
                .map(this::convertirRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicacionResponse obtenerPorId(Integer id) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Publicación no encontrada"
                ));

        return convertirRespuesta(publicacion);
    }

    private PublicacionResponse convertirRespuesta(Publicacion p) {
        String autorNombre =
                p.getUsuario().getNombre() + " "
                        + p.getUsuario().getApellido();

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
                autorNombre
        );
    }
}
