package com.creacionesedimile;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    // En tests, apuntamos a un archivo de credenciales de prueba o desactivamos Firebase
    "firebase.credentials.path=classpath:firebase/service-account-test.json"
})
class CreacionesEdimileApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring Boot carga sin errores
    }
}
