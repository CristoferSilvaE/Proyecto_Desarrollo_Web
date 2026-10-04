package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Usuario;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SesionControllerAdvice {

  private final ClienteRepository clienteRepository;
  private final UsuarioRepository usuarioRepository;

  public SesionControllerAdvice(
      ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {

    this.clienteRepository = clienteRepository;
    this.usuarioRepository = usuarioRepository;
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
}
