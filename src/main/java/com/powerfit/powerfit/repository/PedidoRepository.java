package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Pedido;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

  List<Pedido> findByCliente_IdClienteOrderByFechaPedidoDesc(Long idCliente);

  List<Pedido> findByEstadoOrderByFechaPedidoDesc(String estado);

  List<Pedido> findByEstadoPagoOrderByFechaPedidoDesc(String estadoPago);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Pedido p WHERE p.idPedido = :id")
  Optional<Pedido> buscarParaModificar(@Param("id") Long id);
}
