# Creaciones Edimile — Sistema Web de Gestión

Sistema web desarrollado en **Java con Spring Boot** para la gestión integral de la empresa **Creaciones Edimile**, dedicada a la producción de artículos personalizados: camisetas estampadas, dotaciones empresariales, gorras, lujos para automóviles y más.

---

## Estructura del Repositorio

```
Creaciones-Edimile/
├── pom.xml                                  ← Build Maven
├── .gitignore
├── README.md
├── docs/
│   ├── 01_Objetivos_Justificacion_Entorno.md
│   ├── 02_Requerimientos.md
│   ├── 03_Historias_de_Usuario.md
│   ├── 04_Base_de_Datos/
│   │   ├── creaciones_edimile.sql
│   │   ├── DER.md
│   │   └── Guia_MySQL_Workbench.md
│   └── 05_Login_Firebase_Explicacion.md    ← Explicación técnica del módulo login
└── src/
    ├── main/
    │   ├── java/com/creacionesedimile/
    │   │   ├── CreacionesEdimileApplication.java
    │   │   ├── config/
    │   │   │   ├── FirebaseConfig.java      ← Inicialización Firebase + Firestore
    │   │   │   └── SecurityConfig.java      ← Spring Security (rutas, login, logout)
    │   │   ├── model/        Usuario.java
    │   │   ├── repository/   UsuarioRepository.java  ← CRUD en Firestore
    │   │   ├── service/
    │   │   │   ├── UserDetailsServiceImpl.java  ← Puente Spring Security ↔ Firestore
    │   │   │   └── UsuarioService.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java      ← GET /login, GET /403
    │   │   │   └── DashboardController.java ← Dashboard + admin/usuarios
    │   │   └── init/  DataInitializer.java  ← Crea admin por defecto al arrancar
    │   └── resources/
    │       ├── application.properties
    │       ├── firebase/
    │       │   ├── README.md
    │       │   ├── service-account.json     ← IGNORADO (.gitignore) — debes generarlo
    │       │   └── service-account-example.json
    │       ├── templates/
    │       │   ├── login.html
    │       │   ├── dashboard.html
    │       │   ├── fragments/ (navbar, sidebar)
    │       │   ├── admin/usuarios/ (lista, form)
    │       │   └── error/ (403)
    │       └── static/css/custom.css
    └── test/
```

---

## Módulos del Sistema

| # | Módulo | Descripción |
|---|--------|-------------|
| 1 | Autenticación y Usuarios | Login, roles (Administrador / Vendedor) y gestión de accesos |
| 2 | Clientes | Registro, edición y consulta de clientes |
| 3 | Catálogo de Productos | Productos, categorías y precios base |
| 4 | Inventario de Insumos | Control de stock, entradas/salidas y alertas |
| 5 | Cotizaciones | Generación, gestión de estados y exportación PDF |
| 6 | Ventas | Registro de ventas, métodos de pago y facturación |
| 7 | Reportes | Informes descargables en PDF y Excel |

---

## Tecnologías Utilizadas

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 17 (LTS) |
| Framework | Spring Boot 3.3.x |
| Seguridad | Spring Security 6.x |
| Base de datos | **Firebase Firestore** (NoSQL en la nube) |
| Frontend | Thymeleaf 3.x + Bootstrap 5.3 |
| Reportes | iText (PDF) / Apache POI (Excel) |
| Control de versiones | Git + GitHub |
| Gestor dependencias | Maven 3.8+ |

---

## Cómo ejecutar el proyecto

> **Requisitos previos**: JDK 17+, Maven 3.8+, cuenta en Firebase

### 1. Clonar el repositorio
```bash
git clone https://github.com/TU_USUARIO/creaciones-edimile.git
cd creaciones-edimile
```

### 2. Configurar Firebase
```
a. Ir a https://console.firebase.google.com
b. Crear un proyecto (o usar uno existente)
c. Ir a: Configuración del proyecto → Cuentas de servicio
d. Hacer clic en "Generar nueva clave privada"
e. Guardar el archivo como:
   src/main/resources/firebase/service-account.json
f. Habilitar Firestore Database en modo producción
```

### 3. Ejecutar
```bash
mvn spring-boot:run
```

### 4. Acceder
```
http://localhost:8080
```

### Credenciales del administrador por defecto

| Campo | Valor |
|-------|-------|
| Correo | `admin@creacionesedimile.com` |
| Contraseña | `Admin123!` |

> **Cambia la contraseña tras el primer inicio de sesión.**

---

## Equipo de Desarrollo

> Proyecto desarrollado para la materia de **Programación en Java** — 2024

---

## Documentación

Toda la documentación inicial del proyecto se encuentra en la carpeta [`docs/`](docs/).
