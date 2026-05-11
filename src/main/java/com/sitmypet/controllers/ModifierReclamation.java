package com.sitmypet.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.sitmypet.model.Reclamation;
import com.sitmypet.services.ServiceReclamation;
import com.sitmypet.services.ServiceReponse;

import java.net.URL;
import java.util.ResourceBundle;

public class ModifierReclamation implements Initializable {

    @FXML private TextField        champSujet;
    @FXML private TextArea         champDescription;
    @FXML private ComboBox<String> champPriorite;
    @FXML private ComboBox<String> champStatut;
    @FXML private TextField        champNom;
    @FXML private TextField        champEmail;

    @FXML private Label errSujet;
    @FXML private Label errDescription;
    @FXML private Label errPriorite;
    @FXML private Label errNom;
    @FXML private Label errEmail;

    private final ServiceReclamation service = new ServiceReclamation();
    private Reclamation reclamation;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        champPriorite.setItems(FXCollections.observableArrayList(
                "basse", "moyenne", "haute"
        ));
        champStatut.setItems(FXCollections.observableArrayList(
                "en_attente", "en_cours", "resolue"
        ));
        effacerErreurs();
    }

    // Appelé depuis AfficherReclamations pour pré-remplir
    public void setReclamation(Reclamation r) {
        this.reclamation = r;

        // Remplir les champs
        champSujet.setText(r.getSujet());
        champDescription.setText(r.getDescription());
        champPriorite.setValue(r.getPriorite());
        champStatut.setValue(r.getStatut());
        champNom.setText(r.getNomClient());
        champEmail.setText(r.getEmailClient());

        // ── Règle métier : verrouillage si une réponse existe ────────────────────
        ServiceReponse serviceReponse = new ServiceReponse();
        if (serviceReponse.existsForReclamation(r.getId())) {

            // Désactiver tous les champs de saisie
            champSujet.setDisable(true);
            champDescription.setDisable(true);
            champPriorite.setDisable(true);
            champStatut.setDisable(true);
            champNom.setDisable(true);
            champEmail.setDisable(true);

            // Afficher le message dans errSujet (label visible en haut)
            errSujet.setText("🔒 Réclamation clôturée — aucune modification possible.");
            errSujet.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");

            // Vider les autres labels d'erreur pour ne pas polluer l'affichage
            errDescription.setText("");
            errPriorite.setText("");
            errNom.setText("");
            errEmail.setText("");
        }
    }

    @FXML
    private void enregistrer() {
        effacerErreurs();
        if (!validerChamps()) return;

        reclamation.setSujet(champSujet.getText().trim());
        reclamation.setDescription(champDescription.getText().trim());
        reclamation.setPriorite(champPriorite.getValue());
        reclamation.setStatut(champStatut.getValue());
        reclamation.setNomClient(champNom.getText().trim());
        reclamation.setEmailClient(champEmail.getText().trim());

        // Validation métier du service
        String validation = service.valider(reclamation);
        if (!validation.equals("OK")) {
            new Alert(Alert.AlertType.WARNING, validation).showAndWait();
            return;
        }

        service.update(reclamation);
        new Alert(Alert.AlertType.INFORMATION, " Réclamation modifiée avec succès !")
                .showAndWait();
        fermer();
    }

    // ── Validation champ par champ avec messages inline ──
    private boolean validerChamps() {
        boolean ok = true;

        // Sujet
        if (champSujet.getText().trim().isEmpty()) {
            errSujet.setText("! Le sujet est obligatoire.");
            ok = false;
        } else if (champSujet.getText().trim().length() < 5) {
            errSujet.setText("! Minimum 5 caractères.");
            ok = false;
        }

        // Description
        if (champDescription.getText().trim().isEmpty()) {
            errDescription.setText("! La description est obligatoire.");
            ok = false;
        } else if (champDescription.getText().trim().length() < 10) {
            errDescription.setText("! Minimum 10 caractères.");
            ok = false;
        }

        // Priorité
        if (champPriorite.getValue() == null) {
            errPriorite.setText("! Choisissez une priorité.");
            ok = false;
        }

        // Nom
        if (champNom.getText().trim().isEmpty()) {
            errNom.setText("! Le nom est obligatoire.");
            ok = false;
        }

        // Email
        if (champEmail.getText().trim().isEmpty()) {
            errEmail.setText("! L'email est obligatoire.");
            ok = false;
        } else if (!champEmail.getText().contains("@")) {
            errEmail.setText("! Email invalide (manque @).");
            ok = false;
        } else if (!champEmail.getText().contains(".")) {
            errEmail.setText("! Email invalide (manque domaine).");
            ok = false;
        }

        return ok;
    }

    private void effacerErreurs() {
        errSujet.setText("");
        errDescription.setText("");
        errPriorite.setText("");
        errNom.setText("");
        errEmail.setText("");
    }

    @FXML
    private void annuler() { fermer(); }

    private void fermer() {
        ((Stage) champSujet.getScene().getWindow()).close();
    }
}
