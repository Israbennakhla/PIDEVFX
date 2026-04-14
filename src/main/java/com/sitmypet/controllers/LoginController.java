package com.sitmypet.controllers;

import com.sitmypet.model.User;
import com.sitmypet.services.ServiceUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblErreur;

    private ServiceUser serviceUser;

    @FXML
    public void initialize() {
        serviceUser = new ServiceUser();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            lblErreur.setText("Veuillez remplir tous les champs.");
            lblErreur.setVisible(true);
            return;
        }

        User user = serviceUser.authentifier(email, password);

        if (user != null) {
            String role = user.getRole() != null ? user.getRole().toUpperCase() : "";

            try {
                if (role.contains("ADMIN")) {
                    // Connexion réussie, charger le Dashboard
                    Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/AfficherUser.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    
                    stage.setTitle("SitMyPet - Dashboard Administrateur");
                    stage.setScene(new Scene(root, 1300, 750));
                    stage.centerOnScreen();
                    stage.show();
                } else if (role.contains("GARDIEN") || role.contains("PROPRIETAIRE") || role.contains("PROPRIÉTAIRE")) {
                    // Connexion réussie, charger l'interface Client (Front)
                    Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/FrontAccueil.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    
                    stage.setTitle("SitMyPet - Espace Client");
                    stage.setScene(new Scene(root, 800, 600));
                    stage.centerOnScreen();
                    stage.show();
                } else {
                    lblErreur.setText("⛔ Accès refusé : Rôle non reconnu.");
                    lblErreur.setVisible(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            lblErreur.setText("Email non reconnu ou mot de passe invalide.");
            lblErreur.setVisible(true);
        }
    }
}
