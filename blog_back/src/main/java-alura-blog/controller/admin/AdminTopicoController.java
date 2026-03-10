package alura.blog.controller.admin;

import alura.blog.dominio.topico.TopicoRequest;
import alura.blog.dominio.topico.TopicoResponse;
import alura.blog.dominio.topico.TopicoService;
import alura.blog.dominio.topico.TopicoUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin/topicos")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTopicoController {

    private final TopicoService topicoService;

    private static final Set<String> CAMPOS_ORDENABLES =
            Set.of("id", "titulo", "fechaCreacion", "status");

    // =========================
    // LISTAR TODOS LOS TÓPICOS
    // =========================
    @GetMapping
    public ResponseEntity<Page<TopicoResponse>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        if (!CAMPOS_ORDENABLES.contains(sortBy)) {
            return ResponseEntity.badRequest().body(Page.empty());
        }

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("DESC")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(topicoService.listarTopicosAdmin(pageable));
    }

    // =========================
    // OBTENER TÓPICO POR ID
    // =========================
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TopicoResponse> obtenerTopicoAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(topicoService.detalleAdmin(id));
    }

    // =========================
    // CREAR TÓPICO
    // =========================
    @PostMapping
    public ResponseEntity<TopicoResponse> crearTopico(
            @RequestBody @Valid TopicoRequest dto,
            @AuthenticationPrincipal UserDetails userDetails,
            UriComponentsBuilder uriBuilder
    ) {
        TopicoResponse topicoCreado =
                topicoService.registrarTopico(dto, userDetails.getUsername());

        URI uri = uriBuilder
                .path("/admin/topicos/{id}")
                .buildAndExpand(topicoCreado.getId())
                .toUri();

        return ResponseEntity.created(uri).body(topicoCreado);
    }

    // =========================
    // ACTUALIZAR TÓPICO
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<TopicoResponse> actualizarTopico(
            @PathVariable Long id,
            @RequestBody @Valid TopicoUpdateRequest dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                topicoService.actualizarTopico(id, dto, userDetails)
        );
    }

    // =========================
    // ELIMINAR TÓPICO
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        topicoService.eliminarTopico(id, userDetails);
        return ResponseEntity.noContent().build();
    }


    // =========================
    // ESTADÍSTICAS
    // =========================
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Long>> estadisticas() {
        return ResponseEntity.ok(topicoService.obtenerEstadisticas());
    }
}
