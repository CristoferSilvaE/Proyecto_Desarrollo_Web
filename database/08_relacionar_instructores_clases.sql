-- ============================================================
-- POWERFIT - MIGRACIÓN 08
-- RELACIONAR INSTRUCTORES CON CLASES
-- ============================================================

USE powerfit;

-- 1. Agregar la clave foránea como columna inicialmente nullable
ALTER TABLE instructores
ADD COLUMN id_clase BIGINT NULL AFTER especialidad;


USE powerfit;

-- Desactivar temporalmente la protección
SET SQL_SAFE_UPDATES = 0;

-- Relacionar especialidades que coinciden con el nombre
UPDATE instructores i
JOIN clases c
    ON LOWER(TRIM(i.especialidad)) = LOWER(TRIM(c.nombre))
SET i.id_clase = c.id_clase
WHERE i.id_clase IS NULL;

-- Equivalencia para Entrenamiento Funcional
UPDATE instructores i
JOIN clases c
    ON LOWER(TRIM(c.nombre)) = 'funcional'
SET i.id_clase = c.id_clase
WHERE i.id_clase IS NULL
AND LOWER(i.especialidad) LIKE '%funcional%';

-- Crear la clave foránea
ALTER TABLE instructores
ADD CONSTRAINT fk_instructor_clase
FOREIGN KEY (id_clase)
REFERENCES clases(id_clase)
ON UPDATE CASCADE
ON DELETE RESTRICT;

-- Reactivar protección
SET SQL_SAFE_UPDATES = 1;

-- Comprobar resultados
SELECT
    i.id_instructor,
    i.nombres,
    i.apellidos,
    i.especialidad AS especialidad_anterior,
    c.nombre AS clase_asignada,
    i.id_clase
FROM instructores i
LEFT JOIN clases c ON i.id_clase = c.id_clase
ORDER BY i.id_instructor;

-- Detectar instructores sin especialidad asignada
SELECT *
FROM instructores
WHERE id_clase IS NULL;
