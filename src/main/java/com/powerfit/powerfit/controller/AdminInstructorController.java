package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.service.InstructorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/instructores")
public class AdminInstructorController {

  private final InstructorService instructorService;

  public AdminInstructorController(InstructorService instructorService) {
    this.instructorService = instructorService;
  }

  // REGISTRAR
  @PostMapping("/registrar")
  public String registrar(
      @RequestParam(required = false) Long idClase,
      @RequestParam String nombres,
      @RequestParam String apellidos,
      @RequestParam String telefono,
      @RequestParam String correo,
      RedirectAttributes redirectAttributes) {

    try {
      instructorService.registrar(idClase, nombres, apellidos, telefono, correo);

      redirectAttributes.addFlashAttribute("mensaje", "Instructor registrado correctamente.");

    } catch (IllegalArgumentException e) {

      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=instructores";
  }

  // EDITAR
  @PostMapping("/editar")
  public String editar(
      @RequestParam Long idInstructor,
      @RequestParam(required = false) Long idClase,
      @RequestParam String nombres,
      @RequestParam String apellidos,
      @RequestParam String telefono,
      @RequestParam String correo,
      RedirectAttributes redirectAttributes) {

    try {
      instructorService.editar(idInstructor, idClase, nombres, apellidos, telefono, correo);

      redirectAttributes.addFlashAttribute("mensaje", "Instructor actualizado correctamente.");

    } catch (IllegalArgumentException e) {

      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=instructores";
  }

  // CAMBIAR ESTADO
  @PostMapping("/{id}/estado")
  public String cambiarEstado(
      @PathVariable Long id, @RequestParam String estado, RedirectAttributes redirectAttributes) {

    try {
      instructorService.cambiarEstado(id, estado);

      redirectAttributes.addFlashAttribute(
          "mensaje", "Estado del instructor actualizado correctamente.");

    } catch (IllegalArgumentException e) {

      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=instructores";
  }
}
