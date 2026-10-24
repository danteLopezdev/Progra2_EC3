package pe.cientifica.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.cientifica.backend.dto.PublicacionGestionResponse;
import pe.cientifica.backend.dto.PublicacionRequest;
import pe.cientifica.backend.entity.Categoria;
import pe.cientifica.backend.entity.EstadoPublicacion;
import pe.cientifica.backend.entity.Publicacion;
import pe.cientifica.backend.entity.Sede;
import pe.cientifica.backend.entity.Usuario;
import pe.cientifica.backend.repository.CategoriaRepository;
import pe.cientifica.backend.repository.PublicacionGestionRepository;
import pe.cientifica.backend.repository.SedeRepository;

import java.util.List;

/**
 * Lógica de escritura de publicaciones: crear, editar, eliminar, cambiar estado,
 * listar las propias y retirar (administrador).
 *
 * <p>Las entidades se convierten a DTO dentro de este service, en transacción,
 * porque {@code open-in-view=false}.</p>
 */
@Service
public class PublicacionGestionService {

    private final PublicacionGestionRepository publicacionRepository;
    private final SedeRepository sedeRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioActualService usuarioActual;

    public PublicacionGestionService(PublicacionGestionRepository publicacionRepository,
                                     SedeRepository sedeRepository,
                                     CategoriaRepository categoriaRepository,
                                     UsuarioActualService usuarioActual) {
        this.publicacionRepository = publicacionRepository;
        this.sedeRepository = sedeRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioActual = usuarioActual;
    }

    /**
     * Crea una publicación. El autor sale del token, el estado inicial es VIGENTE
     * y la fecha la pone el servidor.
     *
     * @throws ResponseStatusException 404 si la sede o la categoría no existen
     */
    @Transactional
    public PublicacionGestionResponse crear(PublicacionRequest request, Authentication authentication) {
        Usuario autor = usuarioActual.obtener(authentication);

        Publicacion p = new Publicacion();
        p.setUsuario(autor);
        p.setEstado(EstadoPublicacion.VIGENTE);
        aplicarDatos(p, request);

        return PublicacionGestionResponse.desde(publicacionRepository.save(p));
    }

    /**
     * Edita título, descripción, sede y categoría. No toca autor, estado ni fecha.
     *
     * @throws ResponseStatusException 404 si no existe; 403 si el usuario no es el autor
     */
    @Transactional
    public PublicacionGestionResponse editar(Integer id, PublicacionRequest request, Authentication authentication) {
        Publicacion p = buscarDelAutor(id, authentication);
        aplicarDatos(p, request);
        return PublicacionGestionResponse.desde(publicacionRepository.save(p));
    }

    /**
     * Elimina una publicación propia (sus reportes se borran en cascada en la BD).
     *
     * @throws ResponseStatusException 404 si no existe; 403 si el usuario no es el autor
     */
    @Transactional
    public void eliminar(Integer id, Authentication authentication) {
        publicacionRepository.delete(buscarDelAutor(id, authentication));
    }

    /**
     * Cambia el estado (VIGENTE, ATENDIDA o FINALIZADA) de una publicación propia.
     *
     * @throws ResponseStatusException 404 si no existe; 403 si el usuario no es el autor
     */
    @Transactional
    public PublicacionGestionResponse cambiarEstado(Integer id, EstadoPublicacion estado, Authentication authentication) {
        Publicacion p = buscarDelAutor(id, authentication);
        p.setEstado(estado);
        return PublicacionGestionResponse.desde(publicacionRepository.save(p));
    }

    /** Publicaciones del usuario autenticado, de la más reciente a la más antigua. */
    @Transactional(readOnly = true)
    public List<PublicacionGestionResponse> listarMias(Authentication authentication) {
        Usuario autor = usuarioActual.obtener(authentication);
        return publicacionRepository.findByUsuarioOrderByFechaPublicacionDesc(autor).stream()
                .map(PublicacionGestionResponse::desde)
                .toList();
    }

    /**
     * Retira (elimina) cualquier publicación. Solo lo invoca el panel de administración,
     * protegido por {@code /api/admin/**}. Sus reportes se borran en cascada.
     *
     * @throws ResponseStatusException 404 si no existe
     */
    @Transactional
    public void retirar(Integer id) {
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));
        publicacionRepository.delete(p);
    }

    // ---------- auxiliares ----------

    /** Copia los datos editables del request a la entidad, validando que sede y categoría existan. */
    private void aplicarDatos(Publicacion p, PublicacionRequest request) {
        Sede sede = sedeRepository.findById(request.idSede())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La sede indicada no existe"));
        Categoria categoria = categoriaRepository.findById(request.idCategoria())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría indicada no existe"));

        p.setTitulo(request.titulo().trim());
        p.setDescripcion(request.descripcion().trim());
        p.setSede(sede);
        p.setCategoria(categoria);
    }

    /** Busca la publicación y comprueba que el usuario del token sea su autor. */
    private Publicacion buscarDelAutor(Integer id, Authentication authentication) {
        Usuario usuario = usuarioActual.obtener(authentication);
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));
        if (!p.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres el autor de esta publicación");
        }
        return p;
    }
}
