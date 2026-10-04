package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.PagoMembresia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoMembresiaRepository extends JpaRepository<PagoMembresia, Long> {

  List<PagoMembresia> findByMembresia_IdMembresiaOrderByFechaPagoDesc(Long idMembresia);
}
