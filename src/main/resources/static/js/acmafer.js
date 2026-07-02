/* ACMAFER — Main JavaScript */
'use strict';

/* ── Theme ───────────────────────────────────────────────────────────── */
(function () {
  const saved = localStorage.getItem('acmafer_theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);
  const btn = document.getElementById('themeBtn');
  if (btn) {
    btn.innerHTML = saved === 'dark' ? '<i class="bi bi-sun-fill"></i>' : '<i class="bi bi-moon-fill"></i>';
    btn.addEventListener('click', () => {
      const isDark = document.documentElement.dataset.theme !== 'light';
      const next = isDark ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', next);
      localStorage.setItem('acmafer_theme', next);
      btn.innerHTML = next === 'dark' ? '<i class="bi bi-sun-fill"></i>' : '<i class="bi bi-moon-fill"></i>';
    });
  }
})();

/* ── Navbar Dropdowns - DELEGACIÓN DE EVENTOS (SOLUCIÓN DEFINITIVA) ── */
(function () {
  // Deshabilitar el manejo automático de Bootstrap para dropdowns
  document.addEventListener('click.bs.dropdown.data-api', function (e) {
    e.preventDefault();
    e.stopImmediatePropagation();
  }, true);

  // Delegación de eventos en document (funciona siempre, incluso después de navegación)
  document.addEventListener('click', function (e) {
    const toggle = e.target.closest('.dropdown-toggle');

    if (toggle) {
      e.preventDefault();
      e.stopImmediatePropagation();

      const dropdown = toggle.closest('.dropdown');
      if (!dropdown) return;

      const menu = dropdown.querySelector('.dropdown-menu');
      if (!menu) return;

      const isOpen = menu.classList.contains('show');

      // Cerrar todos los demás dropdowns
      document.querySelectorAll('.dropdown-menu.show').forEach(m => {
        if (m !== menu) m.classList.remove('show');
      });

      // Toggle del dropdown actual
      if (!isOpen) {
        menu.classList.add('show');
        toggle.setAttribute('aria-expanded', 'true');
      } else {
        menu.classList.remove('show');
        toggle.setAttribute('aria-expanded', 'false');
      }
    } else if (!e.target.closest('.dropdown-menu')) {
      // Click fuera → cerrar todos
      document.querySelectorAll('.dropdown-menu.show').forEach(m => {
        m.classList.remove('show');
      });
    }
  }, true); // Usar capture phase para interceptar antes que Bootstrap

  console.log('✓ Delegación de eventos para dropdowns activada');
})();

/* ── Menú Móvil ──────────────────────────────────────────────────────── */
(function () {
  function initMobileMenu() {
    const menuToggle = document.getElementById('menuToggle');
    const navbarNav = document.querySelector('.navbar-nav');

    if (menuToggle && navbarNav) {
      menuToggle.addEventListener('click', function (e) {
        e.stopPropagation();
        navbarNav.classList.toggle('mobile-open');
      });

      document.addEventListener('click', function (e) {
        if (navbarNav.classList.contains('mobile-open')) {
          if (!navbarNav.contains(e.target) && !menuToggle.contains(e.target)) {
            navbarNav.classList.remove('mobile-open');
          }
        }
      });

      window.addEventListener('resize', function () {
        if (window.innerWidth > 992) {
          navbarNav.classList.remove('mobile-open');
        }
      });
    }
  }

  initMobileMenu();
  setTimeout(initMobileMenu, 300);
})();

/* ── Navbar scroll ────────────────────────────────────────────────────── */
window.addEventListener('scroll', () => {
  const nav = document.getElementById('mainNav');
  if (nav) nav.classList.toggle('scrolled', window.scrollY > 40);
}, { passive: true });

/* ── Intro animation ──────────────────────────────────────────────────── */
window.skipIntro = function () {
  const ov = document.getElementById('introOverlay');
  if (!ov) return;
  sessionStorage.setItem('acm_intro', '1');
  ov.classList.add('done');
  setTimeout(() => ov.style.display = 'none', 850);
};
(function () {
  const ov = document.getElementById('introOverlay');
  if (!ov) return;
  if (sessionStorage.getItem('acm_intro')) { skipIntro(); return; }
  setTimeout(() => {
    const w = document.getElementById('cldWrap');
    if (w) { w.style.transition = 'transform 0.8s cubic-bezier(.4,0,.2,1)'; w.style.transform = 'rotate(-32deg) translate(-35px,-18px)'; }
  }, 1800);
  setTimeout(() => skipIntro(), 4500);
})();

/* ── Intro canvas sparks ──────────────────────────────────────────────── */
(function () {
  const c = document.getElementById('introCvs');
  if (!c) return;
  c.width = window.innerWidth; c.height = window.innerHeight;
  const ctx = c.getContext('2d');
  const embers = [];
  class Ember {
    constructor(x, y) {
      this.x = x || c.width / 2; this.y = y || c.height / 2 - 30;
      this.vx = (Math.random() - .5) * 6; this.vy = -(Math.random() * 8 + 3);
      this.sz = Math.random() * 4 + 1; this.life = 1; this.decay = Math.random() * .02 + .015;
      this.col = Math.random() < .5 ? '#FFFF66' : '#FF8A33';
    }
    update() { this.x += this.vx; this.y += this.vy; this.vy += .15; this.life -= this.decay; this.sz *= .985 }
    draw() {
      ctx.save(); ctx.globalAlpha = Math.max(0, this.life);
      const g = ctx.createRadialGradient(this.x, this.y, 0, this.x, this.y, this.sz * 2);
      g.addColorStop(0, this.col); g.addColorStop(1, 'rgba(255,106,0,0)');
      ctx.fillStyle = g; ctx.beginPath(); ctx.arc(this.x, this.y, this.sz * 2, 0, Math.PI * 2); ctx.fill();
      ctx.restore();
    }
    dead() { return this.life <= 0 || this.sz < .2 }
  }
  let ebArr = [];
  function spawnEmbers(n) { for (let i = 0; i < n; i++)ebArr.push(new Ember()) }
  function loopEmbers() {
    ctx.clearRect(0, 0, c.width, c.height);
    ebArr = ebArr.filter(e => !e.dead());
    ebArr.forEach(e => { e.update(); e.draw() });
    if (Math.random() < .08) spawnEmbers(1);
    if (!document.getElementById('introOverlay')?.classList.contains('done')) requestAnimationFrame(loopEmbers);
  }
  spawnEmbers(5); loopEmbers();
  setTimeout(() => spawnEmbers(30), 1800);
  setTimeout(() => spawnEmbers(50), 1850);
})();

/* ── Scroll reveal ────────────────────────────────────────────────────── */
(function () {
  const els = document.querySelectorAll('.reveal,.kpi-card,.product-card,.acm-card,.chart-card');
  if (!els.length) return;
  const obs = new IntersectionObserver(entries => {
    entries.forEach((e, i) => {
      if (e.isIntersecting) {
        setTimeout(() => e.target.classList.add('visible'), i * 70);
        obs.unobserve(e.target);
      }
    });
  }, { threshold: .08 });
  els.forEach(el => obs.observe(el));
})();

/* ── KPI counter animation ────────────────────────────────────────────── */
(function () {
  document.querySelectorAll('[data-count]').forEach(el => {
    const target = parseFloat(el.dataset.count, 10);
    if (isNaN(target)) return;
    let cur = 0; const dur = 1600, step = 16, inc = target / (dur / step);
    const t = setInterval(() => {
      cur = Math.min(cur + inc, target);
      el.textContent = Math.floor(cur).toLocaleString('es-CO');
      if (cur >= target) clearInterval(t);
    }, step);
  });
})();

/* ── Product card 3D tilt ─────────────────────────────────────────────── */
document.querySelectorAll('.product-card').forEach(card => {
  card.addEventListener('mousemove', e => {
    const r = card.getBoundingClientRect();
    const x = (e.clientX - r.left) / r.width - .5;
    const y = (e.clientY - r.top) / r.height - .5;
    card.style.transform = `translateY(-9px) rotateX(${-y * 8}deg) rotateY(${x * 8}deg)`;
  });
  card.addEventListener('mouseleave', () => card.style.transform = '');
});

/* ── Product Modal ────────────────────────────────────────────────────── */
window.openProductModal = function (card) {
  const ov = document.getElementById('productModal');
  if (!ov) return;
  const d = card.dataset;
  document.getElementById('modalTitle').textContent = d.nombre || '';
  document.getElementById('modalCat').textContent = d.categoria || '';
  document.getElementById('modalCode').textContent = d.codigo || '';
  document.getElementById('modalDesc').textContent = d.descripcion || 'Sin descripción';
  document.getElementById('modalPrice').textContent =
    d.precio ? '$' + parseFloat(d.precio).toLocaleString('es-CO') + ' COP' : 'Consultar';
  document.getElementById('modalStock').textContent = d.stock || '0';
  document.getElementById('modalEstado').textContent = d.estado || '';

  const imgEl = document.getElementById('modalProductImg');
  const iconEl = document.getElementById('modal3dIcon');
  if (imgEl && d.imagen) {
    imgEl.src = d.imagen;
    imgEl.style.display = 'block';
    if (iconEl) iconEl.style.display = 'none';
  } else {
    if (imgEl) imgEl.style.display = 'none';
    if (iconEl) { iconEl.style.display = 'flex'; iconEl.textContent = d.icon || '⚙'; }
  }

  const cartBtn = document.getElementById('modalCartBtn');
  if (cartBtn) cartBtn.onclick = () => {
    if (window.agregarAlCarrito) {
      agregarAlCarrito(parseInt(d.id || 0), d.nombre, parseFloat(d.precio) || 0, parseInt(d.stock) || 0);
    }
    closeProductModal();
  };
  const detailBtn = document.getElementById('modalDetailBtn');
  if (detailBtn && d.url) detailBtn.onclick = () => window.location.href = d.url;
  ov.classList.add('open'); document.body.style.overflow = 'hidden';
};
window.closeProductModal = function () {
  const ov = document.getElementById('productModal');
  if (ov) { ov.classList.remove('open'); document.body.style.overflow = ''; }
};
document.addEventListener('keydown', e => { if (e.key === 'Escape') window.closeProductModal() });

/* ── Carousel (top vendidos) ──────────────────────────────────────────── */
(function () {
  const track = document.getElementById('carouselTrack');
  const prev = document.getElementById('carouselPrev');
  const next = document.getElementById('carouselNext');
  if (!track) return;
  let idx = 0;
  const items = track.querySelectorAll('.carousel-item-card');
  const itemW = 240 + 16;
  const visible = Math.floor(track.parentElement.offsetWidth / itemW) || 1;
  const max = Math.max(0, items.length - visible);
  function move(d) {
    idx = Math.max(0, Math.min(idx + d, max));
    track.style.transform = `translateX(-${idx * itemW}px)`;
  }
  if (prev) prev.addEventListener('click', () => move(-1));
  if (next) next.addEventListener('click', () => move(1));
  setInterval(() => move(idx < max ? 1 : -idx), 4000);
})();

/* ── Chatbot AcmaBot — Groq IA + Base de datos ──────────────────────── */
(function () {
  const toggle = document.getElementById('chatToggle');
  const win    = document.getElementById('chatWindow');
  const close  = document.getElementById('chatClose');
  const input  = document.getElementById('chatInput');
  const send   = document.getElementById('chatSend');
  const body   = document.getElementById('chatBody');
  if (!toggle || !win) return;

  /* Sugerencias según la página actual */
  function getSugerencias() {
    const path = window.location.pathname;
    if (path.includes('/productos')) return ['¿Cuál es el más vendido?','¿Qué productos hay disponibles?','¿Cómo hago un pedido?','¿Hay stock bajo?'];
    if (path.includes('/pedidos'))   return ['¿Cómo creo un pedido?','Estado de mis pedidos','¿Cuánto demora la entrega?','Ver catálogo'];
    if (path.includes('/dashboard')) return ['¿Qué productos hay disponibles?','¿Cuál es el más vendido?','¿Pedidos pendientes?','¿Hay stock bajo?'];
    if (path.includes('/tareas'))    return ['¿Cuántas tareas hay pendientes?','¿Tareas bloqueadas?','Ver mis tareas','Ayuda con el sistema'];
    if (path === '/' || path === '') return ['¿Qué es ACMAFER?','¿Qué productos fabrican?','¿Cómo me registro?','¿Dónde están ubicados?'];
    return ['¿Qué productos hay disponibles?','¿Cómo hago un pedido?','¿Cuál es el más vendido?','Información sobre ACMAFER'];
  }

  /* Saludo según página */
  function getSaludo() {
    const path = window.location.pathname;
    if (path === '/' || path === '') return '¡Hola! 👋 Soy <b>AcmaBot</b>, el asistente de <b>ACMAFER</b> 🔥<br>Pregúntame sobre nuestros productos, servicios o cómo registrarte. ¿En qué te ayudo? 😊';
    if (path.includes('/productos')) return '¡Hola! 👋 Estás en el <b>catálogo</b>. Puedo decirte disponibilidad, precios y los más vendidos. ¿Qué buscas? 😊';
    if (path.includes('/pedidos'))   return '¡Hola! 👋 Estás en <b>pedidos</b>. Te ayudo a crear uno, ver el estado o resolver dudas. ¿Qué necesitas? 😊';
    if (path.includes('/dashboard')) return '¡Hola! 👋 Soy <b>AcmaBot</b> 🤖 Tengo acceso al inventario, pedidos y más en tiempo real. ¿Qué consultas hoy? 😊';
    return '¡Hola! 👋 Soy <b>AcmaBot</b>, tu asistente de ACMAFER 🤖🔥<br>Pregúntame sobre productos, pedidos, inventario o cómo navegar el sistema. 😊';
  }

  let saludoMostrado = false;

  /* Abrir/cerrar */
  toggle.addEventListener('click', () => {
    win.classList.toggle('open');
    if (win.classList.contains('open') && !saludoMostrado) {
      saludoMostrado = true;
      setTimeout(() => { appendMsg(getSaludo(), 'bot', true); setTimeout(mostrarSugerencias, 400); }, 200);
    }
  });
  close?.addEventListener('click', () => win.classList.remove('open'));

  /* Chips de sugerencia */
  function mostrarSugerencias() {
    document.querySelector('.acm-chat-sugerencias')?.remove();
    const wrap = document.createElement('div');
    wrap.className = 'acm-chat-sugerencias';
    getSugerencias().forEach(s => {
      const btn = document.createElement('button');
      btn.className = 'acm-chat-chip';
      btn.textContent = s;
      btn.addEventListener('click', () => { wrap.remove(); enviarMensaje(s); });
      wrap.appendChild(btn);
    });
    body.appendChild(wrap);
    body.scrollTop = body.scrollHeight;
  }

  /* Agregar burbuja de mensaje */
  function appendMsg(contenido, quien, esHtml = false) {
    const d = document.createElement('div');
    d.className = 'acm-chat-msg ' + quien;
    if (esHtml) d.innerHTML = contenido; else d.textContent = contenido;
    body.appendChild(d);
    body.scrollTop = body.scrollHeight;
  }

  /* Indicador de escritura */
  function mostrarEscribiendo() {
    const d = document.createElement('div');
    d.className = 'acm-chat-msg bot acm-typing';
    d.id = 'acmTyping';
    d.innerHTML = '<span></span><span></span><span></span>';
    body.appendChild(d);
    body.scrollTop = body.scrollHeight;
    return d;
  }

  /* Enviar al backend (Groq vía Spring Boot) */
  async function enviarMensaje(textoForzado) {
    const txt = (textoForzado || input?.value || '').trim();
    if (!txt) return;
    if (input) input.value = '';
    document.querySelector('.acm-chat-sugerencias')?.remove();
    appendMsg(txt, 'user');
    const typing = mostrarEscribiendo();
    try {
      const res = await fetch('/chatbot/responder', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' },
        body: JSON.stringify({ mensaje: txt })
      });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      const data = await res.json();
      typing.remove();
      appendMsg(data.respuesta || '¡Ups! No recibí respuesta 😅', 'bot', true);
    } catch (err) {
      typing.remove();
      appendMsg('Tuve un problemita de conexión 😅 Inténtalo de nuevo.', 'bot');
    }
    setTimeout(mostrarSugerencias, 600);
  }

  send?.addEventListener('click', () => enviarMensaje());
  input?.addEventListener('keydown', e => { if (e.key === 'Enter') enviarMensaje(); });

  /* Auto-abrir en página pública después de 3.5s */
  if (window.location.pathname === '/' || window.location.pathname === '') {
    setTimeout(() => {
      if (!win.classList.contains('open')) {
        win.classList.add('open');
        if (!saludoMostrado) {
          saludoMostrado = true;
          appendMsg(getSaludo(), 'bot', true);
          setTimeout(mostrarSugerencias, 400);
        }
      }
    }, 3500);
  }
})();

