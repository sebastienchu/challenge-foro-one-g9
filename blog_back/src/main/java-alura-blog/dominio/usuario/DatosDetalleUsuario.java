package alura.blog.dominio.usuario;

import java.util.Set;
import java.util.stream.Collectors;

public record DatosDetalleUsuario(
        Long id,
        String fullName,
        String email,
        Set<String> roles
) {

    public DatosDetalleUsuario(User user) {
        this(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles()
                        .stream()
                        .map(Role::getName)  // <- CORREGIDO
                        .collect(Collectors.toSet())
        );
    }
}
