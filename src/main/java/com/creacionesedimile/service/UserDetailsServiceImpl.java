package com.creacionesedimile.service;

import com.creacionesedimile.model.Usuario;
import com.creacionesedimile.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de UserDetailsService para Spring Security.
 *
 * Es el puente entre Spring Security y nuestra base de datos (Firebase Firestore).
 * Spring Security llama a loadUserByUsername() automáticamente cuando el usuario
 * envía el formulario de login.
 *
 * Flujo:
 *  1. Spring Security recibe email + contraseña del formulario POST /login
 *  2. Llama a loadUserByUsername(email)
 *  3. Este método consulta Firestore con UsuarioRepository
 *  4. Devuelve un UserDetails con email, hash BCrypt y roles
 *  5. Spring Security compara el hash con la contraseña ingresada
 *  6. Si coinciden: sesión creada → redirect a /dashboard
 *  7. Si no: redirect a /login?error=true
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Buscar usuario por email en Firestore
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No existe usuario con email: " + email));

        // 2. Construir el objeto UserDetails que Spring Security entiende
        //    .roles("ADMIN") → Spring Security agrega automáticamente el prefijo "ROLE_"
        //    Resultado: GrantedAuthority = "ROLE_ADMIN"
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())    // ya es un hash BCrypt
                .roles(usuario.getRol())             // "ADMIN" → "ROLE_ADMIN"
                .disabled(!usuario.isActivo())       // usuario inactivo no puede entrar
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(false)
                .build();
    }
}
