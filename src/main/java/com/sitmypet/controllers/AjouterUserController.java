package com.sitmypet.controllers;

import com.sitmypet.model.User;
import com.sitmypet.services.ServiceUser;
import com.sitmypet.utils.AlertHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;

public class AjouterUserController {
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtPhoto;
    @FXML private ComboBox<String> comboRole;


    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
        comboRole.setItems(FXCollections.observableArrayList(
            "ADMIN", "PROPRIETAIRE", "GARDIEN"
        ));
        comboRole.setValue("PROPRIETAIRE"); // Valeur par défaut
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

    @FXML
    private void handleParcourir(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            try {
                File uploadDir = new File("SitMyPet-Desktop/src/main/resources/uploads/profiles");
                if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                    uploadDir = new File("src/main/resources/uploads/profiles");
                    uploadDir.mkdirs();
                }
                String extension = file.getName().substring(file.getName().lastIndexOf("."));
                String newFileName = java.util.UUID.randomUUID().toString() + extension;
                File destFile = new File(uploadDir, newFileName);
                java.nio.file.Files.copy(file.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                txtPhoto.setText(newFileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String telephone = txtTelephone.getText().trim();
        String adresse = txtAdresse.getText().trim();
        String photo = txtPhoto.getText().trim();
        String role = comboRole.getValue();
        boolean isActive = true; // Toujours actif par défaut

        if (!validerFormulaire()) {
            return;
        }

        User newUser = new User(nom, prenom, email, telephone, adresse, photo, role, isActive);
        serviceUser.ajouter(newUser);
        
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtPhoto.clear();
        comboRole.setValue("PROPRIETAIRE");
        
        AlertHelper.showAlert(Alert.AlertType.INFORMATION, txtNom.getScene().getWindow(), "Succès", "Utilisateur ajouté avec succès !");
    }

    @FXML
    private void handleShowList(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/AfficherUser.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
        stage.setMaximized(true);
        stage.centerOnScreen();
        stage.show();
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
            AlertHelper.showAlert(Alert.AlertType.ERROR, txtNom.getScene().getWindow(), "! Erreurs de saisie", erreurs.toString());
            return false;
        }
        
        return true;
    }
}

