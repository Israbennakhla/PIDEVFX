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
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import com.sitmypet.utils.CaptchaGenerator;
import javafx.scene.image.ImageView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblErreur;
    @FXML private ImageView imgCaptcha;
    @FXML private TextField txtCaptcha;

    private String currentCaptchaCode;
    private ServiceUser serviceUser;
    private Timeline lockoutTimer;

    @FXML
    public void initialize() {
        serviceUser = new ServiceUser();
        genererNouveauCaptcha();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        lblErreur.setStyle(""); // Reset any inline color
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

        User user = null;
        try {
            user = serviceUser.authentifier(email, password);
            if (lockoutTimer != null) lockoutTimer.stop();
        } catch (com.sitmypet.exceptions.AuthenticationException e) {
            if (e.getUnlockTimeMillis() > 0) {
                startLockoutTimer(e.getUnlockTimeMillis(), e.getMessage());
            } else {
                lblErreur.setText(e.getMessage());
                lblErreur.setVisible(true);
            }
            return;
        }

        if (user != null) {
            if (!user.isActive()) {
                lblErreur.setText("⛔ Votre compte est inactif ou en attente de validation par un administrateur.");
                lblErreur.setVisible(true);
                return;
            }

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
        System.out.println("Démarrage Google Login...");
        com.sitmypet.services.GoogleOAuthService googleService = new com.sitmypet.services.GoogleOAuthService();
        googleService.authenticate(new com.sitmypet.services.GoogleOAuthService.GoogleAuthCallback() {
            @Override
            public void onSuccess(com.sitmypet.services.GoogleOAuthService.GoogleUser googleUser) {
                System.out.println("Google callback success: " + googleUser.email);
                try {
                    // On utilise le ServiceUser pour inscrire ou connecter
                    User user = serviceUser.authentifierOuInscrireGoogle(
                            googleUser.email, 
                            googleUser.familyName, 
                            googleUser.givenName, 
                            googleUser.picture
                    );
                    
                    System.out.println("Utilisateur DB: " + (user != null ? user.getEmail() : "null"));
                    
                    if (user != null) {
                        if (!user.isActive()) {
                            lblErreur.setText("⛔ Votre compte est inactif ou en attente de validation par un administrateur.");
                            lblErreur.setVisible(true);
                            return;
                        }
                        
                        String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
                        System.out.println("Rôle de l'utilisateur: " + role);
                        
                        Stage stage = (Stage) lblErreur.getScene().getWindow(); // Plus sûr que event.getSource()
                        
                        if (role.contains("ADMIN")) {
                            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/AfficherUser.fxml"));
                            stage.setTitle("SitMyPet - Dashboard Administrateur");
                            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                            stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
                            stage.setMaximized(true);
                            stage.centerOnScreen();
                            stage.show();
                            stage.toFront();
                            stage.requestFocus();
                        } else {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/FrontAccueil.fxml"));
                            Parent root = loader.load();
                            com.sitmypet.controllers.FrontAccueilController controller = loader.getController();
                            controller.setUser(user);
                            stage.setTitle("SitMyPet - Espace Propriétaire/Gardien");
                            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                            stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
                            stage.setMaximized(true);
                            stage.centerOnScreen();
                            stage.show();
                            stage.toFront();
                            stage.requestFocus();
                        }
                    } else {
                        lblErreur.setText("Échec de la connexion Google (Base de données).");
                        lblErreur.setVisible(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Exception dans onSuccess: " + e.getMessage());
                    lblErreur.setText("Erreur inattendue: " + e.getMessage());
                    lblErreur.setVisible(true);
                }
            }

            @Override
            public void onError(String error) {
                System.err.println("Erreur Google callback: " + error);
                lblErreur.setText(error);
                lblErreur.setVisible(true);
            }
        });
    }

    @FXML
    private void handleForgotPassword(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Mot de passe oublié");
        
        javafx.scene.control.DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {}
        
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(20);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(20, 30, 10, 30));
        
        javafx.scene.layout.StackPane iconContainer = new javafx.scene.layout.StackPane();
        iconContainer.setStyle("-fx-background-color: #f3e8ff; -fx-background-radius: 50; -fx-padding: 20; -fx-max-width: 80; -fx-max-height: 80;");
        Label iconLabel = new Label("🔐");
        iconLabel.setStyle("-fx-font-size: 40px;");
        iconContainer.getChildren().add(iconLabel);
        
        Label titleLabel = new Label("Mot de passe oublié ?");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #2d1354;");
        
        Label descLabel = new Label("Entrez votre adresse email associée à votre compte.\nNous vous enverrons un mot de passe temporaire.");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-text-alignment: center;");
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        TextField emailField = new TextField();
        emailField.setPromptText("votre.email@exemple.com");
        emailField.setStyle("-fx-padding: 15; -fx-font-size: 15px; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e2e8f0; -fx-background-color: #f8fafc; -fx-pref-width: 300;");
        
        content.getChildren().addAll(iconContainer, titleLabel, descLabel, emailField);
        dialogPane.setContent(content);
        
        javafx.scene.control.ButtonType btnEnvoyer = new javafx.scene.control.ButtonType("Envoyer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType btnAnnuler = new javafx.scene.control.ButtonType("Annuler", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(btnAnnuler, btnEnvoyer);
        
        javafx.scene.control.Button sendNode = (javafx.scene.control.Button) dialogPane.lookupButton(btnEnvoyer);
        sendNode.setStyle("-fx-background-color: #8e5bd6; -fx-text-fill: white; -fx-padding: 10 25; -fx-font-size: 15px; -fx-background-radius: 30; -fx-font-weight: 900; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(142, 91, 214, 0.4), 10, 0, 0, 3);");
        
        javafx.scene.control.Button cancelNode = (javafx.scene.control.Button) dialogPane.lookupButton(btnAnnuler);
        cancelNode.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0aec0; -fx-font-weight: bold; -fx-font-size: 15px; -fx-cursor: hand;");
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnEnvoyer) {
                return emailField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String email = result.get().trim();
            if (email.isEmpty()) {
                lblErreur.setText("L'adresse email ne peut pas être vide.");
                lblErreur.setVisible(true);
                return;
            }

            User user = serviceUser.trouverParEmail(email);
            if (user == null) {
                lblErreur.setText("Aucun compte trouvé avec cette adresse email.");
                lblErreur.setVisible(true);
                return;
            }

            // Générer un mot de passe temporaire
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
            StringBuilder tempPassword = new StringBuilder("SitMyPet-");
            java.util.Random rnd = new java.util.Random();
            for (int i = 0; i < 6; i++) {
                tempPassword.append(chars.charAt(rnd.nextInt(chars.length())));
            }

            boolean isChanged = serviceUser.changerMotDePasse(email, tempPassword.toString());
            
            if (isChanged) {
                com.sitmypet.services.EmailService emailService = new com.sitmypet.services.EmailService();
                boolean isSent = emailService.envoyerNouveauMotDePasse(email, tempPassword.toString());
                
                if (isSent) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Email envoyé");
                    alert.setHeaderText(null);
                    alert.setContentText("Un nouveau mot de passe a été envoyé à votre adresse email. Veuillez vérifier votre boîte de réception.");
                    alert.showAndWait();
                } else {
                    // Fallback de développement : si l'email échoue, on affiche le mot de passe à l'écran
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Mode Développement - Email non configuré");
                    alert.setHeaderText("L'envoi de l'email a échoué");
                    alert.setContentText("Le mot de passe a été réinitialisé en base de données.\n\n" +
                                         "Étant donné que le service SMTP n'est pas encore configuré avec vos identifiants, " +
                                         "voici le mot de passe généré pour vous permettre de tester :\n\n" +
                                         "Nouveau mot de passe : " + tempPassword.toString());
                    alert.showAndWait();
                }
            } else {
                lblErreur.setText("Une erreur est survenue lors de la réinitialisation.");
                lblErreur.setVisible(true);
            }
        }
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

    private void startLockoutTimer(long targetTimeMillis, String baseMessage) {
        if (lockoutTimer != null) {
            lockoutTimer.stop();
        }
        
        lockoutTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            long remainingSec = (targetTimeMillis - System.currentTimeMillis()) / 1000;
            if (remainingSec <= 0) {
                lblErreur.setText("✅ Fin du blocage. Vous pouvez réessayer de vous connecter.");
                lblErreur.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Green
                if (lockoutTimer != null) lockoutTimer.stop();
            } else {
                // If it's a very long block (e.g. 10 years), just show the message without a timer
                if (remainingSec > 3600 * 24) {
                    lblErreur.setText(baseMessage);
                    lblErreur.setStyle("-fx-text-fill: #e53e3e;"); // Red
                    if (lockoutTimer != null) lockoutTimer.stop();
                    return;
                }
                long min = remainingSec / 60;
                long sec = remainingSec % 60;
                String timerText = String.format("%02d:%02d", min, sec);
                
                String displayMsg = baseMessage.contains("Veuillez réessayer") ? 
                        baseMessage.substring(0, baseMessage.indexOf("Veuillez réessayer")).trim() : 
                        baseMessage;
                
                lblErreur.setText(displayMsg + " Déblocage dans " + timerText);
                lblErreur.setStyle("-fx-text-fill: #e53e3e;"); // Red
            }
        }));
        lockoutTimer.setCycleCount(Timeline.INDEFINITE);
        lockoutTimer.play();
        
        // Force un premier affichage pour éviter le délai de 1 seconde
        lblErreur.setText(baseMessage);
        lblErreur.setVisible(true);
    }
}
