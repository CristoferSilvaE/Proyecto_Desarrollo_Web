-- ============================================================
-- POWERFIT - CONSULTAS DE VERIFICACIÓN
-- No modifica datos.
-- ============================================================

USE powerfit;

SHOW TABLES;


-- ESTRUCTURA
DESCRIBE usuarios;
DESCRIBE clientes;
DESCRIBE membresias;
DESCRIBE pedidos;


-- CATÁLOGOS
SELECT * FROM roles;
SELECT * FROM planes_membresia;

SELECT
    id_clase,
    nombre,
    categoria,
    estado,
    imagen
FROM clases;

SELECT
    id_instructor,
    nombres,
    apellidos,
    especialidad,
    estado
FROM instructores;

SELECT
    id_producto,
    sku,
    nombre,
    categoria,
    precio,
    stock,
    stock_minimo,
    imagen,
    estado
FROM productos;


-- HORARIOS
SELECT
    h.id_horario,
    c.nombre AS clase,
    c.categoria,
    CONCAT(i.nombres, ' ', i.apellidos) AS instructor,
    h.fecha,
    h.hora_inicio,
    h.hora_fin,
    h.capacidad,
    h.sala,
    h.estado
FROM horarios_clase h
JOIN clases c
    ON c.id_clase = h.id_clase
JOIN instructores i
    ON i.id_instructor = h.id_instructor
ORDER BY h.fecha, h.hora_inicio;


-- USUARIOS / CLIENTES
SELECT
    u.id_usuario,
    r.nombre AS rol,
    u.correo,
    u.estado,
    u.fecha_creacion,
    u.cambio_password_pendiente
FROM usuarios u
JOIN roles r
    ON r.id_rol = u.id_rol
ORDER BY u.id_usuario;

SELECT
    c.id_cliente,
    c.nombres,
    c.apellidos,
    c.dni,
    c.telefono,
    c.fecha_nacimiento,
    c.genero,
    c.foto_perfil,
    c.estado,
    u.correo
FROM clientes c
JOIN usuarios u
    ON u.id_usuario = c.id_usuario
ORDER BY c.id_cliente;


-- MEMBRESÍAS
SELECT
    m.id_membresia,
    CONCAT(c.nombres, ' ', c.apellidos) AS cliente,
    p.nombre AS plan,
    m.fecha_solicitud,
    m.fecha_inicio,
    m.fecha_fin,
    m.estado,
    m.observacion
FROM membresias m
JOIN clientes c
    ON c.id_cliente = m.id_cliente
JOIN planes_membresia p
    ON p.id_plan = m.id_plan
ORDER BY m.fecha_solicitud DESC;


-- PEDIDOS
SELECT
    p.id_pedido,
    p.id_cliente,
    p.fecha_pedido,
    p.metodo_entrega,
    p.tipo_comprobante,
    p.metodo_pago,
    p.subtotal,
    p.costo_envio,
    p.total,
    p.estado,
    p.estado_pago
FROM pedidos p
ORDER BY p.fecha_pedido DESC;