/* ── Charts init helper (Chart.js) ───────────────────────────────────── */
window.acmFireGradient = function (ctx, colorStops) {
  const area = ctx.chartArea;
  const top = area ? area.top : 0;
  const bottom = area ? area.bottom : 300;
  const g = ctx.ctx.createLinearGradient(0, top, 0, bottom);
  colorStops.forEach(s => g.addColorStop(s.offset, s.color));
  return g;
};

const ACM_PALETTE = [
  ['#FF6A00', '#FFB347'],
  ['#2563EB', '#60A5FA'],
  ['#16A34A', '#86EFAC'],
  ['#CC7032', '#FFD9A0'],
  ['#098ABD', '#7DD3FC'],
  ['#9CA3AF', '#E5E7EB'],
];

window.acmChart = function (id, type, labels, datasets, opts = {}) {
  const canvas = document.getElementById(id);
  if (!canvas) return;
  const ctx2d = canvas.getContext('2d');
  const isDark = document.documentElement.dataset.theme !== 'light';
  const gridColor = isDark ? 'rgba(255,255,255,.06)' : 'rgba(0,0,0,.06)';
  const textColor = isDark ? '#9CA3AF' : '#485563';
  Chart.defaults.color = textColor;
  Chart.defaults.font.family = "'Rajdhani',sans-serif";

  const isBarOrLine = ['bar', 'line'].includes(type);
  datasets.forEach((ds, i) => {
    const [c1, c2] = ACM_PALETTE[i % ACM_PALETTE.length];
    if (isBarOrLine && !ds.backgroundColor) {
      ds.backgroundColor = (context) => {
        const { chart } = context;
        const { ctx, chartArea } = chart;
        if (!chartArea) return c1;
        const g = ctx.createLinearGradient(0, chartArea.bottom, 0, chartArea.top);
        g.addColorStop(0, c1 + '33');
        g.addColorStop(1, c1 + 'CC');
        return g;
      };
      if (type === 'bar') ds.borderRadius = ds.borderRadius ?? 8;
      if (type === 'bar') ds.borderSkipped = false;
    }
    if (type === 'line') {
      ds.borderColor = ds.borderColor || c1;
      ds.pointBackgroundColor = ds.pointBackgroundColor || c2;
      ds.pointBorderColor = ds.pointBorderColor || '#fff';
      ds.pointRadius = ds.pointRadius ?? 4;
      ds.pointHoverRadius = ds.pointHoverRadius ?? 7;
      ds.borderWidth = ds.borderWidth ?? 3;
      ds.tension = ds.tension ?? 0.4;
      if (ds.fill === undefined) {
        ds.fill = true;
        ds.backgroundColor = (context) => {
          const { chart } = context;
          const { ctx, chartArea } = chart;
          if (!chartArea) return c1 + '22';
          const g = ctx.createLinearGradient(0, chartArea.top, 0, chartArea.bottom);
          g.addColorStop(0, c1 + '55');
          g.addColorStop(1, c1 + '00');
          return g;
        };
      }
    }
    if ((type === 'doughnut' || type === 'pie') && !ds.backgroundColor) {
      ds.backgroundColor = ACM_PALETTE.map(p => p[0] + 'CC');
      ds.borderColor = isDark ? '#1F2937' : '#ffffff';
      ds.borderWidth = 3;
      ds.hoverOffset = 12;
    }
  });

  return new Chart(canvas, {
    type, data: { labels, datasets },
    options: {
      responsive: true, maintainAspectRatio: false,
      animation: { duration: 1400, easing: 'easeOutQuart' },
      interaction: { intersect: false, mode: 'index' },
      plugins: {
        legend: {
          labels: {
            color: textColor, font: { family: "'Rajdhani',sans-serif", size: 12 },
            usePointStyle: true, pointStyle: 'circle', padding: 14
          }
        },
        tooltip: {
          backgroundColor: isDark ? 'rgba(15,23,42,.95)' : 'rgba(255,255,255,.97)',
          titleColor: isDark ? '#F8F5F0' : '#1F2937',
          bodyColor: isDark ? '#9CA3AF' : '#485563',
          borderColor: '#FF6A00', borderWidth: 1,
          padding: 10, cornerRadius: 8,
          titleFont: { family: "'Orbitron',monospace", size: 12 },
          bodyFont: { family: "'Rajdhani',sans-serif", size: 12 },
          displayColors: true,
        },
        ...opts.plugins
      },
      scales: type !== 'pie' && type !== 'doughnut' ? {
        x: { grid: { color: gridColor, drawBorder: false }, ticks: { color: textColor } },
        y: { grid: { color: gridColor, drawBorder: false }, ticks: { color: textColor }, beginAtZero: true },
        ...opts.scales
      } : undefined,
      ...opts
    }
  });
};

