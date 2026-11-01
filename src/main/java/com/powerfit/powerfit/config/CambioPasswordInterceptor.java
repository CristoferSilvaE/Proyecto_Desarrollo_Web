package com.powerfit.powerfit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CambioPasswordInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    HttpSession session = request.getSession(false);

    if (session != null && session.getAttribute("idUsuarioCambioPassword") instanceof Long) {

      response.sendRedirect(request.getContextPath() + "/login?cambioPassword");

      return false;
    }

    return true;
  }
}
