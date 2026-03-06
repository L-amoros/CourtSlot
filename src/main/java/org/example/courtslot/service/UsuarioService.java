package org.example.courtslot.service;

import org.example.courtslot.dao.UsuarioDAO;
import org.example.courtslot.model.Usuario;

import java.util.Optional;
import java.util.regex.Pattern;

public class UsuarioService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    // Constructor alternativo para tests (inyección de dependencia)
    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public void registrar(String nombre, String email, String password, String confirmPassword) {

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

        Usuario usuario = new Usuario(nombre, email, password, Usuario.Rol.USER);
        usuarioDAO.save(usuario);
    }

    public Usuario login(String email, String password) {

        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email no puede estar vacío.");

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");

        Optional<Usuario> opt = usuarioDAO.findByEmail(email);

        if (opt.isEmpty() || !opt.get().getPassword().equals(password))
            throw new IllegalArgumentException("Email o contraseña incorrectos.");

        return opt.get();
    }

    public java.util.List<Usuario> getAll() {
        return usuarioDAO.findAll();
    }

    public void delete(Long id) {
        if (id == null)
            throw new IllegalArgumentException("El id del usuario no puede ser nulo.");
        usuarioDAO.delete(id);
    }
}
