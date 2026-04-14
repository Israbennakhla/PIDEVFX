package Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Reclamation;
import services.ServiceReclamation;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AfficherReclamations implements Initializable {

    @FXML private GridPane     gridCartes;
    @FXML private ComboBox<String> filtreStatut;
    @FXML private ComboBox<String> filtrePriorite;

    private final ServiceReclamation service = new ServiceReclamation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filtreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "en_attente", "en_cours", "resolue"
        ));
        filtrePriorite.setItems(FXCollections.observableArrayList(
                "Tous", "basse", "moyenne", "haute"
        ));
        filtreStatut.setValue("Tous");
        filtrePriorite.setValue("Tous");

        chargerCartes(service.getAll());
    }

    private void chargerCartes(List<Reclamation> list) {
        gridCartes.getChildren().clear();

        if (list.isEmpty()) {
            Label vide = new Label("Aucune réclamation trouvée.");
            vide.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
            gridCartes.add(vide, 0, 0);
            return;
        }

        int col = 0;
        int row = 0;

        for (Reclamation r : list) {
            VBox carte = creerCarte(r);
            gridCartes.add(carte, col, row);

            col++;
            if (col == 3) { // 3 cartes par ligne
                col = 0;
                row++;
            }
        }
    }

    private VBox creerCarte(Reclamation r) {
        VBox carte = new VBox(10);
        carte.setPadding(new Insets(18));
        carte.setPrefWidth(290);
        carte.getStyleClass().add("carte");

        // Bandeau couleur priorité
        HBox bandeau = new HBox();
        bandeau.setPrefHeight(5);
        bandeau.setStyle("-fx-background-color: " + classePriorite((r.getPriorite()) + ";" +
                "-fx-background-radius: 10 10 0 0;"));

        // Sujet
        Label sujet = new Label(r.getSujet());
        sujet.getStyleClass().add("carte-sujet");
        sujet.setWrapText(true);

        // Client
        Label client = new Label("👤  " + r.getNomClient());
        client.getStyleClass().add("carte-info");

        // Email
        Label email = new Label("✉️  " + r.getEmailClient());
        email.getStyleClass().add("carte-info");

        // Description tronquée
        String desc = r.getDescription().length() > 70
                ? r.getDescription().substring(0, 70) + "..." : r.getDescription();
        Label description = new Label(desc);
        description.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");
        description.setWrapText(true);

        // Badges
        HBox badges = new HBox(8);
        Label badgeStatut = new Label(r.getStatut());
        badgeStatut.getStyleClass().add(classeStatut(r.getStatut()));

        Label badgePriorite = new Label(r.getPriorite());
        badgePriorite.getStyleClass().add(classePriorite(r.getPriorite()));
        badges.getChildren().addAll(badgeStatut, badgePriorite);

        // Date
        Label date = new Label("📅  " + (r.getDateReclamation() != null
                ? r.getDateReclamation().toLocalDate() : "N/A"));
        date.getStyleClass().add("carte-date");

        // Séparateur
        Separator sep = new Separator();

        // Boutons
        HBox boutons = new HBox(8);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        Button btnMod = new Button("Modifier");
        btnMod.getStyleClass().add("btn-modifier");
        btnMod.setOnAction(e -> ouvrirModification(r));

        Button btnSup = new Button("🗑️ Supprimer");
        btnSup.getStyleClass().add("btn-supprimer");
        btnSup.setOnAction(e -> supprimerReclamation(r));

        Button btnRep = new Button("💬 Réponses");
        btnRep.getStyleClass().add("btn-reponse");
        btnRep.setOnAction(e -> voirReponses(r));

        boutons.getChildren().addAll(btnMod, btnSup, btnRep);
        carte.getChildren().addAll(sujet, client, email, description, badges, date, sep, boutons);
        return carte;
    }

    private String classeStatut(String statut) {
        if (statut == null) return "badge-attente";
        return switch (statut) {
            case "en_cours"   -> "badge-cours";
            case "resolue"    -> "badge-resolue";
            default           -> "badge-attente";
        };
    }

    private String classePriorite(String priorite) {
        if (priorite == null) return "badge-basse";
        return switch (priorite) {
            case "haute"   -> "badge-haute";
            case "moyenne" -> "badge-moyenne";
            default        -> "badge-basse";
        };
    }

    @FXML
    private void appliquerFiltre() {
        List<Reclamation> list = service.searchByStatutAndPriorite(
                filtreStatut.getValue(), filtrePriorite.getValue()
        );
        chargerCartes(list);
    }

    @FXML
    private void reinitialiser() {
        filtreStatut.setValue("Tous");
        filtrePriorite.setValue("Tous");
        chargerCartes(service.getAll());
    }

    @FXML
    private void ouvrirAjout() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/AjouterReclamation.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Nouvelle Réclamation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerCartes(service.getAll());
        } catch (Exception e) {
            erreur("Erreur ouverture formulaire : " + e.getMessage());
        }
    }

    private void ouvrirModification(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ModifierReclamation.fxml")
            );
            Parent root = loader.load();
            ModifierReclamation ctrl = loader.getController();
            ctrl.setReclamation(r);
            Stage stage = new Stage();
            stage.setTitle("Modifier Réclamation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerCartes(service.getAll());
        } catch (Exception e) {
            erreur("Erreur modification : " + e.getMessage());
        }
    }

    private void supprimerReclamation(Reclamation r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la réclamation : \"" + r.getSujet() + "\" ?"
        );
        confirm.setHeaderText("Confirmation de suppression");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(r);
            chargerCartes(service.getAll());
        }
    }

    private void voirReponses(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/AfficherReponses.fxml")
            );
            Parent root = loader.load();
            AfficherReponses ctrl = loader.getController();
            ctrl.setReclamation(r);
            Stage stage = new Stage();
            stage.setTitle("Réponses — " + r.getSujet());
            stage.setScene(new Scene(root, 600, 450));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            erreur("Erreur réponses : " + e.getMessage());
        }
    }

    private void erreur(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}