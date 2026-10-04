package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.HorarioClase;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioClaseRepository extends JpaRepository<HorarioClase, Long> {

  List<HorarioClase> findByEstadoOrderByFechaAscHoraInicioAsc(String estado);

  List<HorarioClase> findByFechaGreaterThanEqualAndEstadoOrderByFechaAscHoraInicioAsc(
      LocalDate fecha, String estado);

  List<HorarioClase> findByClase_IdClaseAndEstadoOrderByFechaAscHoraInicioAsc(
      Long idClase, String estado);
}
