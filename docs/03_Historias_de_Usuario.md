# Documento 3 — Historias de Usuario
### Sistema Web de Gestión — Creaciones Edimile

---

## Formato de Historia de Usuario

```
HU-XX: [Título]
Como [rol],
quiero [acción o funcionalidad],
para [beneficio o resultado esperado].

Criterios de Aceptación:
  ✔ CA-1: ...
  ✔ CA-2: ...
  ✔ CA-3: ...

Prioridad: Alta / Media / Baja
Estimación: [Puntos de historia]
```

---
---

## MÓDULO 1: AUTENTICACIÓN Y USUARIOS

---

### HU-01: Iniciar Sesión en el Sistema

**Como** usuario del sistema (Administrador o Vendedor),  
**quiero** iniciar sesión con mi correo electrónico y contraseña,  
**para** acceder a las funcionalidades del sistema según mi rol asignado.

**Criterios de Aceptación:**
- ✔ CA-1: Al ingresar credenciales correctas, el sistema redirige al panel de inicio (dashboard).
- ✔ CA-2: Al ingresar credenciales incorrectas, se muestra un mensaje de error claro sin revelar cuál campo es incorrecto.
- ✔ CA-3: El sistema bloquea el acceso a cualquier ruta interna si el usuario no ha iniciado sesión (redirige al login).
- ✔ CA-4: El formulario de login incluye campo de email y contraseña con validación de campos vacíos.

**Prioridad:** Alta | **Estimación:** 3 puntos

---

### HU-02: Cerrar Sesión

**Como** usuario autenticado,  
**quiero** cerrar mi sesión de forma segura,  
**para** proteger la información del sistema cuando deje de usarlo.

**Criterios de Aceptación:**
- ✔ CA-1: Al hacer clic en "Cerrar sesión", el sistema invalida la sesión y redirige al login.
- ✔ CA-2: Tras cerrar sesión, no es posible navegar al dashboard con el botón "atrás" del navegador.

**Prioridad:** Alta | **Estimación:** 1 punto

---

### HU-03: Crear Nuevo Usuario (Administrador)

**Como** Administrador,  
**quiero** crear nuevos usuarios para el sistema,  
**para** permitir que otros empleados accedan con su propio acceso y rol.

**Criterios de Aceptación:**
- ✔ CA-1: El formulario solicita: nombre, apellido, email, contraseña y rol.
- ✔ CA-2: El email debe ser único; si ya existe, se muestra error.
- ✔ CA-3: La contraseña se almacena cifrada con BCrypt.
- ✔ CA-4: El usuario recién creado aparece en el listado con estado "Activo".
- ✔ CA-5: Solo el Administrador puede acceder a la gestión de usuarios.

**Prioridad:** Alta | **Estimación:** 3 puntos

---

### HU-04: Editar y Desactivar Usuario

**Como** Administrador,  
**quiero** editar los datos de un usuario o desactivarlo,  
**para** mantener actualizada la información del personal y revocar accesos cuando sea necesario.

**Criterios de Aceptación:**
- ✔ CA-1: El Administrador puede editar nombre, apellido, email y rol de cualquier usuario.
- ✔ CA-2: El Administrador puede cambiar el estado del usuario entre Activo e Inactivo.
- ✔ CA-3: Un usuario inactivo no puede iniciar sesión.
- ✔ CA-4: No se permite eliminar usuarios permanentemente.

**Prioridad:** Media | **Estimación:** 2 puntos

---
---

## MÓDULO 2: CLIENTES

---

### HU-05: Registrar Nuevo Cliente

**Como** Vendedor o Administrador,  
**quiero** registrar un nuevo cliente en el sistema,  
**para** tener su información disponible al crear cotizaciones y ventas.

**Criterios de Aceptación:**
- ✔ CA-1: El formulario incluye: nombre, apellido, empresa (opcional), tipo de documento, número de documento, teléfono, email y dirección.
- ✔ CA-2: El número de documento debe ser único; si ya existe, se muestra error.
- ✔ CA-3: Los campos nombre, tipo de documento y número de documento son obligatorios.
- ✔ CA-4: El cliente queda registrado con estado Activo y fecha de registro automática.

**Prioridad:** Alta | **Estimación:** 3 puntos

---

### HU-06: Buscar y Listar Clientes

