-- ============================================================
-- POWERFIT - MIGRACION 11
-- REGISTRO DE PAGOS Y BENEFICIOS DE CLASES
-- ============================================================

USE powerfit;

-- Registrar pagos de clases adicionales
-- y beneficios gratuitos de membresias.


CREATE TABLE IF NOT EXISTS pagos_clases (

    id_pago_clase BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_reserva BIGINT NOT NULL,

-- Membresia que otorga el beneficio.
-- NULL cuando el cliente no tiene membresia.
id_membresia BIGINT NULL,

-- GRATUITA o PAGO
tipo_reserva VARCHAR(20) NOT NULL,

-- Importe de la operacion.
-- Las clases gratuitas registran 0.00.
monto DECIMAL(10, 2) NOT NULL DEFAULT 0.00,

-- BENEFICIO_PLAN, TARJETA, YAPE, PLIN o EFECTIVO
metodo_pago VARCHAR(30) NOT NULL,

-- BONIFICADO, PENDIENTE, PAGADO,
-- RECHAZADO o CANCELADO
estado_pago VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',

    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_pago DATETIME NULL,

    observacion VARCHAR(255) NULL,

    INDEX idx_pagos_clases_reserva (id_reserva),

    INDEX idx_pagos_clases_membresia (id_membresia),

    INDEX idx_pagos_clases_estado (estado_pago),

    CONSTRAINT fk_pago_clase_reserva
        FOREIGN KEY (id_reserva)
        REFERENCES reservas(id_reserva)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_pago_clase_membresia
        FOREIGN KEY (id_membresia)
        REFERENCES membresias(id_membresia)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- VERIFICACION
-- ============================================================

DESCRIBE pagos_clases;

SELECT * FROM pagos_clases;