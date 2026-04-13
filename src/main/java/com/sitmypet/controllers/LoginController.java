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
            // Vérifier que c'est bien un Administrateur
            if (user.getRole() != null && user.getRole().contains("ADMIN")) {
                // Connexion réussie, charger le Dashboard
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/AfficherUser.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    
                    stage.setTitle("SitMyPet - Dashboard Administrateur");
                    stage.setScene(new Scene(root, 1300, 750));
                    stage.centerOnScreen();
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                lblErreur.setText("⛔ Accès refusé : L'interface Desktop est strictement\nréservée aux Administrateurs.");
                lblErreur.setVisible(true);
            }
        } else {
            lblErreur.setText("Email non reconnu ou mot de passe invalide.");
            lblErreur.setVisible(true);
        }
    }
}
