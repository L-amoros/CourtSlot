package org.example.courtslot.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.courtslot.HelloApplication;

import java.io.IOException;
public class NavigationUtil {
    public static void navigateTo(String fxmlName, Node anyNode) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("fxml/" + fxmlName)
            );
            Parent root = loader.load();
            Stage stage = (Stage) anyNode.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static <T> T navigateToAndGetController(String fxmlName, Node anyNode) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("fxml/" + fxmlName)
            );
            Parent root = loader.load();
            Stage stage = (Stage) anyNode.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.show();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
