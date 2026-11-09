package com.powerfit.powerfit.service;

import com.powerfit.powerfit.config.ReglasClases;
import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.HorarioClase;
import com.powerfit.powerfit.model.Membresia;
import com.powerfit.powerfit.model.PagoClase;
import com.powerfit.powerfit.model.Reserva;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.HorarioClaseRepository;
import com.powerfit.powerfit.repository.MembresiaRepository;
import com.powerfit.powerfit.repository.PagoClaseRepository;
import com.powerfit.powerfit.repository.ReservaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaService {

  private final ReservaRepository reservaRepository;
  private final HorarioClaseRepository horarioRepository;
  private final ClienteRepository clienteRepository;
  private final MembresiaRepository membresiaRepository;
  private final PagoClaseRepository pagoClaseRepository;

  private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

  private static final List<String> ESTADOS_QUE_OCUPAN_CUPO =
      List.of("RESERVADA", "PENDIENTE", "PENDIENTE_PAGO");

  public ReservaService(
      ReservaRepository reservaRepository,
      HorarioClaseRepository horarioRepository,
      ClienteRepository clienteRepository,
      MembresiaRepository membresiaRepository,
      PagoClaseRepository pagoClaseRepository) {

    this.reservaRepository = reservaRepository;
    this.horarioRepository = horarioRepository;
    this.clienteRepository = clienteRepository;
    this.membresiaRepository = membresiaRepository;
    this.pagoClaseRepository = pagoClaseRepository;
  }

  // ==========================================
  // REGISTRAR RESERVA
  // ==========================================

  @Transactional
  public Reserva reservar(Long idCliente, Long idHorario, String metodoPago) {

    Cliente cliente =
        clienteRepository
            .findById(idCliente)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el cliente."));

    if (!"ACTIVO".equals(cliente.getEstado())) {
      throw new IllegalArgumentException("Tu cuenta no está activa.");
    }

    // Bloquear horario para evitar sobreventa
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

    // Validar fecha y hora
    LocalDate hoy = LocalDate.now(ZONA_LIMA);
    LocalTime ahora = LocalTime.now(ZONA_LIMA);

    if (horario.getFecha().isBefore(hoy)
        || (horario.getFecha().equals(hoy) && !horario.getHoraInicio().isAfter(ahora))) {

      throw new IllegalArgumentException("Esta sesión ya comenzó o ha finalizado.");
    }

    // Verificar reserva anterior
    Optional<Reserva> anterior =
        reservaRepository.findByCliente_IdClienteAndHorario_IdHorario(idCliente, idHorario);

    if (anterior.isPresent() && ESTADOS_QUE_OCUPAN_CUPO.contains(anterior.get().getEstado())) {

      throw new IllegalArgumentException("Ya tienes una reserva o solicitud para esta sesión.");
    }

    if (anterior.isPresent()
        && !List.of("CANCELADA", "RECHAZADA").contains(anterior.get().getEstado())) {

      throw new IllegalArgumentException("No puedes volver a reservar esta sesión.");
    }

    // Verificar capacidad
    long ocupadas =
        reservaRepository.countByHorario_IdHorarioAndEstadoIn(idHorario, ESTADOS_QUE_OCUPAN_CUPO);

    if (ocupadas >= horario.getCapacidad()) {
      throw new IllegalArgumentException("Lo sentimos, ya no quedan cupos disponibles.");
    }

    // Buscar membresía vigente para la fecha de la clase
    Membresia membresia = buscarMembresiaVigente(idCliente, horario.getFecha());

    // Determinar si corresponde beneficio gratuito
    boolean gratuita = tieneBeneficioDisponible(membresia, idCliente, horario.getFecha());

    // Crear o recuperar reserva cancelada
    Reserva reserva = anterior.orElseGet(Reserva::new);

    reserva.setCliente(cliente);
    reserva.setHorario(horario);
    reserva.setFechaReserva(LocalDateTime.now(ZONA_LIMA));
    reserva.setEstado(gratuita ? "RESERVADA" : "PENDIENTE");
    reserva.setAsistencia(Boolean.FALSE);

    Reserva guardada = reservaRepository.save(reserva);

    // Registrar operación de pago o beneficio
    PagoClase pago = new PagoClase();

    pago.setReserva(guardada);
    pago.setMembresia(membresia);

    if (gratuita) {

      pago.setTipoReserva("GRATUITA");
      pago.setMonto(BigDecimal.ZERO);
      pago.setMetodoPago("BENEFICIO_PLAN");
      pago.setEstadoPago("BONIFICADO");

      pago.setObservacion("Clase gratuita incluida en el plan de membresía.");

    } else {

      String metodo = normalizar(metodoPago);

      validarMetodoPago(metodo);

      pago.setTipoReserva("PAGO");
      pago.setMonto(ReglasClases.PRECIO_CLASE_ADICIONAL);
      pago.setMetodoPago(metodo);
      pago.setEstadoPago("PENDIENTE");

      pago.setObservacion("Solicitud pendiente de revisión administrativa.");
    }

    pagoClaseRepository.save(pago);

    return guardada;
  }

  // ==========================================
  // BUSCAR MEMBRESÍA VIGENTE
  // ==========================================

  private Membresia buscarMembresiaVigente(Long idCliente, LocalDate fechaClase) {

    List<Membresia> historial =
        membresiaRepository.findByCliente_IdClienteOrderByFechaSolicitudDesc(idCliente);

    for (Membresia membresia : historial) {

      if (!"ACTIVA".equalsIgnoreCase(membresia.getEstado())) {
        continue;
      }

      if (membresia.getFechaInicio() == null || membresia.getFechaFin() == null) {
        continue;
      }

      boolean dentroVigencia =
          !fechaClase.isBefore(membresia.getFechaInicio())
              && !fechaClase.isAfter(membresia.getFechaFin());

      if (dentroVigencia) {
        return membresia;
      }
    }

    return null;
  }

  // ==========================================
  // VALIDAR BENEFICIO GRATUITO
  // ==========================================

  private boolean tieneBeneficioDisponible(
      Membresia membresia, Long idCliente, LocalDate fechaClase) {

    if (membresia == null || membresia.getPlan() == null) {
      return false;
    }

    int limite = ReglasClases.obtenerClasesGratuitas(membresia.getPlan().getNombre());

    if (limite <= 0) {
      return false;
    }

    LocalDate desde = fechaClase.withDayOfMonth(1);
    LocalDate hasta = fechaClase.withDayOfMonth(fechaClase.lengthOfMonth());

    long utilizadas =
        pagoClaseRepository.contarBeneficiosMensuales(
            idCliente, desde, hasta, "GRATUITA", "BONIFICADO");

    return utilizadas < limite;
  }

  // ==========================================
  // VALIDAR MÉTODO DE PAGO
  // ==========================================

  private void validarMetodoPago(String metodo) {

    if (!List.of("TARJETA", "YAPE", "PLIN", "EFECTIVO").contains(metodo)) {

      throw new IllegalArgumentException("Selecciona un método de pago válido.");
    }
  }

  private String normalizar(String valor) {
    return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
  }

  // ==========================================
  // CANCELAR RESERVA
  // ==========================================

  // ==========================================
  // CANCELAR RESERVA
  // ==========================================

  @Transactional
  public void cancelar(Long idCliente, Long idReserva) {

    Reserva reserva =
        reservaRepository
            .findByIdReservaAndCliente_IdCliente(idReserva, idCliente)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró tu reserva."));

    if (!ESTADOS_QUE_OCUPAN_CUPO.contains(reserva.getEstado())) {
      throw new IllegalArgumentException("Esta reserva ya no está activa.");
    }

    HorarioClase horario = reserva.getHorario();

    LocalDateTime ahora = LocalDateTime.now(ZONA_LIMA);

    LocalDateTime inicioClase = LocalDateTime.of(horario.getFecha(), horario.getHoraInicio());

    // VALIDAR QUE LA CLASE NO HAYA COMENZADO
    if (!inicioClase.isAfter(ahora)) {
      throw new IllegalArgumentException("Ya no puedes cancelar una sesión que comenzó.");
    }

    // VALIDAR ANTICIPACIÓN DE 24 HORAS
    boolean anticipacionValida =
        !inicioClase.isBefore(ahora.plusHours(ReglasClases.HORAS_ANTICIPACION_CANCELACION));

    // OBTENER ÚLTIMO PAGO DE LA RESERVA
    Optional<PagoClase> ultimoPago =
        pagoClaseRepository.findFirstByReserva_IdReservaOrderByFechaRegistroDesc(idReserva);

    if (ultimoPago.isPresent()) {

      PagoClase pago = ultimoPago.get();

      switch (pago.getEstadoPago()) {

        // SOLICITUDES SIN PAGO REALIZADO
        case "PENDIENTE", "AUTORIZADO" -> {
          pago.setEstadoPago("CANCELADO");

          pago.setObservacion("Reserva cancelada por el cliente. No se realizó ningún cobro.");
        }

        // BENEFICIO GRATUITO DE MEMBRESÍA
        case "BONIFICADO" -> {
          if (anticipacionValida) {

            pago.setEstadoPago("REINTEGRADO");

            pago.setObservacion(
                "Reserva cancelada con al menos 24 horas de anticipación. "
                    + "Beneficio mensual reintegrado.");

          } else {

            pago.setObservacion(
                "Reserva cancelada con menos de 24 horas de anticipación. "
                    + "El beneficio mensual se considera utilizado.");
          }
        }

        // PAGO REALIZADO
        case "PAGADO" -> {
          if (anticipacionValida) {

            pago.setEstadoPago("REEMBOLSO_PENDIENTE");

            pago.setObservacion(
                "Reserva cancelada con al menos 24 horas de anticipación. "
                    + "Reembolso simulado pendiente de aprobación administrativa.");

          } else {

            pago.setObservacion(
                "Reserva cancelada con menos de 24 horas de anticipación. "
                    + "No corresponde devolución.");
          }
        }

        default ->
            throw new IllegalArgumentException(
                "El estado del pago no permite cancelar esta reserva.");
      }

      pagoClaseRepository.save(pago);
    }

    // LIBERAR CUPO
    reserva.setEstado("CANCELADA");

    reservaRepository.save(reserva);
  }
}
