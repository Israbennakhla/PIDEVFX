package com.sitmypet.controllers;

import com.sitmypet.SessionContext;
import com.sitmypet.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.sitmypet.model.Announcement;
import com.sitmypet.model.Pet;
import com.sitmypet.model.Postulation;
import com.sitmypet.services.ServiceAnnouncement;
import com.sitmypet.services.ServicePet;
import com.sitmypet.services.ServicePostulation;
import com.sitmypet.services.ServiceUser;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostulationsGardienController {

    @FXML private ListView<Postulation> listPostulations;
    @FXML private Label                 labelCount;

    private final ServicePostulation  servicePost = new ServicePostulation();
    private final ServiceAnnouncement serviceAnn  = new ServiceAnnouncement();
    private final ServicePet          servicePet  = new ServicePet();
    private final ServiceUser        serviceUser = new ServiceUser();

    private final ObservableList<Postulation> postulations = FXCollections.observableArrayList();
    private final Map<Integer, Announcement>  annMap       = new HashMap<>();
    private final Map<Integer, Pet>           petMap       = new HashMap<>();
    private final DateTimeFormatter           fmt          = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        chargerDonnees();
        initListView();
        loadData();
    }

    private void chargerDonnees() {
        for (Announcement a : serviceAnn.getAll()) annMap.put(a.getId(), a);
        for (Pet p : servicePet.getAll()) petMap.put(p.getId(), p);
    }

    private void loadData() {
        User u = SessionContext.getCurrentUser();
        int gid = u != null ? u.getId() : 0;
        postulations.setAll(gid > 0 ? servicePost.getByGardien(gid) : List.of());
        if (labelCount != null) {
            labelCount.setText(postulations.size() + " postulation(s)");
        }
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

        String nomPostulant = serviceUser.getDisplayNameById(p.getGardienId());
        Label titre = new Label(nomPostulant);
        titre.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        String sousTitreTxt = "";
        if (ann != null) {
            sousTitreTxt = "Propriétaire : " + serviceUser.getDisplayNameById(ann.getUserId());
        }
        Label proprioLbl = sousTitreTxt.isEmpty()
                ? null
                : new Label(sousTitreTxt);
        if (proprioLbl != null) {
            proprioLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#7c5cbf;");
        }

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

        VBox infoBox = new VBox(5);
        infoBox.getChildren().add(titre);
        if (proprioLbl != null) {
            infoBox.getChildren().add(proprioLbl);
        }
        infoBox.getChildren().addAll(animalLbl, dateLigne, periodeLbl, prixLbl, adrLbl);
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

}