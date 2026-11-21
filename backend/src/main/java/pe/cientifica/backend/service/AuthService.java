package pe.cientifica.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.cientifica.backend.dto.LoginRequest;
import pe.cientifica.backend.dto.LoginResponse;
import pe.cientifica.backend.dto.RegistroRequest;
import pe.cientifica.backend.entity.Rol;
import pe.cientifica.backend.entity.Usuario;
import pe.cientifica.backend.repository.UsuarioRepository;
import pe.cientifica.backend.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Cantidad máxima de usuarios registrados (incluye al administrador).
     * Se puede cambiar con la propiedad app.max-usuarios (en Railway: APP_MAX_USUARIOS).
     */
    @Value("${app.max-usuarios:9}")
    private long maxUsuarios;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registra un estudiante nuevo.
     *
     * @throws ResponseStatusException 403 si se alcanzó el límite de usuarios;
     *                                 409 si el correo ya está registrado
     */
    public void registrar(RegistroRequest request) {
        if (usuarioRepository.count() >= maxUsuarios) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Se alcanzó el límite de usuarios registrados");
        }

        String correo = request.correo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        usuario.setRol(Rol.ESTUDIANTE);
        usuarioRepository.save(usuario);
    }

    public LoginResponse login(LoginRequest request) {
        String correo = request.correo().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        return new LoginResponse(jwtService.generarToken(usuario),
                usuario.getNombre(), usuario.getRol().name());
    }
}