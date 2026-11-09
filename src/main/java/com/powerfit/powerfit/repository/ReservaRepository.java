package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Reserva;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

  // Verificar si ya tiene una reserva activa
  boolean existsByCliente_IdClienteAndHorario_IdHorarioAndEstado(
      Long idCliente, Long idHorario, String estado);

  // Buscar reserva de un cliente para un horario
  Optional<Reserva> findByCliente_IdClienteAndHorario_IdHorario(Long idCliente, Long idHorario);

  // Historial de reservas
  List<Reserva> findByCliente_IdClienteOrderByFechaReservaDesc(Long idCliente);

  // Buscar reserva perteneciente a un cliente
  Optional<Reserva> findByIdReservaAndCliente_IdCliente(Long idReserva, Long idCliente);

  // Contar reservas activas por horario
  long countByHorario_IdHorarioAndEstado(Long idHorario, String estado);

  // Contar reservas confirmadas y solicitudes pendientes
  long countByHorario_IdHorarioAndEstadoIn(Long idHorario, List<String> estados);

  // LISTAR CLIENTES RESERVADOS POR HORARIO
  List<Reserva> findByHorario_IdHorarioAndEstadoOrderByFechaReservaAsc(
      Long idHorario, String estado);
}
