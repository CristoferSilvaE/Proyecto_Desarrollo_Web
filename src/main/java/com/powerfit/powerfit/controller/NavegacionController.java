package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.repository.PlanMembresiaRepository;
import com.powerfit.powerfit.repository.SedeRepository;
import com.powerfit.powerfit.service.CarritoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavegacionController {

  private final PlanMembresiaRepository planMembresiaRepository;
  private final SedeRepository sedeRepository;
  private final CarritoService carritoService;

  public NavegacionController(
      PlanMembresiaRepository planMembresiaRepository,
      SedeRepository sedeRepository,
      CarritoService carritoService) {

    this.planMembresiaRepository = planMembresiaRepository;
    this.sedeRepository = sedeRepository;
    this.carritoService = carritoService;
  }

  @GetMapping("/")
  public String inicio(Model model) {

    model.addAttribute("planes", planMembresiaRepository.findByEstadoOrderByPrecioAsc("ACTIVO"));

    return "inicio/index";
  }

  @GetMapping("/login")
  public String login(HttpSession session, Model model) {

    boolean cambioPendiente = session.getAttribute("idUsuarioCambioPassword") instanceof Long;

    model.addAttribute("mostrarCambioPassword", cambioPendiente);

    if (cambioPendiente) {
      model.addAttribute(
          "identificadorCambioPassword", session.getAttribute("identificadorCambioPassword"));
    }

    return "auth/login";
  }

  @GetMapping("/registro")
  public String registro() {
    return "auth/registro";
  }

  @GetMapping("/checkout")
  public String checkout(Model model, HttpSession session) {
    if (!(session.getAttribute("idCliente") instanceof Long)) {
      return "redirect:/login";
    }

    if (carritoService.obtenerItems(session).isEmpty()) {
      return "redirect:/carrito";
    }

    // Sedes disponibles desde MySQL
    model.addAttribute("sedes", sedeRepository.findByEstadoOrderByNombreAsc("ACTIVO"));

    // Productos del carrito
    model.addAttribute("itemsCarrito", carritoService.obtenerItems(session));

    // Subtotal
    model.addAttribute("subtotal", carritoService.obtenerSubtotal(session));

    // Total provisional, sin costo de delivery
    model.addAttribute("total", carritoService.obtenerSubtotal(session));

    return "tienda/checkout";
  }
}
