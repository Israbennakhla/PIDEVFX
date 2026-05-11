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
import com.sitmypet.model.Pet;
import com.sitmypet.services.ServicePet;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

public class AdminAnimauxController {

    @FXML private ListView<Pet> listAnimaux;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cbTri;
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
        cbTri.setItems(FXCollections.observableArrayList("Nom (A-Z)", "Nom (Z-A)", "Plus récents", "Poids (Croissant)", "Poids (Décroissant)"));
        
        listAnimaux.setCellFactory(param -> new ListCell<Pet>() {
            @Override
            protected void updateItem(Pet item, boolean empty) {
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
                    Label typeLbl = new Label("\u2022 " + item.getTypePet() + " - " + item.getBreed());
                    typeLbl.getStyleClass().add("user-detail-label");
                    Label infoLbl = new Label("\u2022 " + item.getWeight() + " kg | " + (item.getGender() != null ? item.getGender() : "N/A"));
                    infoLbl.getStyleClass().add("user-detail-label");
                    identity.getChildren().addAll(nameLbl, typeLbl, infoLbl);
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    HBox statusBox = new HBox(5);
                    statusBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    if (item.isVaccinated()) {
                        Label vacLbl = new Label("Vaccin\u00E9");
                        vacLbl.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
                        statusBox.getChildren().add(vacLbl);
                    } else {
                        Label vacLbl = new Label("Non Vaccin\u00E9");
                        vacLbl.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
                        statusBox.getChildren().add(vacLbl);
                    }
                    if (item.isHasCriticalCondition()) {
                        Label critLbl = new Label("Critique");
                        critLbl.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
                        statusBox.getChildren().add(critLbl);
                    }
                    
                    HBox actionBox = new HBox(10);
                    actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    actionBox.setPadding(new javafx.geometry.Insets(0, 0, 0, 15));
                    
                    Button btnModifier = new Button("Modifier");
                    btnModifier.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
                    btnModifier.setOnAction(e -> {
                        listAnimaux.getSelectionModel().select(item);
                        handleModifierBtnClicked();
                    });
                    
                    Button btnSupprimer = new Button("Supprimer");
                    btnSupprimer.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    btnSupprimer.setOnAction(e -> {
                        listAnimaux.getSelectionModel().select(item);
                        handleSupprimer();
                    });
                    
                    actionBox.getChildren().addAll(btnModifier, btnSupprimer);
                    
                    card.getChildren().addAll(identity, spacer, statusBox, new Region() {{ setMinWidth(15); }}, actionBox);
                    
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });

        chargerDonnees();

        listAnimaux.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirFormulaire(newSelection);
            }
        });
    }

    private void chargerDonnees() {
        petsList = FXCollections.observableArrayList(servicePet.getAll());
        
        javafx.collections.transformation.FilteredList<Pet> filteredData = new javafx.collections.transformation.FilteredList<>(petsList, b -> true);
        
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(pet -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (pet.getName() != null && pet.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                if (pet.getBreed() != null && pet.getBreed().toLowerCase().contains(lowerCaseFilter)) return true;
                if (pet.getTypePet() != null && pet.getTypePet().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        
        javafx.collections.transformation.SortedList<Pet> sortedData = new javafx.collections.transformation.SortedList<>(filteredData);
        cbTri.valueProperty().addListener((obs, oldVal, newVal) -> {
            sortedData.setComparator((p1, p2) -> {
                if (newVal == null) return 0;
                switch (newVal) {
                    case "Nom (A-Z)": return p1.getName().compareToIgnoreCase(p2.getName());
                    case "Nom (Z-A)": return p2.getName().compareToIgnoreCase(p1.getName());
                    case "Plus récents": return p2.getBirthDate().compareTo(p1.getBirthDate());
                    case "Poids (Croissant)": return Float.compare(p1.getWeight(), p2.getWeight());
                    case "Poids (Décroissant)": return Float.compare(p2.getWeight(), p1.getWeight());
                    default: return 0;
                }
            });
        });
        
        listAnimaux.setItems(sortedData);
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
        listAnimaux.getSelectionModel().clearSelection();
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

    private void handleModifierBtnClicked() {
        Pet selection = listAnimaux.getSelectionModel().getSelectedItem();
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
        Pet selection = listAnimaux.getSelectionModel().getSelectedItem();
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
        navigate(event, "/com/sitmypet/fxml/Dashboard.fxml");
    }

    @FXML
    private void handleUsers(ActionEvent event) {
        navigate(event, "/com/sitmypet/fxml/AfficherUser.fxml");
    }

    @FXML
    private void handleEvenements(ActionEvent event) {
        navigate(event, "/com/sitmypet/fxml/AdminEvenements.fxml");
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
