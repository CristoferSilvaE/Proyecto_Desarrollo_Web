package com.powerfit.powerfit.service;

import com.powerfit.powerfit.dto.NotificacionAdminDTO;
import com.powerfit.powerfit.model.Membresia;
import com.powerfit.powerfit.model.Pedido;
import com.powerfit.powerfit.model.Producto;
import com.powerfit.powerfit.repository.MembresiaRepository;
import com.powerfit.powerfit.repository.PagoClaseRepository;
import com.powerfit.powerfit.repository.PedidoRepository;
import com.powerfit.powerfit.repository.ProductoRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminNotificacionService {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");

    private final MembresiaRepository membresiaRepository;
    private final PagoClaseRepository pagoClaseRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public AdminNotificacionService(
            MembresiaRepository membresiaRepository,
            PagoClaseRepository pagoClaseRepository,
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository) {
        this.membresiaRepository = membresiaRepository;
        this.pagoClaseRepository = pagoClaseRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificacionAdminDTO> obtenerNotificaciones() {
        List<NotificacionAdminDTO> lista = new ArrayList<>();
        LocalDate hoy = LocalDate.now(ZONA);

        // 1. Reembolsos pendientes de tienda
        List<Pedido> reembolsosTienda = pedidoRepository.findByEstadoPagoOrderByFechaPedidoDesc("REEMBOLSO_PENDIENTE");
        if (!reembolsosTienda.isEmpty()) {
            lista.add(
                    new NotificacionAdminDTO(
                            "Reembolsos de pedidos",
                            reembolsosTienda.size() + " devolución(es) pendiente(s) en tienda",
                            reembolsosTienda.size(),
                            "/admin/pedidos?estadoPago=REEMBOLSO_PENDIENTE",
                            "red"));
        }

        // 2. Reembolsos pendientes de clases
        long reembolsosClases = pagoClaseRepository.countByEstadoPago("REEMBOLSO_PENDIENTE");
        if (reembolsosClases > 0) {
            lista.add(
                    new NotificacionAdminDTO(
                            "Reembolsos de clases",
                            reembolsosClases + " devolución(es) de clase por procesar",
                            reembolsosClases,
                            "/admin/clases?tab=solicitudes",
                            "red"));
        }

        // 3. Productos con stock crítico o agotados
        List<Producto> productosActivos = productoRepository.findByEstadoOrderByNombreAsc("ACTIVO");
        long stockCritico = productosActivos.stream()
                .filter(
                        p -> p.getStock() != null
                                && p.getStockMinimo() != null
                                && p.getStock() <= p.getStockMinimo())
                .count();
        long agotados = productosActivos.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 0)
                .count();

        if (stockCritico > 0) {
            String detalle = agotados > 0
                    ? stockCritico + " producto(s) con stock bajo (" + agotados + " agotado/s)"
                    : stockCritico + " producto(s) en o debajo del mínimo";
            lista.add(
                    new NotificacionAdminDTO(
                            "Stock crítico en inventario",
                            detalle,
                            stockCritico,
                            "/admin/productos?estado=CRITICO",
                            agotados > 0 ? "red" : "orange"));
        }

        // 4. Pedidos pendientes o en proceso
        long pedidosPendientes = pedidoRepository.findByEstadoOrderByFechaPedidoDesc("PENDIENTE").size();
        long pedidosEnProceso = pedidoRepository.findByEstadoOrderByFechaPedidoDesc("EN_PROCESO").size();
        long pedidosPorAtender = pedidosPendientes + pedidosEnProceso;

        if (pedidosPorAtender > 0) {
            String urlPedidos = pedidosPendientes > 0
                    ? "/admin/pedidos?estado=PENDIENTE"
                    : "/admin/pedidos?estado=EN_PROCESO";
            lista.add(
                    new NotificacionAdminDTO(
                            "Pedidos por atender",
                            pedidosPendientes + " pendiente(s) · " + pedidosEnProceso + " en proceso",
                            pedidosPorAtender,
                            urlPedidos,
                            "orange"));
        }

        // 5. Solicitudes de membresías por revisar
        List<Membresia> membresiasPendientes = membresiaRepository.findByEstado("PENDIENTE");
        if (!membresiasPendientes.isEmpty()) {
            lista.add(
                    new NotificacionAdminDTO(
                            "Solicitudes de membresía",
                            membresiasPendientes.size() + " solicitud(es) esperando revisión",
                            membresiasPendientes.size(),
                            "/admin/membresias?estado=PENDIENTE",
                            "blue"));
        }

        // 6. Solicitudes de clases por revisar
        long solicitudesClases = pagoClaseRepository.countByEstadoPago("PENDIENTE");
        if (solicitudesClases > 0) {
            lista.add(
                    new NotificacionAdminDTO(
                            "Solicitudes de clases",
                            solicitudesClases + " pago(s) de clase por revisar",
                            solicitudesClases,
                            "/admin/clases?tab=solicitudes",
                            "blue"));
        }

        // 7. Membresías activas por vencer en los próximos 7 días
        long porVencer = membresiaRepository.findByEstado("ACTIVA").stream()
                .filter(m -> m.getFechaFin() != null)
                .filter(
                        m -> !m.getFechaFin().isBefore(hoy) && !m.getFechaFin().isAfter(hoy.plusDays(7)))
                .count();

        if (porVencer > 0) {
            lista.add(
                    new NotificacionAdminDTO(
                            "Membresías por vencer",
                            porVencer + " membresía(s) vencen en los próximos 7 días",
                            porVencer,
                            "/admin/membresias?estado=ACTIVA",
                            "orange"));
        }

        return lista;
    }
}