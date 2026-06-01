package com.creacionesedimile.controller;

import com.creacionesedimile.service.EmailService;
import com.creacionesedimile.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controlador para el flujo de restablecimiento de contraseña.
 *
 * Rutas:
 *  GET  /forgot-password          → formulario para ingresar email
 *  POST /forgot-password          → genera token y envía email
 *  GET  /reset-password?token=xxx → formulario para nueva contraseña
 *  POST /reset-password           → actualiza contraseña y redirige al login
 */
@Controller
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private final PasswordResetService passwordResetService;
    private final EmailService         emailService;

    public PasswordResetController(PasswordResetService passwordResetService,
                                   EmailService emailService) {
        this.passwordResetService = passwordResetService;
        this.emailService         = emailService;
    }

    // ----------------------------------------------------------
    // GET /forgot-password
    // ----------------------------------------------------------

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    // ----------------------------------------------------------
    // POST /forgot-password
    // ----------------------------------------------------------

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        HttpServletRequest request,
                                        Model model) {
        Optional<String> tokenOpt = passwordResetService.generateResetToken(email.trim().toLowerCase());

        // Siempre mostramos el mismo mensaje para no revelar si el email existe (seguridad)
        model.addAttribute("emailEnviado", true);
        model.addAttribute("email", email.trim().toLowerCase());

        if (tokenOpt.isPresent()) {
            String baseUrl  = request.getScheme() + "://" + request.getServerName()
                              + ":" + request.getServerPort();
            String resetUrl = baseUrl + "/reset-password?token=" + tokenOpt.get();
            try {
                emailService.enviarRecuperacionContrasena(email.trim().toLowerCase(), resetUrl);
            } catch (Exception ex) {
                log.error("[PasswordReset] Fallo al enviar correo a {}: {}", email, ex.getMessage(), ex);
            }
        } else {
            log.warn("[PasswordReset] Email no encontrado en el sistema: {}", email.trim().toLowerCase());
        }

        return "forgot-password";
    }

    // ----------------------------------------------------------
    // GET /reset-password
    // ----------------------------------------------------------

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        if (!passwordResetService.isTokenValid(token)) {
            model.addAttribute("tokenInvalido", true);
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    // ----------------------------------------------------------
    // POST /reset-password
    // ----------------------------------------------------------

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttrs,
                                       Model model) {

        // Validar que las contraseñas coincidan
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("errorMsg", "Las contraseñas no coinciden.");
            return "reset-password";
        }

        // Validar política de contraseña
        String errorPolitica = validarPolitica(password);
        if (errorPolitica != null) {
            model.addAttribute("token", token);
            model.addAttribute("errorMsg", errorPolitica);
            return "reset-password";
        }

        try {
            passwordResetService.resetPassword(token, password);
            redirectAttrs.addFlashAttribute("successMsg",
                    "Contraseña actualizada correctamente. Inicia sesión con tu nueva contraseña.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("token", token);
            model.addAttribute("errorMsg", e.getMessage());
            return "reset-password";
        }
    }

    // ----------------------------------------------------------
    // Validación de política de contraseñas
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
