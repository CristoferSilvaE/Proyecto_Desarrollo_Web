package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.service.PagoClaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clases/pagos")
public class PagoClaseController {

  private final PagoClaseService pagoClaseService;

  public PagoClaseController(PagoClaseService pagoClaseService) {
    this.pagoClaseService = pagoClaseService;
  }

  // ==========================================
  // PAGAR CLASE ADICIONAL
  // ==========================================

  @PostMapping("/{idPago}/pagar")
  public String pagar(
      @PathVariable Long idPago,
      @RequestParam String metodoPago,
      HttpSession session,
      RedirectAttributes redirect) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    try {

      pagoClaseService.simularPagoDigital(idPago, idCliente, metodoPago);

      redirect.addFlashAttribute(
          "mensaje", "¡Pago realizado correctamente! Tu clase está confirmada.");

    } catch (IllegalArgumentException e) {

      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/perfil";
  }
}
