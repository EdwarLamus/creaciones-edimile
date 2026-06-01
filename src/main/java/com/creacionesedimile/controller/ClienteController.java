package com.creacionesedimile.controller;

import com.creacionesedimile.model.Cliente;
import com.creacionesedimile.service.ClienteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controlador del módulo de Gestión de Clientes.
 *
 * Rutas:
 *   GET  /clientes              → lista con búsqueda (RF-02.4)
 *   GET  /clientes/nuevo        → formulario de registro (RF-02.1)
 *   POST /clientes/nuevo        → guardar nuevo cliente
 *   GET  /clientes/{id}/editar  → formulario de edición (RF-02.2)
 *   POST /clientes/{id}/editar  → guardar cambios
 *   POST /clientes/{id}/estado  → activar/desactivar (RF-02.3)
 */
@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // -------------------------------------------------------
    // Listado con búsqueda
    // -------------------------------------------------------

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("clientes", clienteService.buscar(q));
        model.addAttribute("q", q);
        return "clientes/lista";
    }

    // -------------------------------------------------------
    // Nuevo cliente
    // -------------------------------------------------------

    @GetMapping("/nuevo")
    public String formNuevo() {
        return "clientes/form";
    }

    @PostMapping("/nuevo")
    public String guardarNuevo(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam(required = false) String empresa,
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento,
            @RequestParam String telefono,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String direccion,
            RedirectAttributes redirectAttributes) {

        try {
            clienteService.registrarCliente(nombre, apellido, empresa, tipoDocumento,
                                             numeroDocumento, telefono, email, direccion);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Cliente " + nombre + " " + apellido + " registrado exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/clientes/nuevo";
        }

        return "redirect:/clientes";
    }

    // -------------------------------------------------------
    // Editar cliente
    // -------------------------------------------------------

    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable String id, Model model,
                             RedirectAttributes redirectAttributes) {
        Optional<Cliente> cliente = clienteService.buscarPorId(id);
        if (cliente.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Cliente no encontrado.");
            return "redirect:/clientes";
        }
        model.addAttribute("cliente", cliente.get());
        return "clientes/form";
    }

    @PostMapping("/{id}/editar")
    public String guardarEdicion(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam(required = false) String empresa,
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento,
            @RequestParam String telefono,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String direccion,
            RedirectAttributes redirectAttributes) {

        try {
            clienteService.actualizarCliente(id, nombre, apellido, empresa, tipoDocumento,
                                              numeroDocumento, telefono, email, direccion);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Cliente actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/clientes/" + id + "/editar";
        }

        return "redirect:/clientes";
    }

    // -------------------------------------------------------
    // Cambiar estado (activar / desactivar)
    // -------------------------------------------------------

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable String id,
            @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {

        clienteService.actualizarEstado(id, activo);
        String msg = activo ? "Cliente activado correctamente."
                            : "Cliente desactivado correctamente.";
        redirectAttributes.addFlashAttribute("successMsg", msg);
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            clienteService.eliminar(id);
            ra.addFlashAttribute("successMsg", "Cliente eliminado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "No se pudo eliminar el cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }
}
