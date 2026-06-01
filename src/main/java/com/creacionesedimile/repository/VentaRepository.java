package com.creacionesedimile.repository;

import com.creacionesedimile.model.ItemVenta;
import com.creacionesedimile.model.Venta;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Repository
public class VentaRepository {

    private static final Logger log          = LoggerFactory.getLogger(VentaRepository.class);
    private static final String COLLECTION   = "ventas";
    private static final String COL_COUNTER  = "contadores";
    private static final String DOC_COUNTER  = "ventas";

    private final Firestore firestore;

    public VentaRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    // ── Mapeo Firestore → Java ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Venta fromDoc(DocumentSnapshot doc) {
        Venta v = new Venta();
        v.setId(doc.getId());
        v.setNumero(doc.getString("numero"));
        v.setCotizacionId(doc.getString("cotizacionId"));
        v.setCotizacionNumero(doc.getString("cotizacionNumero"));
        v.setClienteId(doc.getString("clienteId"));
        v.setClienteNombre(doc.getString("clienteNombre"));
        v.setClienteDocumento(doc.getString("clienteDocumento"));
        v.setClienteTelefono(doc.getString("clienteTelefono"));
        v.setClienteEmail(doc.getString("clienteEmail"));
        v.setClienteDireccion(doc.getString("clienteDireccion"));
        v.setUsuarioId(doc.getString("usuarioId"));
        v.setUsuarioNombre(doc.getString("usuarioNombre"));
        v.setSubtotal(doc.contains("subtotal") ? doc.getDouble("subtotal") : 0);
        v.setDescuentoPorcentaje(doc.contains("descuentoPorcentaje") ? doc.getDouble("descuentoPorcentaje") : 0);
        v.setDescuentoValor(doc.contains("descuentoValor") ? doc.getDouble("descuentoValor") : 0);
        v.setIvaPorcentaje(doc.contains("ivaPorcentaje") ? doc.getDouble("ivaPorcentaje") : 0);
        v.setIvaValor(doc.contains("ivaValor") ? doc.getDouble("ivaValor") : 0);
        v.setTotal(doc.contains("total") ? doc.getDouble("total") : 0);
        v.setObservaciones(doc.getString("observaciones"));
        v.setMotivoAnulacion(doc.getString("motivoAnulacion"));
        v.setFechaCreacion(doc.getTimestamp("fechaCreacion"));

        String estado = doc.getString("estado");
        if (estado != null) {
            try { v.setEstado(Venta.Estado.valueOf(estado)); }
            catch (IllegalArgumentException ignored) {}
        }

        String metodoPago = doc.getString("metodoPago");
        if (metodoPago != null) {
            try { v.setMetodoPago(Venta.MetodoPago.valueOf(metodoPago)); }
            catch (IllegalArgumentException ignored) {}
        }

