-- ============================================================
-- BASE DE DATOS: creaciones_edimile
-- Motor: MySQL 8.x
-- Proyecto: Sistema Web de Gestión — Creaciones Edimile
-- Descripción: Script de creación completa de la base de datos
--              con datos de ejemplo para desarrollo y pruebas.
-- Fecha: 2026
-- ============================================================

-- ============================================================
-- PASO 1: Crear y seleccionar la base de datos
-- ============================================================
DROP DATABASE IF EXISTS creaciones_edimile;

CREATE DATABASE creaciones_edimile
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE creaciones_edimile;

-- ============================================================
-- PASO 2: Creación de tablas
-- (Orden importante: primero las tablas sin FK, luego las dependientes)
-- ============================================================

-- ------------------------------------------------------------
-- Tabla: roles
-- Almacena los roles del sistema: ADMINISTRADOR, VENDEDOR
-- ------------------------------------------------------------
CREATE TABLE roles (
    id          INT             NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50)     NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    activo      BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_roles PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='Roles de acceso al sistema';

-- ------------------------------------------------------------
-- Tabla: usuarios
-- Empleados que pueden iniciar sesión en el sistema
-- ------------------------------------------------------------
CREATE TABLE usuarios (
    id              INT             NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(100)    NOT NULL,
    apellido        VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL COMMENT 'Hash BCrypt',
    id_rol          INT             NOT NULL,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuarios  PRIMARY KEY (id),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol) REFERENCES roles(id)
) ENGINE=InnoDB COMMENT='Usuarios del sistema';

