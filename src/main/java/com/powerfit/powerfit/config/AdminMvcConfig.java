package com.powerfit.powerfit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminMvcConfig implements WebMvcConfigurer {

  private final AdminAccessInterceptor adminAccessInterceptor;
  private final CambioPasswordInterceptor cambioPasswordInterceptor;

  public AdminMvcConfig(
      AdminAccessInterceptor adminAccessInterceptor,
      CambioPasswordInterceptor cambioPasswordInterceptor) {

    this.adminAccessInterceptor = adminAccessInterceptor;
    this.cambioPasswordInterceptor = cambioPasswordInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {

    // Primero: restringir usuarios con cambio de contraseña pendiente
    registry
        .addInterceptor(cambioPasswordInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/cambiar-password",
            "/logout",
            "/login",
            "/css/**",
            "/js/**",
            "/imagenes/**",
            "/uploads/**",
            "/webjars/**",
            "/favicon.ico",
            "/error");

    // Segundo: proteger el panel administrativo
    registry.addInterceptor(adminAccessInterceptor).addPathPatterns("/admin", "/admin/**");
  }
}
