package com.sitmypet.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.sitmypet.model.Reclamation;
import com.sitmypet.services.ServiceReclamation;
import com.sitmypet.utils.CloudinaryService;
import javafx.beans.value.ChangeListener;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class AjouterReclamation implements Initializable {

    @FXML private TextField        champSujet;
    @FXML private TextArea         champDescription;
    @FXML private ComboBox<String> champPriorite;
    @FXML private TextField        champNom;
    @FXML private TextField        champEmail;
    @FXML private ImageView        apercuPhoto;
    @FXML private Label            lblPhoto;
    @FXML private Button btnChoisirPhoto;
    @FXML private Label lblPrioriteAuto;

    private final ServiceReclamation service = new ServiceReclamation();
    private File   fichierPhotoChoisi  = null;
    private String photoUrlCloudinary  = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        champPriorite.setItems(FXCollections.observableArrayList(
                "basse", "moyenne", "haute"
        ));

        // ── Détection automatique priorité en temps réel ──────────────────────
        ChangeListener<String> detecteur = (obs, oldVal, newVal) -> {
            String sujet = champSujet.getText();
            String desc  = champDescription.getText();

            if (!sujet.isBlank() || !desc.isBlank()) {
                String prioriteDetectee = service.detecterPriorite(sujet, desc);
                champPriorite.setValue(prioriteDetectee);

                // Feedback visuel selon la priorité détectée
                switch (prioriteDetectee) {
                    case "haute" -> {
                        lblPrioriteAuto.setText("Priorité HAUTE détectée!");
                        lblPrioriteAuto.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                    case "moyenne" -> {
                        lblPrioriteAuto.setText("Priorité MOYENNE détectée.");
                        lblPrioriteAuto.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    }
                    default -> {
                        lblPrioriteAuto.setText("Priorité basse.");
                        lblPrioriteAuto.setStyle("-fx-text-fill: #27ae60;");
                    }
                }
            }
        };

        // Observer sur les deux champs
        champSujet.textProperty().addListener(detecteur);
        champDescription.textProperty().addListener(detecteur);
    }

    // ── Choisir photo ─────────────────────────────────────────────────────────
    @FXML
    private void choisirPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        Stage stage = (Stage) btnChoisirPhoto.getScene().getWindow();
        File fichier = fileChooser.showOpenDialog(stage);

        if (fichier != null) {
            fichierPhotoChoisi = fichier;
            Image image = new Image(fichier.toURI().toString(), 150, 150, true, true);
            apercuPhoto.setImage(image);
            apercuPhoto.setVisible(true);
            lblPhoto.setText("📎 " + fichier.getName());
            lblPhoto.setStyle("-fx-text-fill: #27ae60;");
        }
    }

    // ── Retirer la photo ──────────────────────────────────────────────────────
    @FXML
    private void supprimerPhoto() {
        fichierPhotoChoisi = null;
        photoUrlCloudinary = null;
        apercuPhoto.setImage(null);
        apercuPhoto.setVisible(false);
        lblPhoto.setText("Aucune photo sélectionnée");
        lblPhoto.setStyle("-fx-text-fill: #888888;");
    }

    // ── Enregistrer ───────────────────────────────────────────────────────────
    @FXML
    private void enregistrer() {

        // ── Validation champs vides ───────────────────────────────────────────
        if (champSujet.getText().trim().isEmpty()       ||
                champDescription.getText().trim().isEmpty() ||
                champNom.getText().trim().isEmpty()         ||
                champEmail.getText().trim().isEmpty()       ||
                champPriorite.getValue() == null) {

            new Alert(Alert.AlertType.WARNING,
                    " Veuillez remplir tous les champs obligatoires.")
                    .showAndWait();
            return;
        }

        // ── Upload Cloudinary si une photo est choisie ────────────────────────
        if (fichierPhotoChoisi != null) {
            try {
                lblPhoto.setText("⏳ Upload en cours...");
                lblPhoto.setStyle("-fx-text-fill: #e67e22;");
                btnChoisirPhoto.setDisable(true);

                // Upload → récupère l'URL publique
                photoUrlCloudinary = CloudinaryService.getInstance()
                        .uploadImage(fichierPhotoChoisi);

                lblPhoto.setText(" Photo uploadée !");
                lblPhoto.setStyle("-fx-text-fill: #27ae60;");

            } catch (RuntimeException e) {
                lblPhoto.setText(" Échec upload : " + e.getMessage());
                lblPhoto.setStyle("-fx-text-fill: #e74c3c;");
                btnChoisirPhoto.setDisable(false);
                return;  // ← on bloque si l'upload échoue
            }
        }

        // ── Construire la Reclamation avec ton constructeur existant ──────────
        Reclamation r = new Reclamation(
                champSujet.getText().trim(),
                champDescription.getText().trim(),
                LocalDateTime.now(),
                "en_attente",
                champPriorite.getValue(),
                champNom.getText().trim(),
                champEmail.getText().trim(),
                1   // userId par défaut
        );

        // Injecter l'URL photo (null si pas de photo → la DB accepte NULL)
        r.setPhotoUrl(photoUrlCloudinary);

        // ── Validation métier ─────────────────────────────────────────────────
        String validation = service.valider(r);
        if (!validation.equals("OK")) {
            new Alert(Alert.AlertType.WARNING, validation).showAndWait();
            btnChoisirPhoto.setDisable(false);
            return;
        }

        // ── Sauvegarde ────────────────────────────────────────────────────────
        service.add(r);
        new Alert(Alert.AlertType.INFORMATION, " Réclamation ajoutée !").showAndWait();
        fermer();

    }

    @FXML private void annuler() { fermer(); }

    private void fermer() {
        ((Stage) champSujet.getScene().getWindow()).close();
    }
}
