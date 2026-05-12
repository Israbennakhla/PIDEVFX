package com.sitmypet.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.sitmypet.model.Evenement;
import com.sitmypet.model.EventParticipant;
import com.sitmypet.model.User;
import com.sitmypet.services.ServiceEventParticipant;
import com.sitmypet.services.EmailService;
import com.sitmypet.utils.QRCodeGenerator;

import java.io.IOException;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * EventDetailOverlayController  –  Client Event Detail Popup with Map
 * ─────────────────────────────────────────────────────────────────────────────
 * Displays full event details in a styled overlay.
 * Includes a Participer / Se désinscrire button and an embedded map.
 */
public class EventDetailOverlayController {

    @FXML private Label lblEventName;
    @FXML private Label lblDate;
    @FXML private Label lblHeure;
    @FXML private Label lblAddress;
    @FXML private Label lblDescription;
    @FXML private WebView mapWebView;
    @FXML private Button btnAction;

    private final ServiceEventParticipant serviceParticipant = new ServiceEventParticipant();

    private Evenement currentEvent;
    private User currentUser;
    private boolean enrolled = false;
    private boolean actionTaken = false;

    /** Returns true if the user registered/unregistered (so the grid should refresh). */
    public boolean isActionTaken() { return actionTaken; }

    /**
     * Populates the overlay with event data and loads the map.
     */
    public void initData(Evenement ev, User user) {
        this.currentEvent = ev;
        this.currentUser = user;

        lblEventName.setText(ev.getName());
        lblDate.setText(ev.getDate() != null ? ev.getDate().toString() : "N/A");
        lblHeure.setText(ev.getHeure() != null ? ev.getHeure() : "N/A");
        lblAddress.setText(ev.getAddresse() != null ? ev.getAddresse() : "N/A");
        lblDescription.setText(ev.getDescription() != null ? ev.getDescription() : "");

        // Set initial button state
        updateButtonState();

        // Load the map after the WebView is ready
        Platform.runLater(() -> loadMap(ev.getAddresse()));
    }

    /**
     * Updates the button text and style based on enrollment status.
     */
    private void updateButtonState() {
        enrolled = currentUser != null && serviceParticipant.isEnrolled(currentEvent.getId(), currentUser.getId());
        if (enrolled) {
            btnAction.setText("Se désinscrire");
            btnAction.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 13px; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 28; -fx-cursor: hand;");
        } else {
            btnAction.setText("✔ Participer");
            btnAction.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-size: 13px; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 28; -fx-cursor: hand;");
        }
    }

    /**
     * Handles the Participer / Se désinscrire button click.
     * On registration: shows the TicketDialog with QR code and sends the email.
     */
    @FXML
    private void handleParticipation() {
        if (currentUser == null) return;
        EventParticipant ep = new EventParticipant(currentEvent.getId(), currentUser.getId());

        if (enrolled) {
            // ── Désinscription ────────────────────────────────────────────
            serviceParticipant.delete(ep);
        } else {
            // ── Inscription + Ticket Dialog + Email ──────────────────────
            serviceParticipant.add(ep);
            showTicketDialog();
        }
        actionTaken = true;
        updateButtonState();
    }

    /**
     * Opens the TicketDialog with QR code and sends the confirmation email.
     */
    private void showTicketDialog() {
        String fullName = currentUser.getPrenom() + " " + currentUser.getNom();
        String dateStr  = currentEvent.getDate()  != null ? currentEvent.getDate().toString() : "N/A";
        String heureStr = currentEvent.getHeure() != null ? currentEvent.getHeure()           : "N/A";

        // Build QR code with ticket JSON
        String ticketJson = QRCodeGenerator.buildTicketJson(
                currentUser.getId(), currentUser.getNom(), currentUser.getPrenom(),
                currentUser.getEmail(), currentEvent.getId(), currentEvent.getName(),
                dateStr, heureStr);

        Image qrFXImage = null;
        java.awt.image.BufferedImage qrBufferedImage = null;
        try {
            qrBufferedImage = QRCodeGenerator.generateQRCodeImage(ticketJson, 300, 300);
            qrFXImage       = QRCodeGenerator.generateQRCodeFXImage(ticketJson, 300, 300);
        } catch (Exception ex) {
            System.err.println("❌ Erreur génération QR Code : " + ex.getMessage());
        }

        // Open TicketDialog
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/TicketDialog.fxml"));
            Parent root = loader.load();

            TicketDialogController ticketCtrl = loader.getController();
            ticketCtrl.initData(
                    currentEvent.getName(), fullName, currentUser.getEmail(),
                    dateStr, heureStr, currentEvent.getAddresse(),
                    currentUser.getId(), currentEvent.getId(), qrFXImage);

            Stage ticketStage = new Stage();
            ticketStage.initModality(Modality.APPLICATION_MODAL);
            ticketStage.initStyle(StageStyle.TRANSPARENT);
            ticketStage.setTitle("🎟️ Billet Électronique");
            ticketStage.setScene(new Scene(root));
            ticketStage.getScene().setFill(null);
            ticketStage.show();

            // Send email asynchronously
            final java.awt.image.BufferedImage finalQrImage = qrBufferedImage;
            if (finalQrImage != null) {
                EmailService.sendTicketEmail(
                        currentUser.getEmail(), fullName,
                        currentEvent.getName(), dateStr, heureStr,
                        currentEvent.getAddresse(),
                        currentUser.getId(), currentEvent.getId(), finalQrImage)
                    .thenAccept(success -> ticketCtrl.setEmailStatus(success));
            } else {
                ticketCtrl.setEmailStatus(false);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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

        // Force resize AFTER rendering is complete
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                Platform.runLater(() -> {
                    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> {
                        try {
                            engine.executeScript("map.invalidateSize();");
                        } catch (Exception ignored) {}
                    });
                });
            }
        });

        engine.loadContent(html);
    }

    /**
     * Forces the map to recalculate its size.
     * Must be called AFTER the Stage is visible on screen.
     */
    public void forceMapResize() {
        if (mapWebView != null) {
            Platform.runLater(() -> {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    try {
                        mapWebView.getEngine().executeScript("map.invalidateSize();");
                    } catch (Exception ignored) {}
                });
            });
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) lblEventName.getScene().getWindow();
        stage.close();
    }
}

