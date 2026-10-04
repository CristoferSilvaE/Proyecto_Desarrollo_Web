package com.powerfit.powerfit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "planes_membresia")
public class PlanMembresia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_plan")
  private Long idPlan;

  @Column(name = "nombre", nullable = false, length = 30, unique = true)
  private String nombre;

  @Column(name = "descripcion", length = 200)
  private String descripcion;

  @Column(name = "precio", nullable = false, precision = 8, scale = 2)
  private BigDecimal precio;

  @Column(name = "duracion_dias", nullable = false)
  private Integer duracionDias;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  public Long getIdPlan() {
    return idPlan;
  }

  public void setIdPlan(Long idPlan) {
    this.idPlan = idPlan;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public BigDecimal getPrecio() {
    return precio;
  }

  public void setPrecio(BigDecimal precio) {
    this.precio = precio;
  }

  public Integer getDuracionDias() {
    return duracionDias;
  }

  public void setDuracionDias(Integer duracionDias) {
    this.duracionDias = duracionDias;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }
}
