package alura.blog.dominio.curso;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    public List<CursoResponse> listarCursos() {
        return cursoRepository.findAll()  // ✅ findAll() SIEMPRE existe
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CursoResponse toResponse(Curso curso) {
        return new CursoResponse(curso.getId(), curso.getNombreVisible());
    }
}
