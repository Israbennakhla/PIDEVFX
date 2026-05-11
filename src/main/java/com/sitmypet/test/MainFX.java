package com.sitmypet.test;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charge le fichier FXML depuis le dossier 'resources'
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/EvenementView.fxml"));
        Parent root = loader.load();

        // Configuration de la taille de la fenêtre
        Scene scene = new Scene(root, 1280, 720);

        primaryStage.setTitle("PI-DEV : Gestion des Événements");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        // Demande à Java de lancer la fenêtre
        launch(args);
    }
}