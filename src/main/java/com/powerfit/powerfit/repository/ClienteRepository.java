package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Cliente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

  Optional<Cliente> findByUsuario_IdUsuario(Long idUsuario);

  boolean existsByDni(String dni);
}
