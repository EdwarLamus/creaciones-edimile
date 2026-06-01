package com.creacionesedimile.service;

import com.creacionesedimile.model.Categoria;
import com.creacionesedimile.model.Producto;
import com.creacionesedimile.repository.CategoriaRepository;
import com.creacionesedimile.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio de negocio para el módulo de Productos y Categorías.
 *
 * Responsabilidades:
 *  - Gestión de categorías: crear, editar, activar/desactivar (RF-03.1)
 *  - Registro y edición de productos (RF-03.2, RF-03.3)
 *  - Activar/desactivar productos (RF-03.4)
 *  - Búsqueda por nombre o categoría (RF-03.5)
 *  - Desnormalizar el nombre de la categoría en el producto para evitar
 *    lecturas extra al listar el catálogo (RF-03.6)
 */
@Service
public class ProductoService {

    private final ProductoRepository  productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository  = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // ===================================================
    // CATEGORÍAS
    // ===================================================

    public List<Categoria> listarTodasCategorias() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> listarCategoriasActivas() {
        return categoriaRepository.findAllActivas();
    }

    public Optional<Categoria> buscarCategoriaPorId(String id) {
        return categoriaRepository.findById(id);
    }

    /**
     * Crea una nueva categoría.
     *
     * @throws IllegalArgumentException si ya existe una categoría con ese nombre
     */
    public String crearCategoria(String nombre, String descripcion) {
        if (categoriaRepository.existsByNombre(nombre)) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría con el nombre: " + nombre);
        }
        Categoria nueva = new Categoria(nombre, descripcion);
        return categoriaRepository.save(nueva);
    }

    public void actualizarCategoria(String id, String nombre, String descripcion) {
        Map<String, Object> campos = new HashMap<>();
        campos.put("nombre",      nombre);
        campos.put("descripcion", descripcion);
        categoriaRepository.update(id, campos);
    }

    public void actualizarEstadoCategoria(String id, boolean activo) {
        Map<String, Object> campos = new HashMap<>();
        campos.put("activo", activo);
        categoriaRepository.update(id, campos);
    }

    // ===================================================
    // PRODUCTOS
    // ===================================================

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> buscarPorId(String id) {
        return productoRepository.findById(id);
    }

    /**
     * Busca productos por nombre o categoría (RF-03.5).
     * Sin término devuelve todos.
     */
    public List<Producto> buscar(String termino) {
        if (termino == null || termino.isBlank()) {
            return productoRepository.findAll();
        }
        return productoRepository.buscar(termino);
    }

    /**
     * Filtra productos por categoría. Sin ID devuelve todos.
     */
    public List<Producto> listarPorCategoria(String categoriaId) {
        if (categoriaId == null || categoriaId.isBlank()) {
            return productoRepository.findAll();
        }
        return productoRepository.findByCategoriaId(categoriaId);
    }

    /**
     * Registra un nuevo producto desnormalizando el nombre de la categoría (RF-03.2).
     */
    public String registrarProducto(String nombre, String descripcion,
                                    String categoriaId, double precioBase,
                                    String imagenUrl) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        if (categoriaId == null || categoriaId.isBlank())
            throw new IllegalArgumentException("Debes seleccionar una categoría.");
        if (precioBase <= 0)
            throw new IllegalArgumentException("El precio base debe ser mayor a cero.");

        String catNombre = categoriaRepository.findById(categoriaId)
                .map(Categoria::getNombre)
                .orElse("");

        Producto nuevo = new Producto(nombre, descripcion, categoriaId,
                                      catNombre, precioBase, imagenUrl);
        return productoRepository.save(nuevo);
    }

    /**
     * Actualiza los datos de un producto existente (RF-03.3).
     * Re-desnormaliza el nombre de la categoría si cambia.
     */
    public void actualizarProducto(String id, String nombre, String descripcion,
                                   String categoriaId, double precioBase,
                                   String imagenUrl) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        if (categoriaId == null || categoriaId.isBlank())
            throw new IllegalArgumentException("Debes seleccionar una categoría.");
        if (precioBase <= 0)
            throw new IllegalArgumentException("El precio base debe ser mayor a cero.");

        String catNombre = categoriaRepository.findById(categoriaId)
                .map(Categoria::getNombre)
                .orElse("");

        Map<String, Object> campos = new HashMap<>();
        campos.put("nombre",          nombre);
        campos.put("descripcion",     descripcion);
        campos.put("categoriaId",     categoriaId);
        campos.put("categoriaNombre", catNombre);
        campos.put("precioBase",      precioBase);
        campos.put("imagenUrl",       imagenUrl);
        productoRepository.update(id, campos);
    }

    /** Activa o desactiva un producto (RF-03.4). */
    public void actualizarEstadoProducto(String id, boolean activo) {
        Map<String, Object> campos = new HashMap<>();
        campos.put("activo", activo);
        productoRepository.update(id, campos);
    }

    public void eliminarProducto(String id) {
        productoRepository.delete(id);
    }

    public void eliminarCategoria(String id) {
        categoriaRepository.delete(id);
    }
}
