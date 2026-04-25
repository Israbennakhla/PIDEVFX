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

import com.sitmypet.utils.CaptchaGenerator;
import javafx.scene.image.ImageView;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblErreur;
    @FXML private ImageView imgCaptcha;
    @FXML private TextField txtCaptcha;

    private String currentCaptchaCode;

    private ServiceUser serviceUser;

    @FXML
    public void initialize() {
        serviceUser = new ServiceUser();
        genererNouveauCaptcha();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty() || txtCaptcha.getText().trim().isEmpty()) {
            lblErreur.setText("Veuillez remplir tous les champs, y compris le CAPTCHA.");
            lblErreur.setVisible(true);
            return;
        }

        if (!txtCaptcha.getText().trim().equalsIgnoreCase(currentCaptchaCode)) {
            lblErreur.setText("Le code CAPTCHA est incorrect.");
            lblErreur.setVisible(true);
            genererNouveauCaptcha();
            txtCaptcha.clear();
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
                    javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                    stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
                    stage.setMaximized(true);
                    stage.centerOnScreen();
                    stage.show();
                } else if (role.contains("GARDIEN") || role.contains("PROPRIETAIRE") || role.contains("PROPRIÉTAIRE")) {
                    // Connexion réussie, charger l'interface Client (Front)
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/FrontAccueil.fxml"));
                    Parent root = loader.load();
                    
                    FrontAccueilController controller = loader.getController();
                    controller.setUser(user);
                    
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setTitle("SitMyPet - Espace " + (role.contains("GARDIEN") ? "Gardien" : "Propriétaire"));
                    javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                    stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
                    stage.setMaximized(true);
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

    @FXML
    private void handleGoToInscription(javafx.scene.input.MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/Inscription.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("SitMyPet - Inscription");
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setScene(new Scene(root, Math.min(bounds.getWidth() * 0.9, 1000), Math.min(bounds.getHeight() * 0.9, 700)));
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        com.sitmypet.services.GoogleOAuthService googleService = new com.sitmypet.services.GoogleOAuthService();
        googleService.authenticate(new com.sitmypet.services.GoogleOAuthService.GoogleAuthCallback() {
            @Override
            public void onSuccess(com.sitmypet.services.GoogleOAuthService.GoogleUser googleUser) {
                // On utilise le ServiceUser pour inscrire ou connecter
                User user = serviceUser.authentifierOuInscrireGoogle(
                        googleUser.email, 
                        googleUser.familyName, 
                        googleUser.givenName, 
                        googleUser.picture
                );
                
                if (user != null) {
                    try {
                        String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
                        if (role.contains("ADMIN")) {
                            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/AfficherUser.fxml"));
                            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                            stage.setTitle("SitMyPet - Dashboard Administrateur");
                            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                            stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
                            stage.setMaximized(true);
                            stage.centerOnScreen();
                            stage.show();
                        } else {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/FrontAccueil.fxml"));
                            Parent root = loader.load();
                            com.sitmypet.controllers.FrontAccueilController controller = loader.getController();
                            controller.setUser(user);
                            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                            stage.setTitle("SitMyPet - Espace Propriétaire/Gardien");
                            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                            stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
                            stage.setMaximized(true);
                            stage.centerOnScreen();
                            stage.show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        lblErreur.setText("Erreur lors de l'ouverture du Dashboard.");
                        lblErreur.setVisible(true);
                    }
                } else {
                    lblErreur.setText("Échec de la connexion Google.");
                    lblErreur.setVisible(true);
                }
            }

            @Override
            public void onError(String error) {
                lblErreur.setText(error);
                lblErreur.setVisible(true);
            }
        });
    }

    @FXML
    private void genererNouveauCaptcha() {
        com.sitmypet.utils.CaptchaGenerator.CaptchaResult captcha = com.sitmypet.utils.CaptchaGenerator.generateCaptcha();
        this.currentCaptchaCode = captcha.getCode();
        this.imgCaptcha.setImage(captcha.getImage());
        if (this.txtCaptcha != null) {
            this.txtCaptcha.clear();
        }
    }
}
