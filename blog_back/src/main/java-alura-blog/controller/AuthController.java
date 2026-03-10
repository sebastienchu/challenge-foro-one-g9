package alura.blog.controller;

import alura.blog.dominio.usuario.*;
import alura.blog.infra.security.DatosJWTToken;
import alura.blog.infra.security.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid DatosRegistroUsuario datos) {
        userService.register(datos);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<DatosJWTToken> login(@RequestBody @Valid DatosLoginUsuario datosLoginUsuario) {
        // Autenticar usuario
        var authToken = new UsernamePasswordAuthenticationToken(
                datosLoginUsuario.email(),
                datosLoginUsuario.password()
        );
        authenticationManager.authenticate(authToken);

        // Generar token JWT
        String token = tokenService.generateTokenByEmail(datosLoginUsuario.email());

        return ResponseEntity.ok(new DatosJWTToken(token));
    }


    // =========================
    // USUARIO AUTENTICADO
    // =========================
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserMeResponse> me(Authentication authentication) {

        String email = authentication.getName();

        User user = userService.obtenerPorEmail(email);

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .toList();

        return ResponseEntity.ok(
                new UserMeResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        roles
                )
        );
    }
}
