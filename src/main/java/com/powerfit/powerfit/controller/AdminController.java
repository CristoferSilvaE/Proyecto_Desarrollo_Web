package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.service.ClaseService;
import com.powerfit.powerfit.service.InstructorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

  private final InstructorService instructorService;
  private final ClaseService claseService;

  public AdminController(InstructorService instructorService, ClaseService claseService) {

    this.instructorService = instructorService;
    this.claseService = claseService;
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

    model.addAttribute("instructores", instructorService.listarTodos());

    model.addAttribute("clases", claseService.listar());

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
