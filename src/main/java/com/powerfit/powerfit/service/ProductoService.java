package com.powerfit.powerfit.service;

import com.powerfit.powerfit.model.Producto;
import com.powerfit.powerfit.repository.ProductoRepository;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductoService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_SOURCE_PIXELS = 40_000_000L;
    private static final int MAX_DIMENSION = 1920;
    private static final float JPEG_QUALITY = 0.88f;

    private static final Set<String> CATEGORIAS_VALIDAS = Set.of("Nutrición Deportiva", "Equipamiento", "Accesorios");

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Producto guardar(
            Long idProducto,
            String sku,
            String nombre,
            String categoria,
            BigDecimal precio,
            Integer stock,
            Integer stockMinimo,
            String estado,
            String descripcion,
            MultipartFile imagen)
            throws IOException {

        sku = sku == null ? "" : sku.trim().toUpperCase(Locale.ROOT);
        nombre = nombre == null ? "" : nombre.trim();
        categoria = categoria == null ? "" : categoria.trim();
        descripcion = descripcion == null ? "" : descripcion.trim();
        estado = estado == null ? "ACTIVO" : estado.trim().toUpperCase(Locale.ROOT);

        if (sku.isBlank() || nombre.isBlank() || categoria.isBlank()) {
            throw new IllegalArgumentException("El SKU, el nombre y la categoría son obligatorios.");
        }

        if (sku.length() > 30 || nombre.length() > 120 || categoria.length() > 50) {
            throw new IllegalArgumentException("Uno de los campos supera la longitud permitida.");
        }

        if (descripcion.length() > 255) {
            throw new IllegalArgumentException("La descripción no puede superar los 255 caracteres.");
        }

        if (!CATEGORIAS_VALIDAS.contains(categoria)) {
            throw new IllegalArgumentException("Selecciona una categoría válida.");
        }

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a S/ 0.00.");
        }

        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }

        if (stockMinimo == null || stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }

        if (!"ACTIVO".equals(estado) && !"INACTIVO".equals(estado)) {
            throw new IllegalArgumentException("Estado no válido.");
        }

        boolean skuDuplicado = idProducto == null
                ? productoRepository.existsBySkuIgnoreCase(sku)
                : productoRepository.existsBySkuIgnoreCaseAndIdProductoNot(sku, idProducto);

        if (skuDuplicado) {
            throw new IllegalArgumentException("Ya existe un producto registrado con el SKU " + sku + ".");
        }

        Producto producto = idProducto == null
                ? new Producto()
                : productoRepository
                        .findById(idProducto)
                        .orElseThrow(() -> new IllegalArgumentException("No se encontró el producto."));

        String imagenAnterior = producto.getImagen();

        producto.setSku(sku);
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(precio.setScale(2, RoundingMode.HALF_UP));
        producto.setStock(stock);
        producto.setStockMinimo(stockMinimo);
        producto.setEstado(estado);
        producto.setDescripcion(descripcion.isBlank() ? null : descripcion);

        if (imagen != null && !imagen.isEmpty()) {
            String nuevoArchivo = guardarImagen(nombre, imagen);
            producto.setImagen(nuevoArchivo);

            // Si había una imagen anterior subida en uploads/productos, eliminarla para no
            // dejar basura
            eliminarImagenSubidaSiExiste(imagenAnterior);
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public void cambiarEstado(Long idProducto, String nuevoEstado) {
        String estado = nuevoEstado == null ? "" : nuevoEstado.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVO".equals(estado) && !"INACTIVO".equals(estado)) {
            throw new IllegalArgumentException("Estado no válido.");
        }

        Producto producto = productoRepository
                .findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el producto."));

        producto.setEstado(estado);
        productoRepository.save(producto);
    }

    private String guardarImagen(String nombreProducto, MultipartFile archivo) throws IOException {
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("La imagen no debe superar los 5 MB.");
        }

        BufferedImage original;

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
                    throw new IllegalArgumentException("Solo se permiten imágenes JPG o PNG.");
                }

                int anchoOriginal = lector.getWidth(0);
                int altoOriginal = lector.getHeight(0);

                if ((long) anchoOriginal * altoOriginal > MAX_SOURCE_PIXELS) {
                    throw new IllegalArgumentException(
                            "La imagen supera la resolución máxima permitida para procesarla.");
                }

                original = lector.read(0);
            } finally {
                lector.dispose();
            }
        }

        if (original == null) {
            throw new IllegalArgumentException("El archivo no es una imagen válida.");
        }

        int anchoOriginal = original.getWidth();
        int altoOriginal = original.getHeight();

        double escala = Math.min(1.0, (double) MAX_DIMENSION / Math.max(anchoOriginal, altoOriginal));
        int nuevoAncho = Math.max(1, (int) Math.round(anchoOriginal * escala));
        int nuevoAlto = Math.max(1, (int) Math.round(altoOriginal * escala));

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

        String slug = Normalizer.normalize(nombreProducto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            slug = "producto";
        }
        if (slug.length() > 40) {
            slug = slug.substring(0, 40);
        }

        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        String nombreArchivo = "producto-" + slug + "-" + sufijo + ".jpg";

        Path carpeta = Paths.get("uploads", "productos").toAbsolutePath().normalize();
        Files.createDirectories(carpeta);

        Path destino = carpeta.resolve(nombreArchivo).normalize();

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

    private void eliminarImagenSubidaSiExiste(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return;
        }
        try {
            Path carpeta = Paths.get("uploads", "productos").toAbsolutePath().normalize();
            Path archivo = carpeta.resolve(nombreArchivo).normalize();
            if (archivo.startsWith(carpeta)) {
                Files.deleteIfExists(archivo);
            }
        } catch (IOException ignored) {
            // No interrumpir la actualización si no se pudo borrar el archivo previo
        }
    }
}