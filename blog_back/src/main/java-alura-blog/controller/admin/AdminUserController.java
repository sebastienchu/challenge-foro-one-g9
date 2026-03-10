package alura.blog.controller.admin;

import alura.blog.dominio.usuario.DatosActualizarUsuario;
import alura.blog.dominio.usuario.DatosCrearUsuarioAdmin;
import alura.blog.dominio.usuario.DatosListadoUsuario;
import alura.blog.dominio.usuario.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
//@CrossOrigin(origins = "http://127.0.0.1:5501")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<DatosListadoUsuario>> listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<DatosListadoUsuario> usuarios = userService.listarUsuarios(pageable);

        return ResponseEntity.ok(usuarios);
    }

    @PostMapping
    public ResponseEntity<DatosListadoUsuario> crearUsuario(
            @RequestBody @Valid DatosCrearUsuarioAdmin datos,
            UriComponentsBuilder uriBuilder
    ) {
        DatosListadoUsuario usuarioCreado = userService.crearUsuarioAdmin(datos);

        URI url = uriBuilder.path("/admin/users/{id}")
                .buildAndExpand(usuarioCreado.id())
                .toUri();

        return ResponseEntity.created(url).body(usuarioCreado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatosListadoUsuario> actualizarUsuario(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarUsuario datos
    ) {
        DatosListadoUsuario usuarioActualizado = userService.actualizarUsuario(id, datos);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatosListadoUsuario> obtenerUsuario(@PathVariable Long id) {
        DatosListadoUsuario usuario = userService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        userService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<DatosListadoUsuario> habilitarUsuario(@PathVariable Long id) {
        DatosListadoUsuario usuarioHabilitado = userService.habilitarUsuario(id);
        return ResponseEntity.ok(usuarioHabilitado);
    }

    // 🔥 ESTADÍSTICAS SIMPLES
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Long>> estadisticas() {
        return ResponseEntity.ok(userService.obtenerEstadisticas());
    }
}
