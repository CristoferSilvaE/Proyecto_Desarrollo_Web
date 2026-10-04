package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.repository.PlanMembresiaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavegacionController {

  private final PlanMembresiaRepository planMembresiaRepository;

  public NavegacionController(PlanMembresiaRepository planMembresiaRepository) {
    this.planMembresiaRepository = planMembresiaRepository;
  }

  @GetMapping("/")
  public String inicio(Model model) {

    model.addAttribute("planes", planMembresiaRepository.findByEstadoOrderByPrecioAsc("ACTIVO"));

    return "index";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/registro")
  public String registro() {
    return "registro";
  }

  @GetMapping("/membresia")
  public String membresia() {
    return "membresia";
  }

  @GetMapping("/clases")
  public String clases() {
    return "reservaClases";
  }

  @GetMapping("/carrito")
  public String carrito() {
    return "carrito";
  }

  @GetMapping("/perfil")
  public String perfil() {
    return "perfilUsuario";
  }

  @GetMapping("/checkout")
  public String checkout() {
    return "checkout";
  }

  @GetMapping("/pedidos")
  public String pedidos() {
    return "pedidos";
  }

  @GetMapping("/seguimiento")
  public String seguimiento() {
    return "seguimientoFisico";
  }
}
