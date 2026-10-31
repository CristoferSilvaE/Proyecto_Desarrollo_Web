package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.service.ReservaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clases")
public class ReservaController {

  private final ReservaService reservaService;

  public ReservaController(ReservaService reservaService) {
    this.reservaService = reservaService;
  }

  // ==========================================
  // RESERVAR CLASE
  // ==========================================

  @PostMapping("/reservar")
  public String reservar(
      @RequestParam Long idHorario, HttpSession session, RedirectAttributes redirect) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    try {

      reservaService.reservar(idCliente, idHorario);

      redirect.addFlashAttribute("mensaje", "¡Tu reserva se realizó correctamente!");

    } catch (IllegalArgumentException e) {

      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/clases";
  }

  // ==========================================
  // CANCELAR RESERVA
  // ==========================================

  @PostMapping("/cancelar/{idReserva}")
  public String cancelar(
      @PathVariable Long idReserva, HttpSession session, RedirectAttributes redirect) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    try {

      reservaService.cancelar(idCliente, idReserva);

      redirect.addFlashAttribute("mensaje", "Tu reserva fue cancelada correctamente.");

    } catch (IllegalArgumentException e) {

      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/perfil";
  }
}
