# Documento 5 — Login Completo: Spring Boot + Spring Security + Firebase + Thymeleaf
### Sistema Web de Gestión — Creaciones Edimile

---

## 1. Descripción General

Se implementó el **Módulo de Autenticación y Gestión de Usuarios** (RF-01 / HU-01 a HU-04) usando:

| Capa | Tecnología | Responsabilidad |
|------|-----------|----------------|
| Base de datos | **Firebase Firestore** | Almacena documentos de usuarios y tokens de reset en la nube (NoSQL) |
| Seguridad | **Spring Security 6** | Intercepta peticiones, valida credenciales, gestiona sesión |
| Hash de contraseñas | **BCrypt** | Cifrado unidireccional de contraseñas (nunca texto plano) |
| Backend | **Spring Boot 3 / Spring MVC** | Controladores, servicios, repositorios |
| Frontend | **Thymeleaf + Bootstrap 5** | Plantillas HTML renderizadas en servidor |
| Validación cliente | **JavaScript (password-validation.js)** | Criterios de contraseña en tiempo real |

### Módulos implementados

| Módulo | Rutas | Descripción |
|--------|-------|-------------|
| Autenticación | `/login`, `/logout` | Login con email/contraseña, cierre de sesión seguro |
| Dashboard | `/dashboard` | Panel principal según rol del usuario |
| Gestión de usuarios | `/admin/usuarios/**` | CRUD de usuarios (solo ADMIN) |
| Recuperación de contraseña | `/forgot-password`, `/reset-password` | Flujo con token UUID guardado en Firestore |
| Perfil de usuario | `/perfil`, `/perfil/configuracion` | Ver datos y cambiar contraseña con validación |

---

## 2. Arquitectura de Capas

```
┌─────────────────────────────────────────────────────────────┐
│                    NAVEGADOR (Cliente)                       │
│  HTML generado por Thymeleaf + Bootstrap 5                   │
│  password-validation.js (validación en tiempo real)          │
└───────────────────────────┬─────────────────────────────────┘
                            │  HTTP POST /login
                            │  HTTP GET  /dashboard
                            │  HTTP GET/POST /forgot-password
                            │  HTTP GET/POST /reset-password
                            │  HTTP GET/POST /perfil/**
┌───────────────────────────▼─────────────────────────────────┐
│               SPRING SECURITY (Filtro HTTP)                  │
│  SecurityFilterChain: rutas públicas vs. protegidas          │
│  Llama a UserDetailsServiceImpl en cada autenticación        │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│            CONTROLADORES (Spring MVC @Controller)            │
│  AuthController          → GET /login, GET /403              │
│  DashboardController     → GET /dashboard, /admin/usuarios   │
│  PasswordResetController → /forgot-password, /reset-password │
│  PerfilController        → /perfil, /perfil/configuracion    │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                  SERVICIOS (@Service)                        │
│  UserDetailsServiceImpl  → puente Spring Security ↔ Firestore│
│  UsuarioService          → CRUD usuarios, BCrypt, verificar  │
│  PasswordResetService    → generar/validar/consumir tokens   │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│              REPOSITORIOS (@Repository)                      │
│  UsuarioRepository           → colección "usuarios"          │
│  PasswordResetTokenRepository→ colección "password_reset_tokens"│
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│           FIREBASE FIRESTORE (Base de datos NoSQL)           │
│  Colección: "usuarios"                                       │
│  Colección: "password_reset_tokens"                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Flujo Completo de Autenticación (paso a paso)

### 3.1 Login Exitoso

```
Usuario → GET /login
    → Spring Security: ruta pública → permite pasar
    → AuthController.loginPage() → devuelve "login"
    → Thymeleaf renderiza templates/login.html
    → Navegador muestra el formulario

