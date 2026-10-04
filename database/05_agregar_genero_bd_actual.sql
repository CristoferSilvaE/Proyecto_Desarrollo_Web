-- ============================================================
-- POWERFIT - MIGRACIÓN SOLO PARA LA BD ACTUAL
-- NO ejecutar si la base se creó usando 01_estructura.sql,
-- porque esa estructura ya incluye la columna genero.
-- ============================================================

USE powerfit;

ALTER TABLE clientes
ADD COLUMN genero VARCHAR(20) NULL
AFTER fecha_nacimiento;

DESCRIBE clientes;
