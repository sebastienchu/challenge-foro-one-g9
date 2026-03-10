window.initUsers = function () {
  // --------------------
  // HELPER TOAST
  // --------------------
  function showToast(message, type = 'info') {
    const colors = {
      success: 'linear-gradient(to right, #00b09b, #96c93d)',
      error: 'linear-gradient(to right, #ff5f6d, #ffc371)',
      warning: 'linear-gradient(to right, #f7971e, #ffd200)',
      info: 'linear-gradient(to right, #4facfe, #00f2fe)'
    };

    Toastify({
      text: message,
      duration: 3000,
      gravity: 'top',
      position: 'right',
      stopOnFocus: true,
      style: {
        background: colors[type] || colors.info,
      }
    }).showToast();
  }

  // --------------------
  // HELPER CONFIRM
  // --------------------
  async function showConfirm(title, text) {
    const result = await Swal.fire({
      title: title,
      text: text,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, continuar',
      cancelButtonText: 'Cancelar'
    });
    return result.isConfirmed;
  }

  // --------------------
  // AUTH
  // --------------------
  const token = localStorage.getItem('token');
  if (!token) {
    window.location.href = '/login.html';
    return;
  }

  // --------------------
  // ELEMENTOS
  // --------------------
  const userTableBody = document.querySelector('#usersTable tbody');
  const userForm = document.getElementById('userForm');
  const logoutBtn = document.getElementById('logoutBtn');
  const prevBtn = document.getElementById('prevPage');
  const nextBtn = document.getElementById('nextPage');

  if (!userForm || !userTableBody) {
    console.error('Vista de usuarios no cargada');
    return;
  }

  let currentPage = 0;
  const pageSize = 6;

  // --------------------
  // LOGOUT
  // --------------------
  logoutBtn?.addEventListener('click', () => {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
  });

  // --------------------
  // CARGAR USUARIOS (PAGINADO)
  // --------------------
  async function cargarUsuarios() {
    try {
      const res = await fetch(`http://localhost:8081/admin/users?page=${currentPage}&size=${pageSize}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (!res.ok) {
        showToast('Error al cargar usuarios', 'error');
        return;
      }
      
      const pageData = await res.json();
      renderizarUsuarios(pageData.content);
      renderPagination(pageData);
      window.refreshStats();
    } catch (e) {
      console.error(e);
      showToast('Error de conexión al cargar usuarios', 'error');
    }
  }

  // --------------------
  // RENDER TABLA
  // --------------------
  function renderizarUsuarios(usuarios) {
    userTableBody.innerHTML = '';

    usuarios.forEach(u => {
      const row = document.createElement('tr');
      row.innerHTML = `    
      <td>${u.fullName}</td>
      <td>${u.email}</td>
      <td>${u.roles.join(', ')}</td>
      <td>${u.enabled ? 'Sí' : 'No'}</td>
      <td class="actions-cell">
        <button class="edit" onclick="editarUsuario(${u.id})">✏️</button>
        <button class="delete" onclick="toggleUsuario(${u.id}, ${u.enabled})">${u.enabled ? '🗑️' : '✅'}
        </button>
      </td>
    `;

      userTableBody.appendChild(row);
    });
  }

  // --------------------
  // PAGINACIÓN REAL
  // --------------------
  function renderPagination(pageData) {
    prevBtn.disabled = pageData.first;
    nextBtn.disabled = pageData.last;
  }

  window.prevPage = () => {
    if (currentPage > 0) {
      currentPage--;
      cargarUsuarios();
    }
  };

  window.nextPage = () => {
    currentPage++;
    cargarUsuarios();
  };

  // --------------------
  // CREAR / ACTUALIZAR
  // --------------------
  userForm.addEventListener('submit', async e => {
    e.preventDefault();

    const id = document.getElementById('userId').value;
    const fullName = document.getElementById('fullName').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const roles = Array.from(document.getElementById('roles').selectedOptions).map(o => o.value);
    const enabled = document.getElementById('enabled').checked;

    const body = {fullName, email, roles, enabled};
    if (password && password.trim() !== '') {
      body.password = password;
    }

    let url = 'http://localhost:8081/admin/users';
    let method = 'POST';

    if (id) {
      url += `/${id}`;
      method = 'PUT';
    }

    try {
      const res = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(body)
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => null);
        showToast(errorData?.error || errorData?.message || 'Error al guardar usuario', 'error');
        return;
      }
      
      showToast(`Usuario ${id ? 'actualizado' : 'creado'} exitosamente`, 'success');
      userForm.reset();
      document.getElementById('userId').value = '';
      currentPage = 0;
      cargarUsuarios();
      window.refreshStats();
    } catch (e) {
      console.error(e);
      showToast('Error de conexión al guardar usuario', 'error');
    }
  });

  // --------------------
  // EDITAR USUARIO
  // --------------------
  window.editarUsuario = async id => {
    try {
      const res = await fetch(`http://localhost:8081/admin/users/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (!res.ok) {
        showToast('Error al cargar datos del usuario', 'error');
        return;
      }

      const usuario = await res.json();

      document.getElementById('userId').value = usuario.id;
      document.getElementById('fullName').value = usuario.fullName;
      document.getElementById('email').value = usuario.email;
      document.getElementById('password').value = '';
      document.getElementById('enabled').checked = usuario.enabled;

      const rolesSelect = document.getElementById('roles');
      Array.from(rolesSelect.options).forEach(opt => {
        opt.selected = usuario.roles.includes(opt.value);
      });

      userForm.scrollIntoView({ behavior: 'smooth' });
      window.refreshStats();
    } catch (e) {
      console.error(e);
      showToast('Error de conexión al cargar datos del usuario', 'error');
    }
  };

  // --------------------
  // TOGGLE USUARIO (HABILITAR/DESHABILITAR)
  // --------------------
  window.toggleUsuario = async (id, currentStatus) => {
    const nuevoEstado = !currentStatus;
    const accion = nuevoEstado ? 'activar' : 'desactivar';
    
    // Reemplazar confirm() por SweetAlert2
    const confirmed = await showConfirm(
      '¿Estás seguro?',
      `¿Deseas ${accion} este usuario?`
    );
    
    if (!confirmed) return;

    try {
      const res = await fetch(`http://localhost:8081/admin/users/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ enabled: nuevoEstado })
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => null);
        showToast(errorData?.error || 'Error al cambiar el estado del usuario', 'error');
        return;
      }

      showToast(`Usuario ${nuevoEstado ? 'activado' : 'desactivado'} exitosamente`, 'success');
      cargarUsuarios();
      window.refreshStats();
    } catch (e) {
      console.error(e);
      showToast('Error de conexión al cambiar el estado del usuario', 'error');
    }
  };

  // --------------------
  // INIT
  // --------------------
  cargarUsuarios();
  window.refreshStats();
};
