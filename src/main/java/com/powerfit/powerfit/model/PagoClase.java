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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos_clases")
public class PagoClase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_pago_clase")
  private Long idPagoClase;

  @ManyToOne
  @JoinColumn(name = "id_reserva", nullable = false)
  private Reserva reserva;

  @ManyToOne
  @JoinColumn(name = "id_membresia")
  private Membresia membresia;

  @Column(name = "tipo_reserva", nullable = false, length = 20)
  private String tipoReserva;

  @Column(name = "monto", nullable = false, precision = 10, scale = 2)
  private BigDecimal monto;

  @Column(name = "metodo_pago", nullable = false, length = 30)
  private String metodoPago;

  @Column(name = "estado_pago", nullable = false, length = 20)
  private String estadoPago;

  @Column(name = "fecha_registro", nullable = false)
  private LocalDateTime fechaRegistro;

  @Column(name = "fecha_pago")
  private LocalDateTime fechaPago;

  @Column(name = "observacion", length = 255)
  private String observacion;

  @PrePersist
  public void prePersist() {
    if (fechaRegistro == null) {
      fechaRegistro = LocalDateTime.now();
    }
  }

  public Long getIdPagoClase() {
    return idPagoClase;
  }

  public void setIdPagoClase(Long idPagoClase) {
    this.idPagoClase = idPagoClase;
  }

  public Reserva getReserva() {
    return reserva;
  }

  public void setReserva(Reserva reserva) {
    this.reserva = reserva;
  }

  public Membresia getMembresia() {
    return membresia;
  }

  public void setMembresia(Membresia membresia) {
    this.membresia = membresia;
  }

  public String getTipoReserva() {
    return tipoReserva;
  }

  public void setTipoReserva(String tipoReserva) {
    this.tipoReserva = tipoReserva;
  }

  public BigDecimal getMonto() {
    return monto;
  }

  public void setMonto(BigDecimal monto) {
    this.monto = monto;
  }

  public String getMetodoPago() {
    return metodoPago;
  }

  public void setMetodoPago(String metodoPago) {
    this.metodoPago = metodoPago;
  }

  public String getEstadoPago() {
    return estadoPago;
  }

  public void setEstadoPago(String estadoPago) {
    this.estadoPago = estadoPago;
  }

  public LocalDateTime getFechaRegistro() {
    return fechaRegistro;
  }

  public void setFechaRegistro(LocalDateTime fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }

  public LocalDateTime getFechaPago() {
    return fechaPago;
  }

  public void setFechaPago(LocalDateTime fechaPago) {
    this.fechaPago = fechaPago;
  }

  public String getObservacion() {
    return observacion;
  }

  public void setObservacion(String observacion) {
    this.observacion = observacion;
  }
}
