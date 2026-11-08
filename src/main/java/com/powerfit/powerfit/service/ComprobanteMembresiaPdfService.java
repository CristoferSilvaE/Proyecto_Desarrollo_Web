package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Cliente;
import com.powerfit.powerfit.model.Membresia;
import com.powerfit.powerfit.model.PagoMembresia;
import com.powerfit.powerfit.repository.PagoMembresiaRepository;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class ComprobanteMembresiaPdfService {

  private final PagoMembresiaRepository pagoMembresiaRepository;

  private static final Color AZUL = new Color(75, 110, 255);
  private static final Color OSCURO = new Color(20, 23, 34);
  private static final Color GRIS = new Color(105, 112, 130);
  private static final Color FONDO = new Color(245, 247, 252);
  private static final Color BORDE = new Color(220, 225, 235);
  private static final Color VERDE = new Color(20, 145, 105);

  public ComprobanteMembresiaPdfService(PagoMembresiaRepository pagoMembresiaRepository) {
    this.pagoMembresiaRepository = pagoMembresiaRepository;
  }

  @Transactional(readOnly = true)
  public byte[] generarComprobante(Long idPago, Long idCliente) {

    PagoMembresia pago =
        pagoMembresiaRepository
            .findById(idPago)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el pago."));

    Membresia membresia = pago.getMembresia();

    if (membresia == null
        || membresia.getCliente() == null
        || !Objects.equals(membresia.getCliente().getIdCliente(), idCliente)) {
      throw new SecurityException("No tienes permiso para consultar este comprobante.");
    }

    if (!"PAGADO".equalsIgnoreCase(pago.getEstado())) {
      throw new IllegalStateException("Solo se pueden generar comprobantes de pagos realizados.");
    }

    return construirPdf(pago, membresia);
  }

  private byte[] construirPdf(PagoMembresia pago, Membresia membresia) {

    ByteArrayOutputStream salida = new ByteArrayOutputStream();

    Document documento = new Document(PageSize.A4, 42, 42, 40, 40);

    try {

      PdfWriter.getInstance(documento, salida);

      documento.addTitle("Comprobante de pago PowerFit");
      documento.addAuthor("PowerFit");

      documento.open();

      Cliente cliente = membresia.getCliente();

      LocalDateTime fecha = pago.getFechaPago() != null ? pago.getFechaPago() : LocalDateTime.now();

      String fechaTexto = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

      String numeroComprobante = String.format("PF-%d-%06d", fecha.getYear(), pago.getIdPago());

      String nombreCliente = cliente.getNombres() + " " + cliente.getApellidos();

      String nombrePlan = membresia.getPlan().getNombre();

      String duracion = membresia.getDuracionMeses() + " mes(es)";

      BigDecimal monto =
          pago.getMonto() != null
              ? pago.getMonto().setScale(2, RoundingMode.HALF_UP)
              : BigDecimal.ZERO;

      // FUENTES
      Font marca = new Font(Font.HELVETICA, 23, Font.BOLD, Color.WHITE);

      Font subtituloMarca = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.WHITE);

      Font titulo = new Font(Font.HELVETICA, 19, Font.BOLD, OSCURO);

      Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL, OSCURO);

      Font negrita = new Font(Font.HELVETICA, 10, Font.BOLD, OSCURO);

      Font etiqueta = new Font(Font.HELVETICA, 8, Font.BOLD, GRIS);

      Font blanca = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);

      Font totalFont = new Font(Font.HELVETICA, 17, Font.BOLD, AZUL);

      Font pie = new Font(Font.HELVETICA, 8, Font.NORMAL, GRIS);

      // ==================================
      // 1. ENCABEZADO POWERFIT
      // ==================================

      PdfPTable encabezado = new PdfPTable(2);
      encabezado.setWidths(new float[] {70, 30});
      encabezado.setWidthPercentage(100);

      PdfPCell marcaCelda = new PdfPCell();
      marcaCelda.setBackgroundColor(OSCURO);
      marcaCelda.setBorder(Rectangle.NO_BORDER);
      marcaCelda.setPadding(18);

      marcaCelda.addElement(new Paragraph("POWERFIT", marca));
      marcaCelda.addElement(new Paragraph("FITNESS CENTER", subtituloMarca));

      encabezado.addCell(marcaCelda);

      PdfPCell estadoCelda = new PdfPCell(new Phrase("PAGADO", blanca));

      estadoCelda.setBackgroundColor(VERDE);
      estadoCelda.setHorizontalAlignment(Element.ALIGN_CENTER);
      estadoCelda.setVerticalAlignment(Element.ALIGN_MIDDLE);
      estadoCelda.setBorder(Rectangle.NO_BORDER);

      encabezado.addCell(estadoCelda);

      documento.add(encabezado);

      documento.add(new Paragraph(" "));

      // ==================================
      // 2. TITULO Y NUMERO
      // ==================================

      Paragraph tituloPdf = new Paragraph("COMPROBANTE DE PAGO", titulo);

      tituloPdf.setSpacingBefore(12);
      documento.add(tituloPdf);

      Paragraph numero =
          new Paragraph("N.° " + numeroComprobante, new Font(Font.HELVETICA, 10, Font.BOLD, AZUL));

      numero.setSpacingAfter(18);
      documento.add(numero);

      // ==================================
      // 3. DATOS DEL CLIENTE
      // ==================================

      documento.add(
          new Paragraph("DATOS DEL CLIENTE", new Font(Font.HELVETICA, 11, Font.BOLD, OSCURO)));

      PdfPTable datos = new PdfPTable(2);
      datos.setWidths(new float[] {50, 50});
      datos.setWidthPercentage(100);
      datos.setSpacingBefore(10);
      datos.setSpacingAfter(20);

      datos.addCell(crearCeldaDato("CLIENTE", nombreCliente, etiqueta, negrita));

      datos.addCell(crearCeldaDato("DNI", cliente.getDni(), etiqueta, negrita));

      datos.addCell(crearCeldaDato("FECHA DE PAGO", fechaTexto, etiqueta, negrita));

      datos.addCell(
          crearCeldaDato(
              "NÚMERO DE OPERACIÓN", String.valueOf(pago.getIdPago()), etiqueta, negrita));

      documento.add(datos);

      // ==================================
      // 4. DETALLE DEL SERVICIO
      // ==================================

      documento.add(
          new Paragraph("DETALLE DEL SERVICIO", new Font(Font.HELVETICA, 11, Font.BOLD, OSCURO)));

      PdfPTable detalle = new PdfPTable(3);
      detalle.setWidths(new float[] {60, 15, 25});
      detalle.setWidthPercentage(100);
      detalle.setSpacingBefore(10);
      detalle.setSpacingAfter(15);

      detalle.addCell(crearCelda("DESCRIPCIÓN", blanca, Element.ALIGN_LEFT, OSCURO));

      detalle.addCell(crearCelda("CANT.", blanca, Element.ALIGN_CENTER, OSCURO));

      detalle.addCell(crearCelda("IMPORTE", blanca, Element.ALIGN_RIGHT, OSCURO));

      detalle.addCell(
          crearCelda("Plan " + nombrePlan + " - " + duracion, normal, Element.ALIGN_LEFT, FONDO));

      detalle.addCell(crearCelda("1", normal, Element.ALIGN_CENTER, FONDO));

      detalle.addCell(
          crearCelda("S/ " + monto.toPlainString(), negrita, Element.ALIGN_RIGHT, FONDO));

      documento.add(detalle);

      // ==================================
      // 5. TOTAL
      // ==================================

      PdfPTable total = new PdfPTable(2);
      total.setWidths(new float[] {65, 35});
      total.setWidthPercentage(100);
      total.setSpacingAfter(20);

      PdfPCell totalEtiqueta = new PdfPCell(new Phrase("TOTAL PAGADO", negrita));

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

      // ==================================
      // 6. METODO DE PAGO
      // ==================================

      PdfPTable metodo = new PdfPTable(2);
      metodo.setWidthPercentage(100);
      metodo.setWidths(new float[] {50, 50});

      metodo.addCell(
          crearCeldaDato(
              "MÉTODO DE PAGO", formatearMetodo(pago.getMetodoPago()), etiqueta, negrita));

      metodo.addCell(crearCeldaDato("ESTADO DE TRANSACCIÓN", "PAGADO", etiqueta, negrita));

      documento.add(metodo);

      // ==================================
      // 7. PIE DEL COMPROBANTE
      // ==================================

      documento.add(new Paragraph(" "));

      Paragraph agradecimiento =
          new Paragraph(
              "¡Gracias por elegir PowerFit!", new Font(Font.HELVETICA, 12, Font.BOLD, AZUL));

      agradecimiento.setAlignment(Element.ALIGN_CENTER);
      agradecimiento.setSpacingBefore(20);

      documento.add(agradecimiento);

      Paragraph nota =
          new Paragraph(
              "Este documento acredita una operación simulada "
                  + "dentro de PowerFit. "
                  + "No constituye un comprobante tributario.",
              pie);

      nota.setAlignment(Element.ALIGN_CENTER);
      nota.setSpacingBefore(10);

      documento.add(nota);

      Paragraph sistema = new Paragraph("Documento generado automáticamente por PowerFit.", pie);

      sistema.setAlignment(Element.ALIGN_CENTER);
      sistema.setSpacingBefore(8);

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
