package com.creacionesedimile.service;

import com.creacionesedimile.model.Cotizacion;
import com.creacionesedimile.model.ItemCotizacion;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Servicio de generación de PDF para cotizaciones (RF-05.8).
 *
 * Paleta corporativa Creaciones Edimile:
 *   Negro   #000000  — encabezado, texto fuerte
 *   Verde   #198754  — acento (totales, estado)
 *   Gris claro #F7F7F7 — fondo de filas alternas
 *   Blanco  #FFFFFF
 */
@Service
public class CotizacionPdfService {

    private static final Logger log = LoggerFactory.getLogger(CotizacionPdfService.class);

    // ── Colores corporativos ─────────────────────────────────────────────────
    private static final Color COLOR_NEGRO      = new Color(0x00, 0x00, 0x00);
    private static final Color COLOR_VERDE      = new Color(0x19, 0x87, 0x54);
    private static final Color COLOR_GRIS_OSC   = new Color(0x33, 0x33, 0x33);
    private static final Color COLOR_GRIS_CLARO = new Color(0xF7, 0xF7, 0xF7);
    private static final Color COLOR_GRIS_BORDE = new Color(0xCC, 0xCC, 0xCC);
    private static final Color COLOR_BLANCO     = Color.WHITE;

    private static final ZoneId    ZONA     = ZoneId.of("America/Bogota");
    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", LOCALE_CO)
                    .withZone(ZONA);
    private static final NumberFormat FMT_MONEY =
            NumberFormat.getCurrencyInstance(LOCALE_CO);

