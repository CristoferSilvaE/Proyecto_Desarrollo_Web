package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Pedido;
import com.powerfit.powerfit.repository.PedidoRepository;
import com.powerfit.powerfit.repository.SedeRepository;
import com.powerfit.powerfit.service.CarritoService;
import com.powerfit.powerfit.service.ComprobantePedidoPdfService;
import com.powerfit.powerfit.service.GestionPedidoService;
import com.powerfit.powerfit.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PedidoController {

  private final PedidoService pedidoService;
  private final CarritoService carritoService;
  private final PedidoRepository pedidoRepository;
  private final GestionPedidoService gestionPedidoService;
  private final ComprobantePedidoPdfService comprobantePedidoPdfService;
  private final SedeRepository sedeRepository;

  public PedidoController(
      PedidoService pedidoService,
      CarritoService carritoService,
      PedidoRepository pedidoRepository,
      GestionPedidoService gestionPedidoService,
      ComprobantePedidoPdfService comprobantePedidoPdfService,
      SedeRepository sedeRepository) {

    this.pedidoService = pedidoService;
    this.carritoService = carritoService;
    this.pedidoRepository = pedidoRepository;
    this.gestionPedidoService = gestionPedidoService;
    this.comprobantePedidoPdfService = comprobantePedidoPdfService;
    this.sedeRepository = sedeRepository;
  }

  @PostMapping("/pedidos/confirmar")
  public String confirmarCompra(
      @RequestParam String metodoEntrega,
      @RequestParam(required = false) Long idSede,
      @RequestParam(required = false) String direccionEntrega,
      @RequestParam(required = false) String distrito,
      @RequestParam(required = false) String referencia,
      @RequestParam String tipoComprobante,
      @RequestParam(required = false) String ruc,
      @RequestParam(required = false) String razonSocial,
      @RequestParam String metodoPago,
      HttpSession session,
      RedirectAttributes redirect) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    try {
      Pedido pedido =
          pedidoService.registrarPedido(
              idCliente,
              carritoService.obtenerItems(session),
              metodoEntrega,
              idSede,
              direccionEntrega,
              distrito,
              referencia,
              tipoComprobante,
              ruc,
              razonSocial,
              metodoPago);

      carritoService.vaciar(session);

      redirect.addFlashAttribute("pedidoExitoso", pedido.getIdPedido());

      return "redirect:/pedidos";

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
      return "redirect:/checkout";
    }
  }

  @GetMapping("/pedidos")
  public String misPedidos(HttpSession session, Model model) {
    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    model.addAttribute(
        "pedidos", pedidoRepository.findByCliente_IdClienteOrderByFechaPedidoDesc(idCliente));
    model.addAttribute("sedes", sedeRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
    return "pedidos/pedidos";
  }

  // 1. VER DETALLE DEL PEDIDO
  @GetMapping("/pedidos/{id}/detalle")
  public ResponseEntity<?> verDetalle(@PathVariable Long id, HttpSession session) {
    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión."));
    }

    try {
      return ResponseEntity.ok(gestionPedidoService.obtenerDetalle(id, idCliente));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
  }

  // 2. EDITAR PEDIDO PENDIENTE
  @PostMapping("/pedidos/{id}/editar")
  public String editarPedido(
      @PathVariable Long id,
      @RequestParam List<Long> idProducto,
      @RequestParam List<Integer> cantidad,
      @RequestParam String metodoEntrega,
      @RequestParam(required = false) Long idSede,
      @RequestParam(required = false) String direccionEntrega,
      @RequestParam(required = false) String distrito,
      @RequestParam(required = false) String referencia,
      HttpSession session,
      RedirectAttributes redirect) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    try {
      if (idProducto.size() != cantidad.size()) {
        throw new IllegalArgumentException("Los productos y cantidades no coinciden.");
      }

      Map<Long, Integer> cantidades = new LinkedHashMap<>();

      for (int i = 0; i < idProducto.size(); i++) {
        if (cantidades.putIfAbsent(idProducto.get(i), cantidad.get(i)) != null) {
          throw new IllegalArgumentException("Hay productos duplicados.");
        }
      }

      gestionPedidoService.editarPedido(
          id, idCliente, cantidades, metodoEntrega, idSede, direccionEntrega, distrito, referencia);

      redirect.addFlashAttribute("mensaje", "Pedido actualizado correctamente.");

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/pedidos";
  }

  // 3. PAGAR PEDIDO ONLINE (SIMULADO)
  @PostMapping("/pedidos/{id}/pagar")
  public String pagarPedido(
      @PathVariable Long id,
      @RequestParam String metodoPago,
      HttpSession session,
      RedirectAttributes redirect) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    try {
      gestionPedidoService.simularPagoOnline(id, idCliente, metodoPago);
      redirect.addFlashAttribute(
          "mensaje",
          "¡Pago realizado correctamente! Tu pedido #PF-"
              + id
              + " ya está en proceso y puedes descargar tu comprobante.");

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/pedidos";
  }

  // 4. CANCELAR PEDIDO (CON O SIN REEMBOLSO)
  @PostMapping("/pedidos/{id}/cancelar")
  public String cancelarPedido(
      @PathVariable Long id, HttpSession session, RedirectAttributes redirect) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    try {
      boolean requiereReembolso = gestionPedidoService.cancelarPedido(id, idCliente);

      if (requiereReembolso) {
        redirect.addFlashAttribute(
            "mensaje",
            "Pedido #PF-"
                + id
                + " cancelado. Los productos regresaron al inventario y tu reembolso quedó"
                + " pendiente de atención.");
      } else {
        redirect.addFlashAttribute(
            "mensaje", "Pedido #PF-" + id + " cancelado correctamente.");
      }

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/pedidos";
  }

  // 5. DESCARGAR COMPROBANTE PDF
  @GetMapping("/pedidos/{id}/comprobante")
  public ResponseEntity<byte[]> descargarComprobante(
      @PathVariable Long id, HttpSession session) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return ResponseEntity.status(401).build();
    }

    try {
      byte[] pdf = comprobantePedidoPdfService.generarComprobanteCliente(id, idCliente);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(
          ContentDisposition.inline()
              .filename("comprobante-pedido-PF-" + id + ".pdf")
              .build());

      return ResponseEntity.ok().headers(headers).body(pdf);

    } catch (SecurityException e) {
      return ResponseEntity.status(403).build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}