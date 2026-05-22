# Documento 2 — Requerimientos Funcionales y No Funcionales
### Sistema Web de Gestión — Creaciones Edimile

---

## REQUERIMIENTOS FUNCIONALES

Los requerimientos funcionales describen **qué debe hacer el sistema**.

---

### RF-01: Módulo de Autenticación y Gestión de Usuarios

| Código | Requerimiento |
|--------|--------------|
| RF-01.1 | El sistema debe permitir el inicio de sesión mediante email y contraseña. |
| RF-01.2 | El sistema debe gestionar dos roles: **Administrador** y **Vendedor**. |
| RF-01.3 | El Administrador puede **crear** nuevos usuarios con rol asignado. |
| RF-01.4 | El Administrador puede **editar** los datos de cualquier usuario. |
| RF-01.5 | El Administrador puede **activar o desactivar** usuarios (no eliminar permanentemente). |
| RF-01.6 | El sistema debe permitir el **cierre de sesión** en cualquier momento. |
| RF-01.7 | Las **contraseñas** deben almacenarse cifradas con algoritmo BCrypt. |
| RF-01.8 | El sistema debe **restringir el acceso** a rutas no autorizadas según el rol del usuario. |
| RF-01.9 | El Administrador puede **listar y consultar** todos los usuarios registrados. |

---

### RF-02: Módulo de Gestión de Clientes

| Código | Requerimiento |
|--------|--------------|
| RF-02.1 | El sistema debe permitir **registrar** nuevos clientes con: nombre, apellido, empresa (opcional), tipo de documento, número de documento, teléfono, email y dirección. |
| RF-02.2 | El sistema debe permitir **editar** la información de un cliente existente. |
| RF-02.3 | El sistema debe permitir **desactivar** clientes (eliminación lógica, no física). |
| RF-02.4 | El sistema debe permitir **listar** todos los clientes con opción de búsqueda por nombre, número de documento o empresa. |
| RF-02.5 | El sistema debe mostrar el **historial de cotizaciones y ventas** asociadas a un cliente. |
| RF-02.6 | El sistema debe **validar** que el número de documento no se repita. |

---

### RF-03: Módulo de Catálogo de Productos

| Código | Requerimiento |
|--------|--------------|
| RF-03.1 | El Administrador puede **gestionar categorías** de productos: crear, editar y activar/desactivar. |
| RF-03.2 | El sistema debe permitir **registrar productos** con: nombre, descripción, categoría, precio base e imagen de referencia. |
| RF-03.3 | El sistema debe permitir **editar** los datos de un producto. |
| RF-03.4 | El sistema debe permitir **activar/desactivar** productos. |
| RF-03.5 | El sistema debe permitir **listar y buscar** productos por nombre o categoría. |
| RF-03.6 | El sistema debe mostrar una **vista de catálogo** con imagen, nombre y precio base de cada producto. |

---

### RF-04: Módulo de Inventario de Insumos

| Código | Requerimiento |
|--------|--------------|
| RF-04.1 | El sistema debe permitir **registrar insumos** con: nombre, descripción, unidad de medida, stock actual, stock mínimo y precio unitario. |
| RF-04.2 | El sistema debe permitir **registrar entradas** de insumos (aumentar stock). |
| RF-04.3 | El sistema debe permitir **registrar salidas** de insumos (disminuir stock). |
| RF-04.4 | El sistema debe **alertar visualmente** cuando el stock de un insumo esté en o por debajo del stock mínimo. |
| RF-04.5 | El sistema debe mostrar el **listado de insumos** con su stock actual y estado. |
| RF-04.6 | El sistema debe permitir consultar el **historial de movimientos** (entradas y salidas) de un insumo. |
| RF-04.7 | El sistema debe permitir **editar** la información de un insumo. |

---

### RF-05: Módulo de Cotizaciones

