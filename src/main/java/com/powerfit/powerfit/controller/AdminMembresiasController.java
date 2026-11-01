package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.dto.MembresiaAdminDTO;
import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Membresia;
import com.powerfit.powerfit.model.PagoMembresia;
import com.powerfit.powerfit.model.PlanMembresia;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.MembresiaRepository;
import com.powerfit.powerfit.repository.PagoMembresiaRepository;
import com.powerfit.powerfit.repository.PlanMembresiaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/membresias")
public class AdminMembresiasController {

  private static final ZoneId ZONA = ZoneId.of("America/Lima");

  private static final Set<Integer> DURACIONES = Set.of(1, 3, 6);

  private static final Set<String> HORARIOS = Set.of("Mañana", "Tarde", "Noche");

  private static final Set<String> METODOS_PAGO = Set.of("Efectivo", "Tarjeta", "Yape/Plin");

  private final MembresiaRepository membresiaRepository;
  private final PlanMembresiaRepository planRepository;
  private final ClienteRepository clienteRepository;
  private final PagoMembresiaRepository pagoRepository;

  public AdminMembresiasController(
      MembresiaRepository membresiaRepository,
      PlanMembresiaRepository planRepository,
      ClienteRepository clienteRepository,
      PagoMembresiaRepository pagoRepository) {

    this.membresiaRepository = membresiaRepository;
    this.planRepository = planRepository;
    this.clienteRepository = clienteRepository;
    this.pagoRepository = pagoRepository;
  }

  // ==========================================
  // LISTAR MEMBRESÍAS
  // ==========================================

  @GetMapping
  @Transactional
  public String listar(
      @RequestParam(defaultValue = "") String buscar,
      @RequestParam(defaultValue = "TODOS") String estado,
      Model model) {

    LocalDate hoy = LocalDate.now(ZONA);

    List<Membresia> todas = membresiaRepository.findAll();

    // Actualizar membresías vencidas
    boolean huboCambios = actualizarVencidas(todas, hoy);

    if (huboCambios) {
      membresiaRepository.saveAll(todas);
    }

    todas.sort(
        Comparator.comparing(
            Membresia::getFechaSolicitud, Comparator.nullsLast(Comparator.reverseOrder())));

    // Obtener último pago de cada membresía
    Map<Long, PagoMembresia> pagosPorMembresia = new HashMap<>();

    for (PagoMembresia pago : pagoRepository.findAll()) {

      if (pago.getMembresia() == null) {
        continue;
      }

      Long id = pago.getMembresia().getIdMembresia();

      PagoMembresia actual = pagosPorMembresia.get(id);

      if (actual == null
          || (pago.getFechaPago() != null
              && (actual.getFechaPago() == null
                  || pago.getFechaPago().isAfter(actual.getFechaPago())))) {

        pagosPorMembresia.put(id, pago);
      }
    }

    String filtroIngresado = estado.trim().toUpperCase(Locale.ROOT);

    Set<String> estadosValidos =
        Set.of("TODOS", "PENDIENTE", "ACTIVA", "VENCIDA", "RECHAZADA", "CANCELADA");

    final String filtro = estadosValidos.contains(filtroIngresado) ? filtroIngresado : "TODOS";

    // Filtrar y preparar DTO
    List<MembresiaAdminDTO> membresias =
        todas.stream()
            .filter(m -> filtro.equals("TODOS") || filtro.equalsIgnoreCase(m.getEstado()))
            .map(m -> new MembresiaAdminDTO(m, pagosPorMembresia.get(m.getIdMembresia())))
            .filter(f -> coincideBusqueda(f, buscar))
            .toList();

    // Clientes habilitados
    List<Cliente> clientes =
        clienteRepository.findAll().stream()
            .filter(this::clienteHabilitado)
            .sorted(Comparator.comparing(Cliente::getNombres, String.CASE_INSENSITIVE_ORDER))
            .toList();

    List<PlanMembresia> planes = planRepository.findByEstadoOrderByPrecioAsc("ACTIVO");

    long pendientes =
        todas.stream().filter(m -> "PENDIENTE".equalsIgnoreCase(m.getEstado())).count();

    long activas = todas.stream().filter(m -> "ACTIVA".equalsIgnoreCase(m.getEstado())).count();

    long porVencer =
        todas.stream()
            .filter(m -> "ACTIVA".equalsIgnoreCase(m.getEstado()))
            .filter(m -> m.getFechaFin() != null)
            .filter(
                m -> !m.getFechaFin().isBefore(hoy) && !m.getFechaFin().isAfter(hoy.plusDays(7)))
            .count();

    model.addAttribute("membresias", membresias);
    model.addAttribute("clientes", clientes);
    model.addAttribute("planes", planes);

    model.addAttribute("buscar", buscar);
    model.addAttribute("estadoFiltro", filtro);
    model.addAttribute("fechaHoy", hoy);

    model.addAttribute("totalMembresias", todas.size());
    model.addAttribute("membresiasPendientes", pendientes);
    model.addAttribute("membresiasActivas", activas);
    model.addAttribute("membresiasPorVencer", porVencer);

    return "admin/membresias";
  }

