package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.HorarioClase;
import com.powerfit.powerfit.model.PagoClase;
import com.powerfit.powerfit.repository.PagoClaseRepository;
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
public class ComprobanteClasePdfService {

  private final PagoClaseRepository pagoRepository;

  private static final Color AZUL = new Color(75, 110, 255);
  private static final Color OSCURO = new Color(20, 23, 34);
  private static final Color GRIS = new Color(105, 112, 130);
  private static final Color FONDO = new Color(245, 247, 252);
  private static final Color BORDE = new Color(220, 225, 235);
  private static final Color VERDE = new Color(20, 145, 105);

  public ComprobanteClasePdfService(PagoClaseRepository pagoRepository) {
    this.pagoRepository = pagoRepository;
  }

  @Transactional(readOnly = true)
  public byte[] generarComprobante(Long idPago, Long idCliente) {

    PagoClase pago =
        pagoRepository
            .findById(idPago)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el pago."));

    if (pago.getReserva() == null
        || pago.getReserva().getCliente() == null
        || !Objects.equals(pago.getReserva().getCliente().getIdCliente(), idCliente)) {

      throw new SecurityException("No tienes permiso para consultar este comprobante.");
    }

    String estadoPago =
        pago.getEstadoPago() == null ? "" : pago.getEstadoPago().toUpperCase(Locale.ROOT);

    if (!List.of("PAGADO", "REEMBOLSO_PENDIENTE", "REEMBOLSADO").contains(estadoPago)
        || !"PAGO".equalsIgnoreCase(pago.getTipoReserva())) {

      throw new IllegalStateException("Solo se pueden generar comprobantes de pagos realizados.");
    }

    return construirPdf(pago);
  }

  private byte[] construirPdf(PagoClase pago) {

    ByteArrayOutputStream salida = new ByteArrayOutputStream();

    Document documento = new Document(PageSize.A4, 42, 42, 40, 40);

    try {

      PdfWriter.getInstance(documento, salida);

      documento.addTitle("Comprobante de clase PowerFit");
      documento.addAuthor("PowerFit");

      documento.open();

      Cliente cliente = pago.getReserva().getCliente();
      HorarioClase horario = pago.getReserva().getHorario();

      LocalDateTime fechaPago =
          pago.getFechaPago() != null ? pago.getFechaPago() : pago.getFechaRegistro();

      if (fechaPago == null) {
        fechaPago = LocalDateTime.now();
      }

      String fechaTexto = fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

      String fechaClase = horario.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

      String horaClase = horario.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm"));

      String numero = String.format("PF-CL-%d-%06d", fechaPago.getYear(), pago.getIdPagoClase());

      BigDecimal monto =
          pago.getMonto() != null
              ? pago.getMonto().setScale(2, RoundingMode.HALF_UP)
              : BigDecimal.ZERO;

      String estadoPago =
          pago.getEstadoPago() == null ? "" : pago.getEstadoPago().toUpperCase(Locale.ROOT);

      String estadoComprobante =
          switch (estadoPago) {
            case "REEMBOLSO_PENDIENTE" -> "REEMBOLSO PENDIENTE";
            case "REEMBOLSADO" -> "REEMBOLSADO";
            default -> "PAGADO";
          };

      Font marca = new Font(Font.HELVETICA, 23, Font.BOLD, Color.WHITE);

      Font subtitulo = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.WHITE);

      Font titulo = new Font(Font.HELVETICA, 19, Font.BOLD, OSCURO);

      Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL, OSCURO);

      Font negrita = new Font(Font.HELVETICA, 10, Font.BOLD, OSCURO);

      Font etiqueta = new Font(Font.HELVETICA, 8, Font.BOLD, GRIS);