Usuario llena email + contraseña → POST /login
    → Spring Security intercepta (UsernamePasswordAuthenticationFilter)
    → Llama a UserDetailsServiceImpl.loadUserByUsername(email)
        → UsuarioRepository.findByEmail(email) → consulta Firestore
        → Si no existe → UsernameNotFoundException → redirect /login?error
        → Si existe → devuelve UserDetails con:
             username = email
             password = hash BCrypt almacenado en Firestore
             roles    = ["ROLE_ADMIN"] o ["ROLE_VENDEDOR"]
    → Spring Security compara: BCrypt.matches(passwordIngresada, hashFirestore)
        → Si NO coinciden → redirect /login?error=true
        → Si coinciden → crea sesión HTTP → redirect /dashboard

Usuario llega al dashboard
    → Spring Security: sesión válida → permite pasar
    → DashboardController.dashboard() → consulta nombre del usuario
    → Thymeleaf renderiza dashboard.html con nombre y módulos por rol
```

### 3.2 Login Fallido

```
POST /login con credenciales incorrectas
    → Spring Security: BCrypt no coincide
    → Redirige a /login?error=true
    → AuthController agrega: model.addAttribute("errorMsg", "...")
    → Thymeleaf muestra la alerta roja en el formulario
```

### 3.3 Acceso sin Sesión a Ruta Protegida

```
GET /dashboard (sin sesión activa)
    → Spring Security: ruta protegida, sin autenticación
    → Redirige automáticamente a /login
    → Tras login exitoso, Spring Security redirige de vuelta a /dashboard
```

### 3.4 Acceso a Ruta Restringida por Rol

```
Usuario VENDEDOR → GET /admin/usuarios
    → Spring Security: ruta requiere ROLE_ADMIN
    → Usuario tiene solo ROLE_VENDEDOR
    → Lanza AccessDeniedException
    → Redirige a /403
    → AuthController.accessDenied() → renderiza error/403.html
```

### 3.5 Cierre de Sesión

```
Usuario → POST /logout (opción "Cerrar sesión" en el menú desplegable del navbar)
    → Spring Security:
        - Invalida la sesión HTTP (HttpSession.invalidate())
        - Elimina la cookie JSESSIONID
    → Redirige a /login?logout=true
    → AuthController muestra mensaje de confirmación
```

### 3.6 Recuperar Contraseña Olvidada

```
Usuario → GET /login
    → Clic en "¿Olvidaste tu contraseña?"
    → Redirige a GET /forgot-password
    → PasswordResetController muestra formulario de email

Usuario ingresa su email → POST /forgot-password
    → PasswordResetService.generateResetToken(email)
        → UsuarioRepository.findByEmail(email) → busca en Firestore
        → Si NO existe: retorna Optional.empty() (sin revelar si el email existe)
        → Si existe:
            - Genera UUID token
            - Crea documento en Firestore colección "password_reset_tokens":
              { token: "uuid", email: "...", expiresAt: ahora+1h, used: false }
            - Retorna el token
    → La página muestra el enlace de reset (modo desarrollo)
      En producción: se enviaría por email

Usuario visita el enlace → GET /reset-password?token=UUID
    → PasswordResetService.isTokenValid(token)
        → PasswordResetTokenRepository.findByToken(token) → busca en Firestore por ID
        → Verifica: ¿existe? ¿no expiró? ¿no fue usado?
    → Si inválido/expirado: muestra mensaje de error
    → Si válido: muestra formulario para nueva contraseña

Usuario ingresa nueva contraseña → POST /reset-password
    → Valida que ambas contraseñas coincidan
    → Valida política: mín. 8 chars, 1 mayúscula, 1 número, 1 carácter especial
    → PasswordResetService.resetPassword(token, nuevaPassword)
        → UsuarioService.cambiarContrasena(id, password) → actualiza Firestore
        → PasswordResetTokenRepository.markAsUsed(token) → used=true en Firestore
    → Redirige a /login con mensaje de éxito
