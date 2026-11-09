
package pe.cientifica.backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.cientifica.backend.service.ExportacionService;

@RestController
@RequestMapping("/api/admin/exportar")
public class ExportacionController {

    private final ExportacionService exportacionService;

    public ExportacionController(
            ExportacionService exportacionService) {
        this.exportacionService = exportacionService;
    }

    @GetMapping("/publicaciones")
    public ResponseEntity<byte[]> exportarPublicaciones(
            @RequestParam String formato) {

        byte[] archivo =
                exportacionService.exportarPublicaciones(formato);

        return crearRespuesta(archivo, formato, "publicaciones");
    }

    @GetMapping("/reportes")
    public ResponseEntity<byte[]> exportarReportes(
            @RequestParam String formato) {

        byte[] archivo =
                exportacionService.exportarReportes(formato);

        return crearRespuesta(archivo, formato, "reportes");
    }

    private ResponseEntity<byte[]> crearRespuesta(
            byte[] archivo,
            String formato,
            String nombre) {

        boolean pdf = formato.equalsIgnoreCase("pdf");

        String tipo = pdf
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String extension = pdf ? ".pdf" : ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombre + extension + "\"")
                .contentType(MediaType.parseMediaType(tipo))
                .body(archivo);
    }
}
