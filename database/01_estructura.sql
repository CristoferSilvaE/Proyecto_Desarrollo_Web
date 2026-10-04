-- ============================================================
-- POWERFIT - ESTRUCTURA FINAL DE LA BASE DE DATOS
-- Contiene la estructura consolidada.
-- No necesita ejecutar ALTER TABLE posteriores.
-- ============================================================

USE powerfit;

-- ============================================================
-- 1. ROLES
-- ============================================================

CREATE TABLE roles (
    id_rol BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB;


-- ============================================================
-- 2. USUARIOS
-- Credenciales y control de acceso.
-- ============================================================

CREATE TABLE usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_rol BIGINT NOT NULL,
    correo VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cambio_password_pendiente BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;


-- ============================================================
-- 3. CLIENTES
-- Datos personales del cliente.
-- genero es obligatorio en el registro del cliente.
-- Valores previstos: MASCULINO / FEMENINO.
-- ============================================================

CREATE TABLE clientes (
    id_cliente BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    dni CHAR(8) NOT NULL UNIQUE,
    telefono VARCHAR(15),
    fecha_nacimiento DATE,
    genero VARCHAR(20) NOT NULL,
    foto_perfil VARCHAR(255) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',

    CONSTRAINT fk_cliente_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;


-- ============================================================
-- 4. SEGUIMIENTO_FISICO
-- Historial de mediciones físicas del cliente.
-- ============================================================

CREATE TABLE seguimiento_fisico (
    id_seguimiento BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    fecha_registro DATE NOT NULL,
    peso DECIMAL(5,2),
    altura DECIMAL(4,2),
    porcentaje_grasa DECIMAL(5,2),
    masa_muscular DECIMAL(5,2),
    observaciones VARCHAR(255),

    CONSTRAINT fk_seguimiento_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- 5. PLANES_MEMBRESIA
-- ============================================================

CREATE TABLE planes_membresia (
    id_plan BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    precio DECIMAL(8,2) NOT NULL,
    duracion_dias INT NOT NULL DEFAULT 30,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
) ENGINE=InnoDB;


-- ============================================================
-- 6. MEMBRESIAS
-- TABLA TRANSACCIONAL.
-- PENDIENTE: fecha_inicio y fecha_fin permanecen NULL.
-- ============================================================

CREATE TABLE membresias (
    id_membresia BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    id_plan BIGINT NOT NULL,
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio DATE NULL,
    fecha_fin DATE NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observacion VARCHAR(255) NULL,

    CONSTRAINT fk_membresia_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_membresia_plan
        FOREIGN KEY (id_plan)
        REFERENCES planes_membresia(id_plan)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;


-- ============================================================
-- 7. PAGOS_MEMBRESIA
-- ============================================================

CREATE TABLE pagos_membresia (
    id_pago BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_membresia BIGINT NOT NULL,
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto DECIMAL(8,2) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PAGADO',

    CONSTRAINT fk_pago_membresia
        FOREIGN KEY (id_membresia)
        REFERENCES membresias(id_membresia)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;


-- ============================================================
-- 8. INSTRUCTORES
-- ============================================================

CREATE TABLE instructores (
    id_instructor BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    especialidad VARCHAR(80),
    telefono VARCHAR(15),
    correo VARCHAR(120) UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
) ENGINE=InnoDB;


-- ============================================================
-- 9. CLASES
-- ============================================================

CREATE TABLE clases (
    id_clase BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(200),
    categoria VARCHAR(50),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    imagen VARCHAR(255) NULL
) ENGINE=InnoDB;


-- ============================================================
-- 10. HORARIOS_CLASE
-- ============================================================

CREATE TABLE horarios_clase (
    id_horario BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_clase BIGINT NOT NULL,
    id_instructor BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    capacidad INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    sala VARCHAR(80) NULL,

    CONSTRAINT fk_horario_clase
        FOREIGN KEY (id_clase)
        REFERENCES clases(id_clase)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_horario_instructor
        FOREIGN KEY (id_instructor)
        REFERENCES instructores(id_instructor)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;


-- ============================================================
-- 11. RESERVAS
-- TABLA TRANSACCIONAL.
-- ============================================================

CREATE TABLE reservas (
    id_reserva BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    id_horario BIGINT NOT NULL,
    fecha_reserva DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'RESERVADA',
    asistencia BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_reserva_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_reserva_horario
        FOREIGN KEY (id_horario)
        REFERENCES horarios_clase(id_horario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT uk_cliente_horario
        UNIQUE (id_cliente, id_horario)
) ENGINE=InnoDB;


-- ============================================================
-- 12. PRODUCTOS
-- ============================================================

CREATE TABLE productos (
    id_producto BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(30) NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255),
    categoria VARCHAR(50),
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 5,
    imagen VARCHAR(255),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
) ENGINE=InnoDB;


-- ============================================================
-- 13. PEDIDOS
-- TABLA TRANSACCIONAL.
-- ============================================================

CREATE TABLE pedidos (
    id_pedido BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    fecha_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metodo_entrega VARCHAR(30) NOT NULL,
    tipo_comprobante VARCHAR(20) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    costo_envio DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    estado_pago VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    direccion_entrega VARCHAR(200) NULL,
    distrito VARCHAR(80) NULL,
    referencia VARCHAR(200) NULL,
    ruc VARCHAR(11) NULL,
    razon_social VARCHAR(150) NULL,

    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;


-- ============================================================
-- 14. DETALLE_PEDIDO
-- ============================================================

CREATE TABLE detalle_pedido (
    id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pedido BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_detalle_pedido
        FOREIGN KEY (id_pedido)
        REFERENCES pedidos(id_pedido)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_detalle_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;
