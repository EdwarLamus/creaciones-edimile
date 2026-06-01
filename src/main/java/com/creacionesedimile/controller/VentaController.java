package com.creacionesedimile.controller;

import com.creacionesedimile.model.Cliente;
import com.creacionesedimile.model.Cotizacion;
import com.creacionesedimile.model.Producto;
import com.creacionesedimile.model.Venta;
import com.creacionesedimile.repository.ClienteRepository;
import com.creacionesedimile.repository.CotizacionRepository;
import com.creacionesedimile.repository.ProductoRepository;
import com.creacionesedimile.service.ExcelExportService;
import com.creacionesedimile.service.UsuarioService;
import com.creacionesedimile.service.VentaFacturaPdfService;
import com.creacionesedimile.service.VentaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService           ventaService;
    private final ClienteRepository      clienteRepository;
    private final ProductoRepository     productoRepository;
    private final CotizacionRepository   cotizacionRepository;
    private final VentaFacturaPdfService facturaService;
    private final ExcelExportService     excelService;
    private final UsuarioService         usuarioService;

    public VentaController(VentaService ventaService,
                            ClienteRepository clienteRepository,
                            ProductoRepository productoRepository,
                            CotizacionRepository cotizacionRepository,
                            VentaFacturaPdfService facturaService,
                            ExcelExportService excelService,
                            UsuarioService usuarioService) {
        this.ventaService         = ventaService;
        this.clienteRepository    = clienteRepository;
        this.productoRepository   = productoRepository;
        this.cotizacionRepository = cotizacionRepository;
        this.facturaService       = facturaService;
        this.excelService         = excelService;
        this.usuarioService       = usuarioService;
    }

    private String resolverNombreUsuario(Authentication auth) {
        if (auth == null) return "Sistema";
        return usuarioService.buscarPorEmail(auth.getName())
                .map(u -> u.getNombre() + " " + u.getApellido())
                .orElse(auth.getName());
    }

    // ── Listado ─────────────────────────────────────────────────────────────

    @GetMapping
    public String lista(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String estado,
                         Model model) {
        List<Venta> ventas;

        if (estado != null && !estado.isBlank()) {
            try {
                ventas = ventaService.listarPorEstado(Venta.Estado.valueOf(estado));
            } catch (IllegalArgumentException e) {
                ventas = ventaService.listarTodas();
            }
        } else if (q != null && !q.isBlank()) {
            ventas = ventaService.buscar(q);
        } else {
            ventas = ventaService.listarTodas();
        }

        model.addAttribute("ventas", ventas);
        model.addAttribute("q", q);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("estados", Venta.Estado.values());
        return "ventas/lista";
    }

    // ── Exportar Excel ───────────────────────────────────────────────────────

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel() throws Exception {
        List<Venta> ventas = ventaService.listarTodas();
        byte[] bytes = excelService.exportarVentas(ventas);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=ventas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // ── Detalle ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, Model model) {
        Optional<Venta> opt = ventaService.buscarPorId(id);
        if (opt.isEmpty()) return "redirect:/ventas";
        model.addAttribute("venta", opt.get());
        return "ventas/detalle";
    }

    // ── Nueva venta directa ──────────────────────────────────────────────────

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        List<Cliente>  clientes  = clienteRepository.findAll().stream().filter(Cliente::isActivo).toList();
        List<Producto> productos = productoRepository.findAll().stream().filter(Producto::isActivo).toList();
        List<Cotizacion> cotizacionesAprobadas = cotizacionRepository.findAll().stream()
                .filter(Cotizacion::isAprobada).toList();
        model.addAttribute("clientes",  clientes);
        model.addAttribute("productos", productos);
        model.addAttribute("metodosPago", Venta.MetodoPago.values());
        model.addAttribute("cotizacionesAprobadas", cotizacionesAprobadas);
        return "ventas/form";
    }

    @PostMapping("/nueva")
    public String nuevaSubmit(@RequestParam String clienteId,
                               @RequestParam List<String>  productoIds,
                               @RequestParam(required = false) List<String>  descripciones,
                               @RequestParam List<Integer> cantidades,
                               @RequestParam List<Double>  precios,
                               @RequestParam(defaultValue = "0")  double descuentoPct,
                               @RequestParam(defaultValue = "19") double ivaPct,
                               @RequestParam String metodoPago,
                               @RequestParam(required = false) String observaciones,
                               Authentication auth,
                               RedirectAttributes ra) {
        try {
            String usuarioNombre = resolverNombreUsuario(auth);
            Venta venta = ventaService.crearDirecta(clienteId, productoIds, descripciones,
                    cantidades, precios, descuentoPct, ivaPct,
                    metodoPago, observaciones, null, usuarioNombre);
            ra.addFlashAttribute("successMsg",
                    "Venta " + venta.getNumero() + " registrada correctamente.");
            return "redirect:/ventas/" + venta.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/ventas/nueva";
        }
    }

    // ── Desde cotización aprobada ────────────────────────────────────────────

    @GetMapping("/desde-cotizacion/{cotizacionId}")
    public String desdeCotizacionForm(@PathVariable String cotizacionId, Model model) {
        Optional<Cotizacion> opt = cotizacionRepository.findById(cotizacionId);
        if (opt.isEmpty() || !opt.get().isAprobada()) {
            return "redirect:/cotizaciones";
        }
        model.addAttribute("cotizacion",  opt.get());
        model.addAttribute("metodosPago", Venta.MetodoPago.values());
        return "ventas/desde-cotizacion";
    }

    @PostMapping("/desde-cotizacion/{cotizacionId}")
    public String desdeCotizacionSubmit(@PathVariable String cotizacionId,
                                         @RequestParam String metodoPago,
                                         @RequestParam(required = false) String observaciones,
                                         Authentication auth,
                                         RedirectAttributes ra) {
        try {
            String usuarioNombre = resolverNombreUsuario(auth);
            Venta venta = ventaService.crearDesdeCotizacion(
                    cotizacionId, metodoPago, observaciones, null, usuarioNombre);
            ra.addFlashAttribute("successMsg",
                    "Venta " + venta.getNumero() + " creada desde cotización correctamente.");
            return "redirect:/ventas/" + venta.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/ventas/desde-cotizacion/" + cotizacionId;
        }
    }

    // ── Descargar factura PDF ────────────────────────────────────────────────

    @GetMapping("/{id}/factura")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable String id) {
        Optional<Venta> opt = ventaService.buscarPorId(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        byte[] pdf = facturaService.generar(opt.get());
        String filename = "factura-" + opt.get().getNumero() + ".pdf";
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── Registrar pago ───────────────────────────────────────────────────────

    @PostMapping("/{id}/pagar")
    public String pagar(@PathVariable String id, RedirectAttributes ra) {
        try {
            ventaService.registrarPago(id);
            ra.addFlashAttribute("successMsg", "Venta marcada como PAGADA correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/ventas/" + id;
    }

    // ── Anular ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable String id,
                          @RequestParam(required = false) String motivo,
                          RedirectAttributes ra) {
        try {
            ventaService.anular(id, motivo);
            ra.addFlashAttribute("successMsg", "Venta anulada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/ventas/" + id;
    }

    // ── Eliminar (solo ADMIN) ────────────────────────────────────────────────

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            ventaService.eliminar(id);
            ra.addFlashAttribute("successMsg", "Venta eliminada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/ventas";
    }
}
