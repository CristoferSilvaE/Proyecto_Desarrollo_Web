-- ============================================================
-- POWERFIT - DATOS DE DEMOSTRACIÓN
-- Datos útiles para mostrar la aplicación.
-- Las fechas se calculan respecto al día de ejecución.
-- ============================================================

USE powerfit;


-- ============================================================
-- HORARIOS DE CLASE
-- ============================================================

-- MUSCULACIÓN
INSERT INTO horarios_clase
(id_clase, id_instructor, fecha, hora_inicio, hora_fin, capacidad, estado, sala)
SELECT
    c.id_clase,
    i.id_instructor,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY),
    '07:00:00',
    '08:00:00',
    15,
    'ACTIVO',
    'Sala Fuerza'
FROM clases c
JOIN instructores i
    ON i.correo = 'carlos.medina@powerfit.pe'
WHERE c.nombre = 'Musculación'
AND NOT EXISTS (
    SELECT 1
    FROM horarios_clase h
    WHERE h.id_clase = c.id_clase
      AND h.fecha = DATE_ADD(CURDATE(), INTERVAL 1 DAY)
      AND h.hora_inicio = '07:00:00'
);


-- SPINNING
INSERT INTO horarios_clase
(id_clase, id_instructor, fecha, hora_inicio, hora_fin, capacidad, estado, sala)
SELECT
    c.id_clase,
    i.id_instructor,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY),
    '18:00:00',
    '19:00:00',
    20,
    'ACTIVO',
    'Sala Cycling'
FROM clases c
JOIN instructores i
    ON i.correo = 'valeria.ramos@powerfit.pe'
WHERE c.nombre = 'Spinning'
AND NOT EXISTS (
    SELECT 1
    FROM horarios_clase h
    WHERE h.id_clase = c.id_clase
      AND h.fecha = DATE_ADD(CURDATE(), INTERVAL 1 DAY)
      AND h.hora_inicio = '18:00:00'
);


-- FUNCIONAL
INSERT INTO horarios_clase
(id_clase, id_instructor, fecha, hora_inicio, hora_fin, capacidad, estado, sala)
SELECT
    c.id_clase,
    i.id_instructor,
    DATE_ADD(CURDATE(), INTERVAL 2 DAY),
    '19:00:00',
    '20:00:00',
    18,
    'ACTIVO',
    'Sala Funcional'
FROM clases c
JOIN instructores i
    ON i.correo = 'luis.torres@powerfit.pe'
WHERE c.nombre = 'Funcional'
AND NOT EXISTS (
    SELECT 1
    FROM horarios_clase h
    WHERE h.id_clase = c.id_clase
      AND h.fecha = DATE_ADD(CURDATE(), INTERVAL 2 DAY)
      AND h.hora_inicio = '19:00:00'
);


-- ZUMBA
INSERT INTO horarios_clase
(id_clase, id_instructor, fecha, hora_inicio, hora_fin, capacidad, estado, sala)
SELECT
    c.id_clase,
    i.id_instructor,
    DATE_ADD(CURDATE(), INTERVAL 3 DAY),
    '20:00:00',
    '21:00:00',
    20,
    'ACTIVO',
    'Sala Grupal'
FROM clases c
JOIN instructores i
    ON i.correo = 'andrea.flores@powerfit.pe'
WHERE c.nombre = 'Zumba'
AND NOT EXISTS (
    SELECT 1
    FROM horarios_clase h
    WHERE h.id_clase = c.id_clase
      AND h.fecha = DATE_ADD(CURDATE(), INTERVAL 3 DAY)
      AND h.hora_inicio = '20:00:00'
);
