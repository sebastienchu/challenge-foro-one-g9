package alura.blog.dominio.usuario;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    long countByEnabledTrue();
    long countByEnabledFalse();

    // ✅ AGREGAR ESTE - Conteo de ADMINS activos
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE u.enabled = true AND r.name = :roleName")
    long countByEnabledTrueAndRolesName(@Param("roleName") String roleName);
}


