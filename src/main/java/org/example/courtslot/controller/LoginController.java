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




public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;


    @FXML

    protected void onRegisterButtonClick() {

        try {

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/register.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();

            stage.setTitle("Registro");

            stage.setScene(new Scene(root, 1200, 800));

            stage.show();



        } catch (IOException e) {

            e.printStackTrace();

        }

    }



    @FXML

    protected void onIniciarSesionButtonClick() {

        try {

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/homepage.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();

            stage.setTitle("Registro");

            stage.setScene(new Scene(root, 1200, 800));

            stage.show();



        } catch (IOException e) {

            e.printStackTrace();

        }

    }



}
