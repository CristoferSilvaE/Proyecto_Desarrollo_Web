-- ============================================================
-- POWERFIT - ACTUALIZACION DE MEMBRESIAS
-- TERCER AVANCE
-- ============================================================

USE powerfit;

-- Ejecutar una sola vez en bases de datos existentes

ALTER TABLE membresias
    ADD COLUMN duracion_meses INT NOT NULL DEFAULT 1,
    ADD COLUMN horario_preferido VARCHAR(20) NULL,
    ADD COLUMN fecha_inicio_deseada DATE NULL,
    ADD COLUMN metodo_pago_preferido VARCHAR(30) NULL;

-- Verificar los cambios
DESCRIBE membresias;