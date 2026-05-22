package com.creacionesedimile.repository;

import com.creacionesedimile.model.Usuario;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Repositorio de Usuarios usando Firebase Firestore (NoSQL).
 *
 * Reemplaza completamente a Spring Data JPA: en lugar de una tabla SQL,
 * los usuarios se guardan en la colección "usuarios" de Firestore.
 *
 * Las operaciones de Firestore son asíncronas (devuelven ApiFuture<>).
 * Usamos .get() para bloquear hasta obtener el resultado, ya que
 * Spring Security espera respuestas síncronas.
 */
@Repository
public class UsuarioRepository {

    private static final Logger log = LoggerFactory.getLogger(UsuarioRepository.class);
    private static final String COLLECTION = "usuarios";

    private final Firestore firestore;

    public UsuarioRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    // -------------------------------------------------------
    // Consultas
    // -------------------------------------------------------

    public Optional<Usuario> findByEmail(String email) {
        try {
            CollectionReference col = firestore.collection(COLLECTION);
            ApiFuture<QuerySnapshot> future = col
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            if (docs.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(docToUsuario(docs.get(0)));

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error consultando usuario por email en Firestore", e);
        }
    }

    public Optional<Usuario> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION)
                    .document(id).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(docToUsuario(doc));

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error consultando usuario por ID en Firestore", e);
        }
    }

    public List<Usuario> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .orderBy("nombre", Query.Direction.ASCENDING)
                    .get();

            List<Usuario> lista = new ArrayList<>();
            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                lista.add(docToUsuario(doc));
            }
            return lista;

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error listando usuarios desde Firestore", e);
        }
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    // -------------------------------------------------------
    // Persistencia
    // -------------------------------------------------------

    /**
     * Crea o actualiza un usuario.
     * Si el usuario ya tiene ID, actualiza el documento existente.
     * Si no tiene ID, crea un documento nuevo (Firestore asigna el ID).
     *
     * @return ID del documento en Firestore
     */
    public String save(Usuario usuario) {
        try {
            Map<String, Object> data = usuarioToDoc(usuario);

            if (usuario.getId() != null && !usuario.getId().isBlank()) {
                firestore.collection(COLLECTION)
                        .document(usuario.getId())
                        .set(data)
                        .get();
                return usuario.getId();
            } else {
                DocumentReference ref = firestore.collection(COLLECTION)
                        .add(data)
                        .get();
                log.debug("Usuario guardado con ID: {}", ref.getId());
                return ref.getId();
            }

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error guardando usuario en Firestore", e);
        }
    }

    /**
     * Actualiza campos específicos de un usuario sin sobrescribir el documento completo.
     */
    public void update(String id, Map<String, Object> fields) {
        try {
            firestore.collection(COLLECTION).document(id).update(fields).get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error actualizando usuario en Firestore", e);
        }
    }

    // -------------------------------------------------------
    // Helpers de mapeo
    // -------------------------------------------------------

    private Usuario docToUsuario(DocumentSnapshot doc) {
        Usuario u = new Usuario();
        u.setId(doc.getId());
        u.setNombre(doc.getString("nombre"));
        u.setApellido(doc.getString("apellido"));
        u.setEmail(doc.getString("email"));
        u.setPassword(doc.getString("password"));
        u.setRol(doc.getString("rol"));
        Boolean activo = doc.getBoolean("activo");
        u.setActivo(activo != null && activo);
        if (doc.getDate("fechaCreacion") != null) {
            u.setFechaCreacion(doc.getDate("fechaCreacion"));
        }
        return u;
    }

    private Map<String, Object> usuarioToDoc(Usuario u) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre",        u.getNombre());
        data.put("apellido",      u.getApellido());
        data.put("email",         u.getEmail());
        data.put("password",      u.getPassword());
        data.put("rol",           u.getRol());
        data.put("activo",        u.isActivo());
        data.put("fechaCreacion", u.getFechaCreacion() != null ? u.getFechaCreacion() : new Date());
        return data;
    }
}
