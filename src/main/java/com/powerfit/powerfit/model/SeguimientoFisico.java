package com.powerfit.powerfit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "seguimiento_fisico")
public class SeguimientoFisico {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_seguimiento")
  private Long idSeguimiento;

  @ManyToOne
  @JoinColumn(name = "id_cliente", nullable = false)
  private Cliente cliente;

  @Column(name = "fecha_registro", nullable = false)
  private LocalDate fechaRegistro;

  @Column(name = "peso", precision = 5, scale = 2)
  private BigDecimal peso;

  @Column(name = "altura", precision = 4, scale = 2)
  private BigDecimal altura;

  @Column(name = "porcentaje_grasa", precision = 5, scale = 2)
  private BigDecimal porcentajeGrasa;

  @Column(name = "masa_muscular", precision = 5, scale = 2)
  private BigDecimal masaMuscular;

  @Column(name = "observaciones", length = 255)
  private String observaciones;

  public Long getIdSeguimiento() {
    return idSeguimiento;
  }

  public void setIdSeguimiento(Long idSeguimiento) {
    this.idSeguimiento = idSeguimiento;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public LocalDate getFechaRegistro() {
    return fechaRegistro;
  }

  public void setFechaRegistro(LocalDate fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }

  public BigDecimal getPeso() {
    return peso;
  }

  public void setPeso(BigDecimal peso) {
    this.peso = peso;
  }

  public BigDecimal getAltura() {
    return altura;
  }

  public void setAltura(BigDecimal altura) {
    this.altura = altura;
  }

  public BigDecimal getPorcentajeGrasa() {
    return porcentajeGrasa;
  }

  public void setPorcentajeGrasa(BigDecimal porcentajeGrasa) {
    this.porcentajeGrasa = porcentajeGrasa;
  }

  public BigDecimal getMasaMuscular() {
    return masaMuscular;
  }

  public void setMasaMuscular(BigDecimal masaMuscular) {
    this.masaMuscular = masaMuscular;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }
}
