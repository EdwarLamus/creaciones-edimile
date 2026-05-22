# Creaciones Edimile — Sistema Web de Gestión

Sistema web desarrollado en **Java con Spring Boot** para la gestión integral de la empresa **Creaciones Edimile**, dedicada a la producción de artículos personalizados: camisetas estampadas, dotaciones empresariales, gorras, lujos para automóviles y más.

---

## Requisitos previos

| Herramienta | Versión mínima | Verificar con |
|-------------|---------------|---------------|
| Java JDK | 17 o superior | `java -version` |
| Maven | 3.8+ | `mvn -version` *(o usar VS Code Extension Pack for Java)* |
| Cuenta Firebase | — | [console.firebase.google.com](https://console.firebase.google.com) |

---

## Configuración de Firebase (obligatorio antes de ejecutar)

> ⚠️ El archivo `service-account.json` **no está en el repositorio** (seguridad).  
> Cada integrante del equipo debe obtenerlo y colocarlo manualmente.

**Pasos:**

1. Abre [Firebase Console](https://console.firebase.google.com) → proyecto **creaciones-edimile**
2. Ícono de engranaje → **Configuración del proyecto** → pestaña **Cuentas de servicio**
3. Clic en **"Generar nueva clave privada"** → descarga el archivo JSON
4. Renómbralo exactamente como `service-account.json` y colócalo en:
   ```
   src/main/resources/firebase/service-account.json
   ```
5. En Firebase Console → **Firestore Database** → verifica que la base de datos esté creada  
   *(si no existe: "Crear base de datos" → Modo producción → región us-central)*

---

## Cómo ejecutar el proyecto

**Opción A — VS Code (recomendado):**
1. Abrir la carpeta del proyecto en VS Code
2. Instalar la extensión **Extension Pack for Java** (si no la tienes)
3. Abrir `src/main/java/com/creacionesedimile/CreacionesEdimileApplication.java`
4. Clic en el botón ▶ **Run** que aparece sobre el método `main`
5. Acceder en el navegador: `http://localhost:8080`

**Opción B — Terminal (si Maven está instalado):**
```bash
mvn spring-boot:run
```

### Credenciales por defecto (primer arranque)

La aplicación crea automáticamente el usuario administrador si Firestore está vacío:

| Campo | Valor |
|-------|-------|
| Correo | `admin@creacionesedimile.com` |
| Contraseña | `Admin123!` |
| Rol | Administrador |

> ⚠️ Cambia la contraseña tras el primer inicio de sesión.

---

## Módulos implementados

| # | Módulo | Rutas | Estado |
|---|--------|-------|--------|
| 1 | Autenticación | `/login`, `/logout` | ✅ Completo |
| 2 | Dashboard | `/dashboard` | ✅ Completo |
| 3 | Gestión de usuarios (Admin) | `/admin/usuarios/**` | ✅ Completo |
| 4 | Recuperar contraseña | `/forgot-password`, `/reset-password` | ✅ Completo |
| 5 | Perfil de usuario | `/perfil`, `/perfil/configuracion` | ✅ Completo |
| 6 | Clientes | — | 🔲 Pendiente |
| 7 | Catálogo de Productos | — | 🔲 Pendiente |
| 8 | Inventario de Insumos | — | 🔲 Pendiente |
| 9 | Cotizaciones | — | 🔲 Pendiente |
| 10 | Ventas | — | 🔲 Pendiente |
| 11 | Reportes | — | 🔲 Pendiente |

---

## Tecnologías utilizadas

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 20 |
| Framework | Spring Boot 3.3.4 |
| Seguridad | Spring Security 6.x + BCrypt |
| Base de datos | Firebase Firestore (NoSQL en la nube) |
| Frontend | Thymeleaf 3.x + Bootstrap 5.3 + Bootstrap Icons |
| Validación cliente | JavaScript (password-validation.js) |

---

## Estructura del proyecto

```
src/main/java/com/creacionesedimile/
├── config/          FirebaseConfig.java, SecurityConfig.java
├── controller/      AuthController, DashboardController,
│                    PasswordResetController, PerfilController
├── init/            DataInitializer.java   ← crea admin al arrancar
├── model/           Usuario.java, PasswordResetToken.java
├── repository/      UsuarioRepository.java, PasswordResetTokenRepository.java
└── service/         UserDetailsServiceImpl, UsuarioService, PasswordResetService

src/main/resources/
├── firebase/
│   ├── service-account.json       ← NO está en el repo (ver instrucciones arriba)
│   └── README.md                  ← Instrucciones detalladas para obtenerlo
├── templates/
│   ├── login.html, dashboard.html, forgot-password.html, reset-password.html
│   ├── fragments/  navbar.html, sidebar.html
│   ├── admin/usuarios/  lista.html, form.html
│   └── perfil/  ver.html, configuracion.html
└── static/
    ├── css/  custom.css
    └── js/   password-validation.js
```

---

## Documentación técnica

| Archivo | Contenido |
|---------|-----------|
| [docs/01_Objetivos_Justificacion_Entorno.md](docs/01_Objetivos_Justificacion_Entorno.md) | Objetivos del proyecto |
| [docs/02_Requerimientos.md](docs/02_Requerimientos.md) | Requerimientos funcionales y no funcionales |
| [docs/03_Historias_de_Usuario.md](docs/03_Historias_de_Usuario.md) | Historias de usuario |
| [docs/05_Login_Firebase_Explicacion.md](docs/05_Login_Firebase_Explicacion.md) | Explicación técnica del módulo de autenticación |


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

> Proyecto desarrollado para la materia de **Programación en Java** — 2026

---

## Documentación

Toda la documentación inicial del proyecto se encuentra en la carpeta [`docs/`](docs/).
