package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Rol;
import com.powerfit.powerfit.model.Usuario;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.RolRepository;
import com.powerfit.powerfit.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
      @RequestParam String genero,
      @RequestParam String correo,
      @RequestParam("usuario") String nombreUsuario,
      @RequestParam String password,
      @RequestParam String confirmarPassword,
      @RequestParam(required = false) Boolean aceptaTerminos,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
      Model model) {

    nombres = nombres.trim();
    apellidos = apellidos.trim();
    dni = dni.trim();
    telefono = telefono.trim();
    genero = genero.trim().toUpperCase();
    correo = correo.trim().toLowerCase();
    nombreUsuario = nombreUsuario.trim().toLowerCase();

    cargarDatosFormulario(
        model, nombres, apellidos, dni, telefono, genero, correo, nombreUsuario, fechaNacimiento);

    if (nombres.isBlank() || apellidos.isBlank()) {
      model.addAttribute("error", "Los nombres y apellidos son obligatorios.");
      return "auth/registro";
    }

    if (!dni.matches("\\d{8}")) {
      model.addAttribute("error", "El DNI debe contener exactamente 8 números.");
      return "auth/registro";
    }

    if (fechaNacimiento.isAfter(LocalDate.now())) {
      model.addAttribute("error", "La fecha de nacimiento no puede ser futura.");
      return "auth/registro";
    }

    if (password.length() < 8) {
      model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
      return "auth/registro";
    }

    if (!password.equals(confirmarPassword)) {
      model.addAttribute("error", "Las contraseñas no coinciden.");
      return "auth/registro";
    }

    if (!genero.equals("MASCULINO") && !genero.equals("FEMENINO")) {
      model.addAttribute("error", "Selecciona un género válido.");
      return "auth/registro";
    }

    if (!Boolean.TRUE.equals(aceptaTerminos)) {
      model.addAttribute("error", "Debes aceptar los términos y condiciones.");
      return "auth/registro";
    }

    if (usuarioRepository.existsByCorreo(correo)) {
      model.addAttribute("error", "Ya existe una cuenta registrada con ese correo.");
      return "auth/registro";
    }

    if (!nombreUsuario.matches("[a-z0-9._-]{4,30}")) {
      model.addAttribute(
          "error",
          "El usuario debe tener entre 4 y 30 caracteres. Solo se permiten letras, números, puntos,"
              + " guiones y guiones bajos.");
      return "auth/registro";
    }

    if (usuarioRepository.existsByUsuario(nombreUsuario)) {
      model.addAttribute("error", "Ese nombre de usuario ya está registrado. Elige otro.");
      return "auth/registro";
    }

    if (clienteRepository.existsByDni(dni)) {
      model.addAttribute("error", "Ya existe un cliente registrado con ese DNI.");
      return "auth/registro";
    }

    Rol rolCliente = rolRepository.findByNombre("CLIENTE").orElse(null);

    if (rolCliente == null) {
      model.addAttribute("error", "No se encontró el rol CLIENTE en la base de datos.");
      return "auth/registro";
    }

    Usuario usuario = new Usuario();
    usuario.setRol(rolCliente);
    usuario.setCorreo(correo);
    usuario.setUsuario(nombreUsuario);
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
    cliente.setGenero(genero);
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
      String genero,
      String correo,
      String nombreUsuario,
      LocalDate fechaNacimiento) {

    model.addAttribute("nombres", nombres);
    model.addAttribute("apellidos", apellidos);
    model.addAttribute("dni", dni);
    model.addAttribute("telefono", telefono);
    model.addAttribute("genero", genero);
    model.addAttribute("correo", correo);
    model.addAttribute("nombreUsuario", nombreUsuario);
    model.addAttribute("fechaNacimiento", fechaNacimiento);
  }

  @PostMapping("/login")
  public String iniciarSesion(
      @RequestParam String identificador,
      @RequestParam String password,
      HttpServletRequest request,
      Model model) {

    identificador = identificador.trim().toLowerCase();

    Usuario usuario;

    // Buscar por correo o nickname
    if (identificador.contains("@")) {
      usuario = usuarioRepository.findByCorreo(identificador).orElse(null);
    } else {
      usuario = usuarioRepository.findByUsuario(identificador).orElse(null);
    }

    // Validar credenciales
    if (usuario == null || !passwordEncoder.matches(password, usuario.getPasswordHash())) {

      model.addAttribute("error", "Usuario, correo o contraseña incorrectos.");
      model.addAttribute("identificador", identificador);
      return "auth/login";
    }

    // Validar estado
    if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
      model.addAttribute("error", "Tu cuenta no se encuentra activa.");
      model.addAttribute("identificador", identificador);
      return "auth/login";
    }

    // Validar rol
    if (usuario.getRol() == null) {
      model.addAttribute("error", "La cuenta no tiene un perfil asignado.");
      return "auth/login";
    }

    // ==========================================
    // CAMBIO OBLIGATORIO DE CONTRASEÑA
    // ==========================================

    if (Boolean.TRUE.equals(usuario.getCambioPasswordPendiente())) {

      // Evitar reutilizar una sesión anterior
      HttpSession anterior = request.getSession(false);

      if (anterior != null) {
        anterior.invalidate();
      }

      // Crear sesión temporal restringida
      HttpSession temporal = request.getSession(true);

      temporal.setAttribute("idUsuarioCambioPassword", usuario.getIdUsuario());

      temporal.setAttribute(
          "identificadorCambioPassword",
          usuario.getUsuario() != null && !usuario.getUsuario().isBlank()
              ? "@" + usuario.getUsuario()
              : usuario.getCorreo());

      return "redirect:/login?cambioPassword";
    }

    // ==========================================
    // LOGIN NORMAL
    // ==========================================

    HttpSession anterior = request.getSession(false);

    if (anterior != null) {
      anterior.invalidate();
    }

    HttpSession session = request.getSession(true);

    session.setAttribute("idUsuario", usuario.getIdUsuario());
    session.setAttribute("rol", usuario.getRol().getNombre());

    // Acceso del cliente
    if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {

      Cliente cliente =
          clienteRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).orElse(null);

      if (cliente == null) {
        session.invalidate();

        model.addAttribute("error", "No se encontró el perfil del cliente.");
        return "auth/login";
      }

      session.setAttribute("idCliente", cliente.getIdCliente());

      return "redirect:/";
    }

    // Acceso del administrador
    if ("ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) {
      return "redirect:/admin";
    }

    return "redirect:/";
  }

  @PostMapping("/cambiar-password")
  @Transactional
  public String guardarCambioPassword(
      @RequestParam(required = false) String nuevaPassword,
      @RequestParam(required = false) String confirmarPassword,
      HttpServletRequest request,
      RedirectAttributes redirectAttributes) {

    HttpSession temporal = request.getSession(false);

    // Comprobar que exista una sesión temporal autorizada
    if (temporal == null
        || !(temporal.getAttribute("idUsuarioCambioPassword") instanceof Long idUsuario)) {

      redirectAttributes.addFlashAttribute(
          "error", "No existe una solicitud de cambio de contraseña válida.");

      return "redirect:/login";
    }

    Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);

    // Comprobar que la cuenta siga habilitada
    if (usuario == null
        || !"ACTIVO".equalsIgnoreCase(usuario.getEstado())
        || !Boolean.TRUE.equals(usuario.getCambioPasswordPendiente())
        || usuario.getRol() == null) {

      temporal.invalidate();

      redirectAttributes.addFlashAttribute(
          "error", "La cuenta ya no está habilitada para realizar esta operación.");

      return "redirect:/login";
    }

    // Validar longitud
    if (nuevaPassword == null || nuevaPassword.length() < 8) {

      redirectAttributes.addFlashAttribute(
          "errorCambioPassword", "La nueva contraseña debe tener al menos 8 caracteres.");

      return "redirect:/login?cambioPassword";
    }

    // Validar coincidencia
    if (confirmarPassword == null || !nuevaPassword.equals(confirmarPassword)) {

      redirectAttributes.addFlashAttribute("errorCambioPassword", "Las contraseñas no coinciden.");

      return "redirect:/login?cambioPassword";
    }

    // No permitir reutilizar la contraseña temporal
    if (passwordEncoder.matches(nuevaPassword, usuario.getPasswordHash())) {

      redirectAttributes.addFlashAttribute(
          "errorCambioPassword", "Debes elegir una contraseña diferente a la temporal.");

      return "redirect:/login?cambioPassword";
    }

    // Comprobar que el cliente tenga su perfil
    Cliente cliente = null;

    if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {

      cliente = clienteRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).orElse(null);

      if (cliente == null) {
        temporal.invalidate();

        redirectAttributes.addFlashAttribute("error", "No se encontró el perfil del cliente.");

        return "redirect:/login";
      }
    }

    // Actualizar contraseña y quitar restricción
    usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
    usuario.setCambioPasswordPendiente(false);

    usuarioRepository.save(usuario);

    // Destruir sesión temporal
    temporal.invalidate();

    // Crear una nueva sesión autenticada
    HttpSession nuevaSesion = request.getSession(true);

    nuevaSesion.setAttribute("idUsuario", usuario.getIdUsuario());
    nuevaSesion.setAttribute("rol", usuario.getRol().getNombre());

    if (cliente != null) {
      nuevaSesion.setAttribute("idCliente", cliente.getIdCliente());
    }

    redirectAttributes.addFlashAttribute(
        "mensajeLogin", "¡Contraseña actualizada correctamente! Ya puedes ingresar a PowerFit.");

    if ("ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) {
      return "redirect:/admin";
    }

    return "redirect:/";
  }
}
