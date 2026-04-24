package com.sitmypet.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.sitmypet.model.User;
import com.sitmypet.services.ServiceUser;

import java.io.IOException;
import java.util.Optional;

// Imports externes supprimés

public class AfficherUserController {

    @FXML private ListView<User> listUsers;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboRole;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<String> comboTri;
    @FXML private Label lblTotal;
    @FXML private Label lblAdmin;
    @FXML private Label lblProprietaire;
    @FXML private Label lblGardien;
    
    // Eléments du panneau de détails
    @FXML private VBox paneDetails;
    @FXML private Label lblDetailInitials;
    @FXML private Label lblDetailNom;
    @FXML private Label lblDetailId;
    @FXML private Label lblDetailEmail;
    @FXML private Label lblDetailTel;
    @FXML private Label lblDetailAdresse;
    @FXML private Label lblDetailRole;
    @FXML private Label lblDetailStatut;
    @FXML private Label lblDetailDate;
    
    private ServiceUser serviceUser;
    private ObservableList<User> usersList;

    public AfficherUserController() {
        serviceUser = new ServiceUser();
        usersList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Configuration de la ListView pour afficher les utilisateurs en "Cartes"
        listUsers.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
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
                    Label nameLbl = new Label(item.getPrenom() + " " + item.getNom());
                    nameLbl.getStyleClass().add("user-name-label");
                    Label emailLbl = new Label("📧 " + item.getEmail());
                    emailLbl.getStyleClass().add("user-detail-label");
                    Label phoneLbl = new Label("📞 " + (item.getTelephone() != null && !item.getTelephone().isEmpty() ? item.getTelephone() : "Non renseigné"));
                    phoneLbl.getStyleClass().add("user-detail-label");
                    identity.getChildren().addAll(nameLbl, emailLbl, phoneLbl);
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    VBox statusBox = new VBox(5);
                    statusBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    String role = item.getRole() != null ? item.getRole().replace("ROLE_", "") : "USER";
                    Label roleLbl = new Label(role);
                    roleLbl.getStyleClass().add("role-badge");
                    
                    Label statusLbl = new Label(item.isActive() ? "✅ Actif" : "❌ Inactif");
                    statusLbl.setStyle(item.isActive() ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;" : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    
                    statusBox.getChildren().addAll(roleLbl, statusLbl);
                    
                    HBox actionBox = new HBox(10);
                    actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    Button btnModifier = new Button("✎");
                    btnModifier.getStyleClass().add("btn-warning");
                    btnModifier.setStyle("-fx-padding: 5 0; -fx-background-radius: 8; -fx-font-size: 18px; -fx-min-width: 42px; -fx-pref-width: 42px; -fx-min-height: 38px; -fx-alignment: center;");
                    btnModifier.setOnAction(e -> modifierUser(item));
                    
                    Button btnSupprimer = new Button("✖");
                    btnSupprimer.getStyleClass().add("btn-danger");
                    btnSupprimer.setStyle("-fx-padding: 5 0; -fx-background-radius: 8; -fx-font-size: 18px; -fx-min-width: 42px; -fx-pref-width: 42px; -fx-min-height: 38px; -fx-alignment: center;");
                    btnSupprimer.setOnAction(e -> supprimerUser(item));
                    
                    actionBox.getChildren().addAll(btnModifier, btnSupprimer);
                    
                    card.getChildren().addAll(identity, spacer, statusBox, new Region() {{ setMinWidth(20); }}, actionBox);
                    
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });
        
        // Initialiser les filtres
        comboRole.setItems(FXCollections.observableArrayList("Tous", "ADMIN", "PROPRIETAIRE", "GARDIEN"));
        comboRole.setValue("Tous");
        
        comboStatut.setItems(FXCollections.observableArrayList("Tous", "Actif", "Inactif"));
        comboStatut.setValue("Tous");
        
        comboTri.setItems(FXCollections.observableArrayList("Plus récent", "Nom (A-Z)", "Nom (Z-A)"));
        comboTri.setValue("Plus récent");
        
