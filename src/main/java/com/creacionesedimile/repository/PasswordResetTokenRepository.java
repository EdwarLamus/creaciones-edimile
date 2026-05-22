package com.creacionesedimile.repository;

import com.creacionesedimile.model.PasswordResetToken;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Repositorio para la colección "password_reset_tokens" en Firestore.
 *
 * El ID del documento es el token UUID, lo que permite obtenerlo
 * directamente sin índices adicionales.
 */
@Repository
public class PasswordResetTokenRepository {

    private static final String COLLECTION = "password_reset_tokens";

    private final Firestore firestore;

    public PasswordResetTokenRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    // ----------------------------------------------------------
    // Guardar token
    // ----------------------------------------------------------

    public void save(PasswordResetToken token) {
        Map<String, Object> data = new HashMap<>();
        data.put("email",     token.getEmail());
        data.put("expiresAt", token.getExpiresAt());
        data.put("used",      token.isUsed());
        data.put("createdAt", token.getCreatedAt());

        try {
            firestore.collection(COLLECTION)
                     .document(token.getToken())
                     .set(data)
                     .get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error guardando token de restablecimiento", e);
        }
    }

    // ----------------------------------------------------------
    // Buscar por token (= ID del documento)
    // ----------------------------------------------------------

    public Optional<PasswordResetToken> findByToken(String token) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION)
                                            .document(token)
                                            .get()
                                            .get();
            if (!doc.exists()) return Optional.empty();

            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(doc.getId());
            prt.setEmail(doc.getString("email"));
            prt.setUsed(Boolean.TRUE.equals(doc.getBoolean("used")));

            Date expiresAt = doc.getDate("expiresAt");
            Date createdAt = doc.getDate("createdAt");
            prt.setExpiresAt(expiresAt);
            prt.setCreatedAt(createdAt);

            return Optional.of(prt);

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error buscando token de restablecimiento", e);
        }
    }

    // ----------------------------------------------------------
    // Marcar token como usado (en lugar de borrar, para auditoría)
    // ----------------------------------------------------------

    public void markAsUsed(String token) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("used", true);
            firestore.collection(COLLECTION)
                     .document(token)
                     .update(update)
                     .get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error actualizando token", e);
        }
    }
}
