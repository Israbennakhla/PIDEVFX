package com.sitmypet.controllers;

import com.sitmypet.model.Announcement;
import com.sitmypet.model.Pet;
import com.sitmypet.services.ServiceAnnouncement;
import com.sitmypet.services.ServicePet;
import com.sitmypet.services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Liste lecture seule de toutes les annonces (vue administrateur).
 */
public class AdminAnnoncesOverviewController {

    @FXML private TextField searchField;
    @FXML private ListView<Announcement> listAnnonces;
    @FXML private Label labelCount;

    private final ServiceAnnouncement serviceAnn = new ServiceAnnouncement();
    private final ServicePet servicePet = new ServicePet();
    private final ServiceUser serviceUser = new ServiceUser();

    private final ObservableList<Announcement> all = FXCollections.observableArrayList();
    private FilteredList<Announcement> filtered;
    private final Map<Integer, Pet> petById = new HashMap<>();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        for (Pet p : servicePet.getAll()) {
            petById.put(p.getId(), p);
        }

        filtered = new FilteredList<>(all, a -> true);
        listAnnonces.setItems(filtered);
        listAnnonces.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        listAnnonces.setCellFactory(lv -> new ListCell<Announcement>() {
            @Override
            protected void updateItem(Announcement a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                setGraphic(buildCard(a));
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 6 0;");
            }
        });

        searchField.textProperty().addListener((o, x, n) -> applyFilter());
        loadData();
    }

    public void reloadFromDb() {
        petById.clear();
        for (Pet p : servicePet.getAll()) {
            petById.put(p.getId(), p);
        }
        loadData();
    }

    private void loadData() {
        List<Announcement> list = serviceAnn.getAll();
        all.setAll(list);
        labelCount.setText(list.size() + " annonce(s)");
        applyFilter();
    }

    private HBox buildCard(Announcement a) {
        Pet pet = petById.get(a.getPetId());

        String proprio = serviceUser.getDisplayNameById(a.getUserId());

        String titreText = pet != null && pet.getName() != null && !pet.getName().isBlank()
                ? pet.getName().trim()
                : "Annonce";
        Label titre = new Label(titreText);
        titre.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        Label proprioLbl = new Label("Propriétaire : " + proprio);
        proprioLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#7c5cbf;");

        String typeDetail = "";
        if (pet != null) {
            String tp = pet.getTypePet() != null ? pet.getTypePet() : "";
            String br = pet.getBreed() != null && !pet.getBreed().isBlank() ? pet.getBreed() : "";
            typeDetail = tp.isEmpty() ? br : br.isEmpty() ? tp : tp + " · " + br;
        }

        String debut = a.getDateDebut() != null ? a.getDateDebut().toLocalDate().format(FMT) : "—";
        String fin = a.getDateFin() != null ? a.getDateFin().toLocalDate().format(FMT) : "—";
        Label periode = new Label("Période : " + debut + " → " + fin);
        periode.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        String prix = "€" + (int) a.getRemunerationMin() + " – €" + (int) a.getRemunerationMax();
        Label prixLbl = new Label("Rémunération : " + prix + "  ·  " + (a.getCareType() != null ? a.getCareType() : ""));
        prixLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");

        String addr = a.getAddress() != null ? a.getAddress() : "—";
        Label adrLbl = new Label(addr);
        adrLbl.setWrapText(true);
        adrLbl.setMaxWidth(520);
        adrLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#444466;");

        VBox col = new VBox(6);
        col.getChildren().addAll(titre, proprioLbl);
        if (!typeDetail.isEmpty()) {
            Label typeLbl = new Label(typeDetail);
            typeLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");
            col.getChildren().add(typeLbl);
        }
        col.getChildren().addAll(periode, prixLbl, adrLbl);
        col.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(col, Priority.ALWAYS);

        HBox card = new HBox(col);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16px;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(a -> {
            if (q.isEmpty()) return true;
            String addr = a.getAddress() != null ? a.getAddress().toLowerCase() : "";
            String srv = a.getServices() != null ? a.getServices().toLowerCase() : "";
            String idStr = String.valueOf(a.getId());
            return addr.contains(q) || srv.contains(q) || idStr.contains(q);
        });
    }

    @FXML
    private void handleRechercher() {
        applyFilter();
    }

    @FXML
    private void handleReinitialiser() {
        searchField.clear();
        applyFilter();
    }
}