-- ------------------------------------------------------------
-- Tabla: clientes
-- Personas o empresas que solicitan cotizaciones y realizan compras
-- ------------------------------------------------------------
CREATE TABLE clientes (
    id               INT             NOT NULL AUTO_INCREMENT,
    nombre           VARCHAR(100)    NOT NULL,
    apellido         VARCHAR(100),
    empresa          VARCHAR(200),
    tipo_documento   ENUM('CC','NIT','CE','PASAPORTE') NOT NULL,
    numero_documento VARCHAR(20)     NOT NULL UNIQUE,
    telefono         VARCHAR(20),
    email            VARCHAR(150),
    direccion        VARCHAR(300),
    activo           BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_registro   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_clientes PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='Clientes de la empresa';

-- ------------------------------------------------------------
-- Tabla: categorias
-- Categorías para clasificar los productos del catálogo
-- ------------------------------------------------------------
CREATE TABLE categorias (
    id          INT             NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100)    NOT NULL UNIQUE,
    descripcion VARCHAR(300),
    activo      BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_categorias PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='Categorías de productos';

-- ------------------------------------------------------------
-- Tabla: productos
-- Catálogo de productos y servicios que ofrece la empresa
-- ------------------------------------------------------------
CREATE TABLE productos (
    id              INT             NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(150)    NOT NULL,
    descripcion     TEXT,
    id_categoria    INT             NOT NULL,
    precio_base     DECIMAL(10,2)   NOT NULL,
    imagen_url      VARCHAR(500),
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_productos     PRIMARY KEY (id),
    CONSTRAINT fk_productos_cat FOREIGN KEY (id_categoria) REFERENCES categorias(id),
    CONSTRAINT chk_precio_base  CHECK (precio_base > 0)
) ENGINE=InnoDB COMMENT='Catálogo de productos';

-- ------------------------------------------------------------
-- Tabla: insumos
-- Materias primas y materiales utilizados en la producción
-- ------------------------------------------------------------
CREATE TABLE insumos (
    id              INT             NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(150)    NOT NULL,
    descripcion     VARCHAR(300),
    unidad_medida   VARCHAR(50)     NOT NULL,
    stock_actual    DECIMAL(10,2)   NOT NULL DEFAULT 0,
    stock_minimo    DECIMAL(10,2)   NOT NULL DEFAULT 0,
    precio_unitario DECIMAL(10,2)   NOT NULL,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_insumos           PRIMARY KEY (id),
    CONSTRAINT chk_stock_actual     CHECK (stock_actual >= 0),
    CONSTRAINT chk_precio_unitario  CHECK (precio_unitario >= 0)
) ENGINE=InnoDB COMMENT='Inventario de insumos y materias primas';

-- ------------------------------------------------------------
-- Tabla: movimientos_insumo
-- Historial de entradas y salidas de cada insumo
-- ------------------------------------------------------------
CREATE TABLE movimientos_insumo (
    id               INT             NOT NULL AUTO_INCREMENT,
    id_insumo        INT             NOT NULL,
    tipo_movimiento  ENUM('ENTRADA','SALIDA','AJUSTE') NOT NULL,
    cantidad         DECIMAL(10,2)   NOT NULL,
    fecha            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion      VARCHAR(300),
    id_usuario       INT             NOT NULL,
    CONSTRAINT pk_movimientos       PRIMARY KEY (id),
    CONSTRAINT fk_mov_insumo        FOREIGN KEY (id_insumo)   REFERENCES insumos(id),
    CONSTRAINT fk_mov_usuario       FOREIGN KEY (id_usuario)  REFERENCES usuarios(id),
    CONSTRAINT chk_cantidad_mov     CHECK (cantidad > 0)
) ENGINE=InnoDB COMMENT='Movimientos de inventario de insumos';

-- ------------------------------------------------------------
-- Tabla: cotizaciones
-- Propuestas comerciales generadas para los clientes
-- ------------------------------------------------------------
CREATE TABLE cotizaciones (
    id                  INT             NOT NULL AUTO_INCREMENT,
    numero_cotizacion   VARCHAR(20)     NOT NULL UNIQUE,
    id_cliente          INT             NOT NULL,
    id_usuario          INT             NOT NULL,
    fecha               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento   DATE            NOT NULL,
    estado              ENUM('PENDIENTE','APROBADA','RECHAZADA','VENCIDA') NOT NULL DEFAULT 'PENDIENTE',
    subtotal            DECIMAL(12,2)   NOT NULL DEFAULT 0,
    descuento_pct       DECIMAL(5,2)    NOT NULL DEFAULT 0 COMMENT 'Porcentaje de descuento',
    iva_pct             DECIMAL(5,2)    NOT NULL DEFAULT 19 COMMENT 'Porcentaje de IVA',
    total               DECIMAL(12,2)   NOT NULL DEFAULT 0,
    observaciones       TEXT,
    CONSTRAINT pk_cotizaciones      PRIMARY KEY (id),
    CONSTRAINT fk_cot_cliente       FOREIGN KEY (id_cliente)  REFERENCES clientes(id),
    CONSTRAINT fk_cot_usuario       FOREIGN KEY (id_usuario)  REFERENCES usuarios(id),
    CONSTRAINT chk_total_cot        CHECK (total >= 0)
) ENGINE=InnoDB COMMENT='Cotizaciones generadas a clientes';

-- ------------------------------------------------------------
-- Tabla: detalle_cotizacion
-- Ítems (productos) incluidos en cada cotización
-- ------------------------------------------------------------
CREATE TABLE detalle_cotizacion (
    id                          INT             NOT NULL AUTO_INCREMENT,
    id_cotizacion               INT             NOT NULL,
    id_producto                 INT             NOT NULL,
    cantidad                    INT             NOT NULL,
    precio_unitario             DECIMAL(10,2)   NOT NULL,
    descripcion_personalizacion TEXT            COMMENT 'Descripción del diseño o personalización solicitada',
    subtotal                    DECIMAL(12,2)   NOT NULL,
    CONSTRAINT pk_det_cotizacion    PRIMARY KEY (id),
    CONSTRAINT fk_det_cot_cot       FOREIGN KEY (id_cotizacion) REFERENCES cotizaciones(id),
    CONSTRAINT fk_det_cot_prod      FOREIGN KEY (id_producto)   REFERENCES productos(id),
    CONSTRAINT chk_cantidad_det_cot CHECK (cantidad > 0)
) ENGINE=InnoDB COMMENT='Detalle de ítems en cotizaciones';

-- ------------------------------------------------------------
-- Tabla: ventas
-- Registro de transacciones de venta realizadas
-- ------------------------------------------------------------
CREATE TABLE ventas (
    id               INT             NOT NULL AUTO_INCREMENT,
    numero_factura   VARCHAR(20)     NOT NULL UNIQUE,
    id_cotizacion    INT             NULL COMMENT 'Null si la venta es directa sin cotización previa',
    id_cliente       INT             NOT NULL,
    id_usuario       INT             NOT NULL,
    fecha            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    descuento_pct    DECIMAL(5,2)    NOT NULL DEFAULT 0,
    iva_pct          DECIMAL(5,2)    NOT NULL DEFAULT 19,
    total            DECIMAL(12,2)   NOT NULL DEFAULT 0,
    metodo_pago      ENUM('EFECTIVO','TRANSFERENCIA','CHEQUE','TARJETA') NOT NULL,
    estado           ENUM('PENDIENTE','PAGADA','ANULADA') NOT NULL DEFAULT 'PENDIENTE',
    observaciones    TEXT,
    CONSTRAINT pk_ventas        PRIMARY KEY (id),
    CONSTRAINT fk_venta_cot     FOREIGN KEY (id_cotizacion) REFERENCES cotizaciones(id),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente)    REFERENCES clientes(id),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario)    REFERENCES usuarios(id),
    CONSTRAINT chk_total_venta  CHECK (total >= 0)
) ENGINE=InnoDB COMMENT='Ventas registradas';

