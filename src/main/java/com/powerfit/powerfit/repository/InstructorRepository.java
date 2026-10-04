package com.powerfit.powerfit.repository;

import com.powerfit.powerfit.model.Instructor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

  List<Instructor> findByEstadoOrderByNombresAsc(String estado);
}
