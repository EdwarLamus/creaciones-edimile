package com.creacionesedimile.repository;

import com.creacionesedimile.model.Cotizacion;
import com.creacionesedimile.model.ItemCotizacion;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class CotizacionRepository {

    private static final Logger log = LoggerFactory.getLogger(CotizacionRepository.class);
    private static final String COLLECTION  = "cotizaciones";
    private static final String COL_COUNTER = "contadores";
    private static final String DOC_COUNTER = "cotizaciones";

    private final Firestore firestore;

    public CotizacionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    // ── Mapeo ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Cotizacion fromDoc(DocumentSnapshot doc) {
        Cotizacion c = new Cotizacion();
        c.setId(doc.getId());
        c.setNumero(doc.getString("numero"));
        c.setClienteId(doc.getString("clienteId"));
        c.setClienteNombre(doc.getString("clienteNombre"));
        c.setClienteDocumento(doc.getString("clienteDocumento"));
        c.setClienteTelefono(doc.getString("clienteTelefono"));
        c.setClienteEmail(doc.getString("clienteEmail"));
        c.setClienteDireccion(doc.getString("clienteDireccion"));
        c.setUsuarioId(doc.getString("usuarioId"));
        c.setUsuarioNombre(doc.getString("usuarioNombre"));
        c.setSubtotal(doc.contains("subtotal") ? doc.getDouble("subtotal") : 0);
        c.setDescuentoPorcentaje(doc.contains("descuentoPorcentaje") ? doc.getDouble("descuentoPorcentaje") : 0);
        c.setDescuentoValor(doc.contains("descuentoValor") ? doc.getDouble("descuentoValor") : 0);
        c.setIvaPorcentaje(doc.contains("ivaPorcentaje") ? doc.getDouble("ivaPorcentaje") : 0);
        c.setIvaValor(doc.contains("ivaValor") ? doc.getDouble("ivaValor") : 0);
        c.setTotal(doc.contains("total") ? doc.getDouble("total") : 0);
        c.setObservaciones(doc.getString("observaciones"));
        c.setFechaCreacion(doc.getTimestamp("fechaCreacion"));
        c.setFechaVencimiento(doc.getTimestamp("fechaVencimiento"));

        String estado = doc.getString("estado");
        if (estado != null) {
            try { c.setEstado(Cotizacion.Estado.valueOf(estado)); }
            catch (IllegalArgumentException ignored) {}
        }

        // Ítems (array de mapas)
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) doc.get("items");
        if (rawItems != null) {
            List<ItemCotizacion> items = new ArrayList<>();
            for (Map<String, Object> m : rawItems) {
                ItemCotizacion item = new ItemCotizacion();
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
            c.setItems(items);
        }
        return c;
    }

    private Map<String, Object> toMap(Cotizacion c) {
        Map<String, Object> m = new HashMap<>();
        m.put("numero", c.getNumero());
        m.put("clienteId", c.getClienteId());
        m.put("clienteNombre", c.getClienteNombre());
        m.put("clienteDocumento", c.getClienteDocumento());
        m.put("clienteTelefono", c.getClienteTelefono());
        m.put("clienteEmail", c.getClienteEmail());
        m.put("clienteDireccion", c.getClienteDireccion());
        m.put("usuarioId", c.getUsuarioId());
        m.put("usuarioNombre", c.getUsuarioNombre());
        m.put("subtotal", c.getSubtotal());
        m.put("descuentoPorcentaje", c.getDescuentoPorcentaje());
        m.put("descuentoValor", c.getDescuentoValor());
        m.put("ivaPorcentaje", c.getIvaPorcentaje());
        m.put("ivaValor", c.getIvaValor());
        m.put("total", c.getTotal());
        m.put("estado", c.getEstado() != null ? c.getEstado().name() : null);
        m.put("observaciones", c.getObservaciones());
        m.put("fechaVencimiento", c.getFechaVencimiento());

        // Serializar ítems
        List<Map<String, Object>> items = new ArrayList<>();
        for (ItemCotizacion item : c.getItems()) {
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

    public List<Cotizacion> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                    .get();
            List<Cotizacion> lista = new ArrayList<>();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                lista.add(fromDoc(doc));
            }
            return lista;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al listar cotizaciones", e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    public Optional<Cotizacion> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (doc.exists()) return Optional.of(fromDoc(doc));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al buscar cotización: {}", id, e);
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    public List<Cotizacion> findByEstado(Cotizacion.Estado estado) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .whereEqualTo("estado", estado.name())
                    .get();
            List<Cotizacion> lista = new ArrayList<>();
            for (DocumentSnapshot doc : future.get(15, java.util.concurrent.TimeUnit.SECONDS).getDocuments()) {
                lista.add(fromDoc(doc));
            }
            lista.sort((a, b) -> {
                if (a.getFechaCreacion() == null) return 1;
                if (b.getFechaCreacion() == null) return -1;
                return b.getFechaCreacion().compareTo(a.getFechaCreacion());
            });
            return lista;
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout filtrando cotizaciones por estado: {}", estado, e);
            return Collections.emptyList();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al filtrar cotizaciones por estado: {}", estado, e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    public List<Cotizacion> findByClienteId(String clienteId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .whereEqualTo("clienteId", clienteId)
                    .get();
            List<Cotizacion> lista = new ArrayList<>();
            for (DocumentSnapshot doc : future.get(15, java.util.concurrent.TimeUnit.SECONDS).getDocuments()) {
                lista.add(fromDoc(doc));
            }
            lista.sort((a, b) -> {
                if (a.getFechaCreacion() == null) return 1;
                if (b.getFechaCreacion() == null) return -1;
                return b.getFechaCreacion().compareTo(a.getFechaCreacion());
            });
            return lista;
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout filtrando cotizaciones por cliente: {}", clienteId, e);
            return Collections.emptyList();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al filtrar cotizaciones por cliente: {}", clienteId, e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    // ── Escritura ───────────────────────────────────────────────────────────

    public Cotizacion save(Cotizacion cotizacion) {
        try {
            Map<String, Object> data = toMap(cotizacion);
            data.put("fechaCreacion", Timestamp.now());
            DocumentReference ref = firestore.collection(COLLECTION).document();
            ref.set(data).get();
            cotizacion.setId(ref.getId());
            return cotizacion;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al guardar cotización", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo guardar la cotización", e);
        }
    }

    public void delete(String id) {
        try {
            firestore.collection(COLLECTION).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al eliminar cotización: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo eliminar la cotización", e);
        }
    }

    public void update(Cotizacion cotizacion) {
        try {
            firestore.collection(COLLECTION).document(cotizacion.getId())
                    .set(toMap(cotizacion), SetOptions.merge()).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al actualizar cotización: {}", cotizacion.getId(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo actualizar la cotización", e);
        }
    }

    public void updateEstado(String id, Cotizacion.Estado estado) {
        try {
            firestore.collection(COLLECTION).document(id)
                    .update("estado", estado.name()).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al actualizar estado de cotización: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo actualizar el estado", e);
        }
    }

    // ── Numeración automática COT-0001 ──────────────────────────────────────

    /**
     * Genera el siguiente número de cotización de forma atómica usando Firestore
     * transactions. Devuelve una cadena con formato COT-XXXX.
     */
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
            return String.format("COT-%04d", resultHolder[0]);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al generar número de cotización", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo generar el número de cotización", e);
        }
    }
}