  // ==========================================
  // REGISTRAR SOLICITUD DESDE ADMINISTRACIÓN
  // ==========================================

  @PostMapping("/solicitar")
  @Transactional
  public String registrarSolicitud(
      @RequestParam Long idCliente,
      @RequestParam Long idPlan,
      @RequestParam Integer duracionMeses,
      @RequestParam String horarioPreferido,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDeseada,
      @RequestParam String metodoPagoPreferido,
      @RequestParam(required = false) String observacion,
      RedirectAttributes redirectAttributes) {

    Cliente cliente = clienteRepository.findById(idCliente).orElse(null);

    PlanMembresia plan = planRepository.findById(idPlan).orElse(null);

    LocalDate hoy = LocalDate.now(ZONA);

    if (!clienteHabilitado(cliente)
        || plan == null
        || !"ACTIVO".equalsIgnoreCase(plan.getEstado())) {

      redirectAttributes.addFlashAttribute(
          "error", "El cliente o el plan seleccionado no está disponible.");

      return "redirect:/admin/membresias";
    }

    if (duracionMeses == null
        || !DURACIONES.contains(duracionMeses)
        || !HORARIOS.contains(horarioPreferido)
        || !METODOS_PAGO.contains(metodoPagoPreferido)
        || fechaInicioDeseada == null
        || fechaInicioDeseada.isBefore(hoy)) {

      redirectAttributes.addFlashAttribute("error", "Verifica los datos de la solicitud.");

      return "redirect:/admin/membresias";
    }

    observacion = observacion == null ? "" : observacion.trim();

    if (observacion.length() > 255) {
      redirectAttributes.addFlashAttribute(
          "error", "La observación no puede superar 255 caracteres.");

      return "redirect:/admin/membresias";
    }

    List<Membresia> historial =
        membresiaRepository.findByCliente_IdClienteOrderByFechaSolicitudDesc(idCliente);

    if (actualizarVencidas(historial, hoy)) {
      membresiaRepository.saveAll(historial);
    }

    boolean tieneEnCurso =
        historial.stream()
            .anyMatch(m ->
                "PENDIENTE".equalsIgnoreCase(m.getEstado())
                    || "ACTIVA".equalsIgnoreCase(m.getEstado()));

    if (tieneEnCurso) {
      redirectAttributes.addFlashAttribute(
          "error", "El cliente ya tiene una membresía activa o pendiente.");

      return "redirect:/admin/membresias";
    }

    Membresia membresia = new Membresia();

    membresia.setCliente(cliente);
    membresia.setPlan(plan);
    membresia.setDuracionMeses(duracionMeses);
    membresia.setHorarioPreferido(horarioPreferido);
    membresia.setFechaInicioDeseada(fechaInicioDeseada);
    membresia.setMetodoPagoPreferido(metodoPagoPreferido);
    membresia.setObservacion(observacion.isBlank() ? null : observacion);
    membresia.setEstado("PENDIENTE");

    membresiaRepository.save(membresia);

    redirectAttributes.addFlashAttribute(
        "mensaje", "Solicitud de membresía registrada correctamente.");

    return "redirect:/admin/membresias";
  }

  // ==========================================
  // CONFIRMAR PAGO Y ACTIVAR
  // ==========================================

