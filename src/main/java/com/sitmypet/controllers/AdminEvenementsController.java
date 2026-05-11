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
import com.sitmypet.model.Evenement;
import com.sitmypet.services.ServiceEvenement;

import java.io.IOException;
import java.sql.Date;

public class AdminEvenementsController {

    @FXML private ListView<Evenement> listEvenements;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cbTri;
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
        
        cbTri.setItems(FXCollections.observableArrayList("Nom (A-Z)", "Nom (Z-A)", "Date (Croissante)", "Date (Décroissante)"));
        
        listEvenements.setCellFactory(param -> new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement item, boolean empty) {
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
                    Label nameLbl = new Label(item.getName());
                    nameLbl.getStyleClass().add("user-name-label");
                    Label dateLbl = new Label("\u2022 " + item.getDate().toString() + " \u00E0 " + item.getHeure());
                    dateLbl.getStyleClass().add("user-detail-label");
                    Label addrLbl = new Label("\u2022 " + item.getAddresse());
                    addrLbl.getStyleClass().add("user-detail-label");
                    identity.getChildren().addAll(nameLbl, dateLbl, addrLbl);
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    HBox actionBox = new HBox(10);
                    actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    actionBox.setPadding(new javafx.geometry.Insets(0, 0, 0, 15));
                    
                    Button btnModifier = new Button("Modifier");
                    btnModifier.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
                    btnModifier.setOnAction(e -> {
                        listEvenements.getSelectionModel().select(item);
                        handleModifierBtnClicked();
                    });
                    
                    Button btnSupprimer = new Button("Supprimer");
                    btnSupprimer.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    btnSupprimer.setOnAction(e -> {
                        listEvenements.getSelectionModel().select(item);
                        handleSupprimer();
                    });
                    
                    actionBox.getChildren().addAll(btnModifier, btnSupprimer);
                    
                    card.getChildren().addAll(identity, spacer, actionBox);
                    
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });

        chargerDonnees();

        listEvenements.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirFormulaire(newSelection);
            }
        });
    }

    private void chargerDonnees() {
        evenementsList = FXCollections.observableArrayList(serviceEvenement.getAll());
        
        javafx.collections.transformation.FilteredList<Evenement> filteredData = new javafx.collections.transformation.FilteredList<>(evenementsList, b -> true);
        
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(evt -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (evt.getName() != null && evt.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                if (evt.getAddresse() != null && evt.getAddresse().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        
        javafx.collections.transformation.SortedList<Evenement> sortedData = new javafx.collections.transformation.SortedList<>(filteredData);
        cbTri.valueProperty().addListener((obs, oldVal, newVal) -> {
            sortedData.setComparator((e1, e2) -> {
                if (newVal == null) return 0;
                switch (newVal) {
                    case "Nom (A-Z)": return e1.getName().compareToIgnoreCase(e2.getName());
                    case "Nom (Z-A)": return e2.getName().compareToIgnoreCase(e1.getName());
                    case "Date (Croissante)": return e1.getDate().compareTo(e2.getDate());
                    case "Date (Décroissante)": return e2.getDate().compareTo(e1.getDate());
                    default: return 0;
                }
            });
        });
        
        listEvenements.setItems(sortedData);
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
        listEvenements.getSelectionModel().clearSelection();
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

    private void handleModifierBtnClicked() {
        Evenement selection = listEvenements.getSelectionModel().getSelectedItem();
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
        Evenement selection = listEvenements.getSelectionModel().getSelectedItem();
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
    private void handleReclamations(ActionEvent event) {
        navigate(event, "/com/sitmypet/fxml/AdminReclamations.fxml");
    }

    @FXML
    private void handleAnnonces(ActionEvent event) {
        navigate(event, "/com/sitmypet/fxml/AdminAnnonces.fxml");
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
