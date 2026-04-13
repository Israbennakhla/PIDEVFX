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

        Scene scene = new Scene(root, 900, 600);

        primaryStage.setTitle("SitMyPet - Connexion");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Le login a une taille fixe
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
