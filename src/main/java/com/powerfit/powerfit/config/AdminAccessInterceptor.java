package com.powerfit.powerfit.config;

import com.powerfit.powerfit.model.Usuario;
import com.powerfit.powerfit.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Valida el rol y el estado en la BD para cada solicitud /admin/**. */
@Component
public class AdminAccessInterceptor implements HandlerInterceptor {

  private final UsuarioRepository usuarioRepository;

  public AdminAccessInterceptor(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    HttpSession session = request.getSession(false);
    if (session == null || !(session.getAttribute("idUsuario") instanceof Long idUsuario)) {
      response.sendRedirect(request.getContextPath() + "/login");
      return false;
    }

    Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
    if (usuario == null || !"ACTIVO".equalsIgnoreCase(usuario.getEstado())
        || usuario.getRol() == null
        || !"ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso reservado a administradores");
      return false;
    }
    return true;
  }
}
