package com.creacionesedimile.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador de Autenticación.
 *
 * Solo maneja los endpoints públicos relacionados al login/logout.
 * Spring Security procesa las credenciales en POST /login internamente
 * (no se necesita un método @PostMapping aquí para esa ruta).
 */
@Controller
public class AuthController {

    /**
     * GET /login
     * Muestra el formulario de inicio de sesión.
     *
     * Spring Security redirige aquí automáticamente cuando:
     *  - Se intenta acceder a una ruta protegida sin sesión activa
     *  - Las credenciales son incorrectas (?error=true)
     *  - El usuario cierra sesión exitosamente (?logout=true)
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",  required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg",
                    "Correo o contraseña incorrectos. Verifica tus datos.");
        }

        if (logout != null) {
            model.addAttribute("logoutMsg",
                    "Sesión cerrada correctamente.");
        }

        return "login"; // → templates/login.html
    }

    /**
     * GET /403
     * Página de acceso denegado cuando el rol del usuario
     * no tiene permisos para la ruta solicitada.
     */
    @GetMapping("/403")
    public String accessDenied() {
        return "error/403"; // → templates/error/403.html
    }

    /**
     * GET /
     * Redirige la raíz del sitio al dashboard (Spring Security redirigirá
     * al login si no hay sesión activa).
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}