```

### 3.7 Cambiar Contraseña desde el Perfil

```
Usuario autenticado → Clic en su nombre en navbar → "Cambiar contraseña"
    → GET /perfil/configuracion
    → PerfilController.verConfiguracion()
        → Obtiene usuario desde SecurityContext (@AuthenticationPrincipal)
        → Renderiza perfil/configuracion.html con validación en tiempo real

Usuario llena el formulario → POST /perfil/configuracion
    → Obtiene usuario autenticado por email
    → UsuarioService.verificarContrasena(actual, hashFirestore)
        → BCryptPasswordEncoder.matches() → si no coincide, muestra error
    → Valida que nueva == confirmación
    → Valida política de contraseña (mismas reglas que reset)
    → UsuarioService.cambiarContrasena(id, nuevaPassword)
        → BCrypt.encode(nueva) → actualiza campo "password" en Firestore
    → Redirige con mensaje de éxito
```

### 3.8 Menú de Usuario (Navbar Dropdown)

```
Cualquier página con navbar
    → El nombre del usuario en la parte superior es un botón dropdown
    → Al hacer clic despliega el menú con:
        - Cabecera: email + badge de rol
        - "Mi perfil"         → GET /perfil
        - "Cambiar contraseña"→ GET /perfil/configuracion
        - Separador
        - "Cerrar sesión"     → POST /logout (formulario con CSRF)
```

---

## 4. Componentes Clave Explicados

### 4.1 SecurityConfig.java

```java
// Define QUIÉN puede acceder a QUÉ rutas
.authorizeHttpRequests(auth -> auth
    // Rutas públicas: login, recuperación de contraseña, recursos estáticos
    .requestMatchers("/login", "/forgot-password", "/reset-password",
                     "/css/**", "/js/**", "/images/**").permitAll()
    .requestMatchers("/admin/**").hasRole("ADMIN")       // solo admins
    .anyRequest().authenticated()                        // lo demás: login requerido
)

// Configura el formulario de login
.formLogin(form -> form
    .loginPage("/login")              // nuestra página personalizada
    .usernameParameter("email")       // el campo se llama "email" en el form
    .defaultSuccessUrl("/dashboard")  // a dónde ir tras login exitoso
    .failureUrl("/login?error=true")  // a dónde ir si falla
)

// Cierre de sesión seguro
.logout(logout -> logout
    .invalidateHttpSession(true)   // destruye la sesión
    .deleteCookies("JSESSIONID")   // elimina la cookie
)
```

### 4.2 UserDetailsServiceImpl.java

```java
// Spring Security llama a este método para cargar el usuario
public UserDetails loadUserByUsername(String email) {
    Usuario u = usuarioRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException(...));

    return User.builder()
        .username(u.getEmail())
        .password(u.getPassword())  // hash BCrypt — Spring lo compara internamente
        .roles(u.getRol())          // "ADMIN" → Spring convierte a "ROLE_ADMIN"
        .disabled(!u.isActivo())    // usuario inactivo no puede entrar
        .build();
}
```

### 4.3 UsuarioRepository.java (Firebase en lugar de JPA)

```java
// En lugar de extends JpaRepository<Usuario, Long>
// consultamos Firestore directamente usando ApiFuture (async → sync con .get())

public Optional<Usuario> findByEmail(String email) {
    ApiFuture<QuerySnapshot> future = firestore.collection("usuarios")
        .whereEqualTo("email", email)
        .limit(1)
        .get();

    List<QueryDocumentSnapshot> docs = future.get().getDocuments();
    // mapeo manual documento Firestore → objeto Usuario
}
```

### 4.4 Thymeleaf + Spring Security

En los templates HTML, `sec:authorize` muestra/oculta elementos según el rol:

```html
<!-- Solo visible para administradores -->
<div sec:authorize="hasRole('ADMIN')">
    <a href="/admin/usuarios">Gestión de Usuarios</a>
</div>