**Como** Vendedor o Administrador,  
**quiero** buscar y listar clientes registrados,  
**para** seleccionarlos rápidamente al crear cotizaciones o consultar su historial.

**Criterios de Aceptación:**
- ✔ CA-1: El sistema muestra la lista paginada de clientes activos.
- ✔ CA-2: Se puede buscar por nombre, número de documento o empresa.
- ✔ CA-3: El resultado de búsqueda se actualiza al escribir o al presionar buscar.
- ✔ CA-4: Cada fila del listado tiene acciones: Ver detalle, Editar, Desactivar.

**Prioridad:** Alta | **Estimación:** 2 puntos

---

### HU-07: Ver Historial de Cliente

**Como** Vendedor o Administrador,  
**quiero** ver el historial de cotizaciones y ventas de un cliente,  
**para** tener contexto de su relación comercial con la empresa.

**Criterios de Aceptación:**
- ✔ CA-1: La pantalla de detalle del cliente muestra sus datos y dos secciones: cotizaciones y ventas.
- ✔ CA-2: Cada cotización y venta en el historial muestra número, fecha, estado y total.
- ✔ CA-3: Se puede hacer clic en cada ítem del historial para ver su detalle completo.

**Prioridad:** Media | **Estimación:** 2 puntos

---
---

## MÓDULO 3: CATÁLOGO DE PRODUCTOS

---

### HU-08: Gestionar Categorías de Productos

**Como** Administrador,  
**quiero** crear y gestionar categorías de productos,  
**para** organizar el catálogo y facilitar la búsqueda de artículos.

**Criterios de Aceptación:**
- ✔ CA-1: El Administrador puede crear categorías con nombre y descripción.
- ✔ CA-2: Las categorías se pueden editar y activar/desactivar.
- ✔ CA-3: No se puede desactivar una categoría que tenga productos activos asociados.
- ✔ CA-4: El listado de categorías muestra nombre, estado y cantidad de productos asociados.

**Prioridad:** Alta | **Estimación:** 2 puntos

---

### HU-09: Registrar Producto

**Como** Administrador,  
**quiero** registrar un nuevo producto en el catálogo,  
**para** que esté disponible al generar cotizaciones y ventas.

**Criterios de Aceptación:**
- ✔ CA-1: El formulario incluye: nombre, descripción, categoría, precio base e imagen de referencia (URL o carga de archivo).
- ✔ CA-2: El nombre del producto y la categoría son obligatorios.
- ✔ CA-3: El precio base debe ser mayor a cero.
- ✔ CA-4: El producto queda activo y visible en el catálogo al guardarse.

**Prioridad:** Alta | **Estimación:** 3 puntos

---

### HU-10: Ver Catálogo de Productos

**Como** Vendedor o Administrador,  
**quiero** ver el catálogo de productos disponibles,  
**para** conocer la oferta de la empresa al atender clientes.

**Criterios de Aceptación:**
- ✔ CA-1: El catálogo muestra productos en tarjetas con imagen, nombre, categoría y precio base.
- ✔ CA-2: Se puede filtrar por categoría y buscar por nombre.
- ✔ CA-3: Solo se muestran productos activos.

**Prioridad:** Media | **Estimación:** 2 puntos

---
---

## MÓDULO 4: INVENTARIO DE INSUMOS

---

### HU-11: Registrar Insumo

**Como** Administrador,  
**quiero** registrar un nuevo insumo en el inventario,  
**para** llevar el control de los materiales necesarios para la producción.

**Criterios de Aceptación:**
- ✔ CA-1: El formulario incluye: nombre, descripción, unidad de medida, stock inicial, stock mínimo y precio unitario.
- ✔ CA-2: Nombre y unidad de medida son campos obligatorios.
- ✔ CA-3: El stock y el precio unitario deben ser valores positivos o cero.
- ✔ CA-4: Si el stock inicial es menor o igual al stock mínimo, el insumo aparece marcado como "Stock Bajo" al guardarse.

**Prioridad:** Alta | **Estimación:** 3 puntos

---

### HU-12: Registrar Entrada de Insumo

**Como** Administrador,  
**quiero** registrar una entrada de insumo (compra o reposición),  
**para** actualizar el stock disponible y mantener el inventario al día.

**Criterios de Aceptación:**
- ✔ CA-1: El formulario solicita: insumo, cantidad y descripción opcional.
- ✔ CA-2: El stock del insumo se incrementa automáticamente al guardar.
- ✔ CA-3: El movimiento queda registrado en el historial con tipo "ENTRADA", fecha y usuario.

