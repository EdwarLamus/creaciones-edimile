package com.creacionesedimile.model;

import java.util.Date;

/**
 * Modelo de dominio: Producto del catálogo.
 *
 * Se persiste en la colección "productos" de Firebase Firestore.
 * Referencia a su categoría por ID y nombre desnormalizado para
 * evitar lecturas adicionales al listar (RF-03.2, RF-03.5, RF-03.6).
 */
public class Producto {

    /** ID del documento en Firestore (generado automáticamente) */
    private String id;

    private String nombre;
    private String descripcion;

    /** ID del documento de Categoria en Firestore */
    private String categoriaId;

    /** Nombre de la categoría desnormalizado para mostrar en listas */
    private String categoriaNombre;

    /** Precio base del producto (sin IVA ni descuentos) */
    private double precioBase;

    /**
     * URL de la imagen de referencia.
     * Puede ser una URL externa o una ruta relativa al servidor.
     * Vacío o null = sin imagen asignada.
     */
    private String imagenUrl;

    /** false = producto desactivado (eliminación lógica) */
    private boolean activo;

    private Date fechaCreacion;

    // -------------------------------------------------------
    // Constructores
    // -------------------------------------------------------

    public Producto() {}

    public Producto(String nombre, String descripcion, String categoriaId,
                    String categoriaNombre, double precioBase, String imagenUrl) {
        this.nombre          = nombre;
        this.descripcion     = descripcion;
        this.categoriaId     = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.precioBase      = precioBase;
        this.imagenUrl       = imagenUrl;
        this.activo          = true;
        this.fechaCreacion   = new Date();
    }

    // -------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public double getPrecioBase() { return precioBase; }
    public void setPrecioBase(double precioBase) { this.precioBase = precioBase; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    public boolean tieneImagen() {
        return imagenUrl != null && !imagenUrl.isBlank();
    }
}
