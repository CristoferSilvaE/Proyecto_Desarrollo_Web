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
@Table(name = "pedidos")
public class Pedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_pedido")
  private Long idPedido;

  @ManyToOne
  @JoinColumn(name = "id_cliente", nullable = false)
  private Cliente cliente;

  @Column(name = "fecha_pedido", nullable = false)
  private LocalDateTime fechaPedido;

  @Column(name = "metodo_entrega", nullable = false, length = 30)
  private String metodoEntrega;

  @Column(name = "tipo_comprobante", nullable = false, length = 20)
  private String tipoComprobante;

  @Column(name = "metodo_pago", nullable = false, length = 30)
  private String metodoPago;

  @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
  private BigDecimal costoEnvio;

  @Column(name = "total", nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  @Column(name = "estado", nullable = false, length = 30)
  private String estado;

  @Column(name = "estado_pago", nullable = false, length = 20)
  private String estadoPago;

  @Column(name = "direccion_entrega", length = 200)
  private String direccionEntrega;

  @Column(name = "distrito", length = 80)
  private String distrito;

  @Column(name = "referencia", length = 200)
  private String referencia;

  @Column(name = "ruc", length = 11)
  private String ruc;

  @Column(name = "razon_social", length = 150)
  private String razonSocial;

  @PrePersist
  public void prePersist() {

    if (fechaPedido == null) {
      fechaPedido = LocalDateTime.now();
    }

    if (estado == null || estado.isBlank()) {
      estado = "PENDIENTE";
    }

    if (estadoPago == null || estadoPago.isBlank()) {
      estadoPago = "PENDIENTE";
    }

    if (costoEnvio == null) {
      costoEnvio = BigDecimal.ZERO;
    }
  }

  public Long getIdPedido() {
    return idPedido;
  }

  public void setIdPedido(Long idPedido) {
    this.idPedido = idPedido;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public LocalDateTime getFechaPedido() {
    return fechaPedido;
  }

  public void setFechaPedido(LocalDateTime fechaPedido) {
    this.fechaPedido = fechaPedido;
  }

  public String getMetodoEntrega() {
    return metodoEntrega;
  }

  public void setMetodoEntrega(String metodoEntrega) {
    this.metodoEntrega = metodoEntrega;
  }

  public String getTipoComprobante() {
    return tipoComprobante;
  }

  public void setTipoComprobante(String tipoComprobante) {
    this.tipoComprobante = tipoComprobante;
  }

  public String getMetodoPago() {
    return metodoPago;
  }

  public void setMetodoPago(String metodoPago) {
    this.metodoPago = metodoPago;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
  }

  public BigDecimal getCostoEnvio() {
    return costoEnvio;
  }

  public void setCostoEnvio(BigDecimal costoEnvio) {
    this.costoEnvio = costoEnvio;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public void setTotal(BigDecimal total) {
    this.total = total;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public String getEstadoPago() {
    return estadoPago;
  }

  public void setEstadoPago(String estadoPago) {
    this.estadoPago = estadoPago;
  }

  public String getDireccionEntrega() {
    return direccionEntrega;
  }

  public void setDireccionEntrega(String direccionEntrega) {
    this.direccionEntrega = direccionEntrega;
  }

  public String getDistrito() {
    return distrito;
  }

  public void setDistrito(String distrito) {
    this.distrito = distrito;
  }

  public String getReferencia() {
    return referencia;
  }

  public void setReferencia(String referencia) {
    this.referencia = referencia;
  }

  public String getRuc() {
    return ruc;
  }

  public void setRuc(String ruc) {
    this.ruc = ruc;
  }

  public String getRazonSocial() {
    return razonSocial;
  }

  public void setRazonSocial(String razonSocial) {
    this.razonSocial = razonSocial;
  }
}
