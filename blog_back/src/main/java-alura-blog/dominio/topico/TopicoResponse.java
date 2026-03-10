package alura.blog.dominio.topico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicoResponse {

    private Long id;
    private String titulo;
    private String mensaje;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private StatusTopico status;
    private String autor;
    private Long autorId;
    private String curso;  // ← String para frontend
    private Long cantidadRespuestas;

    public TopicoResponse(Topico topico) {
        this.id = topico.getId();
        this.titulo = topico.getTitulo();
        this.mensaje = topico.getMensaje();
        this.fechaCreacion = topico.getFechaCreacion();
        this.fechaActualizacion = topico.getFechaActualizacion();
        this.status = topico.getStatus();
        this.autor = topico.getAutor() != null
                ? topico.getAutor().getFullName()
                : "Desconocido";
        this.autorId = topico.getAutor() != null
                ? topico.getAutor().getId()
                : null;
        this.curso = topico.getCurso() != null
                ? topico.getCurso().getNombreVisible()
                : "Sin curso";
        this.cantidadRespuestas = 0L;
    }
}