| Código | Requerimiento |
|--------|--------------|
| RF-05.1 | El sistema debe permitir **crear cotizaciones** asociadas a un cliente y un usuario vendedor. |
| RF-05.2 | Cada cotización debe permitir **agregar uno o varios ítems** (productos) con cantidad y descripción de personalización. |
| RF-05.3 | El sistema debe **calcular automáticamente** el subtotal, descuento (%), IVA (%) y total de cada cotización. |
| RF-05.4 | El sistema debe asignar un **número de cotización único y autoincremental** (ej: COT-0001). |
| RF-05.5 | El sistema debe gestionar los **estados de cotización**: Pendiente, Aprobada, Rechazada, Vencida. |
| RF-05.6 | El sistema debe permitir **listar y filtrar** cotizaciones por estado, cliente o rango de fechas. |
| RF-05.7 | El sistema debe permitir **convertir una cotización aprobada en venta** con un clic. |
| RF-05.8 | El sistema debe permitir **descargar la cotización en PDF** con el formato de la empresa. |
| RF-05.9 | El sistema debe permitir **editar** cotizaciones en estado Pendiente. |

---

### RF-06: Módulo de Ventas

| Código | Requerimiento |
|--------|--------------|
| RF-06.1 | El sistema debe permitir **registrar ventas directas** sin necesidad de cotización previa. |
| RF-06.2 | El sistema debe permitir **convertir cotizaciones aprobadas** en ventas automáticamente. |
| RF-06.3 | Cada venta debe asociarse a un **cliente**, un **vendedor** y un **método de pago** (Efectivo, Transferencia, Cheque, Tarjeta). |
| RF-06.4 | El sistema debe asignar un **número de factura único** a cada venta (ej: FAC-0001). |
| RF-06.5 | El sistema debe gestionar los **estados de la venta**: Pendiente, Pagada, Anulada. |
| RF-06.6 | El sistema debe permitir **listar y filtrar** ventas por fecha, estado o cliente. |
| RF-06.7 | El sistema debe mostrar el **detalle completo** de una venta. |
| RF-06.8 | El Administrador puede **anular** ventas con una justificación obligatoria. |

---

### RF-07: Módulo de Reportes

| Código | Requerimiento |
|--------|--------------|
| RF-07.1 | El sistema debe generar un **reporte de ventas por período** (diario, semanal, mensual, personalizado). |
| RF-07.2 | El sistema debe generar un **reporte de cotizaciones por estado** y período. |
| RF-07.3 | El sistema debe generar un **reporte de inventario actual** de insumos, destacando los que están en stock mínimo. |
| RF-07.4 | El sistema debe generar un **reporte de productos más vendidos** con cantidades y montos. |
| RF-07.5 | El sistema debe generar un **reporte de ventas por cliente**. |
| RF-07.6 | Todos los reportes deben poder **descargarse en PDF**. |
| RF-07.7 | Los reportes de ventas e inventario deben poder **descargarse en Excel (.xlsx)**. |

---
---

## REQUERIMIENTOS NO FUNCIONALES

Los requerimientos no funcionales describen **cómo debe comportarse el sistema**.

---

### RNF-01: Seguridad

| Código | Requerimiento |
|--------|--------------|
| RNF-01.1 | Las contraseñas deben almacenarse cifradas con **BCrypt** (nunca en texto plano). |
| RNF-01.2 | El sistema debe implementar **Spring Security** para proteger todas las rutas. |
| RNF-01.3 | Solo usuarios **autenticados** pueden acceder al sistema. Intentos sin sesión deben redirigir al login. |
| RNF-01.4 | Cada rol debe tener acceso **únicamente a las funcionalidades permitidas** (Administrador: acceso total; Vendedor: acceso restringido a reportes y administración de usuarios). |
| RNF-01.5 | El sistema debe **protegerse contra inyección SQL** usando consultas parametrizadas (JPA/Hibernate). |
| RNF-01.6 | Los formularios deben incluir protección **CSRF** (Cross-Site Request Forgery) habilitada por Spring Security. |

---

