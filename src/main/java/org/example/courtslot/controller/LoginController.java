package org.example.courtslot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.courtslot.model.Usuario;
import org.example.courtslot.service.UsuarioService;
import org.example.courtslot.util.NavigationUtil;
import org.example.courtslot.util.SessionManager;

/**
 * LoginController — gestiona la pantalla de login.
 *
 * El tutor usa el mismo patrón en HelloController:
 *   @FXML anota los campos del FXML
 *   @FXML void onBoton() { ... } maneja los eventos
 *
 * Aquí añadimos:
 *   - Llamada al Service (en vez de lógica directa)
 *   - Navegación con NavigationUtil
 *   - Guardado de sesión con SessionManager
 */
public class LoginController {

    // ── Campos del FXML (deben tener el mismo fx:id) ──────────────────────────
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginBtn;

    private final UsuarioService usuarioService = new UsuarioService();

    // ── initialize() — se llama automáticamente al cargar el FXML ────────────
    // El tutor no siempre lo usa, pero es el equivalente del @PostConstruct
    @FXML
    public void initialize() {
        errorLabel.setVisible(false);

        // El botón de login queda deshabilitado mientras los campos estén vacíos
        // — bind() conecta la propiedad del botón a la condición de los campos
        loginBtn.disableProperty().bind(
                emailField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty())
        );
    }

    // ── Botón "Iniciar sesión" ─────────────────────────────────────────────────
    @FXML
    protected void onIniciarSesion() {
        errorLabel.setVisible(false);
        try {
            // Llamamos al service — si falla lanza IllegalArgumentException
            Usuario usuario = usuarioService.login(
                    emailField.getText().trim(),
                    passwordField.getText()
            );
            // Login correcto: guardamos sesión y navegamos al homepage
            SessionManager.getInstance().setUsuarioActual(usuario);
            NavigationUtil.navigateTo("homepage.fxml", emailField);

        } catch (IllegalArgumentException e) {
            // Mostramos el mensaje de error del service
            mostrarError(e.getMessage());
        }
    }

    // ── Enlace "¿No tienes cuenta? Regístrate" ────────────────────────────────
    @FXML
    protected void onRegister() {
        NavigationUtil.navigateTo("register.fxml", emailField);
    }

    // ── Ayuda privada ─────────────────────────────────────────────────────────
    private void mostrarError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
