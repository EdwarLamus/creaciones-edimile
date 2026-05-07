# Documento 1 — Objetivos, Justificación y Entorno de Trabajo
### Sistema Web de Gestión — Creaciones Edimile

---

## 1. Objetivos del Proyecto

### 1.1 Objetivo General

Desarrollar un sistema web para la empresa **Creaciones Edimile** que permita gestionar de manera centralizada y eficiente los procesos de autenticación de usuarios, gestión de clientes, catálogo de productos, control de inventario de insumos, cotizaciones, ventas y generación de reportes; utilizando Java con Spring Boot como tecnología principal y MySQL como motor de base de datos.

---

### 1.2 Objetivos Específicos

| # | Objetivo Específico |
|---|---------------------|
| OE-01 | Implementar un módulo de autenticación con control de roles (Administrador y Vendedor) que restrinja el acceso según privilegios. |
| OE-02 | Desarrollar un módulo de gestión de clientes que permita registrar, editar, consultar y desactivar clientes de forma organizada. |
| OE-03 | Construir un módulo de catálogo de productos con categorías, descripción, precio base e imagen de referencia. |
| OE-04 | Crear un módulo de control de inventario de insumos que registre entradas, salidas y emita alertas de stock mínimo. |
| OE-05 | Implementar un módulo de cotizaciones que permita generar propuestas comerciales, hacer seguimiento de su estado y exportarlas en PDF. |
| OE-06 | Desarrollar un módulo de ventas para registrar transacciones, asociarlas a clientes y gestionar los métodos de pago. |
| OE-07 | Implementar un módulo de reportes que permita generar y descargar informes consolidados en formato PDF y Excel. |
| OE-08 | Aplicar principios de desarrollo de software (MVC, SOLID, Clean Code) para garantizar un código mantenible y escalable. |

---

## 2. Justificación

**Creaciones Edimile** es una empresa dedicada a la producción y personalización de artículos como:

- Camisetas con diseños estampados o sublimados.
- Dotaciones corporativas para empresas.
- Gorras personalizadas.
- Lujos y accesorios decorativos para automóviles.
- Artículos en tela, vinilo, cuero, y otros materiales.

### 2.1 Problemática Actual

Actualmente la empresa gestiona sus operaciones de manera **manual o con herramientas no especializadas** (hojas de cálculo, registros en papel), lo que genera las siguientes dificultades:

| Problema | Impacto |
|----------|---------|
| Registro manual de cotizaciones | Pérdida de información, errores en precios y condiciones. |
| Sin control de inventario de insumos | Desabastecimiento inesperado que retrasa pedidos. |
| Historial de clientes disperso | Dificultad para el seguimiento comercial y fidelización. |
| Ausencia de reportes consolidados | Toma de decisiones basada en estimaciones, no en datos reales. |
| Sin control de usuarios y accesos | Cualquier persona puede modificar o eliminar información crítica. |

### 2.2 Beneficios Esperados

- **Centralización de la información**: todos los datos en una sola plataforma accesible desde la red local.
- **Reducción de errores**: validaciones automáticas en formularios y cálculos de totales, IVA y descuentos.
- **Trazabilidad**: historial completo de cotizaciones, ventas y movimientos de inventario.
- **Eficiencia operativa**: generación de cotizaciones y reportes en segundos.
- **Control de accesos**: roles diferenciados para proteger la integridad de los datos.

---

## 3. Entorno de Trabajo

### 3.1 Stack Tecnológico

| Componente | Tecnología | Versión |
|------------|-----------|---------|
| Lenguaje de programación | Java | 17 (LTS) |
| Framework backend | Spring Boot | 3.x |
| Motor de persistencia | Hibernate / Spring Data JPA | 3.x |
| Base de datos | MySQL | 8.x |
| Cliente de base de datos | MySQL Workbench | 8.x |
| Seguridad | Spring Security | 6.x |
| Motor de plantillas (frontend) | Thymeleaf | 3.x |
| Framework CSS | Bootstrap | 5.3 |
| Generación de reportes PDF | iText / OpenPDF | 8.x |
| Generación de reportes Excel | Apache POI | 5.x |
| Gestor de dependencias | Apache Maven | 3.8+ |
| Control de versiones | Git + GitHub | - |
| IDE recomendado | IntelliJ IDEA Community / Eclipse | Última versión estable |
| Servidor de aplicaciones | Tomcat embebido (Spring Boot) | 10.x |

---

### 3.2 Arquitectura del Sistema

El sistema sigue el patrón de diseño **MVC (Modelo-Vista-Controlador)** con una arquitectura en capas:

```
┌─────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                  │
│          Thymeleaf + HTML5 + Bootstrap 5                 │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP Request/Response
┌──────────────────────────▼──────────────────────────────┐
│                    CAPA DE CONTROLADORES                 │
│              Spring MVC (@Controller)                    │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                    CAPA DE SERVICIOS                     │
│              Lógica de negocio (@Service)                │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                    CAPA DE REPOSITORIOS                  │
│            Spring Data JPA (@Repository)                 │
└──────────────────────────┬──────────────────────────────┘
                           │ JDBC / Hibernate
┌──────────────────────────▼──────────────────────────────┐
│                    BASE DE DATOS                         │
│                    MySQL 8.x                             │
└─────────────────────────────────────────────────────────┘
```

---

### 3.3 Estructura del Proyecto Spring Boot

```
src/
├── main/
│   ├── java/com/edimile/
│   │   ├── config/          → Configuración de seguridad, beans
│   │   ├── controller/      → Controladores MVC
│   │   ├── dto/             → Objetos de transferencia de datos
│   │   ├── exception/       → Manejo global de excepciones
│   │   ├── model/           → Entidades JPA (@Entity)
│   │   ├── repository/      → Interfaces JPA Repository
│   │   ├── service/         → Lógica de negocio
│   │   └── util/            → Clases utilitarias (PDF, Excel)
│   └── resources/
│       ├── static/          → CSS, JS, imágenes
│       ├── templates/       → Plantillas Thymeleaf (.html)
│       └── application.properties
└── test/
    └── java/com/edimile/    → Pruebas unitarias
```

---

### 3.4 Herramientas de Desarrollo

| Herramienta | Uso |
|-------------|-----|
| **Git** | Control de versiones local |
| **GitHub** | Repositorio remoto y colaboración |
| **Postman** | Prueba de endpoints (si se exponen APIs REST) |
| **MySQL Workbench** | Diseño y administración de la base de datos |
| **Maven** | Gestión de dependencias y ciclo de vida del proyecto |

---

*Documento elaborado para la materia de Programación en Java — 2024*
