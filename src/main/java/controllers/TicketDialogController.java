package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * TicketDialogController  –  Métier Avancé : Popup Billet Électronique
 * ─────────────────────────────────────────────────────────────────────────────
 * Contrôleur du popup TicketDialog.fxml.
 * Reçoit les données du billet depuis ClientController, affiche le QR Code
 * en JavaFX, et met à jour le statut de l'envoi email en temps réel.
 */
public class TicketDialogController {

    // ── FXML bindings (matching fx:id in TicketDialog.fxml) ──────────────────
    @FXML private Label    lblEventName;
    @FXML private Label    lblParticipant;
    @FXML private Label    lblEmail;
    @FXML private Label    lblDate;
    @FXML private Label    lblHeure;
    @FXML private Label    lblAddress;
    @FXML private Label    lblTicketId;
    @FXML private Label    lblEmailStatus;
    @FXML private ImageView imgQRCode;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Populates all ticket fields and displays the QR Code image.
     * Called by ClientController right after the FXML is loaded.
     *
     * @param eventName    Name of the event
     * @param fullName     Participant full name (Prénom Nom)
     * @param email        Participant email address
     * @param dateStr      Event date as string
     * @param heure        Event time
     * @param address      Event address
     * @param userId       User ID
     * @param eventId      Event ID
     * @param qrFXImage    The JavaFX Image of the QR Code (already generated)
     */
    public void initData(String eventName, String fullName, String email,
                         String dateStr, String heure, String address,
                         int userId, int eventId,
                         Image qrFXImage) {

        lblEventName.setText(eventName);
        lblParticipant.setText(fullName);
        lblEmail.setText(email);
        lblDate.setText(dateStr);
        lblHeure.setText(heure);
        lblAddress.setText(address);
        lblTicketId.setText("EVENT-" + eventId + "-USER-" + userId);

        // Display the pre-generated QR Code in the ImageView
        imgQRCode.setImage(qrFXImage);
    }

    /**
     * Updates the email status label.
     * Safe to call from any thread (uses Platform.runLater internally).
     */
    public void setEmailStatus(boolean success) {
        Platform.runLater(() -> {
            if (success) {
                lblEmailStatus.setText("✅ Billet envoyé par email avec succès !");
                lblEmailStatus.setStyle("-fx-text-fill: #a8f0c6; -fx-font-size: 12px;");
            } else {
                lblEmailStatus.setText("⚠️ Échec d'envoi email. Vérifiez la configuration SMTP.");
                lblEmailStatus.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 12px;");
            }
        });
    }

    // ── Button handler ────────────────────────────────────────────────────────

    @FXML
    private void fermer() {
        Stage stage = (Stage) lblEventName.getScene().getWindow();
        stage.close();
    }
}
