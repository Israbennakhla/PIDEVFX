package com.sitmypet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.sitmypet.model.User;
import com.sitmypet.services.ServiceUser;

import java.io.File;

public class ModifierUserController {

    @FXML
    private TextField txtNom;
    
    @FXML
    private TextField txtPrenom;
    
    @FXML
    private TextField txtEmail;
    
    @FXML
    private TextField txtTelephone;
    
    @FXML
    private TextField txtAdresse;
    
    @FXML
    private TextField txtPhoto;
    
    @FXML
    private ComboBox<String> comboRole;
    

    @FXML
    private Button btnParcourir;
    
    @FXML
    private Button btnEnregistrer;
    
    @FXML
    private Button btnAnnuler;
    
    private ServiceUser serviceUser;
    private User userAModifier;

    public ModifierUserController() {
        serviceUser = new ServiceUser();
    }

    @FXML
    public void initialize() {
        // Initialiser le ComboBox des rôles
        comboRole.getItems().addAll("ADMIN", "PROPRIETAIRE", "GARDIEN");
        setupDynamicValidation();
    }

    private void setupDynamicValidation() {
        txtNom.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.trim().isEmpty() || !newV.trim().matches("^[a-zA-ZÀ-ÿ\\s\\-]+$")) {
                txtNom.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 3px;");
            } else {
                txtNom.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2px; -fx-border-radius: 3px;");
            }
        });

        txtPrenom.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.trim().isEmpty() || !newV.trim().matches("^[a-zA-ZÀ-ÿ\\s\\-]+$")) {
                txtPrenom.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 3px;");
            } else {
                txtPrenom.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2px; -fx-border-radius: 3px;");
            }
        });

        txtEmail.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.trim().isEmpty() || !newV.trim().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                txtEmail.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 3px;");
            } else {
                txtEmail.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2px; -fx-border-radius: 3px;");
            }
        });

        txtTelephone.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.trim().isEmpty() && !newV.trim().matches("^\\d{8}$")) {
                txtTelephone.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 3px;");
            } else if (!newV.trim().isEmpty() && newV.trim().matches("^\\d{8}$")) {
                txtTelephone.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2px; -fx-border-radius: 3px;");
            } else {
                txtTelephone.setStyle("");
            }
        });
    }

    public void setUser(User user) {
        this.userAModifier = user;
        remplirFormulaire();
    }

    private void remplirFormulaire() {
        if (userAModifier != null) {
            txtNom.setText(userAModifier.getNom());
            txtPrenom.setText(userAModifier.getPrenom());
            txtEmail.setText(userAModifier.getEmail());
            txtTelephone.setText(userAModifier.getTelephone());
            txtAdresse.setText(userAModifier.getAdresse());
            txtPhoto.setText(userAModifier.getPhoto());
            comboRole.setValue(userAModifier.getRole());
        }
    }

    @FXML
    private void handleParcourir() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(btnParcourir.getScene().getWindow());
        
        if (file != null) {
            txtPhoto.setText(file.getName());
        }
    }

    @FXML
    private void handleEnregistrer() {
        if (!validerFormulaire()) {
            return;
        }
        
        // Mettre à jour l'utilisateur avec gestion des valeurs nulles
        userAModifier.setNom(txtNom.getText() != null ? txtNom.getText().trim() : "");
        userAModifier.setPrenom(txtPrenom.getText() != null ? txtPrenom.getText().trim() : "");
        userAModifier.setEmail(txtEmail.getText() != null ? txtEmail.getText().trim() : "");
        userAModifier.setTelephone(txtTelephone.getText() != null ? txtTelephone.getText().trim() : "");
        userAModifier.setAdresse(txtAdresse.getText() != null ? txtAdresse.getText().trim() : "");
        userAModifier.setPhoto(txtPhoto.getText() != null ? txtPhoto.getText().trim() : "");
        userAModifier.setRole(comboRole.getValue());
        userAModifier.setActive(true); // Toujours actif
        
        // Enregistrer dans la base de données
        serviceUser.modifier(userAModifier);
        
        // Afficher message de succès
        afficherSucces("Succès", "Utilisateur modifié avec succès !");
        
        // Fermer la fenêtre
        fermerFenetre();
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();
        
        String nom = txtNom.getText() == null ? "" : txtNom.getText().trim();
        String prenom = txtPrenom.getText() == null ? "" : txtPrenom.getText().trim();
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        String tel = txtTelephone.getText() == null ? "" : txtTelephone.getText().trim();
        
        if (nom.isEmpty()) {
            erreurs.append("• Le nom est obligatoire.\n");
        } else if (!nom.matches("^[a-zA-ZÀ-ÿ\\s\\-]+$")) {
            erreurs.append("• Le nom ne doit contenir que des lettres.\n");
        }
        
        if (prenom.isEmpty()) {
            erreurs.append("• Le prénom est obligatoire.\n");
        } else if (!prenom.matches("^[a-zA-ZÀ-ÿ\\s\\-]+$")) {
            erreurs.append("• Le prénom ne doit contenir que des lettres.\n");
        }
        
        if (email.isEmpty()) {
            erreurs.append("• L'email est obligatoire.\n");
        } else if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            erreurs.append("• Le format de l'email est invalide.\n");
        }
        
        if (!tel.isEmpty() && !tel.matches("^\\d{8}$")) {
            erreurs.append("• Le téléphone doit contenir exactement 8 chiffres.\n");
        }
        
        if (comboRole.getValue() == null || comboRole.getValue().toString().isEmpty()) {
            erreurs.append("• Le rôle est obligatoire.\n");
        }
        
        if (erreurs.length() > 0) {
            afficherErreur("⚠️ Erreurs de saisie", erreurs.toString());
            return false;
        }
        
        return true;
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
