package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.service.HorarioClaseService;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/horarios")
public class AdminHorarioController {

  private final HorarioClaseService horarioService;

  public AdminHorarioController(HorarioClaseService horarioService) {
    this.horarioService = horarioService;
  }

  @PostMapping("/guardar")
  public String guardar(
      @RequestParam(required = false) Long idHorario,
      @RequestParam Long idClase,
      @RequestParam Long idInstructor,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
      @RequestParam Integer capacidad,
      @RequestParam(required = false) String sala,
      RedirectAttributes redirect) {

    try {

      horarioService.guardar(
          idHorario, idClase, idInstructor, fecha, horaInicio, horaFin, capacidad, sala);

      redirect.addFlashAttribute(
          "mensaje",
          idHorario == null
              ? "Horario programado correctamente."
              : "Horario actualizado correctamente.");

    } catch (IllegalArgumentException e) {

      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=horarios";
  }

  @PostMapping("/{id}/estado")
  public String cambiarEstado(
      @PathVariable Long id, @RequestParam String estado, RedirectAttributes redirect) {

    try {

      horarioService.cambiarEstado(id, estado);

      redirect.addFlashAttribute("mensaje", "Estado del horario actualizado correctamente.");

    } catch (IllegalArgumentException e) {

      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=horarios";
  }
}
