package com.creacionesedimile.service;

import com.creacionesedimile.model.Cliente;
import com.creacionesedimile.model.Cotizacion;
import com.creacionesedimile.model.ItemCotizacion;
import com.creacionesedimile.model.Producto;
import com.creacionesedimile.repository.ClienteRepository;
import com.creacionesedimile.repository.CotizacionRepository;
import com.creacionesedimile.repository.ProductoRepository;
import com.google.cloud.Timestamp;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final ClienteRepository    clienteRepository;
    private final ProductoRepository   productoRepository;

    public CotizacionService(CotizacionRepository cotizacionRepository,
                              ClienteRepository clienteRepository,
                              ProductoRepository productoRepository) {
        this.cotizacionRepository = cotizacionRepository;
        this.clienteRepository    = clienteRepository;
        this.productoRepository   = productoRepository;
    }

    // ── Consultas ───────────────────────────────────────────────────────────

    public List<Cotizacion> listarTodas() {
        return cotizacionRepository.findAll();
    }

    public List<Cotizacion> listarPorEstado(Cotizacion.Estado estado) {
        return cotizacionRepository.findByEstado(estado);
    }

    public List<Cotizacion> listarPorCliente(String clienteId) {
        return cotizacionRepository.findByClienteId(clienteId);
    }

    public Optional<Cotizacion> buscarPorId(String id) {
        return cotizacionRepository.findById(id);
    }

    /** Filtrado en memoria por número, nombre de cliente o vendedor. */
    public List<Cotizacion> buscar(String termino) {
        if (termino == null || termino.isBlank()) return listarTodas();
        String t = termino.toLowerCase();
        List<Cotizacion> resultado = new ArrayList<>();
        for (Cotizacion c : listarTodas()) {
            if ((c.getNumero() != null && c.getNumero().toLowerCase().contains(t))
                    || (c.getClienteNombre() != null && c.getClienteNombre().toLowerCase().contains(t))
                    || (c.getUsuarioNombre() != null && c.getUsuarioNombre().toLowerCase().contains(t))) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    // ── Creación ────────────────────────────────────────────────────────────

    /**
     * Crea una nueva cotización. Los ítems se reciben como listas paralelas
     * de los parámetros del formulario.
     *
     * @param clienteId       ID del cliente
     * @param productoIds     IDs de productos
     * @param descripciones   Descripciones de personalización por ítem
     * @param cantidades      Cantidades por ítem
     * @param precios         Precios unitarios por ítem
     * @param descuentoPct    Descuento en porcentaje
     * @param ivaPct          IVA en porcentaje
     * @param observaciones   Observaciones generales
     * @param diasVigencia    Días de validez desde hoy
     * @param usuarioId       ID del usuario vendedor
     * @param usuarioNombre   Nombre del usuario vendedor
     */
    public Cotizacion crear(String clienteId,
                             List<String> productoIds,
                             List<String> descripciones,
                             List<Integer> cantidades,
                             List<Double> precios,
                             double descuentoPct,
                             double ivaPct,
                             String observaciones,
                             int diasVigencia,
                             String usuarioId,
                             String usuarioNombre) {

        // Obtener datos del cliente
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + clienteId));

        // Construir ítems
        List<ItemCotizacion> items = buildItems(productoIds, descripciones, cantidades, precios);
        if (items.isEmpty()) throw new IllegalArgumentException("La cotización debe tener al menos un ítem.");

        // Construir cotización
        Cotizacion cot = new Cotizacion();
        cot.setNumero(cotizacionRepository.getNextNumero());
        cot.setClienteId(clienteId);
        cot.setClienteNombre(cliente.getNombreCompleto());
        cot.setClienteDocumento(cliente.getDocumentoCompleto());
        cot.setClienteTelefono(cliente.getTelefono());
        cot.setClienteEmail(cliente.getEmail());
        cot.setClienteDireccion(cliente.getDireccion());
        cot.setUsuarioId(usuarioId);
        cot.setUsuarioNombre(usuarioNombre);
        cot.setItems(items);
        cot.setDescuentoPorcentaje(descuentoPct);
        cot.setIvaPorcentaje(ivaPct);
        cot.setObservaciones(observaciones);
        cot.setEstado(Cotizacion.Estado.PENDIENTE);

        // Calcular totales
        cot.recalcularTotales();

        // Fecha de vencimiento
        Instant vence = Instant.now().plus(diasVigencia, ChronoUnit.DAYS);
        cot.setFechaVencimiento(Timestamp.ofTimeSecondsAndNanos(vence.getEpochSecond(), 0));

        return cotizacionRepository.save(cot);
    }

    // ── Actualización ────────────────────────────────────────────────────────

    public Cotizacion actualizar(String id,
                                  String clienteId,
                                  List<String> productoIds,
                                  List<String> descripciones,
                                  List<Integer> cantidades,
                                  List<Double> precios,
                                  double descuentoPct,
                                  double ivaPct,
                                  String observaciones,
                                  int diasVigencia) {

        Cotizacion cot = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada: " + id));

        if (!cot.isPendiente()) {
            throw new IllegalStateException("Solo se pueden editar cotizaciones en estado PENDIENTE.");
        }

        // Actualizar cliente si cambió
        if (!clienteId.equals(cot.getClienteId())) {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + clienteId));
            cot.setClienteId(clienteId);
            cot.setClienteNombre(cliente.getNombreCompleto());
            cot.setClienteDocumento(cliente.getDocumentoCompleto());
            cot.setClienteTelefono(cliente.getTelefono());
            cot.setClienteEmail(cliente.getEmail());
            cot.setClienteDireccion(cliente.getDireccion());
        }

        List<ItemCotizacion> items = buildItems(productoIds, descripciones, cantidades, precios);
        if (items.isEmpty()) throw new IllegalArgumentException("La cotización debe tener al menos un ítem.");

        cot.setItems(items);
        cot.setDescuentoPorcentaje(descuentoPct);
        cot.setIvaPorcentaje(ivaPct);
        cot.setObservaciones(observaciones);
        cot.recalcularTotales();

        Instant vence = Instant.now().plus(diasVigencia, ChronoUnit.DAYS);
        cot.setFechaVencimiento(Timestamp.ofTimeSecondsAndNanos(vence.getEpochSecond(), 0));

        cotizacionRepository.update(cot);
        return cot;
    }

    // ── Estado ───────────────────────────────────────────────────────────────

    public void cambiarEstado(String id, Cotizacion.Estado nuevoEstado) {
        cotizacionRepository.updateEstado(id, nuevoEstado);
    }

    public void eliminar(String id) {
        cotizacionRepository.delete(id);
    }

    // ── Helper interno ────────────────────────────────────────────────────────

    private List<ItemCotizacion> buildItems(List<String> productoIds,
                                             List<String> descripciones,
                                             List<Integer> cantidades,
                                             List<Double> precios) {
        List<ItemCotizacion> items = new ArrayList<>();
        if (productoIds == null) return items;
        for (int i = 0; i < productoIds.size(); i++) {
            String pid = productoIds.get(i);
            if (pid == null || pid.isBlank()) continue;
            int cantidad = (cantidades != null && cantidades.size() > i && cantidades.get(i) != null)
                    ? cantidades.get(i) : 1;
            double precio = (precios != null && precios.size() > i && precios.get(i) != null)
                    ? precios.get(i) : 0;

            // Obtener nombre del producto
            String nombre = productoRepository.findById(pid)
                    .map(Producto::getNombre)
                    .orElse("Producto");

            ItemCotizacion item = new ItemCotizacion();
            item.setProductoId(pid);
            item.setProductoNombre(nombre);
            item.setDescripcionPersonalizacion(
                    (descripciones != null && descripciones.size() > i) ? descripciones.get(i) : null);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(precio);
            item.calcularSubtotal();
            items.add(item);
        }
        return items;
    }
}
