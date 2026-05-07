# Diagrama Entidad-Relación (DER)
### Base de Datos: creaciones_edimile

---

## Descripción del Diagrama

El diagrama muestra las **11 tablas** de la base de datos y sus relaciones.

---

## DER — Diagrama Mermaid (renderizable en GitHub, Notion, etc.)

> Para visualizarlo: pega el código en [https://mermaid.live](https://mermaid.live) o en cualquier visor Mermaid.

```mermaid
erDiagram

    ROLES {
        int     id              PK
        varchar nombre
        varchar descripcion
        boolean activo
    }

    USUARIOS {
        int       id             PK
        varchar   nombre
        varchar   apellido
        varchar   email
        varchar   password       "Hash BCrypt"
        int       id_rol         FK
        boolean   activo
        timestamp fecha_creacion
    }

    CLIENTES {
        int       id                PK
        varchar   nombre
        varchar   apellido
        varchar   empresa
        enum      tipo_documento    "CC/NIT/CE/PASAPORTE"
        varchar   numero_documento
        varchar   telefono
        varchar   email
        varchar   direccion
        boolean   activo
        timestamp fecha_registro
    }

    CATEGORIAS {
        int     id          PK
        varchar nombre
        varchar descripcion
        boolean activo
    }

    PRODUCTOS {
        int       id             PK
        varchar   nombre
        text      descripcion
        int       id_categoria   FK
        decimal   precio_base
        varchar   imagen_url
        boolean   activo
        timestamp fecha_creacion
    }

    INSUMOS {
        int     id              PK
        varchar nombre
        varchar descripcion
        varchar unidad_medida
        decimal stock_actual
        decimal stock_minimo
        decimal precio_unitario
        boolean activo
    }

    MOVIMIENTOS_INSUMO {
        int       id              PK
        int       id_insumo       FK
        enum      tipo_movimiento "ENTRADA/SALIDA/AJUSTE"
        decimal   cantidad
        timestamp fecha
        varchar   descripcion
        int       id_usuario      FK
    }

    COTIZACIONES {
        int     id                  PK
        varchar numero_cotizacion
        int     id_cliente          FK
        int     id_usuario          FK
        timestamp fecha
        date    fecha_vencimiento
        enum    estado              "PENDIENTE/APROBADA/RECHAZADA/VENCIDA"
        decimal subtotal
        decimal descuento_pct
        decimal iva_pct
        decimal total
        text    observaciones
    }

    DETALLE_COTIZACION {
        int     id                          PK
        int     id_cotizacion               FK
        int     id_producto                 FK
        int     cantidad
        decimal precio_unitario
        text    descripcion_personalizacion
        decimal subtotal
    }

    VENTAS {
        int       id               PK
        varchar   numero_factura
        int       id_cotizacion    FK  "NULL si venta directa"
        int       id_cliente       FK
        int       id_usuario       FK
        timestamp fecha
        decimal   subtotal
        decimal   descuento_pct
        decimal   iva_pct
        decimal   total
        enum      metodo_pago      "EFECTIVO/TRANSFERENCIA/CHEQUE/TARJETA"
        enum      estado           "PENDIENTE/PAGADA/ANULADA"
        text      observaciones
    }

    DETALLE_VENTA {
        int     id                          PK
        int     id_venta                    FK
        int     id_producto                 FK
        int     cantidad
        decimal precio_unitario
        text    descripcion_personalizacion
        decimal subtotal
    }

    %% ─── RELACIONES ───────────────────────────────────────────
    ROLES              ||--o{ USUARIOS              : "tiene"
    USUARIOS           ||--o{ MOVIMIENTOS_INSUMO    : "registra"
    USUARIOS           ||--o{ COTIZACIONES          : "elabora"
    USUARIOS           ||--o{ VENTAS                : "registra"
    CLIENTES           ||--o{ COTIZACIONES          : "solicita"
    CLIENTES           ||--o{ VENTAS                : "realiza"
    CATEGORIAS         ||--o{ PRODUCTOS             : "clasifica"
    PRODUCTOS          ||--o{ DETALLE_COTIZACION    : "aparece en"
    PRODUCTOS          ||--o{ DETALLE_VENTA         : "aparece en"
    INSUMOS            ||--o{ MOVIMIENTOS_INSUMO    : "registra movimientos"
    COTIZACIONES       ||--o{ DETALLE_COTIZACION    : "contiene"
    COTIZACIONES       |o--o| VENTAS                : "origina (opcional)"
    VENTAS             ||--o{ DETALLE_VENTA         : "contiene"
```

---

## Descripción de Relaciones

| Relación | Tipo | Descripción |
|----------|------|-------------|
| ROLES → USUARIOS | 1:N | Un rol puede asignarse a muchos usuarios |
| USUARIOS → COTIZACIONES | 1:N | Un usuario elabora muchas cotizaciones |
| USUARIOS → VENTAS | 1:N | Un usuario registra muchas ventas |
| USUARIOS → MOVIMIENTOS_INSUMO | 1:N | Un usuario registra muchos movimientos |
| CLIENTES → COTIZACIONES | 1:N | Un cliente puede tener muchas cotizaciones |
| CLIENTES → VENTAS | 1:N | Un cliente puede tener muchas ventas |
| CATEGORIAS → PRODUCTOS | 1:N | Una categoría agrupa muchos productos |
| COTIZACIONES → DETALLE_COTIZACION | 1:N | Una cotización tiene uno o más ítems |
| VENTAS → DETALLE_VENTA | 1:N | Una venta tiene uno o más ítems |
| COTIZACIONES → VENTAS | 1:0..1 | Una cotización aprobada puede originar una venta (opcional) |
| INSUMOS → MOVIMIENTOS_INSUMO | 1:N | Un insumo tiene muchos movimientos de stock |
| PRODUCTOS → DETALLE_COTIZACION | 1:N | Un producto aparece en muchos detalles de cotización |
| PRODUCTOS → DETALLE_VENTA | 1:N | Un producto aparece en muchos detalles de venta |

---

## Descripción de Tablas

| Tabla | Registros demo | Propósito |
|-------|---------------|-----------|
| `roles` | 2 | Roles del sistema: ADMINISTRADOR, VENDEDOR |
| `usuarios` | 3 | Empleados que acceden al sistema |
| `clientes` | 5 | Clientes de Creaciones Edimile |
| `categorias` | 6 | Clasificación del catálogo de productos |
| `productos` | 10 | Artículos ofrecidos por la empresa |
| `insumos` | 12 | Materias primas e insumos de producción |
| `movimientos_insumo` | 17 | Entradas y salidas del inventario |
| `cotizaciones` | 3 | Propuestas comerciales generadas |
| `detalle_cotizacion` | 4 | Ítems de cada cotización |
| `ventas` | 2 | Ventas registradas |
| `detalle_venta` | 4 | Ítems de cada venta |

---

*Para visualizar este diagrama en MySQL Workbench, consulta el archivo `Guia_MySQL_Workbench.md`.*
