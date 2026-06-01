package com.creacionesedimile.controller;

import com.creacionesedimile.model.Insumo;
import com.creacionesedimile.model.MovimientoInsumo;
import com.creacionesedimile.service.ExcelExportService;
import com.creacionesedimile.service.InsumoService;
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
@RequestMapping("/inventario")
public class InventarioController {

    private final InsumoService    insumoService;
    private final ExcelExportService excelService;

    public InventarioController(InsumoService insumoService, ExcelExportService excelService) {
        this.insumoService = insumoService;
        this.excelService  = excelService;
    }

    // ── Listado ─────────────────────────────────────────────────────────────

    @GetMapping
    public String lista(@RequestParam(required = false) String q, Model model) {
        List<Insumo> insumos = (q != null && !q.isBlank())
                ? insumoService.buscar(q)
                : insumoService.listarTodos();

        long bajosDeStock = insumos.stream().filter(Insumo::isBajoStock).count();

        model.addAttribute("insumos", insumos);
        model.addAttribute("q", q);
        model.addAttribute("bajosDeStock", bajosDeStock);
        return "inventario/lista";
    }

    // ── Exportar Excel ───────────────────────────────────────────────────────

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel() throws Exception {
        List<Insumo> insumos = insumoService.listarTodos();
        byte[] bytes = excelService.exportarInventario(insumos);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=inventario.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // ── Nuevo insumo ────────────────────────────────────────────────────────

    @GetMapping("/nuevo")
    public String nuevoForm() {
        return "inventario/form";
    }

    @PostMapping("/nuevo")
    public String nuevoSubmit(@RequestParam String nombre,
                               @RequestParam(required = false) String descripcion,
                               @RequestParam String unidadMedida,
                               @RequestParam(required = false, defaultValue = "0") double stockActual,
                               @RequestParam(required = false, defaultValue = "0") double stockMinimo,
                               @RequestParam(required = false, defaultValue = "0") double precioUnitario,
                               RedirectAttributes ra) {
        try {
            insumoService.registrarInsumo(nombre, descripcion, unidadMedida,
                    stockActual, stockMinimo, precioUnitario);
            ra.addFlashAttribute("successMsg", "Insumo registrado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/inventario/nuevo";
        }
        return "redirect:/inventario";
    }

    // ── Editar insumo ───────────────────────────────────────────────────────

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        Optional<Insumo> opt = insumoService.buscarPorId(id);
        if (opt.isEmpty()) return "redirect:/inventario";
        model.addAttribute("insumo", opt.get());
        return "inventario/form";
    }

    @PostMapping("/{id}/editar")
    public String editarSubmit(@PathVariable String id,
                                @RequestParam String nombre,
                                @RequestParam(required = false) String descripcion,
                                @RequestParam String unidadMedida,
                                @RequestParam(required = false, defaultValue = "0") double stockMinimo,
                                @RequestParam(required = false, defaultValue = "0") double precioUnitario,
                                RedirectAttributes ra) {
        try {
            insumoService.actualizarInsumo(id, nombre, descripcion,
                    unidadMedida, stockMinimo, precioUnitario);
            ra.addFlashAttribute("successMsg", "Insumo actualizado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/inventario/" + id + "/editar";
        }
        return "redirect:/inventario";
    }

    // ── Activar / desactivar ────────────────────────────────────────────────

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable String id,
                                 @RequestParam boolean activo,
                                 RedirectAttributes ra) {
        try {
            insumoService.actualizarEstado(id, activo);
            ra.addFlashAttribute("successMsg",
                    activo ? "Insumo activado." : "Insumo desactivado.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/inventario";
    }

    // ── Entrada de insumo ───────────────────────────────────────────────────

    @GetMapping("/{id}/entrada")
    public String entradaForm(@PathVariable String id, Model model) {
        Optional<Insumo> opt = insumoService.buscarPorId(id);
        if (opt.isEmpty()) return "redirect:/inventario";
        model.addAttribute("insumo", opt.get());
        model.addAttribute("tipoMovimiento", "ENTRADA");
        return "inventario/movimiento";
    }

    @PostMapping("/{id}/entrada")
    public String entradaSubmit(@PathVariable String id,
                                 @RequestParam(required = false, defaultValue = "0") double cantidad,
                                 @RequestParam(required = false) String observacion,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        try {
            String usuarioNombre = auth != null ? auth.getName() : "Sistema";
            insumoService.registrarEntrada(id, cantidad, observacion, null, usuarioNombre);
            ra.addFlashAttribute("successMsg", "Entrada registrada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/inventario/" + id + "/entrada";
        }
        return "redirect:/inventario";
    }

    // ── Salida de insumo ────────────────────────────────────────────────────

    @GetMapping("/{id}/salida")
    public String salidaForm(@PathVariable String id, Model model) {
        Optional<Insumo> opt = insumoService.buscarPorId(id);
        if (opt.isEmpty()) return "redirect:/inventario";
        model.addAttribute("insumo", opt.get());
        model.addAttribute("tipoMovimiento", "SALIDA");
        return "inventario/movimiento";
    }

    @PostMapping("/{id}/salida")
    public String salidaSubmit(@PathVariable String id,
                                @RequestParam(required = false, defaultValue = "0") double cantidad,
                                @RequestParam(required = false) String observacion,
                                Authentication auth,
                                RedirectAttributes ra) {
        try {
            String usuarioNombre = auth != null ? auth.getName() : "Sistema";
            insumoService.registrarSalida(id, cantidad, observacion, null, usuarioNombre);
            ra.addFlashAttribute("successMsg", "Salida registrada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/inventario/" + id + "/salida";
        }
        return "redirect:/inventario";
    }

    // ── Historial de movimientos ────────────────────────────────────────────

    @GetMapping("/{id}/movimientos")
    public String historial(@PathVariable String id, Model model, RedirectAttributes ra) {
        try {
            Optional<Insumo> opt = insumoService.buscarPorId(id);
            if (opt.isEmpty()) return "redirect:/inventario";
            List<MovimientoInsumo> movimientos = insumoService.listarMovimientos(id);
            model.addAttribute("insumo", opt.get());
            model.addAttribute("movimientos", movimientos);
            return "inventario/movimientos";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "No se pudo cargar el historial: " + e.getMessage());
            return "redirect:/inventario";
        }
    }

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            insumoService.eliminar(id);
            ra.addFlashAttribute("successMsg", "Insumo eliminado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "No se pudo eliminar el insumo: " + e.getMessage());
        }
        return "redirect:/inventario";
    }
}
