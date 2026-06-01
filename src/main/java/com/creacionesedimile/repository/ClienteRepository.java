package com.creacionesedimile.repository;

import com.creacionesedimile.model.Cliente;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Repositorio de Clientes usando Firebase Firestore (NoSQL).
 *
 * Los clientes se almacenan en la colección "clientes".
 * Soporta búsqueda por nombre, número de documento o empresa (RF-02.4).
 */
@Repository
public class ClienteRepository {

    private static final Logger log = LoggerFactory.getLogger(ClienteRepository.class);
    private static final String COLLECTION = "clientes";

    private final Firestore firestore;

    public ClienteRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    // -------------------------------------------------------
    // Consultas
    // -------------------------------------------------------

    public List<Cliente> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .orderBy("nombre", Query.Direction.ASCENDING)
                    .get();

            List<Cliente> lista = new ArrayList<>();
            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                lista.add(docToCliente(doc));
            }
            return lista;

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error listando clientes desde Firestore", e);
        }
    }

    public Optional<Cliente> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION)
                    .document(id).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(docToCliente(doc));

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error consultando cliente por ID en Firestore", e);
        }
    }

    public Optional<Cliente> findByNumeroDocumento(String numeroDocumento) {
        try {
            CollectionReference col = firestore.collection(COLLECTION);
            ApiFuture<QuerySnapshot> future = col
                    .whereEqualTo("numeroDocumento", numeroDocumento)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            if (docs.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(docToCliente(docs.get(0)));

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error consultando cliente por documento en Firestore", e);
        }
    }

    public boolean existsByNumeroDocumento(String numeroDocumento) {
        return findByNumeroDocumento(numeroDocumento).isPresent();
    }

    /**
     * Búsqueda por término: filtra en memoria sobre todos los clientes activos
     * por nombre completo, número de documento o empresa (RF-02.4).
     */
    public List<Cliente> buscar(String termino) {
        String t = termino.trim().toLowerCase();
        List<Cliente> todos = findAll();
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : todos) {
            boolean coincide =
                    (c.getNombre()          != null && c.getNombre().toLowerCase().contains(t))
                 || (c.getApellido()        != null && c.getApellido().toLowerCase().contains(t))
                 || (c.getNumeroDocumento() != null && c.getNumeroDocumento().toLowerCase().contains(t))
                 || (c.getEmpresa()         != null && c.getEmpresa().toLowerCase().contains(t));
            if (coincide) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    // -------------------------------------------------------
    // Persistencia
    // -------------------------------------------------------

    public String save(Cliente cliente) {
        try {
            Map<String, Object> data = clienteToDoc(cliente);

            if (cliente.getId() != null && !cliente.getId().isBlank()) {
                firestore.collection(COLLECTION)
                        .document(cliente.getId())
                        .set(data)
                        .get();
                return cliente.getId();
            } else {
                DocumentReference ref = firestore.collection(COLLECTION)
                        .add(data)
                        .get();
                log.debug("Cliente guardado con ID: {}", ref.getId());
                return ref.getId();
            }

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error guardando cliente en Firestore", e);
        }
    }

    public void update(String id, Map<String, Object> fields) {
        try {
            firestore.collection(COLLECTION).document(id).update(fields).get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error actualizando cliente en Firestore", e);
        }
    }

    public void delete(String id) {
        try {
            firestore.collection(COLLECTION).document(id).delete().get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error eliminando cliente en Firestore", e);
        }
    }

    // -------------------------------------------------------
    // Helpers de mapeo
    // -------------------------------------------------------

    private Cliente docToCliente(DocumentSnapshot doc) {
        Cliente c = new Cliente();
        c.setId(doc.getId());
        c.setNombre(doc.getString("nombre"));
        c.setApellido(doc.getString("apellido"));
        c.setEmpresa(doc.getString("empresa"));
        c.setTipoDocumento(doc.getString("tipoDocumento"));
        c.setNumeroDocumento(doc.getString("numeroDocumento"));
        c.setTelefono(doc.getString("telefono"));
        c.setEmail(doc.getString("email"));
        c.setDireccion(doc.getString("direccion"));
        Boolean activo = doc.getBoolean("activo");
        c.setActivo(activo != null ? activo : true);
        c.setFechaCreacion(doc.getDate("fechaCreacion"));
        return c;
    }

    private Map<String, Object> clienteToDoc(Cliente c) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre",          c.getNombre());
        data.put("apellido",        c.getApellido());
        data.put("empresa",         c.getEmpresa());
        data.put("tipoDocumento",   c.getTipoDocumento());
        data.put("numeroDocumento", c.getNumeroDocumento());
        data.put("telefono",        c.getTelefono());
        data.put("email",           c.getEmail());
        data.put("direccion",       c.getDireccion());
        data.put("activo",          c.isActivo());
        data.put("fechaCreacion",   c.getFechaCreacion() != null
                                    ? c.getFechaCreacion()
                                    : new java.util.Date());
        return data;
    }
}
