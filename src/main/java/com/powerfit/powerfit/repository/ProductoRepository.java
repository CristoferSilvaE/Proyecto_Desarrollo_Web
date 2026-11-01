package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Producto;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

  List<Producto> findByEstadoOrderByNombreAsc(String estado);

  List<Producto> findByCategoriaAndEstadoOrderByNombreAsc(String categoria, String estado);

  Optional<Producto> findBySku(String sku);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Producto p WHERE p.idProducto = :id")
  Optional<Producto> buscarParaCompra(@Param("id") Long id);
}
