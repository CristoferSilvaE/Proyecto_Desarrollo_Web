package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Instructor;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

  @EntityGraph(attributePaths = "clase")
  List<Instructor> findByEstadoOrderByNombresAsc(String estado);

  @EntityGraph(attributePaths = "clase")
  List<Instructor> findAllByOrderByNombresAscApellidosAsc();

  boolean existsByCorreoIgnoreCase(String correo);

  boolean existsByCorreoIgnoreCaseAndIdInstructorNot(String correo, Long idInstructor);

  long countByEstado(String estado);
}
