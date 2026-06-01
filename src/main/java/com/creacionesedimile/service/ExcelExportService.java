package com.creacionesedimile.service;

import com.creacionesedimile.model.Gasto;
import com.creacionesedimile.model.Insumo;
import com.creacionesedimile.model.Venta;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Genera archivos Excel (.xlsx) para ventas, gastos e inventario.
 */
@Service
public class ExcelExportService {

    private static final String ZONA = "America/Bogota";

    // ── Estilos reutilizables ────────────────────────────────────────────────

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle currencyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }

    private CellStyle dateStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy"));
        return style;
    }

    private void createHeaderRow(Sheet sheet, CellStyle style, String... titles) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(style);
        }
    }

    private void autosize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
            // Añadir un poco de margen
            sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 512, 15000));
        }
    }

    // ── Exportar Ventas ──────────────────────────────────────────────────────

    public byte[] exportarVentas(List<Venta> ventas) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Ventas");
            CellStyle hStyle   = headerStyle(wb);
            CellStyle moneyFmt = currencyStyle(wb);
            CellStyle dateFmt  = dateStyle(wb);

            createHeaderRow(sheet, hStyle,
                    "Número", "Cliente", "Documento", "Fecha", "Estado",
                    "Método Pago", "Subtotal", "Descuento", "IVA", "Total",
                    "Cotización", "Registrado por");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone(ZONA));

            int rowIdx = 1;
            for (Venta v : ventas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nullSafe(v.getNumero()));
                row.createCell(1).setCellValue(nullSafe(v.getClienteNombre()));
                row.createCell(2).setCellValue(nullSafe(v.getClienteDocumento()));

                if (v.getFechaCreacion() != null) {
                    Cell dc = row.createCell(3);
                    dc.setCellValue(sdf.format(v.getFechaCreacion().toDate()));
                }

                row.createCell(4).setCellValue(v.getEstado() != null ? v.getEstado().name() : "");
                row.createCell(5).setCellValue(v.getMetodoPago() != null ? v.getMetodoPago().name() : "");

                Cell sub = row.createCell(6);
                sub.setCellValue(v.getSubtotal());
                sub.setCellStyle(moneyFmt);

                Cell desc = row.createCell(7);
                desc.setCellValue(v.getDescuentoValor());
                desc.setCellStyle(moneyFmt);

                Cell iva = row.createCell(8);
                iva.setCellValue(v.getIvaValor());
                iva.setCellStyle(moneyFmt);

                Cell total = row.createCell(9);
                total.setCellValue(v.getTotal());
                total.setCellStyle(moneyFmt);

                row.createCell(10).setCellValue(nullSafe(v.getCotizacionNumero()));
                row.createCell(11).setCellValue(nullSafe(v.getUsuarioNombre()));
            }

            autosize(sheet, 12);
            return toBytes(wb);
        }
    }

    // ── Exportar Gastos ──────────────────────────────────────────────────────

    public byte[] exportarGastos(List<Gasto> gastos) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Gastos");
            CellStyle hStyle   = headerStyle(wb);
            CellStyle moneyFmt = currencyStyle(wb);

            createHeaderRow(sheet, hStyle,
                    "Número", "Nombre / Motivo", "Categoría", "Proveedor",
                    "Comprobante", "Fecha Pago", "Método Pago", "Total",
                    "Registrado por");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone(ZONA));

            int rowIdx = 1;
            for (Gasto g : gastos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nullSafe(g.getNumero()));
                row.createCell(1).setCellValue(nullSafe(g.getNombre()));
                row.createCell(2).setCellValue(g.getCategoria() != null ? g.getCategoria().name() : "");
                row.createCell(3).setCellValue(nullSafe(g.getProveedor()));
                row.createCell(4).setCellValue(nullSafe(g.getComprobante()));

                if (g.getFechaPago() != null) {
                    row.createCell(5).setCellValue(sdf.format(g.getFechaPago().toDate()));
                }

                row.createCell(6).setCellValue(g.getMetodoPago() != null ? g.getMetodoPago().name() : "");

                Cell total = row.createCell(7);
                total.setCellValue(g.getTotal());
                total.setCellStyle(moneyFmt);

                row.createCell(8).setCellValue(nullSafe(g.getUsuarioNombre()));
            }

            autosize(sheet, 9);
            return toBytes(wb);
        }
    }

    // ── Exportar Inventario ──────────────────────────────────────────────────

    public byte[] exportarInventario(List<Insumo> insumos) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Inventario");
            CellStyle hStyle   = headerStyle(wb);
            CellStyle moneyFmt = currencyStyle(wb);

            createHeaderRow(sheet, hStyle,
                    "Nombre", "Descripción", "Unidad Medida",
                    "Stock Actual", "Stock Mínimo", "Precio Unitario", "Estado");

            int rowIdx = 1;
            for (Insumo ins : insumos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nullSafe(ins.getNombre()));
                row.createCell(1).setCellValue(nullSafe(ins.getDescripcion()));
                row.createCell(2).setCellValue(nullSafe(ins.getUnidadMedida()));
                row.createCell(3).setCellValue(ins.getStockActual());
                row.createCell(4).setCellValue(ins.getStockMinimo());

                Cell precio = row.createCell(5);
                precio.setCellValue(ins.getPrecioUnitario());
                precio.setCellStyle(moneyFmt);

                row.createCell(6).setCellValue(ins.isActivo() ? "Activo" : "Inactivo");
            }

            autosize(sheet, 7);
            return toBytes(wb);
        }
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }
}
