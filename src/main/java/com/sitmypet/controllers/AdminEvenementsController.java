package com.sitmypet.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Evenement;
import services.ServiceEvenement;

import java.io.IOException;
import java.sql.Date;

public class AdminEvenementsController {

    @FXML private TableView<Evenement> tableEvenements;
    @FXML private TextField txtNom;
    @FXML private DatePicker dpDate;
    @FXML private TextField txtHeure;
    @FXML private TextField txtAdresse;
    @FXML private TextArea txtDescription;

    private ServiceEvenement serviceEvenement;
    private ObservableList<Evenement> evenementsList;

    @FXML
    public void initialize() {
        serviceEvenement = new ServiceEvenement();
        chargerDonnees();

        tableEvenements.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirFormulaire(newSelection);
            }
        });
    }

    private void chargerDonnees() {
        evenementsList = FXCollections.observableArrayList(serviceEvenement.getAll());
        tableEvenements.setItems(evenementsList);
    }

    private void remplirFormulaire(Evenement e) {
        txtNom.setText(e.getName());
        if (e.getDate() != null) {
            dpDate.setValue(e.getDate().toLocalDate());
        } else {
            dpDate.setValue(null);
        }
        txtHeure.setText(e.getHeure());
        txtAdresse.setText(e.getAddresse());
        txtDescription.setText(e.getDescription());
    }

    @FXML
    private void handleClear() {
        txtNom.clear();
        dpDate.setValue(null);
        txtHeure.clear();
        txtAdresse.clear();
        txtDescription.clear();
        tableEvenements.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAjouter() {
        try {
            Evenement e = lireFormulaire();
            serviceEvenement.add(e);
            chargerDonnees();
            handleClear();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès !");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Evenement selection = tableEvenements.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un événement à modifier.");
            return;
        }
        try {
            Evenement e = lireFormulaire();
            e.setId(selection.getId());
            serviceEvenement.update(e);
            chargerDonnees();
            handleClear();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement modifié avec succès !");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Evenement selection = tableEvenements.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un événement à supprimer.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setContentText("Voulez-vous vraiment supprimer cet événement ?");
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            serviceEvenement.delete(selection);
            chargerDonnees();
            handleClear();
        }
    }

    private Evenement lireFormulaire() {
        if (txtNom.getText().isEmpty() || dpDate.getValue() == null || txtHeure.getText().isEmpty() || txtAdresse.getText().isEmpty()) {
            throw new IllegalArgumentException("Veuillez remplir tous les champs obligatoires (Nom, Date, Heure, Adresse).");
        }
        
        Evenement e = new Evenement();
        e.setName(txtNom.getText());
        e.setDate(Date.valueOf(dpDate.getValue()));
        e.setHeure(txtHeure.getText());
        e.setAddresse(txtAdresse.getText());
        e.setDescription(txtDescription.getText() != null ? txtDescription.getText() : "");
        
        return e;
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
