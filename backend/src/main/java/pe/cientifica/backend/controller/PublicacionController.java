
package pe.cientifica.backend.controller;


import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import pe.cientifica.backend.dto.PublicacionResponse;
import pe.cientifica.backend.service.PublicacionService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionService publicacionService;

    public PublicacionController(PublicacionService publicacionService) {
        this.publicacionService = publicacionService;
    }

    @GetMapping
    public List<PublicacionResponse> listar(
            @RequestParam(required = false) Integer sede,
            @RequestParam(required = false) Integer categoria,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta,

            @RequestParam(required = false) String q) {

        return publicacionService.listar(
                sede, categoria, desde, hasta, q);
    }


    @GetMapping("/{id}")
    public PublicacionResponse obtenerPorId(@PathVariable Integer id) {
        return publicacionService.obtenerPorId(id);
    }
}
