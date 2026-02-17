package org.example.courtslot.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.example.courtslot.HelloApplication;

import java.io.IOException;

public class HomepageController {

    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;
    @FXML private Label userNameLabel;
    @FXML private Button adminBtn;

    @FXML
    public void initialize() {
        // Aquí puedes cargar las pistas dinámicamente en cardsContainer
        // y mostrar el nombre del usuario en userNameLabel
    }

    @FXML
    protected void onBuscar() {
        String query = searchField.getText();
        System.out.println("Buscando: " + query);
        // TODO: filtrar cardsContainer según query
    }

    @FXML
    protected void onFiltros() {
        System.out.println("Abriendo filtros...");
        // TODO: abrir panel de filtros
    }

    @FXML
    protected void onFavoritos() {
        System.out.println("Filtrando por favoritos..");
        // TODO: abrir panel de filtros
    }

    @FXML
    protected void onMisReservas() {
        System.out.println("Mis reservas...");
        // TODO: navegar a pantalla de reservas
    }

    @FXML
    protected void onAdminPanel() {
        System.out.println("Panel admin...");
        // TODO: navegar al panel de administración
    }

    @FXML
    protected void onCerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setTitle("Iniciar sesión");
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}