/* ═══════════════════════════════════════════════════════════════════
   ACMAFER — Carrito de Pedidos (Drawer)
   Carrito aislado por usuario: cada usuario tiene su propia clave
   en localStorage → acm_carrito_{userId}
   El userId se lee del <meta name="acm-user-id"> inyectado por
   Thymeleaf en el <head> del layout base.
   ═══════════════════════════════════════════════════════════════════ */
'use strict';

document.addEventListener('DOMContentLoaded', function () {

    // ────────────────────────────────────────────────────────────────
    // 1. CLAVE ÚNICA POR USUARIO
    //    Lee el meta "acm-user-id" que el layout base inyecta con
    //    Thymeleaf. Si no existe usa 'anonimo'.
    //    Resultado: acm_carrito_5, acm_carrito_12, etc.
    // ────────────────────────────────────────────────────────────────
    const metaUser        = document.querySelector('meta[name="acm-user-id"]');
    const userId          = (metaUser && metaUser.content && metaUser.content !== '')
                                ? metaUser.content
                                : 'anonimo';
    const CLAVE_CARRITO   = 'acm_carrito_'           + userId;
    const CLAVE_PENDIENTE = 'acm_carrito_pendiente_' + userId;

    // ────────────────────────────────────────────────────────────────
    // 2. CARGA EL CARRITO DEL USUARIO ACTUAL
    // ────────────────────────────────────────────────────────────────
    let carrito      = JSON.parse(localStorage.getItem(CLAVE_CARRITO) || '[]');
    let drawerAbierto = false;

    // ────────────────────────────────────────────────────────────────
    // 3. REFERENCIAS AL DOM
    // ────────────────────────────────────────────────────────────────
    let drawerOverlay = document.getElementById('drawerOverlay');
    let carritoDrawer = document.getElementById('carritoDrawer');
    let drawerClose   = document.getElementById('drawerClose');
    let cartFab       = document.getElementById('cartFab');
    let cartBadge     = document.getElementById('cartBadge');
    let carritoVacio  = document.getElementById('carritoVacio');
    let carritoItems  = document.getElementById('carritoItems');
    let carritoTotal  = document.getElementById('carritoTotal');
    let drawerFooter  = document.getElementById('drawerFooter');
    let btnConfirmar  = document.getElementById('btnConfirmarPedido');

    // ────────────────────────────────────────────────────────────────
    // 4. LIMPIA EL CARRITO SI EL PEDIDO YA FUE CONFIRMADO
    //    Se activa al volver a mis-pedidos con alerta de éxito.
    // ────────────────────────────────────────────────────────────────
    (function limpiarSiPedidoConfirmado() {
        const pendiente    = localStorage.getItem(CLAVE_PENDIENTE);
        if (!pendiente) return;
        const enMisPedidos = window.location.pathname.indexOf('/pedidos/mis-pedidos') === 0;
        const hayExito     = document.querySelector('.acm-alert-success, .alert.acm-alert-success');
        if (enMisPedidos && hayExito) {
            carrito = [];
            localStorage.removeItem(CLAVE_PENDIENTE);
            guardarCarrito();
        }
    })();

    // ────────────────────────────────────────────────────────────────
    // 5. RENDERIZADO INICIAL
    // ────────────────────────────────────────────────────────────────
    actualizarBadge();
    renderCarrito();

    // ────────────────────────────────────────────────────────────────
    // 6. EVENTOS DE UI
    // ────────────────────────────────────────────────────────────────
    if (drawerClose)   drawerClose.addEventListener('click',   () => cerrarDrawer());
    if (drawerOverlay) drawerOverlay.addEventListener('click', (e) => { if (e.target === drawerOverlay) cerrarDrawer(); });
    if (cartFab)       cartFab.addEventListener('click',       () => abrirDrawer());
    if (btnConfirmar)  btnConfirmar.addEventListener('click',  () => confirmarPedido());

    // ────────────────────────────────────────────────────────────────
    // 7. DELEGACIÓN DE EVENTOS — botones de agregar en tarjetas
    // ────────────────────────────────────────────────────────────────
    document.addEventListener('click', function (e) {

        // Botón "+" en tarjeta del catálogo
        const btnCart = e.target.closest('.btn-card-cart');
        if (btnCart) {
            e.preventDefault();
            e.stopPropagation();
            const id     = btnCart.dataset.id;
            const nombre = btnCart.dataset.nombre;
            const precio = btnCart.dataset.precio;
            const stock  = btnCart.dataset.stock;
            if (id && nombre) {
                agregarAlCarrito(parseInt(id), nombre, parseFloat(precio) || 0, parseInt(stock) || 0);
            }
            return;
        }

        // Botón "Solicitar" en página de detalle
        const btnSolicitar = e.target.closest('.btn-solicitar');
        if (btnSolicitar) {
            e.preventDefault();
            const id     = btnSolicitar.dataset.id;
            const nombre = btnSolicitar.dataset.nombre;
            const precio = btnSolicitar.dataset.precio;
            const stock  = btnSolicitar.dataset.stock;
            if (id && nombre && precio) {
                agregarAlCarrito(parseInt(id), nombre, parseFloat(precio), parseInt(stock) || 0);
            }
            return;
        }
    });

    // ════════════════════════════════════════════════════════════════
    //  API PÚBLICA
    // ════════════════════════════════════════════════════════════════

    window.agregarAlCarrito = function (id, nombre, precio, stock) {
        const producto = carrito.find(p => p.id === id);
        if (producto) {
            if (producto.cantidad >= stock) {
                mostrarNotificacion('Stock máximo alcanzado', 'warning');
                return;
            }
            producto.cantidad++;
        } else {
            carrito.push({
                id,
                nombre,
                precio  : parseFloat(precio),
                stock   : parseInt(stock),
                cantidad: 1
            });
        }
        guardarCarrito();
        actualizarBadge();
        renderCarrito();
        mostrarNotificacion(`"${nombre}" agregado al carrito`, 'success');
        if (carrito.length === 1 && producto === undefined) {
            setTimeout(() => abrirDrawer(), 300);
        }
    };

    window.cambiarCantidad = function (id, delta) {
        const producto = carrito.find(p => p.id === id);
        if (!producto) return;
        producto.cantidad += delta;
        if (producto.cantidad <= 0) {
            carrito = carrito.filter(p => p.id !== id);
        } else if (producto.cantidad > producto.stock) {
            producto.cantidad = producto.stock;
            mostrarNotificacion('Stock máximo alcanzado', 'warning');
        }
        guardarCarrito();
        actualizarBadge();
        renderCarrito();
    };

    window.eliminarDelCarrito = function (id) {
        carrito = carrito.filter(p => p.id !== id);
        guardarCarrito();
        actualizarBadge();
        renderCarrito();
        mostrarNotificacion('Producto eliminado', 'info');
    };

    window.abrirDrawer = function () {
        drawerOverlay = document.getElementById('drawerOverlay');
        carritoDrawer = document.getElementById('carritoDrawer');
        if (drawerOverlay && carritoDrawer) {
            drawerOverlay.classList.add('open');
            carritoDrawer.classList.add('open');
            drawerAbierto = true;
            document.body.style.overflow = 'hidden';
        }
    };

    window.cerrarDrawer = function () {
        drawerOverlay = document.getElementById('drawerOverlay');
        carritoDrawer = document.getElementById('carritoDrawer');
        if (drawerOverlay && carritoDrawer) {
            drawerOverlay.classList.remove('open');
            carritoDrawer.classList.remove('open');
            drawerAbierto = false;
            document.body.style.overflow = '';
        }
    };

    window.abrirDrawerPedido = function () {
        if (carrito.length === 0) {
            mostrarNotificacion('Agrega productos al carrito primero', 'info');
            return;
        }
        abrirDrawer();
    };

    // ════════════════════════════════════════════════════════════════
    //  FUNCIONES INTERNAS
    // ════════════════════════════════════════════════════════════════

    function guardarCarrito() {
        localStorage.setItem(CLAVE_CARRITO, JSON.stringify(carrito));
    }

    function actualizarBadge() {
        const total = carrito.reduce((sum, p) => sum + p.cantidad, 0);
        cartBadge = document.getElementById('cartBadge');
        cartFab   = document.getElementById('cartFab');
        if (cartBadge) cartBadge.textContent = total;
        if (cartFab)   cartFab.style.display  = total > 0 ? 'flex' : 'none';
    }

    function renderCarrito() {
        carritoVacio = document.getElementById('carritoVacio');
        carritoItems = document.getElementById('carritoItems');
        carritoTotal = document.getElementById('carritoTotal');
        drawerFooter = document.getElementById('drawerFooter');

        if (!carritoItems) return;

        if (carrito.length === 0) {
            if (carritoVacio) carritoVacio.style.display = 'block';
            carritoItems.style.display                   = 'none';
            if (carritoTotal) carritoTotal.style.display = 'none';
            if (drawerFooter) drawerFooter.style.display = 'none';
        } else {
            if (carritoVacio) carritoVacio.style.display = 'none';
            carritoItems.style.display                   = 'block';
            if (carritoTotal) carritoTotal.style.display = 'block';
            if (drawerFooter) drawerFooter.style.display = 'block';

            let html     = '';
            let subtotal = 0;

            carrito.forEach(producto => {
                const sub = producto.precio * producto.cantidad;
                subtotal += sub;
                html += `
                    <div class="carrito-item">
                        <div class="carrito-item-info">
                            <div class="carrito-item-nombre">${producto.nombre}</div>
                            <div class="carrito-item-precio">
                                $${producto.precio.toLocaleString('es-CO')} c/u
                            </div>
                        </div>
                        <div class="carrito-item-controls">
                            <button class="carrito-btn" onclick="cambiarCantidad(${producto.id}, -1)">
                                <i class="bi bi-dash"></i>
                            </button>
                            <span class="carrito-cantidad">${producto.cantidad}</span>
                            <button class="carrito-btn" onclick="cambiarCantidad(${producto.id}, 1)">
                                <i class="bi bi-plus"></i>
                            </button>
                        </div>
                        <div class="carrito-item-subtotal">
                            $${sub.toLocaleString('es-CO')}
                        </div>
                        <button class="carrito-eliminar" onclick="eliminarDelCarrito(${producto.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>`;
            });

            carritoItems.innerHTML = html;

            const totalEl    = document.getElementById('totalCarrito');
            const subtotalEl = document.getElementById('subtotalCarrito');
            if (totalEl)    totalEl.textContent    = '$' + subtotal.toLocaleString('es-CO');
            if (subtotalEl) subtotalEl.textContent = '$' + subtotal.toLocaleString('es-CO');
        }
    }

    function confirmarPedido() {
        if (carrito.length === 0) {
            mostrarNotificacion('El carrito está vacío', 'error');
            return;
        }

        const observacionesEl = document.getElementById('observaciones');
        const observaciones   = observacionesEl ? observacionesEl.value : '';

        localStorage.setItem(CLAVE_PENDIENTE, '1');

        const token      = document.querySelector('meta[name="_csrf"]')?.content || '';
        const tokenParam = document.querySelector('meta[name="_csrf_param"]')?.content || '_csrf';

        const form         = document.createElement('form');
        form.method        = 'POST';
        form.action        = '/pedidos/checkout';
        form.style.display = 'none';

        if (token) {
            const csrfInput = document.createElement('input');
            csrfInput.type  = 'hidden';
            csrfInput.name  = tokenParam;
            csrfInput.value = token;
            form.appendChild(csrfInput);
        }

        carrito.forEach((p, i) => {
            const idInput        = document.createElement('input');
            idInput.type         = 'hidden';
            idInput.name         = `detalles[${i}].idProducto`;
            idInput.value        = p.id;
            form.appendChild(idInput);

            const cantInput      = document.createElement('input');
            cantInput.type       = 'hidden';
            cantInput.name       = `detalles[${i}].cantidad`;
            cantInput.value      = p.cantidad;
            form.appendChild(cantInput);
        });

        const notasInput     = document.createElement('input');
        notasInput.type      = 'hidden';
        notasInput.name      = 'notas';
        notasInput.value     = observaciones || '';
        form.appendChild(notasInput);

        document.body.appendChild(form);
        form.submit();
    }

    function mostrarNotificacion(mensaje, tipo) {
        const notif     = document.createElement('div');
        notif.className = `acm-notif acm-notif-${tipo}`;
        const icono     = tipo === 'success' ? 'check-circle-fill'
                        : tipo === 'error'   ? 'exclamation-triangle-fill'
                        : tipo === 'warning' ? 'exclamation-circle-fill'
                        :                      'info-circle-fill';
        notif.innerHTML = `<i class="bi bi-${icono}"></i><span>${mensaje}</span>`;
        document.body.appendChild(notif);
        setTimeout(() => notif.classList.add('show'), 10);
        setTimeout(() => {
            notif.classList.remove('show');
            setTimeout(() => notif.remove(), 300);
        }, 3000);
    }

});