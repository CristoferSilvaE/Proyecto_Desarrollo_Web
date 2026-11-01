package com.powerfit.powerfit.dto;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Usuario;

public class UsuarioAdminDTO {

  private final Usuario usuario;
  private final Cliente cliente;

  public UsuarioAdminDTO(Usuario usuario, Cliente cliente) {
    this.usuario = usuario;
    this.cliente = cliente;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public String getNombreCompleto() {

    if (cliente != null) {
      return cliente.getNombres() + " " + cliente.getApellidos();
    }

    if (usuario.getRol() != null
        && "ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) {

      return "Administrador del sistema";
    }

    return "Cuenta del sistema";
  }

  public String getDni() {
    return cliente != null ? cliente.getDni() : "—";
  }
}
