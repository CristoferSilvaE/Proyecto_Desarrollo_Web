package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.DetallePedido;
import com.powerfit.powerfit.model.Pedido;
import com.powerfit.powerfit.model.Producto;
import com.powerfit.powerfit.model.Sede;
import com.powerfit.powerfit.repository.DetallePedidoRepository;
import com.powerfit.powerfit.repository.PedidoRepository;
import com.powerfit.powerfit.repository.ProductoRepository;
import com.powerfit.powerfit.repository.SedeRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestionPedidoService {

  private final PedidoRepository pedidoRepository;
  private final DetallePedidoRepository detalleRepository;
  private final ProductoRepository productoRepository;
  private final SedeRepository sedeRepository;

  public GestionPedidoService(
      PedidoRepository pedidoRepository,
      DetallePedidoRepository detalleRepository,
      ProductoRepository productoRepository,
      SedeRepository sedeRepository) {

    this.pedidoRepository = pedidoRepository;
    this.detalleRepository = detalleRepository;
    this.productoRepository = productoRepository;
    this.sedeRepository = sedeRepository;
  }

  private Pedido obtenerPedido(Long idPedido, Long idCliente) {

    Pedido pedido =
        pedidoRepository
            .buscarParaModificar(idPedido)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));

    if (!pedido.getCliente().getIdCliente().equals(idCliente)) {
      throw new IllegalArgumentException("Pedido no encontrado.");
    }

    return pedido;
  }

  private void validarPendiente(Pedido pedido) {

    if (!"PENDIENTE".equals(pedido.getEstado()) || !"PENDIENTE".equals(pedido.getEstadoPago())) {

      throw new IllegalArgumentException("Este pedido ya no puede modificarse.");
    }
  }

  @Transactional(readOnly = true)
  public Map<String, Object> obtenerDetalle(Long idPedido, Long idCliente) {

    Pedido pedido =
        pedidoRepository
            .findById(idPedido)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));

    if (!pedido.getCliente().getIdCliente().equals(idCliente)) {
      throw new IllegalArgumentException("Pedido no encontrado.");
    }

    List<DetallePedido> detalles = detalleRepository.buscarDetalles(idPedido);

    List<Map<String, Object>> productos = new ArrayList<>();

    for (DetallePedido detalle : detalles) {

      Map<String, Object> producto = new LinkedHashMap<>();

      producto.put("idProducto", detalle.getProducto().getIdProducto());

      producto.put("nombre", detalle.getProducto().getNombre());

      producto.put("cantidad", detalle.getCantidad());
      producto.put("precio", detalle.getPrecioUnitario());
      producto.put("subtotal", detalle.getSubtotal());

      productos.add(producto);
    }

    Map<String, Object> resultado = new LinkedHashMap<>();

    resultado.put("idPedido", pedido.getIdPedido());
    resultado.put("estado", pedido.getEstado());
    resultado.put("estadoPago", pedido.getEstadoPago());
    resultado.put("metodoEntrega", pedido.getMetodoEntrega());
    resultado.put("metodoPago", pedido.getMetodoPago());
    resultado.put("tipoComprobante", pedido.getTipoComprobante());

    resultado.put("idSede", pedido.getSede() == null ? null : pedido.getSede().getIdSede());

    resultado.put("sede", pedido.getSede() == null ? "No aplica" : pedido.getSede().getNombre());

    resultado.put("direccionEntrega", pedido.getDireccionEntrega());
    resultado.put("distrito", pedido.getDistrito());
    resultado.put("referencia", pedido.getReferencia());

    resultado.put("subtotal", pedido.getSubtotal());
    resultado.put("costoEnvio", pedido.getCostoEnvio());
    resultado.put("total", pedido.getTotal());
    resultado.put("productos", productos);

    return resultado;
  }

  @Transactional
  public void editarPedido(
      Long idPedido,
      Long idCliente,
      Map<Long, Integer> cantidades,
      String metodoEntrega,
      Long idSede,
      String direccionEntrega,
      String distrito,
      String referencia) {

    Pedido pedido = obtenerPedido(idPedido, idCliente);
    validarPendiente(pedido);

    List<DetallePedido> detalles = detalleRepository.buscarDetalles(idPedido);

    if (detalles.isEmpty()) {
      throw new IllegalArgumentException("El pedido no contiene productos.");
    }

    if (cantidades == null || cantidades.size() != detalles.size()) {

      throw new IllegalArgumentException("Debes indicar todos los productos del pedido.");
    }

    // Validar entrega
    if ("RECOJO".equals(metodoEntrega)) {

      if (idSede == null) {
        throw new IllegalArgumentException("Selecciona una sede.");
      }

      Sede sede =
          sedeRepository
              .findById(idSede)
              .orElseThrow(() -> new IllegalArgumentException("La sede no existe."));

      if (!"ACTIVO".equals(sede.getEstado())) {
        throw new IllegalArgumentException("La sede no está disponible.");
      }

      pedido.setSede(sede);
      pedido.setDireccionEntrega(null);
      pedido.setDistrito(null);
      pedido.setReferencia(null);

    } else if ("DELIVERY".equals(metodoEntrega)) {

      if (direccionEntrega == null
          || direccionEntrega.isBlank()
          || distrito == null
          || distrito.isBlank()) {

        throw new IllegalArgumentException("Completa la dirección y el distrito.");
      }

      if (direccionEntrega.trim().length() > 200
          || distrito.trim().length() > 80
          || (referencia != null && referencia.trim().length() > 200)) {

        throw new IllegalArgumentException("Los datos de entrega son demasiado largos.");
      }

      pedido.setSede(null);
      pedido.setDireccionEntrega(direccionEntrega.trim());
      pedido.setDistrito(distrito.trim());
      pedido.setReferencia(referencia == null || referencia.isBlank() ? null : referencia.trim());

    } else {
      throw new IllegalArgumentException("Método de entrega inválido.");
    }

    pedido.setMetodoEntrega(metodoEntrega);

    BigDecimal subtotal = BigDecimal.ZERO;

    // Orden estable para bloquear productos
    detalles.sort(Comparator.comparing(d -> d.getProducto().getIdProducto()));

    for (DetallePedido detalle : detalles) {

      Long idProducto = detalle.getProducto().getIdProducto();

      Integer nuevaCantidad = cantidades.get(idProducto);

      if (nuevaCantidad == null || nuevaCantidad < 1) {
        throw new IllegalArgumentException("Las cantidades deben ser mayores que cero.");
      }

      Producto producto =
          productoRepository
              .buscarParaCompra(idProducto)
              .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

      int diferencia = nuevaCantidad - detalle.getCantidad();

      if (diferencia > 0
          && (!"ACTIVO".equals(producto.getEstado()) || producto.getStock() < diferencia)) {

        throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre());
      }

      // Si aumenta cantidad, descuenta stock.
      // Si disminuye cantidad, devuelve stock.
      producto.setStock(producto.getStock() - diferencia);

      detalle.setCantidad(nuevaCantidad);

      // Conservamos el precio acordado al comprar
      detalle.setSubtotal(detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(nuevaCantidad)));

      subtotal = subtotal.add(detalle.getSubtotal());

      productoRepository.save(producto);
      detalleRepository.save(detalle);
    }

    pedido.setSubtotal(subtotal);
    pedido.setTotal(subtotal.add(pedido.getCostoEnvio()));

    pedidoRepository.save(pedido);
  }

  @Transactional
  public void cancelarPedido(Long idPedido, Long idCliente) {

    Pedido pedido = obtenerPedido(idPedido, idCliente);
    validarPendiente(pedido);

    List<DetallePedido> detalles = detalleRepository.buscarDetalles(idPedido);

    detalles.sort(Comparator.comparing(d -> d.getProducto().getIdProducto()));

    // Devolver productos al inventario
    for (DetallePedido detalle : detalles) {

      Producto producto =
          productoRepository
              .buscarParaCompra(detalle.getProducto().getIdProducto())
              .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

      producto.setStock(producto.getStock() + detalle.getCantidad());

      productoRepository.save(producto);
    }

    // Cancelación lógica: conservar historial
    pedido.setEstado("CANCELADO");
    pedidoRepository.save(pedido);
  }
}
