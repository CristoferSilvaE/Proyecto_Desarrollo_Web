package com.powerfit.powerfit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "instructores")
public class Instructor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_instructor")
  private Long idInstructor;

  @Column(name = "nombres", nullable = false, length = 80)
  private String nombres;

  @Column(name = "apellidos", nullable = false, length = 80)
  private String apellidos;

  @Column(name = "especialidad", length = 80)
  private String especialidad;

  @Column(name = "telefono", length = 15)
  private String telefono;

  @Column(name = "correo", length = 120, unique = true)
  private String correo;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  public Long getIdInstructor() {
    return idInstructor;
  }

  public void setIdInstructor(Long idInstructor) {
    this.idInstructor = idInstructor;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public String getEspecialidad() {
    return especialidad;
  }

  public void setEspecialidad(String especialidad) {
    this.especialidad = especialidad;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String correo) {
    this.correo = correo;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }
}
