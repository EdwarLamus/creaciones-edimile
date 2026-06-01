package com.creacionesedimile.model;

import java.util.Date;

/**
 * Modelo de dominio: Categoría de productos.
 *
 * Se persiste en la colección "categorias" de Firebase Firestore.
 * Solo el Administrador puede gestionar categorías (RF-03.1).
 */
public class Categoria {

    /** ID del documento en Firestore (generado automáticamente) */
    private String id;

    private String nombre;
    private String descripcion;

    /** false = categoría desactivada (eliminación lógica) */
    private boolean activo;

    private Date fechaCreacion;

    // -------------------------------------------------------
    // Constructores
    // -------------------------------------------------------

    public Categoria() {}

    public Categoria(String nombre, String descripcion) {
        this.nombre       = nombre;
        this.descripcion  = descripcion;
        this.activo       = true;
        this.fechaCreacion = new Date();
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

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
