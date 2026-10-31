package com.powerfit.powerfit.config;

import com.powerfit.powerfit.model.Rol;
import com.powerfit.powerfit.model.Usuario;
import com.powerfit.powerfit.repository.RolRepository;
import com.powerfit.powerfit.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@PropertySource("classpath:admin.properties")
public class AdminInitializer {

  @Value("${powerfit.admin.enabled:false}")
  private boolean enabled;

  @Value("${powerfit.admin.correo}")
  private String correo;

  @Value("${powerfit.admin.password}")
  private String password;

  @Bean
  CommandLineRunner crearAdministrador(
      UsuarioRepository usuarioRepository, RolRepository rolRepository) {

    return args -> {
      if (!enabled) {
        return;
      }

      String correoAdmin = correo.trim().toLowerCase();

      // Evitar duplicar el administrador
      if (usuarioRepository.existsByCorreo(correoAdmin)) {
        System.out.println("ADMIN: La cuenta ya existe.");
        return;
      }

      // Verificar que exista el rol
      Rol rolAdmin =
          rolRepository
              .findByNombre("ADMINISTRADOR")
              .orElseThrow(
                  () -> new IllegalStateException("No existe el rol ADMINISTRADOR en MySQL."));

      // Crear administrador
      Usuario admin = new Usuario();

      admin.setCorreo(correoAdmin);
      admin.setPasswordHash(new BCryptPasswordEncoder().encode(password));

      admin.setRol(rolAdmin);
      admin.setEstado("ACTIVO");
      admin.setCambioPasswordPendiente(false);

      usuarioRepository.save(admin);

      System.out.println("ADMIN: Administrador creado correctamente.");
    };
  }
}
