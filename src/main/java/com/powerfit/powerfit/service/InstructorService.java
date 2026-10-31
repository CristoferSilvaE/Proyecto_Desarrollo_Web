package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Instructor;
import com.powerfit.powerfit.repository.InstructorRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class InstructorService {

  private final InstructorRepository instructorRepository;

  public InstructorService(InstructorRepository instructorRepository) {
    this.instructorRepository = instructorRepository;
  }

  // LISTAR
  public List<Instructor> listarTodos() {
    return instructorRepository.findAllByOrderByNombresAscApellidosAsc();
  }

  // REGISTRAR
  @Transactional
  public Instructor registrar(
      String nombres, String apellidos, String especialidad, String telefono, String correo) {

    validarDatos(nombres, apellidos, especialidad, telefono, correo);

    String correoNormalizado = correo.trim().toLowerCase();

    if (instructorRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
      throw new IllegalArgumentException("Ya existe un instructor registrado con ese correo.");
    }

    Instructor instructor = new Instructor();

    instructor.setNombres(nombres.trim());
    instructor.setApellidos(apellidos.trim());
    instructor.setEspecialidad(especialidad.trim());
    instructor.setTelefono(telefono.trim());
    instructor.setCorreo(correoNormalizado);
    instructor.setEstado("ACTIVO");

    return instructorRepository.save(instructor);
  }

  // EDITAR
  @Transactional
  public Instructor editar(
      Long idInstructor,
      String nombres,
      String apellidos,
      String especialidad,
      String telefono,
      String correo) {

    validarDatos(nombres, apellidos, especialidad, telefono, correo);

    Instructor instructor =
        instructorRepository
            .findById(idInstructor)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el instructor."));

    String correoNormalizado = correo.trim().toLowerCase();

    if (instructorRepository.existsByCorreoIgnoreCaseAndIdInstructorNot(
        correoNormalizado, idInstructor)) {

      throw new IllegalArgumentException("El correo ya pertenece a otro instructor.");
    }

    instructor.setNombres(nombres.trim());
    instructor.setApellidos(apellidos.trim());
    instructor.setEspecialidad(especialidad.trim());
    instructor.setTelefono(telefono.trim());
    instructor.setCorreo(correoNormalizado);

    return instructorRepository.save(instructor);
  }

  // CAMBIAR ESTADO
  @Transactional
  public void cambiarEstado(Long idInstructor, String nuevoEstado) {

    if (!"ACTIVO".equals(nuevoEstado) && !"INACTIVO".equals(nuevoEstado)) {
      throw new IllegalArgumentException("Estado no válido.");
    }

    Instructor instructor =
        instructorRepository
            .findById(idInstructor)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el instructor."));

    instructor.setEstado(nuevoEstado);

    instructorRepository.save(instructor);
  }

  // VALIDACIONES
  private void validarDatos(
      String nombres, String apellidos, String especialidad, String telefono, String correo) {

    if (nombres == null || nombres.isBlank() || apellidos == null || apellidos.isBlank()) {
      throw new IllegalArgumentException("Los nombres y apellidos son obligatorios.");
    }

    if (nombres.trim().length() > 80 || apellidos.trim().length() > 80) {
      throw new IllegalArgumentException(
          "Los nombres y apellidos no pueden superar 80 caracteres.");
    }

    if (especialidad == null || especialidad.isBlank() || especialidad.trim().length() > 80) {
      throw new IllegalArgumentException("Ingresa una especialidad válida.");
    }

    if (telefono == null || !telefono.trim().matches("\\d{9,15}")) {
      throw new IllegalArgumentException("El teléfono debe contener entre 9 y 15 números.");
    }

    if (correo == null
        || correo.isBlank()
        || correo.trim().length() > 120
        || !correo.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
      throw new IllegalArgumentException("Ingresa un correo electrónico válido.");
    }
  }
}
