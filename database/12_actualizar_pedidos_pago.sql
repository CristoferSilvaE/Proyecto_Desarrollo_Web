USE powerfit;

ALTER TABLE pedidos
  MODIFY COLUMN estado_pago VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
  ADD COLUMN fecha_pago DATETIME NULL AFTER estado_pago,
  ADD COLUMN observacion VARCHAR(255) NULL AFTER razon_social;