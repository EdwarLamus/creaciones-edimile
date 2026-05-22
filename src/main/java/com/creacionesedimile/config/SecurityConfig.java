package com.creacionesedimile.config;

import com.creacionesedimile.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración central de Spring Security.
 *
 * Define:
 *  - Rutas públicas vs. rutas protegidas
 *  - Formulario de login personalizado
 *  - Cierre de sesión seguro
 *  - Cifrado de contraseñas con BCrypt
 *  - Proveedor de autenticación basado en Firestore (DaoAuthenticationProvider)
 *  - Página de acceso denegado (403)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // --------------------------------------------------
            // Control de acceso por URL
            // --------------------------------------------------
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos: login, CSS, JS, imágenes, recuperación de contraseña
                .requestMatchers("/login", "/forgot-password", "/reset-password",
                                 "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // Solo el rol ADMIN puede acceder a gestión de usuarios
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Cualquier otra ruta exige autenticación
                .anyRequest().authenticated()
            )

            // --------------------------------------------------
            // Formulario de Login personalizado
            // --------------------------------------------------
            .formLogin(form -> form
                .loginPage("/login")                  // GET /login → muestra el formulario
                .loginProcessingUrl("/login")         // POST /login → Spring Security procesa las credenciales
                .usernameParameter("email")           // el campo se llama "email" (no "username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true) // redirige aquí si login exitoso
                .failureUrl("/login?error=true")       // redirige aquí si credenciales incorrectas
                .permitAll()
            )

            // --------------------------------------------------
            // Cierre de sesión seguro
            // --------------------------------------------------
            .logout(logout -> logout
                .logoutUrl("/logout")                       // POST /logout
                .logoutSuccessUrl("/login?logout=true")     // tras logout, vuelve al login
                .invalidateHttpSession(true)                // destruye la sesión HTTP
                .deleteCookies("JSESSIONID")                // elimina la cookie de sesión
                .permitAll()
            )

            // --------------------------------------------------
            // Manejo de errores de autorización
            // --------------------------------------------------
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/403")
            );

        return http.build();
    }

    /**
     * BCryptPasswordEncoder: algoritmo seguro para hash de contraseñas.
     * Factor de costo por defecto = 10 (aprox. 100ms por hash, suficiente para
     * frustrar ataques de fuerza bruta).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider conecta Spring Security con nuestro
     * UserDetailsService (que consulta Firebase Firestore).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
