package alura.blog.dominio.topico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicoUpdateRequest(
        @NotBlank String titulo,
        @NotBlank String mensaje,
        @NotBlank String curso,
        StatusTopico status
) {}
