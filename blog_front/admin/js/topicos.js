function initTopicos() {
  // --------------------
  // HELPER TOAST ✅ SIN refreshStats()
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
        background: colors[type] || colors.info
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
    location.href = '../login.html';
    return;
  }

  // --------------------
  // ELEMENTOS
  // --------------------
  const tableBody = document.querySelector('#postsTable tbody');
  const topicoForm = document.getElementById('topicoForm');
  const prevBtn = document.getElementById('prevPage');
  const nextBtn = document.getElementById('nextPage');
  const modal = document.getElementById('topicoModal');
  const newTopicoBtn = document.getElementById('newTopicoBtn');
  const closeModal = document.getElementById('closeModal');
  const cancelModal = document.getElementById('cancelModal');

  if (!tableBody) {
    console.error('❌ No existe #postsTable');
    return;
  }

  let currentPage = 0;
  const pageSize = 6;

  // --------------------
  // CARGAR TÓPICOS
  // --------------------
  async function cargarTopicos() {
    try {
      const res = await fetch(`http://localhost:8081/admin/topicos?page=${currentPage}&size=${pageSize}`, {
        headers: {Authorization: `Bearer ${token}`}
      });

      if (!res.ok) {
        showToast('Error al cargar tópicos', 'error');
        return;
      }

      const page = await res.json();
      renderTopicos(page.content || []);

      prevBtn.disabled = page.first;
      nextBtn.disabled = page.last;
    } catch (e) {
      console.error(e);
      showToast('Error de conexión al cargar tópicos', 'error');
    }
  }

  // --------------------
  // RENDER TABLA
  // --------------------
  function renderTopicos(topicos) {
    tableBody.innerHTML = '';

    if (!topicos.length) {
      tableBody.innerHTML = `<tr><td colspan="8" style="text-align: center;">No hay tópicos</td></tr>`;
      return;
    }

    topicos.forEach(t => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${t.titulo}</td>
        <td>${t.mensaje.length > 50 ? t.mensaje.substring(0, 50) + '...' : t.mensaje}</td>
        <td>${t.autor}</td>
        <td>${t.curso}</td>
        <td>${new Date(t.fechaCreacion).toLocaleString()}</td>
        <td>${new Date(t.fechaActualizacion).toLocaleString()}</td>
        <td><span class="status-badge status-${t.status.toLowerCase()}">${t.status}</span></td>
        <td class="actions-cell">
          <button class="edit" onclick="editarTopico(${t.id})">✏️</button>
        </td>
      `;
      tableBody.appendChild(tr);
    });
  }

  // --------------------
  // MODAL LOGIC ✅
  // --------------------
  newTopicoBtn?.addEventListener('click', () => {
    document.getElementById('topicoForm').reset();
    document.getElementById('topicoId').value = '';
    document.getElementById('modalTitle').textContent = 'Nuevo Tópico';
    modal.style.display = 'block';
  });

  closeModal?.addEventListener('click', () => modal.style.display = 'none');
  cancelModal?.addEventListener('click', () => modal.style.display = 'none');
  
  window.addEventListener('click', (e) => {
    if (e.target === modal) modal.style.display = 'none';
  });

  // --------------------
  // CREAR / ACTUALIZAR TÓPICO ✅ MODAL + AUTO-REFRESH
  // --------------------
  if (topicoForm) {
    topicoForm.addEventListener('submit', async e => {
      e.preventDefault();

      const id = document.getElementById('topicoId').value;
      const titulo = document.getElementById('titulo').value;
      const mensaje = document.getElementById('mensaje').value;
      const curso = document.getElementById('curso').value;
      const status = document.getElementById('status').value;

      const body = {titulo, mensaje, curso, status};
      let url = 'http://localhost:8081/admin/topicos';
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
          showToast(errorData?.error || errorData?.message || 'Error al guardar tópico', 'error');
          return;
        }

        showToast(`Tópico ${id ? 'actualizado' : 'creado'} exitosamente`, 'success');
        modal.style.display = 'none';  // ✅ CERRAR MODAL
        topicoForm.reset();
        document.getElementById('topicoId').value = '';
        currentPage = 0;
        cargarTopicos();
        
        // ✅ AUTO-REFRESH DASHBOARD
        if (window.refreshStats) window.refreshStats();
        
      } catch (e) {
        console.error(e);
        showToast('Error de conexión al guardar tópico', 'error');
      }
    });
  }

  // --------------------
  // EDITAR TÓPICO ✅ ABRE MODAL
  // --------------------
  window.editarTopico = async id => {
    try {
      const res = await fetch(`http://localhost:8081/admin/topicos/${id}`, {
        headers: {Authorization: `Bearer ${token}`}
      });

      if (!res.ok) {
        showToast('Error al cargar datos del tópico', 'error');
        return;
      }

      const topico = await res.json();

      // ✅ LLENAR FORM DEL MODAL
      document.getElementById('topicoId').value = topico.id;
      document.getElementById('titulo').value = topico.titulo;
      document.getElementById('mensaje').value = topico.mensaje;
      document.getElementById('curso').value = topico.curso;
      document.getElementById('status').value = topico.status;
      
      // ✅ ABRIR MODAL
      document.getElementById('modalTitle').textContent = 'Editar Tópico';
      modal.style.display = 'block';
      
    } catch (e) {
      console.error(e);
      showToast('Error de conexión al cargar datos del tópico', 'error');
    }
  };

  // --------------------
  // PAGINACIÓN
  // --------------------
  window.prevPage = () => {
    if (currentPage > 0) {
      currentPage--;
      cargarTopicos();
    }
  };

  window.nextPage = () => {
    currentPage++;
    cargarTopicos();
  };

  // --------------------
  // INIT
  // --------------------
  cargarTopicos();
}

window.initTopicos = initTopicos;
