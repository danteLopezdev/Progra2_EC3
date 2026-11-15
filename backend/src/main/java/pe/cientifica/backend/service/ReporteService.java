package pe.cientifica.backend.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.cientifica.backend.dto.ReporteResponse;
import pe.cientifica.backend.entity.EstadoReporte;
import pe.cientifica.backend.entity.Publicacion;
import pe.cientifica.backend.entity.Reporte;
import pe.cientifica.backend.entity.Usuario;
import pe.cientifica.backend.repository.PublicacionGestionRepository;
import pe.cientifica.backend.repository.ReporteRepository;

import java.util.List;

/**
 * Lógica de los reportes de moderación: que un estudiante reporte una publicación
 * y que el administrador los consulte y los resuelva.
 */
@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final PublicacionGestionRepository publicacionRepository;
    private final UsuarioActualService usuarioActual;

    public ReporteService(ReporteRepository reporteRepository,
                          PublicacionGestionRepository publicacionRepository,
                          UsuarioActualService usuarioActual) {
        this.reporteRepository = reporteRepository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioActual = usuarioActual;
    }

    /**
     * Registra el reporte de una publicación. El usuario sale del token y el estado inicial es PENDIENTE.
     *
     * @throws ResponseStatusException 404 si la publicación no existe; 409 si el usuario ya la reportó
     */
    @Transactional
    public ReporteResponse reportar(Integer idPublicacion, String motivo, Authentication authentication) {
        Usuario usuario = usuarioActual.obtener(authentication);
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

        if (reporteRepository.existsByUsuarioAndPublicacion(usuario, publicacion)) {
            throw yaReportada();
        }

        Reporte r = new Reporte();
        r.setUsuario(usuario);
        r.setPublicacion(publicacion);
        r.setMotivo(motivo.trim());
        r.setEstado(EstadoReporte.PENDIENTE);

        try {
            // saveAndFlush: si dos peticiones llegan a la vez, la restricción UNIQUE de la BD responde aquí.
            return ReporteResponse.desde(reporteRepository.saveAndFlush(r));
        } catch (DataIntegrityViolationException e) {
            throw yaReportada();
        }
    }

    /**
     * Lista los reportes para el administrador.
     *
     * @param estado texto opcional: PENDIENTE, REVISADO o DESCARTADO (sin distinguir mayúsculas)
     * @throws ResponseStatusException 400 si el estado no es válido
     */
    @Transactional(readOnly = true)
    public List<ReporteResponse> listar(String estado) {
        List<Reporte> reportes = (estado == null || estado.isBlank())
                ? reporteRepository.findAllByOrderByFechaReporteDesc()
                : reporteRepository.findByEstadoOrderByFechaReporteDesc(convertirEstado(estado));
        return reportes.stream().map(ReporteResponse::desde).toList();
    }

    /**
     * Marca un reporte como REVISADO o DESCARTADO.
     *
     * @throws ResponseStatusException 404 si no existe; 400 si se intenta volver a PENDIENTE
     */
    @Transactional
    public ReporteResponse actualizarEstado(Integer idReporte, EstadoReporte nuevoEstado) {
        if (nuevoEstado == EstadoReporte.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado debe ser REVISADO o DESCARTADO");
        }
        Reporte r = reporteRepository.findById(idReporte)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
        r.setEstado(nuevoEstado);
        return ReporteResponse.desde(reporteRepository.save(r));
    }

    // ---------- auxiliares ----------

    private EstadoReporte convertirEstado(String texto) {
        try {
            return EstadoReporte.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado inválido. Valores permitidos: PENDIENTE, REVISADO, DESCARTADO");
        }
    }

    private ResponseStatusException yaReportada() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Ya reportaste esta publicación");
    }
}
