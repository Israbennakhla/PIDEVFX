package com.sitmypet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger la vue de Connexion d'abord
        Parent root = FXMLLoader.load(java.util.Objects.requireNonNull(getClass().getResource("/com/sitmypet/fxml/Login.fxml")));

        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, Math.min(bounds.getWidth() * 0.9, 1200), Math.min(bounds.getHeight() * 0.9, 800));

        primaryStage.setTitle("SitMyPet - Connexion");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
