package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.dto.NotificacionAdminDTO;
import com.powerfit.powerfit.service.AdminNotificacionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AdminNotificacionAdvice {

    private final AdminNotificacionService notificacionService;

    public AdminNotificacionAdvice(AdminNotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @ModelAttribute
    public void cargarNotificacionesAdmin(HttpServletRequest request, Model model) {
        if (request == null || !"GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/admin")) {
            return;
        }

        if (uri.endsWith("/detalle")
                || uri.endsWith("/comprobante")
                || uri.endsWith("/exportar")) {
            return;
        }

        List<NotificacionAdminDTO> notificaciones = notificacionService.obtenerNotificaciones();
        long total = notificaciones.stream().mapToLong(NotificacionAdminDTO::getCantidad).sum();

        model.addAttribute("notificacionesAdmin", notificaciones);
        model.addAttribute("totalNotificacionesAdmin", total);
    }
}