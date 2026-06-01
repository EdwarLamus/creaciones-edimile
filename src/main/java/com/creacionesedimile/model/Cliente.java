package com.creacionesedimile.model;

import java.util.Date;

/**
 * Modelo de dominio: Cliente del negocio.
 *
 * Se persiste en la colección "clientes" de Firebase Firestore.
 * Campos opcionales: empresa.
 */
public class Cliente {

    /** ID del documento en Firestore (generado automáticamente) */
    private String id;

    private String nombre;
    private String apellido;

    /** Nombre de la empresa u organización (puede ser null) */
    private String empresa;

    /** "CC", "NIT", "CE", "TI", "PASAPORTE" */
    private String tipoDocumento;

    /** Número de documento — debe ser único en la colección */
    private String numeroDocumento;

    private String telefono;
    private String email;
    private String direccion;

    /** false = cliente desactivado (eliminación lógica) */
    private boolean activo;

    private Date fechaCreacion;

    // -------------------------------------------------------
    // Constructores
    // -------------------------------------------------------

    public Cliente() {}

    public Cliente(String nombre, String apellido, String empresa,
                   String tipoDocumento, String numeroDocumento,
                   String telefono, String email, String direccion) {
        this.nombre          = nombre;
        this.apellido        = apellido;
        this.empresa         = empresa;
        this.tipoDocumento   = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.telefono        = telefono;
        this.email           = email;
        this.direccion       = direccion;
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

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getDocumentoCompleto() {
        return tipoDocumento + " " + numeroDocumento;
    }
}
