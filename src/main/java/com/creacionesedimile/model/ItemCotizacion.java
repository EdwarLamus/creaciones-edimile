package com.creacionesedimile.model;

public class ItemCotizacion {

    private String productoId;
    private String productoNombre;
    private String descripcionPersonalizacion;
    private int cantidad;
    private double precioUnitario;
    private double subtotal; // cantidad * precioUnitario

    public ItemCotizacion() {}

    // ── Getters y Setters ───────────────────────────────────────────────────

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getDescripcionPersonalizacion() { return descripcionPersonalizacion; }
    public void setDescripcionPersonalizacion(String descripcionPersonalizacion) {
        this.descripcionPersonalizacion = descripcionPersonalizacion;
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    // ── Helper ───────────────────────────────────────────────────────────────

    public void calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
    }
}
