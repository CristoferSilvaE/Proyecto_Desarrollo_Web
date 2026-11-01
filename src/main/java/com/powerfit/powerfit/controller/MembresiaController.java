package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Membresia;
import com.powerfit.powerfit.model.PlanMembresia;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.MembresiaRepository;
import com.powerfit.powerfit.repository.PlanMembresiaRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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
      return "membresias/membresia";
    }

    Cliente cliente = clienteRepository.findById(idCliente).orElse(null);

    if (cliente == null) {
      return "membresias/membresia";
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

    return "membresias/membresia";
  }

  @PostMapping("/membresia/solicitar")
  public String solicitar(
      @RequestParam Long idPlan,
      @RequestParam Integer duracionMeses,
      @RequestParam String horarioPreferido,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDeseada,
      @RequestParam String metodoPagoPreferido,
      @RequestParam(required = false) String observacion,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    Cliente cliente = clienteRepository.findById(idCliente).orElse(null);
    PlanMembresia plan = planMembresiaRepository.findById(idPlan).orElse(null);

    if (cliente == null || plan == null || !"ACTIVO".equalsIgnoreCase(plan.getEstado())) {

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

    // Validaciones de los nuevos campos
    if (duracionMeses == null
        || (duracionMeses != 1 && duracionMeses != 3 && duracionMeses != 6)
        || !List.of("Mañana", "Tarde", "Noche").contains(horarioPreferido)
        || fechaInicioDeseada == null
        || fechaInicioDeseada.isBefore(LocalDate.now())
        || !List.of("Efectivo", "Tarjeta", "Yape/Plin").contains(metodoPagoPreferido)
        || (observacion != null && observacion.length() > 255)) {

      redirectAttributes.addFlashAttribute("error", "Verifica los datos de la solicitud.");

      return "redirect:/membresia";
    }

    Membresia membresia = new Membresia();

    membresia.setCliente(cliente);
    membresia.setPlan(plan);
    membresia.setDuracionMeses(duracionMeses);
    membresia.setHorarioPreferido(horarioPreferido);
    membresia.setFechaInicioDeseada(fechaInicioDeseada);
    membresia.setMetodoPagoPreferido(metodoPagoPreferido);
    membresia.setObservacion(observacion);
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
      @RequestParam Integer duracionMeses,
      @RequestParam String horarioPreferido,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDeseada,
      @RequestParam String metodoPagoPreferido,
      @RequestParam(required = false) String observacion,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    Membresia membresia = membresiaRepository.findById(idMembresia).orElse(null);
    PlanMembresia plan = planMembresiaRepository.findById(idPlan).orElse(null);

    // Verificar que la membresía pertenezca al cliente
    if (membresia == null
        || plan == null
        || !"ACTIVO".equalsIgnoreCase(plan.getEstado())
        || !membresia.getCliente().getIdCliente().equals(idCliente)) {

      redirectAttributes.addFlashAttribute("error", "No se pudo actualizar la membresía.");

      return "redirect:/membresia";
    }

    // Solo se pueden editar solicitudes pendientes
    if (!"PENDIENTE".equalsIgnoreCase(membresia.getEstado())) {

      redirectAttributes.addFlashAttribute(
          "error", "Solo puedes modificar solicitudes pendientes.");

      return "redirect:/membresia";
    }

    // Validar los datos ingresados
    if (duracionMeses == null
        || (duracionMeses != 1 && duracionMeses != 3 && duracionMeses != 6)
        || !List.of("Mañana", "Tarde", "Noche").contains(horarioPreferido)
        || fechaInicioDeseada == null
        || fechaInicioDeseada.isBefore(LocalDate.now())
        || !List.of("Efectivo", "Tarjeta", "Yape/Plin").contains(metodoPagoPreferido)
        || (observacion != null && observacion.length() > 255)) {

      redirectAttributes.addFlashAttribute("error", "Verifica los datos ingresados.");

      return "redirect:/membresia";
    }

    // Actualizar los datos de la solicitud
    membresia.setPlan(plan);
    membresia.setDuracionMeses(duracionMeses);
    membresia.setHorarioPreferido(horarioPreferido);
    membresia.setFechaInicioDeseada(fechaInicioDeseada);
    membresia.setMetodoPagoPreferido(metodoPagoPreferido);
    membresia.setObservacion(observacion);

    membresiaRepository.save(membresia);

    redirectAttributes.addFlashAttribute(
        "mensaje", "Solicitud de membresía actualizada correctamente.");

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
