package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Producto;
import com.powerfit.powerfit.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CarritoService {

  private static final String CARRITO_SESSION = "carrito";

  private final ProductoRepository productoRepository;

  public CarritoService(ProductoRepository productoRepository) {
    this.productoRepository = productoRepository;
  }

  // Obtener el carrito de la sesión
  @SuppressWarnings("unchecked")
  private Map<Long, Integer> obtenerCarrito(HttpSession session) {

    Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute(CARRITO_SESSION);

    if (carrito == null) {
      carrito = new LinkedHashMap<>();
      session.setAttribute(CARRITO_SESSION, carrito);
    }

    return carrito;
  }

  // Agregar un producto
  public void agregar(Long idProducto, HttpSession session) {

    Producto producto =
        productoRepository
            .findById(idProducto)
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

    if (!"ACTIVO".equals(producto.getEstado())) {
      throw new IllegalArgumentException("Este producto no está disponible.");
    }

    int stock = producto.getStock() == null ? 0 : producto.getStock();

    Map<Long, Integer> carrito = obtenerCarrito(session);

    int cantidadActual = carrito.getOrDefault(idProducto, 0);

    if (cantidadActual >= stock) {
      throw new IllegalArgumentException(
          "No hay más unidades disponibles de " + producto.getNombre());
    }

    carrito.put(idProducto, cantidadActual + 1);
  }

  // Aumentar o disminuir cantidad
  public void cambiarCantidad(Long idProducto, int cambio, HttpSession session) {

    Map<Long, Integer> carrito = obtenerCarrito(session);

    if (!carrito.containsKey(idProducto)) {
      throw new IllegalArgumentException("El producto no está en el carrito.");
    }

    int cantidadActual = carrito.get(idProducto);
    int nuevaCantidad = cantidadActual + cambio;

    if (nuevaCantidad <= 0) {
      carrito.remove(idProducto);
      return;
    }

    Producto producto =
        productoRepository
            .findById(idProducto)
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

    if (cambio > 0) {

      if (!"ACTIVO".equals(producto.getEstado())) {
        throw new IllegalArgumentException("Este producto ya no está disponible.");
      }

      int stock = producto.getStock() == null ? 0 : producto.getStock();

      if (nuevaCantidad > stock) {
        throw new IllegalArgumentException("Solo hay " + stock + " unidades disponibles.");
      }
    }

    carrito.put(idProducto, nuevaCantidad);
  }

  // Eliminar un producto
  public void eliminar(Long idProducto, HttpSession session) {
    obtenerCarrito(session).remove(idProducto);
  }

  // Vaciar el carrito
  public void vaciar(HttpSession session) {
    obtenerCarrito(session).clear();
  }

  // Obtener los productos con sus datos actuales de MySQL
  public List<ItemCarrito> obtenerItems(HttpSession session) {

    Map<Long, Integer> carrito = obtenerCarrito(session);

    List<ItemCarrito> items = new ArrayList<>();

    for (Map.Entry<Long, Integer> entrada : carrito.entrySet()) {

      productoRepository
          .findById(entrada.getKey())
          .ifPresent(producto -> {
            int cantidad = entrada.getValue();

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));

            items.add(new ItemCarrito(producto, cantidad, subtotal));
          });
    }

    return items;
  }

  // Calcular subtotal
  public BigDecimal obtenerSubtotal(HttpSession session) {

    return obtenerItems(session).stream()
        .map(ItemCarrito::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  // Contar unidades
  public int obtenerCantidadTotal(HttpSession session) {

    return obtenerItems(session).stream().mapToInt(ItemCarrito::getCantidad).sum();
  }

  // Representa un producto dentro del carrito
  public static class ItemCarrito {

    private final Producto producto;
    private final int cantidad;
    private final BigDecimal subtotal;

    public ItemCarrito(Producto producto, int cantidad, BigDecimal subtotal) {

      this.producto = producto;
      this.cantidad = cantidad;
      this.subtotal = subtotal;
    }

    public Producto getProducto() {
      return producto;
    }

    public int getCantidad() {
      return cantidad;
    }

    public BigDecimal getSubtotal() {
      return subtotal;
    }
  }
}
