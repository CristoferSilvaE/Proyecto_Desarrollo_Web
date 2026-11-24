package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.DetallePedido;
import com.powerfit.powerfit.model.Pedido;
import com.powerfit.powerfit.repository.DetallePedidoRepository;
import com.powerfit.powerfit.repository.PedidoRepository;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComprobantePedidoPdfService {

  private final PedidoRepository pedidoRepository;
  private final DetallePedidoRepository detallePedidoRepository;

  private static final Color AZUL = new Color(75, 110, 255);
  private static final Color OSCURO = new Color(20, 23, 34);
  private static final Color GRIS = new Color(105, 112, 130);
  private static final Color FONDO = new Color(245, 247, 252);
  private static final Color BORDE = new Color(220, 225, 235);
  private static final Color VERDE = new Color(20, 145, 105);
  private static final Color AMBAR = new Color(195, 120, 25);
  private static final Color ROJO = new Color(180, 55, 65);

  public ComprobantePedidoPdfService(
      PedidoRepository pedidoRepository, DetallePedidoRepository detallePedidoRepository) {
    this.pedidoRepository = pedidoRepository;
    this.detallePedidoRepository = detallePedidoRepository;
  }

  @Transactional(readOnly = true)
  public byte[] generarComprobanteCliente(Long idPedido, Long idCliente) {
    Pedido pedido = obtenerPedidoPagado(idPedido);

    if (pedido.getCliente() == null
        || !Objects.equals(pedido.getCliente().getIdCliente(), idCliente)) {
      throw new SecurityException("No tienes permiso para consultar este comprobante.");
    }

    List<DetallePedido> detalles = detallePedidoRepository.buscarDetalles(idPedido);
    return construirPdf(pedido, detalles);
  }

  @Transactional(readOnly = true)
  public byte[] generarComprobanteAdmin(Long idPedido) {
    Pedido pedido = obtenerPedidoPagado(idPedido);
    List<DetallePedido> detalles = detallePedidoRepository.buscarDetalles(idPedido);
    return construirPdf(pedido, detalles);
  }

  private Pedido obtenerPedidoPagado(Long idPedido) {
    Pedido pedido =
        pedidoRepository
            .findById(idPedido)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el pedido."));

    String estadoPago = pedido.getEstadoPago();
    boolean permitido =
        "PAGADO".equalsIgnoreCase(estadoPago)
            || "REEMBOLSO_PENDIENTE".equalsIgnoreCase(estadoPago)
            || "REEMBOLSADO".equalsIgnoreCase(estadoPago);

    if (!permitido) {
      throw new IllegalStateException(
          "Solo se pueden generar comprobantes de pedidos con pago registrado.");
    }

    return pedido;
  }

  private byte[] construirPdf(Pedido pedido, List<DetallePedido> detalles) {
    ByteArrayOutputStream salida = new ByteArrayOutputStream();
    Document documento = new Document(PageSize.A4, 42, 42, 40, 40);

    try {
      PdfWriter.getInstance(documento, salida);

      documento.addTitle("Comprobante de pedido PowerFit");
      documento.addAuthor("PowerFit");
      documento.open();

      Cliente cliente = pedido.getCliente();
      LocalDateTime fechaPago =
          pedido.getFechaPago() != null ? pedido.getFechaPago() : pedido.getFechaPedido();
      String fechaPagoTexto = fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
      String numeroComprobante =
          String.format("PF-TIENDA-%d-%06d", fechaPago.getYear(), pedido.getIdPedido());
      String nombreCliente = cliente.getNombres() + " " + cliente.getApellidos();

      BigDecimal subtotal = escala(pedido.getSubtotal());
      BigDecimal costoEnvio = escala(pedido.getCostoEnvio());
      BigDecimal total = escala(pedido.getTotal());

      // FUENTES
      Font marca = new Font(Font.HELVETICA, 23, Font.BOLD, Color.WHITE);
      Font subtituloMarca = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.WHITE);
      Font titulo = new Font(Font.HELVETICA, 18, Font.BOLD, OSCURO);
      Font seccion = new Font(Font.HELVETICA, 11, Font.BOLD, OSCURO);
      Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL, OSCURO);
      Font negrita = new Font(Font.HELVETICA, 10, Font.BOLD, OSCURO);
      Font etiqueta = new Font(Font.HELVETICA, 8, Font.BOLD, GRIS);
      Font blanca = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
      Font totalFont = new Font(Font.HELVETICA, 16, Font.BOLD, AZUL);
      Font pie = new Font(Font.HELVETICA, 8, Font.NORMAL, GRIS);

      // ==================================
      // 1. ENCABEZADO POWERFIT
      // ==================================
      PdfPTable encabezado = new PdfPTable(2);
      encabezado.setWidths(new float[] {68, 32});
      encabezado.setWidthPercentage(100);

      PdfPCell marcaCelda = new PdfPCell();
      marcaCelda.setBackgroundColor(OSCURO);
      marcaCelda.setBorder(Rectangle.NO_BORDER);
      marcaCelda.setPadding(18);
      marcaCelda.addElement(new Paragraph("POWERFIT", marca));
      marcaCelda.addElement(new Paragraph("TIENDA DEPORTIVA", subtituloMarca));
      encabezado.addCell(marcaCelda);

      PdfPCell estadoCelda =
          new PdfPCell(new Phrase(etiquetaEstadoPago(pedido.getEstadoPago()), blanca));
      estadoCelda.setBackgroundColor(colorEstadoPago(pedido.getEstadoPago()));
      estadoCelda.setHorizontalAlignment(Element.ALIGN_CENTER);
      estadoCelda.setVerticalAlignment(Element.ALIGN_MIDDLE);
      estadoCelda.setBorder(Rectangle.NO_BORDER);
      estadoCelda.setPadding(12);
      encabezado.addCell(estadoCelda);

      documento.add(encabezado);
      documento.add(new Paragraph(" "));

      // ==================================
      // 2. TÍTULO Y NÚMERO
      // ==================================
      String tipoDoc =
          "FACTURA".equalsIgnoreCase(pedido.getTipoComprobante())
              ? "COMPROBANTE DE COMPRA - FACTURA"
              : "COMPROBANTE DE COMPRA - BOLETA";

      Paragraph tituloPdf = new Paragraph(tipoDoc, titulo);
      tituloPdf.setSpacingBefore(8);
      documento.add(tituloPdf);

      Paragraph numero =
          new Paragraph(
              "N.° " + numeroComprobante + "  |  Pedido #PF-" + pedido.getIdPedido(),
              new Font(Font.HELVETICA, 10, Font.BOLD, AZUL));
      numero.setSpacingAfter(14);
      documento.add(numero);

      // ==================================
      // 3. DATOS DEL CLIENTE Y ENTREGA
      // ==================================
      documento.add(new Paragraph("DATOS DEL CLIENTE Y ENTREGA", seccion));

      PdfPTable datos = new PdfPTable(2);
      datos.setWidths(new float[] {50, 50});
      datos.setWidthPercentage(100);
      datos.setSpacingBefore(8);
      datos.setSpacingAfter(16);

      datos.addCell(crearCeldaDato("CLIENTE", nombreCliente, etiqueta, negrita));
      datos.addCell(crearCeldaDato("DNI", cliente.getDni(), etiqueta, negrita));
      datos.addCell(crearCeldaDato("FECHA DE PAGO", fechaPagoTexto, etiqueta, negrita));
      datos.addCell(
          crearCeldaDato(
              "MÉTODO DE ENTREGA",
              "RECOJO".equalsIgnoreCase(pedido.getMetodoEntrega()) ? "Recojo en sede" : "Delivery",
              etiqueta,
              negrita));

      if ("FACTURA".equalsIgnoreCase(pedido.getTipoComprobante())) {
        datos.addCell(crearCeldaDato("RUC", pedido.getRuc(), etiqueta, negrita));
        datos.addCell(crearCeldaDato("RAZÓN SOCIAL", pedido.getRazonSocial(), etiqueta, negrita));
      }

      String detalleEntrega;
      if ("RECOJO".equalsIgnoreCase(pedido.getMetodoEntrega())) {
        detalleEntrega =
            pedido.getSede() != null
                ? pedido.getSede().getNombre()
                    + " ("
                    + pedido.getSede().getDireccion()
                    + " - "
                    + pedido.getSede().getDistrito()
                    + ")"
                : "Sede no especificada";
      } else {
        detalleEntrega =
            (pedido.getDireccionEntrega() != null ? pedido.getDireccionEntrega() : "")
                + (pedido.getDistrito() != null ? ", " + pedido.getDistrito() : "")
                + (pedido.getReferencia() != null && !pedido.getReferencia().isBlank()
                    ? " (Ref.: " + pedido.getReferencia() + ")"
                    : "");
      }

      PdfPCell celdaEntrega = crearCeldaDato("DESTINO / SEDE", detalleEntrega, etiqueta, negrita);
      celdaEntrega.setColspan(2);
      datos.addCell(celdaEntrega);

      documento.add(datos);

      // ==================================
      // 4. DETALLE DE PRODUCTOS
      // ==================================
      documento.add(new Paragraph("DETALLE DE PRODUCTOS", seccion));

      PdfPTable tablaProductos = new PdfPTable(4);
      tablaProductos.setWidths(new float[] {50, 12, 19, 19});
      tablaProductos.setWidthPercentage(100);
      tablaProductos.setSpacingBefore(8);
      tablaProductos.setSpacingAfter(12);

      tablaProductos.addCell(crearCelda("PRODUCTO", blanca, Element.ALIGN_LEFT, OSCURO));
      tablaProductos.addCell(crearCelda("CANT.", blanca, Element.ALIGN_CENTER, OSCURO));
      tablaProductos.addCell(crearCelda("P. UNIT.", blanca, Element.ALIGN_RIGHT, OSCURO));
      tablaProductos.addCell(crearCelda("SUBTOTAL", blanca, Element.ALIGN_RIGHT, OSCURO));

      for (DetallePedido d : detalles) {
        tablaProductos.addCell(
            crearCelda(d.getProducto().getNombre(), normal, Element.ALIGN_LEFT, FONDO));
        tablaProductos.addCell(
            crearCelda(String.valueOf(d.getCantidad()), normal, Element.ALIGN_CENTER, FONDO));
        tablaProductos.addCell(
            crearCelda(
                "S/ " + escala(d.getPrecioUnitario()).toPlainString(),
                normal,
                Element.ALIGN_RIGHT,
                FONDO));
        tablaProductos.addCell(
            crearCelda(
                "S/ " + escala(d.getSubtotal()).toPlainString(),
                negrita,
                Element.ALIGN_RIGHT,
                FONDO));
      }

      documento.add(tablaProductos);

      // ==================================
      // 5. RESUMEN DE TOTALES
      // ==================================
      PdfPTable totales = new PdfPTable(2);
      totales.setWidths(new float[] {65, 35});
      totales.setWidthPercentage(100);
      totales.setSpacingAfter(16);

      totales.addCell(crearCelda("SUBTOTAL", normal, Element.ALIGN_RIGHT, FONDO));
      totales.addCell(
          crearCelda("S/ " + subtotal.toPlainString(), negrita, Element.ALIGN_RIGHT, FONDO));

      totales.addCell(crearCelda("COSTO DE ENVÍO", normal, Element.ALIGN_RIGHT, FONDO));
      totales.addCell(
          crearCelda("S/ " + costoEnvio.toPlainString(), negrita, Element.ALIGN_RIGHT, FONDO));

      PdfPCell totalEtiqueta = new PdfPCell(new Phrase("TOTAL PAGADO", negrita));
      totalEtiqueta.setBackgroundColor(FONDO);
      totalEtiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
      totalEtiqueta.setVerticalAlignment(Element.ALIGN_MIDDLE);
      totalEtiqueta.setBorder(Rectangle.TOP);
      totalEtiqueta.setBorderColor(BORDE);
      totalEtiqueta.setPadding(12);
      totales.addCell(totalEtiqueta);

      PdfPCell totalMonto = new PdfPCell(new Phrase("S/ " + total.toPlainString(), totalFont));
      totalMonto.setBackgroundColor(FONDO);
      totalMonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
      totalMonto.setVerticalAlignment(Element.ALIGN_MIDDLE);
      totalMonto.setBorder(Rectangle.TOP);
      totalMonto.setBorderColor(BORDE);
      totalMonto.setPadding(12);
      totales.addCell(totalMonto);

      documento.add(totales);

      // ==================================
      // 6. MÉTODO DE PAGO Y ESTADO
      // ==================================
      PdfPTable metodo = new PdfPTable(2);
      metodo.setWidthPercentage(100);
      metodo.setWidths(new float[] {50, 50});

      metodo.addCell(
          crearCeldaDato(
              "MÉTODO DE PAGO", formatearMetodo(pedido.getMetodoPago()), etiqueta, negrita));
      metodo.addCell(
          crearCeldaDato(
              "ESTADO DE TRANSACCIÓN",
              etiquetaEstadoPago(pedido.getEstadoPago()),
              etiqueta,
              negrita));

      documento.add(metodo);

      // ==================================
      // 7. PIE DEL COMPROBANTE
      // ==================================
      Paragraph agradecimiento =
          new Paragraph(
              "¡Gracias por tu compra en PowerFit!",
              new Font(Font.HELVETICA, 12, Font.BOLD, AZUL));
      agradecimiento.setAlignment(Element.ALIGN_CENTER);
      agradecimiento.setSpacingBefore(18);
      documento.add(agradecimiento);

      Paragraph nota =
          new Paragraph(
              "Este documento acredita una operación simulada dentro de PowerFit. "
                  + "No constituye un comprobante tributario.",
              pie);
      nota.setAlignment(Element.ALIGN_CENTER);
      nota.setSpacingBefore(8);
      documento.add(nota);

      Paragraph sistema = new Paragraph("Documento generado automáticamente por PowerFit.", pie);
      sistema.setAlignment(Element.ALIGN_CENTER);
      sistema.setSpacingBefore(6);
      documento.add(sistema);

      documento.close();
      return salida.toByteArray();

    } catch (DocumentException e) {
      if (documento.isOpen()) {
        documento.close();
      }
      throw new IllegalStateException("No se pudo generar el comprobante PDF.", e);
    }
  }

  private BigDecimal escala(BigDecimal valor) {
    return valor != null
        ? valor.setScale(2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
  }

  private PdfPCell crearCeldaDato(
      String titulo, String valor, Font fuenteTitulo, Font fuenteValor) {
    PdfPCell celda = new PdfPCell();
    celda.setBackgroundColor(FONDO);
    celda.setBorder(Rectangle.BOX);
    celda.setBorderColor(BORDE);
    celda.setPadding(10);
    celda.addElement(new Paragraph(titulo, fuenteTitulo));
    celda.addElement(new Paragraph(valor != null && !valor.isBlank() ? valor : "-", fuenteValor));
    return celda;
  }

  private PdfPCell crearCelda(String texto, Font fuente, int alineacion, Color fondo) {
    PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
    celda.setBackgroundColor(fondo);
    celda.setBorder(Rectangle.BOTTOM);
    celda.setBorderColor(BORDE);
    celda.setPadding(10);
    celda.setHorizontalAlignment(alineacion);
    return celda;
  }

  private String formatearMetodo(String metodo) {
    if (metodo == null) {
      return "No especificado";
    }
    return switch (metodo.toUpperCase(Locale.ROOT)) {
      case "TARJETA" -> "Tarjeta de crédito o débito";
      case "YAPE_PLIN" -> "Yape / Plin";
      case "TRANSFERENCIA" -> "Transferencia bancaria";
      default -> metodo;
    };
  }

  private String etiquetaEstadoPago(String estadoPago) {
    if (estadoPago == null) {
      return "PAGADO";
    }
    return switch (estadoPago.toUpperCase(Locale.ROOT)) {
      case "REEMBOLSO_PENDIENTE" -> "REEMBOLSO PENDIENTE";
      case "REEMBOLSADO" -> "REEMBOLSADO";
      default -> "PAGADO";
    };
  }

  private Color colorEstadoPago(String estadoPago) {
    if ("REEMBOLSO_PENDIENTE".equalsIgnoreCase(estadoPago)) {
      return AMBAR;
    }
    if ("REEMBOLSADO".equalsIgnoreCase(estadoPago)) {
      return ROJO;
    }
    return VERDE;
  }
}