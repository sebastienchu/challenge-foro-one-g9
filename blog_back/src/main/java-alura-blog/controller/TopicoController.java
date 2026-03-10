//package alura.blog.controller;
//
//import alura.blog.dominio.topico.*;
//import alura.blog.dominio.usuario.User;
//import alura.blog.dominio.usuario.UserRepository;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/topicos")
//@RequiredArgsConstructor
//public class TopicoController {
//
//    private final TopicoRepository repository;
//    private final TopicoService topicoService;
//    private final UserRepository userRepository;
//
//    // =========================
//    // REGISTRAR TÓPICO (cualquier usuario autenticado)
//    // =========================
//    @PostMapping
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public ResponseEntity<?> registrar(
//            @AuthenticationPrincipal UserDetails userDetails,
//            @RequestBody @Valid TopicoRequest dto
//    ) {
//        // Buscamos al usuario logueado
//        User autor = userRepository.findByEmail(userDetails.getUsername())
//                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
//
//        Topico topico = new Topico();
//        topico.setTitulo(dto.titulo());
//        topico.setMensaje(dto.mensaje());
//        topico.setCurso(dto.curso());
//        topico.setAutor(autor); // asignamos correctamente el autor
//
//        try {
//            repository.save(topico);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(new TopicoResponse(topico));
//        } catch (DataIntegrityViolationException ex) {
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body("Ya existe un tópico con ese título y mensaje.");
//        }
//    }
//
//
//    // =========================
//    // LISTADO (PAGINADO) - PÚBLICO
//    // =========================
//    @GetMapping
//    public Page<TopicoResponse> listar(
//            @PageableDefault(
//                    size = 10,
//                    sort = "fechaCreacion",
//                    direction = Sort.Direction.ASC
//            ) Pageable pageable
//    ) {
//        return repository.findAll(pageable)
//                .map(TopicoResponse::new);
//    }
//
//    // =========================
//    // LISTAR TÓPICOS DE UN USUARIO
//    // =========================
//    @GetMapping("/usuarios/{id}/topicos")
//    public ResponseEntity<List<Topico>> listarTopicosUsuario(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails logueado) {
//
//        // Obtener la entidad real de usuario
//        User usuario = userRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
//
//        // Determinar si el usuario logueado es admin
//        boolean isAdmin = logueado.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//
//        List<Topico> topicos;
//        if (isAdmin) {
//            topicos = topicoService.listarPorUser(usuario); // admin ve todo
//        } else {
//            topicos = topicoService.listarNoEliminadosPorUsuario(usuario); // user ve solo no eliminados
//        }
//
//        return ResponseEntity.ok(topicos);
//    }
//
//    // =========================
//    // BÚSQUEDA POR CURSO Y AÑO - PÚBLICO
//    // =========================
//    @GetMapping("/buscar")
//    public Page<TopicoResponse> buscar(
//            @RequestParam String curso,
//            @RequestParam int year,
//            Pageable pageable
//    ) {
//        return repository.findByCursoAndYear(curso, year, pageable)
//                .map(TopicoResponse::new);
//    }
//
//    // =========================
//    // DETALLE - PÚBLICO
//    // =========================
//    @GetMapping("/{id}")
//    public ResponseEntity<TopicoResponse> detalle(@PathVariable Long id) {
//        Topico topico = repository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));
//        return ResponseEntity.ok(new TopicoResponse(topico));
//    }
//
//    // =========================
//    // ACTUALIZAR TÓPICO (usuario autenticado)
//    // =========================
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public ResponseEntity<?> actualizar(
//            @PathVariable Long id,
//            @RequestBody @Valid TopicoUpdateRequest dto,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        Optional<Topico> optionalTopico = repository.findById(id);
//
//        if (optionalTopico.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Topico topico = optionalTopico.get();
//
//        // Validar permisos
//        boolean isAdmin = userDetails.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//        if (!topico.getAutor().equals(userDetails.getUsername()) && !isAdmin) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("No tienes permiso para actualizar este tópico");
//        }
//
//        // Actualizar campos permitidos
//        topico.setTitulo(dto.titulo());
//        topico.setMensaje(dto.mensaje());
//        topico.setCurso(dto.curso());
//
//        // Actualizar status según rol
//        if (dto.status() != null) {
//            if (isAdmin) {
//                topico.setStatus(dto.status());
//            } else if (topico.getStatus() == StatusTopico.ABIERTO &&
//                    dto.status() == StatusTopico.CERRADO) {
//                topico.setStatus(StatusTopico.CERRADO);
//            }
//        }
//
//        try {
//            repository.save(topico);
//            return ResponseEntity.ok(new TopicoResponse(topico));
//        } catch (DataIntegrityViolationException e) {
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body("Ya existe un tópico con ese título y mensaje");
//        }
//    }
//
//    // =========================
//    // ELIMINAR TÓPICO (solo ADMIN)
//    // =========================
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public ResponseEntity<?> eliminar(@PathVariable Long id,
//                                      @AuthenticationPrincipal UserDetails userDetails) {
//        Optional<Topico> optionalTopico = repository.findById(id);
//
//        if (optionalTopico.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Topico topico = optionalTopico.get();
//
//        // Solo ADMIN o el autor pueden "eliminar"
//        boolean esAdmin = userDetails.getAuthorities()
//                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//        if (!esAdmin && !topico.getAutor().equals(userDetails.getUsername())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("No tienes permiso para eliminar este tópico");
//        }
//
//        // Soft delete
//        topico.setStatus(StatusTopico.ELIMINADO);
//        repository.save(topico);
//
//        return ResponseEntity.noContent().build(); // 204 No Content
//    }
//}

//
//package alura.blog.controller;
//
//import alura.blog.dominio.topico.*;
//import alura.blog.dominio.usuario.User;
//import alura.blog.dominio.usuario.UserRepository;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/topicos")
//@RequiredArgsConstructor
//public class TopicoController {
//
//    private final TopicoRepository repository;
//    private final TopicoService topicoService;
//    private final UserRepository userRepository;
//
//    // =========================
//    // REGISTRAR TÓPICO (cualquier usuario autenticado)
//    // =========================
/// /    @PostMapping
/// /    @PreAuthorize("hasAnyRole('USER','ADMIN')")
/// /    public ResponseEntity<?> registrar(
/// /            @AuthenticationPrincipal UserDetails userDetails,
/// /            @RequestBody @Valid TopicoRequest dto
/// /    ) {
/// /        System.out.println("DTO recibido: " + dto);
/// /        System.out.println("Usuario logueado: " + userDetails.getUsername());
/// /        User autor = userRepository.findByEmail(userDetails.getUsername())
/// /                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
/// /
/// /        Topico topico = new Topico();
/// /        topico.setTitulo(dto.titulo());
/// /        topico.setMensaje(dto.mensaje());
/// /        topico.setCurso(dto.curso());
/// /        topico.setAutor(autor);
/// /        topico.setFechaCreacion(LocalDateTime.now());
/// /        topico.setStatus(StatusTopico.ABIERTO);
/// /
/// /        try {
/// /            repository.save(topico);
/// /            return ResponseEntity.status(HttpStatus.CREATED)
/// /                    .body(new TopicoResponse(topico));
/// /        } catch (DataIntegrityViolationException ex) {
/// /            return ResponseEntity.status(HttpStatus.CONFLICT)
/// /                    .body("Ya existe un tópico con ese título y mensaje.");
/// /        }
/// /    }
//    @PostMapping
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public ResponseEntity<TopicoResponse> registrar(
//            @AuthenticationPrincipal UserDetails userDetails,
//            @RequestBody @Valid TopicoRequest dto
//    ) {
//        TopicoResponse response = topicoService.registrarTopico(
//                dto,
//                userDetails.getUsername()
//        );
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//
//
//    // =========================
//    // LISTADO (PAGINADO) - PÚBLICO
//    // =========================
//    @GetMapping
//    public Page<TopicoResponse> listar(
//            @PageableDefault(
//                    size = 10,
//                    sort = "fechaCreacion",
//                    direction = Sort.Direction.ASC
//            ) Pageable pageable
//    ) {
//        return repository.findAll(pageable)
//                .map(TopicoResponse::new);
//    }
//
//    // =========================
//    // LISTAR TÓPICOS DE UN USUARIO
//    // =========================
/// /    @GetMapping("/usuarios/{id}/topicos")
/// /    public ResponseEntity<List<Topico>> listarTopicosUsuario(
/// /            @PathVariable Long id,
/// /            @AuthenticationPrincipal UserDetails logueado) {
/// /
/// /        User usuario = userRepository.findById(id)
/// /                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
/// /
/// /        boolean isAdmin = logueado.getAuthorities().stream()
/// /                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
/// /
/// /        List<Topico> topicos;
/// /        if (isAdmin) {
/// /            topicos = topicoService.listarPorUser(usuario);
/// /        } else {
/// /            topicos = topicoService.listarNoEliminadosPorUsuario(usuario);
/// /        }
/// /
/// /        return ResponseEntity.ok(topicos);
/// /    }
/// /
/// /    // =========================
/// /    // BÚSQUEDA POR CURSO Y AÑO - PÚBLICO
/// /    // =========================
/// /    @GetMapping("/buscar")
/// /    public Page<TopicoResponse> buscar(
/// /            @RequestParam String curso,
/// /            @RequestParam int year,
/// /            Pageable pageable
/// /    ) {
/// /        return repository.findByCursoAndYear(curso, year, pageable)
/// /                .map(TopicoResponse::new);
/// /    }
/// /
/// /    // =========================
/// /    // DETALLE - PÚBLICO
/// /    // =========================
/// /    @GetMapping("/{id}")
/// /    public ResponseEntity<TopicoResponse> detalle(@PathVariable Long id) {
/// /        Topico topico = repository.findById(id)
/// /                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));
/// /        return ResponseEntity.ok(new TopicoResponse(topico));
/// /    }
//
//    // =========================
//    // ACTUALIZAR TÓPICO (usuario autenticado)
//    // =========================
/// /    @PutMapping("/{id}")
/// /    @PreAuthorize("hasAnyRole('USER','ADMIN')")
/// /    public ResponseEntity<?> actualizar(
/// /            @PathVariable Long id,
/// /            @RequestBody @Valid TopicoUpdateRequest dto,
/// /            @AuthenticationPrincipal UserDetails userDetails
/// /    ) {
/// /        Topico topico = repository.findById(id)
/// /                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));
/// /
/// /        boolean isAdmin = userDetails.getAuthorities().stream()
/// /                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
/// /
/// /        // Validar permisos
/// /        if (!topico.getAutor().getEmail().equals(userDetails.getUsername()) && !isAdmin) {
/// /            return ResponseEntity.status(HttpStatus.FORBIDDEN)
/// /                    .body("No tienes permiso para actualizar este tópico");
/// /        }
/// /
/// /        // Actualizar campos
/// /        topico.setTitulo(dto.titulo());
/// /        topico.setMensaje(dto.mensaje());
/// /        topico.setCurso(dto.curso());
/// /
/// /        if (dto.status() != null) {
/// /            if (isAdmin) {
/// /                topico.setStatus(dto.status());
/// /            } else if (topico.getStatus() == StatusTopico.ABIERTO &&
/// /                    dto.status() == StatusTopico.CERRADO) {
/// /                topico.setStatus(StatusTopico.CERRADO);
/// /            }
/// /        }
/// /
/// /        try {
/// /            repository.save(topico);
/// /            return ResponseEntity.ok(new TopicoResponse(topico));
/// /        } catch (DataIntegrityViolationException e) {
/// /            return ResponseEntity.status(HttpStatus.CONFLICT)
/// /                    .body("Ya existe un tópico con ese título y mensaje");
/// /        }
/// /    }
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public ResponseEntity<TopicoResponse> actualizar(
//            @PathVariable Long id,
//            @RequestBody @Valid TopicoUpdateRequest dto,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        return ResponseEntity.ok(
//                topicoService.actualizarTopico(id, dto, userDetails)
//        );
//    }
//
//    // =========================
//    // ELIMINAR TÓPICO (solo ADMIN o autor)
//    // =========================
////    @DeleteMapping("/{id}")
////    @PreAuthorize("hasAnyRole('USER','ADMIN')")
////    public ResponseEntity<?> eliminar(@PathVariable Long id,
////                                      @AuthenticationPrincipal UserDetails userDetails) {
////        Topico topico = repository.findById(id)
////                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));
////
////        boolean esAdmin = userDetails.getAuthorities().stream()
////                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
////
////        if (!esAdmin && !topico.getAutor().getEmail().equals(userDetails.getUsername())) {
////            return ResponseEntity.status(HttpStatus.FORBIDDEN)
////                    .body("No tienes permiso para eliminar este tópico");
////        }
////
////        // Soft delete
////        topico.setStatus(StatusTopico.ELIMINADO);
////        repository.save(topico);
////
////        return ResponseEntity.noContent().build();
////    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public ResponseEntity<Void> eliminar(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        boolean isAdmin = userDetails.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//
//        topicoService.eliminarTopico(
//                id,
//                userDetails.getUsername(),
//                isAdmin
//        );
//
//        return ResponseEntity.noContent().build();
//    }
//
//}


package alura.blog.controller;

import alura.blog.dominio.common.PageResponse;
import alura.blog.dominio.topico.*;
import alura.blog.dominio.usuario.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topicos")
@RequiredArgsConstructor
public class TopicoController {

    private final TopicoRepository repository;
    private final TopicoService topicoService;
    private final UserRepository userRepository;

    // =========================
    // REGISTRAR TÓPICO (cualquier usuario autenticado)
    // =========================
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<TopicoResponse> registrar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid TopicoRequest dto
    ) {
        TopicoResponse response = topicoService.registrarTopico(
                dto,
                userDetails.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================
    // ACTUALIZAR TÓPICO (usuario autenticado)
    // =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<TopicoResponse> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid TopicoUpdateRequest dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                topicoService.actualizarTopico(id, dto, userDetails)
        );
    }

    // =========================
    // ELIMINAR TÓPICO (solo ADMIN o autor)
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
    // LISTAR TÓPICOS (PÚBLICO)
    // =========================
    @GetMapping
    public ResponseEntity<PageResponse<TopicoResponse>> listar(
            @PageableDefault(
                    size = 10,
                    sort = "fechaCreacion",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Page<TopicoResponse> page = topicoService.listarTopicos(pageable);
        PageResponse<TopicoResponse> response = new PageResponse<>(page);
        return ResponseEntity.ok(response);
    }

    // =========================
    // LISTAR TÓPICOS (DETALLE PÚBLICO)
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<TopicoResponse> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(topicoService.detalle(id));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<TopicoResponse>> buscar(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TopicoResponse> results = topicoService.buscar(q, pageable);
        return ResponseEntity.ok(new PageResponse<>(results));
    }

    // ✅ AGREGAR ESTOS 2 MÉTODOS
    @PostMapping("/{id}/respuestas")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<RespuestaResponse> responder(
            @PathVariable Long id,
            @RequestBody @Valid RespuestaRequest dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        RespuestaResponse response = topicoService.crearRespuesta(id, dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/respuestas")
    public ResponseEntity<List<RespuestaResponse>> listarRespuestas(@PathVariable Long id) {
        List<RespuestaResponse> respuestas = topicoService.listarRespuestasDeTopico(id);
        return ResponseEntity.ok(respuestas);
    }

    @PostMapping("/{topicoId}/respuestas/{respuestaId}/respuestas")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<RespuestaResponse> responderRespuesta(
            @PathVariable Long topicoId,
            @PathVariable Long respuestaId,
            @RequestBody @Valid RespuestaRequest dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        RespuestaResponse respuestaCreada = topicoService.responderRespuesta(
                respuestaId,
                dto,
                userDetails.getUsername()
        );
        return ResponseEntity.status(201).body(respuestaCreada);
    }

    // ============================================
    // EDITAR RESPUESTA ✅
    // ============================================
    @PutMapping("/{topicoId}/respuestas/{respuestaId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<RespuestaResponse> editarRespuesta(
            @PathVariable Long topicoId,
            @PathVariable Long respuestaId,
            @RequestBody @Valid RespuestaRequest dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        RespuestaResponse respuestaActualizada = topicoService.actualizarRespuesta(
                respuestaId,
                dto,
                userDetails.getUsername()
        );
        return ResponseEntity.ok(respuestaActualizada);
    }

    // ============================================
    // ELIMINAR RESPUESTA ✅
    // ============================================
    @DeleteMapping("/{topicoId}/respuestas/{respuestaId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Void> eliminarRespuesta(
            @PathVariable Long topicoId,
            @PathVariable Long respuestaId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        topicoService.eliminarRespuesta(respuestaId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
