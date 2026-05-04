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
    @FXML private Label lblClient;
    @FXML private Label lblPriorite;
    @FXML private Label lblStatut;
    @FXML private Label lblCompteur;
    @FXML private VBox  listeReponses;

    private final ServiceReponse service = new ServiceReponse();
    private Reclamation reclamation;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    // ── Initialisation depuis AfficherReclamations ────────────────────────────
    public void setReclamation(Reclamation r) {
        this.reclamation = r;
        titreReclamation.setText(r.getSujet());
        lblClient.setText("👤 " + r.getNomClient());
        lblPriorite.setText("🎯 " + r.getPriorite());
        lblStatut.setText("📌 " + r.getStatut().toUpperCase());
        chargerReponses();
    }

    // ── Chargement de la liste ────────────────────────────────────────────────
    private void chargerReponses() {
        listeReponses.getChildren().clear();
        List<Reponse> list = service.getByReclamationId(reclamation.getId());

        lblCompteur.setText(list.isEmpty()
                ? "Aucune réponse pour cette réclamation"
                : list.size() + " réponse(s)");

        if (list.isEmpty()) {
            VBox vide = new VBox(12);
            vide.setAlignment(Pos.CENTER);
            vide.setPadding(new Insets(60));
            Label icon = new Label("📭");
            icon.setStyle("-fx-font-size: 40px;");
            Label msg = new Label("Aucune réponse pour le moment.");
            msg.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
            vide.getChildren().addAll(icon, msg);
            listeReponses.getChildren().add(vide);
            return;
        }

        for (Reponse rep : list) {
            listeReponses.getChildren().add(creerCarteReponse(rep));
        }
    }

    // ── Carte réponse ─────────────────────────────────────────────────────────
    private VBox creerCarteReponse(Reponse rep) {
        VBox carte = new VBox(14);
        carte.setPadding(new Insets(20));
        carte.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(155,142,196,0.18), 12, 0, 0, 4);"
        );

        // ── Barre accent violet en haut ───────────────────────────────────────
        HBox accent = new HBox();
        accent.setPrefHeight(4);
        accent.setStyle(
                "-fx-background-color: linear-gradient(to right, #9b8ec4, #c3b8e8);" +
                        "-fx-background-radius: 10 10 0 0;"
        );

        // ── Header : auteur + date ────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar cercle
        Label avatar = new Label(rep.getAuteur().substring(0, 1).toUpperCase());
        avatar.setStyle(
                "-fx-background-color: #9b8ec4; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 14px;" +
                        "-fx-min-width: 38; -fx-min-height: 38;" +
                        "-fx-max-width: 38; -fx-max-height: 38;" +
                        "-fx-background-radius: 19; -fx-alignment: center;"
        );

        VBox auteurInfo = new VBox(2);
        auteurInfo.setPadding(new Insets(0, 0, 0, 10));
        Label auteurNom = new Label(rep.getAuteur());
        auteurNom.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c2c2c;"
        );
        Label dateLabel = new Label("📅 " + (rep.getDateReponse() != null
                ? rep.getDateReponse().toLocalDate().toString() : "N/A"));
        dateLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        auteurInfo.getChildren().addAll(auteurNom, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge "Réponse officielle"
        Label badge = new Label("✅ Réponse");
        badge.setStyle(
                "-fx-background-color: #eaf7ee; -fx-text-fill: #27ae60;" +
                        "-fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-padding: 4 10 4 10;"
        );

        header.getChildren().addAll(avatar, auteurInfo, spacer, badge);

        // ── Contenu ───────────────────────────────────────────────────────────
        Label contenu = new Label(rep.getContenu());
        contenu.setWrapText(true);
        contenu.setStyle(
                "-fx-text-fill: #444; -fx-font-size: 13px; -fx-line-spacing: 3;" +
                        "-fx-background-color: #fafafa; -fx-background-radius: 8;" +
                        "-fx-padding: 14; -fx-border-color: #eeeeee;" +
                        "-fx-border-radius: 8; -fx-border-width: 1;"
        );

        // ── Zone traduction ───────────────────────────────────────────────────
        VBox zoneTraduction = new VBox(8);
        zoneTraduction.setStyle(
                "-fx-background-color: #f8f6ff; -fx-background-radius: 8; -fx-padding: 12;"
        );

        Label titreTraduction = new Label("🌐 Traduction");
        titreTraduction.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9b8ec4;"
        );

        HBox controlsTraduction = new HBox(10);
        controlsTraduction.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> langueCombo = new ComboBox<>();
        langueCombo.getItems().addAll("🇬🇧 Anglais", "🇸🇦 Arabe");
        langueCombo.setPromptText("Choisir une langue...");
        langueCombo.setStyle("-fx-font-size: 12px;");
        langueCombo.setPrefWidth(180);

        Button btnTraduire = new Button("🌐 Traduire");
        btnTraduire.setStyle(
                "-fx-background-color: #9b8ec4; -fx-text-fill: white;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-font-size: 12px; -fx-padding: 7 16 7 16;"
        );

        Label labelChargement = new Label("");
        labelChargement.setStyle("-fx-font-size: 11px;");

        controlsTraduction.getChildren().addAll(langueCombo, btnTraduire, labelChargement);

        Label labelTraduit = new Label("");
        labelTraduit.setWrapText(true);
        labelTraduit.setStyle(
                "-fx-text-fill: #2c2c2c; -fx-font-size: 13px;" +
                        "-fx-background-color: #ede8f7;" +
                        "-fx-background-radius: 8; -fx-padding: 12;" +
                        "-fx-border-color: #c3b8e8; -fx-border-radius: 8; -fx-border-width: 1;"
        );
        labelTraduit.setVisible(false);
        labelTraduit.setManaged(false);

        btnTraduire.setOnAction(e -> {
            if (langueCombo.getValue() == null) {
                labelChargement.setText("⚠ Choisis une langue !");
                labelChargement.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
                return;
            }
            String langCode = langueCombo.getValue().contains("Anglais") ? "en" : "ar";
            labelChargement.setText("⏳ Traduction en cours...");
            labelChargement.setStyle("-fx-text-fill: #9b8ec4; -fx-font-size: 11px;");
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

        zoneTraduction.getChildren().addAll(titreTraduction, controlsTraduction, labelTraduit);

        // ── Boutons Modifier / Supprimer ──────────────────────────────────────
        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-font-size: 12px; -fx-padding: 8 18 8 18;"
        );
        btnModifier.setOnAction(e -> ouvrirModificationReponse(rep));

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-font-size: 12px; -fx-padding: 8 18 8 18;"
        );
        btnSupprimer.setOnAction(e -> supprimerReponse(rep));

        boutons.getChildren().addAll(btnModifier, btnSupprimer);

        // ── Assemblage ────────────────────────────────────────────────────────
        carte.getChildren().addAll(header, contenu, zoneTraduction, boutons);
        return carte;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    @FXML
    private void ouvrirAjoutReponse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterReponse.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierReponse.fxml"));
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
                "Supprimer cette réponse de " + rep.getAuteur() + " ?");
        confirm.setHeaderText("Confirmation de suppression");
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