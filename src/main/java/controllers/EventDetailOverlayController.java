package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import model.Evenement;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * EventDetailOverlayController  –  Client Event Detail Popup with Map
 * ─────────────────────────────────────────────────────────────────────────────
 * Displays full event details in a premium dark overlay.
 * At the bottom, an embedded OpenStreetMap (Leaflet.js) shows
 * the event's address using Nominatim geocoding (free, no API key).
 */
public class EventDetailOverlayController {

    @FXML private Label lblEventName;
    @FXML private Label lblDate;
    @FXML private Label lblHeure;
    @FXML private Label lblAddress;
    @FXML private Label lblDescription;
    @FXML private WebView mapWebView;

    /**
     * Populates the overlay with event data and loads the map.
     */
    public void initData(Evenement ev) {
        lblEventName.setText(ev.getName());
        lblDate.setText(ev.getDate() != null ? ev.getDate().toString() : "N/A");
        lblHeure.setText(ev.getHeure() != null ? ev.getHeure() : "N/A");
        lblAddress.setText(ev.getAddresse() != null ? ev.getAddresse() : "N/A");
        lblDescription.setText(ev.getDescription() != null ? ev.getDescription() : "");

        // Load the map after the WebView is ready
        Platform.runLater(() -> loadMap(ev.getAddresse()));
    }

    /**
     * Generates an HTML page with Leaflet.js + OpenStreetMap tiles.
     * Uses Nominatim (free geocoding) to resolve the address to lat/lng.
     */
    private void loadMap(String address) {
        if (address == null || address.trim().isEmpty()) {
            address = "Tunis";
        }

        // Escape any special characters for JavaScript
        String safeAddress = address.replace("'", "\\'").replace("\"", "\\\"").replace("\n", " ");

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

                    var marker = L.marker([36.8065, 10.1815]).addTo(map);
                    marker.bindPopup('<b>%s</b>').openPopup();

                    // Geocode the address using Nominatim
                    fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent('%s'))
                        .then(function(response) { return response.json(); })
                        .then(function(data) {
                            if (data && data.length > 0) {
                                var lat = parseFloat(data[0].lat);
                                var lon = parseFloat(data[0].lon);
                                map.setView([lat, lon], 15);
                                marker.setLatLng([lat, lon]);
                                marker.bindPopup('<b>%s</b><br>' + data[0].display_name).openPopup();
                                map.invalidateSize();
                            }
                        })
                        .catch(function(err) { console.log('Geocoding error:', err); });

                    // Continuously fix size until stable
                    var resizeCount = 0;
                    var resizeInterval = setInterval(function() {
                        map.invalidateSize();
                        resizeCount++;
                        if (resizeCount > 20) clearInterval(resizeInterval);
                    }, 200);

                    // Also listen for any resize events
                    window.addEventListener('resize', function() { map.invalidateSize(); });
                    new ResizeObserver(function() { map.invalidateSize(); }).observe(document.getElementById('map'));
                </script>
            </body>
            </html>
            """.formatted(safeAddress, safeAddress, safeAddress);

        WebEngine engine = mapWebView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.loadContent(html);
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) lblEventName.getScene().getWindow();
        stage.close();
    }
}
