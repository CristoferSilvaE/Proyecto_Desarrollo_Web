package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.DetallePedido;
import com.powerfit.powerfit.model.Pedido;
import com.powerfit.powerfit.model.Producto;
import com.powerfit.powerfit.model.Sede;
import com.powerfit.powerfit.repository.DetallePedidoRepository;
import com.powerfit.powerfit.repository.PedidoRepository;
import com.powerfit.powerfit.repository.ProductoRepository;
import com.powerfit.powerfit.repository.SedeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestionPedidoService {

  private static final ZoneId ZONA = ZoneId.of("America/Lima");
  private static final DateTimeFormatter FORMATO_FECHA =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final Set<String> METODOS_PAGO_VALIDOS =
      Set.of("TARJETA", "YAPE_PLIN", "TRANSFERENCIA");

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

  // ==========================================
  // DETALLE (CLIENTE Y ADMIN)
  // ==========================================

  @Transactional(readOnly = true)
  public Map<String, Object> obtenerDetalle(Long idPedido, Long idCliente) {
    Pedido pedido =
        pedidoRepository
            .findById(idPedido)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));

    if (!pedido.getCliente().getIdCliente().equals(idCliente)) {
      throw new IllegalArgumentException("Pedido no encontrado.");
    }

    return construirMapaDetalle(pedido, false);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> obtenerDetalleAdmin(Long idPedido) {
    Pedido pedido =
        pedidoRepository
            .findById(idPedido)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));

    return construirMapaDetalle(pedido, true);
  }

  private Map<String, Object> construirMapaDetalle(Pedido pedido, boolean incluirDatosCliente) {
    List<DetallePedido> detalles = detalleRepository.buscarDetalles(pedido.getIdPedido());
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
    resultado.put(
        "fechaPedido",
        pedido.getFechaPedido() == null ? null : pedido.getFechaPedido().format(FORMATO_FECHA));
    resultado.put(
        "fechaPago",
        pedido.getFechaPago() == null ? null : pedido.getFechaPago().format(FORMATO_FECHA));
    resultado.put("estado", pedido.getEstado());
    resultado.put("estadoPago", pedido.getEstadoPago());
    resultado.put("metodoEntrega", pedido.getMetodoEntrega());
    resultado.put("metodoPago", pedido.getMetodoPago());
    resultado.put("tipoComprobante", pedido.getTipoComprobante());
    resultado.put("ruc", pedido.getRuc());
    resultado.put("razonSocial", pedido.getRazonSocial());
    resultado.put("idSede", pedido.getSede() == null ? null : pedido.getSede().getIdSede());
    resultado.put("sede", pedido.getSede() == null ? "No aplica" : pedido.getSede().getNombre());
    resultado.put(
        "sedeDistrito", pedido.getSede() == null ? null : pedido.getSede().getDistrito());
    resultado.put(
        "sedeDireccion", pedido.getSede() == null ? null : pedido.getSede().getDireccion());
    resultado.put("direccionEntrega", pedido.getDireccionEntrega());
    resultado.put("distrito", pedido.getDistrito());
    resultado.put("referencia", pedido.getReferencia());
    resultado.put("subtotal", pedido.getSubtotal());
    resultado.put("costoEnvio", pedido.getCostoEnvio());
    resultado.put("total", pedido.getTotal());
    resultado.put("observacion", pedido.getObservacion());
    resultado.put("tieneComprobante", tieneComprobanteDisponible(pedido));
    resultado.put("productos", productos);

    if (incluirDatosCliente && pedido.getCliente() != null) {
      Cliente cliente = pedido.getCliente();
      resultado.put("clienteNombre", cliente.getNombres() + " " + cliente.getApellidos());
      resultado.put("clienteDni", cliente.getDni());
      resultado.put("clienteTelefono", cliente.getTelefono());
      resultado.put(
          "clienteCorreo",
          cliente.getUsuario() != null ? cliente.getUsuario().getCorreo() : "-");
    }

    return resultado;
  }

  // ==========================================
  // CLIENTE: EDITAR PEDIDO PENDIENTE
  // ==========================================

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

    Pedido pedido = obtenerPedidoClienteParaModificar(idPedido, idCliente);
    validarPendiente(pedido);

    List<DetallePedido> detalles = detalleRepository.buscarDetalles(idPedido);

    if (detalles.isEmpty()) {
      throw new IllegalArgumentException("El pedido no contiene productos.");
    }

    if (cantidades == null || cantidades.size() != detalles.size()) {
      throw new IllegalArgumentException("Debes indicar todos los productos del pedido.");
    }

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

      producto.setStock(producto.getStock() - diferencia);
      detalle.setCantidad(nuevaCantidad);
      detalle.setSubtotal(detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(nuevaCantidad)));

      subtotal = subtotal.add(detalle.getSubtotal());

      productoRepository.save(producto);
      detalleRepository.save(detalle);
    }

    pedido.setSubtotal(subtotal);
    pedido.setTotal(subtotal.add(pedido.getCostoEnvio()));

    pedidoRepository.save(pedido);
  }

  // ==========================================
  // CLIENTE: PAGO ONLINE SIMULADO (OPCIÓN A)
  // ==========================================

  @Transactional
  public void simularPagoOnline(Long idPedido, Long idCliente, String metodoPago) {
    Pedido pedido = obtenerPedidoClienteParaModificar(idPedido, idCliente);
    validarClienteActivo(pedido.getCliente());
    validarPendiente(pedido);

    String metodoNormalizado = normalizar(metodoPago);
    if (!METODOS_PAGO_VALIDOS.contains(metodoNormalizado)) {
      throw new IllegalArgumentException("Selecciona un método de pago válido.");
    }

    pedido.setMetodoPago(metodoNormalizado);
    pedido.setEstadoPago("PAGADO");
    pedido.setEstado("EN_PROCESO");
    pedido.setFechaPago(LocalDateTime.now(ZONA));
    pedido.setObservacion("Pago digital simulado confirmado correctamente.");

    pedidoRepository.save(pedido);
  }

  // ==========================================
  // CLIENTE: CANCELAR PEDIDO (CON O SIN REEMBOLSO)
  // ==========================================

  @Transactional
  public boolean cancelarPedido(Long idPedido, Long idCliente) {
    Pedido pedido = obtenerPedidoClienteParaModificar(idPedido, idCliente);

    boolean pendienteSinPago =
        "PENDIENTE".equals(pedido.getEstado()) && "PENDIENTE".equals(pedido.getEstadoPago());
    boolean pagadoEnProceso =
        "EN_PROCESO".equals(pedido.getEstado()) && "PAGADO".equals(pedido.getEstadoPago());

    if (!pendienteSinPago && !pagadoEnProceso) {
      throw new IllegalArgumentException("Este pedido ya no puede cancelarse.");
    }

    devolverStockPedido(idPedido);

    pedido.setEstado("CANCELADO");

    if (pagadoEnProceso) {
      pedido.setEstadoPago("REEMBOLSO_PENDIENTE");
      pedido.setObservacion(
          "Cancelado por el cliente. Reembolso simulado pendiente de atención por administración.");
      pedidoRepository.save(pedido);
      return true;
    }

    pedido.setEstadoPago("CANCELADO");
    pedido.setObservacion("Pedido cancelado por el cliente antes del pago.");
    pedidoRepository.save(pedido);
    return false;
  }

  // ==========================================
  // ADMIN: CONFIRMAR PAGO MANUAL
  // ==========================================

  @Transactional
  public void confirmarPagoAdmin(Long idPedido) {
    Pedido pedido = obtenerPedidoAdminParaModificar(idPedido);
    validarPendiente(pedido);

    pedido.setEstadoPago("PAGADO");
    pedido.setEstado("EN_PROCESO");
    pedido.setFechaPago(LocalDateTime.now(ZONA));
    pedido.setObservacion(
        "Pago verificado y confirmado por administración el "
            + LocalDateTime.now(ZONA).format(FORMATO_FECHA)
            + ".");

    pedidoRepository.save(pedido);
  }

  // ==========================================
  // ADMIN: MARCAR COMO ENTREGADO
  // ==========================================

  @Transactional
  public void marcarEntregadoAdmin(Long idPedido) {
    Pedido pedido = obtenerPedidoAdminParaModificar(idPedido);

    if (!"EN_PROCESO".equals(pedido.getEstado()) || !"PAGADO".equals(pedido.getEstadoPago())) {
      throw new IllegalArgumentException(
          "Solo se pueden marcar como entregados los pedidos pagados y en proceso.");
    }

    pedido.setEstado("ENTREGADO");
    pedido.setObservacion(
        "Pedido entregado al cliente el " + LocalDateTime.now(ZONA).format(FORMATO_FECHA) + ".");

    pedidoRepository.save(pedido);
  }

  // ==========================================
  // ADMIN: CANCELAR PEDIDO (CON MOTIVO Y REEMBOLSO)
  // ==========================================

  @Transactional
  public boolean cancelarPedidoAdmin(Long idPedido, String motivo) {
    Pedido pedido = obtenerPedidoAdminParaModificar(idPedido);

    boolean pendienteSinPago =
        "PENDIENTE".equals(pedido.getEstado()) && "PENDIENTE".equals(pedido.getEstadoPago());
    boolean pagadoEnProceso =
        "EN_PROCESO".equals(pedido.getEstado()) && "PAGADO".equals(pedido.getEstadoPago());

    if (!pendienteSinPago && !pagadoEnProceso) {
      throw new IllegalArgumentException("Este pedido ya no puede cancelarse.");
    }

    String motivoLimpio = motivo == null ? "" : motivo.trim();
    if (motivoLimpio.length() > 180) {
      throw new IllegalArgumentException("El motivo no puede superar los 180 caracteres.");
    }

    devolverStockPedido(idPedido);

    pedido.setEstado("CANCELADO");

    if (pagadoEnProceso) {
      pedido.setEstadoPago("REEMBOLSO_PENDIENTE");
      String obs =
          motivoLimpio.isBlank()
              ? "Cancelado por administración. Reembolso simulado pendiente."
              : "Cancelado por administración: " + motivoLimpio + " (Reembolso pendiente).";
      pedido.setObservacion(truncarObservacion(obs));
      pedidoRepository.save(pedido);
      return true;
    }

    pedido.setEstadoPago("CANCELADO");
    String obs =
        motivoLimpio.isBlank()
            ? "Pedido cancelado por administración."
            : "Cancelado por administración: " + motivoLimpio;
    pedido.setObservacion(truncarObservacion(obs));
    pedidoRepository.save(pedido);
    return false;
  }

  // ==========================================
  // ADMIN: PROCESAR REEMBOLSO SIMULADO
  // ==========================================

  @Transactional
  public void procesarReembolsoAdmin(Long idPedido) {
    Pedido pedido = obtenerPedidoAdminParaModificar(idPedido);

    if (!"CANCELADO".equals(pedido.getEstado())
        || !"REEMBOLSO_PENDIENTE".equals(pedido.getEstadoPago())) {
      throw new IllegalArgumentException("Este pedido no tiene un reembolso pendiente.");
    }

    if (pedido.getFechaPago() == null) {
      throw new IllegalArgumentException("No se encontró la fecha del pago original.");
    }

    pedido.setEstadoPago("REEMBOLSADO");
    pedido.setObservacion(
        "Reembolso simulado procesado por administración el "
            + LocalDateTime.now(ZONA).format(FORMATO_FECHA)
            + ". No se realizó transferencia real.");

    pedidoRepository.save(pedido);
  }

  // ==========================================
  // MÉTODOS AUXILIARES
  // ==========================================

  private Pedido obtenerPedidoClienteParaModificar(Long idPedido, Long idCliente) {
    Pedido pedido = obtenerPedidoAdminParaModificar(idPedido);
    if (!pedido.getCliente().getIdCliente().equals(idCliente)) {
      throw new IllegalArgumentException("Pedido no encontrado.");
    }
    return pedido;
  }

  private Pedido obtenerPedidoAdminParaModificar(Long idPedido) {
    return pedidoRepository
        .buscarParaModificar(idPedido)
        .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));
  }

  private void validarPendiente(Pedido pedido) {
    if (!"PENDIENTE".equals(pedido.getEstado()) || !"PENDIENTE".equals(pedido.getEstadoPago())) {
      throw new IllegalArgumentException("Este pedido ya no se encuentra pendiente de pago.");
    }
  }

  private void validarClienteActivo(Cliente cliente) {
    if (cliente == null
        || !"ACTIVO".equalsIgnoreCase(cliente.getEstado())
        || cliente.getUsuario() == null
        || !"ACTIVO".equalsIgnoreCase(cliente.getUsuario().getEstado())) {
      throw new IllegalArgumentException("Tu cuenta no se encuentra habilitada para operar.");
    }
  }

  private void devolverStockPedido(Long idPedido) {
    List<DetallePedido> detalles = detalleRepository.buscarDetalles(idPedido);
    detalles.sort(Comparator.comparing(d -> d.getProducto().getIdProducto()));

    for (DetallePedido detalle : detalles) {
      Producto producto =
          productoRepository
              .buscarParaCompra(detalle.getProducto().getIdProducto())
              .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

      producto.setStock(producto.getStock() + detalle.getCantidad());
      productoRepository.save(producto);
    }
  }

  private boolean tieneComprobanteDisponible(Pedido pedido) {
    String estadoPago = pedido.getEstadoPago();
    return "PAGADO".equals(estadoPago)
        || "REEMBOLSO_PENDIENTE".equals(estadoPago)
        || "REEMBOLSADO".equals(estadoPago);
  }

  private String normalizar(String valor) {
    return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
  }

  private String truncarObservacion(String texto) {
    if (texto == null) {
      return null;
    }
    return texto.length() > 255 ? texto.substring(0, 255) : texto;
  }
}