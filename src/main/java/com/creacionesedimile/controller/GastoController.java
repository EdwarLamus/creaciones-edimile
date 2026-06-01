package com.creacionesedimile.controller;

import com.creacionesedimile.model.Gasto;
import com.creacionesedimile.service.ExcelExportService;
import com.creacionesedimile.service.GastoService;
import com.creacionesedimile.service.UsuarioService;
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
@RequestMapping("/gastos")
public class GastoController {

    private final GastoService       gastoService;
    private final ExcelExportService excelService;
    private final UsuarioService     usuarioService;

    public GastoController(GastoService gastoService, ExcelExportService excelService,
                            UsuarioService usuarioService) {
        this.gastoService   = gastoService;
        this.excelService   = excelService;
        this.usuarioService = usuarioService;
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
                         @RequestParam(required = false) String categoria,
                         Model model) {
        List<Gasto> gastos;

        if (categoria != null && !categoria.isBlank()) {
            try {
                gastos = gastoService.listarPorCategoria(Gasto.Categoria.valueOf(categoria));
            } catch (IllegalArgumentException e) {
                gastos = gastoService.listarTodos();
            }
        } else if (q != null && !q.isBlank()) {
            gastos = gastoService.buscar(q);
        } else {
            gastos = gastoService.listarTodos();
        }

        // Total filtrado (útil para mostrar la suma de la vista actual)
        double totalFiltrado = gastos.stream().mapToDouble(Gasto::getTotal).sum();

        model.addAttribute("gastos",         gastos);
        model.addAttribute("totalFiltrado",  totalFiltrado);
        model.addAttribute("q",              q);
        model.addAttribute("categoriaFiltro",categoria);
        model.addAttribute("categorias",     Gasto.Categoria.values());
        return "gastos/lista";
    }

    // ── Exportar Excel ───────────────────────────────────────────────────────

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel() throws Exception {
        List<Gasto> gastos = gastoService.listarTodos();
        byte[] bytes = excelService.exportarGastos(gastos);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=gastos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // ── Detalle ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, Model model) {
        Optional<Gasto> opt = gastoService.buscarPorId(id);
        if (opt.isEmpty()) return "redirect:/gastos";
        model.addAttribute("gasto", opt.get());
        return "gastos/detalle";
    }

    // ── Nuevo ────────────────────────────────────────────────────────────────

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("categorias",  Gasto.Categoria.values());
        model.addAttribute("metodosPago", Gasto.MetodoPago.values());
        return "gastos/form";
    }

    @PostMapping("/nuevo")
    public String nuevo(@RequestParam String nombre,
                         @RequestParam(required = false) String descripcion,
                         @RequestParam String categoria,
                         @RequestParam(required = false, defaultValue = "0") double total,
                         @RequestParam String fechaPago,
                         @RequestParam String metodoPago,
                         @RequestParam(required = false) String proveedor,
                         @RequestParam(required = false) String comprobante,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            String uid    = auth.getName();
            String uNombre = resolverNombreUsuario(auth);

            Gasto creado = gastoService.crear(nombre, descripcion, categoria, total,
                    fechaPago, metodoPago, proveedor, comprobante, uid, uNombre);
            ra.addFlashAttribute("successMsg", "Gasto " + creado.getNumero() + " registrado correctamente.");
            return "redirect:/gastos/" + creado.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al registrar el gasto: " + e.getMessage());
            return "redirect:/gastos/nuevo";
        }
    }

    // ── Editar ────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        Optional<Gasto> opt = gastoService.buscarPorId(id);
        if (opt.isEmpty()) return "redirect:/gastos";
        model.addAttribute("gasto",       opt.get());
        model.addAttribute("categorias",  Gasto.Categoria.values());
        model.addAttribute("metodosPago", Gasto.MetodoPago.values());
        return "gastos/form";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable String id,
                          @RequestParam String nombre,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam String categoria,
                          @RequestParam(required = false, defaultValue = "0") double total,
                          @RequestParam String fechaPago,
                          @RequestParam String metodoPago,
                          @RequestParam(required = false) String proveedor,
                          @RequestParam(required = false) String comprobante,
                          RedirectAttributes ra) {
        try {
            gastoService.actualizar(id, nombre, descripcion, categoria, total,
                    fechaPago, metodoPago, proveedor, comprobante);
            ra.addFlashAttribute("successMsg", "Gasto actualizado correctamente.");
            return "redirect:/gastos/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al actualizar el gasto: " + e.getMessage());
            return "redirect:/gastos/" + id + "/editar";
        }
    }

    // ── Eliminar ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            gastoService.eliminar(id);
            ra.addFlashAttribute("successMsg", "Gasto eliminado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al eliminar el gasto: " + e.getMessage());
        }
        return "redirect:/gastos";
    }
}
