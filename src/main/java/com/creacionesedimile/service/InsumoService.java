package com.creacionesedimile.service;

import com.creacionesedimile.model.Insumo;
import com.creacionesedimile.model.MovimientoInsumo;
import com.creacionesedimile.repository.InsumoRepository;
import com.creacionesedimile.repository.MovimientoInsumoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InsumoService {

    private final InsumoRepository insumoRepository;
    private final MovimientoInsumoRepository movimientoRepository;

    public InsumoService(InsumoRepository insumoRepository,
                         MovimientoInsumoRepository movimientoRepository) {
        this.insumoRepository = insumoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    // ── Consultas ───────────────────────────────────────────────────────────

    public List<Insumo> listarTodos() {
        return insumoRepository.findAll();
    }

    public Optional<Insumo> buscarPorId(String id) {
        return insumoRepository.findById(id);
    }

    public List<Insumo> buscar(String termino) {
        if (termino == null || termino.isBlank()) return listarTodos();
        return insumoRepository.buscar(termino);
    }

    public List<MovimientoInsumo> listarMovimientos(String insumoId) {
        return movimientoRepository.findByInsumoId(insumoId);
    }

    // ── CRUD de insumos ─────────────────────────────────────────────────────

    public Insumo registrarInsumo(String nombre, String descripcion, String unidadMedida,
                                   double stockActual, double stockMinimo, double precioUnitario) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del insumo es obligatorio.");
        if (unidadMedida == null || unidadMedida.isBlank())
            throw new IllegalArgumentException("La unidad de medida es obligatoria.");
        if (precioUnitario <= 0)
            throw new IllegalArgumentException("El precio unitario debe ser mayor a cero.");
        if (stockMinimo < 0)
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        Insumo insumo = new Insumo();
        insumo.setNombre(nombre.trim());
        insumo.setDescripcion(descripcion != null ? descripcion.trim() : null);
        insumo.setUnidadMedida(unidadMedida.trim());
        insumo.setStockActual(stockActual);
        insumo.setStockMinimo(stockMinimo);
        insumo.setPrecioUnitario(precioUnitario);
        insumo.setActivo(true);
        return insumoRepository.save(insumo);
    }

    public Insumo actualizarInsumo(String id, String nombre, String descripcion,
                                    String unidadMedida, double stockMinimo,
                                    double precioUnitario) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del insumo es obligatorio.");
        if (unidadMedida == null || unidadMedida.isBlank())
            throw new IllegalArgumentException("La unidad de medida es obligatoria.");
        if (precioUnitario <= 0)
            throw new IllegalArgumentException("El precio unitario debe ser mayor a cero.");
        if (stockMinimo < 0)
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado: " + id));
        insumo.setNombre(nombre.trim());
        insumo.setDescripcion(descripcion != null ? descripcion.trim() : null);
        insumo.setUnidadMedida(unidadMedida.trim());
        insumo.setStockMinimo(stockMinimo);
        insumo.setPrecioUnitario(precioUnitario);
        insumoRepository.update(insumo);
        return insumo;
    }

    public void actualizarEstado(String id, boolean activo) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado: " + id));
        insumo.setActivo(activo);
        insumoRepository.update(insumo);
    }

    public void eliminar(String id) {
        insumoRepository.delete(id);
    }

    // ── Movimientos de stock ────────────────────────────────────────────────

    /**
     * Registra una entrada de insumo (aumenta stockActual) — RF-04.2.
     *
     * @param insumoId       ID del insumo
     * @param cantidad       Cantidad a ingresar (debe ser > 0)
     * @param observacion    Motivo o proveedor (opcional)
     * @param usuarioId      ID del usuario que registra
     * @param usuarioNombre  Nombre del usuario que registra
     */
    public MovimientoInsumo registrarEntrada(String insumoId, double cantidad,
                                              String observacion,
                                              String usuarioId, String usuarioNombre) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");

        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado: " + insumoId));

        double stockAnterior = insumo.getStockActual();
        double nuevoStock = stockAnterior + cantidad;

        // Actualizar stock
        insumoRepository.updateStock(insumoId, nuevoStock);

        // Registrar movimiento
        MovimientoInsumo mov = new MovimientoInsumo();
        mov.setInsumoId(insumoId);
        mov.setInsumoNombre(insumo.getNombre());
        mov.setTipo(MovimientoInsumo.Tipo.ENTRADA);
        mov.setCantidad(cantidad);
        mov.setStockAnterior(stockAnterior);
        mov.setStockResultante(nuevoStock);
        mov.setObservacion(observacion);
        mov.setUsuarioId(usuarioId);
        mov.setUsuarioNombre(usuarioNombre);
        return movimientoRepository.save(mov);
    }

    /**
     * Registra una salida de insumo (disminuye stockActual) — RF-04.3.
     *
     * @param insumoId       ID del insumo
     * @param cantidad       Cantidad a retirar (debe ser > 0)
     * @param observacion    Motivo (opcional)
     * @param usuarioId      ID del usuario que registra
     * @param usuarioNombre  Nombre del usuario que registra
     */
    public MovimientoInsumo registrarSalida(String insumoId, double cantidad,
                                             String observacion,
                                             String usuarioId, String usuarioNombre) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");

        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado: " + insumoId));

        double stockAnterior = insumo.getStockActual();
        if (cantidad > stockAnterior) {
            throw new IllegalArgumentException(
                    "Stock insuficiente. Disponible: " + stockAnterior + " " + insumo.getUnidadMedida());
        }

        double nuevoStock = stockAnterior - cantidad;

        // Actualizar stock
        insumoRepository.updateStock(insumoId, nuevoStock);

        // Registrar movimiento
        MovimientoInsumo mov = new MovimientoInsumo();
        mov.setInsumoId(insumoId);
        mov.setInsumoNombre(insumo.getNombre());
        mov.setTipo(MovimientoInsumo.Tipo.SALIDA);
        mov.setCantidad(cantidad);
        mov.setStockAnterior(stockAnterior);
        mov.setStockResultante(nuevoStock);
        mov.setObservacion(observacion);
        mov.setUsuarioId(usuarioId);
        mov.setUsuarioNombre(usuarioNombre);
        return movimientoRepository.save(mov);
    }
}
