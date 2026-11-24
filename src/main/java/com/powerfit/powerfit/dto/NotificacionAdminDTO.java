package com.powerfit.powerfit.dto;

public class NotificacionAdminDTO {

    private final String titulo;
    private final String detalle;
    private final long cantidad;
    private final String url;
    private final String nivel; // red | orange | blue

    public NotificacionAdminDTO(
            String titulo, String detalle, long cantidad, String url, String nivel) {
        this.titulo = titulo;
        this.detalle = detalle;
        this.cantidad = cantidad;
        this.url = url;
        this.nivel = nivel;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDetalle() {
        return detalle;
    }

    public long getCantidad() {
        return cantidad;
    }

    public String getUrl() {
        return url;
    }

    public String getNivel() {
        return nivel;
    }
}