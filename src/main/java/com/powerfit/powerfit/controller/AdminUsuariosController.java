package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.dto.UsuarioAdminDTO;
import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Usuario;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
@RequestMapping("/admin/usuarios")
public class AdminUsuariosController {

  private final UsuarioRepository usuarioRepository;
  private final ClienteRepository clienteRepository;
  private static final SecureRandom RANDOM = new SecureRandom();

  private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
  private static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz";
  private static final String NUMEROS = "23456789";
  private static final String SIMBOLOS = "@#$%";
  private static final String CARACTERES = MAYUSCULAS + MINUSCULAS + NUMEROS + SIMBOLOS;

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AdminUsuariosController(
      UsuarioRepository usuarioRepository, ClienteRepository clienteRepository) {

    this.usuarioRepository = usuarioRepository;
    this.clienteRepository = clienteRepository;
  }

  @GetMapping
  public String listarUsuarios(
      @RequestParam(required = false, defaultValue = "") String buscar, Model model) {

    List<Usuario> usuariosBD = usuarioRepository.findAll();

    usuariosBD.sort(
        Comparator.comparingInt((Usuario u) ->
                u.getRol() != null && "ADMINISTRADOR".equalsIgnoreCase(u.getRol().getNombre())
                    ? 0
                    : 1)
            .thenComparing(Usuario::getIdUsuario, Comparator.reverseOrder()));

    // Obtener los datos personales de los clientes
    Map<Long, Cliente> clientesPorUsuario =
        clienteRepository.findAll().stream()
            .filter(c -> c.getUsuario() != null)
            .collect(
                Collectors.toMap(
                    c -> c.getUsuario().getIdUsuario(),
                    Function.identity(),
                    (anterior, nuevo) -> anterior));

    // Combinar usuarios y clientes
    List<UsuarioAdminDTO> usuarios =
        usuariosBD.stream()
            .map(u -> new UsuarioAdminDTO(u, clientesPorUsuario.get(u.getIdUsuario())))
            .filter(fila -> coincideBusqueda(fila, buscar))
            .toList();

    // Estadísticas
    long activos =
        usuariosBD.stream().filter(u -> "ACTIVO".equalsIgnoreCase(u.getEstado())).count();

    long inactivos =
        usuariosBD.stream().filter(u -> "INACTIVO".equalsIgnoreCase(u.getEstado())).count();

    long administradores =
        usuariosBD.stream()
            .filter(
                u -> u.getRol() != null && "ADMINISTRADOR".equalsIgnoreCase(u.getRol().getNombre()))
            .count();

    model.addAttribute("usuarios", usuarios);
    model.addAttribute("buscar", buscar);
    model.addAttribute("totalUsuarios", usuariosBD.size());
    model.addAttribute("usuariosActivos", activos);
    model.addAttribute("usuariosInactivos", inactivos);
    model.addAttribute("totalAdministradores", administradores);

    return "admin/usuarios";
  }

