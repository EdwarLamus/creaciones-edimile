package com.creacionesedimile.repository;

import com.creacionesedimile.model.MovimientoInsumo;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class MovimientoInsumoRepository {

    private static final Logger log = LoggerFactory.getLogger(MovimientoInsumoRepository.class);
    private static final String COLLECTION = "movimientos_insumo";

    private final Firestore firestore;

    public MovimientoInsumoRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    // ── Mapeo ───────────────────────────────────────────────────────────────

    private MovimientoInsumo fromDoc(DocumentSnapshot doc) {
        MovimientoInsumo m = new MovimientoInsumo();
        m.setId(doc.getId());
        m.setInsumoId(doc.getString("insumoId"));
        m.setInsumoNombre(doc.getString("insumoNombre"));
        String tipo = doc.getString("tipo");
        if (tipo != null) m.setTipo(MovimientoInsumo.Tipo.valueOf(tipo));
        m.setCantidad(doc.contains("cantidad") ? doc.getDouble("cantidad") : 0);
        m.setStockAnterior(doc.contains("stockAnterior") ? doc.getDouble("stockAnterior") : 0);
        m.setStockResultante(doc.contains("stockResultante") ? doc.getDouble("stockResultante") : 0);
        m.setObservacion(doc.getString("observacion"));
        m.setUsuarioId(doc.getString("usuarioId"));
        m.setUsuarioNombre(doc.getString("usuarioNombre"));
        m.setFecha(doc.getTimestamp("fecha"));
        return m;
    }

    // ── Consultas ───────────────────────────────────────────────────────────

    /** Devuelve todos los movimientos de un insumo, ordenados del más reciente al más antiguo. */
    public List<MovimientoInsumo> findByInsumoId(String insumoId) {
        try {
            // Sin orderBy para no requerir índice compuesto en Firestore;
            // el ordenado se hace en memoria.
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .whereEqualTo("insumoId", insumoId)
                    .get();
            List<MovimientoInsumo> lista = new ArrayList<>();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                lista.add(fromDoc(doc));
            }
            // Ordenar del más reciente al más antiguo
            lista.sort((a, b) -> {
                if (a.getFecha() == null && b.getFecha() == null) return 0;
                if (a.getFecha() == null) return 1;
                if (b.getFecha() == null) return -1;
                return b.getFecha().compareTo(a.getFecha());
            });
            return lista;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Hilo interrumpido al listar movimientos del insumo: {}", insumoId, e);
            return Collections.emptyList();
        } catch (ExecutionException e) {
            log.error("Error al listar movimientos del insumo: {}", insumoId, e);
            return Collections.emptyList();
        }
    }

    // ── Escritura ───────────────────────────────────────────────────────────

    public MovimientoInsumo save(MovimientoInsumo movimiento) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("insumoId", movimiento.getInsumoId());
            data.put("insumoNombre", movimiento.getInsumoNombre());
            data.put("tipo", movimiento.getTipo() != null ? movimiento.getTipo().name() : null);
            data.put("cantidad", movimiento.getCantidad());
            data.put("stockAnterior", movimiento.getStockAnterior());
            data.put("stockResultante", movimiento.getStockResultante());
            data.put("observacion", movimiento.getObservacion());
            data.put("usuarioId", movimiento.getUsuarioId());
            data.put("usuarioNombre", movimiento.getUsuarioNombre());
            data.put("fecha", Timestamp.now());

            DocumentReference ref = firestore.collection(COLLECTION).document();
            ref.set(data).get();
            movimiento.setId(ref.getId());
            return movimiento;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al guardar movimiento", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("No se pudo registrar el movimiento", e);
        }
    }
}
