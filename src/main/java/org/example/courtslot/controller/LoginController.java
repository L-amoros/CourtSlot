package org.example.courtslot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.courtslot.model.Usuario;
import org.example.courtslot.service.UsuarioService;
import org.example.courtslot.util.NavigationUtil;
import org.example.courtslot.util.SessionManager;

public class LoginController {

    //Campos del FXML (deben tener el mismo fx:id IMPORTANTE PUTO INUTIL)
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginBtn;

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    //Iniciar sesión
    @FXML
    protected void onIniciarSesion() {
        errorLabel.setVisible(false);

        // Validación manual de campos vacíos
        if (emailField.getText().trim().isEmpty() || passwordField.getText().isEmpty()) {
            mostrarError("Por favor, rellena todos los campos.");
            return;
        }

        try {
            Usuario usuario = usuarioService.login(
                    emailField.getText().trim(),
                    passwordField.getText()
            );
            SessionManager.getInstance().setUsuarioActual(usuario);
            NavigationUtil.navigateTo("homepage.fxml", emailField);

        } catch (IllegalArgumentException e) {
            // Mostramos el mensaje de error del service
            mostrarError(e.getMessage());
        }
    }

    //¿No tienes cuenta? Regístrate
    @FXML
    protected void onRegister() {
        NavigationUtil.navigateTo("register.fxml", emailField);
    }

    //Mensajes de error
    private void mostrarError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}