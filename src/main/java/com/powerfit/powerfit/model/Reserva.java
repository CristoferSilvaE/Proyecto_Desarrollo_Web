package com.powerfit.powerfit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "reservas",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_cliente_horario",
          columnNames = {"id_cliente", "id_horario"})
    })
public class Reserva {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_reserva")
  private Long idReserva;

  @ManyToOne
  @JoinColumn(name = "id_cliente", nullable = false)
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "id_horario", nullable = false)
  private HorarioClase horario;

  @Column(name = "fecha_reserva", nullable = false)
  private LocalDateTime fechaReserva;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  @Column(name = "asistencia")
  private Boolean asistencia;

  public Long getIdReserva() {
    return idReserva;
  }

  public void setIdReserva(Long idReserva) {
    this.idReserva = idReserva;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public HorarioClase getHorario() {
    return horario;
  }

  public void setHorario(HorarioClase horario) {
    this.horario = horario;
  }

  public LocalDateTime getFechaReserva() {
    return fechaReserva;
  }

  public void setFechaReserva(LocalDateTime fechaReserva) {
    this.fechaReserva = fechaReserva;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public Boolean getAsistencia() {
    return asistencia;
  }

  public void setAsistencia(Boolean asistencia) {
    this.asistencia = asistencia;
  }
}
