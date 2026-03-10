package alura.blog.dominio.usuario;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Servicio para registrar usuarios
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public void register(DatosRegistroUsuario datos) {
        User user = new User();
        user.setFullName(datos.fullName());
        user.setEmail(datos.email());
        user.setPassword(passwordEncoder.encode(datos.password()));
        user.setRoles(new HashSet<>());

        // asignar rol USER por defecto
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER no encontrado"));
        user.getRoles().add(userRole);

        userRepository.save(user);
    }

    @Transactional
    public DatosListadoUsuario crearUsuarioAdmin(DatosCrearUsuarioAdmin datos) {
        // Validar que el email no esté en uso
        if (userRepository.findByEmail(datos.email()).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        User user = new User();
        user.setFullName(datos.fullName());
        user.setEmail(datos.email());
        user.setPassword(passwordEncoder.encode(datos.password()));
        user.setEnabled(true);

        // Asignar roles
        Set<Role> roles = new HashSet<>();
        if (datos.roles() == null || datos.roles().isEmpty()) {
            // Si no se especifican roles, asignar USER por defecto
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Role USER no encontrado"));
            roles.add(userRole);
        } else {
            // Asignar los roles especificados
            for (String roleName : datos.roles()) {
                Role role = roleRepository.findByName(roleName.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Role " + roleName + " no encontrado"));
                roles.add(role);
            }
        }
        user.setRoles(roles);

        User usuarioGuardado = userRepository.save(user);
        return new DatosListadoUsuario(usuarioGuardado);
    }

    public User obtenerPorEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("Usuario no encontrado"));
    }

    public Page<DatosListadoUsuario> listarUsuarios(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(DatosListadoUsuario::new);
    }

    @Transactional
    public DatosListadoUsuario actualizarUsuario(Long id, DatosActualizarUsuario datos) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        // Actualización parcial: solo actualiza los campos no nulos
        if (datos.fullName() != null && !datos.fullName().isBlank()) {
            user.setFullName(datos.fullName());
        }

        if (datos.email() != null && !datos.email().isBlank()) {
            // Verificar que el email no esté en uso por otro usuario
            userRepository.findByEmail(datos.email()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new IllegalArgumentException("El email ya está registrado por otro usuario");
                }
            });
            user.setEmail(datos.email());
        }

        if (datos.password() != null && !datos.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(datos.password()));
        }

        if (datos.enabled() != null) {
            user.setEnabled(datos.enabled());
        }

        if (datos.roles() != null && !datos.roles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : datos.roles()) {
                Role role = roleRepository.findByName(roleName.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Role " + roleName + " no encontrado"));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        User usuarioActualizado = userRepository.save(user);
        return new DatosListadoUsuario(usuarioActualizado);
    }

    public DatosListadoUsuario obtenerUsuarioPorId(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        return new DatosListadoUsuario(user);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        // Verificar que el usuario existe
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        // Verificar que no esté ya deshabilitado
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("El usuario ya está deshabilitado");
        }

        // Prevenir que el admin se elimine a sí mismo
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailActual = authentication.getName();

        if (user.getEmail().equals(emailActual)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta de administrador");
        }

        user.setEnabled(false);
        userRepository.save(user);
    }

    // AGREGAR ESTE MÉTODO a tu UserService (después de eliminarUsuario)
    @Transactional
    public DatosListadoUsuario habilitarUsuario(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("El usuario ya está habilitado");
        }

        user.setEnabled(true);
        User usuarioHabilitado = userRepository.save(user);
        return new DatosListadoUsuario(usuarioHabilitado);
    }


    // Método adicional para restaurar/habilitar usuario
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerEstadisticas() {
        long activos = userRepository.countByEnabledTrue();
        long inactivos = userRepository.countByEnabledFalse();
        long administradores = userRepository.countByEnabledTrueAndRolesName("ADMIN");

        return Map.of(
                "activos", activos,
                "inactivos", inactivos,
                "administradores", administradores
        );
    }
}
