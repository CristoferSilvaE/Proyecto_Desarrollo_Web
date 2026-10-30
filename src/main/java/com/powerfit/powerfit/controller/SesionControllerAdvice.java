package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Usuario;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.UsuarioRepository;
import com.powerfit.powerfit.service.CarritoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SesionControllerAdvice {

  private final ClienteRepository clienteRepository;
  private final UsuarioRepository usuarioRepository;
  private final CarritoService carritoService;

  public SesionControllerAdvice(
      ClienteRepository clienteRepository,
      UsuarioRepository usuarioRepository,
      CarritoService carritoService) {

    this.clienteRepository = clienteRepository;
    this.usuarioRepository = usuarioRepository;
    this.carritoService = carritoService;
  }

  @ModelAttribute("cliente")
  public Cliente clienteSesion(HttpSession session) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return null;
    }

    return clienteRepository.findById(idCliente).orElse(null);
  }

  @ModelAttribute("usuario")
  public Usuario usuarioSesion(HttpSession session) {

    Long idUsuario = (Long) session.getAttribute("idUsuario");

    if (idUsuario == null) {
      return null;
    }

    return usuarioRepository.findById(idUsuario).orElse(null);
  }

  @ModelAttribute("cantidadCarrito")
  public int cantidadCarrito(HttpSession session) {

    if (session.getAttribute("idCliente") == null) {
      return 0;
    }

    return carritoService.obtenerCantidadTotal(session);
  }
}
