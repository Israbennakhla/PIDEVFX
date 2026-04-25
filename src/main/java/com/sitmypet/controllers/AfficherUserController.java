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
    @FXML private Button btnVoirCertificat;
    @FXML private Button btnDebloquer;
    
    private ServiceUser serviceUser;
    private ObservableList<User> usersList;
    private User currentUserDetails;

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
                    Region iconModif = new Region();
                    iconModif.setStyle("-fx-background-color: #8e5bd6; -fx-shape: \"M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z\"; -fx-min-width: 22px; -fx-min-height: 22px;");
                    
                    Button btnModifier = new Button();
                    btnModifier.setGraphic(iconModif);
                    btnModifier.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;");
                    btnModifier.setOnAction(e -> modifierUser(item));
                    
                    Region iconSupp = new Region();
                    iconSupp.setStyle("-fx-background-color: #2d3748; -fx-shape: \"M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z\"; -fx-min-width: 22px; -fx-min-height: 22px;");
                    
                    Button btnSupprimer = new Button();
                    btnSupprimer.setGraphic(iconSupp);
                    btnSupprimer.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;");
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
        currentUserDetails = user;
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
        
        if (user.getCertificat() != null && !user.getCertificat().isEmpty()) {
            btnVoirCertificat.setVisible(true);
            btnVoirCertificat.setManaged(true);
        } else {
            btnVoirCertificat.setVisible(false);
            btnVoirCertificat.setManaged(false);
        }
        
        boolean isBloque = serviceUser.estBloque(user.getId());
        if (isBloque) {
            btnDebloquer.setVisible(true);
            btnDebloquer.setManaged(true);
        } else {
            btnDebloquer.setVisible(false);
            btnDebloquer.setManaged(false);
        }
    }

    @FXML
    private void handleDebloquer() {
        if (currentUserDetails != null) {
            serviceUser.debloquerCompte(currentUserDetails.getId());
            btnDebloquer.setVisible(false);
            btnDebloquer.setManaged(false);
            afficherSucces("Compte Débloqué", "Le compte de " + currentUserDetails.getPrenom() + " a été débloqué avec succès.");
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

    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {}
        dialogPane.getStyleClass().add("custom-alert");
    }

    private void supprimerUser(User userSelectionne) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmation de suppression");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {}
        
        VBox content = new VBox(20);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(30, 40, 10, 40));
        
        javafx.scene.layout.StackPane iconContainer = new javafx.scene.layout.StackPane();
        iconContainer.setStyle("-fx-background-color: #fff5f5; -fx-background-radius: 50; -fx-padding: 20; -fx-max-width: 80; -fx-max-height: 80;");
        Label iconLabel = new Label("⚠️");
        iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #e53e3e;");
        iconContainer.getChildren().add(iconLabel);
        
        Label titleLabel = new Label("Supprimer l'utilisateur ?");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #2d3748;");
        
        Label descLabel = new Label("Êtes-vous sûr de vouloir supprimer définitivement\n" + 
                                    userSelectionne.getPrenom() + " " + userSelectionne.getNom() + " ?\nCette action est irréversible.");
        descLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #718096; -fx-text-alignment: center;");
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        content.getChildren().addAll(iconContainer, titleLabel, descLabel);
        dialogPane.setContent(content);
        
        ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnSupprimer = new ButtonType("Oui, supprimer", ButtonBar.ButtonData.OK_DONE);
        
        dialogPane.getButtonTypes().addAll(btnAnnuler, btnSupprimer);
        
        Button cancelNode = (Button) dialogPane.lookupButton(btnAnnuler);
        cancelNode.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0aec0; -fx-font-weight: bold; -fx-font-size: 15px; -fx-cursor: hand;");
        
        Button deleteNode = (Button) dialogPane.lookupButton(btnSupprimer);
        deleteNode.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-padding: 12 25; -fx-font-size: 15px; -fx-background-radius: 30; -fx-font-weight: 900; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(229, 62, 62, 0.4), 15, 0, 0, 5);");
        
        Optional<ButtonType> resultat = dialog.showAndWait();
        
        if (resultat.isPresent() && resultat.get() == btnSupprimer) {
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
        styleAlert(alert);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void afficherAvertissement(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    @FXML
    private void handleDashboard(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/Dashboard.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVoirCertificat(javafx.event.ActionEvent event) {
        if (currentUserDetails == null || currentUserDetails.getCertificat() == null) return;
        try {
            java.io.File file = new java.io.File("SitMyPet-Desktop/src/main/resources/uploads/certificats", currentUserDetails.getCertificat());
            if (!file.exists()) {
                file = new java.io.File("src/main/resources/uploads/certificats", currentUserDetails.getCertificat());
            }
            if (file.exists()) {
                javafx.scene.image.Image image = new javafx.scene.image.Image(file.toURI().toString());
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(600);
                
                ScrollPane scrollPane = new ScrollPane(imageView);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setStyle("-fx-background-color: white;");
                
                VBox mainContainer = new VBox(10);
                mainContainer.setStyle("-fx-background-color: white; -fx-padding: 10;");
                VBox.setVgrow(scrollPane, Priority.ALWAYS);
                
                HBox buttonBox = new HBox(15);
                buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
                buttonBox.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));
                
                Button btnAccepter = new Button("✅ Accepter et Activer");
                btnAccepter.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
                
                Button btnRefuser = new Button("❌ Refuser et Supprimer");
                btnRefuser.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
                
                Stage stage = new Stage();
                
                btnAccepter.setOnAction(e -> {
                    currentUserDetails.setActive(true);
                    serviceUser.modifier(currentUserDetails);
                    afficherSucces("Compte Activé", "Le compte de " + currentUserDetails.getPrenom() + " a été activé avec succès.");
                    chargerUtilisateurs();
                    afficherDetails(currentUserDetails);
                    stage.close();
                });
                
                btnRefuser.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmer le refus");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Êtes-vous sûr de vouloir refuser ce certificat ? Le compte sera supprimé.");
                    styleAlert(confirm);
                    
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        serviceUser.supprimer(currentUserDetails.getId());
                        afficherSucces("Compte Supprimé", "Le compte a été refusé et supprimé.");
                        chargerUtilisateurs();
                        paneDetails.setVisible(false);
                        paneDetails.setManaged(false);
                        stage.close();
                    }
                });
                
                if (!currentUserDetails.isActive()) {
                    buttonBox.getChildren().addAll(btnAccepter, btnRefuser);
                    mainContainer.getChildren().addAll(scrollPane, buttonBox);
                } else {
                    mainContainer.getChildren().add(scrollPane);
                }
                
                stage.setTitle("Certificat de " + currentUserDetails.getNom() + " " + currentUserDetails.getPrenom());
                stage.setScene(new Scene(mainContainer, 650, 750));
                stage.show();
            } else {
                afficherErreur("Fichier introuvable", "Le fichier du certificat est introuvable sur le disque.");
            }
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de charger l'image : " + e.getMessage());
        }
    }


    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/Login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle("SitMyPet - Connexion");
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setScene(new Scene(root, Math.min(bounds.getWidth() * 0.9, 900), Math.min(bounds.getHeight() * 0.9, 600)));
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
