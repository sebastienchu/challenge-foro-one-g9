document.addEventListener('DOMContentLoaded', async () => {
  // =============================
  // VARIABLES GLOBALES
  // =============================
  const tokenKey = 'token';
  const token = localStorage.getItem(tokenKey);
  const root = document.documentElement;
  const themeBtn = document.getElementById('themeToggle');
  const logoutBtn = document.getElementById('logoutBtn');
  const currentPage = window.location.pathname.split('/').pop();
  const protectedPages = ['index.html', 'blog.html', 'post.html', 'admin/index.html'];
  const SESSION_TIMEOUT = 15 * 60 * 1000; // 15 min
  let sessionExpirationTime;
  let tokenCheckInterval;
  let inactivityCheckInterval;
  let currentAbortController = null;

  // =============================
  // HELPER FUNCTIONS
  // =============================
  function getElementSafe(id) {
    const element = document.getElementById(id);
    if (!element) console.warn(`Element #${id} not found`);
    return element || {textContent: ''};
  }

  // =============================
  // TEMA
  // =============================
  const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  let theme = localStorage.getItem('theme') || (systemDark ? 'dark' : 'light');
  applyTheme(theme);

  themeBtn?.addEventListener('click', () => {
    theme = theme === 'dark' ? 'light' : 'dark';
    localStorage.setItem('theme', theme);
    applyTheme(theme);
  });

  function applyTheme(theme) {
    root.setAttribute('data-theme', theme);
    themeBtn && (themeBtn.textContent = theme === 'dark' ? '☀️' : '🌙');
  }

  // =============================
  // FUNCIÓN LOGOUT
  // =============================
  function logout(msg) {
    if (tokenCheckInterval) clearInterval(tokenCheckInterval);
    if (inactivityCheckInterval) clearInterval(inactivityCheckInterval);
    alert(msg || 'Tu sesión ha expirado.');
    localStorage.removeItem(tokenKey);
    window.location.href = '/login.html';
  }

  // =============================
  // TOKEN Y REDIRECCIÓN
  // =============================
  function isTokenExpired(token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Math.floor(Date.now() / 1000);
      return payload.exp < now;
    } catch {
      return true;
    }
  }

  // Verificar token al cargar
  if (!token && protectedPages.includes(currentPage)) {
    window.location.href = '/login.html';
    return;
  }
  if (token && isTokenExpired(token)) {
    logout('Tu sesión ha expirado.');
    return;
  }
  if (token && (currentPage === 'login.html' || currentPage === 'register.html')) {
    window.location.href = '/index.html';
    return;
  }

  // =============================
  // BOTONES LOGIN / LOGOUT
  // =============================
  const loginLink = document.getElementById('loginLink');
  const registerLink = document.getElementById('registerLink');

  if (token) {
    loginLink && (loginLink.style.display = 'none');
    registerLink && (registerLink.style.display = 'none');
    logoutBtn && (logoutBtn.style.display = 'inline-block');
    logoutBtn && (logoutBtn.textContent = 'Cerrar sesión');
  } else {
    logoutBtn && (logoutBtn.style.display = 'none');
    loginLink && (loginLink.style.display = 'inline-block');
    registerLink && (registerLink.style.display = 'inline-block');
  }

  logoutBtn?.addEventListener('click', () => {
    logout('Has cerrado sesión.');
  });

  // =============================
  // NAVEGACIÓN MEJORADA
  // =============================
  const navButtons = document.querySelectorAll('.nav-btn[data-action]');
  const content = document.getElementById('adminContent');

  function setActiveNav(action) {
    navButtons.forEach(btn => btn.classList.remove('active'));
    document.querySelector(`[data-action="${action}"]`)?.classList.add('active');
  }

  function handleNavAction(action) {
    setActiveNav(action);

    switch (action) {
      case 'home':
        window.location.href = '../index.html';
        break;
      case 'dashboard':
        if (content) content.style.display = 'none';
        break;
      case 'users':
      case 'topicos':
        if (content) content.style.display = 'block';
        const view = action;
        loadView(view);
        break;
    }
  }

  // Event listeners para navegación
  navButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      handleNavAction(btn.dataset.action);
    });
  });

  // =============================
  // CARGA DINÁMICA DE VISTAS
  // =============================
  async function loadView(view) {
    if (content) {
      content.style.display = 'block';
    }

    if (currentAbortController) {
      currentAbortController.abort();
    }

    currentAbortController = new AbortController();
    const {signal} = currentAbortController;

    try {
      content.innerHTML = '<div class="loading">Cargando...</div>';

      const res = await fetch(`./partials/${view}.html`, {signal});
      if (!res.ok) throw new Error('Vista no encontrada');

      content.innerHTML = await res.text();

      const oldScript = document.getElementById('adminViewScript');
      oldScript?.remove();

      const script = document.createElement('script');
      script.src = `./js/${view}.js`;
      script.id = 'adminViewScript';
      document.body.appendChild(script);

      script.onload = () => {
        const initFn = window[`init${view.charAt(0).toUpperCase() + view.slice(1)}`];
        if (typeof initFn === 'function') initFn();
      };
    } catch (e) {
      if (e.name !== 'AbortError') {
        console.error(e);
        content.innerHTML = '<p class="error">Error al cargar la vista</p>';
      }
    }
  }

  // =============================
  // DASHBOARD GENERAL - FUNCIÓN GLOBAL ✅
  // =============================
  async function cargarStatsGenerales() {
    console.log('📊 Dashboard general...');

    // TÓPICOS
    try {
      const resTopicos = await fetch('http://localhost:8081/admin/topicos/estadisticas', {
        headers: {Authorization: `Bearer ${token}`}
      });
      if (resTopicos.ok) {
        const data = await resTopicos.json();
        getElementSafe('abiertos-count').textContent = data.abiertos || 0;
        getElementSafe('cerrados-count').textContent = data.cerrados || 0;
        getElementSafe('eliminados-count').textContent = data.eliminados || 0;
        console.log('✅ Tópicos:', data);
      }
    } catch (e) {
      console.error('❌ Tópicos:', e);
    }

    // USUARIOS
    try {
      const resUsuarios = await fetch('http://localhost:8081/admin/users/estadisticas', {
        headers: {Authorization: `Bearer ${token}`}
      });
      if (resUsuarios.ok) {
        const data = await resUsuarios.json();
        getElementSafe('usuarios-activos-count').textContent = data.activos || 0;
        getElementSafe('usuarios-inactivos-count').textContent = data.inactivos || 0;
        getElementSafe('usuarios-admin-count').textContent = data.administradores || 0;
        console.log('✅ Usuarios:', data);
      }
    } catch (e) {
      console.warn('❌ Usuarios:', e);
    }
  }

  // =============================
  // 🔥 AUTO-REFRESH GLOBAL - PARA USERS Y TOPICOS
  // =============================
  window.cargarStatsGenerales = cargarStatsGenerales;
  window.refreshStats = async () => {
    console.log('🔄 Refrescando estadísticas automáticamente...');
    try {
      await cargarStatsGenerales();
      console.log('✅ Estadísticas actualizadas');
    } catch (e) {
      console.error('❌ Error refrescando stats:', e);
    }
  };

  // =============================
  // INICIALIZACIÓN
  // =============================
  try {
    await cargarStatsGenerales();
    setActiveNav('dashboard');
    if (content) content.style.display = 'none';
  } catch (e) {
    console.error('Error en inicialización:', e);
  }

  // =============================
  // TIMERS
  // =============================
  function resetInactivityTimer() {
    sessionExpirationTime = Date.now() + SESSION_TIMEOUT;
  }

  function checkSessionTimeout() {
    if (Date.now() >= sessionExpirationTime) {
      logout('Sesión cerrada por inactividad.');
    }
  }

  resetInactivityTimer();
  inactivityCheckInterval = setInterval(checkSessionTimeout, 1000);

  ['mousemove', 'keydown', 'click', 'scroll'].forEach(evt =>
    document.addEventListener(evt, resetInactivityTimer, {passive: true})
  );

  tokenCheckInterval = setInterval(() => {
    const currentToken = localStorage.getItem(tokenKey);
    if (!currentToken || isTokenExpired(currentToken)) {
      logout('Tu sesión ha expirado.');
    }
  }, 15000);

  window.addEventListener('beforeunload', () => {
    if (tokenCheckInterval) clearInterval(tokenCheckInterval);
    if (inactivityCheckInterval) clearInterval(inactivityCheckInterval);
    if (currentAbortController) currentAbortController.abort();
  });
});
