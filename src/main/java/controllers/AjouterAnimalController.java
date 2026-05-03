package controllers;

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

public class AjouterAnimalController {

    @FXML private TextField    nomAnimal;
    @FXML private TextField    race;
    @FXML private TextField    poids;
    @FXML private DatePicker   dateNaissance;
    @FXML private TextArea     description;
    @FXML private ToggleGroup  typeAnimalGroup;
    @FXML private ToggleGroup  genreGroup;
    @FXML private RadioButton  radioMale;
    @FXML private RadioButton  radioFemale;
    @FXML private CheckBox     vaccine;
    @FXML private CheckBox     maladieContagieuse;
    @FXML private CheckBox     dossierMedical;
    @FXML private CheckBox     etatCritique;
    @FXML private Button       btnEnregistrer;
    @FXML private Button       btnPhoto;
    @FXML private Hyperlink    btnRetour;

    private String selectedImagePath = null;

    @FXML public void initialize() {}

    @FXML
    private void handlePhotoUpload() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une photo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg"));
        File file = fc.showOpenDialog(btnPhoto.getScene().getWindow());
        if (file != null) selectedImagePath = file.getName();
    }

    @FXML
    private void handleEnregistrer() {
        String nom = nomAnimal.getText().trim(), raceVal = race.getText().trim();
        String poidsVal = poids.getText().trim();
        LocalDate dateNaiss = dateNaissance.getValue();
        String desc = description.getText().trim();
        String typeVal = typeAnimalGroup.getSelectedToggle() != null
                ? (String) typeAnimalGroup.getSelectedToggle().getUserData() : "";
        String genreVal = genreGroup.getSelectedToggle() != null
                ? (String) genreGroup.getSelectedToggle().getUserData() : "";

        if (nom.isEmpty() || raceVal.isEmpty() || poidsVal.isEmpty() || dateNaiss == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Remplissez tous les champs obligatoires."); return;
        }
        if (typeVal.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Type manquant", "Sélectionnez un type."); return; }
        if (genreVal.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Genre manquant", "Sélectionnez un genre."); return; }

        float poidsFloat;
        try { poidsFloat = Float.parseFloat(poidsVal); if (poidsFloat <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { showAlert(Alert.AlertType.WARNING, "Poids invalide", "Entrez un nombre > 0."); return; }

        Pet pet = new Pet(nom, java.sql.Date.valueOf(dateNaiss), typeVal, raceVal, poidsFloat, desc, genreVal,
                maladieContagieuse.isSelected(), dossierMedical.isSelected(),
                etatCritique.isSelected(), vaccine.isSelected(), selectedImagePath);

        try {
            new ServicePet().add(pet);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Animal \"" + nom + "\" enregistré !");
            handleRetour();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour() { naviguer("/AfficherAnimales.fxml", "Mes Animaux"); }

    // ── Navigation ────────────────────────────────────────────
    @FXML private void handleNavDashboard()    { naviguer("/Dashboard.fxml",          "Dashboard"); }
    @FXML private void handleNavAnnonces()     { naviguer("/AfficherAnnonces.fxml",   "Mes Annonces"); }
    @FXML private void handleNavEvenements()   { naviguer("/AfficherEvenements.fxml", "Événements"); }
    @FXML private void handleNavAnimaux()      { naviguer("/AfficherAnimales.fxml",   "Mes Animaux"); }
    @FXML private void handleNavReclamations() { naviguer("/AfficherReclamations.fxml","Réclamations"); }

    private void naviguer(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) nomAnimal.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titre);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}