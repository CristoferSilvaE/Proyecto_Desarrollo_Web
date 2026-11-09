package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.PagoClase;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagoClaseRepository extends JpaRepository<PagoClase, Long> {

  // Historial de pagos y beneficios de un cliente
  List<PagoClase> findByReserva_Cliente_IdClienteOrderByFechaRegistroDesc(Long idCliente);

  // Historial de operaciones de una reserva
  List<PagoClase> findByReserva_IdReservaOrderByFechaRegistroDesc(Long idReserva);

  // Ultima operacion registrada para una reserva
  Optional<PagoClase> findFirstByReserva_IdReservaOrderByFechaRegistroDesc(Long idReserva);

  // Contar beneficios gratuitos utilizados en un periodo
  @Query(
      """
      SELECT COUNT(p)
      FROM PagoClase p
      WHERE p.reserva.cliente.idCliente = :idCliente
        AND p.reserva.horario.fecha BETWEEN :desde AND :hasta
        AND p.tipoReserva = :tipoReserva
        AND p.estadoPago = :estadoPago
      """)
  long contarBeneficiosMensuales(
      @Param("idCliente") Long idCliente,
      @Param("desde") LocalDate desde,
      @Param("hasta") LocalDate hasta,
      @Param("tipoReserva") String tipoReserva,
      @Param("estadoPago") String estadoPago);

  // Solicitudes pendientes y pagos autorizados
  List<PagoClase> findByEstadoPagoInOrderByFechaRegistroDesc(List<String> estadosPago);

  // Contar solicitudes pendientes de revisión
  long countByEstadoPago(String estadoPago);
}