<!-- Muestra el email del usuario autenticado -->
<span sec:authentication="name">usuario@email.com</span>
```

### 4.5 Protección CSRF

Spring Security habilita protección CSRF por defecto. Con Thymeleaf, el token
se incluye **automáticamente** en los formularios al usar `th:action`:

```html
<!-- Thymeleaf genera: <input type="hidden" name="_csrf" value="TOKEN"> -->
<form th:action="@{/login}" method="post">
```

---

### 4.6 PasswordResetController.java + PasswordResetService.java

Implementan el flujo completo de recuperación de contraseña con tokens de un solo uso:

```java
// PasswordResetService — generar token
public Optional<String> generateResetToken(String email) {
    Optional<Usuario> usuario = usuarioService.buscarPorEmail(email);
    if (usuario.isEmpty()) return Optional.empty(); // no revelar si el email existe

    String token  = UUID.randomUUID().toString();   // token UUID impredecible
    Date   expiry = ahora + 60 minutos;             // vigencia 1 hora

    tokenRepository.save(new PasswordResetToken(token, email, expiry));
    return Optional.of(token);
}

// PasswordResetService — consumir token
public void resetPassword(String token, String nuevaPassword) {
    PasswordResetToken prt = findValidToken(token) // verifica vigencia
        .orElseThrow(...);

    usuarioService.cambiarContrasena(usuario.getId(), nuevaPassword); // actualiza Firestore
    tokenRepository.markAsUsed(token); // marca used=true en Firestore (no se puede reusar)
}
```

**Características de seguridad del sistema de tokens:**
- Token = UUID v4 aleatorio (128 bits de entropía, impredecible)
- Expira en **60 minutos**
- Es de **un solo uso** — tras usarse, `used = true` en Firestore
- El sistema NO revela si un email está registrado (misma respuesta para emails válidos e inválidos)
- El token es el ID del documento en Firestore (búsqueda directa sin índices adicionales)

---

### 4.7 PerfilController.java

Maneja la vista del perfil y el cambio de contraseña del usuario autenticado:

```java
@GetMapping
public String verPerfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    // @AuthenticationPrincipal obtiene el usuario de la sesión actual
    Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername())
            .orElseThrow(...);
    model.addAttribute("usuario", usuario);
    return "perfil/ver";
}

@PostMapping("/configuracion")
public String cambiarContrasena(@AuthenticationPrincipal UserDetails userDetails,
        @RequestParam String passwordActual,
        @RequestParam String passwordNueva, ...) {

    // 1. Verificar que la contraseña actual sea correcta
    if (!usuarioService.verificarContrasena(passwordActual, usuario.getPassword()))
        → error "La contraseña actual no es correcta"

    // 2. Validar política: mín. 8 chars, 1 mayúscula, 1 número, 1 especial
    // 3. Actualizar en Firestore via UsuarioService.cambiarContrasena()
}
```

> El usuario **no puede cambiar su email** desde el perfil — ese campo solo lo gestiona el administrador.

---

### 4.8 password-validation.js

Script JavaScript compartido entre `reset-password.html` y `perfil/configuracion.html`
que valida los criterios de la contraseña **en tiempo real** mientras el usuario escribe:

```javascript
function evaluarContrasena(valor) {
    // Actualiza visualmente 4 criterios con check verde / X roja
    criterios = {
        'c-length':  valor.length >= 8,          // mínimo 8 caracteres
        'c-upper':   /[A-Z]/.test(valor),         // al menos 1 mayúscula
        'c-number':  /[0-9]/.test(valor),         // al menos 1 número
        'c-special': /[^A-Za-z0-9]/.test(valor)  // al menos 1 carácter especial
    };
}

