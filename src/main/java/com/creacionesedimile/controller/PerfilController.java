package com.creacionesedimile.controller;

import com.creacionesedimile.model.Usuario;
import com.creacionesedimile.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para el perfil del usuario autenticado.
 *
 * Rutas:
 *  GET  /perfil                    → vista de perfil (datos personales)
 *  GET  /perfil/configuracion      → formulario cambio de contraseña
 *  POST /perfil/configuracion      → procesa cambio de contraseña
 */
@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ----------------------------------------------------------
    // GET /perfil
    // ----------------------------------------------------------

    @GetMapping
    public String verPerfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "perfil/ver";
    }

    // ----------------------------------------------------------
    // GET /perfil/configuracion
    // ----------------------------------------------------------

    @GetMapping("/configuracion")
    public String verConfiguracion(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "perfil/configuracion";
    }

    // ----------------------------------------------------------
    // POST /perfil/configuracion
    // ----------------------------------------------------------

    @PostMapping("/configuracion")
    public String cambiarContrasena(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("passwordActual")   String passwordActual,
            @RequestParam("passwordNueva")    String passwordNueva,
            @RequestParam("passwordConfirmar") String passwordConfirmar,
            RedirectAttributes redirectAttrs,
            Model model) {

        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar contraseña actual
        if (!usuarioService.verificarContrasena(passwordActual, usuario.getPassword())) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errorMsg", "La contraseña actual no es correcta.");
            return "perfil/configuracion";
        }

        // Validar que las nuevas contraseñas coincidan
        if (!passwordNueva.equals(passwordConfirmar)) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errorMsg", "Las contraseñas nuevas no coinciden.");
            return "perfil/configuracion";
        }

        // Validar política
        String errorPolitica = validarPolitica(passwordNueva);
        if (errorPolitica != null) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errorMsg", errorPolitica);
            return "perfil/configuracion";
        }

        usuarioService.cambiarContrasena(usuario.getId(), passwordNueva);
        redirectAttrs.addFlashAttribute("successMsg",
                "Contraseña actualizada correctamente.");
        return "redirect:/perfil/configuracion";
    }

    // ----------------------------------------------------------
    // Validación de política de contraseñas (igual que PasswordResetController)
    // ----------------------------------------------------------

    private String validarPolitica(String password) {
        if (password == null || password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "La contraseña debe contener al menos una letra mayúscula.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "La contraseña debe contener al menos un número.";
        }
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            return "La contraseña debe contener al menos un carácter especial.";
        }
        return null;
    }
}
