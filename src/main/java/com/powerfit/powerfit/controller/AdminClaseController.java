package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.service.ClaseService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/clases")
public class AdminClaseController {

  private final ClaseService claseService;

  public AdminClaseController(ClaseService claseService) {
    this.claseService = claseService;
  }

  @PostMapping("/guardar")
  public String guardar(
      @RequestParam(required = false) Long id,
      @RequestParam String nombre,
      @RequestParam String categoria,
      @RequestParam(required = false) String descripcion,
      @RequestParam(required = false) MultipartFile imagen,
      RedirectAttributes redirect) {

    try {
      claseService.guardar(id, nombre, categoria, descripcion, imagen);

      redirect.addFlashAttribute("mensaje", "Clase guardada correctamente.");

    } catch (Exception e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=clases";
  }

  @PostMapping("/{id}/estado")
  public String cambiarEstado(@PathVariable Long id, RedirectAttributes redirect) {

    try {
      claseService.cambiarEstado(id);

      redirect.addFlashAttribute("mensaje", "Estado actualizado correctamente.");

    } catch (IllegalArgumentException e) {
      redirect.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/admin/clases?tab=clases";
  }
}
