package com.sitmypet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.sitmypet.model.Reponse;
import com.sitmypet.services.ServiceReponse;

import java.time.LocalDateTime;

public class AjouterReponse {

    @FXML private TextField champAuteur;
    @FXML private TextArea  champContenu;
    @FXML private Label     errAuteur;
    @FXML private Label     errContenu;

    private final ServiceReponse serviceReponse = new ServiceReponse();
    private int reclamationId;

    public void setReclamationId(int id) {
        this.reclamationId = id;
    }

    @FXML
    private void enregistrer() {
        // ── Réinitialiser les erreurs ─────────────────────────────────────────
        errAuteur.setText("");
        errContenu.setText("");

        // ── Validation ────────────────────────────────────────────────────────
        boolean ok = true;

        String auteur  = champAuteur.getText().trim();
        String contenu = champContenu.getText().trim();

        if (auteur.isEmpty()) {
            errAuteur.setText("⚠ Le nom de l'auteur est obligatoire.");
            errAuteur.setStyle("-fx-text-fill: #e74c3c;");
            ok = false;
        }

        if (contenu.isEmpty()) {
            errContenu.setText("⚠ Le contenu de la réponse est obligatoire.");
            errContenu.setStyle("-fx-text-fill: #e74c3c;");
            ok = false;
        }

        if (!ok) return;

        // ── Construction + envoi ──────────────────────────────────────────────
        Reponse rep = new Reponse(
                contenu,
                LocalDateTime.now(),
                auteur,
                reclamationId
        );

        try {
            serviceReponse.add(rep);  // ← met aussi à jour statut → 'resolue'

            // Succès
            errContenu.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            errContenu.setText(" Réponse envoyée.");
            champContenu.clear();
            champAuteur.clear();

            new Alert(Alert.AlertType.INFORMATION,
                    " Réponse ajoutée avec succès !").showAndWait();
            fermer();

        } catch (IllegalArgumentException e) {
            errContenu.setStyle("-fx-text-fill: #e74c3c;");
            errContenu.setText(e.getMessage());
        } catch (RuntimeException e) {
            errContenu.setStyle("-fx-text-fill: #e74c3c;");
            errContenu.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void annuler() { fermer(); }

    private void fermer() {
        ((Stage) champAuteur.getScene().getWindow()).close();
    }
}
