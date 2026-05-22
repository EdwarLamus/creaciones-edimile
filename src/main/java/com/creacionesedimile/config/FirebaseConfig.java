package com.creacionesedimile.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Configuración de Firebase Admin SDK.
 *
 * Lee el archivo service-account.json desde classpath y:
 *  1. Inicializa la instancia de FirebaseApp (singleton).
 *  2. Expone el bean Firestore para inyección en repositorios.
 *
 * Si el archivo no existe, la aplicación falla al arrancar con un
 * mensaje de error claro indicando cómo obtenerlo.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:classpath:firebase/service-account.json}")
    private String credentialsPath;

    private final ResourceLoader resourceLoader;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Si ya existe una instancia (ej: recarga DevTools), la reutilizamos
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase: instancia existente reutilizada.");
            return FirebaseApp.getInstance();
        }

        Resource resource = resourceLoader.getResource(credentialsPath);

        if (!resource.exists()) {
            throw new IOException(
                    "\n========================================================\n" +
                    "  ERROR: No se encontró el archivo de credenciales Firebase.\n" +
                    "  Ruta esperada: " + credentialsPath + "\n\n" +
                    "  Pasos para obtenerlo:\n" +
                    "  1. Abre https://console.firebase.google.com\n" +
                    "  2. Ve a: Configuración del proyecto → Cuentas de servicio\n" +
                    "  3. Haz clic en 'Generar nueva clave privada'\n" +
                    "  4. Guarda el archivo como:\n" +
                    "     src/main/resources/firebase/service-account.json\n" +
                    "========================================================"
            );
        }

        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase inicializado correctamente.");
            return app;
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
