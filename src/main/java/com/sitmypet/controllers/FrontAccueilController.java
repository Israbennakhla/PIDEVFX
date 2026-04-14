package com.sitmypet.controllers;

import com.sitmypet.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class FrontAccueilController {

    @FXML private VBox boxProprietaire;
    @FXML private VBox boxGardien;

    @FXML private Button btnAccueil;
    @FXML private Button btnProfil;
    @FXML private Button btnMesAnimaux;
    @FXML private Button btnMesAnnonces;
    @FXML private Button btnPostulationsRecues;
    @FXML private Button btnRechercherAnnonces;
    @FXML private Button btnMesPostulations;
    @FXML private Button btnMessagerie;
    @FXML private Button btnArticles;
    @FXML private Button btnDeconnexion;

    @FXML private Label lblWelcomeTitle;
    @FXML private Label lblRoleDisplay;

    private User currentUser;

    @FXML
    public void initialize() {
        // État initial (sera redéfini lors de l'appel à setUser)
    }

    public void setUser(User user) {
        this.currentUser = user;

        if (user != null) {
            String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
            
            lblWelcomeTitle.setText("Bienvenue, " + user.getPrenom() + " !");
            
            // Format du rôle ("ROLE_GARDIEN" -> "GARDIEN")
            String displayRole = role.replace("ROLE_", "");
            lblRoleDisplay.setText(displayRole);

            // Gestion de l'affichage selon la matrice d'accès
            if (role.contains("PROPRIETAIRE") || role.contains("PROPRIÉTAIRE")) {
                boxProprietaire.setVisible(true);
                boxProprietaire.setManaged(true);
            } else if (role.contains("GARDIEN")) {
                boxGardien.setVisible(true);
                boxGardien.setManaged(true);
            }
        }
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("SitMyPet - Connexion");
            stage.setScene(new Scene(root, 900, 600));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
