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
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_usuario")
  private Long idUsuario;

  @ManyToOne
  @JoinColumn(name = "id_rol", nullable = false)
  private Rol rol;

  @Column(name = "correo", nullable = false, length = 120, unique = true)
  private String correo;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  @Column(name = "fecha_creacion", nullable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "cambio_password_pendiente", nullable = false)
  private Boolean cambioPasswordPendiente = false;

  @PrePersist
  public void prePersist() {

    if (estado == null || estado.isBlank()) {
      estado = "ACTIVO";
    }

    if (fechaCreacion == null) {
      fechaCreacion = LocalDateTime.now();
    }

    if (cambioPasswordPendiente == null) {
      cambioPasswordPendiente = false;
    }
  }

  public Long getIdUsuario() {
    return idUsuario;
  }

  public void setIdUsuario(Long idUsuario) {
    this.idUsuario = idUsuario;
  }

  public Rol getRol() {
    return rol;
  }

  public void setRol(Rol rol) {
    this.rol = rol;
  }

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String correo) {
    this.correo = correo;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(LocalDateTime fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  public Boolean getCambioPasswordPendiente() {
    return cambioPasswordPendiente;
  }

  public void setCambioPasswordPendiente(Boolean cambioPasswordPendiente) {
    this.cambioPasswordPendiente = cambioPasswordPendiente;
  }
}
