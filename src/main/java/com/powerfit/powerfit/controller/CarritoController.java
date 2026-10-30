package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.service.CarritoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CarritoController {

  private final CarritoService carritoService;

  public CarritoController(CarritoService carritoService) {
    this.carritoService = carritoService;
  }

  // Mostrar carrito
  @GetMapping("/carrito")
  public String verCarrito(Model model, HttpSession session) {

    if (!clienteAutenticado(session)) {
      return "redirect:/login";
    }

    model.addAttribute("itemsCarrito", carritoService.obtenerItems(session));

    model.addAttribute("subtotal", carritoService.obtenerSubtotal(session));

    model.addAttribute("cantidadTotal", carritoService.obtenerCantidadTotal(session));

    return "carrito";
  }

  // Agregar producto desde la tienda
  @PostMapping("/carrito/agregar/{id}")
  public String agregar(
      @PathVariable("id") Long idProducto, HttpSession session, RedirectAttributes redirect) {

    if (!clienteAutenticado(session)) {
      return "redirect:/login";
    }
    try {
      carritoService.agregar(idProducto, session);
      redirect.addFlashAttribute("mensaje", "Producto añadido al carrito.");

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/tienda";
  }

  // Aumentar cantidad
  @PostMapping("/carrito/aumentar/{id}")
  public String aumentar(
      @PathVariable("id") Long idProducto, HttpSession session, RedirectAttributes redirect) {
    if (!clienteAutenticado(session)) {
      return "redirect:/login";
    }
    try {
      carritoService.cambiarCantidad(idProducto, 1, session);

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/carrito";
  }

  // Disminuir cantidad
  @PostMapping("/carrito/disminuir/{id}")
  public String disminuir(
      @PathVariable("id") Long idProducto, HttpSession session, RedirectAttributes redirect) {
    if (!clienteAutenticado(session)) {
      return "redirect:/login";
    }
    try {
      carritoService.cambiarCantidad(idProducto, -1, session);

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/carrito";
  }

  // Eliminar producto
  @PostMapping("/carrito/eliminar/{id}")
  public String eliminar(@PathVariable("id") Long idProducto, HttpSession session) {
    if (!clienteAutenticado(session)) {
      return "redirect:/login";
    }
    carritoService.eliminar(idProducto, session);

    return "redirect:/carrito";
  }

  // Vaciar carrito
  @PostMapping("/carrito/vaciar")
  public String vaciar(HttpSession session) {
    if (!clienteAutenticado(session)) {
      return "redirect:/login";
    }
    carritoService.vaciar(session);

    return "redirect:/carrito";
  }

  private boolean clienteAutenticado(HttpSession session) {

    return session.getAttribute("idCliente") instanceof Long;
  }
}
