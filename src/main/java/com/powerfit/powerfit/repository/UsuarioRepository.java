package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

  Optional<Usuario> findByCorreo(String correo);

  boolean existsByCorreo(String correo);

  Optional<Usuario> findByUsuario(String usuario);

  boolean existsByUsuario(String usuario);

  boolean existsByCorreoAndIdUsuarioNot(String correo, Long idUsuario);

  boolean existsByUsuarioAndIdUsuarioNot(String usuario, Long idUsuario);
}