**Prioridad:** Alta | **Estimación:** 2 puntos

---

### HU-13: Registrar Salida de Insumo

**Como** Administrador,  
**quiero** registrar la salida de un insumo (uso en producción),  
**para** reflejar el consumo real y evitar discrepancias de inventario.

**Criterios de Aceptación:**
- ✔ CA-1: El sistema valida que la cantidad de salida no supere el stock disponible.
- ✔ CA-2: Si la cantidad supera el stock, se muestra un error y no se registra la salida.
- ✔ CA-3: El stock se decrementa automáticamente al guardar.
- ✔ CA-4: Si el nuevo stock queda igual o por debajo del mínimo, se muestra alerta visual.

**Prioridad:** Alta | **Estimación:** 2 puntos

---

### HU-14: Ver Alertas de Stock Mínimo

**Como** Administrador,  
**quiero** ver un listado de insumos con stock bajo,  
**para** tomar acciones de reposición antes de que afecte la producción.

**Criterios de Aceptación:**
- ✔ CA-1: El dashboard muestra un widget con el conteo de insumos en stock crítico.
- ✔ CA-2: El listado de insumos resalta visualmente (color rojo o naranja) los que están en o bajo el mínimo.
- ✔ CA-3: Existe una vista de "Insumos en Stock Crítico" con filtro rápido.

**Prioridad:** Media | **Estimación:** 2 puntos

---
---

## MÓDULO 5: COTIZACIONES

---

### HU-15: Crear Cotización

**Como** Vendedor o Administrador,  
**quiero** crear una cotización para un cliente,  
**para** formalizar una propuesta comercial con detalle de productos y precios.

**Criterios de Aceptación:**
- ✔ CA-1: La cotización debe asociarse a un cliente existente.
- ✔ CA-2: Se pueden agregar uno o más ítems (producto + cantidad + descripción de personalización).
- ✔ CA-3: El sistema calcula automáticamente subtotal por ítem, subtotal general, descuento (%), IVA (%) y total.
- ✔ CA-4: Se asigna automáticamente un número de cotización (ej: COT-0001).
- ✔ CA-5: La cotización se guarda con estado "Pendiente" y fecha de creación automática.
- ✔ CA-6: La fecha de vencimiento es editable por el usuario.

**Prioridad:** Alta | **Estimación:** 5 puntos

---

### HU-16: Gestionar Estado de Cotización

**Como** Vendedor o Administrador,  
**quiero** cambiar el estado de una cotización,  
**para** registrar si fue aprobada, rechazada o venció sin respuesta.

**Criterios de Aceptación:**
- ✔ CA-1: Los estados posibles son: Pendiente, Aprobada, Rechazada, Vencida.
- ✔ CA-2: Solo se puede pasar a "Aprobada" o "Rechazada" desde "Pendiente".
- ✔ CA-3: Una cotización aprobada muestra el botón "Convertir en Venta".
- ✔ CA-4: Una cotización rechazada o vencida no puede editarse.

**Prioridad:** Alta | **Estimación:** 3 puntos

---

### HU-17: Descargar Cotización en PDF

**Como** Vendedor o Administrador,  
**quiero** descargar la cotización en formato PDF,  
**para** enviarla al cliente por correo o imprimirla.

**Criterios de Aceptación:**
- ✔ CA-1: El PDF incluye: logo/nombre de la empresa, datos del cliente, número y fecha de cotización, tabla de ítems, descuento, IVA y total.
- ✔ CA-2: El PDF se descarga al hacer clic en el botón "Descargar PDF" en la vista de detalle.
- ✔ CA-3: El nombre del archivo sigue el formato: `Cotizacion_COT-0001.pdf`.

**Prioridad:** Media | **Estimación:** 3 puntos

---
---

## MÓDULO 6: VENTAS

---

### HU-18: Registrar Venta Directa

**Como** Vendedor o Administrador,  
**quiero** registrar una venta directa sin necesidad de cotización previa,  
**para** atender clientes que realizan pedidos inmediatos.

