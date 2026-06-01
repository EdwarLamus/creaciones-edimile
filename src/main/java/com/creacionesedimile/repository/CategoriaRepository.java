package com.creacionesedimile.repository;

import com.creacionesedimile.model.Categoria;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Repositorio de Categorías usando Firebase Firestore.
 *
 * Colección: "categorias"
 */
@Repository
public class CategoriaRepository {

    private static final Logger log = LoggerFactory.getLogger(CategoriaRepository.class);
    private static final String COLLECTION = "categorias";

    private final Firestore firestore;

    public CategoriaRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Categoria> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .orderBy("nombre", Query.Direction.ASCENDING)
                    .get();
            List<Categoria> lista = new ArrayList<>();
            for (QueryDocumentSnapshot doc : future.get(15, TimeUnit.SECONDS).getDocuments()) {
                lista.add(docToCategoria(doc));
            }
            return lista;
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout consultando categorías en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error listando categorías desde Firestore", e);
        }
    }

    public List<Categoria> findAllActivas() {
        try {
            // Se omite orderBy para no requerir índice compuesto en Firestore;
            // se ordena en memoria.
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .whereEqualTo("activo", true)
                    .get();
            List<Categoria> lista = new ArrayList<>();
            for (QueryDocumentSnapshot doc : future.get(15, TimeUnit.SECONDS).getDocuments()) {
                lista.add(docToCategoria(doc));
            }
            lista.sort(Comparator.comparing(c -> c.getNombre() != null ? c.getNombre() : ""));
            return lista;
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout consultando categorías activas en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error listando categorías activas desde Firestore", e);
        }
    }

    public Optional<Categoria> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION)
                    .document(id).get().get(15, TimeUnit.SECONDS);
            if (!doc.exists()) return Optional.empty();
            return Optional.of(docToCategoria(doc));
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout consultando categoría por ID en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error consultando categoría por ID en Firestore", e);
        }
    }

    public boolean existsByNombre(String nombre) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .whereEqualTo("nombre", nombre)
                    .limit(1).get();
            return !future.get(15, TimeUnit.SECONDS).getDocuments().isEmpty();
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout verificando nombre de categoría en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error verificando nombre de categoría en Firestore", e);
        }
    }

    public String save(Categoria categoria) {
        try {
            Map<String, Object> data = categoriaToDoc(categoria);
            if (categoria.getId() != null && !categoria.getId().isBlank()) {
                firestore.collection(COLLECTION).document(categoria.getId()).set(data).get(15, TimeUnit.SECONDS);
                return categoria.getId();
            } else {
                DocumentReference ref = firestore.collection(COLLECTION).add(data).get(15, TimeUnit.SECONDS);
                log.debug("Categoría guardada con ID: {}", ref.getId());
                return ref.getId();
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout guardando categoría en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error guardando categoría en Firestore", e);
        }
    }

    public void update(String id, Map<String, Object> fields) {
        try {
            firestore.collection(COLLECTION).document(id).update(fields).get(15, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout actualizando categoría en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error actualizando categoría en Firestore", e);
        }
    }

    public void delete(String id) {
        try {
            firestore.collection(COLLECTION).document(id).delete().get(15, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout eliminando categoría en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error eliminando categoría en Firestore", e);
        }
    }

    // -------------------------------------------------------
    // Mapeo
    // -------------------------------------------------------

    private Categoria docToCategoria(DocumentSnapshot doc) {
        Categoria c = new Categoria();
        c.setId(doc.getId());
        c.setNombre(doc.getString("nombre"));
        c.setDescripcion(doc.getString("descripcion"));
        Boolean activo = doc.getBoolean("activo");
        c.setActivo(activo != null ? activo : true);
        c.setFechaCreacion(doc.getDate("fechaCreacion"));
        return c;
    }

    private Map<String, Object> categoriaToDoc(Categoria c) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre",        c.getNombre());
        data.put("descripcion",   c.getDescripcion());
        data.put("activo",        c.isActivo());
        data.put("fechaCreacion", c.getFechaCreacion() != null
                                  ? c.getFechaCreacion() : new Date());
        return data;
    }
}
