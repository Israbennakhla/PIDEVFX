package controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Pet;
import services.ServicePet;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

public class ModifierAnimalController {

    // ══════════════════════════════════════════════════════════
    // Mêmes credentials que AjouterAnimalController
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

    @FXML private TextField   nomAnimal;
    @FXML private TextField   race;
    @FXML private TextField   poids;
    @FXML private DatePicker  dateNaissance;
    @FXML private TextArea    description;
    @FXML private ToggleGroup typeAnimalGroup;
    @FXML private ToggleGroup genreGroup;
    @FXML private RadioButton radioMale;
    @FXML private RadioButton radioFemale;
    @FXML private CheckBox    vaccine;
    @FXML private CheckBox    maladieContagieuse;
    @FXML private CheckBox    dossierMedical;
    @FXML private CheckBox    etatCritique;
    @FXML private Button      btnEnregistrer;
    @FXML private Button      btnPhoto;
    @FXML private Hyperlink   btnRetour;

    private Pet    petAModifier;
    private String selectedImagePath = null; // URL Cloudinary ou ancien nom

    @FXML public void initialize() {}

    public void setPet(Pet pet) {
        this.petAModifier = pet;
        preRemplirFormulaire();
    }

    private void preRemplirFormulaire() {
        if (petAModifier == null) return;
        nomAnimal.setText(petAModifier.getName());
        race.setText(petAModifier.getBreed());
        poids.setText(String.valueOf(petAModifier.getWeight()));
        description.setText(petAModifier.getDescription());
        selectedImagePath = petAModifier.getImageName(); // garde l'ancienne valeur
        if (petAModifier.getBirthDate() != null)
            dateNaissance.setValue(petAModifier.getBirthDate().toLocalDate());
        if (petAModifier.getTypePet() != null)
            typeAnimalGroup.getToggles().forEach(t -> {
                if (petAModifier.getTypePet().equals(((RadioButton) t).getUserData())) t.setSelected(true);
            });
        if (petAModifier.getGender() != null)
            genreGroup.getToggles().forEach(t -> {
                if (petAModifier.getGender().equals(((RadioButton) t).getUserData())) t.setSelected(true);
            });
        vaccine.setSelected(petAModifier.isVaccinated());
        maladieContagieuse.setSelected(petAModifier.isHasContagiousDisease());
        dossierMedical.setSelected(petAModifier.isHasMedicalRecord());
        etatCritique.setSelected(petAModifier.isHasCriticalCondition());
    }

    // ── Upload Cloudinary au lieu de copie locale ─────────────
    @FXML
    private void handlePhotoUpload() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg","*.gif","*.bmp"));
        File file = fc.showOpenDialog(btnPhoto.getScene().getWindow());
        if (file == null) return;

        // Feedback immédiat
        if (btnEnregistrer != null) btnEnregistrer.setDisable(true);
        if (btnPhoto != null) btnPhoto.setText("Upload en cours...");

        // Upload dans un thread séparé
        Thread t = new Thread(() -> {
            String url = uploadToCloudinary(file);
            Platform.runLater(() -> {
                if (url != null) {
                    selectedImagePath = url;
                    if (btnPhoto != null) btnPhoto.setText("Photo changee !");
                } else {
                    if (btnPhoto != null) btnPhoto.setText("Erreur upload");
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
    private void handleEnregistrer() {
        String nom      = nomAnimal.getText().trim();
        String raceVal  = race.getText().trim();
        String poidsVal = poids.getText().trim();
        LocalDate dateNaiss = dateNaissance.getValue();
        String typeVal  = typeAnimalGroup.getSelectedToggle() != null
                ? (String) typeAnimalGroup.getSelectedToggle().getUserData() : "";
        String genreVal = genreGroup.getSelectedToggle() != null
                ? (String) genreGroup.getSelectedToggle().getUserData() : "";

        if (nom.isEmpty() || raceVal.isEmpty() || poidsVal.isEmpty() || dateNaiss == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Remplissez tous les champs."); return; }
        if (typeVal.isEmpty())  { showAlert(Alert.AlertType.WARNING, "Type manquant",  "Selectionnez un type."); return; }
        if (genreVal.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Genre manquant", "Selectionnez un genre."); return; }

        float poidsFloat;
        try { poidsFloat = Float.parseFloat(poidsVal); }
        catch (NumberFormatException e) { showAlert(Alert.AlertType.WARNING, "Poids invalide", "Entrez un nombre."); return; }

        petAModifier.setName(nom); petAModifier.setBreed(raceVal); petAModifier.setWeight(poidsFloat);
        petAModifier.setBirthDate(java.sql.Date.valueOf(dateNaiss));
        petAModifier.setDescription(description.getText().trim());
        petAModifier.setTypePet(typeVal); petAModifier.setGender(genreVal);
        petAModifier.setVaccinated(vaccine.isSelected());
        petAModifier.setHasContagiousDisease(maladieContagieuse.isSelected());
        petAModifier.setHasMedicalRecord(dossierMedical.isSelected());
        petAModifier.setHasCriticalCondition(etatCritique.isSelected());
        petAModifier.setImageName(selectedImagePath); // URL Cloudinary ou ancienne valeur

        try {
            new ServicePet().update(petAModifier);
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Animal modifie avec succes !");
            handleRetour();
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage()); }
    }

    @FXML private void handleRetour()          { nav("/AfficherAnimales.fxml",    "Mes Animaux"); }
    @FXML private void handleNavDashboard()    { nav("/Dashboard.fxml",            "Dashboard"); }
    @FXML private void handleNavAccueil()      { nav("/Accueil.fxml",              "Accueil"); }
    @FXML private void handleNavPostulations() { nav("/Postulations.fxml",         "Mes Postulations"); }
    @FXML private void handleNavAnnonces()     { nav("/AfficherAnnonces.fxml",     "Mes Annonces"); }
    @FXML private void handleNavEvenements()   { nav("/AfficherEvenements.fxml",   "Evenements"); }
    @FXML private void handleNavAnimaux()      { nav("/AfficherAnimales.fxml",     "Mes Animaux"); }
    @FXML private void handleNavReclamations() { nav("/AfficherReclamations.fxml", "Reclamations"); }

    private void nav(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) nomAnimal.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.setTitle(titre); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}