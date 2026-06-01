package com.creacionesedimile.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Manejador global de excepciones no capturadas.
 * Evita la Whitelabel Error Page mostrando páginas de error amigables.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // ─── Parámetro numérico enviado vacío o con texto ────────────────────────

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                           HttpServletRequest request) {
        String campo = ex.getName();
        ModelAndView mav = buildErrorPage(
                "400",
                "Dato inválido en el formulario",
                "El campo \"" + campo + "\" recibió un valor que no es válido. " +
                "Asegúrate de ingresar solo números donde se requiera.",
                request.getRequestURI()
        );
        mav.setStatus(HttpStatus.BAD_REQUEST);
        return mav;
    }

    // ─── Campo requerido enviado vacío ───────────────────────────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleMissingParam(MissingServletRequestParameterException ex,
                                           HttpServletRequest request) {
        ModelAndView mav = buildErrorPage(
                "400",
                "Campos incompletos",
                "El campo \"" + ex.getParameterName() + "\" es obligatorio y no fue enviado. " +
                "Por favor completa todos los campos requeridos.",
                request.getRequestURI()
        );
        mav.setStatus(HttpStatus.BAD_REQUEST);
        return mav;
    }

    // ─── Recurso no encontrado ────────────────────────────────────────────────

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(NoHandlerFoundException ex,
                                       HttpServletRequest request) {
        ModelAndView mav = buildErrorPage(
                "404",
                "Página no encontrada",
                "La dirección <strong>" + request.getRequestURI() + "</strong> no existe en el sistema.",
                "/dashboard"
        );
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }

    // ─── IllegalArgumentException (validaciones de negocio) ─────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex,
                                               HttpServletRequest request) {
        ModelAndView mav = buildErrorPage(
                "400",
                "Error de validación",
                ex.getMessage(),
                obtenerReferer(request)
        );
        mav.setStatus(HttpStatus.BAD_REQUEST);
        return mav;
    }

    // ─── Cualquier otra excepción ────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneric(Exception ex, HttpServletRequest request) {
        ModelAndView mav = buildErrorPage(
                "500",
                "Error interno del servidor",
                "Ocurrió un problema inesperado. Si el error persiste, contacta al administrador.",
                "/dashboard"
        );
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }

    // ─── Utilidades ──────────────────────────────────────────────────────────

    private ModelAndView buildErrorPage(String codigo, String titulo, String mensaje, String volver) {
        ModelAndView mav = new ModelAndView("error/error-personalizado");
        mav.addObject("errorCodigo",  codigo);
        mav.addObject("errorTitulo",  titulo);
        mav.addObject("errorMensaje", mensaje);
        mav.addObject("volverUrl",    volver != null ? volver : "/dashboard");
        return mav;
    }

    private String obtenerReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            // Solo usar el path, no la URL completa por seguridad
            try {
                java.net.URL url = new java.net.URL(referer);
                return url.getPath();
            } catch (Exception ignored) { }
        }
        return "/dashboard";
    }
}
