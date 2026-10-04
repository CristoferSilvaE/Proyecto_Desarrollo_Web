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
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos_membresia")
public class PagoMembresia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_pago")
  private Long idPago;

  @ManyToOne
  @JoinColumn(name = "id_membresia", nullable = false)
  private Membresia membresia;

  @Column(name = "fecha_pago", nullable = false)
  private LocalDateTime fechaPago;

  @Column(name = "monto", nullable = false, precision = 8, scale = 2)
  private BigDecimal monto;

  @Column(name = "metodo_pago", nullable = false, length = 30)
  private String metodoPago;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  public Long getIdPago() {
    return idPago;
  }

  public void setIdPago(Long idPago) {
    this.idPago = idPago;
  }

  public Membresia getMembresia() {
    return membresia;
  }

  public void setMembresia(Membresia membresia) {
    this.membresia = membresia;
  }

  public LocalDateTime getFechaPago() {
    return fechaPago;
  }

  public void setFechaPago(LocalDateTime fechaPago) {
    this.fechaPago = fechaPago;
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

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }
}
