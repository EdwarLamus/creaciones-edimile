package com.creacionesedimile.model;

import java.util.Date;

/**
 * Modelo de dominio: Usuario del sistema.
 *
 * Se persiste como documento en la colección "usuarios" de Firebase Firestore.
 * No usa anotaciones JPA ya que Firebase es NoSQL.
 *
 * Roles soportados (almacenados sin prefijo "ROLE_"):
 *   - "ADMIN"    → acceso completo
 *   - "VENDEDOR" → acceso restringido
 */
public class Usuario {

    /** ID del documento en Firestore (generado automáticamente) */
    private String id;

    private String nombre;
    private String apellido;

    /** Usado como "username" en Spring Security */
    private String email;

    /** Contraseña almacenada como hash BCrypt (nunca en texto plano) */
    private String password;

    /** "ADMIN" o "VENDEDOR" */
    private String rol;

    /** false = usuario no puede iniciar sesión */
    private boolean activo;

    private Date fechaCreacion;

    // -------------------------------------------------------
    // Constructores
    // -------------------------------------------------------

    public Usuario() {}

    public Usuario(String nombre, String apellido, String email,
                   String password, String rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.activo = true;
        this.fechaCreacion = new Date();
    }

    // -------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    /** Nombre completo para mostrar en la UI */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
