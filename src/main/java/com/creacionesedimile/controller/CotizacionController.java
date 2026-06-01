package com.creacionesedimile.controller;

import com.creacionesedimile.model.Cliente;
import com.creacionesedimile.model.Cotizacion;
import com.creacionesedimile.model.Producto;
import com.creacionesedimile.repository.ClienteRepository;
import com.creacionesedimile.repository.ProductoRepository;
import com.creacionesedimile.service.CotizacionPdfService;
import com.creacionesedimile.service.CotizacionService;
import com.creacionesedimile.service.UsuarioService;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/cotizaciones")
public class CotizacionController {

    private final CotizacionService    cotizacionService;
    private final CotizacionPdfService pdfService;
    private final ClienteRepository    clienteRepository;
    private final ProductoRepository   productoRepository;
    private final UsuarioService       usuarioService;

    public CotizacionController(CotizacionService cotizacionService,
                                  CotizacionPdfService pdfService,
                                  ClienteRepository clienteRepository,
                                  ProductoRepository productoRepository,
                                  UsuarioService usuarioService) {
        this.cotizacionService  = cotizacionService;
        this.pdfService         = pdfService;
        this.clienteRepository  = clienteRepository;
        this.productoRepository = productoRepository;
        this.usuarioService     = usuarioService;
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
        List<Cotizacion> cotizaciones;

        if (estado != null && !estado.isBlank()) {
            try {
                cotizaciones = cotizacionService.listarPorEstado(Cotizacion.Estado.valueOf(estado));
            } catch (IllegalArgumentException e) {
                cotizaciones = cotizacionService.listarTodas();
            }
        } else if (q != null && !q.isBlank()) {
            cotizaciones = cotizacionService.buscar(q);
        } else {
            cotizaciones = cotizacionService.listarTodas();
        }

        model.addAttribute("cotizaciones", cotizaciones);
        model.addAttribute("q", q);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("estados", Cotizacion.Estado.values());
        return "cotizaciones/lista";
    }

    // ── Detalle ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, Model model) {
        Optional<Cotizacion> opt = cotizacionService.buscarPorId(id);
        if (opt.isEmpty()) return "redirect:/cotizaciones";
        model.addAttribute("cotizacion", opt.get());
        return "cotizaciones/detalle";
    }

    // ── Nueva cotización ─────────────────────────────────────────────────────

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        List<Cliente> clientes = clienteRepository.findAll().stream()
                .filter(Cliente::isActivo).toList();
        List<Producto> productos = productoRepository.findAll().stream()
                .filter(Producto::isActivo).toList();
        model.addAttribute("clientes", clientes);
        model.addAttribute("productos", productos);
        return "cotizaciones/form";
    }

    @PostMapping("/nueva")
    public String nuevaSubmit(@RequestParam String clienteId,
                               @RequestParam List<String> productoIds,
                               @RequestParam(required = false) List<String> descripciones,
                               @RequestParam List<Integer> cantidades,
                               @RequestParam List<Double> precios,
                               @RequestParam(defaultValue = "0") double descuentoPct,
                               @RequestParam(defaultValue = "19") double ivaPct,
                               @RequestParam(required = false) String observaciones,
                               @RequestParam(defaultValue = "30") int diasVigencia,
                               Authentication auth,
                               RedirectAttributes ra) {
        try {
            String usuarioNombre = resolverNombreUsuario(auth);
            Cotizacion cot = cotizacionService.crear(clienteId, productoIds, descripciones,
                    cantidades, precios, descuentoPct, ivaPct,
                    observaciones, diasVigencia, null, usuarioNombre);
            ra.addFlashAttribute("successMsg",
                    "Cotización " + cot.getNumero() + " creada correctamente.");
            return "redirect:/cotizaciones/" + cot.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/cotizaciones/nueva";
        }
    }

    // ── Editar cotización ────────────────────────────────────────────────────

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        Optional<Cotizacion> opt = cotizacionService.buscarPorId(id);
        if (opt.isEmpty() || !opt.get().isPendiente()) return "redirect:/cotizaciones";
        List<Cliente> clientes = clienteRepository.findAll().stream()
                .filter(Cliente::isActivo).toList();
        List<Producto> productos = productoRepository.findAll().stream()
                .filter(Producto::isActivo).toList();
        model.addAttribute("cotizacion", opt.get());
        model.addAttribute("clientes", clientes);
        model.addAttribute("productos", productos);
        return "cotizaciones/form";
    }

    @PostMapping("/{id}/editar")
    public String editarSubmit(@PathVariable String id,
                                @RequestParam String clienteId,
                                @RequestParam List<String> productoIds,
                                @RequestParam(required = false) List<String> descripciones,
                                @RequestParam List<Integer> cantidades,
                                @RequestParam List<Double> precios,
                                @RequestParam(defaultValue = "0") double descuentoPct,
                                @RequestParam(defaultValue = "19") double ivaPct,
                                @RequestParam(required = false) String observaciones,
                                @RequestParam(defaultValue = "30") int diasVigencia,
                                RedirectAttributes ra) {
        try {
            cotizacionService.actualizar(id, clienteId, productoIds, descripciones,
                    cantidades, precios, descuentoPct, ivaPct, observaciones, diasVigencia);
            ra.addFlashAttribute("successMsg", "Cotización actualizada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/cotizaciones/" + id + "/editar";
        }
        return "redirect:/cotizaciones/" + id;
    }

    // ── Cambio de estado ─────────────────────────────────────────────────────

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable String id,
                                 @RequestParam String estado,
                                 RedirectAttributes ra) {
        try {
            Cotizacion.Estado nuevoEstado = Cotizacion.Estado.valueOf(estado);
            cotizacionService.cambiarEstado(id, nuevoEstado);
            ra.addFlashAttribute("successMsg",
                    "Estado actualizado a " + nuevoEstado.name() + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cotizaciones/" + id;
    }

    // ── Descargar PDF ─────────────────────────────────────────────────────────

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable String id) {
        Optional<Cotizacion> opt = cotizacionService.buscarPorId(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        byte[] pdf = pdfService.generar(opt.get());
        String filename = "cotizacion-" + opt.get().getNumero() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    // ── Eliminar (solo ADMIN) ──────────────────────────────────────────

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            cotizacionService.eliminar(id);
            ra.addFlashAttribute("successMsg", "Cotización eliminada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "No se pudo eliminar la cotización: " + e.getMessage());
        }
        return "redirect:/cotizaciones";
    }}
