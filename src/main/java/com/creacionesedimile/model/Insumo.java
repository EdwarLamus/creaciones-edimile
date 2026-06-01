package com.creacionesedimile.model;

import com.google.cloud.Timestamp;

public class Insumo {

    private String id;
    private String nombre;
    private String descripcion;
    private String unidadMedida;   // Ej: "unidades", "metros", "litros", "kg"
    private double stockActual;
    private double stockMinimo;
    private double precioUnitario;
    private boolean activo;
    private Timestamp fechaCreacion;

    public Insumo() {}

    // ── Getters y Setters ───────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public double getStockActual() { return stockActual; }
    public void setStockActual(double stockActual) { this.stockActual = stockActual; }

    public double getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(double stockMinimo) { this.stockMinimo = stockMinimo; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /** Devuelve true si el stock actual está en o por debajo del mínimo. */
    public boolean isBajoStock() {
        return stockActual <= stockMinimo;
    }
}
