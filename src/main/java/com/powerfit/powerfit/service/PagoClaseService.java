package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.PagoClase;
import com.powerfit.powerfit.model.Reserva;
import com.powerfit.powerfit.repository.PagoClaseRepository;
import com.powerfit.powerfit.repository.ReservaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoClaseService {

  private static final ZoneId ZONA = ZoneId.of("America/Lima");

  private static final List<String> METODOS_DIGITALES = List.of("TARJETA", "YAPE", "PLIN");

  private final PagoClaseRepository pagoRepository;
  private final ReservaRepository reservaRepository;

  public PagoClaseService(PagoClaseRepository pagoRepository, ReservaRepository reservaRepository) {

    this.pagoRepository = pagoRepository;
    this.reservaRepository = reservaRepository;
  }

  // ==========================================
  // APROBAR SOLICITUD PARA PAGO DIGITAL
  // ==========================================

  @Transactional
  public void aprobarPagoDigital(Long idPago) {

    PagoClase pago = obtenerPago(idPago);

    validarPendiente(pago);

    if ("EFECTIVO".equalsIgnoreCase(pago.getMetodoPago())) {
      throw new IllegalArgumentException("Esta solicitud corresponde a un pago presencial.");
    }

    if (!METODOS_DIGITALES.contains(normalizar(pago.getMetodoPago()))) {
      throw new IllegalArgumentException("El método de pago no es válido.");
    }

    validarCliente(pago.getReserva());
    validarHorario(pago.getReserva());

    pago.setEstadoPago("AUTORIZADO");
    pago.setObservacion("Solicitud aprobada. Pendiente de pago del cliente.");

    pago.getReserva().setEstado("PENDIENTE_PAGO");

    reservaRepository.save(pago.getReserva());
    pagoRepository.save(pago);
  }

  // ==========================================
  // REGISTRAR PAGO PRESENCIAL EN EFECTIVO
  // ==========================================

  @Transactional
  public void registrarPagoEfectivo(Long idPago) {

    PagoClase pago = obtenerPago(idPago);

    validarPendiente(pago);

    if (!"EFECTIVO".equalsIgnoreCase(pago.getMetodoPago())) {
      throw new IllegalArgumentException("La solicitud no corresponde a un pago en efectivo.");
    }

    validarCliente(pago.getReserva());
    validarHorario(pago.getReserva());

    confirmarPago(pago, "EFECTIVO");
  }

  // ==========================================
  // SIMULAR PAGO DIGITAL DEL CLIENTE
  // ==========================================

  @Transactional
  public void simularPagoDigital(Long idPago, Long idCliente, String metodoPago) {

    PagoClase pago = obtenerPago(idPago);

    if (idCliente == null
        || pago.getReserva().getCliente() == null
        || !pago.getReserva().getCliente().getIdCliente().equals(idCliente)) {

      throw new IllegalArgumentException("No se encontró la solicitud de pago.");
    }

    if (!"AUTORIZADO".equals(pago.getEstadoPago())
        || !"PENDIENTE_PAGO".equals(pago.getReserva().getEstado())) {

      throw new IllegalArgumentException("Esta solicitud todavía no está autorizada para el pago.");
    }

    String metodo = normalizar(metodoPago);

    if (!METODOS_DIGITALES.contains(metodo)) {
      throw new IllegalArgumentException("Selecciona un método de pago válido.");
    }

    validarCliente(pago.getReserva());
    validarHorario(pago.getReserva());

    confirmarPago(pago, metodo);
  }

  // ==========================================
  // RECHAZAR SOLICITUD
  // ==========================================

  @Transactional
  public void rechazarSolicitud(Long idPago, String motivo) {

    PagoClase pago = obtenerPago(idPago);

    if (!List.of("PENDIENTE", "AUTORIZADO").contains(pago.getEstadoPago())) {

      throw new IllegalArgumentException("Esta solicitud ya no puede rechazarse.");
    }

    motivo = motivo == null ? "" : motivo.trim();

    if (motivo.length() > 180) {
      throw new IllegalArgumentException("El motivo no puede superar los 180 caracteres.");
    }

    String observacion =
        motivo.isBlank()
            ? "Solicitud rechazada por administración."
            : "Solicitud rechazada: " + motivo;

    pago.setEstadoPago("RECHAZADO");
    pago.setObservacion(observacion);

    pago.getReserva().setEstado("RECHAZADA");

    reservaRepository.save(pago.getReserva());
    pagoRepository.save(pago);
  }

  // ==========================================
  // PROCESAR REEMBOLSO SIMULADO
  // ==========================================

  @Transactional
  public void procesarReembolso(Long idPago) {

    PagoClase pago = obtenerPago(idPago);

    if (!"REEMBOLSO_PENDIENTE".equals(pago.getEstadoPago())) {
      throw new IllegalArgumentException("Este pago no tiene un reembolso pendiente.");
    }

    if (!"PAGO".equals(pago.getTipoReserva())
        || !"CANCELADA".equals(pago.getReserva().getEstado())) {

      throw new IllegalArgumentException(
          "La operación no cumple las condiciones para el reembolso.");
    }

    if (pago.getFechaPago() == null) {
      throw new IllegalArgumentException("No se encontró la fecha del pago original.");
    }

    pago.setEstadoPago("REEMBOLSADO");

    pago.setObservacion(
        "Reembolso simulado procesado por administración el "
            + LocalDateTime.now(ZONA)
            + ". No se realizó transferencia real.");

    pagoRepository.save(pago);
  }

  // ==========================================
  // CONFIRMAR PAGO
  // ==========================================

  private void confirmarPago(PagoClase pago, String metodo) {

    pago.setMetodoPago(metodo);
    pago.setEstadoPago("PAGADO");
    pago.setFechaPago(LocalDateTime.now(ZONA));
    pago.setObservacion("Pago confirmado correctamente.");

    pago.getReserva().setEstado("RESERVADA");

    reservaRepository.save(pago.getReserva());
    pagoRepository.save(pago);
  }

  // ==========================================
  // VALIDACIONES
  // ==========================================

  private PagoClase obtenerPago(Long idPago) {

    return pagoRepository
        .findById(idPago)
        .orElseThrow(() -> new IllegalArgumentException("No se encontró el pago de la clase."));
  }

  private void validarPendiente(PagoClase pago) {

    if (!"PENDIENTE".equals(pago.getEstadoPago())
        || !"PENDIENTE".equals(pago.getReserva().getEstado())) {

      throw new IllegalArgumentException("La solicitud ya fue procesada.");
    }

    if (!"PAGO".equals(pago.getTipoReserva())) {
      throw new IllegalArgumentException("Esta reserva no requiere un pago adicional.");
    }
  }

  private void validarCliente(Reserva reserva) {

    if (reserva.getCliente() == null
        || !"ACTIVO".equalsIgnoreCase(reserva.getCliente().getEstado())
        || reserva.getCliente().getUsuario() == null
        || !"ACTIVO".equalsIgnoreCase(reserva.getCliente().getUsuario().getEstado())) {

      throw new IllegalArgumentException("El cliente no tiene una cuenta habilitada.");
    }
  }

  private void validarHorario(Reserva reserva) {

    LocalDate fecha = reserva.getHorario().getFecha();
    LocalTime hora = reserva.getHorario().getHoraInicio();

    LocalDate hoy = LocalDate.now(ZONA);
    LocalTime ahora = LocalTime.now(ZONA);

    if (fecha.isBefore(hoy) || (fecha.equals(hoy) && !hora.isAfter(ahora))) {

      throw new IllegalArgumentException("La sesión ya comenzó o ha finalizado.");
    }

    if (!"ACTIVO".equalsIgnoreCase(reserva.getHorario().getEstado())) {

      throw new IllegalArgumentException("El horario ya no está disponible.");
    }
  }

  private String normalizar(String valor) {

    return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
  }
}
