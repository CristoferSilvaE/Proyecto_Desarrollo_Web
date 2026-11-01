package com.powerfit.powerfit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sedes")
public class Sede {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_sede")
  private Long idSede;

  @Column(name = "nombre", nullable = false, length = 100)
  private String nombre;

  @Column(name = "direccion", nullable = false, length = 200)
  private String direccion;

  @Column(name = "distrito", nullable = false, length = 80)
  private String distrito;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado = "ACTIVO";

  public Long getIdSede() {
    return idSede;
  }

  public void setIdSede(Long idSede) {
    this.idSede = idSede;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public String getDistrito() {
    return distrito;
  }

  public void setDistrito(String distrito) {
    this.distrito = distrito;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }
}