-- ------------------------------------------------------------
-- Tabla: detalle_venta
-- Ítems (productos) incluidos en cada venta
-- ------------------------------------------------------------
CREATE TABLE detalle_venta (
    id                          INT             NOT NULL AUTO_INCREMENT,
    id_venta                    INT             NOT NULL,
    id_producto                 INT             NOT NULL,
    cantidad                    INT             NOT NULL,
    precio_unitario             DECIMAL(10,2)   NOT NULL,
    descripcion_personalizacion TEXT,
    subtotal                    DECIMAL(12,2)   NOT NULL,
    CONSTRAINT pk_det_venta         PRIMARY KEY (id),
    CONSTRAINT fk_det_venta_venta   FOREIGN KEY (id_venta)    REFERENCES ventas(id),
    CONSTRAINT fk_det_venta_prod    FOREIGN KEY (id_producto) REFERENCES productos(id),
    CONSTRAINT chk_cantidad_det_v   CHECK (cantidad > 0)
) ENGINE=InnoDB COMMENT='Detalle de ítems en ventas';


-- ============================================================
-- PASO 3: Creación de índices adicionales para optimizar consultas
-- ============================================================
CREATE INDEX idx_usuarios_email         ON usuarios(email);
CREATE INDEX idx_clientes_documento     ON clientes(numero_documento);
CREATE INDEX idx_clientes_nombre        ON clientes(nombre, apellido);
CREATE INDEX idx_cotizaciones_numero    ON cotizaciones(numero_cotizacion);
CREATE INDEX idx_cotizaciones_estado    ON cotizaciones(estado);
CREATE INDEX idx_cotizaciones_cliente   ON cotizaciones(id_cliente);
CREATE INDEX idx_ventas_numero          ON ventas(numero_factura);
CREATE INDEX idx_ventas_estado          ON ventas(estado);
CREATE INDEX idx_ventas_cliente         ON ventas(id_cliente);
CREATE INDEX idx_movimientos_insumo     ON movimientos_insumo(id_insumo, fecha);


-- ============================================================
-- PASO 4: Inserción de datos de ejemplo (datos semilla)
-- ============================================================

-- ----------------------------------------
-- Roles del sistema
-- ----------------------------------------
INSERT INTO roles (nombre, descripcion) VALUES
('ADMINISTRADOR', 'Acceso total al sistema: usuarios, productos, inventario, cotizaciones, ventas y reportes.'),
('VENDEDOR',      'Acceso a clientes, catálogo, cotizaciones y ventas. Sin acceso a gestión de usuarios ni configuraciones.');

-- ----------------------------------------
-- Usuarios del sistema
-- NOTA IMPORTANTE: Las contraseñas están cifradas con BCrypt.
-- Contraseña de TODOS los usuarios de prueba: Edimile2024!
-- El hash BCrypt debe generarse desde la aplicación con:
--   new BCryptPasswordEncoder().encode("Edimile2024!")
-- El hash de ejemplo mostrado es SOLO ILUSTRATIVO — no usar en producción.
-- ----------------------------------------
INSERT INTO usuarios (nombre, apellido, email, password, id_rol) VALUES
('Carlos',  'Rodríguez',  'admin@edimile.com',      '$2a$10$xJwL5v18XjIiWhHnEBRXBOo1VpU6GqkK5T0F8HhAaP7XNzHn8Pcy', 1),
('Ana',     'Martínez',   'ana.martinez@edimile.com','$2a$10$xJwL5v18XjIiWhHnEBRXBOo1VpU6GqkK5T0F8HhAaP7XNzHn8Pcy', 2),
('Luis',    'Gómez',      'luis.gomez@edimile.com',  '$2a$10$xJwL5v18XjIiWhHnEBRXBOo1VpU6GqkK5T0F8HhAaP7XNzHn8Pcy', 2);

