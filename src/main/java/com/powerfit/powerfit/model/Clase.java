package com.powerfit.powerfit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clases")
public class Clase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_clase")
  private Long idClase;

  @Column(name = "nombre", nullable = false, length = 80)
  private String nombre;

  @Column(name = "descripcion", length = 200)
  private String descripcion;

  @Column(name = "categoria", length = 50)
  private String categoria;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  @Column(name = "imagen", length = 255)
  private String imagen;

  public Long getIdClase() {
    return idClase;
  }

  public void setIdClase(Long idClase) {
    this.idClase = idClase;
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

  public String getCategoria() {
    return categoria;
  }

  public void setCategoria(String categoria) {
    this.categoria = categoria;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public String getImagen() {
    return imagen;
  }

  public void setImagen(String imagen) {
    this.imagen = imagen;
  }
}
