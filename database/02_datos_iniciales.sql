-- ============================================================
-- POWERFIT - DATOS INICIALES
-- Catálogos y datos necesarios para ejecutar la aplicación.
-- Ejecutar después de 01_estructura.sql.
-- ============================================================

USE powerfit;


-- ============================================================
-- ROLES
-- ============================================================

INSERT INTO roles (nombre)
VALUES
('CLIENTE'),
('ADMINISTRADOR');


-- ============================================================
-- PLANES DE MEMBRESÍA
-- ============================================================

INSERT INTO planes_membresia
(nombre, descripcion, precio, duracion_dias, estado)
VALUES
('Básico', 'Plan de entrenamiento base', 59.00, 30, 'ACTIVO'),
('Premium', 'Plan de rendimiento avanzado', 89.00, 30, 'ACTIVO'),
('Elite', 'Plan de rendimiento máximo', 129.00, 30, 'ACTIVO');


-- ============================================================
-- CLASES
-- ============================================================

INSERT INTO clases
(nombre, descripcion, categoria, estado, imagen)
VALUES
(
    'Musculación',
    'Entrenamiento orientado al desarrollo de fuerza y masa muscular.',
    'Fuerza',
    'ACTIVO',
    'clase-musculacion.jpg'
),
(
    'Spinning',
    'Sesión cardiovascular de alta intensidad sobre bicicleta estacionaria.',
    'Cardio',
    'ACTIVO',
    'clase-spinning.jpg'
),
(
    'Funcional',
    'Entrenamiento de fuerza, movilidad y resistencia mediante movimientos funcionales.',
    'Funcional',
    'ACTIVO',
    'clase-funcional.jpg'
),
(
    'Zumba',
    'Actividad cardiovascular grupal basada en baile y coordinación.',
    'Cardio',
    'ACTIVO',
    'clase-zumba.jpg'
);


-- ============================================================
-- INSTRUCTORES
-- ============================================================

INSERT INTO instructores
(nombres, apellidos, especialidad, telefono, correo, estado)
VALUES
('Carlos', 'Medina', 'Musculación', '987654101', 'carlos.medina@powerfit.pe', 'ACTIVO'),
('Valeria', 'Ramos', 'Spinning', '987654102', 'valeria.ramos@powerfit.pe', 'ACTIVO'),
('Luis', 'Torres', 'Entrenamiento Funcional', '987654103', 'luis.torres@powerfit.pe', 'ACTIVO'),
('Andrea', 'Flores', 'Zumba', '987654104', 'andrea.flores@powerfit.pe', 'ACTIVO');


-- ============================================================
-- PRODUCTOS
-- ============================================================

INSERT INTO productos
(sku, nombre, descripcion, categoria, precio, stock, stock_minimo, imagen, estado)
VALUES
(
    'PF-NUT-001',
    'Premium Whey Protein 2kg',
    'Proteína whey para complementar la alimentación y recuperación deportiva.',
    'Nutrición Deportiva',
    169.00,
    20,
    5,
    'tienda-proteina-goldPro.jpg',
    'ACTIVO'
),
(
    'PF-NUT-002',
    'Hyperion Pre-Workout 300g',
    'Pre-entreno diseñado para acompañar sesiones de entrenamiento de alta intensidad.',
    'Nutrición Deportiva',
    145.00,
    15,
    5,
    'tienda-preEntreno-nitrox.jpg',
    'ACTIVO'
),
(
    'PF-NUT-003',
    'Multi-Vitamin Complex',
    'Complejo multivitamínico para complementar la alimentación diaria.',
    'Nutrición Deportiva',
    95.00,
    18,
    5,
    'tienda-vitaminaB.jpg',
    'ACTIVO'
),
(
    'PF-EQP-001',
    'Mancuernas Ajustables Pro',
    'Juego de mancuernas ajustables para entrenamiento de fuerza.',
    'Equipamiento',
    749.00,
    8,
    2,
    'tienda-mancuernas.jpg',
    'ACTIVO'
),
(
    'PF-EQP-002',
    'Banco de Entrenamiento Elite',
    'Banco ajustable para ejercicios de fuerza y entrenamiento funcional.',
    'Equipamiento',
    929.00,
    5,
    2,
    'tienda-banco.jpg',
    'ACTIVO'
),
(
    'PF-ACC-001',
    'Accesorios de Agarre',
    'Accesorios para mejorar el agarre durante ejercicios de fuerza.',
    'Accesorios',
    110.00,
    25,
    5,
    'tienda-agarre.jpg',
    'ACTIVO'
);
