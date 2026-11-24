package com.powerfit.powerfit.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardAdminDTO {

  private final long clientesActivos;
  private final long clientesTotales;

  private final long membresiasVendidas;
  private final long membresiasActivas;
  private final long membresiasVencidas;

  private final BigDecimal ingresosMensuales;
  private final BigDecimal ingresosMembresiasMes;
  private final BigDecimal ingresosClasesMes;
  private final BigDecimal ingresosTiendaMes;

  private final long clasesHoyTotal;
  private final long reservasHoyTotal;
  private final long capacidadHoyTotal;

  private final List<HorarioHoyDTO> horariosHoy;

  private final long productosStockCritico;
  private final long membresiasPorVencer;
  private final long pedidosPendientes;
  private final long reembolsosPendientes;
  private final long solicitudesClasesPendientes;

  public DashboardAdminDTO(
      long clientesActivos,
      long clientesTotales,
      long membresiasVendidas,
      long membresiasActivas,
      long membresiasVencidas,
      BigDecimal ingresosMensuales,
      BigDecimal ingresosMembresiasMes,
      BigDecimal ingresosClasesMes,
      BigDecimal ingresosTiendaMes,
      long clasesHoyTotal,
      long reservasHoyTotal,
      long capacidadHoyTotal,
      List<HorarioHoyDTO> horariosHoy,
      long productosStockCritico,
      long membresiasPorVencer,
      long pedidosPendientes,
      long reembolsosPendientes,
      long solicitudesClasesPendientes) {
    this.clientesActivos = clientesActivos;
    this.clientesTotales = clientesTotales;
    this.membresiasVendidas = membresiasVendidas;
    this.membresiasActivas = membresiasActivas;
    this.membresiasVencidas = membresiasVencidas;
    this.ingresosMensuales = ingresosMensuales;
    this.ingresosMembresiasMes = ingresosMembresiasMes;
    this.ingresosClasesMes = ingresosClasesMes;
    this.ingresosTiendaMes = ingresosTiendaMes;
    this.clasesHoyTotal = clasesHoyTotal;
    this.reservasHoyTotal = reservasHoyTotal;
    this.capacidadHoyTotal = capacidadHoyTotal;
    this.horariosHoy = horariosHoy;
    this.productosStockCritico = productosStockCritico;
    this.membresiasPorVencer = membresiasPorVencer;
    this.pedidosPendientes = pedidosPendientes;
    this.reembolsosPendientes = reembolsosPendientes;
    this.solicitudesClasesPendientes = solicitudesClasesPendientes;
  }

  public long getClientesActivos() {
    return clientesActivos;
  }

  public long getClientesTotales() {
    return clientesTotales;
  }

  public long getMembresiasVendidas() {
    return membresiasVendidas;
  }

  public long getMembresiasActivas() {
    return membresiasActivas;
  }

  public long getMembresiasVencidas() {
    return membresiasVencidas;
  }

  public BigDecimal getIngresosMensuales() {
    return ingresosMensuales;
  }

  public BigDecimal getIngresosMembresiasMes() {
    return ingresosMembresiasMes;
  }

  public BigDecimal getIngresosClasesMes() {
    return ingresosClasesMes;
  }

  public BigDecimal getIngresosTiendaMes() {
    return ingresosTiendaMes;
  }

  public long getClasesHoyTotal() {
    return clasesHoyTotal;
  }

  public long getReservasHoyTotal() {
    return reservasHoyTotal;
  }

  public long getCapacidadHoyTotal() {
    return capacidadHoyTotal;
  }

  public List<HorarioHoyDTO> getHorariosHoy() {
    return horariosHoy;
  }

  public long getProductosStockCritico() {
    return productosStockCritico;
  }

  public long getMembresiasPorVencer() {
    return membresiasPorVencer;
  }

  public long getPedidosPendientes() {
    return pedidosPendientes;
  }

  public long getReembolsosPendientes() {
    return reembolsosPendientes;
  }

  public long getSolicitudesClasesPendientes() {
    return solicitudesClasesPendientes;
  }

  public static class HorarioHoyDTO {

    private final String horaRango;
    private final String nombreClase;
    private final String sala;
    private final String instructor;
    private final long reservadas;
    private final int capacidad;
    private final String estadoTexto;
    private final String badgeClase;

    public HorarioHoyDTO(
        String horaRango,
        String nombreClase,
        String sala,
        String instructor,
        long reservadas,
        int capacidad,
        String estadoTexto,
        String badgeClase) {
      this.horaRango = horaRango;
      this.nombreClase = nombreClase;
      this.sala = sala;
      this.instructor = instructor;
      this.reservadas = reservadas;
      this.capacidad = capacidad;
      this.estadoTexto = estadoTexto;
      this.badgeClase = badgeClase;
    }

    public String getHoraRango() {
      return horaRango;
    }

    public String getNombreClase() {
      return nombreClase;
    }

    public String getSala() {
      return sala;
    }

    public String getInstructor() {
      return instructor;
    }

    public long getReservadas() {
      return reservadas;
    }

    public int getCapacidad() {
      return capacidad;
    }

    public String getEstadoTexto() {
      return estadoTexto;
    }

    public String getBadgeClase() {
      return badgeClase;
    }
  }
}