package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Membresia;
import com.powerfit.powerfit.model.Reserva;
import com.powerfit.powerfit.model.SeguimientoFisico;
import com.powerfit.powerfit.model.Usuario;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.MembresiaRepository;
import com.powerfit.powerfit.repository.PagoMembresiaRepository;
import com.powerfit.powerfit.repository.ReservaRepository;
import com.powerfit.powerfit.repository.SeguimientoFisicoRepository;
import com.powerfit.powerfit.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PerfilController {

  private final ClienteRepository clienteRepository;
  private final UsuarioRepository usuarioRepository;
  private final MembresiaRepository membresiaRepository;
  private final PagoMembresiaRepository pagoMembresiaRepository;
  private final ReservaRepository reservaRepository;
  private final SeguimientoFisicoRepository seguimientoFisicoRepository;

  public PerfilController(
      ClienteRepository clienteRepository,
      UsuarioRepository usuarioRepository,
      MembresiaRepository membresiaRepository,
      PagoMembresiaRepository pagoMembresiaRepository,
      ReservaRepository reservaRepository,
      SeguimientoFisicoRepository seguimientoFisicoRepository) {

    this.clienteRepository = clienteRepository;
    this.usuarioRepository = usuarioRepository;
    this.membresiaRepository = membresiaRepository;
    this.pagoMembresiaRepository = pagoMembresiaRepository;
    this.reservaRepository = reservaRepository;
    this.seguimientoFisicoRepository = seguimientoFisicoRepository;
  }

  @GetMapping("/perfil")
  public String perfil(HttpSession session, Model model) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "perfilUsuario";
    }

    Cliente cliente = clienteRepository.findById(idCliente).orElse(null);

    if (cliente == null) {
      session.invalidate();
      return "perfilUsuario";
    }

    Membresia membresiaActiva =
        membresiaRepository
            .findFirstByCliente_IdClienteAndEstadoOrderByFechaSolicitudDesc(idCliente, "ACTIVA")
            .orElse(null);

    Membresia membresiaPendiente =
        membresiaRepository
            .findFirstByCliente_IdClienteAndEstadoOrderByFechaSolicitudDesc(idCliente, "PENDIENTE")
            .orElse(null);

    List<Reserva> proximasReservas =
        reservaRepository.findByCliente_IdClienteOrderByFechaReservaDesc(idCliente).stream()
            .filter(reserva -> "RESERVADA".equalsIgnoreCase(reserva.getEstado()))
            .filter(reserva -> reserva.getHorario() != null)
            .filter(reserva -> !reserva.getHorario().getFecha().isBefore(LocalDate.now()))
            .sorted(
                Comparator.comparing((Reserva reserva) -> reserva.getHorario().getFecha())
                    .thenComparing(reserva -> reserva.getHorario().getHoraInicio()))
            .toList();

    model.addAttribute("membresiaActiva", membresiaActiva);
    model.addAttribute("membresiaPendiente", membresiaPendiente);
    model.addAttribute("proximasReservas", proximasReservas);
    model.addAttribute(
        "pagos",
        pagoMembresiaRepository.findByMembresia_Cliente_IdClienteOrderByFechaPagoDesc(idCliente));

    return "perfilUsuario";
  }

  @PostMapping("/perfil/actualizar")
  public String actualizarPerfil(
      @RequestParam String nombres,
      @RequestParam String apellidos,
      @RequestParam String telefono,
      @RequestParam String genero,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
      @RequestParam(required = false) MultipartFile fotoPerfil,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    Cliente cliente = clienteRepository.findById(idCliente).orElse(null);

    if (cliente == null) {
      return "redirect:/login";
    }

    nombres = nombres.trim();
    apellidos = apellidos.trim();
    telefono = telefono.trim();
    genero = genero.trim().toUpperCase();

    if (nombres.isBlank() || apellidos.isBlank()) {
      redirectAttributes.addFlashAttribute("error", "Los nombres y apellidos son obligatorios.");

      return "redirect:/perfil";
    }

    if (!genero.equals("MASCULINO") && !genero.equals("FEMENINO")) {
      redirectAttributes.addFlashAttribute("error", "Selecciona un género válido.");

      return "redirect:/perfil";
    }

    if (fechaNacimiento.isAfter(LocalDate.now())) {
      redirectAttributes.addFlashAttribute("error", "La fecha de nacimiento no puede ser futura.");

      return "redirect:/perfil";
    }

    cliente.setNombres(nombres);
    cliente.setApellidos(apellidos);
    cliente.setTelefono(telefono);
    cliente.setGenero(genero);
    cliente.setFechaNacimiento(fechaNacimiento);

    if (fotoPerfil != null && !fotoPerfil.isEmpty()) {

      if (fotoPerfil.getSize() > 2 * 1024 * 1024) {
        redirectAttributes.addFlashAttribute("error", "La imagen no puede superar los 2 MB.");

        return "redirect:/perfil";
      }

      String tipo = fotoPerfil.getContentType();

      Set<String> tiposPermitidos = Set.of("image/jpeg", "image/png", "image/webp");

      if (tipo == null || !tiposPermitidos.contains(tipo)) {
        redirectAttributes.addFlashAttribute("error", "Solo se permiten imágenes JPG, PNG o WEBP.");

        return "redirect:/perfil";
      }

      String extension;

      if ("image/png".equals(tipo)) {
        extension = ".png";
      } else if ("image/webp".equals(tipo)) {
        extension = ".webp";
      } else {
        extension = ".jpg";
      }

      try {

        Path directorio = Paths.get("uploads", "perfiles").toAbsolutePath().normalize();

        Files.createDirectories(directorio);

        String nombreArchivo =
            "perfil-" + cliente.getIdCliente() + "-" + UUID.randomUUID() + extension;

        Path destino = directorio.resolve(nombreArchivo).normalize();

        Files.copy(fotoPerfil.getInputStream(), destino);

        // Eliminar foto anterior si existía
        if (cliente.getFotoPerfil() != null && !cliente.getFotoPerfil().isBlank()) {

          Path fotoAnterior = directorio.resolve(cliente.getFotoPerfil()).normalize();

          Files.deleteIfExists(fotoAnterior);
        }

        cliente.setFotoPerfil(nombreArchivo);

      } catch (IOException e) {

        redirectAttributes.addFlashAttribute("error", "No se pudo guardar la imagen de perfil.");

        return "redirect:/perfil";
      }
    }

    clienteRepository.save(cliente);

    redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente.");

    return "redirect:/perfil";
  }

  @PostMapping("/perfil/desactivar")
  @Transactional
  public String desactivarCuenta(HttpSession session) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    Cliente cliente = clienteRepository.findById(idCliente).orElse(null);

    if (cliente == null) {
      return "redirect:/login";
    }

    Usuario usuario = cliente.getUsuario();

    cliente.setEstado("INACTIVO");
    usuario.setEstado("INACTIVO");

    clienteRepository.save(cliente);
    usuarioRepository.save(usuario);

    session.invalidate();

    return "redirect:/login?cuenta=desactivada";
  }

  // --- CRUD DE SEGUIMIENTO FÍSICO ---

  @GetMapping("/seguimiento")
  public String verSeguimiento(HttpSession session, Model model) {
    Long idCliente = (Long) session.getAttribute("idCliente");
    if (idCliente == null) return "redirect:/login";

    Cliente cliente = clienteRepository.findById(idCliente).orElse(null);
    model.addAttribute("cliente", cliente); // Necesario para tu navbar

    // Obtener historial
    List<SeguimientoFisico> seguimientos =
        seguimientoFisicoRepository.findByCliente_IdClienteOrderByFechaRegistroDesc(idCliente);
    model.addAttribute("seguimientos", seguimientos);

    // Calcular métricas para las tarjetas superiores
    if (!seguimientos.isEmpty()) {
      SeguimientoFisico ultimo = seguimientos.get(0);
      model.addAttribute("ultimoSeguimiento", ultimo);

      // Calcular IMC (Peso / Altura^2). Asume que la altura está en metros (ej. 1.75)
      // Calcular IMC (Peso / Altura^2). Asume que la altura está en metros (ej. 1.75)
      if (ultimo.getPeso() != null
          && ultimo.getAltura() != null
          && ultimo.getAltura().doubleValue() > 0) {
        double pesoDouble = ultimo.getPeso().doubleValue();
        double alturaDouble = ultimo.getAltura().doubleValue();

        double imc = pesoDouble / Math.pow(alturaDouble, 2);
        model.addAttribute("imc", imc);
      }
    }
    // Preparar el formulario
    if (!model.containsAttribute("nuevoSeguimiento")) {
      model.addAttribute("nuevoSeguimiento", new SeguimientoFisico());
    }

    return "seguimientoFisico";
  }

  @PostMapping("/perfil/seguimiento/guardar")
  public String guardarSeguimiento(
      @ModelAttribute("nuevoSeguimiento") SeguimientoFisico seguimiento, HttpSession session) {

    Long idCliente = (Long) session.getAttribute("idCliente");

    if (idCliente == null) {
      return "redirect:/login";
    }

    Cliente cliente = clienteRepository.findById(idCliente).orElse(null);

    if (cliente == null) {
      return "redirect:/login";
    }

    // CREATE
    if (seguimiento.getIdSeguimiento() == null) {

      seguimiento.setCliente(cliente);
      seguimiento.setFechaRegistro(LocalDate.now());

      seguimientoFisicoRepository.save(seguimiento);

      return "redirect:/seguimiento";
    }

    // UPDATE: comprobar que el registro pertenece al cliente logueado
    SeguimientoFisico existente =
        seguimientoFisicoRepository
            .findByIdSeguimientoAndCliente_IdCliente(seguimiento.getIdSeguimiento(), idCliente)
            .orElse(null);

    if (existente == null) {
      return "redirect:/seguimiento";
    }

    existente.setPeso(seguimiento.getPeso());
    existente.setAltura(seguimiento.getAltura());
    existente.setPorcentajeGrasa(seguimiento.getPorcentajeGrasa());
    existente.setMasaMuscular(seguimiento.getMasaMuscular());
    existente.setObservaciones(seguimiento.getObservaciones());

    seguimientoFisicoRepository.save(existente);

    return "redirect:/seguimiento";
  }

  @GetMapping("/perfil/seguimiento/editar/{id}")
  public String editarSeguimiento(@PathVariable("id") Long id, HttpSession session, Model model) {
    Long idCliente = (Long) session.getAttribute("idCliente");
    if (idCliente == null) return "redirect:/login";

    SeguimientoFisico seguimiento =
        seguimientoFisicoRepository
            .findByIdSeguimientoAndCliente_IdCliente(id, idCliente)
            .orElse(null);

    if (seguimiento == null) {
      return "redirect:/seguimiento";
    }
    model.addAttribute("nuevoSeguimiento", seguimiento);

    // Reutilizamos el método principal para cargar el resto de la página
    return verSeguimiento(session, model);
  }

  @GetMapping("/perfil/seguimiento/eliminar/{id}")
  public String eliminarSeguimiento(@PathVariable("id") Long id, HttpSession session) {
    Long idCliente = (Long) session.getAttribute("idCliente");
    if (idCliente == null) return "redirect:/login";

    SeguimientoFisico seguimiento =
        seguimientoFisicoRepository
            .findByIdSeguimientoAndCliente_IdCliente(id, idCliente)
            .orElse(null);

    if (seguimiento == null) {
      return "redirect:/seguimiento";
    }

    seguimientoFisicoRepository.delete(seguimiento);
    return "redirect:/seguimiento";
  }
}
