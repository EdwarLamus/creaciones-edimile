package com.creacionesedimile.service;

import com.creacionesedimile.model.Gasto;
import com.creacionesedimile.repository.GastoRepository;
import com.google.cloud.Timestamp;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class GastoService {

    private final GastoRepository gastoRepository;

    public GastoService(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    // ── Consultas ───────────────────────────────────────────────────────────

    public List<Gasto> listarTodos() {
        return gastoRepository.findAll();
    }

    public List<Gasto> listarPorCategoria(Gasto.Categoria categoria) {
        return gastoRepository.findByCategoria(categoria);
    }

    public Optional<Gasto> buscarPorId(String id) {
        return gastoRepository.findById(id);
    }

    /** Filtrado en memoria por número, nombre, proveedor o usuario registrador. */
    public List<Gasto> buscar(String termino) {
        if (termino == null || termino.isBlank()) return listarTodos();
        String t = termino.toLowerCase();
        List<Gasto> resultado = new ArrayList<>();
        for (Gasto g : listarTodos()) {
            if ((g.getNumero()       != null && g.getNumero().toLowerCase().contains(t))
             || (g.getNombre()       != null && g.getNombre().toLowerCase().contains(t))
             || (g.getProveedor()    != null && g.getProveedor().toLowerCase().contains(t))
             || (g.getUsuarioNombre()!= null && g.getUsuarioNombre().toLowerCase().contains(t))) {
                resultado.add(g);
            }
        }
        return resultado;
    }

    // ── Crear ────────────────────────────────────────────────────────────────

    public Gasto crear(String nombre,
                       String descripcion,
                       String categoriaStr,
                       double total,
                       String fechaPagoStr,
                       String metodoPagoStr,
                       String proveedor,
                       String comprobante,
                       String usuarioId,
                       String usuarioNombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del gasto es obligatorio.");
        if (total <= 0)
            throw new IllegalArgumentException("El valor del gasto debe ser mayor a cero.");
        if (fechaPagoStr == null || fechaPagoStr.isBlank())
            throw new IllegalArgumentException("La fecha de pago es obligatoria.");

        Gasto g = new Gasto();
        g.setNombre(nombre != null ? nombre.trim() : null);
        g.setDescripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null);
        g.setTotal(total);
        g.setProveedor(proveedor != null && !proveedor.isBlank() ? proveedor.trim() : null);
        g.setComprobante(comprobante != null && !comprobante.isBlank() ? comprobante.trim() : null);
        g.setUsuarioId(usuarioId);
        g.setUsuarioNombre(usuarioNombre);
        g.setCategoria(parseCategoria(categoriaStr));
        g.setMetodoPago(parseMetodoPago(metodoPagoStr));
        g.setFechaPago(parseFecha(fechaPagoStr));

        return gastoRepository.save(g);
    }

    // ── Editar ───────────────────────────────────────────────────────────────

    public void actualizar(String id,
                           String nombre,
                           String descripcion,
                           String categoriaStr,
                           double total,
                           String fechaPagoStr,
                           String metodoPagoStr,
                           String proveedor,
                           String comprobante) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del gasto es obligatorio.");
        if (total <= 0)
            throw new IllegalArgumentException("El valor del gasto debe ser mayor a cero.");
        if (fechaPagoStr == null || fechaPagoStr.isBlank())
            throw new IllegalArgumentException("La fecha de pago es obligatoria.");

        Optional<Gasto> opt = gastoRepository.findById(id);
        if (opt.isEmpty()) throw new IllegalArgumentException("Gasto no encontrado: " + id);

        Gasto g = opt.get();
        g.setNombre(nombre != null ? nombre.trim() : null);
        g.setDescripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null);
        g.setTotal(total);
        g.setProveedor(proveedor != null && !proveedor.isBlank() ? proveedor.trim() : null);
        g.setComprobante(comprobante != null && !comprobante.isBlank() ? comprobante.trim() : null);
        g.setCategoria(parseCategoria(categoriaStr));
        g.setMetodoPago(parseMetodoPago(metodoPagoStr));
        g.setFechaPago(parseFecha(fechaPagoStr));

        gastoRepository.update(g);
    }

    // ── Eliminar ─────────────────────────────────────────────────────────────

    public void eliminar(String id) {
        gastoRepository.delete(id);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private Gasto.Categoria parseCategoria(String s) {
        if (s == null || s.isBlank()) return Gasto.Categoria.OTROS;
        try { return Gasto.Categoria.valueOf(s.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return Gasto.Categoria.OTROS; }
    }

    private Gasto.MetodoPago parseMetodoPago(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Gasto.MetodoPago.valueOf(s.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    /** Convierte una cadena ISO "yyyy-MM-dd" a Firestore Timestamp. */
    private Timestamp parseFecha(String s) {
        if (s == null || s.isBlank()) return Timestamp.now();
        try {
            LocalDate date = LocalDate.parse(s);
            Date d = Date.from(date.atStartOfDay(ZoneId.of("America/Bogota")).toInstant());
            return Timestamp.of(d);
        } catch (Exception e) {
            return Timestamp.now();
        }
    }
}
