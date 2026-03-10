package alura.blog.dominio.topico;

import alura.blog.dominio.respuesta.Respuesta;

import java.time.LocalDateTime;

public record RespuestaResponse(
        Long id,
        String mensaje,
        LocalDateTime fechaCreacion,
        Boolean solucion,
        String autor,
        Long autorId,
        Long padreId  // ← NUEVO para hilos
) {
    public RespuestaResponse(Respuesta r) {
        this(
                r.getId(),
                r.getMensaje(),
                r.getFechaCreacion(),
                r.getSolucion(),
                r.getAutor().getFullName(),
                r.getAutor().getId(),
                r.getPadre() != null ? r.getPadre().getId() : null  // ← NUEVO
        );
    }
}
