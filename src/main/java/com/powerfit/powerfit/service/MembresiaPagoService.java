package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Membresia;
import com.powerfit.powerfit.model.PagoMembresia;
import com.powerfit.powerfit.repository.MembresiaRepository;
import com.powerfit.powerfit.repository.PagoMembresiaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembresiaPagoService {

  private static final ZoneId ZONA = ZoneId.of("America/Lima");

  private final MembresiaRepository membresiaRepository;
  private final PagoMembresiaRepository pagoRepository;

  public MembresiaPagoService(
      MembresiaRepository membresiaRepository, PagoMembresiaRepository pagoRepository) {

    this.membresiaRepository = membresiaRepository;
    this.pagoRepository = pagoRepository;
  }

  // ==========================================
  // APROBAR SOLICITUD PARA PAGO DIGITAL
  // ==========================================

  @Transactional
  public void aprobarPagoDigital(Long idMembresia) {

    Membresia membresia = obtenerMembresia(idMembresia);

    validarEstado(membresia, "PENDIENTE");

    validarCliente(membresia.getCliente());

    if (!esPagoDigital(membresia.getMetodoPagoPreferido())) {
      throw new IllegalArgumentException("Esta solicitud no corresponde a un pago digital.");
    }

    validarSinOtraActiva(membresia);
    validarSinPagoRegistrado(idMembresia);
    validarDuracionYPrecio(membresia);

    membresia.setEstado("PENDIENTE_PAGO");

    membresiaRepository.save(membresia);
  }

  // ==========================================
  // REGISTRAR PAGO PRESENCIAL EN EFECTIVO
  // ==========================================

  @Transactional
  public void registrarPagoEfectivo(Long idMembresia, LocalDate fechaInicio) {

    Membresia membresia = obtenerMembresia(idMembresia);

    validarEstado(membresia, "PENDIENTE");

    validarCliente(membresia.getCliente());

    if (!"EFECTIVO".equals(normalizar(membresia.getMetodoPagoPreferido()))) {

      throw new IllegalArgumentException("Esta solicitud no corresponde a un pago en efectivo.");
    }

    validarFechaInicio(fechaInicio);
    validarSinOtraActiva(membresia);
    validarSinPagoRegistrado(idMembresia);

    BigDecimal monto = validarDuracionYPrecio(membresia);

    activarYRegistrarPago(membresia, fechaInicio, "Efectivo", monto);
  }

  // ==========================================
  // SIMULAR PAGO DIGITAL DEL CLIENTE
  // ==========================================

  @Transactional
  public void simularPagoDigital(Long idMembresia, Long idCliente, String metodoPago) {

    Membresia membresia = obtenerMembresia(idMembresia);

    // Verificar que la membresía pertenezca al cliente
    if (idCliente == null
        || membresia.getCliente() == null
        || !membresia.getCliente().getIdCliente().equals(idCliente)) {

      throw new IllegalArgumentException("Membresía no encontrada.");
    }

    validarEstado(membresia, "PENDIENTE_PAGO");

    validarCliente(membresia.getCliente());

    // Validar modalidad digital de la solicitud
    if (!esPagoDigital(membresia.getMetodoPagoPreferido())) {

      throw new IllegalArgumentException("Esta membresía no permite pagos digitales.");
    }

    // Validar método elegido para el pago
    String metodoNormalizado = normalizar(metodoPago);

    String metodoRegistrado;

    switch (metodoNormalizado) {
      case "TARJETA":
        metodoRegistrado = "Tarjeta";
        break;

      case "YAPE":
        metodoRegistrado = "Yape";
        break;

      case "PLIN":
        metodoRegistrado = "Plin";
        break;

      default:
        throw new IllegalArgumentException("Selecciona un método de pago válido.");
    }

    validarSinOtraActiva(membresia);
    validarSinPagoRegistrado(idMembresia);

    BigDecimal monto = validarDuracionYPrecio(membresia);

    LocalDate hoy = LocalDate.now(ZONA);

    LocalDate fechaInicio = membresia.getFechaInicioDeseada();

    if (fechaInicio == null || fechaInicio.isBefore(hoy)) {
      fechaInicio = hoy;
    }

    // Registrar pago y activar membresía
    activarYRegistrarPago(membresia, fechaInicio, metodoRegistrado, monto);
  }

  // ==========================================
  // ACTIVAR MEMBRESÍA Y REGISTRAR PAGO
  // ==========================================

  private void activarYRegistrarPago(
      Membresia membresia, LocalDate fechaInicio, String metodoPago, BigDecimal monto) {

    int meses = membresia.getDuracionMeses();

    membresia.setFechaInicio(fechaInicio);

    membresia.setFechaFin(fechaInicio.plusMonths(meses).minusDays(1));

    membresia.setEstado("ACTIVA");

    membresiaRepository.save(membresia);

    PagoMembresia pago = new PagoMembresia();

    pago.setMembresia(membresia);
    pago.setFechaPago(LocalDateTime.now(ZONA));
    pago.setMonto(monto);
    pago.setMetodoPago(metodoPago);
    pago.setEstado("PAGADO");

    pagoRepository.save(pago);
  }

  // ==========================================
  // VALIDACIONES
  // ==========================================

  private Membresia obtenerMembresia(Long idMembresia) {

    return membresiaRepository
        .findById(idMembresia)
        .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada."));
  }

  private void validarEstado(Membresia membresia, String estadoEsperado) {

    if (!estadoEsperado.equalsIgnoreCase(membresia.getEstado())) {

      throw new IllegalArgumentException("La membresía ya no se encuentra en el estado esperado.");
    }
  }

  private void validarCliente(Cliente cliente) {

    if (cliente == null
        || !"ACTIVO".equalsIgnoreCase(cliente.getEstado())
        || cliente.getUsuario() == null
        || !"ACTIVO".equalsIgnoreCase(cliente.getUsuario().getEstado())
        || cliente.getUsuario().getRol() == null
        || !"CLIENTE".equalsIgnoreCase(cliente.getUsuario().getRol().getNombre())) {

      throw new IllegalArgumentException("El cliente no tiene una cuenta habilitada.");
    }
  }

  private void validarFechaInicio(LocalDate fechaInicio) {

    if (fechaInicio == null || fechaInicio.isBefore(LocalDate.now(ZONA))) {

      throw new IllegalArgumentException("La fecha de inicio no es válida.");
    }
  }

  private boolean esPagoDigital(String metodo) {

    String valor = normalizar(metodo);

    return "TARJETA".equals(valor) || "YAPE/PLIN".equals(valor);
  }

  private String normalizar(String valor) {

    return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
  }

  private BigDecimal validarDuracionYPrecio(Membresia membresia) {

    Integer meses = membresia.getDuracionMeses();

    if (meses == null || (meses != 1 && meses != 3 && meses != 6)) {

      throw new IllegalArgumentException("La duración de la membresía no es válida.");
    }

    if (membresia.getPlan() == null
        || membresia.getPlan().getPrecio() == null
        || membresia.getPlan().getPrecio().compareTo(BigDecimal.ZERO) <= 0) {

      throw new IllegalArgumentException("El precio del plan no es válido.");
    }

    return membresia.getPlan().getPrecio().multiply(BigDecimal.valueOf(meses));
  }

  private void validarSinOtraActiva(Membresia membresia) {

    LocalDate hoy = LocalDate.now(ZONA);

    List<Membresia> historial =
        membresiaRepository.findByCliente_IdClienteOrderByFechaSolicitudDesc(
            membresia.getCliente().getIdCliente());

    for (Membresia otra : historial) {

      if (otra.getIdMembresia().equals(membresia.getIdMembresia())) {
        continue;
      }

      if ("ACTIVA".equalsIgnoreCase(otra.getEstado())) {

        if (otra.getFechaFin() != null && otra.getFechaFin().isBefore(hoy)) {

          otra.setEstado("VENCIDA");
          membresiaRepository.save(otra);

        } else {

          throw new IllegalArgumentException("El cliente ya tiene otra membresía activa.");
        }
      }
    }
  }

  private void validarSinPagoRegistrado(Long idMembresia) {

    boolean existePago =
        pagoRepository.findByMembresia_IdMembresiaOrderByFechaPagoDesc(idMembresia).stream()
            .anyMatch(pago -> "PAGADO".equalsIgnoreCase(pago.getEstado()));

    if (existePago) {
      throw new IllegalArgumentException("Esta membresía ya tiene un pago registrado.");
    }
  }
}
