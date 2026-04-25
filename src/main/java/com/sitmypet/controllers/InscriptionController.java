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

import com.sitmypet.utils.CaptchaGenerator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

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
    @FXML private ImageView imgCaptcha;
    @FXML private TextField txtCaptcha;
    @FXML private VBox vboxCertificat;
    @FXML private Label lblCertificatFile;

    private String currentCaptchaCode;
    private java.io.File certificatFile;

    private ServiceUser serviceUser;

    @FXML
    public void initialize() {
        serviceUser = new ServiceUser();
        // Peupler le choix de rôles
        choiceRole.getItems().addAll("GARDIEN", "PROPRIETAIRE");
        choiceRole.getSelectionModel().selectFirst();
        
        choiceRole.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("GARDIEN".equals(newVal)) {
                vboxCertificat.setVisible(true);
                vboxCertificat.setManaged(true);
            } else {
                vboxCertificat.setVisible(false);
                vboxCertificat.setManaged(false);
            }
        });
        
        // Initialiser la visibilité au démarrage
        vboxCertificat.setVisible(true);
        vboxCertificat.setManaged(true);

        genererNouveauCaptcha();

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
        try {
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String tel = txtTelephone.getText().trim();
            String pass = txtPassword.getText();
            String confPass = txtConfirmPassword.getText();
            String role = choiceRole.getValue();

            lblErreur.setVisible(false);

            // 1. Contrôle de saisie global
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || pass.isEmpty() || confPass.isEmpty() || txtCaptcha.getText().trim().isEmpty()) {
                afficherErreur("⚠️ Veuillez remplir tous les champs obligatoires, y compris le CAPTCHA.");
                return;
            }

            // 1.5 Vérification du CAPTCHA (insensible à la casse)
            if (!txtCaptcha.getText().trim().equalsIgnoreCase(currentCaptchaCode)) {
                afficherErreur("⚠️ Le code CAPTCHA est incorrect.");
                genererNouveauCaptcha(); // On force un nouveau si erreur
                txtCaptcha.clear();
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

            boolean isGardien = "GARDIEN".equals(role);
            if (isGardien && certificatFile == null) {
                afficherErreur("⚠️ Veuillez importer votre certificat pour continuer l'inscription.");
                return;
            }

            boolean isActive = true;
            String certificatFileName = null;
            if (isGardien) {
                try {
                    // Créer le dossier de destination
                    java.io.File uploadDir = new java.io.File("SitMyPet-Desktop/src/main/resources/uploads/certificats");
                    if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                        // Essayer le chemin sans le dossier parent si le CWD est différent
                        uploadDir = new java.io.File("src/main/resources/uploads/certificats");
                        uploadDir.mkdirs();
                    }
                    
                    // Copie du fichier
                    certificatFileName = java.util.UUID.randomUUID().toString() + "_" + certificatFile.getName();
                    java.io.File destFile = new java.io.File(uploadDir, certificatFileName);
                    java.nio.file.Files.copy(certificatFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    net.sourceforge.tess4j.Tesseract tesseract = new net.sourceforge.tess4j.Tesseract();
                    // Utiliser LoadLibs pour extraire correctement depuis le classpath
                    java.io.File tessDataFolder = net.sourceforge.tess4j.util.LoadLibs.extractTessResources("tessdata");
                    tesseract.setDatapath(tessDataFolder.getAbsolutePath());
                    tesseract.setLanguage("fra"); 
                    
                    String ocrResult = tesseract.doOCR(certificatFile).toLowerCase();
                    if (ocrResult.contains("certificat") || ocrResult.contains("diplôme") || ocrResult.contains("diplome") || ocrResult.contains("attestation")) {
                        isActive = true;
                    } else {
                        isActive = false;
                        System.out.println("OCR n'a pas validé le document. Statut : En attente.");
                    }
                } catch (Throwable e) {
                    System.err.println("Erreur copie/OCR: " + e.getMessage());
                    isActive = false;
                }
            }

            // 4. Création objet utilisateur
            User newUser = new User(nom, prenom, email, tel, "", "default.png", role, isActive);
            if (isGardien && certificatFileName != null) {
                newUser.setCertificat(certificatFileName);
            }

            // 5. Sauvegarde
            boolean success = serviceUser.inscrire(newUser, pass);
            
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Inscription réussie");
                alert.setHeaderText(null);
                
                if (isGardien && !isActive) {
                    alert.setContentText("Votre compte a été créé. Toutefois, la vérification automatique du certificat n'a pas pu aboutir. Votre compte est en attente d'activation manuelle par un administrateur.");
                } else {
                    alert.setContentText("Votre compte a bien été créé ! Vous pouvez maintenant vous connecter.");
                }
                alert.showAndWait();

                retourLogin(((Node) event.getSource()).getScene().getWindow());
            } else {
                afficherErreur("Erreur lors de la création du compte (l'email existe peut-être déjà).");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur Critique");
            alert.setHeaderText("Une erreur est survenue lors de l'inscription");
            alert.setContentText(t.toString());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleUploadCertificat(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir un certificat (Image)");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            certificatFile = selectedFile;
            lblCertificatFile.setText(selectedFile.getName());
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

    @FXML
    private void handleGoogleSignUp(ActionEvent event) {
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
                    // Rediriger vers FrontAccueil
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/FrontAccueil.fxml"));
                        Parent root = loader.load();
                        
                        com.sitmypet.controllers.FrontAccueilController controller = loader.getController();
                        controller.setUser(user);
                        
                        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                        stage.setTitle("SitMyPet - Espace Propriétaire");
                        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                        stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
                        stage.setMaximized(true);
                        stage.centerOnScreen();
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        afficherErreur("⚠️ Erreur lors de l'ouverture du Dashboard.");
                    }
                } else {
                    afficherErreur("⚠️ Échec de la création du compte Google.");
                }
            }

            @Override
            public void onError(String error) {
                afficherErreur(error);
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
