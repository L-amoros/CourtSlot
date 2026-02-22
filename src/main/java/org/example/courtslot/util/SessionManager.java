package org.example.courtslot.util;

import org.example.courtslot.model.Usuario;

public class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioActual;

    // Constructor privado — nadie puede hacer "new SessionManager()"
    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public boolean estaLogueado() {
        return usuarioActual != null;
    }

    public boolean esAdmin() {
        return estaLogueado() && usuarioActual.isAdmin();
    }
}