    // ── Fuentes ──────────────────────────────────────────────────────────────
    private Font fontTitulo()    { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  22, COLOR_BLANCO); }
    private Font fontSubtitulo() { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, COLOR_BLANCO); }
    private Font fontH2()        { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, COLOR_NEGRO); }
    private Font fontNormal()    { return FontFactory.getFont(FontFactory.HELVETICA,         9, COLOR_GRIS_OSC); }
    private Font fontNegrita()   { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, COLOR_NEGRO); }
    private Font fontTabHdr()    { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, COLOR_BLANCO); }
    private Font fontTotal()     { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, COLOR_BLANCO); }
    private Font fontTotalLbl()  { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, COLOR_NEGRO); }

    // ── API pública ──────────────────────────────────────────────────────────

    public byte[] generar(Cotizacion cotizacion) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new FooterEvent(cotizacion.getNumero()));
            doc.open();

            doc.add(buildEncabezado(cotizacion));
            doc.add(Chunk.NEWLINE);
            doc.add(buildInfoSection(cotizacion));
            doc.add(Chunk.NEWLINE);
            doc.add(buildTablaItems(cotizacion));
            doc.add(Chunk.NEWLINE);
            doc.add(buildTotales(cotizacion));

            if (cotizacion.getObservaciones() != null && !cotizacion.getObservaciones().isBlank()) {
                doc.add(Chunk.NEWLINE);
                doc.add(buildObservaciones(cotizacion.getObservaciones()));
            }

            doc.add(buildMensajeFinal());

        } catch (DocumentException | IOException e) {
            log.error("Error al generar PDF de cotización {}", cotizacion.getNumero(), e);
            throw new RuntimeException("No se pudo generar el PDF", e);
        } finally {
            doc.close();
        }
        return baos.toByteArray();
    }

    // ── Bloques del documento ─────────────────────────────────────────────────

    /** Encabezado negro con logo + nombre empresa + datos de cotización */
    private PdfPTable buildEncabezado(Cotizacion cot) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(new float[]{2f, 3f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(0);

        // Celda izquierda: logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBackgroundColor(COLOR_NEGRO);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(10);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        try (InputStream logoStream = new ClassPathResource("static/images/logo.png").getInputStream()) {
            Image logo = Image.getInstance(logoStream.readAllBytes());
            logo.scaleToFit(220, 120);
            logo.setAlignment(Image.ALIGN_LEFT);
            logoCell.addElement(logo);
        } catch (Exception e) {
            // Si no se puede cargar el logo, mostrar texto
            Paragraph p = new Paragraph("CREACIONES\nEDIMILE", fontTitulo());
            p.setAlignment(Element.ALIGN_LEFT);
            logoCell.addElement(p);
        }

        // Celda derecha: datos de la cotización
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBackgroundColor(COLOR_NEGRO);
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPadding(12);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph cotLabel = new Paragraph("COTIZACIÓN", fontTitulo());
        cotLabel.setAlignment(Element.ALIGN_RIGHT);
        infoCell.addElement(cotLabel);

        Paragraph numeroPar = new Paragraph(cot.getNumero(), fontSubtitulo());
        numeroPar.setAlignment(Element.ALIGN_RIGHT);
        infoCell.addElement(numeroPar);

        String fechaStr = cot.getFechaCreacion() != null
                ? FMT_FECHA.format(Instant.ofEpochSecond(cot.getFechaCreacion().getSeconds()))
                : FMT_FECHA.format(Instant.now());
        Paragraph fechaPar = new Paragraph("Fecha: " + fechaStr, fontSubtitulo());
        fechaPar.setAlignment(Element.ALIGN_RIGHT);
        infoCell.addElement(fechaPar);

        if (cot.getFechaVencimiento() != null) {
            String vence = FMT_FECHA.format(
                    Instant.ofEpochSecond(cot.getFechaVencimiento().getSeconds()));
            Paragraph vencePar = new Paragraph("Válida hasta: " + vence, fontSubtitulo());
            vencePar.setAlignment(Element.ALIGN_RIGHT);
            infoCell.addElement(vencePar);
        }

        table.addCell(logoCell);
        table.addCell(infoCell);
        return table;
    }

    /** Sección de datos: cliente (izquierda) + vendedor (derecha) */
    private PdfPTable buildInfoSection(Cotizacion cot) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1f, 1f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(4);

        table.addCell(buildInfoBox("DATOS DEL CLIENTE",
                new String[][]{
                        {"Nombre",    cot.getClienteNombre()},
                        {"Documento", cot.getClienteDocumento()},
                        {"Teléfono",  cot.getClienteTelefono()},
                        {"Email",     cot.getClienteEmail()},
                        {"Dirección", cot.getClienteDireccion()},
                }, true));

        table.addCell(buildInfoBox("VENDEDOR / COTIZADOR",
                new String[][]{
                        {"Nombre", cot.getUsuarioNombre()},
                        {"Estado", cot.getEstado() != null ? cot.getEstado().name() : "—"},
                }, false));

        return table;
    }

    private PdfPCell buildInfoBox(String titulo, String[][] filas, boolean leftBorder) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8);
        if (leftBorder) cell.setPaddingRight(16);

        Paragraph tit = new Paragraph(titulo, fontH2());
        tit.setSpacingAfter(4);
        cell.addElement(tit);

        // Línea bajo el título
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell(new Phrase(" "));
        lineCell.setBorder(Rectangle.BOTTOM);
        lineCell.setBorderColor(COLOR_VERDE);
        lineCell.setBorderWidth(1.5f);
        lineCell.setPadding(0);
        line.addCell(lineCell);
        cell.addElement(line);

        for (String[] fila : filas) {
            if (fila[1] == null || fila[1].isBlank()) continue;
            Paragraph p = new Paragraph();
            p.add(new Chunk(fila[0] + ": ", fontNegrita()));
            p.add(new Chunk(fila[1], fontNormal()));
            p.setSpacingBefore(2);
            cell.addElement(p);
        }
        return cell;
    }

    /** Tabla de ítems */
    private PdfPTable buildTablaItems(Cotizacion cot) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{0.5f, 3f, 4f, 1f, 1.5f, 1.5f});
        table.setWidthPercentage(100);

        // Encabezado de tabla
        String[] headers = {"#", "Producto", "Descripción / Personalización", "Cant.", "P. Unitario", "Subtotal"};
        for (String h : headers) {
            PdfPCell hc = new PdfPCell(new Phrase(h, fontTabHdr()));
            hc.setBackgroundColor(COLOR_NEGRO);
            hc.setBorder(Rectangle.NO_BORDER);
            hc.setPadding(7);
            hc.setHorizontalAlignment(h.equals("#") ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
            table.addCell(hc);
        }

        // Filas
        int idx = 1;
        for (ItemCotizacion item : cot.getItems()) {
            boolean par = idx % 2 == 0;
            Color bg = par ? COLOR_GRIS_CLARO : COLOR_BLANCO;

            addDataCell(table, String.valueOf(idx++), bg, Element.ALIGN_CENTER);
            addDataCell(table, item.getProductoNombre(), bg, Element.ALIGN_LEFT);
            String desc = item.getDescripcionPersonalizacion();
            addDataCell(table, (desc != null && !desc.isBlank()) ? desc : "—", bg, Element.ALIGN_LEFT, true);
            addDataCell(table, String.valueOf(item.getCantidad()), bg, Element.ALIGN_CENTER);
            addDataCell(table, FMT_MONEY.format(item.getPrecioUnitario()), bg, Element.ALIGN_RIGHT);
            addDataCell(table, FMT_MONEY.format(item.getSubtotal()), bg, Element.ALIGN_RIGHT);
        }
        return table;
    }

    private void addDataCell(PdfPTable table, String text, Color bg, int align) {
        addDataCell(table, text, bg, align, false);
    }

    private void addDataCell(PdfPTable table, String text, Color bg, int align, boolean wrap) {
        PdfPCell cell;
        if (wrap) {
            Paragraph p = new Paragraph(text, fontNormal());
            p.setLeading(0, 1.2f);
            cell = new PdfPCell();
            cell.addElement(p);
        } else {
            cell = new PdfPCell(new Phrase(text, fontNormal()));
        }
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(COLOR_GRIS_BORDE);
        cell.setBorderWidth(0.5f);
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        cell.setNoWrap(false);
        table.addCell(cell);
    }

    /** Bloque de totales alineado a la derecha */
    private PdfPTable buildTotales(Cotizacion cot) throws DocumentException {
        // Tabla exterior: espacio izquierdo + totales
        PdfPTable outer = new PdfPTable(new float[]{3f, 2f});
        outer.setWidthPercentage(100);

        // Celda izquierda vacía
        PdfPCell empty = new PdfPCell();
        empty.setBorder(Rectangle.NO_BORDER);
        outer.addCell(empty);

        // Tabla interior de totales
        PdfPTable totales = new PdfPTable(new float[]{2f, 1.5f});
        totales.setWidthPercentage(100);

        addTotalRow(totales, "Subtotal:", FMT_MONEY.format(cot.getSubtotal()), false);
        if (cot.getDescuentoPorcentaje() > 0) {
            addTotalRow(totales,
                    "Descuento (" + (int) cot.getDescuentoPorcentaje() + "%):",
                    "- " + FMT_MONEY.format(cot.getDescuentoValor()), false);
        }
        if (cot.getIvaPorcentaje() > 0) {
            addTotalRow(totales,
                    "IVA (" + (int) cot.getIvaPorcentaje() + "%):",
                    FMT_MONEY.format(cot.getIvaValor()), false);
        }

        // Fila TOTAL destacada
        PdfPCell lblTotal = new PdfPCell(new Phrase("TOTAL:", fontTotal()));
        lblTotal.setBackgroundColor(COLOR_VERDE);
        lblTotal.setBorder(Rectangle.NO_BORDER);
        lblTotal.setPadding(7);
        lblTotal.setHorizontalAlignment(Element.ALIGN_LEFT);
        totales.addCell(lblTotal);

        PdfPCell valTotal = new PdfPCell(new Phrase(FMT_MONEY.format(cot.getTotal()), fontTotal()));
        valTotal.setBackgroundColor(COLOR_VERDE);
        valTotal.setBorder(Rectangle.NO_BORDER);
        valTotal.setPadding(7);
        valTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.addCell(valTotal);

        PdfPCell totalesCell = new PdfPCell(totales);
        totalesCell.setBorder(Rectangle.NO_BORDER);
        totalesCell.setPadding(0);
        outer.addCell(totalesCell);

        return outer;
    }

    private void addTotalRow(PdfPTable t, String label, String value, boolean highlight) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, fontTotalLbl()));
        lbl.setBorder(Rectangle.BOTTOM);
        lbl.setBorderColor(COLOR_GRIS_BORDE);
        lbl.setBorderWidth(0.5f);
        lbl.setPadding(5);
        lbl.setHorizontalAlignment(Element.ALIGN_LEFT);
        t.addCell(lbl);

        PdfPCell val = new PdfPCell(new Phrase(value, fontNormal()));
        val.setBorder(Rectangle.BOTTOM);
        val.setBorderColor(COLOR_GRIS_BORDE);
        val.setBorderWidth(0.5f);
        val.setPadding(5);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(val);
    }

    /** Bloque de observaciones */
    private PdfPTable buildObservaciones(String texto) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.LEFT);
        cell.setBorderColor(COLOR_VERDE);
        cell.setBorderWidth(3f);
        cell.setPadding(8);

        Paragraph tit = new Paragraph("OBSERVACIONES", fontH2());
        tit.setSpacingAfter(4);
        cell.addElement(tit);
        cell.addElement(new Paragraph(texto, fontNormal()));
        table.addCell(cell);
        return table;
    }

    /** Mensaje final de agradecimiento */
    private Paragraph buildMensajeFinal() {
        Paragraph p = new Paragraph(
                "\nGracias por confiar en Creaciones Edimile. "
                + "Esta cotización es válida hasta la fecha indicada.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, COLOR_GRIS_OSC));
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(16);
        return p;
    }

    // ── Pie de página ─────────────────────────────────────────────────────────

    private static class FooterEvent extends PdfPageEventHelper {
        private final String numero;
        FooterEvent(String numero) { this.numero = numero; }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf;
            try { bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED); }
            catch (Exception e) { return; }

            float y = document.bottom() - 10;
            cb.setColorFill(new Color(0x99, 0x99, 0x99));
            cb.setFontAndSize(bf, 7.5f);

            // Número de cotización (izquierda)
            cb.beginText();
            cb.setTextMatrix(document.left(), y);
            cb.showText("Cotización " + numero + "  |  Creaciones Edimile");
            cb.endText();

            // Número de página (derecha)
            cb.beginText();
            String page = "Página " + writer.getPageNumber();
            float w = bf.getWidthPoint(page, 7.5f);
            cb.setTextMatrix(document.right() - w, y);
            cb.showText(page);
            cb.endText();

            // Línea separadora
            cb.setLineWidth(0.5f);
            cb.setColorStroke(new Color(0xCC, 0xCC, 0xCC));
            cb.moveTo(document.left(), y + 8);
            cb.lineTo(document.right(), y + 8);
            cb.stroke();
        }
    }
}