-- ----------------------------------------
-- Clientes
-- ----------------------------------------
INSERT INTO clientes (nombre, apellido, empresa, tipo_documento, numero_documento, telefono, email, direccion) VALUES
('María',    'Pérez',     NULL,                        'CC',        '1020304050', '3101234567', 'maria.perez@gmail.com',     'Cra 5 # 10-20, Bogotá'),
('Juan',     'Torres',    'Empresa ABC S.A.S.',        'NIT',       '900123456-1','3209876543', 'compras@empresaabc.com',    'Calle 80 # 45-12, Medellín'),
('Claudia',  'Sánchez',   NULL,                        'CC',        '5060708090', '3153456789', 'claudia.s@hotmail.com',     'Av. 30 # 22-11, Cali'),
('Roberto',  'Morales',   'AutoLujo Pereira Ltda.',    'NIT',       '800987654-2','3001122334', 'rmorales@autolujo.com',     'Av. Circunvalar # 5-30, Pereira'),
('Sandra',   'López',     'Uniformes Industriales SAS','NIT',       '901234567-3','3155544332', 'sandra.lopez@uniformes.co', 'Cra 15 # 68-30, Barranquilla');

-- ----------------------------------------
-- Categorías de productos
-- ----------------------------------------
INSERT INTO categorias (nombre, descripcion) VALUES
('Camisetas y Polos',          'Camisetas, polos y prendas de vestir con estampados y sublimados'),
('Gorras y Sombreros',         'Gorras planas, gorras tipo trucker, sombreros personalizados'),
('Dotaciones Empresariales',   'Uniformes, chalecos, chaquetas y prendas corporativas'),
('Accesorios Automotrices',    'Lujos, tapetes, fundas, stickers y accesorios decorativos para vehículos'),
('Diseños Personalizados',     'Artículos varios con diseño a medida del cliente'),
('Maletines y Bolsos',         'Maletines corporativos, tulas y bolsos personalizados');

-- ----------------------------------------
-- Productos
-- ----------------------------------------
INSERT INTO productos (nombre, descripcion, id_categoria, precio_base, imagen_url) VALUES
('Camiseta Cuello Redondo Sublimada',   'Camiseta 100% poliéster con sublimación full color en frente y espalda. Tallas S a XL.', 1, 35000.00,  '/images/camiseta-sublimada.jpg'),
('Camiseta Tipo Polo Bordada',          'Camiseta polo con bordado en pecho izquierdo. Tela piqué 180 g. Tallas S a XXL.',           1, 45000.00,  '/images/polo-bordado.jpg'),
('Camiseta Algodón Estampada',          'Camiseta 100% algodón con estampado en vinilo textil o serigrafía.',                         1, 28000.00,  '/images/camiseta-algodon.jpg'),
('Gorra Trucker Bordada',               'Gorra trucker con malla trasera, frente rígido, bordado personalizado en relieve.',          2, 25000.00,  '/images/gorra-trucker.jpg'),
('Gorra Plana Full Color',              'Gorra plana con panel sublimado full color, talla única ajustable.',                         2, 30000.00,  '/images/gorra-full.jpg'),
('Chaleco Corporativo Reflectivo',      'Chaleco de seguridad con franjas reflectivas y logo bordado. Colores: amarillo, naranja.',   3, 55000.00,  '/images/chaleco.jpg'),
('Chaqueta Softshell Empresarial',      'Chaqueta tipo softshell con cierre YKK, logo bordado en pecho.',                            3, 120000.00, '/images/chaqueta.jpg'),
('Tapete Personalizado Vehículo',       'Tapete para piso de vehículo en material caucho o carpet, con logo o diseño grabado.',       4, 80000.00,  '/images/tapete-auto.jpg'),
('Sticker Vinil Cortado Vehículo',      'Sticker en vinil de corte de alta durabilidad, resistente a UV y agua. Medidas variables.', 4, 15000.00,  '/images/sticker-auto.jpg'),
('Maletín Corporativo Estampado',       'Maletín en tela Oxford 600D con impresión en serigrafía o sublimación.',                    6, 65000.00,  '/images/maletin.jpg');

