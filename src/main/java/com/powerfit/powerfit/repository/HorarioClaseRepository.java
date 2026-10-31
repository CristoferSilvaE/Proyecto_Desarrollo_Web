package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.HorarioClase;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorarioClaseRepository extends JpaRepository<HorarioClase, Long> {

  List<HorarioClase> findAllByOrderByFechaAscHoraInicioAsc();

  List<HorarioClase> findByEstadoOrderByFechaAscHoraInicioAsc(String estado);

  List<HorarioClase> findByFechaGreaterThanEqualAndEstadoOrderByFechaAscHoraInicioAsc(
      LocalDate fecha, String estado);

  List<HorarioClase> findByFechaAndEstadoOrderByHoraInicioAsc(LocalDate fecha, String estado);

  List<HorarioClase> findByClase_IdClaseAndEstadoOrderByFechaAscHoraInicioAsc(
      Long idClase, String estado);

  // BLOQUEAR HORARIO DURANTE EL REGISTRO DE UNA RESERVA
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT h FROM HorarioClase h WHERE h.idHorario = :idHorario")
  Optional<HorarioClase> findByIdForUpdate(@Param("idHorario") Long idHorario);
}
