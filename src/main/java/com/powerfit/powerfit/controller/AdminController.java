package com.powerfit.powerfit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Solo muestra las vistas iniciales. La lógica CRUD se implementará después. */
@Controller
@RequestMapping("/admin")
public class AdminController {

  @GetMapping({"", "/"})
  public String dashboard() { return "admin/dashboard"; }

  @GetMapping("/clientes")
  public String clientes() { return "admin/clientes"; }

  @GetMapping("/membresias")
  public String membresias() { return "admin/membresias"; }

  @GetMapping("/clases")
  public String clases() { return "admin/clases"; }

  @GetMapping("/productos")
  public String productos() { return "admin/productos"; }

  @GetMapping("/pedidos")
  public String pedidos() { return "admin/pedidos"; }
}
