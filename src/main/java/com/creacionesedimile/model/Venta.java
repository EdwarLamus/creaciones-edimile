package com.creacionesedimile.model;

import com.google.cloud.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Venta {

    public enum Estado { PENDIENTE, PAGADA, ANULADA }

    public enum MetodoPago { EFECTIVO, TRANSFERENCIA, CHEQUE, TARJETA }

    private String id;
    private String numero;              // FAC-0001

    // Origen (opcional — cuando viene de una cotización aprobada)
    private String cotizacionId;
    private String cotizacionNumero;

    // Cliente (desnormalizado)
    private String clienteId;
    private String clienteNombre;
    private String clienteDocumento;
    private String clienteTelefono;
    private String clienteEmail;
    private String clienteDireccion;

    // Vendedor
    private String usuarioId;
    private String usuarioNombre;

    // Ítems
    private List<ItemVenta> items = new ArrayList<>();

    // Totales
    private double subtotal;
    private double descuentoPorcentaje;
    private double descuentoValor;
    private double ivaPorcentaje;
    private double ivaValor;
    private double total;

    // Metadatos
    private MetodoPago metodoPago;
    private Estado     estado;
    private String     observaciones;
    private String     motivoAnulacion;
    private Timestamp  fechaCreacion;

    public Venta() {}

    // ── Getters y Setters ───────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getCotizacionId() { return cotizacionId; }
    public void setCotizacionId(String cotizacionId) { this.cotizacionId = cotizacionId; }

    public String getCotizacionNumero() { return cotizacionNumero; }
    public void setCotizacionNumero(String cotizacionNumero) { this.cotizacionNumero = cotizacionNumero; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }

    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }

    public String getClienteDireccion() { return clienteDireccion; }
    public void setClienteDireccion(String clienteDireccion) { this.clienteDireccion = clienteDireccion; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public List<ItemVenta> getItems() { return items; }
    public void setItems(List<ItemVenta> items) { this.items = items; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDescuentoPorcentaje() { return descuentoPorcentaje; }
    public void setDescuentoPorcentaje(double descuentoPorcentaje) { this.descuentoPorcentaje = descuentoPorcentaje; }

    public double getDescuentoValor() { return descuentoValor; }
    public void setDescuentoValor(double descuentoValor) { this.descuentoValor = descuentoValor; }

    public double getIvaPorcentaje() { return ivaPorcentaje; }
    public void setIvaPorcentaje(double ivaPorcentaje) { this.ivaPorcentaje = ivaPorcentaje; }

    public double getIvaValor() { return ivaValor; }
    public void setIvaValor(double ivaValor) { this.ivaValor = ivaValor; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String motivoAnulacion) { this.motivoAnulacion = motivoAnulacion; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // ── Helpers ─────────────────────────────────────────────────────────────

    public void recalcularTotales() {
        this.subtotal       = items.stream().mapToDouble(ItemVenta::getSubtotal).sum();
        this.descuentoValor = this.subtotal * (this.descuentoPorcentaje / 100.0);
        double baseIva      = this.subtotal - this.descuentoValor;
        this.ivaValor       = baseIva * (this.ivaPorcentaje / 100.0);
        this.total          = baseIva + this.ivaValor;
    }

    public boolean isPendiente() { return Estado.PENDIENTE.equals(estado); }
    public boolean isPagada()    { return Estado.PAGADA.equals(estado); }
    public boolean isAnulada()   { return Estado.ANULADA.equals(estado); }

    public boolean tieneCotizacion() {
        return cotizacionId != null && !cotizacionId.isBlank();
    }
}
