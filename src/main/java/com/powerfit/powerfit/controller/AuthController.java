package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Rol;
import com.powerfit.powerfit.model.Usuario;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.RolRepository;
import com.powerfit.powerfit.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

  private final UsuarioRepository usuarioRepository;
  private final ClienteRepository clienteRepository;
  private final RolRepository rolRepository;

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AuthController(
      UsuarioRepository usuarioRepository,
      ClienteRepository clienteRepository,
      RolRepository rolRepository) {

    this.usuarioRepository = usuarioRepository;
    this.clienteRepository = clienteRepository;
    this.rolRepository = rolRepository;
  }

  @PostMapping("/registro")
  @Transactional
  public String registrar(
      @RequestParam String nombres,
      @RequestParam String apellidos,
      @RequestParam String dni,
      @RequestParam String telefono,
      @RequestParam String correo,
      @RequestParam String password,
      @RequestParam String confirmarPassword,
      @RequestParam(required = false) Boolean aceptaTerminos,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
      Model model) {

    nombres = nombres.trim();
    apellidos = apellidos.trim();
    dni = dni.trim();
    telefono = telefono.trim();
    correo = correo.trim().toLowerCase();

    cargarDatosFormulario(model, nombres, apellidos, dni, telefono, correo, fechaNacimiento);

    if (nombres.isBlank() || apellidos.isBlank()) {
      model.addAttribute("error", "Los nombres y apellidos son obligatorios.");
      return "registro";
    }

    if (!dni.matches("\\d{8}")) {
      model.addAttribute("error", "El DNI debe contener exactamente 8 números.");
      return "registro";
    }

    if (fechaNacimiento.isAfter(LocalDate.now())) {
      model.addAttribute("error", "La fecha de nacimiento no puede ser futura.");
      return "registro";
    }

    if (password.length() < 8) {
      model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
      return "registro";
    }

    if (!password.equals(confirmarPassword)) {
      model.addAttribute("error", "Las contraseñas no coinciden.");
      return "registro";
    }

    if (!Boolean.TRUE.equals(aceptaTerminos)) {
      model.addAttribute("error", "Debes aceptar los términos y condiciones.");
      return "registro";
    }

    if (usuarioRepository.existsByCorreo(correo)) {
      model.addAttribute("error", "Ya existe una cuenta registrada con ese correo.");
      return "registro";
    }

    if (clienteRepository.existsByDni(dni)) {
      model.addAttribute("error", "Ya existe un cliente registrado con ese DNI.");
      return "registro";
    }

    Rol rolCliente = rolRepository.findByNombre("CLIENTE").orElse(null);

    if (rolCliente == null) {
      model.addAttribute("error", "No se encontró el rol CLIENTE en la base de datos.");
      return "registro";
    }

    Usuario usuario = new Usuario();
    usuario.setRol(rolCliente);
    usuario.setCorreo(correo);
    usuario.setPasswordHash(passwordEncoder.encode(password));
    usuario.setEstado("ACTIVO");
    usuario.setCambioPasswordPendiente(false);

    usuarioRepository.save(usuario);

    Cliente cliente = new Cliente();
    cliente.setUsuario(usuario);
    cliente.setNombres(nombres);
    cliente.setApellidos(apellidos);
    cliente.setDni(dni);
    cliente.setTelefono(telefono);
    cliente.setFechaNacimiento(fechaNacimiento);
    cliente.setEstado("ACTIVO");

    clienteRepository.save(cliente);

    return "redirect:/login?registro=exitoso";
  }

  @GetMapping("/logout")
  public String logout(HttpSession session) {

    session.invalidate();

    return "redirect:/";
  }

  private void cargarDatosFormulario(
      Model model,
      String nombres,
      String apellidos,
      String dni,
      String telefono,
      String correo,
      LocalDate fechaNacimiento) {

    model.addAttribute("nombres", nombres);
    model.addAttribute("apellidos", apellidos);
    model.addAttribute("dni", dni);
    model.addAttribute("telefono", telefono);
    model.addAttribute("correo", correo);
    model.addAttribute("fechaNacimiento", fechaNacimiento);
  }

  @PostMapping("/login")
  public String iniciarSesion(
      @RequestParam String correo,
      @RequestParam String password,
      HttpSession session,
      Model model) {

    correo = correo.trim().toLowerCase();

    Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

    if (usuario == null || !passwordEncoder.matches(password, usuario.getPasswordHash())) {
      model.addAttribute("error", "Correo o contraseña incorrectos.");
      return "login";
    }

    if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
      model.addAttribute("error", "Tu cuenta no se encuentra activa.");
      return "login";
    }

    session.setAttribute("idUsuario", usuario.getIdUsuario());
    session.setAttribute("rol", usuario.getRol().getNombre());

    if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {

      Cliente cliente =
          clienteRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).orElse(null);

      if (cliente == null) {
        model.addAttribute("error", "No se encontró el perfil del cliente.");
        return "login";
      }

      session.setAttribute("idCliente", cliente.getIdCliente());

      return "redirect:/";
    }

    return "redirect:/";
  }
}
