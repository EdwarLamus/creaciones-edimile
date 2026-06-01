package com.creacionesedimile.service;

import com.creacionesedimile.model.Usuario;
import com.creacionesedimile.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio de negocio para gestión de Usuarios.
 *
 * Responsabilidades:
 *  - Validar reglas de negocio (email único, etc.)
 *  - Cifrar contraseñas antes de persistir (BCrypt)
 *  - Delegar operaciones CRUD al repositorio
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -------------------------------------------------------
    // Consultas
    // -------------------------------------------------------

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    // -------------------------------------------------------
    // Creación
    // -------------------------------------------------------

    /**
     * Crea un nuevo usuario cifrando su contraseña con BCrypt.
     *
     * @throws IllegalArgumentException si el email ya está registrado
     * @return ID del documento creado en Firestore
     */
    public String crearUsuario(String nombre, String apellido,
                               String email, String passwordPlano, String rol) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el correo: " + email);
        }

        // La contraseña se cifra aquí; NUNCA se almacena en texto plano
        String hashBcrypt = passwordEncoder.encode(passwordPlano);

        Usuario nuevo = new Usuario(nombre, apellido, email, hashBcrypt, rol);
        nuevo.setFechaCreacion(new Date());
        return usuarioRepository.save(nuevo);
    }

    // -------------------------------------------------------
    // Actualización
    // -------------------------------------------------------

    /**
     * Activa o desactiva un usuario (eliminación lógica).
     * Un usuario desactivado no puede iniciar sesión.
     */
    public void actualizarEstado(String id, boolean activo) {
        Map<String, Object> campos = new HashMap<>();
        campos.put("activo", activo);
        usuarioRepository.update(id, campos);
    }

    /**
     * Actualiza los datos básicos de un usuario (sin cambiar contraseña).
     */
    public void actualizarDatos(String id, String nombre, String apellido, String rol) {
        Map<String, Object> campos = new HashMap<>();
        campos.put("nombre",   nombre);
        campos.put("apellido", apellido);
        campos.put("rol",      rol);
        usuarioRepository.update(id, campos);
    }

    /**
     * Cambia la contraseña de un usuario cifrándola con BCrypt.
     * Usado tanto por el reset de contraseña como por el perfil del usuario.
     */
    public void cambiarContrasena(String id, String nuevaPasswordPlana) {
        Map<String, Object> campos = new HashMap<>();
        campos.put("password", passwordEncoder.encode(nuevaPasswordPlana));
        usuarioRepository.update(id, campos);
    }

    /**
     * Verifica que una contraseña en texto plano coincida con el hash BCrypt almacenado.
     */
    public boolean verificarContrasena(String passwordPlano, String hashAlmacenado) {
        return passwordEncoder.matches(passwordPlano, hashAlmacenado);
    }

    public void eliminar(String id) {
        usuarioRepository.delete(id);
    }
}
