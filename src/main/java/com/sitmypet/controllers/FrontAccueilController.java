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
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class FrontAccueilController {

    @FXML private javafx.scene.layout.HBox boxProprietaire;
    @FXML private javafx.scene.layout.HBox boxGardien;

    @FXML private BorderPane mainContainer;
    @FXML private VBox accueilContent;

    @FXML private Button btnAccueil;
    @FXML private Button btnProfil;
    @FXML private Button btnMesAnimaux;
    @FXML private Button btnMesAnnonces;
    @FXML private Button btnPostulationsRecues;
    @FXML private Button btnRechercherAnnonces;
    @FXML private Button btnMesPostulations;
    @FXML private Button btnMessagerie;
    @FXML private Button btnEvenements;
    @FXML private Button btnReclamations;
    @FXML private Button btnArticles;
    @FXML private Button btnDeconnexion;

    @FXML private Label lblWelcomeTitle;
    @FXML private Label lblRoleDisplay;

    private User currentUser;

    @FXML
    public void initialize() {
        // Appliquer le thème s'il y en a un de défini
        // (La racine n'est pas encore disponible, on le fera ailleurs ou via un listener)
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

    @FXML
    private void handleShowAccueil(ActionEvent event) {
        if (mainContainer != null && accueilContent != null) {
            mainContainer.setCenter(accueilContent);
            updateNavStyles(btnAccueil);
        }
    }

    @FXML
    private void handleShowProfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/UserProfile.fxml"));
            Node profileView = loader.load();
            
            // On peut passer l'utilisateur actuel au contrôleur du profil s'il existe
            Object controller = loader.getController();
            if (controller instanceof com.sitmypet.controllers.UserProfileController) {
                ((com.sitmypet.controllers.UserProfileController) controller).setUser(currentUser, mainContainer);
            }
            
            mainContainer.setCenter(profileView);
            updateNavStyles(btnProfil);
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback en cas d'erreur
            System.err.println("Erreur lors du chargement du profil: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowAnimaux(ActionEvent event) {
        System.out.println("Afficher les animaux");
        loadCenterView("/com/sitmypet/fxml/FrontAnimaux.fxml");
        updateNavStyles(btnMesAnimaux);
    }

    @FXML
    private void handleShowEvenements(ActionEvent event) {
        System.out.println("Afficher les événements");
        loadCenterView("/com/sitmypet/fxml/FrontEvenements.fxml");
        updateNavStyles(btnEvenements);
    }

    @FXML
    private void handleShowReclamations(ActionEvent event) {
        System.out.println("Afficher les réclamations");
        loadCenterView("/com/sitmypet/fxml/FrontReclamations.fxml");
        updateNavStyles(btnReclamations);
    }

    private void loadCenterView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof FrontReclamationsController) {
                ((FrontReclamationsController) controller).setUser(currentUser);
            }
            
            mainContainer.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue: " + fxmlPath);
            showAlert("Erreur", "Impossible de charger la vue demandée.");
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateNavStyles(Button activeBtn) {
        String activeStyle = "-fx-background-color: transparent; -fx-text-fill: #8e5bd6; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: transparent transparent #8e5bd6 transparent; -fx-border-width: 0 0 3 0; -fx-padding: 5 10;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #4a5568; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 5 10;";

        Button[] buttons = {btnAccueil, btnProfil, btnMesAnimaux, btnMesAnnonces, btnPostulationsRecues, 
                            btnRechercherAnnonces, btnMesPostulations, btnMessagerie, btnEvenements, 
                            btnReclamations, btnArticles};

        for (Button btn : buttons) {
            if (btn != null) {
                btn.setStyle(inactiveStyle);
            }
        }

        if (activeBtn != null) {
            activeBtn.setStyle(activeStyle);
        }
    }
}
