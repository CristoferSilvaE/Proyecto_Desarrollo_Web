package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.PlanMembresia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanMembresiaRepository extends JpaRepository<PlanMembresia, Long> {

  List<PlanMembresia> findByEstadoOrderByPrecioAsc(String estado);

  Optional<PlanMembresia> findByNombre(String nombre);
}
