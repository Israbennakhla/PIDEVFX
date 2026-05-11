package com.sitmypet.controllers;

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
import com.sitmypet.model.Announcement;
import com.sitmypet.model.Pet;
import com.sitmypet.services.ServiceAnnouncement;
import com.sitmypet.services.ServicePet;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModifierAnnonceController {

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
    @FXML private Button           btnEnregistrer;

    private final List<TextField> visitePickers = new ArrayList<>();
    private final List<Pet>       petsList      = new ArrayList<>();
    private Announcement          annonceAModifier;
    private int                   visiteCounter = 0;

    @FXML public void initialize() { chargerAnimaux(); }

    private void chargerAnimaux() {
        petsList.clear();
        petsList.addAll(new ServicePet().getAll());
        for (Pet p : petsList) comboAnimal.getItems().add(p.getName() + " (" + p.getTypePet() + ")");
    }

    public void setAnnonce(Announcement annonce, Map<Integer, String> petMap) {
        this.annonceAModifier = annonce;
        preRemplir();
    }

    private void preRemplir() {
        if (annonceAModifier == null) return;
        for (int i = 0; i < petsList.size(); i++)
            if (petsList.get(i).getId() == annonceAModifier.getPetId()) { comboAnimal.getSelectionModel().select(i); break; }

        if ("CHEZ_MOI".equals(annonceAModifier.getCareType())) {
            radioChezMoi.setSelected(true);
            sectionVisites.setVisible(true); sectionVisites.setManaged(true);
            String json = annonceAModifier.getVisitHours();
            if (json != null && !json.equals("[]")) {
                String cleaned = json.replaceAll("[\\[\\]\"]","").trim();
                for (String h : cleaned.split(",")) ajouterLigneVisite(h.trim());
            } else ajouterLigneVisite("");
        } else { radioEnChenil.setSelected(true); sectionVisites.setVisible(false); sectionVisites.setManaged(false); }

        fieldVisitesJour.setText(String.valueOf(annonceAModifier.getVisitPerDay()));
        if (annonceAModifier.getDateDebut() != null) dateDebut.setValue(annonceAModifier.getDateDebut().toLocalDate());
        if (annonceAModifier.getDateFin() != null)   dateFin.setValue(annonceAModifier.getDateFin().toLocalDate());
        fieldRemuMin.setText(String.valueOf((int) annonceAModifier.getRemunerationMin()));
        fieldRemuMax.setText(String.valueOf((int) annonceAModifier.getRemunerationMax()));
        fieldAdresse.setText(annonceAModifier.getAddress() != null ? annonceAModifier.getAddress() : "");
        fieldService.setText(annonceAModifier.getServices() != null ? annonceAModifier.getServices() : "");
    }

    @FXML
    private void handleTypeGardeChange() {
        Toggle selected = typeGardeGroup.getSelectedToggle();
        if (selected == null) return;
        boolean isChezMoi = "CHEZ_MOI".equals(selected.getUserData());
        sectionVisites.setVisible(isChezMoi); sectionVisites.setManaged(isChezMoi);
        if (isChezMoi && listeVisites.getChildren().isEmpty()) ajouterLigneVisite("");
    }

    @FXML private void handleAjouterVisite() { ajouterLigneVisite(""); }

    private void ajouterLigneVisite(String valeur) {
        visiteCounter++;
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("Visite\n" + visiteCounter);
        label.setStyle("-fx-font-size:12px;-fx-text-fill:#555;-fx-font-weight:bold;"); label.setMinWidth(45);
        TextField heureField = new TextField(valeur); heureField.setPromptText("--:--");
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
    private void handleEnregistrer() {
        if (comboAnimal.getSelectionModel().getSelectedIndex() < 0) { showAlert(Alert.AlertType.WARNING,"Animal manquant","Choisissez un animal."); return; }
        if (typeGardeGroup.getSelectedToggle() == null) { showAlert(Alert.AlertType.WARNING,"Type manquant","Choisissez un type."); return; }
        if (dateDebut.getValue() == null || dateFin.getValue() == null) { showAlert(Alert.AlertType.WARNING,"Dates manquantes","Renseignez les dates."); return; }
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

        annonceAModifier.setAddress(fieldAdresse.getText().trim());
        annonceAModifier.setVisitHours(visitHoursCSV);
        annonceAModifier.setCareType(careType);
        annonceAModifier.setDateDebut(Date.valueOf(dateDebut.getValue()));
        annonceAModifier.setDateFin(Date.valueOf(dateFin.getValue()));
        annonceAModifier.setVisitPerDay(visitPerDay);
        annonceAModifier.setRemunerationMin(remuMin);
        annonceAModifier.setRemunerationMax(remuMax);
        annonceAModifier.setServices(fieldService.getText().trim());
        annonceAModifier.setPetId(petId);

        try {
            new ServiceAnnouncement().update(annonceAModifier);
            showAlert(Alert.AlertType.INFORMATION,"Succès","Annonce modifiée !");
            handleRetour();
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR,"Erreur","Erreur : " + e.getMessage()); }
    }

    @FXML private void handleRetour() { naviguer("/com/sitmypet/fxml/AfficherAnnonces.fxml","Mes Annonces"); }

    // ── Navigation ────────────────────────────────────────────
    @FXML private void handleNavDashboard()    { naviguer("/com/sitmypet/fxml/Dashboard.fxml",          "Dashboard"); }
    @FXML private void handleNavAnnonces()     { naviguer("/com/sitmypet/fxml/AfficherAnnonces.fxml",   "Mes Annonces"); }
    @FXML private void handleNavEvenements()   { naviguer("/com/sitmypet/fxml/AfficherEvenements.fxml", "Événements"); }
    @FXML private void handleNavAnimaux()      { naviguer("/com/sitmypet/fxml/AfficherAnimales.fxml",   "Mes Animaux"); }
    @FXML private void handleNavReclamations() { naviguer("/com/sitmypet/fxml/AfficherReclamations.fxml","Réclamations"); }
    @FXML private void handleNavAccueil()      { naviguer("/com/sitmypet/fxml/Accueil.fxml",      "Accueil"); }
    @FXML private void handleNavPostulations() { naviguer("/com/sitmypet/fxml/Postulations.fxml", "Mes Postulations"); }
    @FXML private void handleNavMessages() { naviguer("/com/sitmypet/fxml/Messages.fxml", "Messages"); }

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