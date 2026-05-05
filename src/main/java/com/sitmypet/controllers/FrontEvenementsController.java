package com.sitmypet.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Evenement;
import services.ServiceEvenement;

import java.util.List;

public class FrontEvenementsController {

    @FXML private TilePane cardsContainer;
    private ServiceEvenement serviceEvenement;

    @FXML
    public void initialize() {
        serviceEvenement = new ServiceEvenement();
        chargerCartes();
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
        Label dateIcon = new Label("📅");
        Label dateLabel = new Label(e.getDate() != null ? e.getDate().toString() : "Date inconnue");
        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        
        Label timeIcon = new Label("⏰");
        Label timeLabel = new Label(e.getHeure() != null ? e.getHeure() : "--:--");
        timeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        
        dateBox.getChildren().addAll(dateIcon, dateLabel, new Region(), timeIcon, timeLabel);
        
        // Adresse
        HBox addressBox = new HBox(10);
        addressBox.setAlignment(Pos.CENTER_LEFT);
        Label addressIcon = new Label("📍");
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
        
        card.getChildren().addAll(nameLabel, dateBox, addressBox, sep, descLabel);
        
        // Effet de survol
        card.setOnMouseEntered(event -> card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5); -fx-border-radius: 15; -fx-border-color: #e2e8f0; -fx-cursor: hand;"));
        card.setOnMouseExited(event -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-border-radius: 15; -fx-border-color: #edf2f7;"));
        
        return card;
    }
}
