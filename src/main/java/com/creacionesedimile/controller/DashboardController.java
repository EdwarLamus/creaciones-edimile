package com.creacionesedimile.controller;

import com.creacionesedimile.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador del Dashboard y módulo de Gestión de Usuarios.
 *
 * Las rutas bajo /admin/** están protegidas en SecurityConfig:
 * solo usuarios con rol ADMIN pueden acceder.
 */
@Controller
public class DashboardController {

    private final UsuarioService usuarioService;

    public DashboardController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // -------------------------------------------------------
    // Dashboard principal
    // -------------------------------------------------------

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Obtenemos el email del usuario actualmente autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Cargamos sus datos desde Firestore para personalizar la vista
        usuarioService.buscarPorEmail(email)
                .ifPresent(u -> model.addAttribute("usuario", u));

        return "dashboard"; // → templates/dashboard.html
    }

    // -------------------------------------------------------
    // Gestión de Usuarios (solo ADMIN)
    // -------------------------------------------------------

    /** Lista todos los usuarios del sistema */
    @GetMapping("/admin/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/usuarios/lista"; // → templates/admin/usuarios/lista.html
    }

    /** Muestra el formulario para crear un nuevo usuario */
    @GetMapping("/admin/usuarios/nuevo")
    public String formNuevoUsuario() {
        return "admin/usuarios/form"; // → templates/admin/usuarios/form.html
    }

    /** Procesa el formulario de creación de usuario */
    @PostMapping("/admin/usuarios/nuevo")
    public String crearUsuario(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String rol,
            RedirectAttributes redirectAttributes) {

        try {
            usuarioService.crearUsuario(nombre, apellido, email, password, rol);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Usuario " + nombre + " " + apellido + " creado exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/usuarios/nuevo";
        }

        return "redirect:/admin/usuarios";
    }

    /** Activa o desactiva un usuario */
    @PostMapping("/admin/usuarios/{id}/estado")
    public String cambiarEstado(
            @PathVariable String id,
            @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {

        usuarioService.actualizarEstado(id, activo);
        String msg = activo ? "Usuario activado correctamente." : "Usuario desactivado correctamente.";
        redirectAttributes.addFlashAttribute("successMsg", msg);

        return "redirect:/admin/usuarios";
    }
}