/* ── Filter buttons ────────────────────────────────────────────────────── */
document.querySelectorAll('.filter-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    const f = btn.dataset.filter;
    document.querySelectorAll('.product-card,.carousel-item-card').forEach(card => {
      const match = f === 'all' || card.dataset.category === f;
      card.style.opacity = match ? '1' : '.2';
      card.style.transform = match ? '' : 'scale(.95)';
      card.style.pointerEvents = match ? '' : 'none';
    });
  });
});

/* ── Chatbot CSS ──────────────────────────────────────────────────────── */
const chatStyle = document.createElement('style');
chatStyle.textContent = `
.acm-chatbot{position:fixed;bottom:1.8rem;right:1.8rem;z-index:2000}
.acm-chat-toggle{width:54px;height:54px;border-radius:50%;background:linear-gradient(135deg,#FF6A00,#E65C00);border:none;color:#fff;font-size:1.3rem;cursor:pointer;box-shadow:0 6px 20px rgba(255,106,0,.45);display:flex;align-items:center;justify-content:center;transition:transform .3s;position:relative}
.acm-chat-toggle:hover{transform:scale(1.1)}
.chat-pulse{position:absolute;inset:-4px;border-radius:50%;border:2px solid rgba(255,106,0,.4);animation:chatPulse 2s ease-in-out infinite}
@keyframes chatPulse{0%,100%{opacity:.5;transform:scale(1)}50%{opacity:1;transform:scale(1.1)}}
.acm-chat-window{position:absolute;bottom:65px;right:0;width:320px;background:var(--surface,#1F2937);border:1px solid var(--border,rgba(255,106,0,.18));border-radius:16px;overflow:hidden;box-shadow:0 16px 50px rgba(0,0,0,.4);display:none;flex-direction:column}
.acm-chat-window.open{display:flex}
.acm-chat-header{background:linear-gradient(135deg,#FF6A00,#E65C00);padding:.7rem 1rem;display:flex;justify-content:space-between;align-items:center;font-family:'Orbitron',monospace;font-size:.85rem;font-weight:700;color:#fff}
.acm-chat-body{flex:1;padding:1rem;overflow-y:auto;max-height:260px;display:flex;flex-direction:column;gap:.6rem;background:var(--bg,#0F172A)}
.acm-chat-msg{padding:.55rem .85rem;border-radius:10px;font-size:.83rem;max-width:85%;line-height:1.5}
.acm-chat-msg.bot{background:var(--surface,#1F2937);color:var(--text,#F8F5F0);align-self:flex-start;border:1px solid var(--border,rgba(255,106,0,.18))}
.acm-chat-msg.user{background:linear-gradient(135deg,#FF6A00,#E65C00);color:#fff;align-self:flex-end}
.acm-chat-footer{display:flex;border-top:1px solid var(--border,rgba(255,106,0,.18))}
.acm-chat-input{flex:1;padding:.6rem .9rem;background:var(--surface,#1F2937);border:none;color:var(--text,#F8F5F0);font-family:'Rajdhani',sans-serif;font-size:.88rem;outline:none}
.acm-chat-send{padding:.6rem .9rem;background:linear-gradient(135deg,#FF6A00,#E65C00);border:none;color:#fff;cursor:pointer;font-size:.95rem}
.acm-chat-send:hover{opacity:.85}
`;
document.head.appendChild(chatStyle);