-- ----------------------------------------
-- Insumos (materias primas)
-- ----------------------------------------
INSERT INTO insumos (nombre, descripcion, unidad_medida, stock_actual, stock_minimo, precio_unitario) VALUES
('Tela Poliéster Sublimación',          'Tela 100% poliéster para sublimación, blanco hueso. Rollo de 1.50m de ancho.',  'Metro',     150.00,  30.00,  8500.00),
('Tela Algodón Peinado 180g',           'Tela algodón peinado, 180g/m². Blanca y colores surtidos.',                      'Metro',      80.00,  20.00, 12000.00),
('Tinta Sublimación (set 4 colores)',   'Set de tintas Epson/Inktec para sublimación CMYK. Frasco 250ml c/u.',             'Set',         5.00,   2.00, 95000.00),
('Vinilo Textil HTV Colores',           'Vinilo de transferencia térmica para telas. Rollo 50cm x 25mt.',                  'Rollo',       8.00,   3.00, 75000.00),
('Papel Sublimación 100g A4',           'Papel especial para sublimación, 100g, tamaño A4. Paquete x 100 hojas.',          'Paquete',    20.00,   5.00, 18000.00),
('Hilo de Bordar (colores surtidos)',   'Hilo 100% poliéster para bordado en maquinaria industrial. Cono 3000m.',          'Cono',       30.00,  10.00,  4500.00),
('Tela Piqué Polo 200g',                'Tela piqué 200g/m² para camisetas tipo polo. Colores surtidos.',                  'Metro',      60.00,  15.00, 14000.00),
('Gorra Trucker Blanca (base)',         'Gorra trucker sin diseño para personalizar. Frente blanco, malla negra.',         'Unidad',    100.00,  20.00,  8000.00),
('Gorra Plana Sublimable (base)',       'Gorra plana todo-panel en tela sublimable blanca. Talla única.',                  'Unidad',     80.00,  15.00,  9500.00),
('Caucho para Tapetes Automotrices',    'Caucho microporoso 4mm para fabricación de tapetes. Medidas 1.20x2.50m.',         'Plancha',    15.00,   5.00, 45000.00),
('Vinil de Corte Adhesivo',             'Vinil adhesivo para corte en plotter. Rollo 60cm x 30mt. Colores surtidos.',      'Rollo',      12.00,   4.00, 55000.00),
('Tela Oxford 600D',                    'Tela Oxford 600 Denier impermeable para maletines y bolsos. Ancho 1.50m.',        'Metro',      40.00,  10.00, 11500.00);

-- ----------------------------------------
-- Movimientos de insumos (entradas iniciales de inventario)
-- ----------------------------------------
INSERT INTO movimientos_insumo (id_insumo, tipo_movimiento, cantidad, descripcion, id_usuario) VALUES
(1,  'ENTRADA', 150.00, 'Inventario inicial — compra proveedor Textiles del Norte', 1),
(2,  'ENTRADA',  80.00, 'Inventario inicial — compra proveedor Textiles del Norte', 1),
(3,  'ENTRADA',   5.00, 'Inventario inicial — compra tintas importadas', 1),
(4,  'ENTRADA',   8.00, 'Inventario inicial — compra vinil HTV', 1),
(5,  'ENTRADA',  20.00, 'Inventario inicial — compra papelería', 1),
(6,  'ENTRADA',  30.00, 'Inventario inicial — compra hilos bordado', 1),
(7,  'ENTRADA',  60.00, 'Inventario inicial — compra tela piqué', 1),
(8,  'ENTRADA', 100.00, 'Inventario inicial — compra gorras base', 1),
(9,  'ENTRADA',  80.00, 'Inventario inicial — compra gorras planas', 1),
(10, 'ENTRADA',  15.00, 'Inventario inicial — compra caucho automotriz', 1),
(11, 'ENTRADA',  12.00, 'Inventario inicial — compra vinil de corte', 1),
(12, 'ENTRADA',  40.00, 'Inventario inicial — compra tela oxford', 1);

