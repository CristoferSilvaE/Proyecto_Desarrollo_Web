package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.HorarioClase;
import com.powerfit.powerfit.model.Reserva;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.HorarioClaseRepository;
import com.powerfit.powerfit.repository.ReservaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaService {

  private final ReservaRepository reservaRepository;
  private final HorarioClaseRepository horarioRepository;
  private final ClienteRepository clienteRepository;

  private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

  public ReservaService(
      ReservaRepository reservaRepository,
      HorarioClaseRepository horarioRepository,
      ClienteRepository clienteRepository) {

    this.reservaRepository = reservaRepository;
    this.horarioRepository = horarioRepository;
    this.clienteRepository = clienteRepository;
  }

  // ==========================================
  // REGISTRAR RESERVA
  // ==========================================

  @Transactional
  public Reserva reservar(Long idCliente, Long idHorario) {

    Cliente cliente =
        clienteRepository
            .findById(idCliente)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el cliente."));

    if (!"ACTIVO".equals(cliente.getEstado())) {
      throw new IllegalArgumentException("Tu cuenta no está activa.");
    }

    // Bloqueamos el horario para evitar sobreventa.
    HorarioClase horario =
        horarioRepository
            .findByIdForUpdate(idHorario)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el horario."));

    if (!"ACTIVO".equals(horario.getEstado())) {
      throw new IllegalArgumentException("Este horario ya no está disponible.");
    }

    if (!"ACTIVO".equals(horario.getClase().getEstado())
        || !"ACTIVO".equals(horario.getInstructor().getEstado())) {

      throw new IllegalArgumentException("La clase o el instructor no están disponibles.");
    }

    // VALIDAR FECHA Y HORA
    LocalDate hoy = LocalDate.now(ZONA_LIMA);
    LocalTime ahora = LocalTime.now(ZONA_LIMA);

    if (horario.getFecha().isBefore(hoy)
        || (horario.getFecha().equals(hoy) && !horario.getHoraInicio().isAfter(ahora))) {

      throw new IllegalArgumentException("Esta sesión ya comenzó o ha finalizado.");
    }

    // BUSCAR RESERVA ANTERIOR
    Optional<Reserva> anterior =
        reservaRepository.findByCliente_IdClienteAndHorario_IdHorario(idCliente, idHorario);

    if (anterior.isPresent() && "RESERVADA".equals(anterior.get().getEstado())) {

      throw new IllegalArgumentException("Ya tienes una reserva para esta sesión.");
    }

    if (anterior.isPresent() && !"CANCELADA".equals(anterior.get().getEstado())) {

      throw new IllegalArgumentException("No puedes volver a reservar esta sesión.");
    }

    // VERIFICAR CAPACIDAD
    long reservadas = reservaRepository.countByHorario_IdHorarioAndEstado(idHorario, "RESERVADA");

    if (reservadas >= horario.getCapacidad()) {
      throw new IllegalArgumentException("Lo sentimos, ya no quedan cupos disponibles.");
    }

    // CREAR O RECUPERAR RESERVA CANCELADA
    Reserva reserva = anterior.orElseGet(Reserva::new);

    reserva.setCliente(cliente);
    reserva.setHorario(horario);
    reserva.setFechaReserva(LocalDateTime.now(ZONA_LIMA));
    reserva.setEstado("RESERVADA");
    reserva.setAsistencia(Boolean.FALSE);

    return reservaRepository.save(reserva);
  }

  // ==========================================
  // CANCELAR RESERVA
  // ==========================================

  @Transactional
  public void cancelar(Long idCliente, Long idReserva) {

    Reserva reserva =
        reservaRepository
            .findByIdReservaAndCliente_IdCliente(idReserva, idCliente)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró tu reserva."));

    if (!"RESERVADA".equals(reserva.getEstado())) {
      throw new IllegalArgumentException("Esta reserva ya no está activa.");
    }

    HorarioClase horario = reserva.getHorario();

    LocalDate hoy = LocalDate.now(ZONA_LIMA);
    LocalTime ahora = LocalTime.now(ZONA_LIMA);

    if (horario.getFecha().isBefore(hoy)
        || (horario.getFecha().equals(hoy) && !horario.getHoraInicio().isAfter(ahora))) {

      throw new IllegalArgumentException("Ya no puedes cancelar una sesión que comenzó.");
    }

    reserva.setEstado("CANCELADA");

    reservaRepository.save(reserva);
  }
}
