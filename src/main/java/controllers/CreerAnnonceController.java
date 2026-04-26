package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import model.Announcement;
import model.Pet;
import netscape.javascript.JSObject;
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
    @FXML private TextField        fieldAdresse;   // lecture seule, rempli par la carte
    @FXML private TextField        fieldService;
    @FXML private WebView          mapView;         // carte Leaflet

    private WebEngine webEngine;

    private final List<TextField> visitePickers = new ArrayList<>();
    private final List<Pet>       petsList      = new ArrayList<>();
    private int visiteCounter = 0;
    private final int CURRENT_USER_ID = 1;

    // Pont Java ↔ JavaScript
    public class JavaBridge {
        /**
         * Appelé depuis JavaScript quand l'utilisateur clique sur la carte.
         * @param adresse  adresse geocodee par Nominatim
         * @param lat      latitude
         * @param lon      longitude
         */
        public void setAdresse(String adresse, double lat, double lon) {
            javafx.application.Platform.runLater(() -> {
                fieldAdresse.setText(adresse);
                fieldAdresse.setStyle(
                        "-fx-background-color:#f0fff4;-fx-border-color:#4caf50;" +
                                "-fx-border-radius:8px;-fx-background-radius:8px;" +
                                "-fx-padding:8px 12px;-fx-font-size:13px;-fx-text-fill:#1b5e20;");
            });
        }
    }

    @FXML
    public void initialize() {
        chargerAnimaux();
        initMap();
    }

    private void chargerAnimaux() {
        petsList.clear();
        petsList.addAll(new ServicePet().getAll());
        for (Pet p : petsList)
            comboAnimal.getItems().add(p.getName() + " (" + p.getTypePet() + ")");
    }

    // ── Initialisation de la carte Leaflet ───────────────────
    private void initMap() {
        webEngine = mapView.getEngine();

        // Activer JavaScript
        webEngine.setJavaScriptEnabled(true);

        // Exposer le pont Java au JS une fois la page chargée
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaBridge", new JavaBridge());
            }
        });

        webEngine.loadContent(buildMapHtml());
    }

    // ── HTML de la carte ─────────────────────────────────────
    private String buildMapHtml() {
        return "<!DOCTYPE html><html><head>" +
                "<meta charset='utf-8'/>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>" +
                "  * { margin:0; padding:0; box-sizing:border-box; }" +
                "  body { font-family: sans-serif; background: #f8f8f8; }" +
                "  #map { width:100%; height:315px; }" +
                "  #status {" +
                "    position: absolute; bottom: 36px; left: 50%; transform: translateX(-50%);" +
                "    background: rgba(155,114,232,0.92); color: white; padding: 6px 18px;" +
                "    border-radius: 20px; font-size: 13px; font-weight: bold;" +
                "    z-index: 1000; pointer-events: none;" +
                "    transition: opacity 0.4s;" +
                "  }" +
                "  #status.hidden { opacity: 0; }" +
                "</style></head><body>" +
                "<div id='map'></div>" +
                "<div id='status'>📍 Cliquez sur la carte pour choisir une position</div>" +
                "<script>" +

                // Carte centrée sur la Tunisie
                "var map = L.map('map', { zoomControl: true }).setView([36.8, 10.18], 7);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "  attribution: '© OpenStreetMap'," +
                "  maxZoom: 19" +
                "}).addTo(map);" +

                "var marker = null;" +
                "var statusEl = document.getElementById('status');" +

                // Clic sur la carte
                "map.on('click', function(e) {" +
                "  var lat = e.latlng.lat;" +
                "  var lon = e.latlng.lng;" +

                // Placer ou déplacer le marqueur
                "  if (marker) {" +
                "    marker.setLatLng([lat, lon]);" +
                "  } else {" +
                "    marker = L.marker([lat, lon], { draggable: true }).addTo(map);" +
                "    marker.on('dragend', function(ev) {" +
                "      var pos = ev.target.getLatLng();" +
                "      geocodeReverse(pos.lat, pos.lng);" +
                "    });" +
                "  }" +

                // Géocodage inverse Nominatim
                "  geocodeReverse(lat, lon);" +
                "});" +

                // Fonction géocodage inverse
                "function geocodeReverse(lat, lon) {" +
                "  statusEl.textContent = '⏳ Recherche de l adresse...';" +
                "  statusEl.classList.remove('hidden');" +
                "  var url = 'https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lon + '&accept-language=fr';" +
                "  fetch(url, { headers: { 'User-Agent': 'SitMyPetApp/1.0' } })" +
                "  .then(function(r) { return r.json(); })" +
                "  .then(function(data) {" +
                "    var adresse = data.display_name || (lat.toFixed(5) + ', ' + lon.toFixed(5));" +
                "    if (marker) marker.bindPopup('<b>📍 ' + adresse + '</b>').openPopup();" +

                // Appel vers Java
                "    if (window.javaBridge) {" +
                "      window.javaBridge.setAdresse(adresse, lat, lon);" +
                "    }" +
                "    statusEl.textContent = '✅ Position selectionnee';" +
                "    setTimeout(function() { statusEl.classList.add('hidden'); }, 2500);" +
                "  })" +
                "  .catch(function() {" +
                "    var fallback = lat.toFixed(5) + ', ' + lon.toFixed(5);" +
                "    if (window.javaBridge) window.javaBridge.setAdresse(fallback, lat, lon);" +
                "    statusEl.textContent = '📍 Position enregistree';" +
                "    setTimeout(function() { statusEl.classList.add('hidden'); }, 2000);" +
                "  });" +
                "}" +

                // Fonction reset appelable depuis Java
                "function resetMarker() {" +
                "  if (marker) { map.removeLayer(marker); marker = null; }" +
                "  statusEl.textContent = '📍 Cliquez sur la carte pour choisir une position';" +
                "  statusEl.classList.remove('hidden');" +
                "}" +

                "</script></body></html>";
    }

    // ── Reset adresse ────────────────────────────────────────
    @FXML
    private void handleResetAdresse() {
        fieldAdresse.clear();
        fieldAdresse.setStyle(
                "-fx-background-color:#f0f0f8;-fx-border-color:#9b72e8;" +
                        "-fx-border-radius:8px;-fx-background-radius:8px;" +
                        "-fx-padding:8px 12px;-fx-font-size:13px;-fx-text-fill:#333;");
        if (webEngine != null) {
            webEngine.executeScript("resetMarker()");
        }
    }

    // ── Type de garde ────────────────────────────────────────
    @FXML
    private void handleTypeGardeChange() {
        Toggle selected = typeGardeGroup.getSelectedToggle();
        if (selected == null) return;
        boolean isChezMoi = "CHEZ_MOI".equals(selected.getUserData());
        sectionVisites.setVisible(isChezMoi);
        sectionVisites.setManaged(isChezMoi);
        if (isChezMoi && listeVisites.getChildren().isEmpty()) ajouterLigneVisite();
    }

    @FXML
    private void handleAjouterVisite() { ajouterLigneVisite(); }

    private void ajouterLigneVisite() {
        visiteCounter++;
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("Visite\n" + visiteCounter);
        label.setStyle("-fx-font-size:12px;-fx-text-fill:#555;-fx-font-weight:bold;");
        label.setMinWidth(45);

        TextField heureField = new TextField();
        heureField.setPromptText("--:--");
        heureField.setStyle("-fx-background-color:white;-fx-border-color:#cccccc;" +
                "-fx-border-radius:8px;-fx-background-radius:8px;-fx-padding:8px 10px;-fx-font-size:13px;");
        HBox.setHgrow(heureField, Priority.ALWAYS);

        heureField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String f = newVal.replaceAll("[^0-9:]", "");
            if (f.length() == 2 && !f.contains(":") && (oldVal == null || !oldVal.contains(":"))) f += ":";
            if (f.length() > 5) f = f.substring(0, 5);
            if (!f.equals(newVal)) heureField.setText(f);
        });

        Button btnSuppr = new Button("✕");
        btnSuppr.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-font-size:12px;" +
                "-fx-font-weight:bold;-fx-background-radius:6px;-fx-cursor:hand;-fx-padding:6 10;");
        btnSuppr.setOnAction(e -> {
            listeVisites.getChildren().remove(row);
            visitePickers.remove(heureField);
            renumeroterVisites();
        });

        row.getChildren().addAll(label, heureField, btnSuppr);
        listeVisites.getChildren().add(row);
        visitePickers.add(heureField);
    }

    private void renumeroterVisites() {
        int num = 1;
        for (var node : listeVisites.getChildren()) {
            if (node instanceof HBox r && !r.getChildren().isEmpty()
                    && r.getChildren().get(0) instanceof Label lbl) {
                lbl.setText("Visite\n" + num++);
            }
        }
        visiteCounter = listeVisites.getChildren().size();
    }

    // ── Creer ────────────────────────────────────────────────
    @FXML
    private void handleCreer() {
        if (comboAnimal.getSelectionModel().getSelectedIndex() < 0) {
            showAlert(Alert.AlertType.WARNING, "Animal manquant", "Choisissez un animal."); return;
        }
        if (typeGardeGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.WARNING, "Type manquant", "Choisissez un type de garde."); return;
        }
        if (dateDebut.getValue() == null || dateFin.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Dates manquantes", "Renseignez les deux dates."); return;
        }
        if (dateDebut.getValue().isAfter(dateFin.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Dates invalides", "Date debut avant date fin."); return;
        }
        if (fieldAdresse.getText() == null || fieldAdresse.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Adresse manquante",
                    "Cliquez sur la carte pour choisir une position."); return;
        }

        String careType = (String) typeGardeGroup.getSelectedToggle().getUserData();
        int petId = petsList.get(comboAnimal.getSelectionModel().getSelectedIndex()).getId();

        String visitHoursCSV = "";
        int visitPerDay = 0;
        if ("CHEZ_MOI".equals(careType)) {
            List<String> horaires = new ArrayList<>();
            for (TextField tp : visitePickers) {
                String h = tp.getText().trim();
                if (!h.isEmpty()) horaires.add(h);
            }
            visitHoursCSV = String.join(",", horaires);
            visitPerDay   = horaires.size();
            String vjText = fieldVisitesJour.getText().trim();
            if (!vjText.isEmpty()) {
                try { visitPerDay = Integer.parseInt(vjText); } catch (Exception ignored) {}
            }
        }

        float remuMin = 0f, remuMax = 0f;
        try { remuMin = Float.parseFloat(fieldRemuMin.getText().trim()); } catch (Exception ignored) {}
        try { remuMax = Float.parseFloat(fieldRemuMax.getText().trim()); } catch (Exception ignored) {}

        Announcement annonce = new Announcement(
                fieldAdresse.getText().trim(), visitHoursCSV, careType,
                Date.valueOf(dateDebut.getValue()), Date.valueOf(dateFin.getValue()),
                visitPerDay, remuMin, remuMax,
                fieldService.getText().trim(), petId, CURRENT_USER_ID);

        try {
            new ServiceAnnouncement().add(annonce);
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Annonce creee avec succes !");
            handleRetour();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
        }
    }

    @FXML private void handleRetour()           { naviguer("/AfficherAnnonces.fxml",   "Mes Annonces"); }
    @FXML private void handleNavDashboard()     { naviguer("/Dashboard.fxml",           "Dashboard"); }
    @FXML private void handleNavAnnonces()      { naviguer("/AfficherAnnonces.fxml",    "Mes Annonces"); }
    @FXML private void handleNavEvenements()    { naviguer("/AfficherEvenements.fxml",  "Evenements"); }
    @FXML private void handleNavAnimaux()       { naviguer("/AfficherAnimales.fxml",    "Mes Animaux"); }
    @FXML private void handleNavReclamations()  { naviguer("/AfficherReclamations.fxml","Reclamations"); }
    @FXML private void handleNavAccueil()      { naviguer("/Accueil.fxml",      "Accueil"); }
    @FXML private void handleNavPostulations() { naviguer("/Postulations.fxml", "Mes Postulations"); }
    private void naviguer(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Parent root  = FXMLLoader.load(resource);
            Stage  stage = (Stage) fieldService.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.setTitle(titre); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
