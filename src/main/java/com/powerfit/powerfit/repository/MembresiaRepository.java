package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Membresia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembresiaRepository extends JpaRepository<Membresia, Long> {

  List<Membresia> findByCliente_IdClienteOrderByFechaSolicitudDesc(Long idCliente);

  Optional<Membresia> findFirstByCliente_IdClienteAndEstadoOrderByFechaSolicitudDesc(
      Long idCliente, String estado);

  List<Membresia> findByEstado(String estado);
}
