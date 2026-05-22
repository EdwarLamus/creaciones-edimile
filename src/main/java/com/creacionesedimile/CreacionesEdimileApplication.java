package com.creacionesedimile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Creaciones Edimile.
 * Spring Boot configura automáticamente:
 *  - Tomcat embebido (puerto 8080)
 *  - Spring Security (todas las rutas protegidas por defecto)
 *  - Thymeleaf (motor de plantillas)
 *  - Firebase Admin SDK (configurado en FirebaseConfig)
 */
@SpringBootApplication
public class CreacionesEdimileApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreacionesEdimileApplication.class, args);
    }
}
