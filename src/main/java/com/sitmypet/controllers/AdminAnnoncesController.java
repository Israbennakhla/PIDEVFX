package com.sitmypet.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.sitmypet.model.Announcement;
import com.sitmypet.services.ServiceAnnouncement;

import java.io.IOException;

public class AdminAnnoncesController {

    @FXML private ListView<Announcement> listAnnonces;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cbTri;

    private ServiceAnnouncement serviceAnnouncement;
    private ObservableList<Announcement> annoncesList;

    @FXML
    public void initialize() {
        serviceAnnouncement = new ServiceAnnouncement();
        cbTri.setItems(FXCollections.observableArrayList("Date Croissante", "Date Décroissante", "Rémunération Min", "Rémunération Max"));
        
        listAnnonces.setCellFactory(param -> new ListCell<Announcement>() {
            @Override
            protected void updateItem(Announcement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox card = new HBox(15);
                    card.getStyleClass().add("user-card");
                    card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    VBox identity = new VBox(5);
                    Label nameLbl = new Label("\u2022 Annonce: " + item.getCareType());
                    nameLbl.getStyleClass().add("user-name-label");
                    Label detailsLbl = new Label("\u2022 " + item.getAddress() + " | Du " + item.getDateDebut() + " au " + item.getDateFin());
                    detailsLbl.getStyleClass().add("user-detail-label");
                    Label priceLbl = new Label("\u2022 " + item.getRemunerationMin() + " TND - " + item.getRemunerationMax() + " TND");
                    priceLbl.getStyleClass().add("user-detail-label");
                    
                    identity.getChildren().addAll(nameLbl, detailsLbl, priceLbl);
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    HBox actionBox = new HBox(10);
                    actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    
                    Button btnSupprimer = new Button("Supprimer");
                    btnSupprimer.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    btnSupprimer.setOnAction(e -> {
                        listAnnonces.getSelectionModel().select(item);
                        handleSupprimer();
                    });
                    
                    actionBox.getChildren().add(btnSupprimer);
                    
                    card.getChildren().addAll(identity, spacer, actionBox);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });

        chargerDonnees();
    }

    private void chargerDonnees() {
        annoncesList = FXCollections.observableArrayList(serviceAnnouncement.getAll());
        
        FilteredList<Announcement> filteredData = new FilteredList<>(annoncesList, b -> true);
        
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(annonce -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (annonce.getCareType() != null && annonce.getCareType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (annonce.getAddress() != null && annonce.getAddress().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        
        SortedList<Announcement> sortedData = new SortedList<>(filteredData);
        cbTri.valueProperty().addListener((obs, oldVal, newVal) -> {
            sortedData.setComparator((a1, a2) -> {
                if (newVal == null) return 0;
                if (newVal.equals("Date Croissante")) {
                    return a1.getDateDebut().compareTo(a2.getDateDebut());
                } else if (newVal.equals("Date Décroissante")) {
                    return a2.getDateDebut().compareTo(a1.getDateDebut());
                } else if (newVal.equals("Rémunération Min")) {
                    return Float.compare(a1.getRemunerationMin(), a2.getRemunerationMin());
                } else if (newVal.equals("Rémunération Max")) {
                    return Float.compare(a2.getRemunerationMax(), a1.getRemunerationMax());
                }
                return 0;
            });
        });
        
        listAnnonces.setItems(sortedData);
    }

    private void handleSupprimer() {
        Announcement selection = listAnnonces.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une annonce à supprimer.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setContentText("Voulez-vous vraiment supprimer cette annonce ?");
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            serviceAnnouncement.delete(selection);
            chargerDonnees();
        }
    }

    @FXML private void handleRetour(ActionEvent event) { navigate(event, "/com/sitmypet/fxml/Dashboard.fxml"); }
    @FXML private void handleUsers(ActionEvent event) { navigate(event, "/com/sitmypet/fxml/AfficherUser.fxml"); }
    @FXML private void handleAnimaux(ActionEvent event) { navigate(event, "/com/sitmypet/fxml/AdminAnimaux.fxml"); }
    @FXML private void handleEvenements(ActionEvent event) { navigate(event, "/com/sitmypet/fxml/AdminEvenements.fxml"); }
    @FXML private void handleReclamations(ActionEvent event) { navigate(event, "/com/sitmypet/fxml/AdminReclamations.fxml"); }
    @FXML private void handleAnnonces(ActionEvent event) { navigate(event, "/com/sitmypet/fxml/AdminAnnonces.fxml"); }
    @FXML private void handleLogout(ActionEvent event) { navigate(event, "/com/sitmypet/fxml/Login.fxml"); }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
            stage.setMaximized(true);
            stage.centerOnScreen();
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
