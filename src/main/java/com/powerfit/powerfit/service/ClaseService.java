package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Clase;
import com.powerfit.powerfit.repository.ClaseRepository;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClaseService {

  private final ClaseRepository repository;

  private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
  private static final long MAX_SOURCE_PIXELS = 40_000_000L;
  private static final int MAX_DIMENSION = 1920;
  private static final float JPEG_QUALITY = 0.88f;

  public ClaseService(ClaseRepository repository) {
    this.repository = repository;
  }

  public List<Clase> listar() {
    return repository.findAllByOrderByNombreAsc();
  }

  public Clase guardar(
      Long id, String nombre, String categoria, String descripcion, MultipartFile imagen)
      throws IOException {

    if (nombre == null || nombre.isBlank() || categoria == null || categoria.isBlank()) {
      throw new IllegalArgumentException("El nombre y la categoría son obligatorios.");
    }

    nombre = nombre.trim();
    categoria = categoria.trim();

    if (nombre.length() > 80
        || categoria.length() > 50
        || (descripcion != null && descripcion.length() > 200)) {
      throw new IllegalArgumentException("Uno de los campos supera la longitud permitida.");
    }

    Clase clase =
        id == null
            ? new Clase()
            : repository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la clase."));

    boolean duplicado =
        id == null
            ? repository.existsByNombreIgnoreCase(nombre)
            : repository.existsByNombreIgnoreCaseAndIdClaseNot(nombre, id);

    if (duplicado) {
      throw new IllegalArgumentException("Ya existe una clase con ese nombre.");
    }

    clase.setNombre(nombre);
    clase.setCategoria(categoria);
    clase.setDescripcion(descripcion == null ? "" : descripcion.trim());

    if (id == null) {
      clase.setEstado("ACTIVO");
    }

    if (imagen != null && !imagen.isEmpty()) {
      clase.setImagen(guardarImagen(nombre, imagen));
    }

    return repository.save(clase);
  }

  public void cambiarEstado(Long id) {

    Clase clase =
        repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la clase."));

    String nuevoEstado = "ACTIVO".equals(clase.getEstado()) ? "INACTIVO" : "ACTIVO";

    clase.setEstado(nuevoEstado);
    repository.save(clase);
  }

  private String guardarImagen(String nombreClase, MultipartFile archivo) throws IOException {

    if (archivo.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("La imagen no debe superar los 5 MB.");
    }

    BufferedImage original;

    // Leer y validar la imagen antes de procesarla.
    try (InputStream stream = archivo.getInputStream();
        ImageInputStream input = ImageIO.createImageInputStream(stream)) {

      if (input == null) {
        throw new IllegalArgumentException("No se pudo leer el archivo de imagen.");
      }

      Iterator<ImageReader> lectores = ImageIO.getImageReaders(input);

      if (!lectores.hasNext()) {
        throw new IllegalArgumentException("Selecciona una imagen JPG o PNG válida.");
      }

      ImageReader lector = lectores.next();

      try {
        lector.setInput(input, false, true);

        String formato = lector.getFormatName().toLowerCase(Locale.ROOT);

        if (!formato.equals("jpg") && !formato.equals("jpeg") && !formato.equals("png")) {
          throw new IllegalArgumentException("Selecciona una imagen JPG o PNG.");
        }

        int anchoOriginal = lector.getWidth(0);
        int altoOriginal = lector.getHeight(0);

        if ((long) anchoOriginal * altoOriginal > MAX_SOURCE_PIXELS) {
          throw new IllegalArgumentException(
              "La imagen original supera la resolución " + "máxima permitida para procesarla.");
        }

        original = lector.read(0);

      } finally {
        lector.dispose();
      }
    }

    if (original == null) {
      throw new IllegalArgumentException("El archivo no es una imagen válida.");
    }

    // Calcular nuevas dimensiones sin deformar la imagen.
    int anchoOriginal = original.getWidth();
    int altoOriginal = original.getHeight();

    double escala = Math.min(1.0, (double) MAX_DIMENSION / Math.max(anchoOriginal, altoOriginal));

    int nuevoAncho = Math.max(1, (int) Math.round(anchoOriginal * escala));

    int nuevoAlto = Math.max(1, (int) Math.round(altoOriginal * escala));

    // Convertir a JPG y aplicar redimensionamiento.
    BufferedImage jpg = new BufferedImage(nuevoAncho, nuevoAlto, BufferedImage.TYPE_INT_RGB);

    Graphics2D graphics = jpg.createGraphics();

    try {
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, nuevoAncho, nuevoAlto);

      graphics.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

      graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      graphics.drawImage(original, 0, 0, nuevoAncho, nuevoAlto, null);

    } finally {
      graphics.dispose();
    }

    // Generar nombre uniforme sin tildes ni espacios.
    String nombreNormalizado =
        Normalizer.normalize(nombreClase.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");

    String nombreArchivo = "clase-" + nombreNormalizado + ".jpg";

    // Crear carpeta de almacenamiento.
    Path carpeta = Paths.get("uploads", "clases").toAbsolutePath().normalize();

    Files.createDirectories(carpeta);

    Path destino = carpeta.resolve(nombreArchivo);

    // Guardar JPG con calidad del 88 %.
    Iterator<ImageWriter> escritores = ImageIO.getImageWritersByFormatName("jpg");

    if (!escritores.hasNext()) {
      throw new IOException("No se encontró un codificador JPG.");
    }

    ImageWriter escritor = escritores.next();

    try (ImageOutputStream output = ImageIO.createImageOutputStream(destino.toFile())) {

      if (output == null) {
        throw new IOException("No se pudo crear el archivo de imagen.");
      }

      escritor.setOutput(output);

      ImageWriteParam parametros = escritor.getDefaultWriteParam();

      if (parametros.canWriteCompressed()) {
        parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

        parametros.setCompressionQuality(JPEG_QUALITY);
      }

      escritor.write(null, new IIOImage(jpg, null, null), parametros);

      output.flush();

    } finally {
      escritor.dispose();
    }

    return nombreArchivo;
  }
}
