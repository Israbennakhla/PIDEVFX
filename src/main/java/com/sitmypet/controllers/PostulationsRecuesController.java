package com.sitmypet.controllers;

import com.sitmypet.SessionContext;
import com.sitmypet.model.Announcement;
import com.sitmypet.model.Notification;
import com.sitmypet.model.Pet;
import com.sitmypet.model.Postulation;
import com.sitmypet.model.User;
import com.sitmypet.services.ServiceAnnouncement;
import com.sitmypet.services.ServiceNotification;
import com.sitmypet.services.ServicePet;
import com.sitmypet.services.ServicePostulation;
import com.sitmypet.services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Postulations sur les annonces du propriétaire connecté (accepter / refuser).
 */
public class PostulationsRecuesController {

    @FXML private ListView<Postulation> listPostulations;
    @FXML private Label                 labelCount;

    private final ServicePostulation  servicePost  = new ServicePostulation();
    private final ServiceAnnouncement serviceAnn   = new ServiceAnnouncement();
    private final ServicePet          servicePet   = new ServicePet();
    private final ServiceNotification serviceNotif = new ServiceNotification();
    private final ServiceUser         serviceUser  = new ServiceUser();

    private final ObservableList<Postulation> postulations = FXCollections.observableArrayList();
    private final Map<Integer, Announcement>  annCache     = new HashMap<>();
    private final Map<Integer, Pet>           petCache     = new HashMap<>();
    private final DateTimeFormatter           fmt          = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        initListView();
        loadData();
    }

    private int currentUserId() {
        User u = SessionContext.getCurrentUser();
        return u != null ? u.getId() : 0;
    }

    private void loadData() {
        annCache.clear();
        petCache.clear();
        int uid = currentUserId();
        List<Postulation> list = uid > 0 ? servicePost.getByProprietaireId(uid) : List.of();
        for (Pet p : servicePet.getByOwnerId(uid)) {
            petCache.put(p.getId(), p);
        }
        postulations.setAll(list);
        if (labelCount != null) {
            labelCount.setText(list.size() + " postulation(s)");
        }
    }

    private Announcement annoncePour(Postulation p) {
        return annCache.computeIfAbsent(p.getAnnouncementId(), serviceAnn::getById);
    }

    private void initListView() {
        listPostulations.setItems(postulations);
        listPostulations.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        listPostulations.setCellFactory(lv -> new ListCell<Postulation>() {
            @Override
            protected void updateItem(Postulation postulation, boolean empty) {
                super.updateItem(postulation, empty);
                if (empty || postulation == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color:transparent;");
                    return;
                }
                setGraphic(buildCard(postulation));
                setText(null);
                setStyle("-fx-background-color:transparent;-fx-padding:5 0;");
            }
        });
    }

    private HBox buildCard(Postulation p) {
        Announcement ann = annoncePour(p);
        Pet pet = ann != null ? petCache.get(ann.getPetId()) : null;

        String gardienLabel = serviceUser.getDisplayNameById(p.getGardienId());
        if (gardienLabel == null || gardienLabel.isBlank()) {
            gardienLabel = "Gardien";
        }

        String petName = pet != null
                ? pet.getName() + " (" + capitalize(pet.getTypePet()) + ")"
                : "Animal inconnu";

        Label titre = new Label(gardienLabel);
        titre.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        String statut = p.getStatut() != null ? p.getStatut() : "";
        Label badgeStatut = new Label(libelleStatut(statut));
        badgeStatut.setStyle(
                "-fx-background-color:" + couleurStatut(statut) + ";-fx-text-fill:white;"
                        + "-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:20px;-fx-padding:3 12;");

        Label animalLbl = new Label("Animal : " + petName);
        animalLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        String datePost = p.getDatePostulation() != null
                ? p.getDatePostulation().toLocalDate().format(fmt) : "—";
        Label dateLigne = new Label("Date : " + datePost);
        dateLigne.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        String deb = ann != null && ann.getDateDebut() != null
                ? ann.getDateDebut().toLocalDate().format(fmt) : "—";
        String fin = ann != null && ann.getDateFin() != null
                ? ann.getDateFin().toLocalDate().format(fmt) : "—";
        Label periodeLbl = new Label("Période annonce : " + deb + " — " + fin);
        periodeLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        VBox infoBox = new VBox(5, titre, animalLbl, dateLigne, periodeLbl);
        infoBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        VBox actionsCol = new VBox(8, badgeStatut);
        actionsCol.setAlignment(Pos.TOP_CENTER);
        actionsCol.setMinWidth(130);

        boolean enAttente = "EN_ATTENTE".equals(statut);
        if (enAttente) {
            Button accepter = new Button("Accepter");
            accepter.setStyle(
                    "-fx-background-color:#9b72e8;-fx-text-fill:white;-fx-font-size:12px;"
                            + "-fx-font-weight:bold;-fx-background-radius:8px;-fx-padding:8 16;-fx-cursor:hand;");
            Button refuser = new Button("Refuser");
            refuser.setStyle(
                    "-fx-background-color:white;-fx-text-fill:#e87272;-fx-font-size:12px;"
                            + "-fx-font-weight:bold;-fx-background-radius:8px;-fx-padding:8 16;-fx-cursor:hand;"
                            + "-fx-border-color:#e87272;-fx-border-radius:8px;");

            accepter.setOnAction(e -> accepterPostulation(p, petName, ann));
            refuser.setOnAction(e -> refuserPostulation(p));
            actionsCol.getChildren().addAll(accepter, refuser);
        }

        HBox card = new HBox(14, infoBox, actionsCol);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16px;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void accepterPostulation(Postulation p, String petName, Announcement ann) {
        servicePost.updateStatut(p.getId(), "ACCEPTE");

        String deb = ann != null && ann.getDateDebut() != null
                ? ann.getDateDebut().toLocalDate().format(fmt) : "—";
        String fin = ann != null && ann.getDateFin() != null
                ? ann.getDateFin().toLocalDate().format(fmt) : "—";
        String prix = ann != null
                ? "€" + (int) ann.getRemunerationMin() + " - €" + (int) ann.getRemunerationMax()
                : "—";

        String msgGardien = "Votre postulation a été acceptée !\n\n"
                + "Animal  : " + petName + "\n"
                + "Période : " + deb + " -> " + fin + "\n"
                + "Prix    : " + prix + "\n\n"
                + "Le propriétaire vous attend.";

        serviceNotif.envoyer(new Notification(
                p.getGardienId(),
                currentUserId(),
                p.getId(),
                msgGardien,
                "POSTULATION_ACCEPTEE"
        ));

        showInfo("Acceptée", "La postulation a été acceptée. Le gardien sera notifié.");
        loadData();
    }

    private void refuserPostulation(Postulation p) {
        servicePost.updateStatut(p.getId(), "REFUSE");
        showInfo("Refusée", "La postulation a été refusée.");
        loadData();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String libelleStatut(String s) {
        return switch (s == null ? "" : s) {
            case "EN_ATTENTE" -> "En attente";
            case "ACCEPTE" -> "Acceptée";
            case "REFUSE" -> "Refusée";
            case "ANNULE" -> "Annulée";
            default -> s;
        };
    }

    private String couleurStatut(String s) {
        return switch (s == null ? "" : s) {
            case "EN_ATTENTE" -> "#9b72e8";
            case "ACCEPTE" -> "#4caf50";
            case "REFUSE" -> "#e87272";
            case "ANNULE" -> "#aaaaaa";
            default -> "#888888";
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
