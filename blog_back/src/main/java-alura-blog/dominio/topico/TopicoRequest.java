package alura.blog.dominio.topico;

import jakarta.validation.constraints.NotBlank;

public record TopicoRequest(
        @NotBlank String titulo,
        @NotBlank String mensaje,
        @NotBlank String curso
) {}