function evaluarCoincidencia() {
    // Compara el campo de confirmación con el campo de nueva contraseña
    // Muestra criterio "c-match" con check o X en tiempo real
}
```

Los criterios usan las clases CSS `.criterio`, `.criterio-ok`, `.criterio-fail`
con variante `.criterio-light` para fondos blancos (perfil) y oscuros (login/reset).

---

## 5. Modelo de Datos en Firebase Firestore

### Colección: `usuarios`

Cada usuario es un **documento** con los siguientes campos:

```json
{
  "nombre":        "Juan",
  "apellido":      "Pérez",
  "email":         "juan@creacionesedimile.com",
  "password":      "$2a$10$xyz...",   // hash BCrypt (NUNCA texto plano)
  "rol":           "VENDEDOR",         // "ADMIN" o "VENDEDOR"
  "activo":        true,
  "fechaCreacion": "2024-01-15T10:30:00Z"
}
```

### Colección: `password_reset_tokens`

Cada solicitud de recuperación de contraseña crea un documento. El **ID del documento es el token UUID** (permite búsqueda directa sin índices):

```json
{
  "email":     "juan@creacionesedimile.com",
  "expiresAt": "2024-01-15T11:30:00Z",  // ahora + 1 hora
  "used":      false,                    // true tras ser usado (no se borra, queda como auditoría)
  "createdAt": "2024-01-15T10:30:00Z"
}
```

> El token NO se almacena como campo — es el ID del documento Firestore.  
> Los tokens usados o expirados no se borran, sirven de **registro de auditoría**.

### Diferencia con MySQL (modelo anterior)

| MySQL (anterior) | Firebase Firestore (actual) |
|-----------------|----------------------------|
| Tabla `usuarios` con columnas fijas | Colección `usuarios` con documentos flexibles |
| ID numérico autoincremental | ID alfanumérico generado por Firestore |
| Spring Data JPA + Hibernate | Firebase Admin SDK (`ApiFuture<>`) |
| `@Entity`, `@Table`, `@Column` | POJO plano + mapeo manual |
| Consultas SQL | Métodos de la API de Firestore |
| No había soporte para tokens de reset | Colección `password_reset_tokens` con expiración |

---

## 6. Dependencias Maven Utilizadas

```xml
<!-- Spring Security: autenticación y autorización -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Thymeleaf: motor de plantillas -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Integración Thymeleaf + Spring Security (sec:authorize) -->
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>

<!-- Firebase Admin SDK (Firestore) -->
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

---

## 7. Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/creacionesedimile/
│   │   ├── CreacionesEdimileApplication.java       # Punto de entrada
│   │   ├── config/
│   │   │   ├── FirebaseConfig.java                 # Inicializa Firebase + bean Firestore
│   │   │   └── SecurityConfig.java                 # Reglas de seguridad Spring Security
│   │   ├── model/
│   │   │   ├── Usuario.java                        # POJO del usuario
│   │   │   └── PasswordResetToken.java             # POJO del token de reset
│   │   ├── repository/
│   │   │   ├── UsuarioRepository.java              # CRUD colección "usuarios"
│   │   │   └── PasswordResetTokenRepository.java   # CRUD colección "password_reset_tokens"
│   │   ├── service/
│   │   │   ├── UserDetailsServiceImpl.java         # Puente Spring Security ↔ Firestore
│   │   │   ├── UsuarioService.java                 # Lógica de negocio + BCrypt
│   │   │   └── PasswordResetService.java           # Generar/validar/consumir tokens
│   │   ├── controller/
│   │   │   ├── AuthController.java                 # GET /login, GET /403
│   │   │   ├── DashboardController.java            # /dashboard, /admin/usuarios/**
│   │   │   ├── PasswordResetController.java        # /forgot-password, /reset-password
│   │   │   └── PerfilController.java               # /perfil, /perfil/configuracion
│   │   └── init/
│   │       └── DataInitializer.java                # Crea usuario admin en primer arranque
│   └── resources/
│       ├── application.properties
│       ├── firebase/
│       │   ├── README.md
│       │   ├── service-account.json                # IGNORADO por .gitignore
│       │   └── service-account-example.json        # Plantilla de referencia
│       ├── templates/
│       │   ├── login.html                          # Página de login (con enlace "olvidé")
│       │   ├── dashboard.html                      # Panel principal
│       │   ├── forgot-password.html                # Formulario solicitar reset
│       │   ├── reset-password.html                 # Formulario nueva contraseña + criterios
│       │   ├── fragments/
│       │   │   ├── navbar.html                     # Navbar con dropdown de usuario
│       │   │   └── sidebar.html                    # Menú lateral
│       │   ├── admin/usuarios/
│       │   │   ├── lista.html                      # Listado de usuarios
│       │   │   └── form.html                       # Formulario nuevo usuario
│       │   ├── perfil/
│       │   │   ├── ver.html                        # Vista de perfil del usuario
│       │   │   └── configuracion.html              # Cambio de contraseña + criterios
│       │   └── error/
│       │       └── 403.html                        # Página de acceso denegado
│       └── static/
│           ├── css/
│           │   └── custom.css                      # Estilos globales + criterios contraseña
│           └── js/
│               └── password-validation.js          # Validación en tiempo real (compartido)
└── test/
    └── java/com/creacionesedimile/
        └── CreacionesEdimileApplicationTests.java
