package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.HorarioClase;
import com.powerfit.powerfit.service.ClaseService;
import com.powerfit.powerfit.service.HorarioClaseService;
import com.powerfit.powerfit.service.InstructorService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

  private final InstructorService instructorService;
  private final ClaseService claseService;
  private final HorarioClaseService horarioService;

  public AdminController(
      InstructorService instructorService,
      ClaseService claseService,
      HorarioClaseService horarioService) {

    this.instructorService = instructorService;
    this.claseService = claseService;
    this.horarioService = horarioService;
  }

  @GetMapping({"", "/"})
  public String dashboard() {
    return "admin/dashboard";
  }

  @GetMapping("/clientes")
  public String clientes() {
    return "admin/clientes";
  }

  @GetMapping("/membresias")
  public String membresias() {
    return "admin/membresias";
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

    model.addAttribute("fechaHoy", LocalDate.now(ZoneId.of("America/Lima")));

    model.addAttribute("clasesProgramadas", horarioService.contarClasesProgramadas());

    model.addAttribute("ocupacionPromedio", horarioService.calcularOcupacionPromedio());

    model.addAttribute("instructoresActivosTotal", horarioService.contarInstructoresActivos());

    model.addAttribute("cuposDisponibles", horarioService.contarCuposDisponibles());

    return "admin/clases";
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
