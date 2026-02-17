package org.example.courtslot.controller;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.courtslot.HelloApplication;
import java.io.IOException;

public class RegisterController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;


    @FXML

    protected void onLoginButtonClick() {

        try {

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/login.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();

            stage.setTitle("Iniciar sesiÃ³n");

            stage.setScene(new Scene(root, 1200, 800));

            stage.show();



        } catch (IOException e) {

            e.printStackTrace();

        }

    }

}


