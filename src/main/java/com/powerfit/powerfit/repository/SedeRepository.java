package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Sede;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SedeRepository extends JpaRepository<Sede, Long> {

  List<Sede> findByEstadoOrderByNombreAsc(String estado);
}
