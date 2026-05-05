package com.sitmypet.controllers;

import com.sitmypet.model.User;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Reclamation;
import services.ServiceReclamation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class FrontReclamationsController {

    @FXML private TextField txtSujet;
    @FXML private TextArea txtDescription;
    @FXML private VBox historiqueContainer;

    private ServiceReclamation serviceReclamation;
    private User currentUser;

    @FXML
    public void initialize() {
        serviceReclamation = new ServiceReclamation();
    }

    public void setUser(User user) {
        this.currentUser = user;
        chargerHistorique();
    }

    @FXML
    private void handleEnvoyer() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour envoyer une réclamation.");
            return;
        }

        String sujet = txtSujet.getText().trim();
        String desc = txtDescription.getText().trim();

        if (sujet.isEmpty() || desc.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez remplir tous les champs.");
            return;
        }

        // Auto-détection de la priorité selon le texte (optionnel mais cool)
        String priorite = serviceReclamation.detecterPriorite(sujet, desc);
        String nomComplet = currentUser.getNom() + " " + currentUser.getPrenom();

        Reclamation r = new Reclamation(
                sujet, 
                desc, 
                LocalDateTime.now(), 
                "en_attente", 
                priorite, 
                nomComplet, 
                currentUser.getEmail(), 
                currentUser.getId()
        );

        try {
            serviceReclamation.add(r);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre réclamation a bien été envoyée. Nous vous répondrons dans les plus brefs délais.");
            txtSujet.clear();
            txtDescription.clear();
            chargerHistorique();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void chargerHistorique() {
        if (currentUser == null) return;
        
        historiqueContainer.getChildren().clear();
        
        // Filtrer par ID utilisateur (ou email)
        List<Reclamation> mesReclamations = serviceReclamation.getAll().stream()
                .filter(r -> r.getUserId() == currentUser.getId() || 
                            (r.getEmailClient() != null && r.getEmailClient().equalsIgnoreCase(currentUser.getEmail())))
                .collect(Collectors.toList());

        if (mesReclamations.isEmpty()) {
            Label lblEmpty = new Label("Vous n'avez soumis aucune réclamation.");
            lblEmpty.setStyle("-fx-text-fill: #a0aec0; -fx-font-style: italic;");
            historiqueContainer.getChildren().add(lblEmpty);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Reclamation r : mesReclamations) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0;");
            
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            
            Label lblSujet = new Label(r.getSujet());
            lblSujet.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2d1354;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label lblStatut = createStatutBadge(r.getStatut());
            
            header.getChildren().addAll(lblSujet, spacer, lblStatut);
            
            Label lblDate = new Label(r.getDateReclamation() != null ? r.getDateReclamation().format(formatter) : "");
            lblDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
            
            Label lblDesc = new Label(r.getDescription());
            lblDesc.setStyle("-fx-text-fill: #4a5568;");
            lblDesc.setWrapText(true);
            
            card.getChildren().addAll(header, lblDate, new Separator(), lblDesc);
            historiqueContainer.getChildren().add(card);
        }
    }
    
    private Label createStatutBadge(String statut) {
        String bg = "#e2e8f0";
        String fg = "#4a5568";
        String txt = "Inconnu";
        
        if ("en_attente".equals(statut)) {
            bg = "#fefcbf"; fg = "#975a16"; txt = "En attente";
        } else if ("en_cours".equals(statut)) {
            bg = "#bee3f8"; fg = "#2c5282"; txt = "En cours";
        } else if ("resolue".equals(statut)) {
            bg = "#c6f6d5"; fg = "#22543d"; txt = "Résolue";
        }
        
        Label badge = new Label(txt);
        badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        return badge;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
