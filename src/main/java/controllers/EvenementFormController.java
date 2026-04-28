package controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;

import javafx.scene.control.*;
import model.Evenement;
import services.ServiceEvenement;
import java.sql.Date;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * EvenementFormController  –  Admin Event Form Overlay with Interactive Map
 * ─────────────────────────────────────────────────────────────────────────────
 * Displayed as a modal overlay on top of the event list.
 * Two-way binding between the address TextField and an OpenStreetMap map:
 *   • Click on map  → reverse geocode via Nominatim → auto-fill address field
 *   • Type address  → forward geocode via Nominatim → move map marker
 */
public class EvenementFormController {
    @FXML private Label lblTitle;
    @FXML private TextField tfName;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfHeure;
    @FXML private TextField tfAddresse;
    @FXML private TextField tfDescription;
    @FXML private WebView adminMapWebView;

    private ServiceEvenement service = new ServiceEvenement();
    private Evenement evenementEdition = null;

    // ── IMPORTANT: Strong reference to prevent GC of the JS bridge ──────────
    private JavaBridge javaBridgeRef;

    // Debounce timer for address text field changes
    private Timer debounceTimer;
    private boolean suppressAddressListener = false;

    // Flag set to true after save so parent knows to refresh
    private boolean saved = false;

    public boolean isSaved() { return saved; }

    @FXML
    public void initialize() {
        if (EvenementController.evenementSelectionneToEdit != null) {
            evenementEdition = EvenementController.evenementSelectionneToEdit;
            lblTitle.setText("✏️ Modifier Événement");
            tfName.setText(evenementEdition.getName());
            if (evenementEdition.getDate() != null) dpDate.setValue(evenementEdition.getDate().toLocalDate());
            tfHeure.setText(evenementEdition.getHeure());
            tfAddresse.setText(evenementEdition.getAddresse());
            tfDescription.setText(evenementEdition.getDescription());
        } else {
            lblTitle.setText("➕ Ajouter Événement");
        }

        // Initialize the map
        if (adminMapWebView != null) {
            Platform.runLater(this::loadAdminMap);
        }

        // Listen for address field changes → update map marker (debounced)
        tfAddresse.textProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressAddressListener) return;
            if (newVal == null || newVal.trim().isEmpty()) return;
            debounceGeocodeForward(newVal.trim());
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  MAP LOGIC
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Loads the Leaflet.js interactive map into the WebView.
     * Clicking on the map triggers a JavaScript→Java callback for reverse geocoding.
     */
    private void loadAdminMap() {
        String initialAddress = (tfAddresse.getText() != null && !tfAddresse.getText().trim().isEmpty())
                ? tfAddresse.getText().trim() : "Tunis";
        String safeAddress = initialAddress.replace("'", "\\'").replace("\"", "\\\"");

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8"/>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    * { margin: 0; padding: 0; }
                    html, body { width: 100%%; height: 100%%; overflow: hidden; }
                    #map { position: absolute; top: 0; left: 0; right: 0; bottom: 0; }
                    .leaflet-control-attribution { font-size: 9px !important; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map', { zoomControl: true }).setView([36.8065, 10.1815], 13);

                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '&copy; OpenStreetMap',
                        maxZoom: 19
                    }).addTo(map);

                    var marker = L.marker([36.8065, 10.1815], {draggable: true}).addTo(map);