-- ----------------------------------------
-- Cotizaciones de ejemplo
-- ----------------------------------------
INSERT INTO cotizaciones (numero_cotizacion, id_cliente, id_usuario, fecha_vencimiento, estado, subtotal, descuento_pct, iva_pct, total, observaciones)
VALUES
('COT-0001', 2, 2, '2024-02-15', 'APROBADA',  335000.00, 5.00, 19.00, 377162.50,  'Pedido urgente para evento corporativo. Entrega en 8 días hábiles.'),
('COT-0002', 4, 2, '2024-02-20', 'PENDIENTE', 160000.00, 0.00, 19.00, 190400.00,  'Tapetes para 2 vehículos, diseño con logo de la empresa.'),
('COT-0003', 1, 3, '2024-03-01', 'RECHAZADA', 112000.00, 0.00, 19.00, 133280.00,  'Cliente solicitó ajuste en precios. Se enviará nueva cotización.');

-- ----------------------------------------
-- Detalle de cotizaciones
-- ----------------------------------------
INSERT INTO detalle_cotizacion (id_cotizacion, id_producto, cantidad, precio_unitario, descripcion_personalizacion, subtotal) VALUES
-- COT-0001: 5 polos y 5 chalecos para Empresa ABC
(1, 2, 5,  45000.00, 'Bordado logo Empresa ABC en pecho izquierdo, color azul rey', 225000.00),
(1, 6, 2,  55000.00, 'Logo bordado Empresa ABC, talla M y XL, fondo amarillo',       110000.00),
-- COT-0002: 2 tapetes para AutoLujo
(2, 8, 2,  80000.00, 'Logo AutoLujo Pereira, fondo negro, letras doradas',           160000.00),
-- COT-0003: 4 camisetas para María Pérez
(3, 1, 4,  35000.00, 'Nombre personalizado espalda + diseño floral sublimado',       140000.00);

-- ----------------------------------------
-- Venta generada a partir de la cotización aprobada COT-0001
-- ----------------------------------------
INSERT INTO ventas (numero_factura, id_cotizacion, id_cliente, id_usuario, subtotal, descuento_pct, iva_pct, total, metodo_pago, estado, observaciones)
VALUES
('FAC-0001', 1, 2, 2, 335000.00, 5.00, 19.00, 377162.50, 'TRANSFERENCIA', 'PAGADA',   'Transferencia bancaria recibida. Pedido entregado el 2024-02-20.');

-- Detalle de la venta FAC-0001 (mismo que la cotización COT-0001)
INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, descripcion_personalizacion, subtotal) VALUES
(1, 2, 5, 45000.00, 'Bordado logo Empresa ABC en pecho izquierdo, color azul rey', 225000.00),
(1, 6, 2, 55000.00, 'Logo bordado Empresa ABC, talla M y XL, fondo amarillo',      110000.00);

-- Venta directa sin cotización previa
INSERT INTO ventas (numero_factura, id_cotizacion, id_cliente, id_usuario, subtotal, descuento_pct, iva_pct, total, metodo_pago, estado, observaciones)
VALUES
('FAC-0002', NULL, 3, 3, 56000.00, 0.00, 19.00, 66640.00, 'EFECTIVO', 'PAGADA', 'Venta directa en local. Pago en efectivo.');

INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, descripcion_personalizacion, subtotal) VALUES
(2, 4, 2, 25000.00, 'Bordado nombre propio en frente gorra, hilo dorado', 50000.00),
(2, 9, 1, 15000.00, 'Sticker nombre vehículo, vinil blanco',               15000.00);

-- Actualizar stock de insumos por la producción de FAC-0001 y FAC-0002 (salidas)
INSERT INTO movimientos_insumo (id_insumo, tipo_movimiento, cantidad, descripcion, id_usuario) VALUES
(7,  'SALIDA',  3.50, 'Producción FAC-0001: 5 polos — consumo tela piqué',    2),
(6,  'SALIDA',  2.00, 'Producción FAC-0001: bordado logo 7 unidades',          2),
(6,  'SALIDA',  1.00, 'Producción FAC-0002: bordado gorras 2 unidades',        3),
(8,  'SALIDA',  2.00, 'Producción FAC-0002: gorras trucker personalizadas',    3),
(11, 'SALIDA',  0.50, 'Producción FAC-0002: sticker vehículo',                 3);


-- ============================================================
-- PASO 5: Verificación de la estructura creada
-- ============================================================
SELECT 'BASE DE DATOS CREADA EXITOSAMENTE' AS mensaje;

SELECT
    TABLE_NAME    AS 'Tabla',
    TABLE_ROWS    AS 'Filas aprox.',
    TABLE_COMMENT AS 'Descripción'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'creaciones_edimile'
ORDER BY TABLE_NAME;