**Criterios de Aceptación:**
- ✔ CA-1: El formulario incluye: cliente, ítem(s) de producto, método de pago y observaciones.
- ✔ CA-2: Se asigna automáticamente un número de factura (ej: FAC-0001).
- ✔ CA-3: El sistema calcula subtotal, descuento, IVA y total.
- ✔ CA-4: La venta queda guardada con estado "Pendiente" (hasta confirmar pago).

**Prioridad:** Alta | **Estimación:** 5 puntos

---

### HU-19: Convertir Cotización en Venta

**Como** Vendedor o Administrador,  
**quiero** convertir una cotización aprobada en una venta,  
**para** registrar formalmente la transacción sin volver a ingresar los datos.

**Criterios de Aceptación:**
- ✔ CA-1: Solo cotizaciones con estado "Aprobada" tienen el botón "Convertir en Venta".
- ✔ CA-2: Al convertir, se crea una venta con los mismos ítems y totales de la cotización.
- ✔ CA-3: El usuario solo debe seleccionar el método de pago antes de confirmar.
- ✔ CA-4: La cotización queda vinculada a la venta generada.

**Prioridad:** Alta | **Estimación:** 3 puntos

---

### HU-20: Gestionar Estado de Venta

**Como** Administrador,  
**quiero** cambiar el estado de una venta (Pendiente → Pagada, o Anular),  
**para** reflejar con exactitud el estado financiero de cada transacción.

**Criterios de Aceptación:**
- ✔ CA-1: El Administrador puede marcar una venta como "Pagada".
- ✔ CA-2: El Administrador puede "Anular" una venta con un campo de justificación obligatorio.
- ✔ CA-3: Las ventas anuladas aparecen en el listado con indicador visual diferenciado.
- ✔ CA-4: Los Vendedores solo pueden consultar ventas, no cambiar su estado.

**Prioridad:** Alta | **Estimación:** 2 puntos

---
---

## MÓDULO 7: REPORTES

---

### HU-21: Generar Reporte de Ventas por Período

**Como** Administrador,  
**quiero** generar un reporte de ventas filtrado por fechas,  
**para** analizar el desempeño comercial en un período determinado.

**Criterios de Aceptación:**
- ✔ CA-1: El reporte permite seleccionar rango de fechas (inicio y fin).
- ✔ CA-2: El reporte muestra: número de factura, cliente, fecha, método de pago, estado y total.
- ✔ CA-3: El reporte muestra el total de ventas del período seleccionado.
- ✔ CA-4: El reporte puede descargarse en PDF y Excel.

**Prioridad:** Alta | **Estimación:** 4 puntos

---

### HU-22: Generar Reporte de Inventario de Insumos

**Como** Administrador,  
**quiero** generar un reporte del estado actual del inventario,  
**para** tomar decisiones de compra y planificación de producción.

**Criterios de Aceptación:**
- ✔ CA-1: El reporte muestra todos los insumos con: nombre, unidad, stock actual, stock mínimo y estado.
- ✔ CA-2: Los insumos en stock crítico se destacan visualmente en el reporte.
- ✔ CA-3: El reporte puede descargarse en PDF y Excel.

**Prioridad:** Media | **Estimación:** 3 puntos

---

### HU-23: Generar Reporte de Cotizaciones

**Como** Vendedor o Administrador,  
**quiero** generar un reporte de cotizaciones filtrado por estado y período,  
**para** hacer seguimiento a las propuestas comerciales en curso y cerradas.

**Criterios de Aceptación:**
- ✔ CA-1: Se puede filtrar por estado (Pendiente, Aprobada, Rechazada, Vencida) y rango de fechas.
- ✔ CA-2: El reporte muestra: número de cotización, cliente, fecha, estado y total.
- ✔ CA-3: El reporte puede descargarse en PDF.

**Prioridad:** Media | **Estimación:** 3 puntos

---

## Resumen de Historias de Usuario

| Módulo | Historias | Puntos Estimados |
|--------|-----------|-----------------|
| Autenticación y Usuarios | HU-01 a HU-04 | 9 |
| Clientes | HU-05 a HU-07 | 7 |
| Productos | HU-08 a HU-10 | 7 |
| Inventario de Insumos | HU-11 a HU-14 | 9 |
| Cotizaciones | HU-15 a HU-17 | 11 |
| Ventas | HU-18 a HU-20 | 10 |
| Reportes | HU-21 a HU-23 | 10 |
| **Total** | **23 Historias** | **63 puntos** |

---

*Documento elaborado para la materia de Programación en Java — 2024*
