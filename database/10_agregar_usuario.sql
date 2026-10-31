-- POWERFIT - TERCER AVANCE
-- Agregar nombre de usuario para el inicio de sesión

USE powerfit;

ALTER TABLE usuarios
ADD COLUMN usuario VARCHAR(30) NULL AFTER correo,
ADD CONSTRAINT uk_usuarios_usuario UNIQUE (usuario);

-- Verificar cambios
SHOW COLUMNS FROM usuarios;
SHOW INDEX FROM usuarios;