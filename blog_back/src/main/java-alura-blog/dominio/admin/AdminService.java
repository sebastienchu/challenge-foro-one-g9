package alura.blog.dominio.admin;

import alura.blog.dominio.curso.Curso;
import alura.blog.dominio.curso.CursoRepository;
import alura.blog.dominio.topico.*;
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

@Service
@RequiredArgsConstructor
public class AdminService {

    private final TopicoRepository repository;
    private final UserRepository userRepository;
    private final CursoRepository cursoRepository;  // ✅ YA LO TENÉS

    // =========================
    // REGISTRAR TÓPICO
    // =========================
    public TopicoResponse registrarTopico(TopicoRequest dto, String emailUsuario) {
        User autor = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (repository.existsByTituloAndMensaje(dto.titulo(), dto.mensaje())) {
            throw new DataIntegrityViolationException("Ya existe un tópico con ese título y mensaje");
        }

        Curso curso = cursoRepository.findByNombreVisible(dto.curso())
                .orElseThrow(() -> new EntityNotFoundException("Curso inválido: " + dto.curso()));

        Topico topico = new Topico();
        topico.setTitulo(dto.titulo());
        topico.setMensaje(dto.mensaje());
        topico.setCurso(curso);  // ✅ OBJETO Curso
        topico.setAutor(autor);

        repository.save(topico);
        return new TopicoResponse(topico);
    }

    // =========================
    // ACTUALIZAR TÓPICO
    // =========================
    public TopicoResponse actualizarTopico(Long id, TopicoUpdateRequest dto, UserDetails userDetails) {
        Topico topico = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!topico.getAutor().getEmail().equals(userDetails.getUsername()) && !isAdmin) {
            throw new AccessDeniedException("No tienes permiso para actualizar este tópico");
        }

        if (repository.existsByTituloAndMensajeAndIdNot(dto.titulo(), dto.mensaje(), id)) {
            throw new DataIntegrityViolationException("Tópico duplicado");
        }

        topico.setTitulo(dto.titulo());
        topico.setMensaje(dto.mensaje());

        if (dto.curso() != null && !dto.curso().isBlank()) {
            Curso curso = cursoRepository.findByNombreVisible(dto.curso())
                    .orElseThrow(() -> new EntityNotFoundException("Curso inválido: " + dto.curso()));
            topico.setCurso(curso);
        }

        if (dto.status() != null) {
            if (isAdmin || (topico.getStatus() == StatusTopico.ABIERTO && dto.status() == StatusTopico.CERRADO)) {
                topico.setStatus(dto.status());
            }
        }

        repository.save(topico);
        return new TopicoResponse(topico);
    }

    // =========================
    // ELIMINAR TÓPICO
    // =========================
    public void eliminarTopico(
            Long id,
            String emailUsuario,
            boolean isAdmin
    ) {
        Topico topico = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        // Validación de permisos
        if (!isAdmin && !topico.getAutor().getEmail().equals(emailUsuario)) {
            throw new AccessDeniedException(
                    "No tienes permiso para eliminar este tópico"
            );
        }

        topico.setStatus(StatusTopico.ELIMINADO);
        repository.save(topico);
    }

    // =========================
    // LISTAR TÓPICOS
    // =========================
    public Page<TopicoResponse> listarTopicos(Pageable pageable) {
        return repository
                .findByStatusNot(StatusTopico.ELIMINADO, pageable)
                .map(TopicoResponse::new);
    }


    // =========================
    // LISTAR TÓPICOS - Detalle público
    // =========================
    public TopicoResponse detalle(Long id) {
        Topico topico = repository
                .findByIdAndStatusNot(id, StatusTopico.ELIMINADO)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        return new TopicoResponse(topico);
    }

    // =========================
    // LISTAR ADMIN TÓPICOS
    // =========================
    public Page<TopicoResponse> listarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(TopicoResponse::new);
    }

}
