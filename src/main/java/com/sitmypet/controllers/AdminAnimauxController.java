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
import model.Pet;
import services.ServicePet;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

public class AdminAnimauxController {

    @FXML private TableView<Pet> tableAnimaux;
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField txtRace;
    @FXML private ComboBox<String> cbGenre;
    @FXML private TextField txtPoids;
    @FXML private DatePicker dpNaissance;
    @FXML private TextArea txtDescription;
    
    @FXML private CheckBox chkVaccine;
    @FXML private CheckBox chkMaladie;
    @FXML private CheckBox chkDossier;
    @FXML private CheckBox chkCritique;

    private ServicePet servicePet;
    private ObservableList<Pet> petsList;

    @FXML
    public void initialize() {
        servicePet = new ServicePet();
        
        cbType.setItems(FXCollections.observableArrayList("Chien", "Chat", "Oiseau", "Rongeur", "Autre"));
        cbGenre.setItems(FXCollections.observableArrayList("Mâle", "Femelle"));
        
        chargerDonnees();

        // Sélection dans la table
        tableAnimaux.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirFormulaire(newSelection);
            }
        });
    }

    private void chargerDonnees() {
        petsList = FXCollections.observableArrayList(servicePet.getAll());
        tableAnimaux.setItems(petsList);
    }

    private void remplirFormulaire(Pet p) {
        txtNom.setText(p.getName());
        cbType.setValue(p.getTypePet());
        txtRace.setText(p.getBreed());
        cbGenre.setValue(p.getGender());
        txtPoids.setText(String.valueOf(p.getWeight()));
        if (p.getBirthDate() != null) {
            dpNaissance.setValue(p.getBirthDate().toLocalDate());
        } else {
            dpNaissance.setValue(null);
        }
        txtDescription.setText(p.getDescription());
        
        chkVaccine.setSelected(p.isVaccinated());
        chkMaladie.setSelected(p.isHasContagiousDisease());
        chkDossier.setSelected(p.isHasMedicalRecord());
        chkCritique.setSelected(p.isHasCriticalCondition());
    }

    @FXML
    private void handleClear() {
        txtNom.clear();
        cbType.setValue(null);
        txtRace.clear();
        cbGenre.setValue(null);
        txtPoids.clear();
        dpNaissance.setValue(null);
        txtDescription.clear();
        chkVaccine.setSelected(false);
        chkMaladie.setSelected(false);
        chkDossier.setSelected(false);
        chkCritique.setSelected(false);
        tableAnimaux.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAjouter() {
        try {
            Pet p = lireFormulaire();
            servicePet.add(p);
            chargerDonnees();
            handleClear();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Animal ajouté avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Pet selection = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un animal à modifier.");
            return;
        }
        try {
            Pet p = lireFormulaire();
            p.setId(selection.getId());
            p.setImageName(selection.getImageName()); // Keep existing image
            servicePet.update(p);
            chargerDonnees();
            handleClear();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Animal modifié avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Pet selection = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un animal à supprimer.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setContentText("Voulez-vous vraiment supprimer cet animal ?");
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            servicePet.delete(selection);
            chargerDonnees();
            handleClear();
        }
    }

    private Pet lireFormulaire() {
        if (txtNom.getText().isEmpty() || cbType.getValue() == null || dpNaissance.getValue() == null) {
            throw new IllegalArgumentException("Veuillez remplir au moins le nom, le type et la date de naissance.");
        }
        
        float poids = 0;
        if (!txtPoids.getText().isEmpty()) {
            try {
                poids = Float.parseFloat(txtPoids.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Le poids doit être un nombre valide.");
            }
        }
        
        return new Pet(
            txtNom.getText(),
            Date.valueOf(dpNaissance.getValue()),
            cbType.getValue(),
            txtRace.getText() != null ? txtRace.getText() : "",
            poids,
            txtDescription.getText() != null ? txtDescription.getText() : "",
            cbGenre.getValue() != null ? cbGenre.getValue() : "",
            chkMaladie.isSelected(),
            chkDossier.isSelected(),
            chkCritique.isSelected(),
            chkVaccine.isSelected(),
            "default.png"
        );
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
