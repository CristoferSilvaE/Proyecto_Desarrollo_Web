package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

  List<Producto> findByEstadoOrderByNombreAsc(String estado);

  List<Producto> findByCategoriaAndEstadoOrderByNombreAsc(String categoria, String estado);

  Optional<Producto> findBySku(String sku);
}
