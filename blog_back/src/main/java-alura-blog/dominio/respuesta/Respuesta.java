package alura.blog.dominio.respuesta;

import alura.blog.dominio.topico.Topico;
import alura.blog.dominio.usuario.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "respuestas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topico_id", nullable = false)
    private Topico topico;

    // ✅ USA autor_id (la columna que tiene los datos)
    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private User autor;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    private Boolean solucion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime fechaActualizacion;

    @ManyToOne
    @JoinColumn(name = "padre_id")
    private Respuesta padre;

    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Respuesta> respuestas;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean eliminada = false;  // ✅ AGREGAR ESTO
}
