package com.creacionesedimile.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio para almacenar imágenes subidas desde el equipo del usuario.
 *
 * Las imágenes se guardan en el directorio configurado en {@code app.uploads.dir}
 * y se sirven estáticamente bajo la ruta {@code /uploads/**}.
 *
 * Restricciones de seguridad:
 *  - Solo se aceptan extensiones de imagen conocidas (jpg, jpeg, png, gif, webp).
 *  - El nombre del archivo se genera aleatoriamente (UUID) para evitar colisiones
 *    y ataques de path traversal.
 *  - Se limita el tamaño en application.properties (spring.servlet.multipart.max-file-size).
 */
@Service
public class FileStorageService {

    private static final Set<String> EXTENSIONES_PERMITIDAS =
            Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path directorioUploads;

    public FileStorageService(@Value("${app.uploads.dir:./uploads}") String uploadsDir) {
        this.directorioUploads = Paths.get(uploadsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directorioUploads);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads: " + uploadsDir, e);
        }
    }

    /**
     * Guarda el archivo subido y devuelve la URL relativa para almacenar en Firestore.
     *
     * @param archivo archivo recibido del formulario
     * @return URL relativa, p. ej. {@code /uploads/a1b2c3d4.jpg}
     * @throws IllegalArgumentException si el tipo de archivo no está permitido
     * @throws RuntimeException         si ocurre un error de I/O
     */
    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío.");
        }

        String nombreOriginal = archivo.getOriginalFilename() != null
                ? archivo.getOriginalFilename() : "";
        String extension = obtenerExtension(nombreOriginal);

        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. Use: jpg, jpeg, png, gif o webp.");
        }

        String nombreFinal = UUID.randomUUID() + "." + extension;
        Path destino = this.directorioUploads.resolve(nombreFinal);

        try {
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen en el servidor.", e);
        }

        return "/uploads/" + nombreFinal;
    }

    /**
     * Elimina un archivo local si su URL corresponde a un upload propio
     * (empieza con {@code /uploads/}).  Si es una URL externa, no hace nada.
     */
    public void eliminarSiLocal(String imagenUrl) {
        if (imagenUrl == null || !imagenUrl.startsWith("/uploads/")) return;
        String nombreArchivo = imagenUrl.substring("/uploads/".length());
        // Validar que no haya path traversal en el nombre
        if (nombreArchivo.contains("..") || nombreArchivo.contains("/")) return;
        Path ruta = directorioUploads.resolve(nombreArchivo);
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException ignored) {
            // No crítico: el archivo ya no existe o no se puede borrar
        }
    }

    // -------------------------------------------------------

    private String obtenerExtension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto < 0) return "";
        return nombre.substring(punto + 1).toLowerCase();
    }
}
