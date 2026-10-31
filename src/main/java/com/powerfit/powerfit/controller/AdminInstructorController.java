package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.service.InstructorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/instructores")
public class AdminInstructorController {

  private final InstructorService instructorService;

  public AdminInstructorController(InstructorService instructorService) {
    this.instructorService = instructorService;
  }

  @PostMapping("/registrar")
  public String registrar(
      @RequestParam String nombres,
      @RequestParam String apellidos,
      @RequestParam String especialidad,
      @RequestParam String telefono,
      @RequestParam String correo,
      RedirectAttributes redirectAttributes) {

    try {

      instructorService.registrar(nombres, apellidos, especialidad, telefono, correo);

      redirectAttributes.addFlashAttribute("mensaje", "Instructor registrado correctamente.");

    } catch (IllegalArgumentException e) {

      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=instructores";
  }
}
