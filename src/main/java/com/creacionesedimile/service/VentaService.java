package com.creacionesedimile.service;

import com.creacionesedimile.model.*;
import com.creacionesedimile.repository.ClienteRepository;
import com.creacionesedimile.repository.CotizacionRepository;
import com.creacionesedimile.repository.ProductoRepository;
import com.creacionesedimile.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    private final VentaRepository      ventaRepository;
    private final ClienteRepository    clienteRepository;
    private final ProductoRepository   productoRepository;
    private final CotizacionRepository cotizacionRepository;

    public VentaService(VentaRepository ventaRepository,
                        ClienteRepository clienteRepository,
                        ProductoRepository productoRepository,
                        CotizacionRepository cotizacionRepository) {
        this.ventaRepository      = ventaRepository;
        this.clienteRepository    = clienteRepository;
        this.productoRepository   = productoRepository;
        this.cotizacionRepository = cotizacionRepository;
    }

    // ── Consultas ───────────────────────────────────────────────────────────

    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    public List<Venta> listarPorEstado(Venta.Estado estado) {
        return ventaRepository.findByEstado(estado);
    }

    public List<Venta> listarPorCliente(String clienteId) {
        return ventaRepository.findByClienteId(clienteId);
    }

    public Optional<Venta> buscarPorId(String id) {
        return ventaRepository.findById(id);
    }

    /** Filtrado en memoria por número de factura, nombre de cliente o vendedor. */
    public List<Venta> buscar(String termino) {
        if (termino == null || termino.isBlank()) return listarTodas();
        String t = termino.toLowerCase();
        List<Venta> resultado = new ArrayList<>();
        for (Venta v : listarTodas()) {
            if ((v.getNumero()        != null && v.getNumero().toLowerCase().contains(t))
             || (v.getClienteNombre() != null && v.getClienteNombre().toLowerCase().contains(t))
             || (v.getUsuarioNombre() != null && v.getUsuarioNombre().toLowerCase().contains(t))) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    // ── Creación directa (sin cotización) ───────────────────────────────────

    public Venta crearDirecta(String clienteId,
                               List<String>  productoIds,
                               List<String>  descripciones,
                               List<Integer> cantidades,
                               List<Double>  precios,
                               double        descuentoPct,
                               double        ivaPct,
                               String        metodoPagoStr,
                               String        observaciones,
                               String        usuarioId,
                               String        usuarioNombre) {

        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un cliente para registrar la venta.");
        }
        if (metodoPagoStr == null || metodoPagoStr.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un método de pago.");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + clienteId));

        List<ItemVenta> items = buildItems(productoIds, descripciones, cantidades, precios);
        if (items.isEmpty()) throw new IllegalArgumentException("Debe agregar al menos un producto a la venta.");

        Venta.MetodoPago metodoPago = parseMetodoPago(metodoPagoStr);

        Venta venta = new Venta();
        venta.setNumero(ventaRepository.getNextNumero());
        poblarCliente(venta, cliente);
        venta.setUsuarioId(usuarioId);
        venta.setUsuarioNombre(usuarioNombre);
        venta.setItems(items);
        venta.setDescuentoPorcentaje(descuentoPct);
        venta.setIvaPorcentaje(ivaPct);
        venta.setMetodoPago(metodoPago);
        venta.setObservaciones(observaciones);
        venta.setEstado(Venta.Estado.PENDIENTE);
        venta.recalcularTotales();

        return ventaRepository.save(venta);
    }

    // ── Creación desde cotización aprobada ──────────────────────────────────

    public Venta crearDesdeCotizacion(String cotizacionId,
                                       String metodoPagoStr,
                                       String observaciones,
                                       String usuarioId,
                                       String usuarioNombre) {

        Cotizacion cot = cotizacionRepository.findById(cotizacionId)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada: " + cotizacionId));

        if (!cot.isAprobada()) {
            throw new IllegalStateException("Solo se pueden convertir cotizaciones APROBADAS.");
        }

        Venta.MetodoPago metodoPago = parseMetodoPago(metodoPagoStr);

        // Convertir ítems de cotización a ítems de venta
        List<ItemVenta> items = new ArrayList<>();
        for (ItemCotizacion ic : cot.getItems()) {
            ItemVenta iv = new ItemVenta();
            iv.setProductoId(ic.getProductoId());
            iv.setProductoNombre(ic.getProductoNombre());
            iv.setDescripcionPersonalizacion(ic.getDescripcionPersonalizacion());
            iv.setCantidad(ic.getCantidad());
            iv.setPrecioUnitario(ic.getPrecioUnitario());
            iv.calcularSubtotal();
            items.add(iv);
        }

        Venta venta = new Venta();
        venta.setNumero(ventaRepository.getNextNumero());
        venta.setCotizacionId(cot.getId());
        venta.setCotizacionNumero(cot.getNumero());
        venta.setClienteId(cot.getClienteId());
        venta.setClienteNombre(cot.getClienteNombre());
        venta.setClienteDocumento(cot.getClienteDocumento());
        venta.setClienteTelefono(cot.getClienteTelefono());
        venta.setClienteEmail(cot.getClienteEmail());
        venta.setClienteDireccion(cot.getClienteDireccion());
        venta.setUsuarioId(usuarioId);
        venta.setUsuarioNombre(usuarioNombre);
        venta.setItems(items);
        venta.setDescuentoPorcentaje(cot.getDescuentoPorcentaje());
        venta.setIvaPorcentaje(cot.getIvaPorcentaje());
        venta.setMetodoPago(metodoPago);
        venta.setObservaciones(observaciones != null && !observaciones.isBlank()
                ? observaciones : cot.getObservaciones());
        venta.setEstado(Venta.Estado.PENDIENTE);
        venta.recalcularTotales();

        return ventaRepository.save(venta);
    }

    // ── Cambio de estado ────────────────────────────────────────────────────

    public void registrarPago(String id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + id));
        if (venta.isAnulada()) throw new IllegalStateException("No se puede pagar una venta anulada.");
        ventaRepository.updateEstado(id, Venta.Estado.PAGADA);
    }

    public void anular(String id, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de anulación es obligatorio.");
        }
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + id));
        if (venta.isAnulada()) throw new IllegalStateException("La venta ya está anulada.");
        ventaRepository.updateEstadoYMotivo(id, Venta.Estado.ANULADA, motivo);
    }

    public void eliminar(String id) {
        ventaRepository.delete(id);
    }

    // ── Helpers privados ────────────────────────────────────────────────────

    private void poblarCliente(Venta venta, Cliente cliente) {
        venta.setClienteId(cliente.getId());
        venta.setClienteNombre(cliente.getNombreCompleto());
        venta.setClienteDocumento(cliente.getDocumentoCompleto());
        venta.setClienteTelefono(cliente.getTelefono());
        venta.setClienteEmail(cliente.getEmail());
        venta.setClienteDireccion(cliente.getDireccion());
    }

    private Venta.MetodoPago parseMetodoPago(String value) {
        try {
            return Venta.MetodoPago.valueOf(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Método de pago inválido: " + value);
        }
    }

    private List<ItemVenta> buildItems(List<String>  productoIds,
                                        List<String>  descripciones,
                                        List<Integer> cantidades,
                                        List<Double>  precios) {
        List<ItemVenta> items = new ArrayList<>();
        if (productoIds == null) return items;
        for (int i = 0; i < productoIds.size(); i++) {
            String pid = productoIds.get(i);
            if (pid == null || pid.isBlank()) continue;

            Producto producto = productoRepository.findById(pid)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + pid));

            ItemVenta item = new ItemVenta();
            item.setProductoId(pid);
            item.setProductoNombre(producto.getNombre());
            item.setDescripcionPersonalizacion(
                    (descripciones != null && i < descripciones.size()) ? descripciones.get(i) : null);
            item.setCantidad((cantidades != null && i < cantidades.size()) ? cantidades.get(i) : 1);
            item.setPrecioUnitario((precios != null && i < precios.size()) ? precios.get(i) : 0);
            item.calcularSubtotal();
            items.add(item);
        }
        return items;
    }
}
