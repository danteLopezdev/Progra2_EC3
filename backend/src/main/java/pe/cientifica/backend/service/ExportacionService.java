
package pe.cientifica.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.cientifica.backend.entity.Publicacion;
import pe.cientifica.backend.entity.Reporte;
import pe.cientifica.backend.repository.PublicacionRepository;
import pe.cientifica.backend.repository.ReporteRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ExportacionService {

    private final PublicacionRepository publicacionRepository;
    private final ReporteRepository reporteRepository;

    public ExportacionService(
            PublicacionRepository publicacionRepository,
            ReporteRepository reporteRepository) {
        this.publicacionRepository = publicacionRepository;
        this.reporteRepository = reporteRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exportarPublicaciones(String formato) {
        String f = validarFormato(formato);
        List<Publicacion> datos = publicacionRepository.findAll();

        String[] encabezados = {
                "ID", "Título", "Descripción", "Fecha",
                "Estado", "Categoría", "Sede", "Autor"
        };

        List<String[]> filas = datos.stream().map(p -> new String[]{
                texto(p.getId()),
                texto(p.getTitulo()),
                texto(p.getDescripcion()),
                texto(p.getFechaPublicacion()),
                texto(p.getEstado()),
                texto(p.getCategoria().getNombre()),
                texto(p.getSede().getNombre()),
                p.getUsuario().getNombre() + " "
                        + p.getUsuario().getApellido()
        }).toList();

        return generarArchivo(
                f, "Publicaciones", encabezados, filas);
    }

    @Transactional(readOnly = true)
    public byte[] exportarReportes(String formato) {
        String f = validarFormato(formato);
        List<Reporte> datos = reporteRepository.findAll();

        String[] encabezados = {
                "ID reporte", "Motivo", "Fecha",
                "Estado", "Usuario", "ID publicación",
                "Título publicación"
        };

        List<String[]> filas = datos.stream().map(r -> new String[]{
                texto(r.getId()),
                texto(r.getMotivo()),
                texto(r.getFechaReporte()),
                texto(r.getEstado()),
                r.getUsuario().getNombre() + " "
                        + r.getUsuario().getApellido(),
                texto(r.getPublicacion().getId()),
                texto(r.getPublicacion().getTitulo())
        }).toList();

        return generarArchivo(
                f, "Reportes", encabezados, filas);
    }

    private String validarFormato(String formato) {
        if (formato == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar formato=pdf o formato=xlsx");
        }

        String f = formato.trim().toLowerCase(Locale.ROOT);

        if (!f.equals("pdf") && !f.equals("xlsx")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato no válido. Usa pdf o xlsx");
        }

        return f;
    }

    private byte[] generarArchivo(
            String formato,
            String titulo,
            String[] encabezados,
            List<String[]> filas) {

        if (formato.equals("pdf")) {
            return generarPdf(titulo, encabezados, filas);
        }

        return generarExcel(titulo, encabezados, filas);
    }

    private byte[] generarPdf(
            String titulo,
            String[] encabezados,
            List<String[]> filas) {

        try (ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            Document documento = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(documento, salida);
            documento.open();

            documento.add(new Paragraph(titulo));
            documento.add(new Paragraph(
                    "Fecha de generación: "
                            + LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern(
                                    "yyyy-MM-dd HH:mm"))));
            documento.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(encabezados.length);
            tabla.setWidthPercentage(100);

            for (String encabezado : encabezados) {
                tabla.addCell(new Phrase(encabezado));
            }

            for (String[] fila : filas) {
                for (String valor : fila) {
                    tabla.addCell(new Phrase(valor));
                }
            }

            documento.add(tabla);
            documento.close();

            return salida.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo generar el PDF", e);
        }
    }

    private byte[] generarExcel(
            String titulo,
            String[] encabezados,
            List<String[]> filas) {

        try (XSSFWorkbook libro = new XSSFWorkbook();
             ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            Sheet hoja = libro.createSheet(titulo);

            CellStyle estiloTitulo = libro.createCellStyle();
            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            estiloTitulo.setFont(fuenteTitulo);

            Row filaTitulo = hoja.createRow(0);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue(titulo);
            celdaTitulo.setCellStyle(estiloTitulo);

            Row filaFecha = hoja.createRow(1);
            filaFecha.createCell(0).setCellValue(
                    "Generado: " + LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern(
                                    "yyyy-MM-dd HH:mm")));

            CellStyle estiloCabecera = libro.createCellStyle();
            Font fuenteCabecera = libro.createFont();
            fuenteCabecera.setBold(true);
            estiloCabecera.setFont(fuenteCabecera);

            Row filaCabecera = hoja.createRow(3);

            for (int i = 0; i < encabezados.length; i++) {
                Cell celda = filaCabecera.createCell(i);
                celda.setCellValue(encabezados[i]);
                celda.setCellStyle(estiloCabecera);
            }

            int numeroFila = 4;

            for (String[] datos : filas) {
                Row fila = hoja.createRow(numeroFila++);

                for (int i = 0; i < datos.length; i++) {
                    fila.createCell(i).setCellValue(datos[i]);
                }
            }

            for (int i = 0; i < encabezados.length; i++) {
                hoja.autoSizeColumn(i);
            }

            libro.write(salida);
            return salida.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo generar el Excel", e);
        }
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}
