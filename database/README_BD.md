# Base de datos PowerFit

Esta carpeta reemplaza el script antiguo acumulado.

## Orden recomendado para una instalación limpia

1. `00_crear_bd.sql`
2. `01_estructura.sql`
3. `02_datos_iniciales.sql`
4. `03_datos_demo.sql`
5. `04_consultas_verificacion.sql` (opcional)

Si existe una base antigua y no hay datos que conservar:

1. ejecutar `99_reset_bd.sql`
2. repetir el orden anterior.

## Sobre `genero`

`genero` pertenece a `clientes`, no a `usuarios` ni a `seguimiento_fisico`.

- `usuarios` contiene credenciales, rol y estado de acceso.
- `clientes` contiene información personal/demográfica.
- `seguimiento_fisico` contiene mediciones que cambian en el tiempo.

La estructura define `genero` como `VARCHAR(20) NOT NULL`, ya que el campo está integrado en el formulario de registro, en la entidad `Cliente` y en la edición del perfil.

Valores permitidos actualmente:

- `MASCULINO`
- `FEMENINO`

## Importante

`01_estructura.sql` ya incluye directamente todos los cambios que antes aparecían como `ALTER TABLE`:

- `usuarios.cambio_password_pendiente`
- `clientes.foto_perfil`
- `clientes.genero`
- `membresias.fecha_solicitud`
- `membresias.observacion`
- `membresias.fecha_inicio` y `fecha_fin` permiten NULL
- `pedidos.estado_pago`
- datos de delivery/factura en `pedidos`
- `horarios_clase.sala`
- `productos.sku`
- `clases.imagen`

Por eso no se deben volver a ejecutar los ALTER del script antiguo después de crear la base nueva.