      Font blanca = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);

      Font totalFont = new Font(Font.HELVETICA, 17, Font.BOLD, AZUL);

      Font pie = new Font(Font.HELVETICA, 8, Font.NORMAL, GRIS);

      // ENCABEZADO

      PdfPTable encabezado = new PdfPTable(2);
      encabezado.setWidths(new float[] {70, 30});
      encabezado.setWidthPercentage(100);

      PdfPCell marcaCelda = new PdfPCell();
      marcaCelda.setBackgroundColor(OSCURO);
      marcaCelda.setBorder(Rectangle.NO_BORDER);
      marcaCelda.setPadding(18);

      marcaCelda.addElement(new Paragraph("POWERFIT", marca));
      marcaCelda.addElement(new Paragraph("FITNESS CENTER", subtitulo));

      encabezado.addCell(marcaCelda);

      PdfPCell estado = new PdfPCell(new Phrase(estadoComprobante, blanca));

      estado.setBackgroundColor(
          "REEMBOLSO_PENDIENTE".equals(estadoPago) ? new Color(190, 120, 25) : VERDE);

      estado.setHorizontalAlignment(Element.ALIGN_CENTER);
      estado.setVerticalAlignment(Element.ALIGN_MIDDLE);
      estado.setBorder(Rectangle.NO_BORDER);

      encabezado.addCell(estado);

      documento.add(encabezado);
      documento.add(new Paragraph(" "));

      // TITULO

      Paragraph tituloPdf = new Paragraph("COMPROBANTE DE PAGO", titulo);

      tituloPdf.setSpacingBefore(12);
      documento.add(tituloPdf);

      Paragraph codigo =
          new Paragraph("N.° " + numero, new Font(Font.HELVETICA, 10, Font.BOLD, AZUL));

      codigo.setSpacingAfter(18);
      documento.add(codigo);

      // DATOS DEL CLIENTE

      documento.add(
          new Paragraph("DATOS DEL CLIENTE", new Font(Font.HELVETICA, 11, Font.BOLD, OSCURO)));

      PdfPTable datos = new PdfPTable(2);
      datos.setWidths(new float[] {50, 50});
      datos.setWidthPercentage(100);
      datos.setSpacingBefore(10);
      datos.setSpacingAfter(20);

      datos.addCell(
          crearCeldaDato(
              "CLIENTE", cliente.getNombres() + " " + cliente.getApellidos(), etiqueta, negrita));

      datos.addCell(crearCeldaDato("DNI", cliente.getDni(), etiqueta, negrita));

      datos.addCell(crearCeldaDato("FECHA DE PAGO", fechaTexto, etiqueta, negrita));

      datos.addCell(
          crearCeldaDato(
              "NÚMERO DE OPERACIÓN", String.valueOf(pago.getIdPagoClase()), etiqueta, negrita));

      documento.add(datos);

      // DETALLE DE LA CLASE

      documento.add(
          new Paragraph("DETALLE DEL SERVICIO", new Font(Font.HELVETICA, 11, Font.BOLD, OSCURO)));

      PdfPTable detalle = new PdfPTable(3);
      detalle.setWidths(new float[] {45, 30, 25});
      detalle.setWidthPercentage(100);
      detalle.setSpacingBefore(10);
      detalle.setSpacingAfter(15);

      detalle.addCell(crearCelda("CLASE", blanca, Element.ALIGN_LEFT, OSCURO));

      detalle.addCell(crearCelda("SESIÓN", blanca, Element.ALIGN_CENTER, OSCURO));

      detalle.addCell(crearCelda("IMPORTE", blanca, Element.ALIGN_RIGHT, OSCURO));

      detalle.addCell(
          crearCelda(horario.getClase().getNombre(), normal, Element.ALIGN_LEFT, FONDO));

      detalle.addCell(
          crearCelda(fechaClase + " - " + horaClase, normal, Element.ALIGN_CENTER, FONDO));

      detalle.addCell(
          crearCelda("S/ " + monto.toPlainString(), negrita, Element.ALIGN_RIGHT, FONDO));

      documento.add(detalle);

      // TOTAL

      PdfPTable total = new PdfPTable(2);
      total.setWidths(new float[] {65, 35});
      total.setWidthPercentage(100);
      total.setSpacingAfter(20);

      String etiquetaTotal =
          switch (estadoPago) {
            case "REEMBOLSADO" -> "IMPORTE ORIGINAL (REEMBOLSADO)";
            case "REEMBOLSO_PENDIENTE" -> "IMPORTE ORIGINAL";
            default -> "TOTAL PAGADO";
          };

      PdfPCell totalEtiqueta = new PdfPCell(new Phrase(etiquetaTotal, negrita));

      totalEtiqueta.setBackgroundColor(FONDO);
      totalEtiqueta.setBorder(Rectangle.NO_BORDER);
      totalEtiqueta.setPadding(15);

      total.addCell(totalEtiqueta);

      PdfPCell totalMonto = new PdfPCell(new Phrase("S/ " + monto.toPlainString(), totalFont));

      totalMonto.setBackgroundColor(FONDO);
      totalMonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
      totalMonto.setVerticalAlignment(Element.ALIGN_MIDDLE);
      totalMonto.setBorder(Rectangle.NO_BORDER);
      totalMonto.setPadding(15);

      total.addCell(totalMonto);

      documento.add(total);

      // METODO DE PAGO

      PdfPTable metodo = new PdfPTable(2);
      metodo.setWidthPercentage(100);
      metodo.setWidths(new float[] {50, 50});

      metodo.addCell(
          crearCeldaDato(
              "MÉTODO DE PAGO", formatearMetodo(pago.getMetodoPago()), etiqueta, negrita));

      metodo.addCell(crearCeldaDato("ESTADO DE TRANSACCIÓN", estadoComprobante, etiqueta, negrita));

      documento.add(metodo);

      // PIE

      Paragraph agradecimiento =
          new Paragraph(
              "¡Gracias por elegir PowerFit!", new Font(Font.HELVETICA, 12, Font.BOLD, AZUL));

      agradecimiento.setAlignment(Element.ALIGN_CENTER);
      agradecimiento.setSpacingBefore(25);

      documento.add(agradecimiento);

      Paragraph nota =
          new Paragraph(
              "Este documento acredita una operación simulada "
                  + "dentro de PowerFit. No constituye un comprobante tributario.",
              pie);

      nota.setAlignment(Element.ALIGN_CENTER);
      nota.setSpacingBefore(10);

      documento.add(nota);

      documento.close();

      return salida.toByteArray();

    } catch (DocumentException e) {

      if (documento.isOpen()) {
        documento.close();
      }

      throw new IllegalStateException("No se pudo generar el comprobante PDF.", e);
    }
  }

  private PdfPCell crearCeldaDato(
      String titulo, String valor, Font fuenteTitulo, Font fuenteValor) {

    PdfPCell celda = new PdfPCell();

    celda.setBackgroundColor(FONDO);
    celda.setBorder(Rectangle.BOX);
    celda.setBorderColor(BORDE);
    celda.setPadding(12);

    celda.addElement(new Paragraph(titulo, fuenteTitulo));
    celda.addElement(new Paragraph(valor != null ? valor : "-", fuenteValor));

    return celda;
  }

  private PdfPCell crearCelda(String texto, Font fuente, int alineacion, Color fondo) {

    PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));

    celda.setBackgroundColor(fondo);
    celda.setBorder(Rectangle.NO_BORDER);
    celda.setPadding(12);
    celda.setHorizontalAlignment(alineacion);

    return celda;
  }

  private String formatearMetodo(String metodo) {

    if (metodo == null) {
      return "No especificado";
    }

    return switch (metodo.toUpperCase(Locale.ROOT)) {
      case "TARJETA" -> "Tarjeta de crédito o débito";
      case "YAPE" -> "Yape";
      case "PLIN" -> "Plin";
      case "EFECTIVO" -> "Efectivo";
      default -> metodo;
    };
  }
}
