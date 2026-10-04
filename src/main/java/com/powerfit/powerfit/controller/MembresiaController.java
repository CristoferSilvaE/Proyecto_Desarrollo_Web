package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Membresia;
import com.powerfit.powerfit.model.PlanMembresia;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.MembresiaRepository;
import com.powerfit.powerfit.repository.PlanMembresiaRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MembresiaController {

  private final MembresiaRepository membresiaRepository;
  private final PlanMembresiaRepository planMembresiaRepository;
  private final ClienteRepository clienteRepository;

  public MembresiaController(
      MembresiaRepository membresiaRepository,
      PlanMembresiaRepository planMembresiaRepository,
      ClienteRepository clienteRepository) {

    this.membresiaRepository = membresiaRepository;
    this.planMembresiaRepository = planMembresiaRepository;
    this.clienteRepository = clienteRepository;
  }

  @GetMapping("/membresia")
  public String membresia(
      @RequestParam(required = false) Long plan, HttpSession session, Model model) {

    model.addAttribute("planes", planMembresiaRepository.findByEstadoOrderByPrecioAsc("ACTIVO"));

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "membresia";
    }

    Cliente cliente = clienteRepository.findById(idCliente).orElse(null);

    if (cliente == null) {
      return "membresia";
    }

    List<Membresia> historial =
        membresiaRepository.findByCliente_IdClienteOrderByFechaSolicitudDesc(idCliente);

    Membresia pendiente =
        membresiaRepository
            .findFirstByCliente_IdClienteAndEstadoOrderByFechaSolicitudDesc(idCliente, "PENDIENTE")
            .orElse(null);

    Membresia activa =
        membresiaRepository
            .findFirstByCliente_IdClienteAndEstadoOrderByFechaSolicitudDesc(idCliente, "ACTIVA")
            .orElse(null);

    model.addAttribute("historialMembresias", historial);
    model.addAttribute("membresiaPendiente", pendiente);
    model.addAttribute("membresiaActiva", activa);
    model.addAttribute("planSeleccionado", plan);

    return "membresia";
  }

  @PostMapping("/membresia/solicitar")
  public String solicitar(
      @RequestParam Long idPlan, HttpSession session, RedirectAttributes redirectAttributes) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    Cliente cliente = clienteRepository.findById(idCliente).orElse(null);
    PlanMembresia plan = planMembresiaRepository.findById(idPlan).orElse(null);

    if (cliente == null || plan == null) {
      redirectAttributes.addFlashAttribute("error", "No se pudo procesar la solicitud.");

      return "redirect:/membresia";
    }

    boolean tienePendiente =
        membresiaRepository
            .findFirstByCliente_IdClienteAndEstadoOrderByFechaSolicitudDesc(idCliente, "PENDIENTE")
            .isPresent();

    boolean tieneActiva =
        membresiaRepository
            .findFirstByCliente_IdClienteAndEstadoOrderByFechaSolicitudDesc(idCliente, "ACTIVA")
            .isPresent();

    if (tienePendiente || tieneActiva) {
      redirectAttributes.addFlashAttribute(
          "error", "Ya tienes una membresía activa o una solicitud pendiente.");

      return "redirect:/membresia";
    }

    Membresia membresia = new Membresia();
    membresia.setCliente(cliente);
    membresia.setPlan(plan);
    membresia.setEstado("PENDIENTE");

    membresiaRepository.save(membresia);

    redirectAttributes.addFlashAttribute(
        "mensaje", "Solicitud de membresía registrada correctamente.");

    return "redirect:/membresia";
  }

  @PostMapping("/membresia/actualizar")
  public String actualizar(
      @RequestParam Long idMembresia,
      @RequestParam Long idPlan,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    Membresia membresia = membresiaRepository.findById(idMembresia).orElse(null);

    PlanMembresia plan = planMembresiaRepository.findById(idPlan).orElse(null);

    if (membresia == null
        || plan == null
        || !membresia.getCliente().getIdCliente().equals(idCliente)) {

      redirectAttributes.addFlashAttribute("error", "No se pudo actualizar la membresía.");

      return "redirect:/membresia";
    }

    if (!"PENDIENTE".equalsIgnoreCase(membresia.getEstado())) {
      redirectAttributes.addFlashAttribute(
          "error", "Solo puedes modificar solicitudes pendientes.");

      return "redirect:/membresia";
    }

    membresia.setPlan(plan);

    membresiaRepository.save(membresia);

    redirectAttributes.addFlashAttribute("mensaje", "Plan actualizado correctamente.");

    return "redirect:/membresia";
  }

  @PostMapping("/membresia/eliminar")
  public String eliminar(
      @RequestParam Long idMembresia, HttpSession session, RedirectAttributes redirectAttributes) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    Membresia membresia = membresiaRepository.findById(idMembresia).orElse(null);

    if (membresia == null || !membresia.getCliente().getIdCliente().equals(idCliente)) {

      redirectAttributes.addFlashAttribute("error", "No se encontró la solicitud.");

      return "redirect:/membresia";
    }

    if (!"PENDIENTE".equalsIgnoreCase(membresia.getEstado())) {
      redirectAttributes.addFlashAttribute("error", "Solo puedes eliminar solicitudes pendientes.");

      return "redirect:/membresia";
    }

    membresiaRepository.delete(membresia);

    redirectAttributes.addFlashAttribute("mensaje", "Solicitud cancelada correctamente.");

    return "redirect:/membresia";
  }
}
