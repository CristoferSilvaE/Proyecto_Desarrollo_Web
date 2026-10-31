package com.powerfit.powerfit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Configuración independiente: no modifica WebConfig de las imágenes de perfil. */
@Configuration
public class AdminMvcConfig implements WebMvcConfigurer {

  private final AdminAccessInterceptor adminAccessInterceptor;

  public AdminMvcConfig(AdminAccessInterceptor adminAccessInterceptor) {
    this.adminAccessInterceptor = adminAccessInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(adminAccessInterceptor).addPathPatterns("/admin", "/admin/**");
  }
}
