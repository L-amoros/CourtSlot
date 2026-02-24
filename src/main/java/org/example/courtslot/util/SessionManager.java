package org.example.courtslot.util;

import org.example.courtslot.model.Usuario;

/**
 * SessionManager — guarda quién está logueado en este momento.
 *
 * Patrón Singleton: una única instancia en toda la aplicación.
 *
 * USO:
 *   SessionManager.getInstance().setUsuarioActual(usuario); // al hacer login
 *   SessionManager.getInstance().getUsuarioActual();        // en cualquier pantalla
 *   SessionManager.getInstance().cerrarSesion();            // al salir
 */
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
