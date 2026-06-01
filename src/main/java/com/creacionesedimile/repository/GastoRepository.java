package com.creacionesedimile.repository;

import com.creacionesedimile.model.Gasto;
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
public class GastoRepository {

    private static final Logger log         = LoggerFactory.getLogger(GastoRepository.class);
    private static final String COLLECTION  = "gastos";
    private static final String COL_COUNTER = "contadores";
    private static final String DOC_COUNTER = "gastos";

    private final Firestore firestore;

    public GastoRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    // ── Mapeo Firestore → Java ──────────────────────────────────────────────

    private Gasto fromDoc(DocumentSnapshot doc) {
        Gasto g = new Gasto();
        g.setId(doc.getId());
        g.setNumero(doc.getString("numero"));
        g.setNombre(doc.getString("nombre"));
        g.setDescripcion(doc.getString("descripcion"));
        g.setTotal(doc.contains("total") ? doc.getDouble("total") : 0);
        g.setFechaPago(doc.getTimestamp("fechaPago"));
        g.setProveedor(doc.getString("proveedor"));
        g.setComprobante(doc.getString("comprobante"));
        g.setUsuarioId(doc.getString("usuarioId"));
        g.setUsuarioNombre(doc.getString("usuarioNombre"));
        g.setFechaCreacion(doc.getTimestamp("fechaCreacion"));

        String categoria = doc.getString("categoria");
        if (categoria != null) {
            try { g.setCategoria(Gasto.Categoria.valueOf(categoria)); }
            catch (IllegalArgumentException ignored) {}
        }

        String metodoPago = doc.getString("metodoPago");
        if (metodoPago != null) {
            try { g.setMetodoPago(Gasto.MetodoPago.valueOf(metodoPago)); }
            catch (IllegalArgumentException ignored) {}
        }
        return g;
    }

    // ── Mapeo Java → Firestore ──────────────────────────────────────────────

    private Map<String, Object> toMap(Gasto g) {
        Map<String, Object> m = new HashMap<>();
        m.put("numero",        g.getNumero());
        m.put("nombre",        g.getNombre());
        m.put("descripcion",   g.getDescripcion());
        m.put("categoria",     g.getCategoria()  != null ? g.getCategoria().name()  : null);
        m.put("total",         g.getTotal());
        m.put("fechaPago",     g.getFechaPago());
        m.put("metodoPago",    g.getMetodoPago() != null ? g.getMetodoPago().name() : null);
        m.put("proveedor",     g.getProveedor());
        m.put("comprobante",   g.getComprobante());
        m.put("usuarioId",     g.getUsuarioId());
        m.put("usuarioNombre", g.getUsuarioNombre());
        m.put("fechaCreacion", g.getFechaCreacion());
        return m;
    }

    // ── Consultas ───────────────────────────────────────────────────────────

    public List<Gasto> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .orderBy("fechaPago", Query.Direction.DESCENDING)
                    .get();
            List<Gasto> lista = new ArrayList<>();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                lista.add(fromDoc(doc));
            }
            return lista;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al listar gastos", e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    public Optional<Gasto> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (doc.exists()) return Optional.of(fromDoc(doc));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al buscar gasto: {}", id, e);
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    public List<Gasto> findByCategoria(Gasto.Categoria categoria) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .whereEqualTo("categoria", categoria.name())
                    .get();
            List<Gasto> lista = new ArrayList<>();
            for (DocumentSnapshot doc : future.get(15, TimeUnit.SECONDS).getDocuments()) {
                lista.add(fromDoc(doc));
            }
            lista.sort((a, b) -> {
                if (a.getFechaPago() == null) return 1;
                if (b.getFechaPago() == null) return -1;
                return b.getFechaPago().compareTo(a.getFechaPago());
            });
            return lista;
        } catch (TimeoutException e) {
            log.error("Timeout filtrando gastos por categoría: {}", categoria, e);
            return Collections.emptyList();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al filtrar gastos por categoría: {}", categoria, e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    // ── Persistencia ────────────────────────────────────────────────────────

    public Gasto save(Gasto g) {
        g.setFechaCreacion(Timestamp.now());
        g.setNumero(getNextNumero());
        try {
            DocumentReference ref = firestore.collection(COLLECTION).document();
            g.setId(ref.getId());
            ref.set(toMap(g)).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al guardar gasto", e);
            Thread.currentThread().interrupt();
        }
        return g;
    }

    public void update(Gasto g) {
        if (g.getId() == null) return;
        try {
            Map<String, Object> data = toMap(g);
            data.remove("numero");       // no sobrescribir número auto
            data.remove("fechaCreacion");
            firestore.collection(COLLECTION).document(g.getId()).update(data).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al actualizar gasto: {}", g.getId(), e);
            Thread.currentThread().interrupt();
        }
    }

    public void delete(String id) {
        try {
            firestore.collection(COLLECTION).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al eliminar gasto: {}", id, e);
            Thread.currentThread().interrupt();
        }
    }

    // ── Contador auto-incremental ───────────────────────────────────────────

    private String getNextNumero() {
        DocumentReference counterRef = firestore.collection(COL_COUNTER).document(DOC_COUNTER);
        try {
            long[] result = {1};
            firestore.runTransaction(tx -> {
                DocumentSnapshot snap = tx.get(counterRef).get();
                long current = snap.exists() && snap.contains("valor")
                        ? snap.getLong("valor") : 0L;
                result[0] = current + 1;
                tx.set(counterRef, Map.of("valor", result[0]));
                return null;
            }).get();
            return String.format("GAS-%04d", result[0]);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al obtener próximo número de gasto", e);
            Thread.currentThread().interrupt();
            return "GAS-ERR";
        }
    }
}
