package com.creacionesedimile.service;

import com.creacionesedimile.model.Gasto;
import com.creacionesedimile.model.ItemVenta;
import com.creacionesedimile.model.Venta;
import com.google.cloud.Timestamp;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final VentaService ventaService;
    private final GastoService gastoService;

    public ReporteService(VentaService ventaService, GastoService gastoService) {
        this.ventaService = ventaService;
        this.gastoService = gastoService;
    }

    // ── DTOs ────────────────────────────────────────────────────────────────

    /** Resumen combinado ingresos vs egresos para el período. */
    public static class ResumenPeriodo {
        public double totalIngresos;           // ventas PAGADAS
        public double totalEgresos;            // todos los gastos
        public double balance;                 // ingresos - egresos
        public int    cantidadVentas;
        public int    cantidadGastos;
        public int    ventasPagadas;
        public int    ventasPendientes;
        public int    ventasAnuladas;
        public Map<String, Double>  ventasPorMetodoPago = new LinkedHashMap<>();
        public Map<String, Double>  gastosPorCategoria  = new LinkedHashMap<>();
        public List<Venta>          ventas              = new ArrayList<>();
        public List<Gasto>          gastos              = new ArrayList<>();
    }

    /** Datos para el reporte detallado de ventas. */
    public static class ReporteVentasData {
        public double totalGeneral;
        public double totalPagadas;
        public double totalPendientes;
        public int    cantidadTotal;
        public Map<String, Double>  porEstado      = new LinkedHashMap<>();
        public Map<String, Double>  porMetodoPago  = new LinkedHashMap<>();
        public Map<String, Double>  porCliente     = new LinkedHashMap<>(); // top 10 clientes por monto
        public Map<String, Integer> topProductos   = new LinkedHashMap<>(); // top 10 productos por unidades
        public List<Venta>          ventas         = new ArrayList<>();
    }

    /** Datos para el reporte detallado de gastos. */
    public static class ReporteGastosData {
        public double totalGeneral;
        public int    cantidad;
        public Map<String, Double>  porCategoria  = new LinkedHashMap<>();
        public Map<String, Double>  porMetodoPago = new LinkedHashMap<>();
        public List<Gasto>          gastos        = new ArrayList<>();
    }

    // ── Utilidades de fecha ─────────────────────────────────────────────────

    private boolean enPeriodo(Timestamp ts, LocalDate desde, LocalDate hasta) {
        if (ts == null) return false;
        LocalDate fecha = ts.toDate().toInstant().atZone(ZONA).toLocalDate();
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private LocalDate parse(String s, LocalDate defecto) {
        if (s == null || s.isBlank()) return defecto;
        try { return LocalDate.parse(s); } catch (Exception e) { return defecto; }
    }

    // ── Reporte: Resumen del período (Ingresos vs Egresos) ──────────────────

    public ResumenPeriodo getResumenPeriodo(String desdeStr, String hastaStr) {
        LocalDate hoy   = LocalDate.now(ZONA);
        LocalDate desde = parse(desdeStr, hoy.withDayOfMonth(1));
        LocalDate hasta = parse(hastaStr, hoy);

        List<Venta> ventas = ventaService.listarTodas().stream()
                .filter(v -> enPeriodo(v.getFechaCreacion(), desde, hasta))
                .sorted(Comparator.comparing(
                        v -> v.getFechaCreacion() != null ? v.getFechaCreacion().toDate() : new Date(0),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());

        List<Gasto> gastos = gastoService.listarTodos().stream()
                .filter(g -> enPeriodo(g.getFechaPago(), desde, hasta))
                .sorted(Comparator.comparing(
                        g -> g.getFechaPago() != null ? g.getFechaPago().toDate() : new Date(0),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());

        ResumenPeriodo r = new ResumenPeriodo();
        r.ventas = ventas;
        r.gastos = gastos;
        r.cantidadVentas    = ventas.size();
        r.cantidadGastos    = gastos.size();
        r.ventasPagadas     = (int) ventas.stream().filter(v -> v.getEstado() == Venta.Estado.PAGADA).count();
        r.ventasPendientes  = (int) ventas.stream().filter(v -> v.getEstado() == Venta.Estado.PENDIENTE).count();
        r.ventasAnuladas    = (int) ventas.stream().filter(v -> v.getEstado() == Venta.Estado.ANULADA).count();

        // Solo ventas PAGADAS cuentan como ingreso real
        r.totalIngresos = ventas.stream()
                .filter(v -> v.getEstado() == Venta.Estado.PAGADA)
                .mapToDouble(Venta::getTotal).sum();

        r.totalEgresos = gastos.stream().mapToDouble(Gasto::getTotal).sum();
        r.balance      = r.totalIngresos - r.totalEgresos;

        // Ventas PAGADAS por método de pago
        for (Venta v : ventas) {
            if (v.getEstado() == Venta.Estado.PAGADA && v.getMetodoPago() != null) {
                r.ventasPorMetodoPago.merge(v.getMetodoPago().name(), v.getTotal(), Double::sum);
            }
        }
        ordenarDesc(r.ventasPorMetodoPago);

        // Gastos por categoría
        for (Gasto g : gastos) {
            String key = g.getCategoria() != null ? g.getCategoria().name() : "OTROS";
            r.gastosPorCategoria.merge(key, g.getTotal(), Double::sum);
        }
        ordenarDesc(r.gastosPorCategoria);

        return r;
    }

    // ── Reporte: Ventas detallado ────────────────────────────────────────────

    public ReporteVentasData getReporteVentas(String desdeStr, String hastaStr) {
        LocalDate hoy   = LocalDate.now(ZONA);
        LocalDate desde = parse(desdeStr, hoy.withDayOfMonth(1));
        LocalDate hasta = parse(hastaStr, hoy);

        List<Venta> ventas = ventaService.listarTodas().stream()
                .filter(v -> enPeriodo(v.getFechaCreacion(), desde, hasta))
                .sorted(Comparator.comparing(
                        v -> v.getFechaCreacion() != null ? v.getFechaCreacion().toDate() : new Date(0),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());

        ReporteVentasData r = new ReporteVentasData();
        r.ventas       = ventas;
        r.cantidadTotal = ventas.size();
        r.totalGeneral  = ventas.stream().mapToDouble(Venta::getTotal).sum();
        r.totalPagadas  = ventas.stream()
                .filter(v -> v.getEstado() == Venta.Estado.PAGADA)
                .mapToDouble(Venta::getTotal).sum();
        r.totalPendientes = ventas.stream()
                .filter(v -> v.getEstado() == Venta.Estado.PENDIENTE)
                .mapToDouble(Venta::getTotal).sum();

        // Por estado
        for (Venta v : ventas) {
            String key = v.getEstado() != null ? v.getEstado().name() : "SIN_ESTADO";
            r.porEstado.merge(key, v.getTotal(), Double::sum);
        }

        // Por método de pago (solo PAGADAS)
        for (Venta v : ventas) {
            if (v.getEstado() == Venta.Estado.PAGADA && v.getMetodoPago() != null) {
                r.porMetodoPago.merge(v.getMetodoPago().name(), v.getTotal(), Double::sum);
            }
        }
        ordenarDesc(r.porMetodoPago);

        // Top 10 clientes por monto (excluye ANULADAS)
        Map<String, Double> clienteMap = new HashMap<>();
        for (Venta v : ventas) {
            if (v.getEstado() != Venta.Estado.ANULADA && v.getClienteNombre() != null) {
                clienteMap.merge(v.getClienteNombre(), v.getTotal(), Double::sum);
            }
        }
        clienteMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> r.porCliente.put(e.getKey(), e.getValue()));

        // Top 10 productos por unidades vendidas (excluye ANULADAS)
        Map<String, Integer> prodMap = new HashMap<>();
        for (Venta v : ventas) {
            if (v.getEstado() != Venta.Estado.ANULADA && v.getItems() != null) {
                for (ItemVenta item : v.getItems()) {
                    if (item.getProductoNombre() != null) {
                        prodMap.merge(item.getProductoNombre(), item.getCantidad(), Integer::sum);
                    }
                }
            }
        }
        prodMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> r.topProductos.put(e.getKey(), e.getValue()));

        return r;
    }

    // ── Reporte: Gastos detallado ────────────────────────────────────────────

    public ReporteGastosData getReporteGastos(String desdeStr, String hastaStr) {
        LocalDate hoy   = LocalDate.now(ZONA);
        LocalDate desde = parse(desdeStr, hoy.withDayOfMonth(1));
        LocalDate hasta = parse(hastaStr, hoy);

        List<Gasto> gastos = gastoService.listarTodos().stream()
                .filter(g -> enPeriodo(g.getFechaPago(), desde, hasta))
                .sorted(Comparator.comparing(
                        g -> g.getFechaPago() != null ? g.getFechaPago().toDate() : new Date(0),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());

        ReporteGastosData r = new ReporteGastosData();
        r.gastos       = gastos;
        r.cantidad     = gastos.size();
        r.totalGeneral = gastos.stream().mapToDouble(Gasto::getTotal).sum();

        // Por categoría (ordenado descendente)
        for (Gasto g : gastos) {
            String key = g.getCategoria() != null ? g.getCategoria().name() : "OTROS";
            r.porCategoria.merge(key, g.getTotal(), Double::sum);
        }
        ordenarDesc(r.porCategoria);

        // Por método de pago
        for (Gasto g : gastos) {
            if (g.getMetodoPago() != null) {
                r.porMetodoPago.merge(g.getMetodoPago().name(), g.getTotal(), Double::sum);
            }
        }
        ordenarDesc(r.porMetodoPago);

        return r;
    }

    // ── Utilidad ─────────────────────────────────────────────────────────────

    private void ordenarDesc(Map<String, Double> mapa) {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(mapa.entrySet());
        entries.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        mapa.clear();
        entries.forEach(e -> mapa.put(e.getKey(), e.getValue()));
    }
}
