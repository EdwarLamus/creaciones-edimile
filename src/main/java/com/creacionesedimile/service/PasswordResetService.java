package com.creacionesedimile.service;

import com.creacionesedimile.model.PasswordResetToken;
import com.creacionesedimile.model.Usuario;
import com.creacionesedimile.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para el flujo completo de restablecimiento de contraseña.
 *
 * Flujo:
 *  1. El usuario solicita reset → generateResetToken(email) crea y persiste un token UUID
 *  2. El usuario visita /reset-password?token=UUID → validateToken() verifica vigencia
 *  3. El usuario establece nueva contraseña → resetPassword() actualiza y marca el token como usado
 *
 * Los tokens expiran en 1 hora y son de un solo uso.
 */
@Service
public class PasswordResetService {

    private static final int EXPIRY_MINUTES = 60;

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioService usuarioService;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UsuarioService usuarioService) {
        this.tokenRepository = tokenRepository;
        this.usuarioService  = usuarioService;
    }

    // ----------------------------------------------------------
    // Generar token
    // ----------------------------------------------------------

    /**
     * Genera un token UUID, lo persiste en Firestore con 1h de vigencia
     * y lo retorna para que el controlador pueda mostrarlo/enviarlo.
     *
     * @param email Email del usuario
     * @return El token generado, o empty si el email no existe en el sistema
     */
    public Optional<String> generateResetToken(String email) {
        // Verificar que el email esté registrado
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(email);
        if (usuario.isEmpty()) {
            // Retornamos empty pero SIN revelar en la UI si el email existe (seguridad)
            return Optional.empty();
        }

        String token    = UUID.randomUUID().toString();
        Date   expiry   = calcularExpiracion();

        tokenRepository.save(new PasswordResetToken(token, email, expiry));
        return Optional.of(token);
    }

    // ----------------------------------------------------------
    // Validar token
    // ----------------------------------------------------------

    /**
     * Verifica que el token exista, no haya expirado y no haya sido usado.
     */
    public boolean isTokenValid(String token) {
        return tokenRepository.findByToken(token)
                              .map(t -> !t.isExpiredOrUsed())
                              .orElse(false);
    }

    /**
     * Retorna el token si es válido.
     */
    public Optional<PasswordResetToken> findValidToken(String token) {
        return tokenRepository.findByToken(token)
                              .filter(t -> !t.isExpiredOrUsed());
    }

    // ----------------------------------------------------------
    // Ejecutar el reset
    // ----------------------------------------------------------

    /**
     * Valida el token, actualiza la contraseña del usuario y marca el token como usado.
     *
     * @throws IllegalArgumentException si el token no es válido o el usuario no existe
     */
    public void resetPassword(String token, String nuevaPasswordPlana) {
        PasswordResetToken prt = findValidToken(token)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El enlace de restablecimiento no es válido o ha expirado."));

        Usuario usuario = usuarioService.buscarPorEmail(prt.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el usuario asociado al token."));

        usuarioService.cambiarContrasena(usuario.getId(), nuevaPasswordPlana);
        tokenRepository.markAsUsed(token);
    }

    // ----------------------------------------------------------
    // Privados
    // ----------------------------------------------------------

    private Date calcularExpiracion() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, EXPIRY_MINUTES);
        return cal.getTime();
    }
}
