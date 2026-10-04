package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.DetallePedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

  List<DetallePedido> findByPedido_IdPedido(Long idPedido);
}
