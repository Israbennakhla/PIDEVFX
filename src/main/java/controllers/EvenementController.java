package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;


import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Evenement;
import services.ServiceEvenement;
import java.sql.Date;

public class EvenementController {
    @FXML private TextField tfName;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfHeure;
    @FXML private TextField tfAddresse;
    @FXML private TextField tfDescription;
    @FXML private ListView<Evenement> listEvenements;
    private ServiceEvenement service = new ServiceEvenement();
    private Evenement evenementSelectionne = null;
    @FXML
    public void initialize() {
        afficherEvenements();
    }
    private void afficherEvenements() {
        ObservableList<Evenement> list = FXCollections.observableArrayList(service.getAll());
        listEvenements.setItems(list);
    }
    @FXML
    public void ajouterEvenement(ActionEvent event) {
        if (champsValides()) {
            Evenement e = new Evenement(0, tfName.getText(), Date.valueOf(dpDate.getValue()), tfHeure.getText(), tfAddresse.getText(), tfDescription.getText());
            service.add(e);
            afficherEvenements();
            viderFormulaire();
        }
    }
    @FXML
    public void modifierEvenement(ActionEvent event) {
        if (evenementSelectionne != null && champsValides()) {
            evenementSelectionne.setName(tfName.getText());
            evenementSelectionne.setDate(Date.valueOf(dpDate.getValue()));
            evenementSelectionne.setHeure(tfHeure.getText());
            evenementSelectionne.setAddresse(tfAddresse.getText());
            evenementSelectionne.setDescription(tfDescription.getText());
            service.update(evenementSelectionne);
            afficherEvenements();
            viderFormulaire();
        }
    }
    @FXML
    public void supprimerEvenement(ActionEvent event) {
        evenementSelectionne = listEvenements.getSelectionModel().getSelectedItem();
        if (evenementSelectionne != null) {
            service.delete(evenementSelectionne);
            afficherEvenements();
            viderFormulaire();
        }
    }
    @FXML
    public void remplirFormulaire() {
        evenementSelectionne = listEvenements.getSelectionModel().getSelectedItem();
        if (evenementSelectionne != null) {
            tfName.setText(evenementSelectionne.getName());
            if (evenementSelectionne.getDate() != null) dpDate.setValue(evenementSelectionne.getDate().toLocalDate());
            tfHeure.setText(evenementSelectionne.getHeure());
            tfAddresse.setText(evenementSelectionne.getAddresse());
            tfDescription.setText(evenementSelectionne.getDescription());
        }
    }
    @FXML
    public void viderFormulaire() {
        tfName.clear(); dpDate.setValue(null); tfHeure.clear(); tfAddresse.clear(); tfDescription.clear();
        evenementSelectionne = null;
    }
    private boolean champsValides() {
        return !tfName.getText().isEmpty() && dpDate.getValue() != null;
    }
}