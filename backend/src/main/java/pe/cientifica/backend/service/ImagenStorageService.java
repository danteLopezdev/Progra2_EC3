package pe.cientifica.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Guarda, sirve y elimina imágenes de publicaciones en la carpeta {@code uploads/}.
 * Valida el tipo real por los primeros bytes y genera un nombre propio (UUID).
 */
@Service
public class ImagenStorageService {

    private static final long MAX_BYTES = 2L * 1024 * 1024;
    private static final Pattern NOMBRE_VALIDO =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)$");

    private final Path carpeta;

    public ImagenStorageService(@Value("${app.uploads.dir:uploads}") String dir) throws IOException {
        this.carpeta = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(carpeta);
    }

    /**
     * Guarda la imagen y devuelve el nombre generado.
     * @throws ResponseStatusException 400 si está vacía, es muy grande o no es JPG/PNG/WebP
     */
    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes enviar una imagen");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen no puede superar los 2 MB");
        }
        try {
            byte[] cabecera;
            try (InputStream in = archivo.getInputStream()) {
                cabecera = in.readNBytes(12);
            }
            String extension = detectarExtension(cabecera);
            if (extension == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Solo se permiten imágenes JPG, PNG o WebP");
            }
            String nombre = UUID.randomUUID() + "." + extension;
            try (InputStream in = archivo.getInputStream()) {
                Files.copy(in, carpeta.resolve(nombre));
            }
            return nombre;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen");
        }
    }

    /** Devuelve el archivo o 404 si el nombre no es válido o no existe. */
    public Resource cargar(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada");
        }
        Path ruta = carpeta.resolve(nombre).normalize();
        if (!ruta.startsWith(carpeta) || !Files.isRegularFile(ruta)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada");
        }
        return new FileSystemResource(ruta);
    }

    /** Elimina el archivo si existe; no falla si no está. */
    public void eliminar(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) return;
        try {
            Files.deleteIfExists(carpeta.resolve(nombre).normalize());
        } catch (IOException ignorado) {
            // archivo huérfano: no debe romper la operación principal
        }
    }

    /** Tipo de contenido según la extensión del nombre (ya validado). */
    public static String tipoContenido(String nombre) {
        if (nombre.endsWith(".png")) return "image/png";
        if (nombre.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private String detectarExtension(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "jpg";
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) return "png";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "webp";
        return null;
    }
}