package com.creacionesedimile.controller;

import com.creacionesedimile.model.Producto;
import com.creacionesedimile.service.FileStorageService;
import com.creacionesedimile.service.ProductoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controlador del módulo de Productos y Categorías.
 *
 * Rutas de productos (todos los usuarios autenticados):
 *   GET  /productos                → lista con búsqueda/filtro (RF-03.5)
 *   GET  /productos/catalogo       → vista catálogo de cards (RF-03.6)
 *   GET  /productos/nuevo          → formulario de registro (RF-03.2)
 *   POST /productos/nuevo          → guardar nuevo producto
 *   GET  /productos/{id}/editar    → formulario de edición (RF-03.3)
 *   POST /productos/{id}/editar    → guardar cambios
 *   POST /productos/{id}/estado    → activar/desactivar (RF-03.4)
 *
 * Rutas de categorías (solo ADMIN — RF-03.1):
 *   GET  /productos/categorias             → lista de categorías
 *   GET  /productos/categorias/nueva       → formulario nueva categoría
 *   POST /productos/categorias/nueva       → guardar categoría
 *   GET  /productos/categorias/{id}/editar → formulario editar categoría
 *   POST /productos/categorias/{id}/editar → guardar cambios
 *   POST /productos/categorias/{id}/estado → activar/desactivar categoría
 */
