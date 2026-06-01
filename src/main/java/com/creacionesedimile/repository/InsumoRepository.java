package com.creacionesedimile.repository;

import com.creacionesedimile.model.Insumo;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class InsumoRepository {

    private static final Logger log = LoggerFactory.getLogger(InsumoRepository.class);
    private static final String COLLECTION = "insumos";

    private final Firestore firestore;

    public InsumoRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    // ── Mapeo ───────────────────────────────────────────────────────────────

    private Insumo fromDoc(DocumentSnapshot doc) {
        Insumo i = new Insumo();
        i.setId(doc.getId());
        i.setNombre(doc.getString("nombre"));
        i.setDescripcion(doc.getString("descripcion"));
        i.setUnidadMedida(doc.getString("unidadMedida"));
        i.setStockActual(doc.contains("stockActual") ? doc.getDouble("stockActual") : 0);
        i.setStockMinimo(doc.contains("stockMinimo") ? doc.getDouble("stockMinimo") : 0);
        i.setPrecioUnitario(doc.contains("precioUnitario") ? doc.getDouble("precioUnitario") : 0);
        i.setActivo(Boolean.TRUE.equals(doc.getBoolean("activo")));
        i.setFechaCreacion(doc.getTimestamp("fechaCreacion"));
        return i;
    }

    private Map<String, Object> toMap(Insumo i) {
        Map<String, Object> m = new HashMap<>();
        m.put("nombre", i.getNombre());
        m.put("descripcion", i.getDescripcion());
        m.put("unidadMedida", i.getUnidadMedida());
        m.put("stockActual", i.getStockActual());
        m.put("stockMinimo", i.getStockMinimo());
        m.put("precioUnitario", i.getPrecioUnitario());
        m.put("activo", i.isActivo());
        return m;
    }

    // ── Consultas ───────────────────────────────────────────────────────────

    public List<Insumo> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .orderBy("nombre", Query.Direction.ASCENDING)
                    .get();
            List<Insumo> lista = new ArrayList<>();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                lista.add(fromDoc(doc));
            }
            return lista;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al listar insumos", e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    public Optional<Insumo> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (doc.exists()) return Optional.of(fromDoc(doc));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al buscar insumo por id: {}", id, e);
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    /** Filtrado en memoria por nombre o unidadMedida. */
    public List<Insumo> buscar(String termino) {
        String t = termino.toLowerCase();
        List<Insumo> todos = findAll();
        List<Insumo> resultado = new ArrayList<>();
        for (Insumo i : todos) {
            if ((i.getNombre() != null && i.getNombre().toLowerCase().contains(t))
                    || (i.getUnidadMedida() != null && i.getUnidadMedida().toLowerCase().contains(t))) {
                resultado.add(i);
            }
        }
        return resultado;
    }

    // ── Escritura ───────────────────────────────────────────────────────────

    public Insumo save(Insumo insumo) {
        try {
            Map<String, Object> data = toMap(insumo);
            data.put("fechaCreacion", Timestamp.now());
            DocumentReference ref = firestore.collection(COLLECTION).document();
            ref.set(data).get();
            insumo.setId(ref.getId());
            return insumo;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al guardar insumo", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo guardar el insumo", e);
        }
    }

    public void update(Insumo insumo) {
        try {
            firestore.collection(COLLECTION).document(insumo.getId())
                    .set(toMap(insumo), SetOptions.merge()).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al actualizar insumo: {}", insumo.getId(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo actualizar el insumo", e);
        }
    }

    public void delete(String id) {
        try {
            firestore.collection(COLLECTION).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al eliminar insumo: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo eliminar el insumo", e);
        }
    }

    /** Actualiza solo el campo stockActual (usado al registrar movimientos). */
    public void updateStock(String id, double nuevoStock) {
        try {
            firestore.collection(COLLECTION).document(id)
                    .update("stockActual", nuevoStock).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al actualizar stock del insumo: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo actualizar el stock", e);
        }
    }
}
