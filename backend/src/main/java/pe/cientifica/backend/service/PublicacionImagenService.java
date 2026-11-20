package pe.cientifica.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pe.cientifica.backend.dto.PublicacionResponse;
import pe.cientifica.backend.entity.Publicacion;
import pe.cientifica.backend.entity.Usuario;
import pe.cientifica.backend.repository.PublicacionGestionRepository;

/** Asocia o quita la imagen de una publicación propia. */
@Service
public class PublicacionImagenService {

    private final PublicacionGestionRepository repo;
    private final UsuarioActualService usuarioActual;
    private final ImagenStorageService storage;

    public PublicacionImagenService(PublicacionGestionRepository repo,
                                    UsuarioActualService usuarioActual,
                                    ImagenStorageService storage) {
        this.repo = repo;
        this.usuarioActual = usuarioActual;
        this.storage = storage;
    }

    /** Sube (o reemplaza) la imagen. 403 si no es el autor, 404 si no existe, 400 si el archivo es inválido. */
    @Transactional
    public PublicacionResponse subir(Integer id, MultipartFile archivo, Authentication auth) {
        Publicacion p = buscarDelAutor(id, auth);
        String anterior = p.getImagen();
        p.setImagen(storage.guardar(archivo));
        repo.save(p);
        storage.eliminar(anterior);
        return PublicacionResponse.desde(p);
    }

    /** Quita la imagen de la publicación y borra el archivo. */
    @Transactional
    public void quitar(Integer id, Authentication auth) {
        Publicacion p = buscarDelAutor(id, auth);
        String anterior = p.getImagen();
        p.setImagen(null);
        repo.save(p);
        storage.eliminar(anterior);
    }

    private Publicacion buscarDelAutor(Integer id, Authentication auth) {
        Usuario u = usuarioActual.obtener(auth);
        Publicacion p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));
        if (!p.getUsuario().getId().equals(u.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres el autor de esta publicación");
        }
        return p;
    }
}