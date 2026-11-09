package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.HorarioClase;
import com.powerfit.powerfit.model.PagoClase;
import com.powerfit.powerfit.repository.PagoClaseRepository;
import com.powerfit.powerfit.service.ClaseService;
import com.powerfit.powerfit.service.HorarioClaseService;
import com.powerfit.powerfit.service.InstructorService;
import com.powerfit.powerfit.service.PagoClaseService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

  private final InstructorService instructorService;
  private final ClaseService claseService;
  private final HorarioClaseService horarioService;
  private final PagoClaseRepository pagoClaseRepository;
  private final PagoClaseService pagoClaseService;

  public AdminController(
      InstructorService instructorService,
      ClaseService claseService,
      HorarioClaseService horarioService,
      PagoClaseRepository pagoClaseRepository,
      PagoClaseService pagoClaseService) {

    this.instructorService = instructorService;
    this.claseService = claseService;
    this.horarioService = horarioService;
    this.pagoClaseRepository = pagoClaseRepository;
    this.pagoClaseService = pagoClaseService;
  }

  @GetMapping({"", "/"})
  public String dashboard() {
    return "admin/dashboard";
  }

  @GetMapping("/clases")
  public String clases(Model model) {

    List<HorarioClase> horarios = horarioService.listarTodos();

    model.addAttribute("instructores", instructorService.listarTodos());
    model.addAttribute("clases", claseService.listar());

    model.addAttribute("horarios", horarios);

    model.addAttribute("clasesActivas", horarioService.listarClasesActivas());

    model.addAttribute("instructoresActivos", horarioService.listarInstructoresActivos());

    model.addAttribute("reservasPorHorario", horarioService.obtenerReservasPorHorario(horarios));

    model.addAttribute(
        "reservasDetallePorHorario", horarioService.obtenerDetalleReservasPorHorario(horarios));

    model.addAttribute("fechaHoy", LocalDate.now(ZoneId.of("America/Lima")));

    model.addAttribute("clasesProgramadas", horarioService.contarClasesProgramadas());

    model.addAttribute("ocupacionPromedio", horarioService.calcularOcupacionPromedio());

    model.addAttribute("instructoresActivosTotal", horarioService.contarInstructoresActivos());

    model.addAttribute("cuposDisponibles", horarioService.contarCuposDisponibles());

    // SOLICITUDES Y PAGOS DE CLASES
    List<PagoClase> solicitudesClases =
        pagoClaseRepository.findByEstadoPagoInOrderByFechaRegistroDesc(
            List.of("PENDIENTE", "AUTORIZADO", "PAGADO", "BONIFICADO"));

    model.addAttribute("solicitudesClases", solicitudesClases);

    model.addAttribute(
        "totalSolicitudesPendientes", pagoClaseRepository.countByEstadoPago("PENDIENTE"));

    return "admin/clases";
  }

  // ==========================================
  // APROBAR PAGO DIGITAL DE CLASE
  // ==========================================

  @PostMapping("/clases/solicitudes/{idPago}/aprobar-digital")
  public String aprobarPagoClase(@PathVariable Long idPago, RedirectAttributes redirect) {

    try {
      pagoClaseService.aprobarPagoDigital(idPago);

      redirect.addFlashAttribute(
          "mensaje", "Solicitud aprobada. El cliente ya puede realizar el pago.");

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=solicitudes";
  }

  // ==========================================
  // REGISTRAR PAGO EN EFECTIVO
  // ==========================================

  @PostMapping("/clases/solicitudes/{idPago}/efectivo")
  public String registrarPagoClaseEfectivo(@PathVariable Long idPago, RedirectAttributes redirect) {

    try {
      pagoClaseService.registrarPagoEfectivo(idPago);

      redirect.addFlashAttribute(
          "mensaje", "Pago presencial registrado. La reserva quedó confirmada.");

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=solicitudes";
  }

  // ==========================================
  // RECHAZAR SOLICITUD
  // ==========================================

  @PostMapping("/clases/solicitudes/{idPago}/rechazar")
  public String rechazarSolicitudClase(
      @PathVariable Long idPago,
      @RequestParam(required = false) String motivo,
      RedirectAttributes redirect) {

    try {
      pagoClaseService.rechazarSolicitud(idPago, motivo);

      redirect.addFlashAttribute("mensaje", "Solicitud rechazada y cupo liberado.");

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=solicitudes";
  }

  @GetMapping("/productos")
  public String productos() {
    return "admin/productos";
  }

  @GetMapping("/pedidos")
  public String pedidos() {
    return "admin/pedidos";
  }
}
