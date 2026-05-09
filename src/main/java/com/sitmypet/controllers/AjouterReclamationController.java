package com.sitmypet.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sitmypet.SessionContext;
import com.sitmypet.i18n.AppTexts;
import com.sitmypet.model.Reclamation;
import com.sitmypet.model.User;
import com.sitmypet.services.ServiceReclamation;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;


public class AjouterReclamationController {

    /* Même sandbox Cloudinary que {@link AjouterAnimalController}. */
    private static final String CLOUD_NAME = "dzfroarbr";
    private static final String API_KEY = "536889856547135";
    private static final String API_SECRET = "XzVnSr8igerEsyjlqn3lcHXD1T8";

    private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", CLOUD_NAME,
            "api_key", API_KEY,
            "api_secret", API_SECRET,
            "secure", true
    ));

    @FXML private Label lblTitle;
    @FXML private Label lblNotice;
    @FXML private Label lblSujet;
    @FXML private TextField champSujet;
    @FXML private Label lblDesc;
    @FXML private TextArea champDescription;
    @FXML private Label lblPriorite;
    @FXML private ComboBox<String> champPriorite;
    @FXML private Label lblPrioriteAuto;
    @FXML private Label lblNom;
    @FXML private TextField champNom;
    @FXML private Label lblEmail;
    @FXML private TextField champEmail;
    @FXML private Button btnChoisirPhoto;
    @FXML private Button btnRetirerPhoto;
    @FXML private ImageView apercuPhoto;
    @FXML private Label lblPhoto;
    @FXML private Button btnAnnuler;
    @FXML private Button btnEnvoyer;

    private final ServiceReclamation service = new ServiceReclamation();
    private File fichierPhotoChoisi;
    private String photoUrlCloudinary;

    @FXML
    public void initialize() {
        lblNotice.setText(AppTexts.isEnglish()
                ? ""
                : " ");
        champPriorite.getItems().setAll("basse", "moyenne", "haute");
        champPriorite.setValue("basse");
        refreshStaticTexts();

        User u = SessionContext.getCurrentUser();
        if (u != null) {
            champNom.setText((u.getPrenom() != null ? u.getPrenom() : "").trim()
                    + " " + (u.getNom() != null ? u.getNom() : "").trim());
            if (u.getEmail() != null) champEmail.setText(u.getEmail());
        }

        ChangeListener<String> detecteur = (obs, ov, nv) -> {
            String sujet = champSujet.getText();
            String desc = champDescription.getText();
            if (!sujet.isBlank() || !desc.isBlank()) {
                String p = service.detecterPriorite(sujet, desc);
                champPriorite.setValue(p);
                switch (p) {
                    case "haute" -> {
                        lblPrioriteAuto.setText((AppTexts.isEnglish() ? "HIGH priority detected" : "Priorité HAUTE détectée !"));
                        lblPrioriteAuto.setStyle("-fx-text-fill:#e74c3c;-fx-font-weight:bold;");
                    }
                    case "moyenne" -> {
                        lblPrioriteAuto.setText(AppTexts.isEnglish() ? "Medium priority." : "Priorité MOYENNE détectée.");
                        lblPrioriteAuto.setStyle("-fx-text-fill:#e67e22;-fx-font-weight:bold;");
                    }
                    default -> {
                        lblPrioriteAuto.setText(AppTexts.isEnglish() ? "Low priority." : "Priorité basse.");
                        lblPrioriteAuto.setStyle("-fx-text-fill:#27ae60;");
                    }
                }
            }
        };
        champSujet.textProperty().addListener(detecteur);
        champDescription.textProperty().addListener(detecteur);

        lblPhoto.setText(AppTexts.isEnglish() ? "No photo" : "Aucune pièce jointe");
        lblPhoto.setStyle("-fx-text-fill:#888888;");
        apercuPhoto.setVisible(false);

        fichierPhotoChoisi = null;
        photoUrlCloudinary = null;
    }

    private void refreshStaticTexts() {
        lblTitle.setText(AppTexts.t("rec.dialog.title"));
        lblSujet.setText(AppTexts.t("rec.sujet"));
        lblDesc.setText(AppTexts.t("rec.description"));
        lblPriorite.setText(AppTexts.t("rec.priorite"));
        lblNom.setText(AppTexts.t("rec.nom"));
        lblEmail.setText(AppTexts.t("rec.email"));
        btnChoisirPhoto.setText(AppTexts.t("rec.photo"));
        btnRetirerPhoto.setText(AppTexts.isEnglish() ? "Remove" : "Retirer");
        btnAnnuler.setText(AppTexts.t("rec.cancel"));
        btnEnvoyer.setText(AppTexts.t("rec.submit"));
    }

    @FXML
    private void choisirPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(AppTexts.isEnglish() ? "Choose image" : "Choisir une image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"));

        Stage stage = (Stage) champSujet.getScene().getWindow();
        File fichier = fileChooser.showOpenDialog(stage);

        if (fichier != null) {
            fichierPhotoChoisi = fichier;
            Image image = new Image(fichier.toURI().toString(), 150, 150, true, true);
            apercuPhoto.setImage(image);
            apercuPhoto.setVisible(true);
            lblPhoto.setText("📎 " + fichier.getName());
            lblPhoto.setStyle("-fx-text-fill:#27ae60;");
        }
    }

    @FXML
    private void supprimerPhoto() {
        fichierPhotoChoisi = null;
        photoUrlCloudinary = null;
        apercuPhoto.setImage(null);
        apercuPhoto.setVisible(false);
        lblPhoto.setText(AppTexts.isEnglish() ? "No photo" : "Aucune pièce jointe");
        lblPhoto.setStyle("-fx-text-fill:#888888;");
    }

    @FXML
    private void annuler() {
        ((Stage) champSujet.getScene().getWindow()).close();
    }

    @FXML
    private void enregistrer() {
        User deposant = SessionContext.getCurrentUser();
        if (deposant == null || !service.utilisateurPeutDeposerReclamation(deposant.getId())) {
            alert(Alert.AlertType.ERROR,
                    AppTexts.isEnglish()
                            ? "Access denied."
                            : "Accès réservé aux comptes propriétaire ou gardien.");
            return;
        }

        if (champSujet.getText().isBlank()
                || champDescription.getText().isBlank()
                || champNom.getText().isBlank()
                || champEmail.getText().isBlank()
                || champPriorite.getValue() == null) {
            alert(Alert.AlertType.WARNING,
                    AppTexts.isEnglish() ? "Please fill all required fields." : "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (fichierPhotoChoisi != null) {
            lblPhoto.setText("⏳ …");
            btnEnvoyer.setDisable(true);
            File f = fichierPhotoChoisi;
            Thread upl = new Thread(() -> {
                try {
                    String url = uploadToCloudinary(f);
                    Platform.runLater(() -> {
                        photoUrlCloudinary = url;
                        lblPhoto.setText("✅ OK");
                        lblPhoto.setStyle("-fx-text-fill:#27ae60;");
                        persistComplaint(deposant);
                        btnEnvoyer.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        lblPhoto.setText("❌ " + ex.getMessage());
                        lblPhoto.setStyle("-fx-text-fill:#e74c3c;");
                        btnEnvoyer.setDisable(false);
                    });
                }
            });
            upl.setDaemon(true);
            upl.start();
            return;
        }

        persistComplaint(deposant);
    }

    private void persistComplaint(User deposant) {
        Reclamation r = new Reclamation();
        r.setSujet(champSujet.getText().trim());
        r.setDescription(champDescription.getText().trim());
        r.setDateReclamation(LocalDateTime.now());
        r.setStatut("en_attente");
        r.setPriorite(champPriorite.getValue());
        r.setNomClient(champNom.getText().trim());
        r.setEmailClient(champEmail.getText().trim());
        r.setUserId(deposant.getId());
        r.setPhotoUrl(photoUrlCloudinary);

        String validation = service.valider(r);
        if (!validation.equals("OK")) {
            alert(Alert.AlertType.WARNING, validation);
            return;
        }

        try {
            service.add(r, deposant.getId());
            alert(Alert.AlertType.INFORMATION,
                    AppTexts.isEnglish() ? "Complaint submitted." : "Réclamation enregistrée.");
            ((Stage) champSujet.getScene().getWindow()).close();
        } catch (RuntimeException ex) {
            alert(Alert.AlertType.ERROR, ex.getMessage());
        }
    }

    private String uploadToCloudinary(File file) throws Exception {
        Map<?, ?> result = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                "folder", "sitmypet/reclamations",
                "resource_type", "image"
        ));
        String url = (String) result.get("secure_url");
        if (url == null || url.isEmpty()) throw new Exception("Réponse vide");
        return url;
    }

    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