@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService     productoService;
    private final FileStorageService  fileStorageService;

    public ProductoController(ProductoService productoService,
                              FileStorageService fileStorageService) {
        this.productoService    = productoService;
        this.fileStorageService = fileStorageService;
    }

    // ===================================================
    // LISTA DE PRODUCTOS
    // ===================================================

    @GetMapping
    public String listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoriaId,
            Model model) {

        if (q != null && !q.isBlank()) {
            model.addAttribute("productos", productoService.buscar(q));
        } else {
            model.addAttribute("productos", productoService.listarPorCategoria(categoriaId));
        }
        model.addAttribute("categorias",  productoService.listarCategoriasActivas());
        model.addAttribute("q",           q);
        model.addAttribute("categoriaId", categoriaId);
        return "productos/lista";
    }

    // ===================================================
    // CATÁLOGO (RF-03.6)
    // ===================================================

    @GetMapping("/catalogo")
    public String catalogo(
            @RequestParam(required = false) String categoriaId,
            Model model) {

        model.addAttribute("productos",   productoService.listarPorCategoria(categoriaId));
        model.addAttribute("categorias",  productoService.listarCategoriasActivas());
        model.addAttribute("categoriaId", categoriaId);
        return "productos/catalogo";
    }

    // ===================================================
    // NUEVO PRODUCTO
    // ===================================================

    @GetMapping("/nuevo")
    public String formNuevo(Model model) {
        model.addAttribute("categorias", productoService.listarCategoriasActivas());
        return "productos/form";
    }

    @PostMapping("/nuevo")
    public String guardarNuevo(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam String categoriaId,
            @RequestParam(required = false, defaultValue = "0") double precioBase,
            @RequestParam(required = false) String imagenUrl,
            @RequestParam(required = false) MultipartFile imagenArchivo,
            RedirectAttributes redirectAttributes) {

        try {
            String urlFinal = resolverUrl(imagenUrl, imagenArchivo);
            productoService.registrarProducto(nombre, descripcion, categoriaId,
                                               precioBase, urlFinal);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Producto \"" + nombre + "\" registrado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/productos/nuevo";
        }
        return "redirect:/productos";
    }

    // ===================================================
    // EDITAR PRODUCTO
    // ===================================================

    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable String id, Model model,
                             RedirectAttributes redirectAttributes) {
        Optional<Producto> producto = productoService.buscarPorId(id);
        if (producto.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Producto no encontrado.");
            return "redirect:/productos";
        }
        model.addAttribute("producto",   producto.get());
        model.addAttribute("categorias", productoService.listarCategoriasActivas());
        return "productos/form";
    }

    @PostMapping("/{id}/editar")
    public String guardarEdicion(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam String categoriaId,
            @RequestParam(required = false, defaultValue = "0") double precioBase,
            @RequestParam(required = false) String imagenUrl,
            @RequestParam(required = false) MultipartFile imagenArchivo,
            RedirectAttributes redirectAttributes) {

        try {
            String urlFinal = resolverUrl(imagenUrl, imagenArchivo);
            productoService.actualizarProducto(id, nombre, descripcion, categoriaId,
                                               precioBase, urlFinal);
            redirectAttributes.addFlashAttribute("successMsg", "Producto actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/productos/" + id + "/editar";
        }
        return "redirect:/productos";
    }

    // -------------------------------------------------------
    // Helper: decide la URL final de imagen
    // -------------------------------------------------------

    /**
     * Si el usuario subió un archivo local, se guarda y devuelve su URL.
     * Si no, se usa la URL de texto ingresada (puede ser vacía).
     */
    private String resolverUrl(String imagenUrl, MultipartFile imagenArchivo) {
        if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
            return fileStorageService.guardar(imagenArchivo);
        }
        return (imagenUrl != null && !imagenUrl.isBlank()) ? imagenUrl.trim() : null;
    }

    // ===================================================
    // ESTADO PRODUCTO
    // ===================================================

    @PostMapping("/{id}/estado")
    public String cambiarEstadoProducto(
            @PathVariable String id,
            @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {

        productoService.actualizarEstadoProducto(id, activo);
        redirectAttributes.addFlashAttribute("successMsg",
                activo ? "Producto activado." : "Producto desactivado.");
        return "redirect:/productos";
    }

    // ===================================================
    // CATEGORÍAS — SOLO ADMIN (RF-03.1)
    // ===================================================

    @GetMapping("/categorias")
    @PreAuthorize("hasRole('ADMIN')")
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", productoService.listarTodasCategorias());
        return "productos/categorias/lista";
    }

    @GetMapping("/categorias/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public String formNuevaCategoria() {
        return "productos/categorias/form";
    }

    @PostMapping("/categorias/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarNuevaCategoria(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            RedirectAttributes redirectAttributes) {

        try {
            productoService.crearCategoria(nombre, descripcion);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Categoría \"" + nombre + "\" creada exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/productos/categorias/nueva";
        }
        return "redirect:/productos/categorias";
    }

    @GetMapping("/categorias/{id}/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public String formEditarCategoria(@PathVariable String id, Model model,
                                      RedirectAttributes redirectAttributes) {
        var cat = productoService.buscarCategoriaPorId(id);
        if (cat.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Categoría no encontrada.");
            return "redirect:/productos/categorias";
        }
        model.addAttribute("categoria", cat.get());
        return "productos/categorias/form";
    }

    @PostMapping("/categorias/{id}/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarEdicionCategoria(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            RedirectAttributes redirectAttributes) {

        productoService.actualizarCategoria(id, nombre, descripcion);
        redirectAttributes.addFlashAttribute("successMsg", "Categoría actualizada correctamente.");
        return "redirect:/productos/categorias";
    }

    @PostMapping("/categorias/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public String cambiarEstadoCategoria(
            @PathVariable String id,
            @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {

        productoService.actualizarEstadoCategoria(id, activo);
        redirectAttributes.addFlashAttribute("successMsg",
                activo ? "Categoría activada." : "Categoría desactivada.");
        return "redirect:/productos/categorias";
    }

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarProducto(@PathVariable String id, RedirectAttributes ra) {
        try {
            productoService.eliminarProducto(id);
            ra.addFlashAttribute("successMsg", "Producto eliminado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "No se pudo eliminar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    @PostMapping("/categorias/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarCategoria(@PathVariable String id, RedirectAttributes ra) {
        try {
            productoService.eliminarCategoria(id);
            ra.addFlashAttribute("successMsg", "Categoría eliminada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "No se pudo eliminar la categoría: " + e.getMessage());
        }
        return "redirect:/productos/categorias";
    }
}
