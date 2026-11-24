package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Pedido;
import com.powerfit.powerfit.repository.PedidoRepository;
import com.powerfit.powerfit.service.ComprobantePedidoPdfService;
import com.powerfit.powerfit.service.GestionPedidoService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidosController {

  private static final ZoneId ZONA = ZoneId.of("America/Lima");
  private static final Set<String> ESTADOS_VALIDOS =
      Set.of("TODOS", "PENDIENTE", "EN_PROCESO", "ENTREGADO", "CANCELADO");
  private static final Set<String> ESTADOS_PAGO_VALIDOS =
      Set.of("TODOS", "PENDIENTE", "PAGADO", "REEMBOLSO_PENDIENTE", "REEMBOLSADO", "CANCELADO");

  private final PedidoRepository pedidoRepository;
  private final GestionPedidoService gestionPedidoService;
  private final ComprobantePedidoPdfService comprobantePedidoPdfService;

  public AdminPedidosController(
      PedidoRepository pedidoRepository,
      GestionPedidoService gestionPedidoService,
      ComprobantePedidoPdfService comprobantePedidoPdfService) {
    this.pedidoRepository = pedidoRepository;
    this.gestionPedidoService = gestionPedidoService;
    this.comprobantePedidoPdfService = comprobantePedidoPdfService;
  }

  // ==========================================
  // LISTAR PEDIDOS CON FILTROS E INDICADORES
  // ==========================================

  @GetMapping
  public String listar(
      @RequestParam(defaultValue = "") String buscar,
      @RequestParam(defaultValue = "TODOS") String estado,
      @RequestParam(defaultValue = "TODOS") String estadoPago,
      Model model) {

    List<Pedido> todos = pedidoRepository.findAllByOrderByFechaPedidoDesc();

    String filtroEstado = normalizarFiltro(estado, ESTADOS_VALIDOS);
    String filtroPago = normalizarFiltro(estadoPago, ESTADOS_PAGO_VALIDOS);

    List<Pedido> filtrados =
        todos.stream()
            .filter(p -> filtroEstado.equals("TODOS") || filtroEstado.equalsIgnoreCase(p.getEstado()))
            .filter(
                p -> filtroPago.equals("TODOS") || filtroPago.equalsIgnoreCase(p.getEstadoPago()))
            .filter(p -> coincideBusqueda(p, buscar))
            .toList();

    LocalDate hoy = LocalDate.now(ZONA);

    long pedidosPendientes =
        todos.stream()
            .filter(
                p ->
                    "PENDIENTE".equalsIgnoreCase(p.getEstado())
                        || "EN_PROCESO".equalsIgnoreCase(p.getEstado()))
            .count();

    long pagosConfirmados =
        todos.stream().filter(p -> "PAGADO".equalsIgnoreCase(p.getEstadoPago())).count();

    long reembolsosPendientes =
        todos.stream()
            .filter(p -> "REEMBOLSO_PENDIENTE".equalsIgnoreCase(p.getEstadoPago()))
            .count();

    BigDecimal ventasMes =
        todos.stream()
            .filter(p -> "PAGADO".equalsIgnoreCase(p.getEstadoPago()))
            .filter(p -> !"CANCELADO".equalsIgnoreCase(p.getEstado()))
            .filter(
                p -> {
                  LocalDate fechaRef =
                      p.getFechaPago() != null
                          ? p.getFechaPago().toLocalDate()
                          : p.getFechaPedido().toLocalDate();
                  return fechaRef.getYear() == hoy.getYear()
                      && fechaRef.getMonth() == hoy.getMonth();
                })
            .map(Pedido::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    model.addAttribute("pedidos", filtrados);
    model.addAttribute("buscar", buscar);
    model.addAttribute("estadoFiltro", filtroEstado);
    model.addAttribute("estadoPagoFiltro", filtroPago);

    model.addAttribute("pedidosTotales", todos.size());
    model.addAttribute("pedidosPendientes", pedidosPendientes);
    model.addAttribute("pagosConfirmados", pagosConfirmados);
    model.addAttribute("reembolsosPendientes", reembolsosPendientes);
    model.addAttribute("ventasMes", ventasMes);

    return "admin/pedidos";
  }

  // ==========================================
  // VER DETALLE JSON
  // ==========================================

  @GetMapping("/{id}/detalle")
  public ResponseEntity<?> verDetalle(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(gestionPedidoService.obtenerDetalleAdmin(id));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
  }

  // ==========================================
  // CONFIRMAR PAGO MANUAL
  // ==========================================

  @PostMapping("/{id}/confirmar-pago")
  public String confirmarPago(@PathVariable Long id, RedirectAttributes redirect) {
    try {
      gestionPedidoService.confirmarPagoAdmin(id);
      redirect.addFlashAttribute(
          "mensaje", "Pago del pedido #PF-" + id + " confirmado. El pedido pasó a EN PROCESO.");
    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/admin/pedidos";
  }

  // ==========================================
  // MARCAR COMO ENTREGADO
  // ==========================================

  @PostMapping("/{id}/entregar")
  public String marcarEntregado(@PathVariable Long id, RedirectAttributes redirect) {
    try {
      gestionPedidoService.marcarEntregadoAdmin(id);
      redirect.addFlashAttribute("mensaje", "Pedido #PF-" + id + " marcado como ENTREGADO.");
    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/admin/pedidos";
  }

  // ==========================================
  // CANCELAR PEDIDO
  // ==========================================

  @PostMapping("/{id}/cancelar")
  public String cancelarPedido(
      @PathVariable Long id,
      @RequestParam(required = false) String motivo,
      RedirectAttributes redirect) {
    try {
      boolean requiereReembolso = gestionPedidoService.cancelarPedidoAdmin(id, motivo);
      if (requiereReembolso) {
        redirect.addFlashAttribute(
            "mensaje",
            "Pedido #PF-"
                + id
                + " cancelado. El stock fue restaurado y quedó un reembolso pendiente.");
      } else {
        redirect.addFlashAttribute(
            "mensaje", "Pedido #PF-" + id + " cancelado y stock restaurado correctamente.");
      }
    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/admin/pedidos";
  }

  // ==========================================
  // PROCESAR REEMBOLSO SIMULADO
  // ==========================================

  @PostMapping("/{id}/reembolso")
  public String procesarReembolso(@PathVariable Long id, RedirectAttributes redirect) {
    try {
      gestionPedidoService.procesarReembolsoAdmin(id);
      redirect.addFlashAttribute(
          "mensaje", "Reembolso simulado del pedido #PF-" + id + " procesado correctamente.");
    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/admin/pedidos";
  }

  // ==========================================
  // COMPROBANTE PDF DESDE ADMIN
  // ==========================================

  @GetMapping("/{id}/comprobante")
  public ResponseEntity<byte[]> descargarComprobante(@PathVariable Long id) {
    try {
      byte[] pdf = comprobantePedidoPdfService.generarComprobanteAdmin(id);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(
          ContentDisposition.inline()
              .filename("comprobante-pedido-PF-" + id + ".pdf")
              .build());
      return ResponseEntity.ok().headers(headers).body(pdf);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // ==========================================
  // EXPORTAR REPORTE CSV
  // ==========================================

  @GetMapping("/exportar")
  public ResponseEntity<byte[]> exportarCsv(
      @RequestParam(defaultValue = "") String buscar,
      @RequestParam(defaultValue = "TODOS") String estado,
      @RequestParam(defaultValue = "TODOS") String estadoPago) {

    String filtroEstado = normalizarFiltro(estado, ESTADOS_VALIDOS);
    String filtroPago = normalizarFiltro(estadoPago, ESTADOS_PAGO_VALIDOS);
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    List<Pedido> pedidos =
        pedidoRepository.findAllByOrderByFechaPedidoDesc().stream()
            .filter(p -> filtroEstado.equals("TODOS") || filtroEstado.equalsIgnoreCase(p.getEstado()))
            .filter(
                p -> filtroPago.equals("TODOS") || filtroPago.equalsIgnoreCase(p.getEstadoPago()))
            .filter(p -> coincideBusqueda(p, buscar))
            .toList();

    StringBuilder csv = new StringBuilder("\uFEFF");
    csv.append("Pedido,Cliente,DNI,Fecha,Entrega,MetodoPago,Comprobante,Total,Estado,EstadoPago\n");

    for (Pedido p : pedidos) {
      String cliente =
          p.getCliente() != null
              ? p.getCliente().getNombres() + " " + p.getCliente().getApellidos()
              : "";
      String dni = p.getCliente() != null ? p.getCliente().getDni() : "";
      String fecha = p.getFechaPedido() != null ? p.getFechaPedido().format(fmt) : "";

      csv.append(escaparCsv("#PF-" + p.getIdPedido()))
          .append(',')
          .append(escaparCsv(cliente))
          .append(',')
          .append(escaparCsv(dni))
          .append(',')
          .append(escaparCsv(fecha))
          .append(',')
          .append(escaparCsv(p.getMetodoEntrega()))
          .append(',')
          .append(escaparCsv(p.getMetodoPago()))
          .append(',')
          .append(escaparCsv(p.getTipoComprobante()))
          .append(',')
          .append(p.getTotal())
          .append(',')
          .append(escaparCsv(p.getEstado()))
          .append(',')
          .append(escaparCsv(p.getEstadoPago()))
          .append('\n');
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
    headers.setContentDisposition(
        ContentDisposition.attachment().filename("reporte-pedidos-powerfit.csv").build());

    return ResponseEntity.ok()
        .headers(headers)
        .body(csv.toString().getBytes(StandardCharsets.UTF_8));
  }

  // ==========================================
  // AUXILIARES
  // ==========================================

  private String normalizarFiltro(String valor, Set<String> permitidos) {
    String limpio = valor == null ? "TODOS" : valor.trim().toUpperCase(Locale.ROOT);
    return permitidos.contains(limpio) ? limpio : "TODOS";
  }

  private boolean coincideBusqueda(Pedido pedido, String buscar) {
    if (buscar == null || buscar.isBlank()) {
      return true;
    }

    String termino = buscar.trim().toLowerCase(Locale.ROOT);
    String codigo = ("#pf-" + pedido.getIdPedido()).toLowerCase(Locale.ROOT);
    String nombre =
        pedido.getCliente() != null
            ? (pedido.getCliente().getNombres() + " " + pedido.getCliente().getApellidos())
                .toLowerCase(Locale.ROOT)
            : "";
    String dni =
        pedido.getCliente() != null && pedido.getCliente().getDni() != null
            ? pedido.getCliente().getDni().toLowerCase(Locale.ROOT)
            : "";
    String correo =
        pedido.getCliente() != null
                && pedido.getCliente().getUsuario() != null
                && pedido.getCliente().getUsuario().getCorreo() != null
            ? pedido.getCliente().getUsuario().getCorreo().toLowerCase(Locale.ROOT)
            : "";

    return codigo.contains(termino)
        || String.valueOf(pedido.getIdPedido()).contains(termino)
        || nombre.contains(termino)
        || dni.contains(termino)
        || correo.contains(termino);
  }

  private String escaparCsv(String valor) {
    if (valor == null) {
      return "";
    }
    String limpio = valor.replace("\"", "\"\"");
    return "\"" + limpio + "\"";
  }
}