```

---

## 8. Cómo Ejecutar el Proyecto

### Requisitos previos
- Java 17+ (`java -version`)
- Maven 3.8+ (`mvn -version`)
- Cuenta en [Firebase](https://firebase.google.com) con proyecto creado

### Pasos

**1. Clonar el repositorio:**
```bash
git clone https://github.com/TU_USUARIO/creaciones-edimile.git
cd creaciones-edimile
```

**2. Configurar Firebase:**
```
a. Abre Firebase Console → tu proyecto → Configuración → Cuentas de servicio
b. Haz clic en "Generar nueva clave privada"
c. Guarda el archivo como:
   src/main/resources/firebase/service-account.json
```

**3. Habilitar Firestore:**
```
Firebase Console → Firestore Database → Crear base de datos → Modo producción
```

**4. Ejecutar:**
```bash
mvn spring-boot:run
```

**5. Acceder en el navegador:**
```
http://localhost:8080
```

### Credenciales por defecto (primer inicio)

> El `DataInitializer` crea este usuario automáticamente si la base de datos está vacía:

| Campo | Valor |
|-------|-------|
| Correo | `admin@creacionesedimile.com` |
| Contraseña | `Admin123!` |
| Rol | Administrador |

> ⚠️ **Cambia la contraseña inmediatamente** tras el primer inicio de sesión.

---

## 9. Seguridad Implementada (cumplimiento RNF-01)

| Requerimiento | Implementación |
|--------------|---------------|
| RNF-01.1: BCrypt | `BCryptPasswordEncoder` en `SecurityConfig` |
| RNF-01.2: Spring Security | `SecurityFilterChain` en `SecurityConfig` |
| RNF-01.3: Rutas protegidas | `.anyRequest().authenticated()` |
| RNF-01.4: Control por rol | `.hasRole("ADMIN")` + `sec:authorize` en templates |
| RNF-01.5: Sin inyección SQL | Firebase Firestore usa API parametrizada (sin SQL) |
| RNF-01.6: CSRF | Activado por defecto + Thymeleaf inserta token automáticamente |
| RNF-01.7: Política de contraseñas | Mín. 8 chars, 1 mayúscula, 1 número, 1 carácter especial (validado en backend y frontend) |
| RNF-01.8: Reset seguro de contraseña | Token UUID v4, expira en 1h, un solo uso, sin revelar si el email existe |
| RNF-01.9: Verificación de identidad | El cambio de contraseña desde el perfil exige la contraseña actual (BCrypt.matches) |
| RNF-01.10: Auditoría de tokens | Los tokens usados/expirados quedan en Firestore (no se borran) |
