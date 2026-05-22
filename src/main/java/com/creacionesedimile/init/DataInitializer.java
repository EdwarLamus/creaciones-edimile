package com.creacionesedimile.init;

import com.creacionesedimile.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos que se ejecuta al arrancar la aplicación.
 *
 * Verifica si existe un usuario administrador en Firestore.
 * Si no existe (primera vez que se ejecuta el sistema), lo crea
 * automáticamente con credenciales por defecto.
 *
 * IMPORTANTE: Cambiar la contraseña del admin tras el primer login.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ADMIN_EMAIL    = "admin@creacionesedimile.com";
    private static final String ADMIN_PASSWORD = "Admin123!";

    private final UsuarioService usuarioService;

    public DataInitializer(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioService.buscarPorEmail(ADMIN_EMAIL).isEmpty()) {

            log.info("========================================================");
            log.info("  Primer inicio: creando usuario administrador...");

            usuarioService.crearUsuario(
                    "Administrador",
                    "Sistema",
                    ADMIN_EMAIL,
                    ADMIN_PASSWORD,
                    "ADMIN"
            );

            log.info("  Usuario creado exitosamente.");
            log.info("  Email   : {}", ADMIN_EMAIL);
            log.info("  Password: {}", ADMIN_PASSWORD);
            log.warn("  !! CAMBIA ESTA CONTRASEÑA TRAS EL PRIMER LOGIN !!");
            log.info("========================================================");

        } else {
            log.info("Usuario administrador ya existe en Firestore. OK.");
        }
    }
}
