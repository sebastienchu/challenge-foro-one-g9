package alura.blog.dominio.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record DatosActualizarUsuario(
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String fullName,

        @Email(message = "El email debe ser válido")
        String email,

        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        Boolean enabled,

        Set<String> roles
) {

}