### RNF-02: Usabilidad

| Código | Requerimiento |
|--------|--------------|
| RNF-02.1 | La interfaz debe ser **responsiva**, adaptable a pantallas de escritorio y tablets. |
| RNF-02.2 | Los formularios deben mostrar **mensajes de validación claros** ante errores de entrada. |
| RNF-02.3 | El sistema debe proporcionar **retroalimentación visual** (mensajes de éxito o error) tras cada operación CRUD. |
| RNF-02.4 | La navegación debe ser **intuitiva** con menú lateral o superior organizado por módulos. |
| RNF-02.5 | Las listas con más de 10 registros deben implementar **paginación**. |

---

### RNF-03: Rendimiento

| Código | Requerimiento |
|--------|--------------|
| RNF-03.1 | El sistema debe responder a consultas simples en **menos de 3 segundos** bajo condiciones normales. |
| RNF-03.2 | Las consultas a la base de datos deben estar **optimizadas** con índices en campos de búsqueda frecuente (email, número de documento, número de cotización/factura). |
| RNF-03.3 | La generación de reportes en PDF/Excel debe completarse en **menos de 10 segundos**. |

---

### RNF-04: Disponibilidad y Confiabilidad

| Código | Requerimiento |
|--------|--------------|
| RNF-04.1 | El sistema debe manejar **errores sin colapsar**, mostrando páginas de error amigables (404, 500). |
| RNF-04.2 | El sistema debe operar en el **entorno de red local** de la empresa (desarrollo y despliegue local). |
| RNF-04.3 | La base de datos debe tener **restricciones de integridad referencial** (llaves foráneas) para evitar datos huérfanos. |

---

### RNF-05: Mantenibilidad

| Código | Requerimiento |
|--------|--------------|
| RNF-05.1 | El código debe seguir los **principios SOLID** y buenas prácticas de Java. |
| RNF-05.2 | El proyecto debe estar estructurado en **capas bien definidas** (Controller, Service, Repository, Model). |
| RNF-05.3 | Se debe usar **control de versiones Git** con ramas organizadas (main, develop, feature/*). |
| RNF-05.4 | Las **eliminaciones deben ser lógicas** (campo `activo = false`), no físicas, para preservar el historial. |

---

### RNF-06: Portabilidad

| Código | Requerimiento |
|--------|--------------|
| RNF-06.1 | El sistema debe poder ejecutarse en **Windows, Linux y macOS** sin modificaciones de código. |
| RNF-06.2 | El sistema debe ser empaquetable como un **archivo JAR ejecutable** (`mvn package`). |
| RNF-06.3 | La configuración de la base de datos debe ser **externalizable** vía `application.properties`. |

---

### RNF-07: Integridad de Datos

| Código | Requerimiento |
|--------|--------------|
| RNF-07.1 | Los campos obligatorios deben estar **validados** tanto en el frontend (Thymeleaf + HTML5) como en el backend (Bean Validation / @Valid). |
| RNF-07.2 | Los precios, cantidades y totales deben ser **siempre positivos** y no nulos. |
| RNF-07.3 | El número de documento de cliente y el email de usuario deben ser **únicos** en la base de datos. |

---

## Resumen de Requerimientos

| Módulo | RF Funcionales | RNF Aplicables |
|--------|---------------|----------------|
| Autenticación y Usuarios | 9 | RNF-01, RNF-02, RNF-07 |
| Clientes | 6 | RNF-02, RNF-05, RNF-07 |
| Productos | 6 | RNF-02, RNF-05 |
| Inventario de Insumos | 7 | RNF-02, RNF-03, RNF-07 |
| Cotizaciones | 9 | RNF-01, RNF-02, RNF-03, RNF-07 |
| Ventas | 8 | RNF-01, RNF-02, RNF-03, RNF-07 |
| Reportes | 7 | RNF-02, RNF-03 |
| **Total** | **52** | — |

---

*Documento elaborado para la materia de Programación en Java — 2026*
