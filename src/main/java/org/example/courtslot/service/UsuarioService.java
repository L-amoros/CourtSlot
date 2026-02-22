package org.example.courtslot.service;

import org.example.courtslot.dao.UsuarioDAO;
import org.example.courtslot.model.Usuario;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * UsuarioService — lógica de negocio para usuarios.
 *
 * El tutor estructura igual sus servicios (MedicoService, EspecialidadService):
 *   - El Service llama al DAO para hablar con la BD
 *   - El Service valida los datos antes de llamar al DAO
 *   - Los controladores llaman al Service, nunca directamente al DAO
 *
 * Si los datos no son válidos, lanza IllegalArgumentException,
 * que el controlador captura y muestra al usuario.
 */
public class UsuarioService {

    // Patrón básico de email
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private final UsuarioDAO usuarioDAO;

    // Constructor normal — crea su propio DAO (igual que el tutor)
    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    // Constructor alternativo para tests (inyección de dependencia)
    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    // ── REGISTRAR ─────────────────────────────────────────────────────────────
    /**
     * Valida los datos y crea el usuario en la BD.
     * @throws IllegalArgumentException si algo no es válido
     */
    public void registrar(String nombre, String email, String password, String confirmPassword) {

        // Validaciones — lanzan excepción si fallan
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío.");

        if (email == null || !EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("El email no tiene un formato válido.");

        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");

        if (!password.equals(confirmPassword))
            throw new IllegalArgumentException("Las contraseñas no coinciden.");

        if (usuarioDAO.existeEmail(email))
            throw new IllegalArgumentException("Ya existe una cuenta con ese email.");

        // Todo correcto — creamos y guardamos el usuario con rol USER
        Usuario usuario = new Usuario(nombre, email, password, Usuario.Rol.USER);
        usuarioDAO.save(usuario);
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────
    /**
     * Autentica al usuario y lo devuelve si las credenciales son correctas.
     * @throws IllegalArgumentException si email o contraseña son incorrectos
     */
    public Usuario login(String email, String password) {

        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email no puede estar vacío.");

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");

        Optional<Usuario> opt = usuarioDAO.findByEmail(email);

        // Si no existe el email O la contraseña no coincide — mismo mensaje de error
        // (por seguridad no decimos cuál de los dos falló)
        if (opt.isEmpty() || !opt.get().getPassword().equals(password))
            throw new IllegalArgumentException("Email o contraseña incorrectos.");

        return opt.get();
    }
}
