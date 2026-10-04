package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {

  Optional<Rol> findByNombre(String nombre);
}
