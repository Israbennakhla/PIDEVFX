package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

public class EvenementController {
    @FXML private TextField tfName;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfHeure;
    @FXML private TextField tfAddresse;
    @FXML private TextField tfDescription;
    @FXML private ListView<Evenement> listEvenements;
    
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbSort;

    private ServiceEvenement service = new ServiceEvenement();
    private Evenement evenementSelectionne = null;
    
    private ObservableList<Evenement> evenementsList = FXCollections.observableArrayList();
    private FilteredList<Evenement> filteredData;
    private SortedList<Evenement> sortedData;

    @FXML
    public void initialize() {
        if (cbSort != null) {
            cbSort.getItems().addAll("Nom (A-Z)", "Nom (Z-A)", "Date (Croissante)", "Date (Décroissante)");
            cbSort.getSelectionModel().selectFirst();
        }

        filteredData = new FilteredList<>(evenementsList, b -> true);
        
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(ev -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return ev.getName().toLowerCase().contains(lower) || 
                           ev.getAddresse().toLowerCase().contains(lower);
                });
            });
        }

        sortedData = new SortedList<>(filteredData);
        if (cbSort != null) {
            cbSort.valueProperty().addListener((obs, oldVal, newVal) -> actuateSort(newVal));
            actuateSort(cbSort.getValue());
        }
        
        listEvenements.setItems(sortedData);
        afficherEvenements();
    }
    
    private void actuateSort(String newVal) {
        if (newVal == null) return;
        sortedData.setComparator((e1, e2) -> {
            switch (newVal) {
                case "Nom (Z-A)": return e2.getName().compareToIgnoreCase(e1.getName());
                case "Date (Croissante)": 
                    if (e1.getDate() == null) return 1;
                    if (e2.getDate() == null) return -1;
                    return e1.getDate().compareTo(e2.getDate());
                case "Date (Décroissante)": 
                    if (e1.getDate() == null) return 1;
                    if (e2.getDate() == null) return -1;
                    return e2.getDate().compareTo(e1.getDate());
                case "Nom (A-Z)":
                default: 
                    return e1.getName().compareToIgnoreCase(e2.getName());
            }
        });
    }

    private void afficherEvenements() {
        evenementsList.setAll(service.getAll());
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