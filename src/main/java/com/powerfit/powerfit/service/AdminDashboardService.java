package com.powerfit.powerfit.service;

import com.powerfit.powerfit.dto.DashboardAdminDTO;
import com.powerfit.powerfit.dto.DashboardAdminDTO.HorarioHoyDTO;
import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.HorarioClase;
import com.powerfit.powerfit.model.Membresia;
import com.powerfit.powerfit.model.PagoClase;
import com.powerfit.powerfit.model.PagoMembresia;
import com.powerfit.powerfit.model.Pedido;
import com.powerfit.powerfit.model.Producto;
import com.powerfit.powerfit.repository.ClienteRepository;
import com.powerfit.powerfit.repository.HorarioClaseRepository;
import com.powerfit.powerfit.repository.MembresiaRepository;
import com.powerfit.powerfit.repository.PagoClaseRepository;
import com.powerfit.powerfit.repository.PagoMembresiaRepository;
import com.powerfit.powerfit.repository.PedidoRepository;
import com.powerfit.powerfit.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ClienteRepository clienteRepository;
    private final MembresiaRepository membresiaRepository;
    private final PagoMembresiaRepository pagoMembresiaRepository;
    private final PagoClaseRepository pagoClaseRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final HorarioClaseRepository horarioClaseRepository;
    private final HorarioClaseService horarioClaseService;

    public AdminDashboardService(
            ClienteRepository clienteRepository,
            MembresiaRepository membresiaRepository,
            PagoMembresiaRepository pagoMembresiaRepository,
            PagoClaseRepository pagoClaseRepository,
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            HorarioClaseRepository horarioClaseRepository,
            HorarioClaseService horarioClaseService) {
        this.clienteRepository = clienteRepository;
        this.membresiaRepository = membresiaRepository;
        this.pagoMembresiaRepository = pagoMembresiaRepository;
        this.pagoClaseRepository = pagoClaseRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.horarioClaseRepository = horarioClaseRepository;
        this.horarioClaseService = horarioClaseService;
    }

    @Transactional(readOnly = true)
    public DashboardAdminDTO obtenerResumen() {
        LocalDate hoy = LocalDate.now(ZONA);
        LocalTime ahora = LocalTime.now(ZONA);

        // 1. CLIENTES ACTIVOS
        List<Cliente> clientes = clienteRepository.findAll();
        long clientesActivos = clientes.stream().filter(this::esClienteActivo).count();
        long clientesTotales = clientes.size();

        // 2. MEMBRESÍAS (VENDIDAS, ACTIVAS, VENCIDAS Y POR VENCER)
        List<Membresia> membresias = membresiaRepository.findAll();
        List<PagoMembresia> pagosMembresia = pagoMembresiaRepository.findAll();

        long membresiasVendidas = pagosMembresia.stream().filter(p -> "PAGADO".equalsIgnoreCase(p.getEstado())).count();

        long membresiasActivas = membresias.stream()
                .filter(
                        m -> "ACTIVA".equalsIgnoreCase(m.getEstado())
                                && (m.getFechaFin() == null || !m.getFechaFin().isBefore(hoy)))
                .count();

        long membresiasVencidas = membresias.stream()
                .filter(
                        m -> "VENCIDA".equalsIgnoreCase(m.getEstado())
                                || ("ACTIVA".equalsIgnoreCase(m.getEstado())
                                        && m.getFechaFin() != null
                                        && m.getFechaFin().isBefore(hoy)))
                .count();

        long membresiasPorVencer = membresias.stream()
                .filter(m -> "ACTIVA".equalsIgnoreCase(m.getEstado()))
                .filter(m -> m.getFechaFin() != null)
                .filter(
                        m -> !m.getFechaFin().isBefore(hoy) && !m.getFechaFin().isAfter(hoy.plusDays(7)))
                .count();

        // 3. INGRESOS MENSUALES (MEMBRESÍAS + CLASES + TIENDA)
        BigDecimal ingresosMembresiasMes = pagosMembresia.stream()
                .filter(p -> "PAGADO".equalsIgnoreCase(p.getEstado()))
                .filter(p -> esDelMesActual(p.getFechaPago(), hoy))
                .map(p -> p.getMonto() != null ? p.getMonto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PagoClase> pagosClases = pagoClaseRepository.findAll();

        BigDecimal ingresosClasesMes = pagosClases.stream()
                .filter(p -> "PAGADO".equalsIgnoreCase(p.getEstadoPago()))
                .filter(
                        p -> esDelMesActual(
                                p.getFechaPago() != null ? p.getFechaPago() : p.getFechaRegistro(), hoy))
                .map(p -> p.getMonto() != null ? p.getMonto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Pedido> pedidos = pedidoRepository.findAll();

        BigDecimal ingresosTiendaMes = pedidos.stream()
                .filter(p -> "PAGADO".equalsIgnoreCase(p.getEstadoPago()))
                .filter(p -> !"CANCELADO".equalsIgnoreCase(p.getEstado()))
                .filter(
                        p -> esDelMesActual(
                                p.getFechaPago() != null ? p.getFechaPago() : p.getFechaPedido(), hoy))
                .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresosMensuales = ingresosMembresiasMes.add(ingresosClasesMes).add(ingresosTiendaMes);

        // 4. CLASES DE HOY Y HORARIO DEL DÍA
        List<HorarioClase> horariosHoyEntidad = horarioClaseRepository.findByFechaAndEstadoOrderByHoraInicioAsc(hoy,
                "ACTIVO");

        Map<Long, Long> reservasPorHorario = horarioClaseService.obtenerReservasPorHorario(horariosHoyEntidad);

        List<HorarioHoyDTO> horariosHoy = new ArrayList<>();
        long reservasHoyTotal = 0;
        long capacidadHoyTotal = 0;

        for (HorarioClase h : horariosHoyEntidad) {
            long reservadas = reservasPorHorario.getOrDefault(h.getIdHorario(), 0L);
            int capacidad = h.getCapacidad() != null ? h.getCapacidad() : 0;

            reservasHoyTotal += reservadas;
            capacidadHoyTotal += capacidad;

            String horaRango = h.getHoraInicio().format(FORMATO_HORA) + " - " + h.getHoraFin().format(FORMATO_HORA);

            String nombreClase = h.getClase() != null ? h.getClase().getNombre() : "Sin clase";
            String instructor = h.getInstructor() != null
                    ? h.getInstructor().getNombres() + " " + h.getInstructor().getApellidos()
                    : "Por asignar";

            String estadoTexto;
            String badgeClase;

            if (ahora.isAfter(h.getHoraFin())) {
                estadoTexto = "FINALIZADA";
                badgeClase = "";
            } else if (!ahora.isBefore(h.getHoraInicio()) && !ahora.isAfter(h.getHoraFin())) {
                estadoTexto = "EN CURSO";
                badgeClase = "green";
            } else if (capacidad > 0 && reservadas >= capacidad) {
                estadoTexto = "CUPOS LLENOS";
                badgeClase = "orange";
            } else {
                estadoTexto = "PROGRAMADA";
                badgeClase = "";
            }

            horariosHoy.add(
                    new HorarioHoyDTO(
                            horaRango,
                            nombreClase,
                            h.getSala(),
                            instructor,
                            reservadas,
                            capacidad,
                            estadoTexto,
                            badgeClase));
        }

        // 5. ALERTAS DEL SISTEMA
        List<Producto> productosActivos = productoRepository.findByEstadoOrderByNombreAsc("ACTIVO");
        long productosStockCritico = productosActivos.stream()
                .filter(
                        p -> p.getStock() != null
                                && p.getStockMinimo() != null
                                && p.getStock() <= p.getStockMinimo())
                .count();

        long pedidosPendientes = pedidos.stream()
                .filter(
                        p -> "PENDIENTE".equalsIgnoreCase(p.getEstado())
                                || "EN_PROCESO".equalsIgnoreCase(p.getEstado()))
                .count();

        long reembolsosPedidos = pedidos.stream()
                .filter(p -> "REEMBOLSO_PENDIENTE".equalsIgnoreCase(p.getEstadoPago()))
                .count();

        long reembolsosClases = pagosClases.stream()
                .filter(p -> "REEMBOLSO_PENDIENTE".equalsIgnoreCase(p.getEstadoPago()))
                .count();

        long solicitudesClasesPendientes = pagoClaseRepository.countByEstadoPago("PENDIENTE");

        return new DashboardAdminDTO(
                clientesActivos,
                clientesTotales,
                membresiasVendidas,
                membresiasActivas,
                membresiasVencidas,
                ingresosMensuales,
                ingresosMembresiasMes,
                ingresosClasesMes,
                ingresosTiendaMes,
                horariosHoy.size(),
                reservasHoyTotal,
                capacidadHoyTotal,
                horariosHoy,
                productosStockCritico,
                membresiasPorVencer,
                pedidosPendientes,
                reembolsosPedidos + reembolsosClases,
                solicitudesClasesPendientes);
    }

    private boolean esClienteActivo(Cliente cliente) {
        return cliente != null
                && "ACTIVO".equalsIgnoreCase(cliente.getEstado())
                && cliente.getUsuario() != null
                && "ACTIVO".equalsIgnoreCase(cliente.getUsuario().getEstado())
                && cliente.getUsuario().getRol() != null
                && "CLIENTE".equalsIgnoreCase(cliente.getUsuario().getRol().getNombre());
    }

    private boolean esDelMesActual(LocalDateTime fecha, LocalDate hoy) {
        if (fecha == null) {
            return false;
        }
        return fecha.getYear() == hoy.getYear() && fecha.getMonth() == hoy.getMonth();
    }
}