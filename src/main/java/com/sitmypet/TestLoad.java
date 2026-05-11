package com.sitmypet;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import java.io.File;
import java.net.URL;

public class TestLoad {
    public static void main(String[] args) throws Exception {
        Platform.startup(() -> {});
        String[] files = {"AdminAnimaux.fxml", "AdminEvenements.fxml", "AdminReclamations.fxml", "AdminAnnonces.fxml"};
        for(String f : files) {
            try {
                URL url = new File("src/main/resources/com/sitmypet/fxml/" + f).toURI().toURL();
                FXMLLoader.load(url);
                System.out.println("SUCCESS: " + f);
            } catch(Exception e) {
                System.out.println("FAILED: " + f);
                e.printStackTrace();
            }
        }
        Platform.exit();
    }
}