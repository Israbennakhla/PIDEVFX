package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Pet;
import services.ServicePet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class AjouterAnimalController {

    // ── Champs injectés depuis le FXML existant ───────────────
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
    @FXML private Button      btnPhoto;         // bouton dans le FXML

    // ── Preview photo : créés en Java dans initialize() ───────
    //    (pas d'injection FXML pour éviter le NullPointerException)
    private ImageView previewImage;
    private VBox      placeholderBox;
    private Label     labelNomFichier;

    // ── État ─────────────────────────────────────────────────
    private String savedImageName = null;

    // ──────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Insérer la zone preview juste au-dessus du btnPhoto
        // On récupère le parent du btnPhoto et on y insère le StackPane avant lui
        if (btnPhoto != null && btnPhoto.getParent() instanceof Pane parent) {
            StackPane photoPane = buildPhotoPane();
            int index = parent.getChildren().indexOf(btnPhoto);
            if (index >= 0) {
                parent.getChildren().add(index, photoPane);
            } else {
                parent.getChildren().add(0, photoPane);
            }
        }
    }

    // ── Construction du StackPane preview ────────────────────
    private StackPane buildPhotoPane() {
        // ImageView
        previewImage = new ImageView();
        previewImage.setFitWidth(176);
        previewImage.setFitHeight(176);
        previewImage.setPreserveRatio(true);
        previewImage.setVisible(false);

        // Placeholder
        Label emoji = new Label("🐾");
        emoji.setStyle("-fx-font-size:52px;");
        Label hint = new Label("Cliquez pour\najouter une photo");
        hint.setStyle("-fx-font-size:11px;-fx-text-fill:#9b72e8;-fx-font-weight:bold;-fx-text-alignment:center;");
        hint.setWrapText(true);

        placeholderBox = new VBox(6, emoji, hint);
        placeholderBox.setAlignment(Pos.CENTER);

        // Label nom fichier
        labelNomFichier = new Label("Aucune photo sélectionnée");
        labelNomFichier.setStyle("-fx-font-size:10px;-fx-text-fill:#aaaaaa;");

        // StackPane
        StackPane pane = new StackPane(placeholderBox, previewImage);
        pane.setPrefSize(180, 180);
        pane.setMinSize(180, 180);
        pane.setMaxSize(180, 180);
        pane.setStyle(
                "-fx-background-color:#ede9f8;" +
                        "-fx-background-radius:16px;" +
                        "-fx-border-color:#c9b8f0;" +
                        "-fx-border-radius:16px;" +
                        "-fx-border-width:2;" +
                        "-fx-cursor:hand;");
        pane.setOnMouseClicked(e -> doPhotoUpload());

        return pane;
    }

    // ── handlePhotoUpload : @FXML car référencé dans le FXML ─
    @FXML
    private void handlePhotoUpload() {
        doPhotoUpload();
    }

    // ── Logique réelle (appelée par FXML et par le StackPane) ─
    private void doPhotoUpload() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une photo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg","*.gif","*.bmp"));

        // Utilise btnPhoto ou nomAnimal pour obtenir la fenêtre
        javafx.stage.Window window = btnPhoto != null
                ? btnPhoto.getScene().getWindow()
                : nomAnimal.getScene().getWindow();

        File file = fc.showOpenDialog(window);
        if (file == null) return;

        // Afficher preview
        try {
            Image img = new Image(file.toURI().toString(), 176, 176, true, true);
            if (!img.isError() && previewImage != null) {
                previewImage.setImage(img);
                previewImage.setVisible(true);
                if (placeholderBox != null) placeholderBox.setVisible(false);
                if (labelNomFichier != null) {
                    labelNomFichier.setText(file.getName());
                    labelNomFichier.setStyle("-fx-font-size:10px;-fx-text-fill:#9b72e8;");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        savedImageName = copyImage(file);
    }

    // ── Supprimer photo ───────────────────────────────────────
    @FXML
    private void handleSupprimerPhoto() {
        if (previewImage    != null) { previewImage.setImage(null); previewImage.setVisible(false); }
        if (placeholderBox  != null) placeholderBox.setVisible(true);
        if (labelNomFichier != null) {
            labelNomFichier.setText("Aucune photo sélectionnée");
            labelNomFichier.setStyle("-fx-font-size:10px;-fx-text-fill:#aaaaaa;");
        }
        savedImageName = null;
    }

    // ── Copie image ───────────────────────────────────────────
    private String copyImage(File src) {
        String[] candidates = {"src/main/resources/images","resources/images","images"};
        Path dir = null;
        for (String c : candidates) {
            Path p = Paths.get(c);
            if (Files.exists(p) && Files.isDirectory(p)) { dir = p; break; }
        }
        if (dir == null) {
            dir = Paths.get("images");
            try { Files.createDirectories(dir); } catch (IOException e) { e.printStackTrace(); }
        }
        String name = System.currentTimeMillis() + "_" + src.getName();
        try {
            Files.copy(src.toPath(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            return name;
        } catch (IOException e) { e.printStackTrace(); return src.getName(); }
    }

    // ── Enregistrer ──────────────────────────────────────────
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
            alert(Alert.AlertType.WARNING, "Champs manquants", "Remplissez tous les champs obligatoires."); return;
        }
        if (typeVal.isEmpty())  { alert(Alert.AlertType.WARNING, "Type manquant",  "Sélectionnez un type."); return; }
        if (genreVal.isEmpty()) { alert(Alert.AlertType.WARNING, "Genre manquant", "Sélectionnez un genre."); return; }

        float w;
        try {
            w = Float.parseFloat(poidsVal);
            if (w <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            alert(Alert.AlertType.WARNING, "Poids invalide", "Entrez un nombre positif."); return;
        }

        Pet pet = new Pet(nom, java.sql.Date.valueOf(date), typeVal, raceVal, w, desc, genreVal,
                maladieContagieuse.isSelected(), dossierMedical.isSelected(),
                etatCritique.isSelected(), vaccine.isSelected(), savedImageName);
        try {
            new ServicePet().add(pet);
            alert(Alert.AlertType.INFORMATION, "Succès", "Animal \"" + nom + "\" enregistré !");
            handleRetour();
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
        }
    }

    // ── Retour & Navigation ───────────────────────────────────
    @FXML private void handleRetour()           { nav("/AfficherAnimales.fxml",    "Mes Animaux"); }
    @FXML private void handleNavDashboard()     { nav("/Dashboard.fxml",            "Dashboard"); }
    @FXML private void handleNavAnnonces()      { nav("/AfficherAnnonces.fxml",     "Mes Annonces"); }
    @FXML private void handleNavEvenements()    { nav("/AfficherEvenements.fxml",   "Événements"); }
    @FXML private void handleNavAnimaux()       { nav("/AfficherAnimales.fxml",     "Mes Animaux"); }
    @FXML private void handleNavReclamations()  { nav("/AfficherReclamations.fxml", "Réclamations"); }

    private void nav(String fxml, String titre) {
        try {
            var res = getClass().getResource(fxml);
            if (res == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Stage stage = (Stage) nomAnimal.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(res)));
            stage.setTitle(titre);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void alert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}
