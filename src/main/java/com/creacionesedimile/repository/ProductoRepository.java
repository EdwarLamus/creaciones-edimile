package com.creacionesedimile.repository;

import com.creacionesedimile.model.Producto;
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
 * Repositorio de Productos usando Firebase Firestore.
 *
 * Colección: "productos"
 * Soporta búsqueda por nombre o categoría (RF-03.5).
 */
@Repository
public class ProductoRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductoRepository.class);
    private static final String COLLECTION = "productos";

    private final Firestore firestore;

    public ProductoRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Producto> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .orderBy("nombre", Query.Direction.ASCENDING)
                    .get();
            List<Producto> lista = new ArrayList<>();
            for (QueryDocumentSnapshot doc : future.get(15, TimeUnit.SECONDS).getDocuments()) {
                lista.add(docToProducto(doc));
            }
            return lista;
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout listando productos en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error listando productos desde Firestore", e);
        }
    }

    public Optional<Producto> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION)
                    .document(id).get().get(15, TimeUnit.SECONDS);
            if (!doc.exists()) return Optional.empty();
            return Optional.of(docToProducto(doc));
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout consultando producto por ID en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error consultando producto por ID en Firestore", e);
        }
    }

    public List<Producto> findByCategoriaId(String categoriaId) {
        try {
            // Se omite orderBy para no requerir índice compuesto en Firestore;
            // se ordena en memoria.
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .whereEqualTo("categoriaId", categoriaId)
                    .get();
            List<Producto> lista = new ArrayList<>();
            for (QueryDocumentSnapshot doc : future.get(15, TimeUnit.SECONDS).getDocuments()) {
                lista.add(docToProducto(doc));
            }
            lista.sort(Comparator.comparing(p -> p.getNombre() != null ? p.getNombre() : ""));
            return lista;
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout filtrando productos por categoría en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error filtrando productos por categoría en Firestore", e);
        }
    }

    /**
     * Búsqueda en memoria por nombre del producto o nombre de la categoría (RF-03.5).
     */
    public List<Producto> buscar(String termino) {
        String t = termino.trim().toLowerCase();
        List<Producto> todos = findAll();
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : todos) {
            boolean coincide =
                    (p.getNombre()          != null && p.getNombre().toLowerCase().contains(t))
                 || (p.getCategoriaNombre() != null && p.getCategoriaNombre().toLowerCase().contains(t));
            if (coincide) resultado.add(p);
        }
        return resultado;
    }

    public String save(Producto producto) {
        try {
            Map<String, Object> data = productoToDoc(producto);
            if (producto.getId() != null && !producto.getId().isBlank()) {
                firestore.collection(COLLECTION).document(producto.getId()).set(data).get(15, TimeUnit.SECONDS);
                return producto.getId();
            } else {
                DocumentReference ref = firestore.collection(COLLECTION).add(data).get(15, TimeUnit.SECONDS);
                log.debug("Producto guardado con ID: {}", ref.getId());
                return ref.getId();
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout guardando producto en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error guardando producto en Firestore", e);
        }
    }

    public void update(String id, Map<String, Object> fields) {
        try {
            firestore.collection(COLLECTION).document(id).update(fields).get(15, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout actualizando producto en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error actualizando producto en Firestore", e);
        }
    }

    public void delete(String id) {
        try {
            firestore.collection(COLLECTION).document(id).delete().get(15, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout eliminando producto en Firestore", e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error eliminando producto en Firestore", e);
        }
    }

    // -------------------------------------------------------
    // Mapeo
    // -------------------------------------------------------

    private Producto docToProducto(DocumentSnapshot doc) {
        Producto p = new Producto();
        p.setId(doc.getId());
        p.setNombre(doc.getString("nombre"));
        p.setDescripcion(doc.getString("descripcion"));
        p.setCategoriaId(doc.getString("categoriaId"));
        p.setCategoriaNombre(doc.getString("categoriaNombre"));
        Double precio = doc.getDouble("precioBase");
        p.setPrecioBase(precio != null ? precio : 0.0);
        p.setImagenUrl(doc.getString("imagenUrl"));
        Boolean activo = doc.getBoolean("activo");
        p.setActivo(activo != null ? activo : true);
        p.setFechaCreacion(doc.getDate("fechaCreacion"));
        return p;
    }

    private Map<String, Object> productoToDoc(Producto p) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre",          p.getNombre());
        data.put("descripcion",     p.getDescripcion());
        data.put("categoriaId",     p.getCategoriaId());
        data.put("categoriaNombre", p.getCategoriaNombre());
        data.put("precioBase",      p.getPrecioBase());
        data.put("imagenUrl",       p.getImagenUrl());
        data.put("activo",          p.isActivo());
        data.put("fechaCreacion",   p.getFechaCreacion() != null
                                    ? p.getFechaCreacion() : new Date());
        return data;
    }
}
