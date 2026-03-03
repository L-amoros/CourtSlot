package org.example.courtslot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.courtslot.service.UsuarioService;
import org.example.courtslot.util.NavigationUtil;

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
    }

    @FXML
    protected void onRegistrar() {
        errorLabel.setVisible(false);

        // Validación manual de campos vacíos
        if (nombreField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                passwordField.getText().isEmpty() ||
                confirmPasswordField.getText().isEmpty()) {
            mostrarError("Por favor, rellena todos los campos.");
            return;
        }

        try {
            usuarioService.registrar(
                    nombreField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText(),
                    confirmPasswordField.getText()
            );
            mostrarExito("¡Cuenta creada correctamente! Ahora puedes iniciar sesión.");
            limpiarCampos();

        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    //¿Ya tienes cuenta? Inicia sesión
    @FXML
    protected void onLogin() {
        NavigationUtil.navigateTo("login.fxml", emailField);
    }

    //Mensajes de error
    private void mostrarError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #ef4444;");
        errorLabel.setVisible(true);
    }

    private void mostrarExito(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #22c55e;");
        errorLabel.setVisible(true);
    }

    private void limpiarCampos() {
        nombreField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}