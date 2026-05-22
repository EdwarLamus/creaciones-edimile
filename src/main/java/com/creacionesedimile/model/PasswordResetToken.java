package com.creacionesedimile.model;

import java.util.Date;

/**
 * Representa un token de restablecimiento de contraseña.
 * Se persiste en la colección "password_reset_tokens" de Firestore.
 *
 * El ID del documento ES el propio token UUID, para permitir
 * búsquedas directas por ID sin necesidad de índices adicionales.
 */
public class PasswordResetToken {

    /** UUID que actúa como token y como ID del documento Firestore */
    private String token;

    /** Email del usuario que solicitó el restablecimiento */
    private String email;

    /** Fecha/hora de vencimiento (1 hora tras la creación) */
    private Date expiresAt;

    /** true si el token ya fue utilizado */
    private boolean used;

    /** Fecha de creación del token */
    private Date createdAt;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, String email, Date expiresAt) {
        this.token     = token;
        this.email     = email;
        this.expiresAt = expiresAt;
        this.used      = false;
        this.createdAt = new Date();
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    /** Retorna true si el token ya venció o fue marcado como usado */
    public boolean isExpiredOrUsed() {
        return used || (expiresAt != null && expiresAt.before(new Date()));
    }

    // -------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------

    public String getToken()              { return token; }
    public void setToken(String token)    { this.token = token; }

    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }

    public Date getExpiresAt()            { return expiresAt; }
    public void setExpiresAt(Date d)      { this.expiresAt = d; }

    public boolean isUsed()               { return used; }
    public void setUsed(boolean used)     { this.used = used; }

    public Date getCreatedAt()            { return createdAt; }
    public void setCreatedAt(Date d)      { this.createdAt = d; }
}
