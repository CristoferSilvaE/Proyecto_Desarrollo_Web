package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.repository.ProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TiendaController {

  private final ProductoRepository productoRepository;

  public TiendaController(ProductoRepository productoRepository) {
    this.productoRepository = productoRepository;
  }

  @GetMapping("/tienda")
  public String tienda(Model model) {

    model.addAttribute("productos", productoRepository.findByEstadoOrderByNombreAsc("ACTIVO"));

    return "tienda";
  }
}
