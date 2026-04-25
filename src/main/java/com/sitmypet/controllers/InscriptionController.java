package com.sitmypet.controllers;

import com.sitmypet.model.User;
import com.sitmypet.services.ServiceUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class InscriptionController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> choiceRole;
    @FXML private Label lblErreur;
    @FXML private Label lblPasswordStrength;

    private ServiceUser serviceUser;

    @FXML
    public void initialize() {
        serviceUser = new ServiceUser();
        // Peupler le choix de rôles
        choiceRole.getItems().addAll("GARDIEN", "PROPRIETAIRE");
        choiceRole.getSelectionModel().selectFirst();

        // Évaluation en temps réel de la force du mot de passe
        txtPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            evaluerMotDePasse(newValue);
        });

        // Validation visuelle en temps réel du numéro de téléphone
        txtTelephone.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.trim().isEmpty() && !newV.trim().matches("^\\d{8}$")) {
                txtTelephone.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5px;");
            } else if (!newV.trim().isEmpty() && newV.trim().matches("^\\d{8}$")) {
                txtTelephone.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2px; -fx-border-radius: 5px;");
            } else {
                txtTelephone.setStyle("");
            }
        });
    }

    private void evaluerMotDePasse(String password) {
        if (password.isEmpty()) {
            lblPasswordStrength.setText("");
            return;
        }

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;

        if (score <= 2) {
            lblPasswordStrength.setText("Force : Faible 🔴");
            lblPasswordStrength.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else if (score == 3 || score == 4) {
            lblPasswordStrength.setText("Force : Moyenne 🟡");
            lblPasswordStrength.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            lblPasswordStrength.setText("Force : Forte 🟢");
            lblPasswordStrength.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleInscription(ActionEvent event) {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String tel = txtTelephone.getText().trim();
        String pass = txtPassword.getText();
        String confPass = txtConfirmPassword.getText();
        String role = choiceRole.getValue();

        lblErreur.setVisible(false);

        // 1. Contrôle de saisie global
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || pass.isEmpty() || confPass.isEmpty()) {
            afficherErreur("⚠️ Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // 2. Format Nom / Prénom
        if (nom.length() < 2 || prenom.length() < 2) {
            afficherErreur("⚠️ Le nom et le prénom doivent contenir au moins 2 caractères.");
            return;
        }

        // 3. Validation de l'email correct
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            afficherErreur("⚠️ L'adresse email est invalide.");
            return;
        }

        // 4. Validation du téléphone (obligatoire, 8 chiffres)
        if (tel.isEmpty() || !tel.matches("^\\d{8}$")) {
            afficherErreur("⚠️ Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return;
        }

        // 5. Validation sécuritaire des mots de passe
        if (!pass.equals(confPass)) {
            afficherErreur("⚠️ Les mots de passe ne correspondent pas.");
            return;
        }

        if (pass.length() < 6) {
            afficherErreur("⚠️ Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        // 4. Création objet utilisateur
        User newUser = new User(nom, prenom, email, tel, "", "default.png", role, true);

        // 5. Sauvegarde
        boolean success = serviceUser.inscrire(newUser, pass);
        
        if (success) {
            // Afficher une alerte de succès (optionnel) ou rediriger
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Inscription réussie");
            alert.setHeaderText(null);
            alert.setContentText("Votre compte a bien été créé ! Vous pouvez maintenant vous connecter.");
            alert.showAndWait();

            // Redirection vers le login
            retourLogin(((Node) event.getSource()).getScene().getWindow());
        } else {
            afficherErreur("Erreur lors de la création du compte (l'email existe peut-être déjà).");
        }
    }

    @FXML
    private void handleGoToLogin(javafx.scene.input.MouseEvent event) {
        retourLogin(((Node) event.getSource()).getScene().getWindow());
    }

    private void retourLogin(javafx.stage.Window window) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/Login.fxml"));
            Stage stage = (Stage) window;
            stage.setTitle("SitMyPet - Connexion");
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setScene(new Scene(root, Math.min(bounds.getWidth() * 0.9, 900), Math.min(bounds.getHeight() * 0.9, 600)));
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherErreur(String msg) {
        lblErreur.setText(msg);
        lblErreur.setVisible(true);
    }
}
