package pe.cientifica.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.cientifica.backend.entity.Usuario;
import pe.cientifica.backend.repository.UsuarioRepository;

/**
 * Obtiene el usuario autenticado a partir del token (el principal es el correo).
 *
 * <p>El autor de una publicación o de un reporte siempre se obtiene aquí,
 * nunca de un campo del JSON.</p>
 */
@Service
public class UsuarioActualService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActualService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * @param authentication autenticación del contexto de seguridad
     * @return el usuario dueño del token
     * @throws ResponseStatusException 401 si no hay sesión válida o el usuario ya no existe
     */
    public Usuario obtener(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida");
        }
        return usuarioRepository.findByCorreo(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
    }
}
