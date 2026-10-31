-- ============================================================
-- POWERFIT - MIGRACIÓN 09
-- ELIMINAR COLUMNA ESPECIALIDAD DE INSTRUCTORES
-- ============================================================

USE powerfit;

-- 1. Comprobar que todos los instructores
-- tengan una clase asignada.

SELECT
    id_instructor,
    nombres,
    apellidos,
    id_clase
FROM instructores
WHERE id_clase IS NULL;

-- IMPORTANTE:
-- Continuar solamente si la consulta anterior
-- no devuelve registros.

-- 2. Hacer obligatoria la relación con clases.

ALTER TABLE instructores
MODIFY COLUMN id_clase BIGINT NOT NULL;

-- 3. Eliminar la columna antigua.

ALTER TABLE instructores
DROP COLUMN especialidad;

-- 4. Verificar el resultado.

DESCRIBE instructores;

SELECT
    i.id_instructor,
    i.nombres,
    i.apellidos,
    c.nombre AS clase_asignada,
    i.telefono,
    i.correo,
    i.estado
FROM instructores i
INNER JOIN clases c
    ON c.id_clase = i.id_clase;