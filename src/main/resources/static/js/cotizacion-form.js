/**
 * cotizacion-form.js
 * Lógica dinámica para el formulario de cotización (RF-05)
 * - Añade/elimina filas de ítems
 * - Rellena precio unitario según el producto seleccionado
 * - Calcula subtotales y totales en tiempo real
 */

(function () {
    'use strict';

    // ── Referencias DOM ──────────────────────────────────────────────────────
    const itemsContainer = document.getElementById('itemsContainer');
    const btnAgregar     = document.getElementById('btnAgregarItem');
    const noItemsMsg     = document.getElementById('noItemsMsg');
    const descuentoInput = document.getElementById('descuentoPct');
    const ivaInput       = document.getElementById('ivaPct');
    const resSubtotal    = document.getElementById('resSubtotal');
    const resDescuento   = document.getElementById('resDescuento');
    const resIva         = document.getElementById('resIva');
    const resTotal       = document.getElementById('resTotal');

    // Mapa id → precioBase construido desde productosData (inyectado por Thymeleaf)
    const preciosPorId = {};
    if (typeof productosData !== 'undefined' && Array.isArray(productosData)) {
        productosData.forEach(p => {
            preciosPorId[p.id] = p.precioBase || 0;
        });
    }

    // ── Construcción de opciones de productos para filas nuevas ─────────────
    function buildProductoOptions() {
        let html = '<option value="">— Producto —</option>';
        if (typeof productosData !== 'undefined' && Array.isArray(productosData)) {
            productosData.forEach(p => {
                html += `<option value="${escHtml(p.id)}" data-precio="${p.precioBase || 0}">
                            ${escHtml(p.nombre)}
                         </option>`;
            });
        }
        return html;
    }

    // ── Agregar fila ─────────────────────────────────────────────────────────
    btnAgregar.addEventListener('click', () => {
        const template = document.getElementById('itemRowTemplate');
        const clone    = template.content.cloneNode(true);
        const row      = clone.querySelector('tr');

        // Sustituir opciones de producto
        const select = row.querySelector('.prod-select');
        select.innerHTML = buildProductoOptions();

        // Enlazar eventos de la fila nueva
        bindRowEvents(row);

        itemsContainer.appendChild(row);
        noItemsMsg.style.display = 'none';
        actualizarTotales();
    });

    // ── Vincular eventos a una fila ──────────────────────────────────────────
    function bindRowEvents(row) {
        const prodSelect  = row.querySelector('.prod-select');
        const cantInput   = row.querySelector('.cant-input');
        const precioInput = row.querySelector('.precio-input');
        const elimBtn     = row.querySelector('.btn-eliminar-item');

        prodSelect.addEventListener('change', () => {
            const precio = preciosPorId[prodSelect.value] || 0;
            precioInput.value = precio.toFixed(2);
            actualizarSubtotalFila(row);
            actualizarTotales();
        });

        cantInput.addEventListener('input',   () => { actualizarSubtotalFila(row); actualizarTotales(); });
        precioInput.addEventListener('input',  () => { actualizarSubtotalFila(row); actualizarTotales(); });

        elimBtn.addEventListener('click', () => {
            row.remove();
            if (itemsContainer.querySelectorAll('.item-row').length === 0) {
                noItemsMsg.style.display = '';
            }
            actualizarTotales();
        });
    }

    // Actualiza el span de subtotal de una fila
    function actualizarSubtotalFila(row) {
        const cant   = parseFloat(row.querySelector('.cant-input')?.value)   || 0;
        const precio = parseFloat(row.querySelector('.precio-input')?.value) || 0;
        const sub    = cant * precio;
        const span   = row.querySelector('.subtotal-span');
        if (span) span.textContent = formatMoney(sub);
    }

    // ── Recalcular totales del resumen ───────────────────────────────────────
    function actualizarTotales() {
        let subtotal = 0;
        itemsContainer.querySelectorAll('.item-row').forEach(row => {
            const cant   = parseFloat(row.querySelector('.cant-input')?.value)   || 0;
            const precio = parseFloat(row.querySelector('.precio-input')?.value) || 0;
            subtotal += cant * precio;
        });

        const descPct = parseFloat(descuentoInput?.value) || 0;
        const ivaPct  = parseFloat(ivaInput?.value)       || 0;

        const descVal = subtotal * (descPct / 100);
        const base    = subtotal - descVal;
        const ivaVal  = base * (ivaPct / 100);
        const total   = base + ivaVal;

        resSubtotal.textContent  = formatMoney(subtotal);
        resDescuento.textContent = '- ' + formatMoney(descVal);
        resIva.textContent       = formatMoney(ivaVal);
        resTotal.textContent     = formatMoney(total);
    }

    // ── Utilidades ────────────────────────────────────────────────────────────
    function formatMoney(n) {
        return '$ ' + n.toLocaleString('es-CO', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
    }

    function escHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/"/g, '&quot;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }

    // ── Inicialización para filas existentes (modo edición) ──────────────────
    itemsContainer.querySelectorAll('.item-row').forEach(row => {
        bindRowEvents(row);
        actualizarSubtotalFila(row);
    });
    actualizarTotales();

    // Recalcular cuando cambia descuento o IVA
    descuentoInput?.addEventListener('input', actualizarTotales);
    ivaInput?.addEventListener('input',       actualizarTotales);

    // Validar al enviar: al menos un ítem
    document.getElementById('formCotizacion')?.addEventListener('submit', e => {
        const filas = itemsContainer.querySelectorAll('.item-row');
        if (filas.length === 0) {
            e.preventDefault();
            alert('Debes agregar al menos un ítem a la cotización.');
            return;
        }
        // Validar que cada fila tenga producto seleccionado
        let valid = true;
        filas.forEach(row => {
            const sel = row.querySelector('.prod-select');
            if (!sel || !sel.value) { valid = false; sel?.focus(); }
        });
        if (!valid) {
            e.preventDefault();
            alert('Selecciona un producto en cada ítem antes de guardar.');
        }
    });
})();
