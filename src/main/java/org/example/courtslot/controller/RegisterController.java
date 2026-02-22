package org.example.courtslot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.courtslot.service.UsuarioService;
import org.example.courtslot.util.NavigationUtil;

/**
 * RegisterController — gestiona la pantalla de registro.
 *
 * Mismo patrón que LoginController / HelloController del tutor:
 *   @FXML campos, @FXML métodos de evento, llamada al service.
 *
 * Si el registro tiene éxito → muestra mensaje verde y limpia campos.
 * Si hay error          → muestra mensaje rojo con el motivo.
 */
public class RegisterController {

    @FXML private TextField     nombreField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;
    @FXML private Button        registerBtn;

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);

        // Botón deshabilitado mientras algún campo esté vacío
        registerBtn.disableProperty().bind(
                nombreField.textProperty().isEmpty()
                        .or(emailField.textProperty().isEmpty())
                        .or(passwordField.textProperty().isEmpty())
                        .or(confirmPasswordField.textProperty().isEmpty())
        );
    }

    // ── Botón "Crear cuenta" ──────────────────────────────────────────────────
    @FXML
    protected void onRegistrar() {
        errorLabel.setVisible(false);
        try {
            usuarioService.registrar(
                    nombreField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText(),
                    confirmPasswordField.getText()
            );
            // Registro exitoso — mostramos mensaje verde y limpiamos
            mostrarExito("¡Cuenta creada correctamente! Ahora puedes iniciar sesión.");
            limpiarCampos();

        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    // ── Enlace "¿Ya tienes cuenta? Inicia sesión" ─────────────────────────────
    @FXML
    protected void onLogin() {
        NavigationUtil.navigateTo("login.fxml", emailField);
    }

    // ── Ayudas privadas ───────────────────────────────────────────────────────
    private void mostrarError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #ef4444;"); // rojo
        errorLabel.setVisible(true);
    }

    private void mostrarExito(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #22c55e;"); // verde
        errorLabel.setVisible(true);
    }

    private void limpiarCampos() {
        nombreField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}
