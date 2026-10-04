package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Reserva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

  boolean existsByCliente_IdClienteAndHorarioClase_IdHorario(Long idCliente, Long idHorario);

  List<Reserva> findByCliente_IdClienteOrderByFechaReservaDesc(Long idCliente);

  long countByHorarioClase_IdHorarioAndEstado(Long idHorario, String estado);
}
