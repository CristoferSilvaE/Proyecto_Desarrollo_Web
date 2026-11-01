-- ============================================================
-- POWERFIT - ACTUALIZACIÓN DE BASE DE DATOS
-- SCRIPT 07: CREACIÓN DE SEDES
-- ============================================================
-- Este script se ejecuta sobre una base de datos existente.
-- No elimina tablas ni registros anteriores.
-- Ejecutar una sola vez.
-- ============================================================

USE powerfit;

-- 1. CREAR TABLA DE SEDES

CREATE TABLE sedes (
    id_sede BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    distrito VARCHAR(80) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
) ENGINE=InnoDB;


-- 2. REGISTRAR SEDES INICIALES

INSERT INTO sedes (nombre, direccion, distrito, estado)
VALUES
('PowerFit Comas', 'Av. Universitaria 1200', 'Comas', 'ACTIVO'),
('PowerFit Los Olivos', 'Av. Carlos Izaguirre 850', 'Los Olivos', 'ACTIVO'),
('PowerFit Independencia', 'Av. Túpac Amaru 1500', 'Independencia', 'ACTIVO');


-- 3. AGREGAR SEDE A LA TABLA PEDIDOS

ALTER TABLE pedidos
ADD COLUMN id_sede BIGINT NULL,
ADD CONSTRAINT fk_pedido_sede
    FOREIGN KEY (id_sede)
    REFERENCES sedes(id_sede);


-- 4. VERIFICAR RESULTADOS

SELECT * FROM sedes;

SHOW COLUMNS FROM pedidos LIKE 'id_sede';