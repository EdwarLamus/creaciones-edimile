package com.creacionesedimile.service;

import com.creacionesedimile.model.Cliente;
import com.creacionesedimile.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio de negocio para gestión de Clientes.
 *
 * Responsabilidades:
 *  - Validar que el número de documento no se repita (RF-02.6)
 *  - Delegar operaciones CRUD al repositorio
 *  - Gestionar activación/desactivación lógica (RF-02.3)
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // -------------------------------------------------------
    // Consultas
    // -------------------------------------------------------

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    /**
     * Busca clientes por término (nombre, documento o empresa). RF-02.4
     * Si el término está vacío devuelve todos.
     */
    public List<Cliente> buscar(String termino) {
        if (termino == null || termino.isBlank()) {
            return clienteRepository.findAll();
        }
        return clienteRepository.buscar(termino);
    }

    // -------------------------------------------------------
    // Creación
    // -------------------------------------------------------

    /**
     * Registra un nuevo cliente.
     *
     * @throws IllegalArgumentException si el número de documento ya existe (RF-02.6)
     * @return ID del documento creado en Firestore
     */
    public String registrarCliente(String nombre, String apellido, String empresa,
                                   String tipoDocumento, String numeroDocumento,
                                   String telefono, String email, String direccion) {

        validarTelefono(telefono);
        validarNumeroDocumento(numeroDocumento);
        if (clienteRepository.existsByNumeroDocumento(numeroDocumento)) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con el número de documento: " + numeroDocumento);
        }

        Cliente nuevo = new Cliente(nombre, apellido, empresa, tipoDocumento,
                                    numeroDocumento, telefono, email, direccion);
        return clienteRepository.save(nuevo);
    }

    // -------------------------------------------------------
    // Actualización
    // -------------------------------------------------------

    /**
     * Actualiza los datos de un cliente existente. (RF-02.2)
     * Valida que el número de documento no colisione con otro cliente distinto.
     */
    public void actualizarCliente(String id, String nombre, String apellido, String empresa,
                                  String tipoDocumento, String numeroDocumento,
                                  String telefono, String email, String direccion) {

        validarTelefono(telefono);
        validarNumeroDocumento(numeroDocumento);
        Optional<Cliente> existente = clienteRepository.findByNumeroDocumento(numeroDocumento);
        if (existente.isPresent() && !existente.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "El número de documento ya está asignado a otro cliente.");
        }

        Map<String, Object> campos = new HashMap<>();
        campos.put("nombre",          nombre);
        campos.put("apellido",        apellido);
        campos.put("empresa",         empresa);
        campos.put("tipoDocumento",   tipoDocumento);
        campos.put("numeroDocumento", numeroDocumento);
        campos.put("telefono",        telefono);
        campos.put("email",           email);
        campos.put("direccion",       direccion);
        clienteRepository.update(id, campos);
    }

    /**
     * Activa o desactiva un cliente (eliminación lógica). (RF-02.3)
     */
    public void actualizarEstado(String id, boolean activo) {
        Map<String, Object> campos = new HashMap<>();
        campos.put("activo", activo);
        clienteRepository.update(id, campos);
    }

    public void eliminar(String id) {
        clienteRepository.delete(id);
    }

    // -------------------------------------------------------
    // Utilidades internas
    // -------------------------------------------------------

    /**
     * Valida que el teléfono no sea vacío y tenga formato numérico válido.
     * Acepta dígitos, espacios, guiones y + inicial. Mínimo 7 dígitos reales.
     */
    private void validarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }
        String soloDigitos = telefono.replaceAll("[\\s\\-+()]", "");
        if (!soloDigitos.matches("[0-9]{7,15}")) {
            throw new IllegalArgumentException(
                    "El teléfono debe contener entre 7 y 15 dígitos. Solo se permiten números, espacios, guiones y el símbolo +.");
        }
    }

    private void validarNumeroDocumento(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio.");
        }
        if (!numero.matches("[A-Za-z0-9\\-]{5,20}")) {
            throw new IllegalArgumentException(
                    "El número de documento solo puede contener letras, números y guiones, con entre 5 y 20 caracteres.");
        }
    }
}
