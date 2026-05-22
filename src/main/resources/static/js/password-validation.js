/**
 * password-validation.js
 * Validación de contraseña en tiempo real — Edimile Creaciones
 *
 * Funciones exportadas:
 *  - evaluarContrasena(valor) : evalúa criterios y actualiza UI
 *  - evaluarCoincidencia()    : comprueba que ambos campos coincidan
 *  - togglePassword(id, iconId) : muestra/oculta contraseña
 */

/** Muestra / oculta un campo de contraseña */
function togglePassword(inputId, iconId) {
    const input = document.getElementById(inputId);
    const icon  = document.getElementById(iconId);
    if (!input || !icon) return;

    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.replace('bi-eye', 'bi-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.replace('bi-eye-slash', 'bi-eye');
    }
}

/** Evalúa los criterios de la nueva contraseña y actualiza los indicadores */
function evaluarContrasena(valor) {
    const contenedor = document.getElementById('criterios');
    if (!contenedor) return;
    contenedor.style.display = 'block';

    const criterios = {
        'c-length':  valor.length >= 8,
        'c-upper':   /[A-Z]/.test(valor),
        'c-number':  /[0-9]/.test(valor),
        'c-special': /[^A-Za-z0-9]/.test(valor)
    };

    Object.entries(criterios).forEach(([id, cumple]) => {
        const el   = document.getElementById(id);
        const icon = el ? el.querySelector('i') : null;
        if (!el || !icon) return;

        if (cumple) {
            el.classList.add('criterio-ok');
            el.classList.remove('criterio-fail');
            icon.className = 'bi bi-check-circle-fill me-1';
        } else {
            el.classList.add('criterio-fail');
            el.classList.remove('criterio-ok');
            icon.className = 'bi bi-x-circle-fill me-1';
        }
    });

    // Volver a evaluar coincidencia si ya hay algo en el campo confirmación
    evaluarCoincidencia();
}

/** Evalúa si los dos campos de contraseña coinciden */
function evaluarCoincidencia() {
    // Busca el campo de confirmación (puede tener distintos IDs según la página)
    const confirmInput = document.getElementById('confirmPassword')
                      || document.getElementById('passwordConfirmar');
    const nuevoInput   = document.getElementById('password')
                      || document.getElementById('passwordNueva');
    const el           = document.getElementById('c-match');

    if (!confirmInput || !nuevoInput || !el) return;

    const confirmar = confirmInput.value;
    if (!confirmar) {
        el.style.display = 'none';
        return;
    }

    el.style.display = 'block';
    const icon = el.querySelector('i');

    if (confirmar === nuevoInput.value) {
        el.classList.add('criterio-ok');
        el.classList.remove('criterio-fail');
        if (icon) icon.className = 'bi bi-check-circle-fill me-1';
    } else {
        el.classList.add('criterio-fail');
        el.classList.remove('criterio-ok');
        if (icon) icon.className = 'bi bi-x-circle-fill me-1';
    }
}

// Compatibilidad: también activa el toggle del login original si existe
document.addEventListener('DOMContentLoaded', function () {
    const toggleBtn = document.querySelector('.toggle-pwd');
    if (toggleBtn && !toggleBtn.getAttribute('onclick')) {
        toggleBtn.addEventListener('click', function () {
            togglePassword('password', 'toggleIcon');
        });
    }
});
