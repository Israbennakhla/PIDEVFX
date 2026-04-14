package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Announcement;
import model.Pet;
import services.ServiceAnnouncement;
import services.ServicePet;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class CreerAnnonceController {

    @FXML private Hyperlink        btnRetour;
    @FXML private ComboBox<String> comboAnimal;
    @FXML private ToggleGroup      typeGardeGroup;
    @FXML private RadioButton      radioChezMoi;
    @FXML private RadioButton      radioEnChenil;
    @FXML private VBox             sectionVisites;
    @FXML private TextField        fieldVisitesJour;
    @FXML private VBox             listeVisites;
    @FXML private DatePicker       dateDebut;
    @FXML private DatePicker       dateFin;
    @FXML private TextField        fieldRemuMin;
    @FXML private TextField        fieldRemuMax;
    @FXML private TextField        fieldAdresse;
    @FXML private TextField        fieldService;

    private final List<TextField> visitePickers = new ArrayList<>();
    private final List<Pet>       petsList      = new ArrayList<>();
    private int visiteCounter = 0;
    private final int CURRENT_USER_ID = 1;

    @FXML public void initialize() { chargerAnimaux(); }

    private void chargerAnimaux() {
        petsList.clear();
        petsList.addAll(new ServicePet().getAll());
        for (Pet p : petsList) comboAnimal.getItems().add(p.getName() + " (" + p.getTypePet() + ")");
    }

    @FXML
    private void handleTypeGardeChange() {
        Toggle selected = typeGardeGroup.getSelectedToggle();
        if (selected == null) return;
        boolean isChezMoi = "CHEZ_MOI".equals(selected.getUserData());
        sectionVisites.setVisible(isChezMoi);
        sectionVisites.setManaged(isChezMoi);
        if (isChezMoi && listeVisites.getChildren().isEmpty()) ajouterLigneVisite();
    }

    @FXML private void handleAjouterVisite() { ajouterLigneVisite(); }

    private void ajouterLigneVisite() {
        visiteCounter++;
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("Visite\n" + visiteCounter);
        label.setStyle("-fx-font-size:12px;-fx-text-fill:#555;-fx-font-weight:bold;"); label.setMinWidth(45);
        TextField heureField = new TextField(); heureField.setPromptText("--:--");
        heureField.setStyle("-fx-background-color:white;-fx-border-color:#cccccc;-fx-border-radius:8px;-fx-background-radius:8px;-fx-padding:8px 10px;-fx-font-size:13px;");
        HBox.setHgrow(heureField, Priority.ALWAYS);
        heureField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String f = newVal.replaceAll("[^0-9:]","");
            if (f.length()==2 && !f.contains(":") && (oldVal==null || !oldVal.contains(":"))) f+=":";
            if (f.length()>5) f=f.substring(0,5);
            if (!f.equals(newVal)) heureField.setText(f);
        });
        Button btnSuppr = new Button("✕");
        btnSuppr.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;-fx-background-radius:6px;-fx-cursor:hand;-fx-padding:6 10;");
        btnSuppr.setOnAction(e -> { listeVisites.getChildren().remove(row); visitePickers.remove(heureField); renumeroterVisites(); });
        row.getChildren().addAll(label, heureField, btnSuppr);
        listeVisites.getChildren().add(row); visitePickers.add(heureField);
    }

    private void renumeroterVisites() {
        int num = 1;
        for (var node : listeVisites.getChildren())
            if (node instanceof HBox row && !row.getChildren().isEmpty() && row.getChildren().get(0) instanceof Label lbl)
                lbl.setText("Visite\n" + num++);
        visiteCounter = listeVisites.getChildren().size();
    }

    @FXML
    private void handleCreer() {
        if (comboAnimal.getSelectionModel().getSelectedIndex() < 0) { showAlert(Alert.AlertType.WARNING,"Animal manquant","Choisissez un animal."); return; }
        if (typeGardeGroup.getSelectedToggle() == null) { showAlert(Alert.AlertType.WARNING,"Type manquant","Choisissez un type de garde."); return; }
        if (dateDebut.getValue() == null || dateFin.getValue() == null) { showAlert(Alert.AlertType.WARNING,"Dates manquantes","Renseignez les deux dates."); return; }
        if (dateDebut.getValue().isAfter(dateFin.getValue())) { showAlert(Alert.AlertType.WARNING,"Dates invalides","Date début avant date fin."); return; }
        if (fieldAdresse.getText().trim().isEmpty()) { showAlert(Alert.AlertType.WARNING,"Adresse manquante","Renseignez l'adresse."); return; }

        String careType = (String) typeGardeGroup.getSelectedToggle().getUserData();
        int petId = petsList.get(comboAnimal.getSelectionModel().getSelectedIndex()).getId();

        String visitHoursCSV = ""; int visitPerDay = 0;
        if ("CHEZ_MOI".equals(careType)) {
            List<String> horaires = new ArrayList<>();
            for (TextField tp : visitePickers) { String h = tp.getText().trim(); if (!h.isEmpty()) horaires.add(h); }
            visitHoursCSV = String.join(",", horaires); visitPerDay = horaires.size();
            String vjText = fieldVisitesJour.getText().trim();
            if (!vjText.isEmpty()) { try { visitPerDay = Integer.parseInt(vjText); } catch (Exception ignored) {} }
        }

        float remuMin=0f, remuMax=0f;
        try { remuMin = Float.parseFloat(fieldRemuMin.getText().trim()); } catch (Exception ignored) {}
        try { remuMax = Float.parseFloat(fieldRemuMax.getText().trim()); } catch (Exception ignored) {}

        Announcement annonce = new Announcement(fieldAdresse.getText().trim(), visitHoursCSV, careType,
                Date.valueOf(dateDebut.getValue()), Date.valueOf(dateFin.getValue()),
                visitPerDay, remuMin, remuMax, fieldService.getText().trim(), petId, CURRENT_USER_ID);

        try {
            new ServiceAnnouncement().add(annonce);
            showAlert(Alert.AlertType.INFORMATION,"Succès","Annonce créée avec succès !");
            handleRetour();
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR,"Erreur","Erreur : " + e.getMessage()); }
    }

    @FXML private void handleRetour() { naviguer("/AfficherAnnonces.fxml", "Mes Annonces"); }

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
            Stage stage = (Stage) fieldAdresse.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.setTitle(titre); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}