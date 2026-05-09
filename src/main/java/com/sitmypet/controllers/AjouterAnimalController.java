package com.sitmypet.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sitmypet.SessionContext;
import com.sitmypet.model.Pet;
import com.sitmypet.model.User;
import com.sitmypet.services.ServicePet;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;

public class AjouterAnimalController {

    // ══════════════════════════════════════════════════════════
    // CONFIG CLOUDINARY — remplace par tes vraies credentials
    // Inscription gratuite sur https://cloudinary.com
    // ══════════════════════════════════════════════════════════
    private static final String CLOUD_NAME = "dzfroarbr";        // ✅ déjà visible
    private static final String API_KEY    = "536889856547135";        // depuis API Keys
    private static final String API_SECRET = "XzVnSr8igerEsyjlqn3lcHXD1T8";        // depuis API Keys

    private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", CLOUD_NAME,
            "api_key",    API_KEY,
            "api_secret", API_SECRET,
            "secure",     true
    ));

    // ── Champs FXML ───────────────────────────────────────────
    @FXML private TextField   nomAnimal;
    @FXML private TextField   race;
    @FXML private TextField   poids;
    @FXML private DatePicker  dateNaissance;
    @FXML private TextArea    description;
    @FXML private ToggleGroup typeAnimalGroup;
    @FXML private ToggleGroup genreGroup;
    @FXML private CheckBox    vaccine;
    @FXML private CheckBox    maladieContagieuse;
    @FXML private CheckBox    dossierMedical;
    @FXML private CheckBox    etatCritique;
    @FXML private Button      btnEnregistrer;
    @FXML private Button      btnPhoto;

    // ── Preview (créés en Java) ───────────────────────────────
    private ImageView previewImage;
    private VBox      placeholderBox;
    private Label     labelNomFichier;

    // URL Cloudinary retournée après upload
    private String savedImageName = null;

    @FXML
    public void initialize() {
        if (btnPhoto != null && btnPhoto.getParent() instanceof Pane parent) {
            StackPane photoPane = buildPhotoPane();
            int index = parent.getChildren().indexOf(btnPhoto);
            parent.getChildren().add(index >= 0 ? index : 0, photoPane);
        }
    }

    private StackPane buildPhotoPane() {
        previewImage = new ImageView();
        previewImage.setFitWidth(176); previewImage.setFitHeight(176);
        previewImage.setPreserveRatio(true); previewImage.setVisible(false);

        Label emoji = new Label("\uD83D\uDC3E");
        emoji.setStyle("-fx-font-size:52px;");
        Label hint = new Label("Cliquez pour\najouter une photo");
        hint.setStyle("-fx-font-size:11px;-fx-text-fill:#9b72e8;-fx-font-weight:bold;-fx-text-alignment:center;");
        hint.setWrapText(true);
        placeholderBox = new VBox(6, emoji, hint);
        placeholderBox.setAlignment(Pos.CENTER);

        labelNomFichier = new Label("Aucune photo selectionnee");
        labelNomFichier.setStyle("-fx-font-size:10px;-fx-text-fill:#aaaaaa;");

        StackPane pane = new StackPane(placeholderBox, previewImage);
        pane.setPrefSize(180, 180); pane.setMinSize(180, 180); pane.setMaxSize(180, 180);
        pane.setStyle("-fx-background-color:#ede9f8;-fx-background-radius:16px;"
                + "-fx-border-color:#c9b8f0;-fx-border-radius:16px;-fx-border-width:2;-fx-cursor:hand;");
        pane.setOnMouseClicked(e -> doPhotoUpload());
        return pane;
    }

    @FXML private void handlePhotoUpload() { doPhotoUpload(); }

    private void doPhotoUpload() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une photo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg","*.gif","*.bmp"));
        javafx.stage.Window w = btnPhoto != null
                ? btnPhoto.getScene().getWindow() : nomAnimal.getScene().getWindow();
        File file = fc.showOpenDialog(w);
        if (file == null) return;

        // 1) Afficher preview locale immédiatement
        try {
            Image img = new Image(file.toURI().toString(), 176, 176, true, true);
            if (!img.isError() && previewImage != null) {
                previewImage.setImage(img);
                previewImage.setVisible(true);
                if (placeholderBox != null) placeholderBox.setVisible(false);
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 2) Feedback "upload en cours"
        if (labelNomFichier != null) {
            labelNomFichier.setText("Upload Cloudinary en cours...");
            labelNomFichier.setStyle("-fx-font-size:10px;-fx-text-fill:#9b72e8;-fx-font-weight:bold;");
        }
        if (btnEnregistrer != null) btnEnregistrer.setDisable(true);

        // 3) Upload dans un thread séparé (ne bloque pas l'UI)
        Thread t = new Thread(() -> {
            String url = uploadToCloudinary(file);
            Platform.runLater(() -> {
                if (url != null) {
                    savedImageName = url;
                    if (labelNomFichier != null) {
                        labelNomFichier.setText("Photo uploadee !");
                        labelNomFichier.setStyle("-fx-font-size:10px;-fx-text-fill:#4caf50;-fx-font-weight:bold;");
                    }
                } else {
                    if (labelNomFichier != null) {
                        labelNomFichier.setText("Erreur upload - verifiez votre connexion");
                        labelNomFichier.setStyle("-fx-font-size:10px;-fx-text-fill:#e87272;");
                    }
                }
                if (btnEnregistrer != null) btnEnregistrer.setDisable(false);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ── Upload Cloudinary ─────────────────────────────────────
    private String uploadToCloudinary(File file) {
        try {
            Map result = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                    "folder",        "sitmypet/animaux",
                    "resource_type", "image"
            ));
            return (String) result.get("secure_url");
        } catch (Exception e) {
            System.out.println("Erreur Cloudinary upload : " + e.getMessage());
            return null;
        }
    }

    @FXML
    private void handleSupprimerPhoto() {
        if (previewImage   != null) { previewImage.setImage(null); previewImage.setVisible(false); }
        if (placeholderBox != null) placeholderBox.setVisible(true);
        if (labelNomFichier != null) {
            labelNomFichier.setText("Aucune photo selectionnee");
            labelNomFichier.setStyle("-fx-font-size:10px;-fx-text-fill:#aaaaaa;");
        }
        savedImageName = null;
    }

    @FXML
    private void handleEnregistrer() {
        String nom      = nomAnimal.getText()   == null ? "" : nomAnimal.getText().trim();
        String raceVal  = race.getText()        == null ? "" : race.getText().trim();
        String poidsVal = poids.getText()       == null ? "" : poids.getText().trim();
        LocalDate date  = dateNaissance.getValue();
        String desc     = description.getText() == null ? "" : description.getText().trim();
        String typeVal  = typeAnimalGroup.getSelectedToggle() != null
                ? (String) typeAnimalGroup.getSelectedToggle().getUserData() : "";
        String genreVal = genreGroup.getSelectedToggle() != null
                ? (String) genreGroup.getSelectedToggle().getUserData() : "";

        if (nom.isEmpty() || raceVal.isEmpty() || poidsVal.isEmpty() || date == null) {
            alert(Alert.AlertType.WARNING, "Champs manquants", "Remplissez tous les champs obligatoires."); return; }
        if (typeVal.isEmpty())  { alert(Alert.AlertType.WARNING, "Type manquant",  "Selectionnez un type."); return; }
        if (genreVal.isEmpty()) { alert(Alert.AlertType.WARNING, "Genre manquant", "Selectionnez un genre."); return; }
        float w;
        try { w = Float.parseFloat(poidsVal); if (w <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { alert(Alert.AlertType.WARNING, "Poids invalide", "Entrez un nombre positif."); return; }

        User user = SessionContext.getCurrentUser();
        if (user == null) {
            alert(Alert.AlertType.ERROR, "Session", "Utilisateur non connecté.");
            return;
        }
        Pet pet = new Pet(nom, java.sql.Date.valueOf(date), typeVal, raceVal, w, desc, genreVal,
                maladieContagieuse.isSelected(), dossierMedical.isSelected(),
                etatCritique.isSelected(), vaccine.isSelected(), savedImageName, user.getId());
        try {
            new ServicePet().add(pet);
            alert(Alert.AlertType.INFORMATION, "Succès", "Animal \"" + nom + "\" enregistré !");
            Platform.runLater(() -> ((Stage) nomAnimal.getScene().getWindow()).close());
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
        }
    }

    @FXML private void handleRetour() {
        Platform.runLater(() -> ((Stage) nomAnimal.getScene().getWindow()).close());
    }

    private void alert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}