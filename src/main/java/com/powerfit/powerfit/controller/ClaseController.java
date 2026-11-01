package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.HorarioClase;
import com.powerfit.powerfit.repository.ClaseRepository;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.HorarioClaseRepository;
import com.powerfit.powerfit.repository.ReservaRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClaseController {

  private final HorarioClaseRepository horarioClaseRepository;
  private final ReservaRepository reservaRepository;
  private final ClaseRepository claseRepository;
  private final ClienteRepository clienteRepository;

  public ClaseController(
      HorarioClaseRepository horarioClaseRepository,
      ReservaRepository reservaRepository,
      ClaseRepository claseRepository,
      ClienteRepository clienteRepository) {
    this.horarioClaseRepository = horarioClaseRepository;
    this.reservaRepository = reservaRepository;
    this.claseRepository = claseRepository;
    this.clienteRepository = clienteRepository;
  }

  @GetMapping("/clases")
  public String clases(
      @RequestParam(required = false) String categoria, Model model, HttpSession session) {

    List<HorarioClase> horarios =
        horarioClaseRepository.findByFechaGreaterThanEqualAndEstadoOrderByFechaAscHoraInicioAsc(
            LocalDate.now(), "ACTIVO");

    // Filtrar por categoría
    if (categoria != null && !categoria.isBlank()) {

      horarios =
          horarios.stream()
              .filter(horario ->
                  horario.getClase().getCategoria() != null
                      && horario.getClase().getCategoria().equalsIgnoreCase(categoria))
              .toList();
    }

    // Reservas activas por horario
    Map<Long, Long> reservasPorHorario = new HashMap<>();

    for (HorarioClase horario : horarios) {

      long reservas =
          reservaRepository.countByHorario_IdHorarioAndEstado(horario.getIdHorario(), "RESERVADA");

      reservasPorHorario.put(horario.getIdHorario(), reservas);
    }

    // Categorías para los filtros
    List<String> categorias =
        claseRepository.findByEstadoOrderByNombreAsc("ACTIVO").stream()
            .map(clase -> clase.getCategoria())
            .filter(cat -> cat != null && !cat.isBlank())
            .distinct()
            .sorted()
            .toList();

    Long idCliente = (Long) session.getAttribute("idCliente");

    Cliente cliente = null;
    Set<Long> horariosReservados = new HashSet<>();

    if (idCliente != null) {

      cliente = clienteRepository.findById(idCliente).orElse(null);

      for (HorarioClase horario : horarios) {

        if (reservaRepository.existsByCliente_IdClienteAndHorario_IdHorarioAndEstado(
            idCliente, horario.getIdHorario(), "RESERVADA")) {

          horariosReservados.add(horario.getIdHorario());
        }
      }
    }

    model.addAttribute("cliente", cliente);
    model.addAttribute("horariosReservados", horariosReservados);
    model.addAttribute("horarios", horarios);
    model.addAttribute("reservasPorHorario", reservasPorHorario);
    model.addAttribute("categorias", categorias);
    model.addAttribute("categoriaSeleccionada", categoria);

    return "clases/reservaClases";
  }
}
