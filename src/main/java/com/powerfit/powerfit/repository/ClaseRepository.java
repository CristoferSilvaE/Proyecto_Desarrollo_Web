package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Clase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaseRepository extends JpaRepository<Clase, Long> {

  List<Clase> findByEstadoOrderByNombreAsc(String estado);

  List<Clase> findByCategoriaAndEstadoOrderByNombreAsc(String categoria, String estado);
}
