package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // FXMLLoader charge le fichier FXML depuis le dossier 'resources'
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/EvenementView.fxml"));
        Parent root = loader.load();
        
        // Configuration de la scène
        Scene scene = new Scene(root, 850, 500);
        
        primaryStage.setTitle("PI-DEV : Gestion des Événements");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Lancer l'application JavaFX
        launch(args);
    }
}