        // --- Recherche dynamique ---
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> handleRechercher());
        comboRole.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) handleRechercher();
        });
        comboStatut.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) handleRechercher();
        });
        comboTri.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) handleRechercher();
        });
        
        // --- Écouteur de sélection pour le panneau de détails ---
        listUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                afficherDetails(newSelection);
            } else {
                paneDetails.setVisible(false);
                paneDetails.setManaged(false);
            }
        });
        
        // Charger les données initialement
        chargerUtilisateurs();
        
        // Menu contextuel (clic droit)
        creerMenuContextuel();
    }

    private void afficherDetails(User user) {
        paneDetails.setVisible(true);
        paneDetails.setManaged(true);
        
        lblDetailId.setText(String.valueOf(user.getId()));
        lblDetailNom.setText(user.getPrenom() + " " + user.getNom());
        
        // Initiales pour l'avatar
        String initials = "";
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) initials += user.getPrenom().substring(0, 1).toUpperCase();
        if (user.getNom() != null && !user.getNom().isEmpty()) initials += user.getNom().substring(0, 1).toUpperCase();
        lblDetailInitials.setText(initials);
        
        lblDetailEmail.setText(user.getEmail());
        lblDetailTel.setText((user.getTelephone() != null && !user.getTelephone().isEmpty()) ? user.getTelephone() : "Non renseigné");
        lblDetailAdresse.setText((user.getAdresse() != null && !user.getAdresse().isEmpty()) ? user.getAdresse() : "Non renseignée");
        
        String role = user.getRole() != null ? user.getRole().replace("ROLE_", "") : "USER";
        lblDetailRole.setText(role);
        
        lblDetailStatut.setText(user.isActive() ? "✅ Actif" : "❌ Inactif");
        lblDetailStatut.setStyle(user.isActive() ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;" : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        
        if (user.getCreatedAt() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            lblDetailDate.setText(user.getCreatedAt().format(formatter));
        } else {
            lblDetailDate.setText("Inconnue");
        }
    }

    private void chargerUtilisateurs() {
        usersList.clear();
        usersList.addAll(serviceUser.afficher());
        listUsers.setItems(usersList);
        mettreAJourStatistiques();
    }

    @FXML
    private void handleRechercher() {
        String searchTerm = txtRecherche.getText().trim();
        String roleFilter = comboRole.getValue();
        String statusFilter = comboStatut.getValue();
        String triFilter = comboTri.getValue();
        
        usersList.clear();
        usersList.addAll(serviceUser.rechercher(searchTerm, roleFilter, statusFilter, triFilter));
        listUsers.setItems(usersList);
        mettreAJourStatistiques();
    }

    @FXML
    private void handleReinitialiser() {
        txtRecherche.clear();
        comboRole.setValue("Tous");
        comboStatut.setValue("Tous");
        comboTri.setValue("Plus récent");
        chargerUtilisateurs();
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/AjouterUser.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un utilisateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            chargerUtilisateurs();
        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire d'ajout.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifier() {
        User userSelectionne = listUsers.getSelectionModel().getSelectedItem();
        
        if (userSelectionne == null) {
            afficherAvertissement("Aucune sélection", "Veuillez sélectionner un utilisateur à modifier.");
            return;
        }
        
        modifierUser(userSelectionne);
    }

    private void modifierUser(User userSelectionne) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/ModifierUser.fxml"));
            Parent root = loader.load();
            
            com.sitmypet.controllers.ModifierUserController controller = loader.getController();
            controller.setUser(userSelectionne);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier l'utilisateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            chargerUtilisateurs();
        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire de modification.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimer() {
        User userSelectionne = listUsers.getSelectionModel().getSelectedItem();
        
        if (userSelectionne == null) {
            afficherAvertissement("Aucune sélection", "Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }
        
        supprimerUser(userSelectionne);
    }

    private void supprimerUser(User userSelectionne) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'utilisateur ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " + 
                                    userSelectionne.getPrenom() + " " + userSelectionne.getNom() + " ?");
        
        Optional<ButtonType> resultat = confirmation.showAndWait();
        
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            serviceUser.supprimer(userSelectionne.getId());
            afficherSucces("Succès", "Utilisateur supprimé avec succès !");
            chargerUtilisateurs();
        }
    }

    private void creerMenuContextuel() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem itemModifier = new MenuItem("Modifier");
        itemModifier.setOnAction(e -> handleModifier());
        
        MenuItem itemSupprimer = new MenuItem("Supprimer");
        itemSupprimer.setOnAction(e -> handleSupprimer());
        
        contextMenu.getItems().addAll(itemModifier, itemSupprimer);
        listUsers.setContextMenu(contextMenu);
    }

    private void mettreAJourStatistiques() {
        int total = usersList.size();
        long admins = usersList.stream().filter(u -> u.getRole() != null && u.getRole().contains("ADMIN")).count();
        long proprietaires = usersList.stream().filter(u -> u.getRole() != null && u.getRole().contains("PROPRIETAIRE")).count();
        long gardiens = usersList.stream().filter(u -> u.getRole() != null && u.getRole().contains("GARDIEN")).count();
        
        lblTotal.setText(String.valueOf(total));
        lblAdmin.setText(String.valueOf(admins));
        lblProprietaire.setText(String.valueOf(proprietaires));
        lblGardien.setText(String.valueOf(gardiens));
    }

    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherAvertissement(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDashboard(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/Dashboard.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/Login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle("SitMyPet - Connexion");
            stage.setScene(new Scene(root, 900, 600));
            stage.setResizable(false);
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