  @PostMapping("/{id}/activar")
  @Transactional
  public String activar(
      @PathVariable Long id,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam String metodoPago,
      RedirectAttributes redirectAttributes) {

    Membresia membresia = membresiaRepository.findById(id).orElse(null);

    LocalDate hoy = LocalDate.now(ZONA);

    if (membresia == null || !"PENDIENTE".equalsIgnoreCase(membresia.getEstado())) {

      redirectAttributes.addFlashAttribute("error", "La solicitud ya no está pendiente.");

      return "redirect:/admin/membresias";
    }

    if (!clienteHabilitado(membresia.getCliente())) {

      redirectAttributes.addFlashAttribute("error", "El cliente no tiene una cuenta habilitada.");

      return "redirect:/admin/membresias";
    }

    if (fechaInicio == null
        || fechaInicio.isBefore(hoy)
        || !METODOS_PAGO.contains(metodoPago)
        || membresia.getDuracionMeses() == null
        || !DURACIONES.contains(membresia.getDuracionMeses())) {

      redirectAttributes.addFlashAttribute(
          "error", "Verifica la fecha, duración y método de pago.");

      return "redirect:/admin/membresias";
    }

    List<Membresia> historial =
        membresiaRepository.findByCliente_IdClienteOrderByFechaSolicitudDesc(
            membresia.getCliente().getIdCliente());

    if (actualizarVencidas(historial, hoy)) {
      membresiaRepository.saveAll(historial);
    }

    boolean otraActiva =
        historial.stream()
            .filter(m -> !m.getIdMembresia().equals(id))
            .anyMatch(m -> "ACTIVA".equalsIgnoreCase(m.getEstado()));

    if (otraActiva) {
      redirectAttributes.addFlashAttribute("error", "El cliente ya tiene otra membresía activa.");

      return "redirect:/admin/membresias";
    }

    int meses = membresia.getDuracionMeses();

    BigDecimal monto = membresia.getPlan().getPrecio().multiply(BigDecimal.valueOf(meses));

    // Activar membresía
    membresia.setFechaInicio(fechaInicio);
    membresia.setFechaFin(fechaInicio.plusMonths(meses).minusDays(1));
    membresia.setEstado("ACTIVA");

    membresiaRepository.save(membresia);

    // Registrar pago confirmado
    PagoMembresia pago = new PagoMembresia();

    pago.setMembresia(membresia);
    pago.setFechaPago(LocalDateTime.now(ZONA));
    pago.setMonto(monto);
    pago.setMetodoPago(metodoPago);
    pago.setEstado("PAGADO");

    pagoRepository.save(pago);

    redirectAttributes.addFlashAttribute(
        "mensaje", "Pago registrado y membresía activada correctamente.");

    return "redirect:/admin/membresias";
  }

  // ==========================================
  // RECHAZAR SOLICITUD
  // ==========================================

  @PostMapping("/{id}/rechazar")
  @Transactional
  public String rechazar(
      @PathVariable Long id,
      @RequestParam(required = false) String motivo,
      RedirectAttributes redirectAttributes) {

    Membresia membresia = membresiaRepository.findById(id).orElse(null);

    if (membresia == null || !"PENDIENTE".equalsIgnoreCase(membresia.getEstado())) {

      redirectAttributes.addFlashAttribute("error", "La solicitud ya no está pendiente.");

      return "redirect:/admin/membresias";
    }

    motivo = motivo == null ? "" : motivo.trim();

    if (motivo.length() > 180) {
      redirectAttributes.addFlashAttribute("error", "El motivo no puede superar 180 caracteres.");

      return "redirect:/admin/membresias";
    }

    String detalle =
        motivo.isBlank() ? "Solicitud rechazada por administración." : "Rechazo: " + motivo;

    String observacionActual = membresia.getObservacion();

    String nuevaObservacion =
        observacionActual == null || observacionActual.isBlank()
            ? detalle
            : observacionActual + " | " + detalle;

    if (nuevaObservacion.length() > 255) {
      nuevaObservacion = nuevaObservacion.substring(0, 255);
    }

    membresia.setObservacion(nuevaObservacion);
    membresia.setEstado("RECHAZADA");

    membresiaRepository.save(membresia);

    redirectAttributes.addFlashAttribute(
        "mensaje", "La solicitud fue rechazada. Se conservó su historial.");

    return "redirect:/admin/membresias";
  }

  // ==========================================
  // MÉTODOS AUXILIARES
  // ==========================================

  private boolean actualizarVencidas(List<Membresia> membresias, LocalDate hoy) {

    boolean cambios = false;

    for (Membresia membresia : membresias) {

      if ("ACTIVA".equalsIgnoreCase(membresia.getEstado())
          && membresia.getFechaFin() != null
          && membresia.getFechaFin().isBefore(hoy)) {

        membresia.setEstado("VENCIDA");
        cambios = true;
      }
    }

    return cambios;
  }

  private boolean clienteHabilitado(Cliente cliente) {

    return cliente != null
        && "ACTIVO".equalsIgnoreCase(cliente.getEstado())
        && cliente.getUsuario() != null
        && "ACTIVO".equalsIgnoreCase(cliente.getUsuario().getEstado())
        && cliente.getUsuario().getRol() != null
        && "CLIENTE".equalsIgnoreCase(cliente.getUsuario().getRol().getNombre());
  }

  private boolean coincideBusqueda(MembresiaAdminDTO fila, String buscar) {

    if (buscar == null || buscar.isBlank()) {
      return true;
    }

    String termino = buscar.trim().toLowerCase(Locale.ROOT);

    return normalizar(fila.getNombreCliente()).contains(termino)
        || normalizar(fila.getCorreoCliente()).contains(termino)
        || normalizar(fila.getDniCliente()).contains(termino)
        || normalizar(fila.getMembresia().getPlan().getNombre()).contains(termino)
        || normalizar(fila.getMembresia().getEstado()).contains(termino);
  }

  private String normalizar(String valor) {
    return valor == null ? "" : valor.toLowerCase(Locale.ROOT);
  }
}
