package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.*;
import com.powerfit.powerfit.repository.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HorarioClaseService {

  private final HorarioClaseRepository horarioRepository;
  private final ClaseRepository claseRepository;
  private final InstructorRepository instructorRepository;
  private final ReservaRepository reservaRepository;

  private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

  public HorarioClaseService(
      HorarioClaseRepository horarioRepository,
      ClaseRepository claseRepository,
      InstructorRepository instructorRepository,
      ReservaRepository reservaRepository) {

    this.horarioRepository = horarioRepository;
    this.claseRepository = claseRepository;
    this.instructorRepository = instructorRepository;
    this.reservaRepository = reservaRepository;
  }

  // LISTAR TODOS LOS HORARIOS
  public List<HorarioClase> listarTodos() {
    return horarioRepository.findAllByOrderByFechaAscHoraInicioAsc();
  }

  // CLASES ACTIVAS PARA EL FORMULARIO
  public List<Clase> listarClasesActivas() {
    return claseRepository.findByEstadoOrderByNombreAsc("ACTIVO");
  }

  // INSTRUCTORES ACTIVOS PARA EL FORMULARIO
  public List<Instructor> listarInstructoresActivos() {
    return instructorRepository.findByEstadoOrderByNombresAsc("ACTIVO");
  }

  // REGISTRAR O EDITAR
  @Transactional
  public HorarioClase guardar(
      Long idHorario,
      Long idClase,
      Long idInstructor,
      LocalDate fecha,
      LocalTime horaInicio,
      LocalTime horaFin,
      Integer capacidad,
      String sala) {

    validarDatos(fecha, horaInicio, horaFin, capacidad, sala);

    Clase clase =
        claseRepository
            .findById(idClase)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la clase."));

    Instructor instructor =
        instructorRepository
            .findById(idInstructor)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el instructor."));

    if (!"ACTIVO".equals(clase.getEstado())) {
      throw new IllegalArgumentException("La clase seleccionada está inactiva.");
    }

    if (!"ACTIVO".equals(instructor.getEstado())) {
      throw new IllegalArgumentException("El instructor seleccionado está inactivo.");
    }

    if (instructor.getClase() == null
        || !clase.getIdClase().equals(instructor.getClase().getIdClase())) {

      throw new IllegalArgumentException(
          "El instructor no pertenece a la especialidad seleccionada.");
    }

    HorarioClase horario;

    if (idHorario == null) {
      horario = new HorarioClase();
      horario.setEstado("ACTIVO");
    } else {
      horario =
          horarioRepository
              .findById(idHorario)
              .orElseThrow(() -> new IllegalArgumentException("No se encontró el horario."));
    }

    // No permitir reducir la capacidad por debajo
    // de las reservas existentes.
    if (idHorario != null) {

      long reservas = reservaRepository.countByHorario_IdHorarioAndEstado(idHorario, "RESERVADA");

      if (capacidad < reservas) {
        throw new IllegalArgumentException(
            "La capacidad no puede ser menor que las reservas existentes.");
      }
    }

    validarCruces(fecha, horaInicio, horaFin, idInstructor, sala, idHorario);

    horario.setClase(clase);
    horario.setInstructor(instructor);
    horario.setFecha(fecha);
    horario.setHoraInicio(horaInicio);
    horario.setHoraFin(horaFin);
    horario.setCapacidad(capacidad);
    horario.setSala(sala == null || sala.isBlank() ? null : sala.trim());

    return horarioRepository.save(horario);
  }

  // CAMBIAR ESTADO
  @Transactional
  public void cambiarEstado(Long idHorario, String nuevoEstado) {

    if (!"ACTIVO".equals(nuevoEstado) && !"INACTIVO".equals(nuevoEstado)) {

      throw new IllegalArgumentException("Estado no válido.");
    }

    HorarioClase horario =
        horarioRepository
            .findById(idHorario)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el horario."));

    if ("ACTIVO".equals(nuevoEstado)) {

      if (!"ACTIVO".equals(horario.getClase().getEstado())
          || !"ACTIVO".equals(horario.getInstructor().getEstado())) {

        throw new IllegalArgumentException(
            "No puedes activar un horario con clase o instructor inactivo.");
      }

      validarFechaHora(horario.getFecha(), horario.getHoraInicio());

      validarCruces(
          horario.getFecha(),
          horario.getHoraInicio(),
          horario.getHoraFin(),
          horario.getInstructor().getIdInstructor(),
          horario.getSala(),
          idHorario);
    }

    horario.setEstado(nuevoEstado);
    horarioRepository.save(horario);
  }

  // ESTADÍSTICAS
  private List<HorarioClase> horariosFuturos() {

    LocalDate hoy = LocalDate.now(ZONA_LIMA);
    LocalTime ahora = LocalTime.now(ZONA_LIMA);

    return horarioRepository
        .findByFechaGreaterThanEqualAndEstadoOrderByFechaAscHoraInicioAsc(hoy, "ACTIVO")
        .stream()
        .filter(h -> h.getFecha().isAfter(hoy) || h.getHoraInicio().isAfter(ahora))
        .toList();
  }

  public long contarClasesProgramadas() {
    return horariosFuturos().size();
  }

  public long contarInstructoresActivos() {
    return instructorRepository.countByEstado("ACTIVO");
  }

  public long contarCuposDisponibles() {

    long disponibles = 0;

    for (HorarioClase horario : horariosFuturos()) {

      long reservadas =
          reservaRepository.countByHorario_IdHorarioAndEstado(horario.getIdHorario(), "RESERVADA");

      disponibles += Math.max(0, horario.getCapacidad() - reservadas);
    }

    return disponibles;
  }

  public double calcularOcupacionPromedio() {

    long capacidadTotal = 0;
    long reservasTotal = 0;

    for (HorarioClase horario : horariosFuturos()) {

      capacidadTotal += horario.getCapacidad();

      reservasTotal +=
          reservaRepository.countByHorario_IdHorarioAndEstado(horario.getIdHorario(), "RESERVADA");
    }

    if (capacidadTotal == 0) {
      return 0.0;
    }

    return Math.round((reservasTotal * 1000.0) / capacidadTotal) / 10.0;
  }

  public Map<Long, Long> obtenerReservasPorHorario(List<HorarioClase> horarios) {

    Map<Long, Long> resultado = new HashMap<>();

    for (HorarioClase horario : horarios) {

      long reservadas =
          reservaRepository.countByHorario_IdHorarioAndEstado(horario.getIdHorario(), "RESERVADA");

      resultado.put(horario.getIdHorario(), reservadas);
    }

    return resultado;
  }

  // ==========================================
  // DETALLE DE CLIENTES RESERVADOS POR HORARIO
  // ==========================================

  public Map<Long, List<Reserva>> obtenerDetalleReservasPorHorario(List<HorarioClase> horarios) {

    Map<Long, List<Reserva>> resultado = new HashMap<>();

    for (HorarioClase horario : horarios) {

      List<Reserva> reservas =
          reservaRepository.findByHorario_IdHorarioAndEstadoOrderByFechaReservaAsc(
              horario.getIdHorario(), "RESERVADA");

      resultado.put(horario.getIdHorario(), reservas);
    }

    return resultado;
  }

  // VALIDACIONES
  private void validarDatos(
      LocalDate fecha, LocalTime inicio, LocalTime fin, Integer capacidad, String sala) {

    if (fecha == null || inicio == null || fin == null || capacidad == null) {

      throw new IllegalArgumentException("Completa todos los campos obligatorios.");
    }

    validarFechaHora(fecha, inicio);

    if (!inicio.isBefore(fin)) {
      throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
    }

    if (capacidad <= 0 || capacidad > 500) {
      throw new IllegalArgumentException("La capacidad debe estar entre 1 y 500.");
    }

    if (sala != null && sala.trim().length() > 80) {
      throw new IllegalArgumentException("El nombre de la sala supera los 80 caracteres.");
    }
  }

  private void validarFechaHora(LocalDate fecha, LocalTime inicio) {

    LocalDate hoy = LocalDate.now(ZONA_LIMA);
    LocalTime ahora = LocalTime.now(ZONA_LIMA);

    if (fecha.isBefore(hoy)) {
      throw new IllegalArgumentException("No puedes programar un horario en una fecha pasada.");
    }

    if (fecha.equals(hoy) && !inicio.isAfter(ahora)) {
      throw new IllegalArgumentException("La hora de inicio ya ha pasado.");
    }
  }

  // EVITAR CRUCES DE INSTRUCTORES Y SALAS
  private void validarCruces(
      LocalDate fecha,
      LocalTime inicio,
      LocalTime fin,
      Long idInstructor,
      String sala,
      Long idHorarioActual) {

    List<HorarioClase> existentes =
        horarioRepository.findByFechaAndEstadoOrderByHoraInicioAsc(fecha, "ACTIVO");

    for (HorarioClase otro : existentes) {

      if (idHorarioActual != null && idHorarioActual.equals(otro.getIdHorario())) {
        continue;
      }

      boolean seCruzan = inicio.isBefore(otro.getHoraFin()) && fin.isAfter(otro.getHoraInicio());

      if (!seCruzan) {
        continue;
      }

      if (otro.getInstructor().getIdInstructor().equals(idInstructor)) {

        throw new IllegalArgumentException("El instructor ya tiene otra clase en ese horario.");
      }

      if (sala != null
          && !sala.isBlank()
          && otro.getSala() != null
          && sala.trim().equalsIgnoreCase(otro.getSala().trim())) {

        throw new IllegalArgumentException("La sala ya está ocupada en ese horario.");
      }
    }
  }
}
