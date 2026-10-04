package com.powerfit.powerfit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "membresias")
public class Membresia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_membresia")
  private Long idMembresia;

  @ManyToOne
  @JoinColumn(name = "id_cliente", nullable = false)
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "id_plan", nullable = false)
  private PlanMembresia plan;

  @Column(name = "fecha_solicitud", nullable = false)
  private LocalDateTime fechaSolicitud;

  @Column(name = "fecha_inicio")
  private LocalDate fechaInicio;

  @Column(name = "fecha_fin")
  private LocalDate fechaFin;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  @Column(name = "observacion", length = 255)
  private String observacion;

  @PrePersist
  public void prePersist() {

    if (fechaSolicitud == null) {
      fechaSolicitud = LocalDateTime.now();
    }

    if (estado == null || estado.isBlank()) {
      estado = "PENDIENTE";
    }
  }

  public Long getIdMembresia() {
    return idMembresia;
  }

  public void setIdMembresia(Long idMembresia) {
    this.idMembresia = idMembresia;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public PlanMembresia getPlan() {
    return plan;
  }

  public void setPlan(PlanMembresia plan) {
    this.plan = plan;
  }

  public LocalDateTime getFechaSolicitud() {
    return fechaSolicitud;
  }

  public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
    this.fechaSolicitud = fechaSolicitud;
  }

  public LocalDate getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDate getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDate fechaFin) {
    this.fechaFin = fechaFin;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public String getObservacion() {
    return observacion;
  }

  public void setObservacion(String observacion) {
    this.observacion = observacion;
  }
}
