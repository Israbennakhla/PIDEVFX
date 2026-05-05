package com.sitmypet.controllers;

import com.sitmypet.model.Announcement;
import com.sitmypet.model.Pet;
import com.sitmypet.model.Postulation;
import com.sitmypet.services.ServiceAnnouncement;
import com.sitmypet.services.ServicePet;
import com.sitmypet.services.ServicePostulation;
import com.sitmypet.services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
 * Liste lecture seule de toutes les postulations (vue administrateur).
 */
public class AdminPostulationsOverviewController {

    @FXML private ListView<Postulation> listPostulations;
    @FXML private Label labelCount;

    private final ServicePostulation servicePost = new ServicePostulation();
    private final ServiceAnnouncement serviceAnn = new ServiceAnnouncement();
    private final ServicePet servicePet = new ServicePet();
    private final ServiceUser serviceUser = new ServiceUser();

    private final ObservableList<Postulation> postulations = FXCollections.observableArrayList();
    private final Map<Integer, Announcement> annMap = new HashMap<>();
    private final Map<Integer, Pet> petMap = new HashMap<>();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        refreshMaps();
        listPostulations.setItems(postulations);
        listPostulations.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        listPostulations.setCellFactory(lv -> new ListCell<Postulation>() {
            @Override
            protected void updateItem(Postulation p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color:transparent;");
                    return;
                }
                setGraphic(buildCard(p));
                setText(null);
                setStyle("-fx-background-color:transparent;-fx-padding:5 0;");
            }
        });
        loadData();
    }

    public void reloadFromDb() {
        refreshMaps();
        loadData();
    }

    private void refreshMaps() {
        annMap.clear();
        petMap.clear();
        for (Announcement a : serviceAnn.getAll()) {
            annMap.put(a.getId(), a);
        }
        for (Pet p : servicePet.getAll()) {
            petMap.put(p.getId(), p);
        }
    }

    private void loadData() {
        List<Postulation> list = servicePost.getAll();
        postulations.setAll(list);
        if (labelCount != null) {
            labelCount.setText(list.size() + " postulation(s)");
        }
    }

    private HBox buildCard(Postulation p) {
        Announcement ann = annMap.get(p.getAnnouncementId());
        Pet pet = ann != null ? petMap.get(ann.getPetId()) : null;

        String statutColor = statutColor(p.getStatut());
        Label badgeStatut = new Label(statutLabel(p.getStatut()));
        badgeStatut.setStyle(
                "-fx-background-color:" + statutColor + ";-fx-text-fill:white;"
                        + "-fx-font-size:11px;-fx-font-weight:bold;"
                        + "-fx-background-radius:20px;-fx-padding:3 12;");

        String titreNom = pet != null && pet.getName() != null && !pet.getName().isBlank()
                ? pet.getName().trim()
                : serviceUser.getDisplayNameById(p.getGardienId());
        Label titre = new Label(titreNom);
        titre.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        Label gardLbl = new Label("Gardien : " + serviceUser.getDisplayNameById(p.getGardienId()));
        gardLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        String proprioTxt = ann != null
                ? serviceUser.getDisplayNameById(ann.getUserId())
                : "—";
        Label proprioLbl = new Label("Propriétaire : " + proprioTxt);
        proprioLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#7c5cbf;");

        String typeDetail = "";
        if (pet != null) {
            String tp = pet.getTypePet() != null ? pet.getTypePet() : "";
            String br = pet.getBreed() != null && !pet.getBreed().isBlank() ? pet.getBreed() : "";
            typeDetail = tp.isEmpty() ? br : br.isEmpty() ? tp : tp + " · " + br;
        }

        String dateLbl = p.getDatePostulation() != null
                ? p.getDatePostulation().toLocalDate().format(FMT) : "—";
        Label dateLigne = new Label("Date : " + dateLbl);
        dateLigne.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        String adr = ann != null && ann.getAddress() != null ? ann.getAddress() : "—";
        Label adrLbl = new Label(adr);
        adrLbl.setWrapText(true);
        adrLbl.setMaxWidth(400);
        adrLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        String debut = ann != null && ann.getDateDebut() != null
                ? ann.getDateDebut().toLocalDate().format(FMT) : "—";
        String fin = ann != null && ann.getDateFin() != null
                ? ann.getDateFin().toLocalDate().format(FMT) : "—";
        Label periodeLbl = new Label("Période : " + debut + " – " + fin);
        periodeLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(titre, gardLbl, proprioLbl);
        if (!typeDetail.isEmpty()) {
            Label typeLbl = new Label(typeDetail);
            typeLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");
            infoBox.getChildren().add(typeLbl);
        }
        infoBox.getChildren().addAll(dateLigne, periodeLbl, adrLbl);
        infoBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        VBox rightBox = new VBox(10, badgeStatut);
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.setMinWidth(110);

        HBox card = new HBox(14, infoBox, rightBox);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle(
                "-fx-background-color:white;-fx-background-radius:16px;"
                        + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private static String statutLabel(String s) {
        return switch (s == null ? "" : s) {
            case "EN_ATTENTE" -> "En attente";
            case "ACCEPTE" -> "Acceptée";
            case "REFUSE" -> "Refusée";
            case "ANNULE" -> "Annulée";
            default -> s;
        };
    }

    private static String statutColor(String s) {
        return switch (s == null ? "" : s) {
            case "EN_ATTENTE" -> "#9b72e8";
            case "ACCEPTE" -> "#4caf50";
            case "REFUSE" -> "#e87272";
            case "ANNULE" -> "#aaaaaa";
            default -> "#888888";
        };
    }
}
