package alura.blog.dominio.topico;

import alura.blog.dominio.curso.Curso;
import alura.blog.dominio.curso.CursoRepository;
import alura.blog.dominio.respuesta.Respuesta;
import alura.blog.dominio.respuesta.RespuestaRepository;
import alura.blog.dominio.usuario.User;
import alura.blog.dominio.usuario.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TopicoService {

    private final TopicoRepository repository;
    private final RespuestaRepository respuestaRepository;
    private final UserRepository userRepository;
    private final CursoRepository cursoRepository;

    // =========================
    // REGISTRAR TÓPICO
    // =========================
    @Transactional
    public TopicoResponse registrarTopico(TopicoRequest dto, String emailUsuario) {

        User autor = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (repository.existsByTituloAndMensaje(dto.titulo(), dto.mensaje())) {
            throw new DataIntegrityViolationException("Ya existe un tópico con ese título y mensaje");
        }

        Curso curso = cursoRepository.findByNombreVisible(dto.curso())
                .orElseThrow(() -> new EntityNotFoundException("Curso no válido"));

        Topico topico = new Topico();
        topico.setTitulo(dto.titulo());
        topico.setMensaje(dto.mensaje());
        topico.setCurso(curso);
        topico.setAutor(autor);
        topico.setStatus(StatusTopico.ABIERTO);

        repository.save(topico);
        return mapToResponse(topico);
    }

    // =========================
    // CREAR RESPUESTA
    // =========================
    @Transactional
    public RespuestaResponse crearRespuesta(Long topicoId, RespuestaRequest dto, String emailUsuario) {

        Topico topico = repository.findByIdAndStatusNot(topicoId, StatusTopico.ELIMINADO)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        User autor = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Respuesta respuesta = Respuesta.builder()
                .topico(topico)
                .autor(autor)
                .mensaje(dto.mensaje())
                .solucion(Boolean.TRUE.equals(dto.solucion()))
                .eliminada(false)  // ✅ AGREGAR ESTA LÍNEA
                .build();

        respuestaRepository.save(respuesta);
        return new RespuestaResponse(respuesta);
    }

    // =========================
    // LISTAR RESPUESTAS
    // =========================
    @Transactional(readOnly = true)
    public List<RespuestaResponse> listarRespuestasDeTopico(Long topicoId) {
        return respuestaRepository.findByTopicoIdAndNotEliminadaOrderByFechaCreacionAsc(topicoId)
                .stream()
                .map(RespuestaResponse::new)
                .toList();
    }

    // =========================
    // ACTUALIZAR TÓPICO
    // =========================
    @Transactional
    public TopicoResponse actualizarTopico(Long id, TopicoUpdateRequest dto, UserDetails userDetails) {

        Topico topico = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        boolean isAdmin = userDetails.getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !topico.getAutor().getEmail().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("No tienes permiso para actualizar este tópico");
        }

        if (repository.existsByTituloAndMensajeAndIdNot(dto.titulo(), dto.mensaje(), id)) {
            throw new DataIntegrityViolationException("Tópico duplicado");
        }

        topico.setTitulo(dto.titulo());
        topico.setMensaje(dto.mensaje());

        if (dto.curso() != null && !dto.curso().isBlank()) {
            Curso curso = cursoRepository.findByNombreVisible(dto.curso())
                    .orElseThrow(() -> new EntityNotFoundException("Curso no válido"));
            topico.setCurso(curso);
        }

        if (dto.status() != null) {
            if (isAdmin || (topico.getStatus() == StatusTopico.ABIERTO && dto.status() == StatusTopico.CERRADO)) {
                topico.setStatus(dto.status());
            }
        }

        repository.save(topico);
        return mapToResponse(topico);
    }

    // =========================
    // ELIMINAR TÓPICO
    // =========================
    @Transactional
    public void eliminarTopico(Long id, UserDetails userDetails) {

        Topico topico = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        boolean isAdmin = userDetails.getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !topico.getAutor().getEmail().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("No tienes permiso para eliminar este tópico");
        }

        topico.setStatus(StatusTopico.ELIMINADO);
        repository.save(topico);
    }

    // =========================
    // LISTAR TÓPICOS (PUBLICO)
    // =========================
    @Transactional(readOnly = true)
    public Page<TopicoResponse> listarTopicos(Pageable pageable) {

        return repository.findByStatusNot(StatusTopico.ELIMINADO, pageable)
                .map(this::mapToResponse);
    }

    // =========================
    // DETALLE TÓPICO (PÚBLICO - No muestra eliminados)
    // =========================
    @Transactional(readOnly = true)
    public TopicoResponse detalle(Long id) {
        Topico topico = repository.findByIdAndStatusNot(id, StatusTopico.ELIMINADO)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));
        return mapToResponse(topico);
    }

    // =========================
    // DETALLE TÓPICO (ADMIN - Muestra todos)
    // =========================
    @Transactional(readOnly = true)
    public TopicoResponse detalleAdmin(Long id) {
        Topico topico = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));
        return mapToResponse(topico);
    }

    // =========================
    // LISTAR TÓPICOS (ADMIN)
    // =========================
    @Transactional(readOnly = true)
    public Page<TopicoResponse> listarTopicosAdmin(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    // =========================
    // BUSCAR
    // =========================
    @Transactional(readOnly = true)
    public Page<TopicoResponse> buscar(String q, Pageable pageable) {
        return repository.buscarPorTituloOMensaje(q, pageable)
                .map(this::mapToResponse);
    }

    // =========================
    // ESTADÍSTICAS
    // =========================
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerEstadisticas() {
        return Map.of(
                "abiertos", repository.countByStatus(StatusTopico.ABIERTO),
                "cerrados", repository.countByStatus(StatusTopico.CERRADO),
                "eliminados", repository.countByStatus(StatusTopico.ELIMINADO)
        );
    }

    // =========================
    // MAPPER CENTRAL
    // =========================
    private TopicoResponse mapToResponse(Topico topico) {
        return TopicoResponse.builder()
                .id(topico.getId())
                .titulo(topico.getTitulo())
                .mensaje(topico.getMensaje())
                .fechaCreacion(topico.getFechaCreacion())
                .fechaActualizacion(topico.getFechaActualizacion())
                .status(topico.getStatus())
                .autor(topico.getAutor() != null ? topico.getAutor().getFullName() : "Desconocido")
                .autorId(topico.getAutor() != null ? topico.getAutor().getId() : null)
                .curso(topico.getCurso() != null ? topico.getCurso().getNombreVisible() : null)  // ✅ String
//                .cantidadRespuestas(respuestaRepository.countByTopicoId(topico.getId()))
                .cantidadRespuestas(respuestaRepository.countByTopicoIdAndNotEliminada(topico.getId()))
                .build();
    }

    @Transactional
    public RespuestaResponse responderRespuesta(Long respuestaId, RespuestaRequest dto, String emailUsuario) {

        Respuesta respuestaPadre = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        User autor = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Respuesta respuestaAnidada = Respuesta.builder()
                .topico(respuestaPadre.getTopico())
                .autor(autor)
                .mensaje(dto.mensaje())
                .solucion(Boolean.TRUE.equals(dto.solucion()))
                .padre(respuestaPadre)
                .eliminada(false)  // ✅ AGREGAR ESTO
                .build();

        respuestaRepository.save(respuestaAnidada);
        return new RespuestaResponse(respuestaAnidada);
    }

    @Transactional
    public RespuestaResponse actualizarRespuesta(Long respuestaId, RespuestaRequest dto, String emailUsuario) {

        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        User usuario = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // ✅ OBTENER EL ROLE REAL DEL USUARIO
        boolean isAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        // Verificar permisos
        if (!isAdmin && !respuesta.getAutor().getEmail().equals(emailUsuario)) {
            throw new AccessDeniedException("No tienes permiso para editar esta respuesta");
        }

        respuesta.setMensaje(dto.mensaje());
        respuesta.setSolucion(Boolean.TRUE.equals(dto.solucion()));
        respuestaRepository.save(respuesta);

        return new RespuestaResponse(respuesta);
    }

    @Transactional
    public void eliminarRespuesta(Long respuestaId, String emailUsuario) {

        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        User usuario = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Verificar que el usuario sea el autor o admin
        boolean isAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isAdmin && !respuesta.getAutor().getEmail().equals(emailUsuario)) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta respuesta");
        }

        respuesta.setEliminada(true);
        respuestaRepository.save(respuesta);
    }
}


