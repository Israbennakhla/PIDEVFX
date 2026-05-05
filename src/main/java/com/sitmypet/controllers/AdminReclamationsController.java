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
import model.Reclamation;
import services.ServiceReclamation;

import java.io.IOException;

public class AdminReclamationsController {

    @FXML private TableView<Reclamation> tableReclamations;
    @FXML private TextField txtClient;
    @FXML private TextField txtSujet;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbStatut;

    private ServiceReclamation serviceReclamation;
    private ObservableList<Reclamation> reclamationsList;

    @FXML
    public void initialize() {
        serviceReclamation = new ServiceReclamation();
        
        cbStatut.setItems(FXCollections.observableArrayList("en_attente", "en_cours", "resolue"));
        
        chargerDonnees();

        tableReclamations.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                afficherDetails(newSelection);
            }
        });
    }

    private void chargerDonnees() {
        reclamationsList = FXCollections.observableArrayList(serviceReclamation.getAll());
        tableReclamations.setItems(reclamationsList);
    }

    private void afficherDetails(Reclamation r) {
        txtClient.setText(r.getNomClient() + " (" + r.getEmailClient() + ")");
        txtSujet.setText(r.getSujet());
        txtDescription.setText(r.getDescription());
        cbStatut.setValue(r.getStatut());
    }

    @FXML
    private void handleUpdateStatut() {
        Reclamation selection = tableReclamations.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une réclamation.");
            return;
        }
        
        String nouveauStatut = cbStatut.getValue();
        if (nouveauStatut == null) return;
        
        serviceReclamation.changerStatut(selection.getId(), nouveauStatut);
        chargerDonnees();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Le statut a été mis à jour.");
    }
    
    @FXML
    private void handleSupprimer() {
        Reclamation selection = tableReclamations.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une réclamation à supprimer.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setContentText("Voulez-vous vraiment supprimer cette réclamation ?");
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            serviceReclamation.delete(selection);
            txtClient.clear();
            txtSujet.clear();
            txtDescription.clear();
            cbStatut.setValue(null);
            chargerDonnees();
        }
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
