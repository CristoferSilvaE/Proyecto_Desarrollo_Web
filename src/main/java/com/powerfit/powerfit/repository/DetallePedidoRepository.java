package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.DetallePedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

  List<DetallePedido> findByPedido_IdPedido(Long idPedido);

  @Query(
      """
      SELECT d FROM DetallePedido d
      JOIN FETCH d.producto
      WHERE d.pedido.idPedido = :idPedido
      ORDER BY d.producto.idProducto
      """)
  List<DetallePedido> buscarDetalles(@Param("idPedido") Long idPedido);
}
