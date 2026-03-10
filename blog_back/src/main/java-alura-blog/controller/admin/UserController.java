package alura.blog.controller.admin;

import alura.blog.dominio.usuario.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de usuario protegido por JWT
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    public User getMe(@AuthenticationPrincipal User user) {
        return user;
    }
}
