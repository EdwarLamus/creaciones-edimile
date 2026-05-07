# Creaciones Edimile — Sistema Web de Gestión

Sistema web desarrollado en **Java con Spring Boot** para la gestión integral de la empresa **Creaciones Edimile**, dedicada a la producción de artículos personalizados: camisetas estampadas, dotaciones empresariales, gorras, lujos para automóviles y más.

---

## Estructura del Repositorio

```
Creaciones-Edimile/
├── README.md
└── docs/
    ├── 01_Objetivos_Justificacion_Entorno.md
    ├── 02_Requerimientos.md
    ├── 03_Historias_de_Usuario.md
    └── 04_Base_de_Datos/
        ├── creaciones_edimile.sql
        ├── DER.md
        └── Guia_MySQL_Workbench.md
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
| Framework | Spring Boot 3.x |
| Seguridad | Spring Security |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL 8.x |
| Frontend | Thymeleaf + Bootstrap 5 |
| Reportes | iText (PDF) / Apache POI (Excel) |
| Control de versiones | Git + GitHub |
| Gestor dependencias | Maven |

---

## Cómo ejecutar el proyecto

> **Requisitos previos**: JDK 17+, Maven 3.8+, MySQL 8.x

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/TU_USUARIO/creaciones-edimile.git
   ```
2. Crear la base de datos ejecutando el script:
   ```
   docs/04_Base_de_Datos/creaciones_edimile.sql
   ```
3. Configurar `application.properties` con las credenciales de MySQL.
4. Ejecutar:
   ```bash
   mvn spring-boot:run
   ```
5. Acceder en: `http://localhost:8080`

---

## Equipo de Desarrollo

> Proyecto desarrollado para la materia de **Programación en Java** — 2024

---

## Documentación

Toda la documentación inicial del proyecto se encuentra en la carpeta [`docs/`](docs/).
