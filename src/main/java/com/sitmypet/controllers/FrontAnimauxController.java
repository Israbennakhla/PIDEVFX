package com.sitmypet.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.sitmypet.model.Pet;
import com.sitmypet.services.ServicePet;

import java.sql.Date;
import java.util.List;

public class FrontAnimauxController {

    @FXML private TilePane cardsContainer;
    @FXML private VBox formContainer;
    @FXML private Label lblFormTitle;
    
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
    
    @FXML private Button btnSubmit;

    private ServicePet servicePet;
    private Pet currentEditingPet = null;

    @FXML
    public void initialize() {
        servicePet = new ServicePet();
        
        cbType.setItems(FXCollections.observableArrayList("Chien", "Chat", "Oiseau", "Rongeur", "Autre"));
        cbGenre.setItems(FXCollections.observableArrayList("Mâle", "Femelle"));
        
        chargerCartes();
    }

    private void chargerCartes() {
        cardsContainer.getChildren().clear();
        List<Pet> pets = servicePet.getAll();
        
        if (pets.isEmpty()) {
            Label noDataLabel = new Label("Vous n'avez pas encore ajouté d'animaux.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #718096; -fx-padding: 50;");
            cardsContainer.getChildren().add(noDataLabel);
            return;
        }

        for (Pet p : pets) {
            VBox card = createPetCard(p);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createPetCard(Pet p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-border-radius: 15; -fx-border-color: #edf2f7;");
        card.setPrefWidth(280);
        
        // Header de la carte (Icone + Nom)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(getIconForType(p.getTypePet()));
        iconLabel.setStyle("-fx-font-size: 30px; -fx-background-color: #f3e8ff; -fx-background-radius: 50; -fx-min-width: 50; -fx-min-height: 50; -fx-alignment: center;");
        
        VBox titleBox = new VBox(2);
        Label nameLabel = new Label(p.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #2d1354;");
        Label typeLabel = new Label(p.getTypePet() + (p.getBreed() != null && !p.getBreed().isEmpty() ? " - " + p.getBreed() : ""));
        typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        titleBox.getChildren().addAll(nameLabel, typeLabel);
        
        header.getChildren().addAll(iconLabel, titleBox);
        
        // Détails (Âge, Poids)
        HBox detailsBox = new HBox(15);
        detailsBox.setStyle("-fx-padding: 10 0; -fx-border-color: #edf2f7; -fx-border-width: 1 0;");
        
        VBox wBox = new VBox(2);
        Label wTitle = new Label("Poids");
        wTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0; -fx-font-weight: bold;");
        Label wVal = new Label(p.getWeight() + " kg");
        wVal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        wBox.getChildren().addAll(wTitle, wVal);
        
        VBox gBox = new VBox(2);
        Label gTitle = new Label("Genre");
        gTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0; -fx-font-weight: bold;");
        Label gVal = new Label(p.getGender());
        gVal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        gBox.getChildren().addAll(gTitle, gVal);
        
        detailsBox.getChildren().addAll(wBox, gBox);
        
        // Badges santé
        FlowPane badges = new FlowPane(5, 5);
        if (p.isVaccinated()) badges.getChildren().add(createBadge("Vacciné", "#c6f6d5", "#22543d"));
        if (p.isHasMedicalRecord()) badges.getChildren().add(createBadge("Carnet Dispo", "#ebf8ff", "#2b6cb0"));
        if (p.isHasContagiousDisease()) badges.getChildren().add(createBadge("Contagieux", "#fed7d7", "#9b2c2c"));
        if (p.isHasCriticalCondition()) badges.getChildren().add(createBadge("Cond. Critique", "#fed7d7", "#9b2c2c"));
        
        // Boutons actions
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setStyle("-fx-padding: 10 0 0 0;");
        
        Button btnEdit = new Button("Modifier");
        btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #3182ce; -fx-cursor: hand; -fx-font-weight: bold;");
        btnEdit.setOnAction(e -> handleEditCard(p));
        
        Button btnDel = new Button("Supprimer");
        btnDel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #e53e3e; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 5 10;");
        btnDel.setOnAction(e -> handleDeleteCard(p));
        
        actionsBox.getChildren().addAll(btnEdit, btnDel);
        
        card.getChildren().addAll(header, detailsBox, badges, actionsBox);
        return card;
    }

    private Label createBadge(String text, String bgColor, String textColor) {
        Label badge = new Label(text);
        badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");
        return badge;
    }

    private String getIconForType(String type) {
        if (type == null) return "";
        switch (type.toLowerCase()) {
            case "chien": return "🐶";
            case "chat": return "🐱";
            case "oiseau": return "🦜";
            case "rongeur": return "🐹";
            default: return "";
        }
    }

    @FXML
    private void handleShowForm() {
        currentEditingPet = null;
        lblFormTitle.setText("Nouveau compagnon");
        btnSubmit.setText("Ajouter");
        clearForm();
        formContainer.setVisible(true);
        formContainer.setManaged(true);
    }

    @FXML
    private void handleCloseForm() {
        formContainer.setVisible(false);
        formContainer.setManaged(false);
    }

    private void handleEditCard(Pet p) {
        currentEditingPet = p;
        lblFormTitle.setText("Modifier " + p.getName());
        btnSubmit.setText("Enregistrer les modifications");
        
        txtNom.setText(p.getName());
        cbType.setValue(p.getTypePet());
        txtRace.setText(p.getBreed());
        cbGenre.setValue(p.getGender());
        txtPoids.setText(String.valueOf(p.getWeight()));
        if (p.getBirthDate() != null) dpNaissance.setValue(p.getBirthDate().toLocalDate());
        txtDescription.setText(p.getDescription());
        chkVaccine.setSelected(p.isVaccinated());
        chkMaladie.setSelected(p.isHasContagiousDisease());
        chkDossier.setSelected(p.isHasMedicalRecord());
        chkCritique.setSelected(p.isHasCriticalCondition());
        
        formContainer.setVisible(true);
        formContainer.setManaged(true);
    }

    private void handleDeleteCard(Pet p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer " + p.getName() + " ?");
        
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            servicePet.delete(p);
            chargerCartes();
            if (currentEditingPet != null && currentEditingPet.getId() == p.getId()) {
                handleCloseForm();
            }
        }
    }

    @FXML
    private void handleSubmit() {
        if (txtNom.getText().isEmpty() || cbType.getValue() == null || dpNaissance.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir le nom, le type et la date de naissance.");
            return;
        }
        
        float poids = 0;
        if (!txtPoids.getText().isEmpty()) {
            try {
                poids = Float.parseFloat(txtPoids.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Le poids doit être un nombre valide.");
                return;
            }
        }
        
        Pet p = new Pet(
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
        
        if (currentEditingPet == null) {
            servicePet.add(p);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Animal ajouté avec succès !");
        } else {
            p.setId(currentEditingPet.getId());
            p.setImageName(currentEditingPet.getImageName());
            servicePet.update(p);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Animal modifié avec succès !");
        }
        
        chargerCartes();
        handleCloseForm();
    }

    private void clearForm() {
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
    }
    
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

