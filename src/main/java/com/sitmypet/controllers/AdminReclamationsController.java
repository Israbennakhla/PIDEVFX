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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.sitmypet.model.Reclamation;
import com.sitmypet.services.ServiceReclamation;

import java.io.IOException;

public class AdminReclamationsController {

    @FXML private ListView<Reclamation> listReclamations;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cbTri;
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
        cbTri.setItems(FXCollections.observableArrayList("Date (Plus récentes)", "Date (Plus anciennes)", "Priorité (Haute d'abord)", "Statut (En attente d'abord)"));
        
        listReclamations.setCellFactory(param -> new ListCell<Reclamation>() {
            @Override
            protected void updateItem(Reclamation item, boolean empty) {
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
                    Label nameLbl = new Label(item.getSujet());
                    nameLbl.getStyleClass().add("user-name-label");
                    Label clientLbl = new Label("\u2022 " + item.getNomClient() + " - " + item.getEmailClient());
                    clientLbl.getStyleClass().add("user-detail-label");
                    Label dateLbl = new Label("\u2022 " + (item.getDateReclamation() != null ? item.getDateReclamation().toString() : "N/A"));
                    dateLbl.getStyleClass().add("user-detail-label");
                    identity.getChildren().addAll(nameLbl, clientLbl, dateLbl);
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    VBox statusBox = new VBox(5);
                    statusBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    
                    String statut = item.getStatut();
                    Label statLbl = new Label(statut != null ? statut.toUpperCase() : "N/A");
                    if ("resolue".equalsIgnoreCase(statut)) {
                        statLbl.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else if ("en_cours".equalsIgnoreCase(statut)) {
                        statLbl.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        statLbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                    
                    Label prioLbl = new Label("Prio: " + item.getPriorite());
                    prioLbl.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
                    
                    statusBox.getChildren().addAll(statLbl, prioLbl);
                    
                    HBox actionBox = new HBox(10);
                    actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    actionBox.setPadding(new javafx.geometry.Insets(0, 0, 0, 15));
                    
                    Button btnRepondre = new Button("R\u00E9pondre");
                    btnRepondre.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
                    btnRepondre.setOnAction(e -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/AjouterReponse.fxml"));
                            Parent r = loader.load();
                            com.sitmypet.controllers.AjouterReponse ctrl = loader.getController();
                            ctrl.setReclamationId(item.getId());
                            Stage s = new Stage();
                            s.setScene(new Scene(r));
                            s.showAndWait();
                            chargerDonnees();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                    
                    Button btnSupprimer = new Button("Supprimer");
                    btnSupprimer.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    btnSupprimer.setOnAction(e -> {
                        listReclamations.getSelectionModel().select(item);
                        handleSupprimer();
                    });
                    
                    actionBox.getChildren().addAll(btnRepondre, btnSupprimer);
                    
                    card.getChildren().addAll(identity, spacer, statusBox, new Region() {{ setMinWidth(15); }}, actionBox);
                    
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });
        
        chargerDonnees();

        listReclamations.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                afficherDetails(newSelection);
            }
        });
    }

    private void chargerDonnees() {
        reclamationsList = FXCollections.observableArrayList(serviceReclamation.getAll());
        
        javafx.collections.transformation.FilteredList<Reclamation> filteredData = new javafx.collections.transformation.FilteredList<>(reclamationsList, b -> true);
        
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(rec -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (rec.getSujet() != null && rec.getSujet().toLowerCase().contains(lowerCaseFilter)) return true;
                if (rec.getNomClient() != null && rec.getNomClient().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        
        javafx.collections.transformation.SortedList<Reclamation> sortedData = new javafx.collections.transformation.SortedList<>(filteredData);
        cbTri.valueProperty().addListener((obs, oldVal, newVal) -> {
            sortedData.setComparator((r1, r2) -> {
                if (newVal == null) return 0;
                switch (newVal) {
                    case "Date (Plus récentes)": 
                        if(r1.getDateReclamation() == null || r2.getDateReclamation() == null) return 0;
                        return r2.getDateReclamation().compareTo(r1.getDateReclamation());
                    case "Date (Plus anciennes)": 
                        if(r1.getDateReclamation() == null || r2.getDateReclamation() == null) return 0;
                        return r1.getDateReclamation().compareTo(r2.getDateReclamation());
                    case "Priorité (Haute d'abord)":
                        // Assuming priorities are like "Haute", "Moyenne", "Basse"
                        return getPriorityValue(r2.getPriorite()) - getPriorityValue(r1.getPriorite());
                    case "Statut (En attente d'abord)":
                        return getStatusValue(r1.getStatut()) - getStatusValue(r2.getStatut());
                    default: return 0;
                }
            });
        });
        
        listReclamations.setItems(sortedData);
    }
    
    private int getPriorityValue(String prio) {
        if (prio == null) return 0;
        if (prio.equalsIgnoreCase("haute")) return 3;
        if (prio.equalsIgnoreCase("moyenne")) return 2;
        if (prio.equalsIgnoreCase("basse")) return 1;
        return 0;
    }
    
    private int getStatusValue(String status) {
        if (status == null) return 3;
        if (status.equalsIgnoreCase("en_attente")) return 1;
        if (status.equalsIgnoreCase("en_cours")) return 2;
        if (status.equalsIgnoreCase("resolue")) return 3;
        return 4;
    }

    private void afficherDetails(Reclamation r) {
        txtClient.setText(r.getNomClient() + " (" + r.getEmailClient() + ")");
        txtSujet.setText(r.getSujet());
        txtDescription.setText(r.getDescription());
        cbStatut.setValue(r.getStatut());
    }

    @FXML
    private void handleUpdateStatut() {
        Reclamation selection = listReclamations.getSelectionModel().getSelectedItem();
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
        Reclamation selection = listReclamations.getSelectionModel().getSelectedItem();
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
        navigate(event, "/com/sitmypet/fxml/Dashboard.fxml");
    }

    @FXML
    private void handleUsers(ActionEvent event) {
        navigate(event, "/com/sitmypet/fxml/AfficherUser.fxml");
    }

    @FXML
    private void handleAnimaux(ActionEvent event) {
        navigate(event, "/com/sitmypet/fxml/AdminAnimaux.fxml");
    }

    @FXML
    private void handleAnnonces(ActionEvent event) {
        navigate(event, "/com/sitmypet/fxml/AdminAnnonces.fxml");
    }

    @FXML
    private void handleEvenements(ActionEvent event) {
        navigate(event, "/com/sitmypet/fxml/AdminEvenements.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        navigate(event, "/com/sitmypet/fxml/Login.fxml");
    }

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