  @PostMapping("/{id}/editar")
  @Transactional
  public String editarUsuario(
      @PathVariable Long id,
      @RequestParam String nombres,
      @RequestParam String apellidos,
      @RequestParam String dni,
      @RequestParam String correo,
      @RequestParam(required = false, defaultValue = "") String nombreUsuario,
      RedirectAttributes redirectAttributes) {

    Usuario usuario = usuarioRepository.findById(id).orElse(null);

    if (usuario == null) {
      redirectAttributes.addFlashAttribute("error", "No se encontró el usuario.");
      return "redirect:/admin/usuarios";
    }

    // Por ahora solo se permite editar cuentas CLIENTE
    if (usuario.getRol() == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {

      redirectAttributes.addFlashAttribute(
          "error", "Esta cuenta no puede modificarse desde este módulo.");

      return "redirect:/admin/usuarios";
    }

    Cliente cliente = clienteRepository.findByUsuario_IdUsuario(id).orElse(null);

    if (cliente == null) {
      redirectAttributes.addFlashAttribute("error", "No se encontró la información del cliente.");
      return "redirect:/admin/usuarios";
    }

    // Normalizar información
    nombres = nombres.trim();
    apellidos = apellidos.trim();
    dni = dni.trim();
    correo = correo.trim().toLowerCase(Locale.ROOT);
    nombreUsuario = nombreUsuario.trim().toLowerCase(Locale.ROOT);

    // Validar campos obligatorios
    if (nombres.isBlank() || apellidos.isBlank() || dni.isBlank() || correo.isBlank()) {

      redirectAttributes.addFlashAttribute("error", "Completa todos los campos obligatorios.");

      return "redirect:/admin/usuarios";
    }

    // Validar DNI
    if (!dni.matches("[0-9]{8}")) {
      redirectAttributes.addFlashAttribute("error", "El DNI debe contener exactamente 8 dígitos.");

      return "redirect:/admin/usuarios";
    }

    // Validar DNI duplicado, excluyendo al cliente actual
    if (clienteRepository.existsByDniAndIdClienteNot(dni, cliente.getIdCliente())) {

      redirectAttributes.addFlashAttribute("error", "El DNI ya pertenece a otro cliente.");

      return "redirect:/admin/usuarios";
    }

    // Validar correo duplicado
    if (usuarioRepository.existsByCorreoAndIdUsuarioNot(correo, id)) {

      redirectAttributes.addFlashAttribute("error", "El correo electrónico ya está registrado.");

      return "redirect:/admin/usuarios";
    }

    // Validar formato del nickname si se proporciona
    if (!nombreUsuario.isBlank() && !nombreUsuario.matches("[a-z0-9._-]{4,30}")) {

      redirectAttributes.addFlashAttribute(
          "error", "El nickname debe tener entre 4 y 30 caracteres.");

      return "redirect:/admin/usuarios";
    }

    // Validar nickname duplicado
    if (!nombreUsuario.isBlank()
        && usuarioRepository.existsByUsuarioAndIdUsuarioNot(nombreUsuario, id)) {

      redirectAttributes.addFlashAttribute(
          "error", "Ese nombre de usuario ya pertenece a otra cuenta.");

      return "redirect:/admin/usuarios";
    }

    // Actualizar información personal
    cliente.setNombres(nombres);
    cliente.setApellidos(apellidos);
    cliente.setDni(dni);

    // Actualizar información de acceso
    usuario.setCorreo(correo);
    usuario.setUsuario(nombreUsuario.isBlank() ? null : nombreUsuario);

    clienteRepository.save(cliente);
    usuarioRepository.save(usuario);

    redirectAttributes.addFlashAttribute(
        "mensaje", "La información del usuario se actualizó correctamente.");

    return "redirect:/admin/usuarios";
  }

  @PostMapping("/{id}/estado")
  @Transactional
  public String cambiarEstado(
      @PathVariable Long id, @RequestParam String estado, RedirectAttributes redirectAttributes) {

    Usuario usuario = usuarioRepository.findById(id).orElse(null);

    if (usuario == null) {
      redirectAttributes.addFlashAttribute("error", "No se encontró la cuenta solicitada.");
      return "redirect:/admin/usuarios";
    }

    // Proteger cuentas administrativas y cuentas sin rol
    if (usuario.getRol() == null
        || "ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) {

      redirectAttributes.addFlashAttribute(
          "error", "No puedes modificar el estado de esta cuenta.");

      return "redirect:/admin/usuarios";
    }

    String nuevoEstado = estado.trim().toUpperCase(Locale.ROOT);

    if (!nuevoEstado.equals("ACTIVO") && !nuevoEstado.equals("INACTIVO")) {
      redirectAttributes.addFlashAttribute("error", "Estado de cuenta no válido.");
      return "redirect:/admin/usuarios";
    }

    if (nuevoEstado.equalsIgnoreCase(usuario.getEstado())) {
      redirectAttributes.addFlashAttribute("error", "La cuenta ya tiene ese estado.");
      return "redirect:/admin/usuarios";
    }

    // Actualizar estado de la cuenta
    usuario.setEstado(nuevoEstado);
    usuarioRepository.save(usuario);

    // Sincronizar el estado del cliente, si tiene ficha personal
    clienteRepository
        .findByUsuario_IdUsuario(id)
        .ifPresent(cliente -> {
          cliente.setEstado(nuevoEstado);
          clienteRepository.save(cliente);
        });

    String mensaje =
        nuevoEstado.equals("INACTIVO")
            ? "La cuenta fue dada de baja correctamente."
            : "La cuenta fue reactivada correctamente.";

    redirectAttributes.addFlashAttribute("mensaje", mensaje);

    return "redirect:/admin/usuarios";
  }

  @PostMapping("/{id}/restablecer-password")
  @Transactional
  public String restablecerPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {

    Usuario usuario = usuarioRepository.findById(id).orElse(null);

    if (usuario == null) {
      redirectAttributes.addFlashAttribute("error", "No se encontró la cuenta solicitada.");
      return "redirect:/admin/usuarios";
    }

    // Proteger cuentas administrativas y cuentas sin rol
    if (usuario.getRol() == null
        || "ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) {

      redirectAttributes.addFlashAttribute(
          "error", "No puedes restablecer la contraseña de esta cuenta.");

      return "redirect:/admin/usuarios";
    }

    // Generar contraseña temporal
    String passwordTemporal = generarPasswordTemporal();

    // Guardar únicamente el hash
    usuario.setPasswordHash(passwordEncoder.encode(passwordTemporal));
    usuario.setCambioPasswordPendiente(true);

    usuarioRepository.save(usuario);

    // Mostrar contraseña temporal una sola vez al administrador
    redirectAttributes.addFlashAttribute("passwordTemporal", passwordTemporal);

    redirectAttributes.addFlashAttribute(
        "usuarioRestablecido",
        usuario.getUsuario() != null ? "@" + usuario.getUsuario() : usuario.getCorreo());

    redirectAttributes.addFlashAttribute(
        "mensaje", "Contraseña restablecida. Entrega la contraseña temporal al usuario.");

    return "redirect:/admin/usuarios";
  }

  private String generarPasswordTemporal() {

    char[] password = new char[14];

    // Garantizar variedad de caracteres
    password[0] = elegirCaracter(MAYUSCULAS);
    password[1] = elegirCaracter(MINUSCULAS);
    password[2] = elegirCaracter(NUMEROS);
    password[3] = elegirCaracter(SIMBOLOS);

    // Completar contraseña
    for (int i = 4; i < password.length; i++) {
      password[i] = elegirCaracter(CARACTERES);
    }

    // Mezclar posiciones
    for (int i = password.length - 1; i > 0; i--) {
      int j = RANDOM.nextInt(i + 1);

      char temporal = password[i];
      password[i] = password[j];
      password[j] = temporal;
    }

    return new String(password);
  }

  private char elegirCaracter(String caracteres) {
    return caracteres.charAt(RANDOM.nextInt(caracteres.length()));
  }

  private boolean coincideBusqueda(UsuarioAdminDTO fila, String buscar) {

    if (buscar == null || buscar.isBlank()) {
      return true;
    }

    String termino = buscar.trim().toLowerCase(Locale.ROOT);

    Usuario usuario = fila.getUsuario();

    return normalizar(fila.getNombreCompleto()).contains(termino)
        || normalizar(fila.getDni()).contains(termino)
        || normalizar(usuario.getCorreo()).contains(termino)
        || normalizar(usuario.getUsuario()).contains(termino)
        || normalizar(usuario.getRol().getNombre()).contains(termino);
  }

  private String normalizar(String valor) {
    return valor == null ? "" : valor.toLowerCase(Locale.ROOT);
  }
}
