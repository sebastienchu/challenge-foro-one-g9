package alura.blog.dominio.topico;

import jakarta.validation.constraints.NotBlank;

public record RespuestaRequest(
        @NotBlank(message = "Mensaje es obligatorio")
        String mensaje,
        Boolean solucion
) {}
