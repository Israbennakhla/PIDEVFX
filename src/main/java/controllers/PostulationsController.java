package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Announcement;
import model.Pet;
import model.Postulation;
import services.ServiceAnnouncement;
import services.ServicePet;
import services.ServicePostulation;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PostulationsController {

    @FXML private ListView<Postulation> listPostulations;
    @FXML private Label                 labelCount;

    private final ServicePostulation  servicePost = new ServicePostulation();
    private final ServiceAnnouncement serviceAnn  = new ServiceAnnouncement();
    private final ServicePet          servicePet  = new ServicePet();

    private final ObservableList<Postulation> postulations = FXCollections.observableArrayList();
    private final Map<Integer, Announcement>  annMap       = new HashMap<>();
    private final Map<Integer, Pet>           petMap       = new HashMap<>();
    private final DateTimeFormatter           fmt          = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int CURRENT_GARDIEN_ID = 2;

    @FXML
    public void initialize() {
        servicePost.createTableIfNotExists();
        chargerDonnees();
        initListView();
        loadData();
    }

    private void chargerDonnees() {
        for (Announcement a : serviceAnn.getAll()) annMap.put(a.getId(), a);
        for (Pet p : servicePet.getAll()) petMap.put(p.getId(), p);
    }

    private void loadData() {
        postulations.setAll(servicePost.getByGardien(CURRENT_GARDIEN_ID));
        if (labelCount != null)
            labelCount.setText(postulations.size() + " postulation(s)");
    }

    private void initListView() {
        listPostulations.setItems(postulations);
        listPostulations.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        listPostulations.setCellFactory(lv -> new ListCell<Postulation>() {
            @Override
            protected void updateItem(Postulation p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setGraphic(null); setText(null);
                    setStyle("-fx-background-color:transparent;");
                    return;
                }
                setGraphic(buildCard(p));
                setText(null);
                setStyle("-fx-background-color:transparent;-fx-padding:5 0;");
            }
        });
    }

    private HBox buildCard(Postulation p) {
        Announcement ann = annMap.get(p.getAnnouncementId());
        Pet pet = ann != null ? petMap.get(ann.getPetId()) : null;

        // Statut badge
        String statutText  = statutLabel(p.getStatut());
        String statutColor = statutColor(p.getStatut());

        Label badgeStatut = new Label(statutText);
        badgeStatut.setStyle(
                "-fx-background-color:" + statutColor + ";-fx-text-fill:white;" +
                        "-fx-font-size:11px;-fx-font-weight:bold;" +
                        "-fx-background-radius:20px;-fx-padding:3 12;");

        // Infos annonce
        String petName = pet != null
                ? pet.getName() + " (" + pet.getTypePet().toLowerCase() + ")"
                : "Animal inconnu";

        Label titre = new Label("Postulation #" + p.getId());
        titre.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        Label animalLbl = new Label("Animal : " + petName);
        animalLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        String dateLbl = p.getDatePostulation() != null
                ? p.getDatePostulation().toLocalDate().format(fmt) : "—";
        Label dateLigne = new Label("Date de postulation : " + dateLbl);
        dateLigne.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        String adr = ann != null && ann.getAddress() != null ? ann.getAddress() : "—";
        Label adrLbl = new Label("Adresse annonce : " + adr);
        adrLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");
        adrLbl.setWrapText(true);
        adrLbl.setMaxWidth(400);

        String debut = ann != null && ann.getDateDebut() != null
                ? ann.getDateDebut().toLocalDate().format(fmt) : "—";
        String fin   = ann != null && ann.getDateFin() != null
                ? ann.getDateFin().toLocalDate().format(fmt) : "—";
        Label periodeLbl = new Label("Periode : " + debut + " - " + fin);
        periodeLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        String prix = ann != null
                ? "€" + (int) ann.getRemunerationMin() + " - €" + (int) ann.getRemunerationMax()
                : "—";
        Label prixLbl = new Label("Remuneration : " + prix);
        prixLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        VBox infoBox = new VBox(5, titre, animalLbl, dateLigne, periodeLbl, prixLbl, adrLbl);
        infoBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // ── Bouton Annuler ────────────────────────────────────
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle(
                "-fx-background-color:#fff0f0;-fx-text-fill:#e87272;-fx-font-size:12px;" +
                        "-fx-font-weight:bold;-fx-background-radius:8px;-fx-padding:8 18;-fx-cursor:hand;" +
                        "-fx-border-color:#f5c5c5;-fx-border-radius:8px;");
        // Masquer le bouton si postulation ACCEPTE, REFUSE ou ANNULE
        boolean nonAnnulable = "ACCEPTE".equals(p.getStatut())
                || "REFUSE".equals(p.getStatut())
                || "ANNULE".equals(p.getStatut());
        btnAnnuler.setDisable(nonAnnulable);
        btnAnnuler.setVisible(!nonAnnulable);
        btnAnnuler.setManaged(!nonAnnulable);
        btnAnnuler.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Annuler la postulation");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous annuler cette postulation ?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    servicePost.delete(p);
                    loadData();
                }
            });
        });

        VBox rightBox = new VBox(10, badgeStatut, btnAnnuler);
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.setMinWidth(110);

        HBox card = new HBox(14, infoBox, rightBox);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle(
                "-fx-background-color:white;-fx-background-radius:16px;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private String statutLabel(String s) {
        return switch (s == null ? "" : s) {
            case "EN_ATTENTE" -> "En attente";
            case "ACCEPTE"    -> "Accepte";
            case "REFUSE"     -> "Refuse";
            case "ANNULE"     -> "Annule";
            default           -> s;
        };
    }

    private String statutColor(String s) {
        return switch (s == null ? "" : s) {
            case "EN_ATTENTE" -> "#9b72e8";
            case "ACCEPTE"    -> "#4caf50";
            case "REFUSE"     -> "#e87272";
            case "ANNULE"     -> "#aaaaaa";
            default           -> "#888888";
        };
    }

    @FXML private void handleNavDashboard()    { naviguer("/Dashboard.fxml",           "Dashboard"); }
    @FXML private void handleNavAccueil()      { naviguer("/Accueil.fxml",             "Accueil"); }
    @FXML private void handleNavPostulations() { naviguer("/Postulations.fxml",        "Mes Postulations"); }
    @FXML private void handleNavAnnonces()     { naviguer("/AfficherAnnonces.fxml",    "Mes Annonces"); }
    @FXML private void handleNavEvenements()   { naviguer("/AfficherEvenements.fxml",  "Evenements"); }
    @FXML private void handleNavAnimaux()      { naviguer("/AfficherAnimales.fxml",    "Mes Animaux"); }
    @FXML private void handleNavReclamations() { naviguer("/AfficherReclamations.fxml","Reclamations"); }

    private void naviguer(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Parent root  = FXMLLoader.load(resource);
            Stage  stage = (Stage) listPostulations.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.setTitle(titre); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}