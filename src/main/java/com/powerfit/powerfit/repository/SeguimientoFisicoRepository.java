package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.SeguimientoFisico;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeguimientoFisicoRepository extends JpaRepository<SeguimientoFisico, Long> {

  List<SeguimientoFisico> findByCliente_IdClienteOrderByFechaRegistroDesc(Long idCliente);

  Optional<SeguimientoFisico> findFirstByCliente_IdClienteOrderByFechaRegistroDesc(Long idCliente);

  Optional<SeguimientoFisico> findByIdSeguimientoAndCliente_IdCliente(
      Long idSeguimiento, Long idCliente);
}
