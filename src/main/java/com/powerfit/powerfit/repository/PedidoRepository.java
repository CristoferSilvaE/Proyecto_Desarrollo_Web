package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

  List<Pedido> findByCliente_IdClienteOrderByFechaPedidoDesc(Long idCliente);

  List<Pedido> findByEstadoOrderByFechaPedidoDesc(String estado);

  List<Pedido> findByEstadoPagoOrderByFechaPedidoDesc(String estadoPago);
}