                    // ── When user clicks on map ─────────────────────────────
                    map.on('click', function(e) {
                        var lat = e.latlng.lat;
                        var lng = e.latlng.lng;
                        marker.setLatLng([lat, lng]);

                        fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lng + '&zoom=18&addressdetails=1', {
                            headers: { 'Accept-Language': 'fr' }
                        })
                        .then(function(r) { return r.json(); })
                        .then(function(data) {
                            if (data && data.display_name) {
                                marker.bindPopup(data.display_name).openPopup();
                                if (window.javaBridge) {
                                    window.javaBridge.setAddress(data.display_name);
                                }
                            }
                        })
                        .catch(function(err) { console.log('Reverse geocode error:', err); });
                    });

                    // ── When marker is dragged ──────────────────────────────
                    marker.on('dragend', function(e) {
                        var pos = marker.getLatLng();
                        fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat=' + pos.lat + '&lon=' + pos.lng + '&zoom=18&addressdetails=1', {
                            headers: { 'Accept-Language': 'fr' }
                        })
                        .then(function(r) { return r.json(); })
                        .then(function(data) {
                            if (data && data.display_name) {
                                marker.bindPopup(data.display_name).openPopup();
                                if (window.javaBridge) {
                                    window.javaBridge.setAddress(data.display_name);
                                }
                            }
                        })
                        .catch(function(err) { console.log('Reverse geocode error:', err); });
                    });

                    // ── JS function called by Java to move marker ───────────
                    function moveMarker(lat, lng, label) {
                        map.setView([lat, lng], 16);
                        marker.setLatLng([lat, lng]);
                        marker.bindPopup(label).openPopup();
                        map.invalidateSize();
                    }

                    // ── Geocode initial address ─────────────────────────────
                    fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent('%s'), {
                        headers: { 'Accept-Language': 'fr' }
                    })
                        .then(function(r) { return r.json(); })
                        .then(function(data) {
                            if (data && data.length > 0) {
                                var lat = parseFloat(data[0].lat);
                                var lon = parseFloat(data[0].lon);
                                map.setView([lat, lon], 15);
                                marker.setLatLng([lat, lon]);
                                marker.bindPopup('%s').openPopup();
                                map.invalidateSize();
                            }
                        })
                        .catch(function(err) { console.log(err); });

                    // Continuously fix size until stable
                    var resizeCount = 0;
                    var resizeInterval = setInterval(function() {
                        map.invalidateSize();
                        resizeCount++;
                        if (resizeCount > 20) clearInterval(resizeInterval);
                    }, 200);

                    window.addEventListener('resize', function() { map.invalidateSize(); });
                    new ResizeObserver(function() { map.invalidateSize(); }).observe(document.getElementById('map'));
                </script>
            </body>
            </html>
            """.formatted(safeAddress, safeAddress);

        WebEngine engine = adminMapWebView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Create the bridge and keep a STRONG reference (prevents GC!)
        javaBridgeRef = new JavaBridge();

        // Register Java→JS bridge once the page is loaded
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", javaBridgeRef);
            }
        });

        engine.loadContent(html);
    }

    /**
     * Java bridge object exposed to JavaScript.
     * JavaScript calls javaBridge.setAddress(address) after reverse geocoding.
     */
    public class JavaBridge {
        /**
         * Called from JavaScript with the resolved address string.
         */
        public void setAddress(String address) {
            Platform.runLater(() -> {
                suppressAddressListener = true;
                tfAddresse.setText(address);
                suppressAddressListener = false;
            });
        }
    }

    /**
     * Debounced forward geocoding: when the user types an address,
     * wait 800ms after the last keystroke before geocoding.
     */
    private void debounceGeocodeForward(String address) {
        if (debounceTimer != null) {
            debounceTimer.cancel();
        }
        debounceTimer = new Timer();
        debounceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                geocodeForward(address);
            }
        }, 800);
    }

    /**
     * Forward geocoding: converts an address string to lat/lng
     * and moves the map marker to that location.
     */
    private void geocodeForward(String address) {
        try {
            String encoded = java.net.URLEncoder.encode(address, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encoded + "&limit=1";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "SitMyPet-JavaFX/1.0")
                    .header("Accept-Language", "fr")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            // Parse lat/lon from JSON array response
            if (body.contains("\"lat\"")) {
                String lat = extractJsonValue(body, "lat");
                String lon = extractJsonValue(body, "lon");

                if (lat != null && lon != null) {
                    String safeName = address.replace("'", "\\'").replace("\"", "\\\"");
                    Platform.runLater(() -> {
                        if (adminMapWebView != null) {
                            adminMapWebView.getEngine().executeScript(
                                    "moveMarker(" + lat + ", " + lon + ", '" + safeName + "');");
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Forward geocoding error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  JSON HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx + pattern.length());
        if (colon < 0) return null;
        int start = json.indexOf("\"", colon) + 1;
        int end = json.indexOf("\"", start);
        if (start <= 0 || end <= start) return null;
        return json.substring(start, end);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  FORM LOGIC
    // ═══════════════════════════════════════════════════════════════════════════

    @FXML
    public void enregistrerEvenement(ActionEvent event) {
        if (champsValides()) {
            if (evenementEdition == null) {
                Evenement e = new Evenement(0, tfName.getText(), Date.valueOf(dpDate.getValue()), tfHeure.getText(), tfAddresse.getText(), tfDescription.getText());
                service.add(e);
            } else {
                evenementEdition.setName(tfName.getText());
                evenementEdition.setDate(Date.valueOf(dpDate.getValue()));
                evenementEdition.setHeure(tfHeure.getText());
                evenementEdition.setAddresse(tfAddresse.getText());
                evenementEdition.setDescription(tfDescription.getText());
                service.update(evenementEdition);
            }
            saved = true;
            fermerOverlay(event);
        }
    }

    @FXML
    public void retourListe(ActionEvent event) {
        EvenementController.evenementSelectionneToEdit = null;
        fermerOverlay(event);
    }

    /**
     * Closes this overlay stage.
     */
    private void fermerOverlay(ActionEvent event) {
        if (debounceTimer != null) debounceTimer.cancel();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private boolean champsValides() {
        if (tfName.getText() == null || tfName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le nom de l'événement est obligatoire.");
            return false;
        }
        if (dpDate.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La date de l'événement est obligatoire.");
            return false;
        }
        if (dpDate.getValue().isBefore(java.time.LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Erreur de validation", "La date de l'événement ne peut pas être dans le passé.");
            return false;
        }
        if (tfHeure.getText() == null || !tfHeure.getText().matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "L'heure doit être un format valide HH:MM (ex: 14:30).");
            return false;
        }
        if (tfAddresse.getText() == null || tfAddresse.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "L'adresse de l'événement est obligatoire.");
            return false;
        }
        if (tfDescription.getText() == null || tfDescription.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La description est obligatoire.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
