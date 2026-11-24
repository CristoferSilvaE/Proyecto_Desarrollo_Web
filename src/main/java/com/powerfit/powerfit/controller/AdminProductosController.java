package com.powerfit.powerfit.controller;

import com.powerfit.powerfit.model.Pedido;
import com.powerfit.powerfit.model.Producto;
import com.powerfit.powerfit.repository.PedidoRepository;
import com.powerfit.powerfit.service.ProductoService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/productos")
public class AdminProductosController {

    private static final List<String> CATEGORIAS = List.of("Nutrición Deportiva", "Equipamiento", "Accesorios");

    private final ProductoService productoService;
    private final PedidoRepository pedidoRepository;

    public AdminProductosController(
            ProductoService productoService, PedidoRepository pedidoRepository) {
        this.productoService = productoService;
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "TODAS") String categoria,
            @RequestParam(defaultValue = "TODOS") String estado,
            Model model) {

        List<Producto> todosProductos = productoService.listarTodos();
        List<Pedido> todosPedidos = pedidoRepository.findAllByOrderByFechaPedidoDesc();

        String filtroCategoria = CATEGORIAS.contains(categoria) ? categoria : "TODAS";
        String filtroEstado = List.of("TODOS", "ACTIVO", "INACTIVO", "CRITICO")
                .contains(estado.toUpperCase(Locale.ROOT))
                        ? estado.toUpperCase(Locale.ROOT)
                        : "TODOS";

        List<Producto> filtrados = todosProductos.stream()
                .filter(
                        p -> "TODAS".equals(filtroCategoria)
                                || filtroCategoria.equalsIgnoreCase(p.getCategoria()))
                .filter(p -> coincideEstado(p, filtroEstado))
                .filter(p -> coincideBusqueda(p, buscar))
                .toList();

        // KPIs reales desde MySQL
        BigDecimal ingresosTienda = todosPedidos.stream()
                .filter(p -> "PAGADO".equalsIgnoreCase(p.getEstadoPago()))
                .filter(p -> !"CANCELADO".equalsIgnoreCase(p.getEstado()))
                .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pedidosTotales = todosPedidos.size();
        long pedidosPendientes = todosPedidos.stream()
                .filter(
                        p -> "PENDIENTE".equalsIgnoreCase(p.getEstado())
                                || "EN_PROCESO".equalsIgnoreCase(p.getEstado()))
                .count();

        long stockCritico = todosProductos.stream()
                .filter(p -> "ACTIVO".equalsIgnoreCase(p.getEstado()))
                .filter(this::tieneStockCritico)
                .count();

        long agotados = todosProductos.stream()
                .filter(p -> "ACTIVO".equalsIgnoreCase(p.getEstado()))
                .filter(p -> p.getStock() != null && p.getStock() <= 0)
                .count();

        long productosActivos = todosProductos.stream().filter(p -> "ACTIVO".equalsIgnoreCase(p.getEstado())).count();

        List<Pedido> pedidosRecientes = todosPedidos.stream().limit(5).toList();

        model.addAttribute("productos", filtrados);
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("buscar", buscar);
        model.addAttribute("categoriaFiltro", filtroCategoria);
        model.addAttribute("estadoFiltro", filtroEstado);

        model.addAttribute("ingresosTienda", ingresosTienda);
        model.addAttribute("pedidosTotales", pedidosTotales);
        model.addAttribute("pedidosPendientes", pedidosPendientes);
        model.addAttribute("stockCritico", stockCritico);
        model.addAttribute("agotados", agotados);
        model.addAttribute("totalCatalogo", todosProductos.size());
        model.addAttribute("productosActivos", productosActivos);
        model.addAttribute("pedidosRecientes", pedidosRecientes);

        return "admin/productos";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam(required = false) Long idProducto,
            @RequestParam String sku,
            @RequestParam String nombre,
            @RequestParam String categoria,
            @RequestParam BigDecimal precio,
            @RequestParam Integer stock,
            @RequestParam Integer stockMinimo,
            @RequestParam(defaultValue = "ACTIVO") String estado,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) MultipartFile imagen,
            RedirectAttributes redirect) {

        try {
            productoService.guardar(
                    idProducto, sku, nombre, categoria, precio, stock, stockMinimo, estado, descripcion, imagen);

            redirect.addFlashAttribute(
                    "mensaje",
                    idProducto == null
                            ? "Producto registrado correctamente en el catálogo."
                            : "Producto actualizado correctamente.");

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            redirect.addFlashAttribute("error", "No se pudo guardar la imagen del producto.");
        }

        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id, @RequestParam String estado, RedirectAttributes redirect) {

        try {
            productoService.cambiarEstado(id, estado);
            redirect.addFlashAttribute(
                    "mensaje",
                    "ACTIVO".equalsIgnoreCase(estado)
                            ? "Producto reactivado y visible en la tienda."
                            : "Producto desactivado. Ya no se mostrará en la tienda.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/productos";
    }

    private boolean tieneStockCritico(Producto p) {
        return p.getStock() != null && p.getStockMinimo() != null && p.getStock() <= p.getStockMinimo();
    }

    private boolean coincideEstado(Producto p, String filtroEstado) {
        return switch (filtroEstado) {
            case "ACTIVO" -> "ACTIVO".equalsIgnoreCase(p.getEstado());
            case "INACTIVO" -> "INACTIVO".equalsIgnoreCase(p.getEstado());
            case "CRITICO" -> "ACTIVO".equalsIgnoreCase(p.getEstado()) && tieneStockCritico(p);
            default -> true;
        };
    }

    private boolean coincideBusqueda(Producto p, String buscar) {
        if (buscar == null || buscar.isBlank()) {
            return true;
        }
        String termino = buscar.trim().toLowerCase(Locale.ROOT);
        return normalizar(p.getNombre()).contains(termino)
                || normalizar(p.getSku()).contains(termino)
                || normalizar(p.getCategoria()).contains(termino)
                || normalizar(p.getDescripcion()).contains(termino);
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.toLowerCase(Locale.ROOT);
    }
}