package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.DetallePedido;
import com.powerfit.powerfit.model.Pedido;
import com.powerfit.powerfit.model.Producto;
import com.powerfit.powerfit.model.Sede;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.DetallePedidoRepository;
import com.powerfit.powerfit.repository.PedidoRepository;
import com.powerfit.powerfit.repository.ProductoRepository;
import com.powerfit.powerfit.repository.SedeRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

  private final PedidoRepository pedidoRepository;
  private final DetallePedidoRepository detallePedidoRepository;
  private final ProductoRepository productoRepository;
  private final ClienteRepository clienteRepository;
  private final SedeRepository sedeRepository;

  public PedidoService(
      PedidoRepository pedidoRepository,
      DetallePedidoRepository detallePedidoRepository,
      ProductoRepository productoRepository,
      ClienteRepository clienteRepository,
      SedeRepository sedeRepository) {

    this.pedidoRepository = pedidoRepository;
    this.detallePedidoRepository = detallePedidoRepository;
    this.productoRepository = productoRepository;
    this.clienteRepository = clienteRepository;
    this.sedeRepository = sedeRepository;
  }

  @Transactional
  public Pedido registrarPedido(
      Long idCliente,
      List<CarritoService.ItemCarrito> items,
      String metodoEntrega,
      Long idSede,
      String direccionEntrega,
      String distrito,
      String referencia,
      String tipoComprobante,
      String ruc,
      String razonSocial,
      String metodoPago) {

    // VALIDAR CLIENTE
    Cliente cliente =
        clienteRepository
            .findById(idCliente)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));

    if (!"ACTIVO".equalsIgnoreCase(cliente.getEstado())) {
      throw new IllegalArgumentException("Tu cuenta no se encuentra activa.");
    }

    // VALIDAR CARRITO
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("Tu carrito está vacío.");
    }

    // VALIDAR MÉTODO DE ENTREGA
    if (!"RECOJO".equals(metodoEntrega) && !"DELIVERY".equals(metodoEntrega)) {

      throw new IllegalArgumentException("Selecciona un método de entrega válido.");
    }

    // VALIDAR COMPROBANTE
    if (!"BOLETA".equals(tipoComprobante) && !"FACTURA".equals(tipoComprobante)) {

      throw new IllegalArgumentException("Selecciona un comprobante válido.");
    }

    // VALIDAR MÉTODO DE PAGO
    if (!"TARJETA".equals(metodoPago)
        && !"YAPE_PLIN".equals(metodoPago)
        && !"TRANSFERENCIA".equals(metodoPago)) {

      throw new IllegalArgumentException("Selecciona un método de pago válido.");
    }

    // CREAR PEDIDO
    Pedido pedido = new Pedido();

    pedido.setCliente(cliente);
    pedido.setMetodoEntrega(metodoEntrega);
    pedido.setTipoComprobante(tipoComprobante);
    pedido.setMetodoPago(metodoPago);

    pedido.setEstado("PENDIENTE");
    pedido.setEstadoPago("PENDIENTE");

    // VALIDAR ENTREGA
    if ("RECOJO".equals(metodoEntrega)) {

      if (idSede == null) {
        throw new IllegalArgumentException("Selecciona una sede de recojo.");
      }

      Sede sede =
          sedeRepository
              .findById(idSede)
              .orElseThrow(() -> new IllegalArgumentException("La sede seleccionada no existe."));

      if (!"ACTIVO".equals(sede.getEstado())) {
        throw new IllegalArgumentException("La sede seleccionada no está disponible.");
      }

      pedido.setSede(sede);
      pedido.setDireccionEntrega(null);
      pedido.setDistrito(null);
      pedido.setReferencia(null);

    } else {

      if (direccionEntrega == null
          || direccionEntrega.isBlank()
          || distrito == null
          || distrito.isBlank()) {

        throw new IllegalArgumentException("Ingresa la dirección y el distrito de entrega.");
      }

      if (direccionEntrega.trim().length() > 200
          || distrito.trim().length() > 80
          || (referencia != null && referencia.trim().length() > 200)) {

        throw new IllegalArgumentException("Los datos de entrega superan la longitud permitida.");
      }

      pedido.setSede(null);
      pedido.setDireccionEntrega(direccionEntrega.trim());
      pedido.setDistrito(distrito.trim());

      pedido.setReferencia(referencia == null || referencia.isBlank() ? null : referencia.trim());
    }

    // VALIDAR FACTURA
    if ("FACTURA".equals(tipoComprobante)) {

      if (ruc == null || !ruc.matches("\\d{11}")) {
        throw new IllegalArgumentException("El RUC debe contener 11 dígitos.");
      }

      if (razonSocial == null || razonSocial.isBlank() || razonSocial.trim().length() > 150) {

        throw new IllegalArgumentException("Ingresa una razón social válida.");
      }

      pedido.setRuc(ruc);
      pedido.setRazonSocial(razonSocial.trim());

    } else {
      pedido.setRuc(null);
      pedido.setRazonSocial(null);
    }

    // VALIDAR PRODUCTOS Y CALCULAR SUBTOTAL
    BigDecimal subtotal = BigDecimal.ZERO;

    List<DetallePedido> detalles = new ArrayList<>();

    // Orden fijo para reducir conflictos entre compras simultáneas
    List<CarritoService.ItemCarrito> itemsOrdenados = new ArrayList<>(items);

    itemsOrdenados.sort(Comparator.comparing(item -> item.getProducto().getIdProducto()));

    for (CarritoService.ItemCarrito item : itemsOrdenados) {

      Long idProducto = item.getProducto().getIdProducto();
      int cantidad = item.getCantidad();

      if (cantidad <= 0) {
        throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
      }

      // Consultar y bloquear el producto en MySQL
      Producto producto =
          productoRepository
              .buscarParaCompra(idProducto)
              .orElseThrow(
                  () -> new IllegalArgumentException("Uno de los productos ya no existe."));

      if (!"ACTIVO".equals(producto.getEstado())) {
        throw new IllegalArgumentException(producto.getNombre() + " ya no está disponible.");
      }

      if (producto.getStock() == null || producto.getStock() < cantidad) {

        throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre());
      }

      BigDecimal precio = producto.getPrecio();

      BigDecimal subtotalProducto = precio.multiply(BigDecimal.valueOf(cantidad));

      // Descontar stock
      producto.setStock(producto.getStock() - cantidad);

      productoRepository.save(producto);

      // Crear detalle
      DetallePedido detalle = new DetallePedido();

      detalle.setPedido(pedido);
      detalle.setProducto(producto);
      detalle.setCantidad(cantidad);
      detalle.setPrecioUnitario(precio);
      detalle.setSubtotal(subtotalProducto);

      detalles.add(detalle);

      subtotal = subtotal.add(subtotalProducto);
    }

    // COSTO DE ENVÍO
    // Por ahora el delivery tendrá costo S/ 0.00.
    // Posteriormente podremos definir una tarifa real.
    BigDecimal costoEnvio = BigDecimal.ZERO;

    pedido.setSubtotal(subtotal);
    pedido.setCostoEnvio(costoEnvio);
    pedido.setTotal(subtotal.add(costoEnvio));

    // GUARDAR PEDIDO
    Pedido pedidoGuardado = pedidoRepository.save(pedido);

    // GUARDAR DETALLES
    detallePedidoRepository.saveAll(detalles);

    return pedidoGuardado;
  }
}
