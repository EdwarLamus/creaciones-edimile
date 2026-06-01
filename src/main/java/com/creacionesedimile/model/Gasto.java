package com.creacionesedimile.model;

import com.google.cloud.Timestamp;

public class Gasto {

    /**
     * Categorías de gasto — útiles para filtrar y agrupar en reportes.
     */
    public enum Categoria {
        NOMINA,
        ARRIENDO,
        SERVICIOS_PUBLICOS,
        SUMINISTROS,
        MANTENIMIENTO,
        MARKETING,
        TRANSPORTE,
        IMPUESTOS,
        OTROS
    }

    public enum MetodoPago { EFECTIVO, TRANSFERENCIA, CHEQUE, TARJETA }

    private String id;
    private String numero;          // GAS-0001

    private String     nombre;      // Motivo / nombre del gasto
    private String     descripcion; // Detalle adicional
    private Categoria  categoria;
    private double     total;

    // Pago
    private Timestamp  fechaPago;   // Fecha en que se realizó el pago
    private MetodoPago metodoPago;
    private String     proveedor;   // Proveedor / acreedor (opcional)
    private String     comprobante; // Nro. de factura o recibo (opcional)

    // Auditoría
    private String    usuarioId;
    private String    usuarioNombre;
    private Timestamp fechaCreacion;

    public Gasto() {}

    // ── Getters y Setters ───────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public Timestamp getFechaPago() { return fechaPago; }
    public void setFechaPago(Timestamp fechaPago) { this.fechaPago = fechaPago; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public String getComprobante() { return comprobante; }
    public void setComprobante(String comprobante) { this.comprobante = comprobante; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
