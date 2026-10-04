package com.powerfit.powerfit.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {

    Path directorioPerfiles = Paths.get("uploads", "perfiles").toAbsolutePath().normalize();

    registry
        .addResourceHandler("/uploads/perfiles/**")
        .addResourceLocations(directorioPerfiles.toUri().toString());
  }
}
