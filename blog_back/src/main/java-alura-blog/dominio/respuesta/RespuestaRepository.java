package alura.blog.dominio.respuesta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    @Query("SELECT r FROM Respuesta r WHERE r.topico.id = :topicoId AND r.eliminada = false " +
            "ORDER BY r.fechaCreacion ASC")
    List<Respuesta> findByTopicoIdAndNotEliminadaOrderByFechaCreacionAsc(@Param("topicoId") Long topicoId);

    @Query("SELECT COUNT(r) FROM Respuesta r WHERE r.topico.id = :topicoId AND r.eliminada = false")
    Long countByTopicoIdAndNotEliminada(@Param("topicoId") Long topicoId);

}
