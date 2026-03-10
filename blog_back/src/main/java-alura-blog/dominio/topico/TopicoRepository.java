package alura.blog.dominio.topico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TopicoRepository extends JpaRepository<Topico, Long> {

    // Validar duplicados al crear
    boolean existsByTituloAndMensaje(String titulo, String mensaje);

    // Validar duplicados al actualizar (excluyendo el ID actual)
    boolean existsByTituloAndMensajeAndIdNot(
            String titulo,
            String mensaje,
            Long id
    );

    // ESTE MÉTODO DEBE ESTAR en TopicoRepository
    long countByStatus(StatusTopico status);

    // Listar tópicos activos (público - excluye eliminados)
    Page<Topico> findByStatusNot(StatusTopico status, Pageable pageable);

    // Detalle de tópico público (excluye eliminados)
    Optional<Topico> findByIdAndStatusNot(Long id, StatusTopico status);

    @Query("""
    SELECT t FROM Topico t 
    WHERE 
        LOWER(t.titulo) LIKE LOWER(CONCAT('%', :q, '%')) 
        OR LOWER(t.mensaje) LIKE LOWER(CONCAT('%', :q, '%'))
        OR LOWER(t.curso) LIKE LOWER(CONCAT('%', :q, '%'))
        OR LOWER(t.autor.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
        OR YEAR(t.fechaCreacion) = CAST(:q AS int)
        OR MONTH(t.fechaCreacion) = CAST(:q AS int)
        OR DAY(t.fechaCreacion) = CAST(:q AS int)
    AND t.status = 'ABIERTO'
    ORDER BY t.fechaCreacion DESC
""")
    Page<Topico> buscarPorTituloOMensaje(@Param("q") String q, Pageable pageable);

    // ✅ SOLO AGREGAR ESTA LÍNEA al final de tu repository existente:
    @Query("SELECT COUNT(r) FROM Respuesta r WHERE r.topico.id = :topicoId")
    Long countRespuestasByTopicoId(@Param("topicoId") Long topicoId);


}
