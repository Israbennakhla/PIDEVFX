package Controllers;

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
import model.Reponse;
import services.ServiceReponse;
import utils.TraductionService;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AfficherReponses implements Initializable {

    @FXML private Label titreReclamation;
    @FXML private Label infoReclamation;
    @FXML private VBox  listeReponses;

    private final ServiceReponse service = new ServiceReponse();
    private Reclamation reclamation;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void setReclamation(Reclamation r) {
        this.reclamation = r;
        titreReclamation.setText("💬 Réponses — " + r.getSujet());
        infoReclamation.setText("Client : " + r.getNomClient() +
                "  |  Priorité : " + r.getPriorite() +
                "  |  Statut : " + r.getStatut());
        chargerReponses();
    }

    private void chargerReponses() {
        listeReponses.getChildren().clear();
        List<Reponse> list = service.getByReclamationId(reclamation.getId());

        if (list.isEmpty()) {
            Label vide = new Label("Aucune réponse pour cette réclamation.");
            vide.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
            listeReponses.getChildren().add(vide);
            return;
        }

        for (Reponse rep : list) {
            listeReponses.getChildren().add(creerCarteReponse(rep));
        }
    }

    private VBox creerCarteReponse(Reponse rep) {
        VBox carte = new VBox(8);
        carte.setPadding(new Insets(15));
        carte.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);"
        );

        // ── Header : auteur + date ──────────────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label auteur = new Label("👤 " + rep.getAuteur());
        auteur.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        auteur.setTextFill(Color.web("#2c3e50"));

        Label date = new Label("📅 " + (rep.getDateReponse() != null
                ? rep.getDateReponse().toLocalDate().toString() : "N/A"));
        date.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(auteur, spacer, date);

        // ── Contenu de la réponse ───────────────────────────────
        Label contenu = new Label(rep.getContenu());
        contenu.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
        contenu.setWrapText(true);

        // ── Zone traduction ─────────────────────────────────────
        HBox zoneTraduction = new HBox(8);
        zoneTraduction.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> langueCombo = new ComboBox<>();
        langueCombo.getItems().addAll("🇬🇧 Anglais", "🇸🇦 Arabe");
        langueCombo.setPromptText("Langue...");
        langueCombo.setStyle("-fx-font-size: 11px;");
        langueCombo.setPrefWidth(130);

        Button btnTraduire = new Button("🌐 Traduire");
        btnTraduire.setStyle(
                "-fx-background-color: #9b59b6; -fx-text-fill: white;" +
                        "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11px;"
        );

        Label labelChargement = new Label("");
        labelChargement.setStyle("-fx-text-fill: #9b59b6; -fx-font-size: 11px;");

        zoneTraduction.getChildren().addAll(langueCombo, btnTraduire, labelChargement);

        // Label affichant le résultat de la traduction
        Label labelTraduit = new Label("");
        labelTraduit.setWrapText(true);
        labelTraduit.setStyle(
                "-fx-text-fill: #2c3e50; -fx-font-size: 12px;" +
                        "-fx-background-color: #f0e6ff;" +
                        "-fx-background-radius: 5; -fx-padding: 8;"
        );
        labelTraduit.setVisible(false);
        labelTraduit.setManaged(false);

        // ── Action bouton Traduire ──────────────────────────────
        btnTraduire.setOnAction(e -> {
            if (langueCombo.getValue() == null) {
                labelChargement.setText("⚠ Choisis une langue !");
                labelChargement.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
                return;
            }

            String langCode = langueCombo.getValue().contains("Anglais") ? "en" : "ar";
            labelChargement.setText("⏳ Traduction en cours...");
            labelChargement.setStyle("-fx-text-fill: #9b59b6; -fx-font-size: 11px;");
            btnTraduire.setDisable(true);

            new Thread(() -> {
                try {
                    String traduit = TraductionService.traduire(rep.getContenu(), langCode);
                    javafx.application.Platform.runLater(() -> {
                        labelTraduit.setText("🌐 " + traduit);
                        labelTraduit.setVisible(true);
                        labelTraduit.setManaged(true);
                        labelChargement.setText("✔ Traduit !");
                        labelChargement.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                        btnTraduire.setDisable(false);
                    });
                } catch (RuntimeException ex) {
                    javafx.application.Platform.runLater(() -> {
                        labelChargement.setText("❌ Erreur traduction");
                        labelChargement.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
                        btnTraduire.setDisable(false);
                    });
                }
            }).start();
        });

        // ── Boutons Modifier / Supprimer ────────────────────────
        HBox boutons = new HBox(8);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white;" +
                        "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11px;"
        );
        btnModifier.setOnAction(e -> ouvrirModificationReponse(rep));

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11px;"
        );
        btnSupprimer.setOnAction(e -> supprimerReponse(rep));

        boutons.getChildren().addAll(btnModifier, btnSupprimer);

        // ── Assemblage de la carte ──────────────────────────────
        carte.getChildren().addAll(header, contenu, zoneTraduction, labelTraduit, boutons);
        return carte;
    }

    @FXML
    private void ouvrirAjoutReponse() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/AjouterReponse.fxml")
            );
            Parent root = loader.load();
            AjouterReponse ctrl = loader.getController();
            ctrl.setReclamationId(reclamation.getId());
            Stage stage = new Stage();
            stage.setTitle("Nouvelle Réponse");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerReponses();
        } catch (Exception e) {
            erreur("Erreur : " + e.getMessage());
        }
    }

    private void ouvrirModificationReponse(Reponse rep) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ModifierReponse.fxml")
            );
            Parent root = loader.load();
            ModifierReponse ctrl = loader.getController();
            ctrl.setReponse(rep);
            Stage stage = new Stage();
            stage.setTitle("Modifier Réponse");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerReponses();
        } catch (Exception e) {
            erreur("Erreur : " + e.getMessage());
        }
    }

    private void supprimerReponse(Reponse rep) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette réponse de " + rep.getAuteur() + " ?"
        );
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(rep);
            chargerReponses();
        }
    }

    private void erreur(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}