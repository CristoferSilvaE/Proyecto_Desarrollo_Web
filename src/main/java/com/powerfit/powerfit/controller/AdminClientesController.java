package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.repository.ClienteRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
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
@RequestMapping("/admin/clientes")
public class AdminClientesController {

  private final ClienteRepository clienteRepository;

  public AdminClientesController(ClienteRepository clienteRepository) {
    this.clienteRepository = clienteRepository;
  }

  @GetMapping
  public String listarClientes(
      @RequestParam(defaultValue = "") String buscar,
      @RequestParam(defaultValue = "TODOS") String estado,
      Model model) {

    List<Cliente> todos = clienteRepository.findAll();

    List<Cliente> clientes =
        todos.stream()
            .filter(c ->
                c.getUsuario() != null
                    && c.getUsuario().getRol() != null
                    && "CLIENTE".equalsIgnoreCase(c.getUsuario().getRol().getNombre()))
            .filter(c -> {
              if ("TODOS".equalsIgnoreCase(estado)) {
                return true;
              }
              return estado.equalsIgnoreCase(c.getEstado());
            })
            .filter(c -> coincideBusqueda(c, buscar))
            .sorted((a, b) -> b.getIdCliente().compareTo(a.getIdCliente()))
            .toList();

    long activos = todos.stream().filter(c -> "ACTIVO".equalsIgnoreCase(c.getEstado())).count();

    long inactivos = todos.stream().filter(c -> "INACTIVO".equalsIgnoreCase(c.getEstado())).count();

    YearMonth mesActual = YearMonth.now(ZoneId.of("America/Lima"));

    long nuevos =
        todos.stream()
            .filter(c ->
                c.getUsuario() != null
                    && c.getUsuario().getFechaCreacion() != null
                    && YearMonth.from(c.getUsuario().getFechaCreacion()).equals(mesActual))
            .count();

    model.addAttribute("clientes", clientes);
    model.addAttribute("buscar", buscar);
    model.addAttribute("estadoFiltro", estado);
    model.addAttribute("totalClientes", todos.size());
    model.addAttribute("clientesActivos", activos);
    model.addAttribute("clientesInactivos", inactivos);
    model.addAttribute("clientesNuevos", nuevos);

    return "admin/clientes";
  }

  @PostMapping("/{id}/editar")
  @Transactional
  public String editarCliente(
      @PathVariable Long id,
      @RequestParam String nombres,
      @RequestParam String apellidos,
      @RequestParam(required = false) String telefono,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fechaNacimiento,
      @RequestParam(required = false) String genero,
      RedirectAttributes redirectAttributes) {

    Cliente cliente = clienteRepository.findById(id).orElse(null);

    if (cliente == null
        || cliente.getUsuario() == null
        || cliente.getUsuario().getRol() == null
        || !"CLIENTE".equalsIgnoreCase(cliente.getUsuario().getRol().getNombre())) {

      redirectAttributes.addFlashAttribute("error", "No se encontró el cliente solicitado.");

      return "redirect:/admin/clientes";
    }

    nombres = nombres.trim();
    apellidos = apellidos.trim();
    telefono = telefono == null ? "" : telefono.trim();
    genero = genero == null ? "" : genero.trim().toUpperCase(Locale.ROOT);

    if (nombres.isBlank()
        || apellidos.isBlank()
        || nombres.length() > 80
        || apellidos.length() > 80) {

      redirectAttributes.addFlashAttribute("error", "Verifica los nombres y apellidos.");

      return "redirect:/admin/clientes";
    }

    if (telefono.length() > 15
        || (!telefono.isBlank() && !telefono.matches("[0-9+()\\s-]{7,15}"))) {

      redirectAttributes.addFlashAttribute("error", "El número de teléfono no es válido.");

      return "redirect:/admin/clientes";
    }

    if (fechaNacimiento != null
        && fechaNacimiento.isAfter(LocalDate.now(ZoneId.of("America/Lima")))) {

      redirectAttributes.addFlashAttribute("error", "La fecha de nacimiento no puede ser futura.");

      return "redirect:/admin/clientes";
    }

    if (!genero.isBlank() && !genero.equals("MASCULINO") && !genero.equals("FEMENINO")) {

      redirectAttributes.addFlashAttribute("error", "Selecciona un género válido.");

      return "redirect:/admin/clientes";
    }

    cliente.setNombres(nombres);
    cliente.setApellidos(apellidos);
    cliente.setTelefono(telefono.isBlank() ? null : telefono);
    cliente.setFechaNacimiento(fechaNacimiento);
    cliente.setGenero(genero.isBlank() ? null : genero);

    clienteRepository.save(cliente);

    redirectAttributes.addFlashAttribute(
        "mensaje", "Información del cliente actualizada correctamente.");

    return "redirect:/admin/clientes";
  }

  private boolean coincideBusqueda(Cliente cliente, String buscar) {

    if (buscar == null || buscar.isBlank()) {
      return true;
    }

    String termino = buscar.trim().toLowerCase(Locale.ROOT);

    return normalizar(cliente.getNombres()).contains(termino)
        || normalizar(cliente.getApellidos()).contains(termino)
        || normalizar(cliente.getTelefono()).contains(termino)
        || normalizar(cliente.getUsuario().getCorreo()).contains(termino);
  }

  private String normalizar(String valor) {
    return valor == null ? "" : valor.toLowerCase(Locale.ROOT);
  }
}
