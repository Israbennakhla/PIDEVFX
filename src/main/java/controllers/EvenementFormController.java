package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;

import javafx.scene.control.*;
import model.Evenement;
import services.ServiceEvenement;
import java.sql.Date;

public class EvenementFormController {
    @FXML private Label lblTitle;
    @FXML private TextField tfName;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfHeure;
    @FXML private TextField tfAddresse;
    @FXML private TextField tfDescription;

    private ServiceEvenement service = new ServiceEvenement();
    private Evenement evenementEdition = null;

    @FXML
    public void initialize() {
        if (EvenementController.evenementSelectionneToEdit != null) {
            evenementEdition = EvenementController.evenementSelectionneToEdit;
            lblTitle.setText("📋 Modifier Événement");
            tfName.setText(evenementEdition.getName());
            if (evenementEdition.getDate() != null) dpDate.setValue(evenementEdition.getDate().toLocalDate());
            tfHeure.setText(evenementEdition.getHeure());
            tfAddresse.setText(evenementEdition.getAddresse());
            tfDescription.setText(evenementEdition.getDescription());
        } else {
            lblTitle.setText("📋 Ajouter Événement");
        }
    }

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
            retourListe(event);
        }
    }

    @FXML
    public void retourListe(ActionEvent event) {
        EvenementController.evenementSelectionneToEdit = null;
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/EvenementView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("PI-DEV : Gestion des Événements");
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @FXML
    public void ouvrirParticipants(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ParticipantView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("PI-DEV : Gestion des Participants");
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirClient(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ClientView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("PI-DEV : Espace Client (Front-Office)");
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
