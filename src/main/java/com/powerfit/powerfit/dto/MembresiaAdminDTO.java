package com.powerfit.powerfit.dto;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Membresia;
import com.powerfit.powerfit.model.PagoMembresia;
import java.math.BigDecimal;

public class MembresiaAdminDTO {

  private final Membresia membresia;
  private final PagoMembresia ultimoPago;

  public MembresiaAdminDTO(Membresia membresia, PagoMembresia ultimoPago) {

    this.membresia = membresia;
    this.ultimoPago = ultimoPago;
  }

  public Membresia getMembresia() {
    return membresia;
  }

  public PagoMembresia getUltimoPago() {
    return ultimoPago;
  }

  public String getNombreCliente() {
    Cliente cliente = membresia.getCliente();

    return cliente.getNombres() + " " + cliente.getApellidos();
  }

  public String getCorreoCliente() {
    return membresia.getCliente().getUsuario().getCorreo();
  }

  public String getDniCliente() {
    return membresia.getCliente().getDni();
  }

  public BigDecimal getMontoTotal() {

    if (membresia.getPlan() == null || membresia.getPlan().getPrecio() == null) {
      return BigDecimal.ZERO;
    }

    int meses = membresia.getDuracionMeses() != null ? membresia.getDuracionMeses() : 1;

    return membresia.getPlan().getPrecio().multiply(BigDecimal.valueOf(meses));
  }

  public String getMetodoPago() {

    if (ultimoPago != null) {
      return ultimoPago.getMetodoPago();
    }

    return membresia.getMetodoPagoPreferido() != null ? membresia.getMetodoPagoPreferido() : "—";
  }

  public String getEstadoPago() {

    return ultimoPago != null ? ultimoPago.getEstado() : "SIN PAGO";
  }
}