        // Ítems (array de mapas)
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) doc.get("items");
        if (rawItems != null) {
            List<ItemVenta> items = new ArrayList<>();
            for (Map<String, Object> m : rawItems) {
                ItemVenta item = new ItemVenta();
                item.setProductoId((String) m.get("productoId"));
                item.setProductoNombre((String) m.get("productoNombre"));
                item.setDescripcionPersonalizacion((String) m.get("descripcionPersonalizacion"));
                Object cant = m.get("cantidad");
                item.setCantidad(cant instanceof Number ? ((Number) cant).intValue() : 0);
                Object pu = m.get("precioUnitario");
                item.setPrecioUnitario(pu instanceof Number ? ((Number) pu).doubleValue() : 0);
                Object sub = m.get("subtotal");
                item.setSubtotal(sub instanceof Number ? ((Number) sub).doubleValue() : 0);
                items.add(item);
            }
            v.setItems(items);
        }
        return v;
    }

    // ── Mapeo Java → Firestore ──────────────────────────────────────────────

    private Map<String, Object> toMap(Venta v) {
        Map<String, Object> m = new HashMap<>();
        m.put("numero", v.getNumero());
        m.put("cotizacionId", v.getCotizacionId());
        m.put("cotizacionNumero", v.getCotizacionNumero());
        m.put("clienteId", v.getClienteId());
        m.put("clienteNombre", v.getClienteNombre());
        m.put("clienteDocumento", v.getClienteDocumento());
        m.put("clienteTelefono", v.getClienteTelefono());
        m.put("clienteEmail", v.getClienteEmail());
        m.put("clienteDireccion", v.getClienteDireccion());
        m.put("usuarioId", v.getUsuarioId());
        m.put("usuarioNombre", v.getUsuarioNombre());
        m.put("subtotal", v.getSubtotal());
        m.put("descuentoPorcentaje", v.getDescuentoPorcentaje());
        m.put("descuentoValor", v.getDescuentoValor());
        m.put("ivaPorcentaje", v.getIvaPorcentaje());
        m.put("ivaValor", v.getIvaValor());
        m.put("total", v.getTotal());
        m.put("estado", v.getEstado() != null ? v.getEstado().name() : null);
        m.put("metodoPago", v.getMetodoPago() != null ? v.getMetodoPago().name() : null);
        m.put("observaciones", v.getObservaciones());
        m.put("motivoAnulacion", v.getMotivoAnulacion());

        List<Map<String, Object>> items = new ArrayList<>();
        for (ItemVenta item : v.getItems()) {
            Map<String, Object> im = new HashMap<>();
            im.put("productoId", item.getProductoId());
            im.put("productoNombre", item.getProductoNombre());
            im.put("descripcionPersonalizacion", item.getDescripcionPersonalizacion());
            im.put("cantidad", item.getCantidad());
            im.put("precioUnitario", item.getPrecioUnitario());
            im.put("subtotal", item.getSubtotal());
            items.add(im);
        }
        m.put("items", items);
        return m;
    }

    // ── Consultas ───────────────────────────────────────────────────────────

    public List<Venta> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                    .get();
            List<Venta> lista = new ArrayList<>();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                lista.add(fromDoc(doc));
            }
            return lista;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al listar ventas", e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    public Optional<Venta> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (doc.exists()) return Optional.of(fromDoc(doc));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al buscar venta: {}", id, e);
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    public List<Venta> findByEstado(Venta.Estado estado) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .whereEqualTo("estado", estado.name())
                    .get();
            List<Venta> lista = new ArrayList<>();
            for (DocumentSnapshot doc : future.get(15, TimeUnit.SECONDS).getDocuments()) {
                lista.add(fromDoc(doc));
            }
            lista.sort((a, b) -> {
                if (a.getFechaCreacion() == null) return 1;
                if (b.getFechaCreacion() == null) return -1;
                return b.getFechaCreacion().compareTo(a.getFechaCreacion());
            });
            return lista;
        } catch (TimeoutException e) {
            log.error("Timeout filtrando ventas por estado: {}", estado, e);
            return Collections.emptyList();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al filtrar ventas por estado: {}", estado, e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    public List<Venta> findByClienteId(String clienteId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .whereEqualTo("clienteId", clienteId)
                    .get();
            List<Venta> lista = new ArrayList<>();
            for (DocumentSnapshot doc : future.get(15, TimeUnit.SECONDS).getDocuments()) {
                lista.add(fromDoc(doc));
            }
            lista.sort((a, b) -> {
                if (a.getFechaCreacion() == null) return 1;
                if (b.getFechaCreacion() == null) return -1;
                return b.getFechaCreacion().compareTo(a.getFechaCreacion());
            });
            return lista;
        } catch (TimeoutException e) {
            log.error("Timeout filtrando ventas por cliente: {}", clienteId, e);
            return Collections.emptyList();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al filtrar ventas por cliente: {}", clienteId, e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    // ── Escritura ───────────────────────────────────────────────────────────

    public Venta save(Venta venta) {
        try {
            Map<String, Object> data = toMap(venta);
            data.put("fechaCreacion", Timestamp.now());
            DocumentReference ref = firestore.collection(COLLECTION).document();
            ref.set(data).get();
            venta.setId(ref.getId());
            return venta;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al guardar venta", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo guardar la venta", e);
        }
    }

    public void update(Venta venta) {
        try {
            firestore.collection(COLLECTION).document(venta.getId())
                    .set(toMap(venta), SetOptions.merge()).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al actualizar venta: {}", venta.getId(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo actualizar la venta", e);
        }
    }

    public void updateEstado(String id, Venta.Estado estado) {
        try {
            firestore.collection(COLLECTION).document(id)
                    .update("estado", estado.name()).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al actualizar estado de venta: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo actualizar el estado de la venta", e);
        }
    }

    public void updateEstadoYMotivo(String id, Venta.Estado estado, String motivo) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("estado", estado.name());
            updates.put("motivoAnulacion", motivo);
            firestore.collection(COLLECTION).document(id).update(updates).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al anular venta: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo anular la venta", e);
        }
    }

    public void delete(String id) {
        try {
            firestore.collection(COLLECTION).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al eliminar venta: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo eliminar la venta", e);
        }
    }

    // ── Numeración automática FAC-0001 ──────────────────────────────────────

    public String getNextNumero() {
        try {
            long[] resultHolder = {0};
            firestore.runTransaction(tx -> {
                DocumentReference counterRef = firestore
                        .collection(COL_COUNTER).document(DOC_COUNTER);
                DocumentSnapshot snap = tx.get(counterRef).get();
                long current = snap.exists() && snap.contains("valor")
                        ? snap.getLong("valor") : 0L;
                long next = current + 1;
                tx.set(counterRef, Map.of("valor", next), SetOptions.merge());
                resultHolder[0] = next;
                return null;
            }).get();
            return String.format("FAC-%04d", resultHolder[0]);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al generar número de factura", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo generar el número de factura", e);
        }
    }
}
