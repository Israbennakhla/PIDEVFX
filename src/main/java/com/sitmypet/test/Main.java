package com.sitmypet.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/sitmypet/fxml/CreerAnnonce.fxml"));

        Parent root = loader.load();

        Scene scene = new Scene(root, 600, 750);

        primaryStage.setTitle("SitMyPet - Créer une annonce");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}