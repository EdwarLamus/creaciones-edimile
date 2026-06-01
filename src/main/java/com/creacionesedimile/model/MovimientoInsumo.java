package com.creacionesedimile.model;

import com.google.cloud.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MovimientoInsumo {

    public enum Tipo { ENTRADA, SALIDA }

    private String id;
    private String insumoId;
    private String insumoNombre;   // desnormalizado para historial
    private Tipo tipo;             // ENTRADA o SALIDA
    private double cantidad;
    private double stockAnterior;
    private double stockResultante;
    private String observacion;
    private String usuarioId;
    private String usuarioNombre;  // desnormalizado
    private Timestamp fecha;

    public MovimientoInsumo() {}

    // ── Getters y Setters ───────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInsumoId() { return insumoId; }
    public void setInsumoId(String insumoId) { this.insumoId = insumoId; }

    public String getInsumoNombre() { return insumoNombre; }
    public void setInsumoNombre(String insumoNombre) { this.insumoNombre = insumoNombre; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public double getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(double stockAnterior) { this.stockAnterior = stockAnterior; }

    public double getStockResultante() { return stockResultante; }
    public void setStockResultante(double stockResultante) { this.stockResultante = stockResultante; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    /** Devuelve la fecha formateada para mostrarse en la vista. */
    public String getFechaTexto() {
        if (fecha == null) return "—";
        return fecha.toDate().toInstant()
                .atZone(ZoneId.of("America/Bogota"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
