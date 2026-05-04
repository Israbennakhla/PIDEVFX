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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Reclamation;
import services.ServiceReclamation;
import services.ServiceReponse;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class AfficherReclamations implements Initializable {

    @FXML private TextField        rechercheNom;
    @FXML private GridPane         gridCartes;
    @FXML private ComboBox<String> filtreStatut;
    @FXML private ComboBox<String> filtrePriorite;

    // Dashboard labels — optionnel si tu as le HBox dans le FXML
    @FXML private Label lblEnAttente;
    @FXML private Label lblEnCours;
    @FXML private Label lblResolues;

    private final ServiceReclamation service        = new ServiceReclamation();
    private final ServiceReponse     serviceReponse = new ServiceReponse();

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

        rechercheNom.textProperty().addListener(
                (observable, oldValue, newValue) -> appliquerFiltre()
        );

        chargerStats();
        chargerCartes(service.getAll());
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    private void chargerStats() {
        // null-safe : fonctionne avec ou sans le HBox dans le FXML
        if (lblEnAttente == null || lblEnCours == null || lblResolues == null) return;
        Map<String, Integer> stats = service.countByStatut();
        lblEnAttente.setText(String.valueOf(stats.getOrDefault("en_attente", 0)));
        lblEnCours.setText(String.valueOf(stats.getOrDefault("en_cours",    0)));
        lblResolues.setText(String.valueOf(stats.getOrDefault("resolue",    0)));
    }

    // ── Affichage des cartes ──────────────────────────────────────────────────
    private void chargerCartes(List<Reclamation> list) {
        gridCartes.getChildren().clear();
        gridCartes.getColumnConstraints().clear();

        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            cc.setHgrow(Priority.ALWAYS);
            gridCartes.getColumnConstraints().add(cc);
        }

        if (list.isEmpty()) {
            Label vide = new Label("Aucune réclamation trouvée.");
            vide.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
            gridCartes.add(vide, 0, 0);
            return;
        }

        int col = 0, row = 0;
        for (Reclamation r : list) {
            VBox carte = creerCarte(r);
            carte.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(carte, Priority.ALWAYS);
            gridCartes.add(carte, col, row);
            col++;
            if (col == 3) { col = 0; row++; }
        }
    }

    // ── Construction carte ────────────────────────────────────────────────────
    private VBox creerCarte(Reclamation r) {
        boolean estCloturee = serviceReponse.existsForReclamation(r.getId());

        VBox carte = new VBox(10);
        carte.setPadding(new Insets(18));
        carte.setMaxWidth(Double.MAX_VALUE);
        carte.getStyleClass().add("carte");

        // ── Texte ─────────────────────────────────────────────────────────────────
        Label sujet = new Label(r.getSujet());
        sujet.getStyleClass().add("carte-sujet");
        sujet.setWrapText(true);

        Label client = new Label("👤  " + r.getNomClient());
        client.getStyleClass().add("carte-info");

        Label email = new Label("✉️  " + r.getEmailClient());
        email.getStyleClass().add("carte-info");

        String descTxt = r.getDescription().length() > 70
                ? r.getDescription().substring(0, 70) + "..." : r.getDescription();
        Label description = new Label(descTxt);
        description.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");
        description.setWrapText(true);

        // ── Badges ────────────────────────────────────────────────────────────────
        HBox badges = new HBox(8);
        Label badgeStatut   = new Label(r.getStatut());
        badgeStatut.getStyleClass().add(classeStatut(r.getStatut()));
        Label badgePriorite = new Label(r.getPriorite());
        badgePriorite.getStyleClass().add(classePriorite(r.getPriorite()));
        badges.getChildren().addAll(badgeStatut, badgePriorite);

        if (estCloturee) {
            Label badgeClos = new Label("🔒 Clôturée");
            badgeClos.setStyle(
                    "-fx-background-color:#555; -fx-text-fill:#ccc;" +
                            "-fx-background-radius:8; -fx-padding:2 8 2 8; -fx-font-size:10px;"
            );
            badges.getChildren().add(badgeClos);
        }

        Label date = new Label("📅  " + (r.getDateReclamation() != null
                ? r.getDateReclamation().toLocalDate() : "N/A"));
        date.getStyleClass().add("carte-date");

        // ── Ajouter le texte EN PREMIER ───────────────────────────────────────────
        carte.getChildren().addAll(sujet, client, email, description, badges, date);

        // ── Photo APRÈS le texte ──────────────────────────────────────────────────
        if (r.getPhotoUrl() != null && !r.getPhotoUrl().isEmpty()) {
            Label lblChargement = new Label("⏳ Chargement image...");
            lblChargement.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");

            ImageView imgView = new ImageView();
            imgView.setFitWidth(240);
            imgView.setFitHeight(150);
            imgView.setPreserveRatio(true);
            imgView.setStyle("-fx-cursor: hand;");

            carte.getChildren().addAll(lblChargement, imgView);

            new Thread(() -> {
                try {
                    Image img = new Image(r.getPhotoUrl(), 240, 150, true, true, false);
                    javafx.application.Platform.runLater(() -> {
                        if (img.isError()) {
                            lblChargement.setText("❌ Image indisponible");
                        } else {
                            imgView.setImage(img);
                            lblChargement.setVisible(false);
                            lblChargement.setManaged(false);
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() ->
                            lblChargement.setText("❌ Erreur image"));
                }
            }).start();

            imgView.setOnMouseClicked(e -> ouvrirPhotoEnGrand(r.getPhotoUrl()));
        }

        // ── Séparateur + Boutons EN DERNIER ───────────────────────────────────────
        Separator sep = new Separator();
        HBox boutons = new HBox(8);
        boutons.setAlignment(Pos.CENTER_LEFT);

        Button btnMod = new Button("✏ Modifier");
        btnMod.setMinWidth(90);
        btnMod.getStyleClass().add("btn-modifier");

        Button btnSup = new Button("🗑 Supprimer");
        btnSup.setMinWidth(110);
        btnSup.getStyleClass().add("btn-supprimer");

        Button btnRep = new Button("💬 Réponses");
        btnRep.setMinWidth(100);
        btnRep.getStyleClass().add("btn-reponse");

        if (estCloturee) {
            btnMod.setDisable(true);
            btnSup.setDisable(true);
            btnMod.setStyle("-fx-background-color:#3a3a3a; -fx-text-fill:#666; -fx-background-radius:8;");
            btnSup.setStyle("-fx-background-color:#3a3a3a; -fx-text-fill:#666; -fx-background-radius:8;");
            Tooltip tip = new Tooltip("Réclamation clôturée — aucune modification possible.");
            Tooltip.install(btnMod, tip);
            Tooltip.install(btnSup, tip);
        } else {
            btnMod.setOnAction(e -> ouvrirModification(r));
            btnSup.setOnAction(e -> supprimerReclamation(r));
        }

        btnRep.setOnAction(e -> voirReponses(r));
        boutons.getChildren().addAll(btnMod, btnSup, btnRep);

        // ← UN SEUL addAll final pour sep et boutons
        carte.getChildren().addAll(sep, boutons);
        return carte;
    }

    // ── Helpers CSS ───────────────────────────────────────────────────────────
    private String classeStatut(String statut) {
        if (statut == null) return "badge-attente";
        return switch (statut) {
            case "en_cours" -> "badge-cours";
            case "resolue"  -> "badge-resolue";
            default         -> "badge-attente";
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

    // ── Filtre ────────────────────────────────────────────────────────────────
    @FXML
    private void appliquerFiltre() {
        String nom      = rechercheNom.getText().trim();
        String statut   = filtreStatut.getValue();
        String priorite = filtrePriorite.getValue();

        List<Reclamation> list;
        if (!nom.isEmpty() && statut.equals("Tous") && priorite.equals("Tous")) {
            list = service.searchByNom(nom);
        } else if (!nom.isEmpty()) {
            list = service.searchByNomStatutPriorite(nom, statut, priorite);
        } else {
            list = service.searchByStatutAndPriorite(statut, priorite);
        }
        chargerCartes(list);
    }

    @FXML
    private void reinitialiser() {
        rechercheNom.setText("");
        filtreStatut.setValue("Tous");
        filtrePriorite.setValue("Tous");
        chargerStats();
        chargerCartes(service.getAll());
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    @FXML
    private void ouvrirAjout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterReclamation.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Nouvelle Réclamation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerStats();
            chargerCartes(service.getAll());
        } catch (Exception e) {
            erreur("Erreur ouverture formulaire : " + e.getMessage());
        }
    }

    private void ouvrirModification(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierReclamation.fxml"));
            Parent root = loader.load();
            ModifierReclamation ctrl = loader.getController();
            ctrl.setReclamation(r);
            Stage stage = new Stage();
            stage.setTitle("Modifier Réclamation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerStats();
            chargerCartes(service.getAll());
        } catch (Exception e) {
            erreur("Erreur modification : " + e.getMessage());
        }
    }

    private void supprimerReclamation(Reclamation r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la réclamation : \"" + r.getSujet() + "\" ?");
        confirm.setHeaderText("Confirmation de suppression");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(r);
            chargerStats();
            chargerCartes(service.getAll());
        }
    }

    private void voirReponses(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReponses.fxml"));
            Parent root = loader.load();
            AfficherReponses ctrl = loader.getController();
            ctrl.setReclamation(r);
            Stage stage = new Stage();
            stage.setTitle("Réponses — " + r.getSujet());
            stage.setScene(new Scene(root, 600, 450));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerStats();
            chargerCartes(service.getAll());
        } catch (Exception e) {
            erreur("Erreur réponses : " + e.getMessage());
        }
    }

    private void ouvrirPhotoEnGrand(String url) {
        try {
            Stage stage = new Stage();
            ImageView iv = new ImageView(new Image(url));
            iv.setFitWidth(700);
            iv.setPreserveRatio(true);
            stage.setTitle("📷 Photo de la réclamation");
            stage.setScene(new Scene(new StackPane(iv)));
            stage.show();
        } catch (Exception e) {
            erreur("Impossible d'ouvrir la photo.");
        }
    }

    private void erreur(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}