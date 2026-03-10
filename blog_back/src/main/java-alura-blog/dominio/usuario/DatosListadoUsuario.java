package alura.blog.dominio.usuario;

import java.util.Set;
import java.util.stream.Collectors;

public record DatosListadoUsuario(
        Long id,
        String fullName,
        String email,
        boolean enabled,
        Set<String> roles
) {
    public DatosListadoUsuario(User user) {
        this(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }
}


