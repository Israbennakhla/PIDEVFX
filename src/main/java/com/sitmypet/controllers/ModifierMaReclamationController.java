package com.sitmypet.controllers;

import com.sitmypet.SessionContext;
import com.sitmypet.i18n.AppTexts;
import com.sitmypet.model.Reclamation;
import com.sitmypet.model.User;
import com.sitmypet.services.ServiceReclamation;
import com.sitmypet.services.ServiceReponse;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Modification d’une réclamation par son auteur (sans statut ni photo ; bloqué si réponse admin).
 */
public class ModifierMaReclamationController {

    @FXML private Label lblTitle;
    @FXML private Label lblLocked;
    @FXML private Label lblSujet;
    @FXML private TextField champSujet;
    @FXML private Label lblDesc;
    @FXML private TextArea champDescription;
    @FXML private Label lblPriorite;
    @FXML private ComboBox<String> champPriorite;
    @FXML private Label lblNom;
    @FXML private TextField champNom;
    @FXML private Label lblEmail;
    @FXML private TextField champEmail;
    @FXML private Button btnAnnuler;
    @FXML private Button btnEnregistrer;

    private final ServiceReclamation service = new ServiceReclamation();
    private final ServiceReponse serviceRep = new ServiceReponse();
    private Reclamation reclamation;

    @FXML
    public void initialize() {
        champPriorite.setItems(FXCollections.observableArrayList("basse", "moyenne", "haute"));
        refreshTexts();
    }

    public void setReclamation(Reclamation r) {
        this.reclamation = r;
        refreshTexts();

        champSujet.setText(r.getSujet() != null ? r.getSujet() : "");
        champDescription.setText(r.getDescription() != null ? r.getDescription() : "");
        champPriorite.setValue(r.getPriorite() != null ? r.getPriorite() : "basse");
        champNom.setText(r.getNomClient() != null ? r.getNomClient() : "");
        champEmail.setText(r.getEmailClient() != null ? r.getEmailClient() : "");

        boolean locked = serviceRep.existsForReclamation(r.getId());
        champSujet.setDisable(locked);
        champDescription.setDisable(locked);
        champPriorite.setDisable(locked);
        champNom.setDisable(locked);
        champEmail.setDisable(locked);
        btnEnregistrer.setDisable(locked);

        lblLocked.setVisible(locked);
        lblLocked.setManaged(locked);
        if (locked) lblLocked.setText(AppTexts.t("mes.rec.modify.locked"));
    }

    private void refreshTexts() {
        lblTitle.setText(AppTexts.t("mes.rec.modify.title"));
        lblSujet.setText(AppTexts.t("rec.sujet"));
        lblDesc.setText(AppTexts.t("rec.description"));
        lblPriorite.setText(AppTexts.t("rec.priorite"));
        lblNom.setText(AppTexts.t("rec.nom"));
        lblEmail.setText(AppTexts.t("rec.email"));
        btnAnnuler.setText(AppTexts.t("rec.cancel"));
        btnEnregistrer.setText(AppTexts.t("mes.rec.modify.save"));
    }

    @FXML
    private void enregistrer() {
        if (reclamation == null || SessionContext.getCurrentUser() == null) return;
        User me = SessionContext.getCurrentUser();

        reclamation.setSujet(champSujet.getText());
        reclamation.setDescription(champDescription.getText());
        reclamation.setPriorite(champPriorite.getValue());
        reclamation.setNomClient(champNom.getText());
        reclamation.setEmailClient(champEmail.getText());

        try {
            service.updateByOwner(reclamation, me.getId());
            Alert ok = new Alert(Alert.AlertType.INFORMATION, AppTexts.t("mes.rec.modify.ok"), ButtonType.OK);
            ok.showAndWait();
            fermer();
        } catch (RuntimeException ex) {
            Alert err = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            err.showAndWait();
        }
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        if (champSujet.getScene() != null && champSujet.getScene().getWindow() instanceof Stage s)
            s.close();
    }
}
