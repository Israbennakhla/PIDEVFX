package com.sitmypet.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.sitmypet.model.Evenement;
import com.sitmypet.model.EventParticipant;
import com.sitmypet.services.ServiceEvenement;
import com.sitmypet.services.ServiceEventParticipant;
import com.sitmypet.services.EmailService;
import java.util.concurrent.CompletableFuture;

import java.util.List;
import com.sitmypet.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class FrontEvenementsController {

    @FXML private TilePane cardsContainer;
    @FXML private Button btnAjouter;

    private ServiceEvenement serviceEvenement;
    private User currentUser;

    @FXML
    public void initialize() {
        serviceEvenement = new ServiceEvenement();
        chargerCartes();
        
        // Hide by default until setUser is called
        if (btnAjouter != null) {
            btnAjouter.setVisible(false);
            btnAjouter.setManaged(false);
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (currentUser != null && currentUser.getRole() != null) {
            String role = currentUser.getRole().toUpperCase();
            if (role.contains("GARDIEN")) {
                if (btnAjouter != null) {
                    btnAjouter.setVisible(true);
                    btnAjouter.setManaged(true);
                }
            }
        }
        // Recharger les cartes avec le contexte utilisateur pour afficher les boutons d'inscription
        chargerCartes();
    }

    @FXML
    private void handleShowForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/EvenementFormView.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Événement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.setScene(new Scene(root, 740, 560));
            stage.showAndWait();
            
            // Recharger la liste après la fermeture de la modale
            chargerCartes();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture du formulaire");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void chargerCartes() {
        cardsContainer.getChildren().clear();
        List<Evenement> evenements = serviceEvenement.getAll();
        
        if (evenements.isEmpty()) {
            Label noDataLabel = new Label("Aucun événement prévu pour le moment.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #718096; -fx-padding: 50;");
            cardsContainer.getChildren().add(noDataLabel);
            return;
        }

        for (Evenement e : evenements) {
            VBox card = createEventCard(e);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createEventCard(Evenement e) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-border-radius: 15; -fx-border-color: #edf2f7;");
        card.setPrefWidth(300);
        
        // Header
        Label nameLabel = new Label(e.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #2d1354;");
        nameLabel.setWrapText(true);
        
        // Date & Heure
        HBox dateBox = new HBox(10);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = new Label("");
        Label dateLabel = new Label(e.getDate() != null ? e.getDate().toString() : "Date inconnue");
        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        
        Label timeIcon = new Label("⏰");
        Label timeLabel = new Label(e.getHeure() != null ? e.getHeure() : "--:--");
        timeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        
        dateBox.getChildren().addAll(dateIcon, dateLabel, new Region(), timeIcon, timeLabel);
        
        // Adresse
        HBox addressBox = new HBox(10);
        addressBox.setAlignment(Pos.CENTER_LEFT);
        Label addressIcon = new Label("");
        Label addressLabel = new Label(e.getAddresse());
        addressLabel.setStyle("-fx-text-fill: #718096;");
        addressLabel.setWrapText(true);
        addressBox.getChildren().addAll(addressIcon, addressLabel);
        
        // Séparateur
        Separator sep = new Separator();
        
        // Description
        Label descLabel = new Label(e.getDescription());
        descLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");
        descLabel.setWrapText(true);
        
        // Inscription Button
        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        
        if (currentUser != null && currentUser.getRole() != null && (currentUser.getRole().contains("PROPRIETAIRE") || currentUser.getRole().contains("PROPRIÉTAIRE") || currentUser.getRole().contains("GARDIEN"))) {
            ServiceEventParticipant sepService = new ServiceEventParticipant();
            boolean isEnrolled = sepService.isEnrolled(e.getId(), currentUser.getId());
            
            Button btnInscription = new Button();
            if (isEnrolled) {
                btnInscription.setText("Se désinscrire");
                btnInscription.setStyle("-fx-background-color: #f43f5e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
            } else {
                btnInscription.setText("S'inscrire");
                btnInscription.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
            }
            
            btnInscription.setOnAction(event -> {
                if (sepService.isEnrolled(e.getId(), currentUser.getId())) {
                    sepService.delete(new EventParticipant(e.getId(), currentUser.getId()));
                } else {
                    sepService.add(new EventParticipant(e.getId(), currentUser.getId()));
                    
                    // Envoi du mail de confirmation avec QR Code et lien Maps de manière asynchrone
                    CompletableFuture.runAsync(() -> {
                        EmailService emailService = new EmailService();
                        boolean success = emailService.envoyerEmailInscriptionEvenement(currentUser, e);
                        
                        javafx.application.Platform.runLater(() -> {
                            Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
                            alert.setTitle(success ? "Inscription confirmée" : "Inscription enregistrée");
                            alert.setHeaderText(null);
                            alert.setContentText(success 
                                ? "Votre billet avec QR Code a été envoyé à votre adresse email (" + currentUser.getEmail() + ")." 
                                : "L'inscription est réussie, mais une erreur est survenue lors de l'envoi de l'email.");
                            alert.showAndWait();
                        });
                    });
                }
                chargerCartes();
            });
            
            bottomBox.getChildren().add(btnInscription);
        }
        
        card.getChildren().addAll(nameLabel, dateBox, addressBox, sep, descLabel, bottomBox);
        
        // Effet de survol
        card.setOnMouseEntered(event -> card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5); -fx-border-radius: 15; -fx-border-color: #e2e8f0; -fx-cursor: hand;"));
        card.setOnMouseExited(event -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-border-radius: 15; -fx-border-color: #edf2f7;"));
        
        return card;
    }
}

