package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Announcement;
import model.Pet;
import services.ServiceAnnouncement;
import services.ServicePet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AfficherAnnoncesController {

    @FXML private TextField  searchAdresse;
    @FXML private DatePicker filterDateDebut;
    @FXML private DatePicker filterDateFin;

    @FXML private TableView<Announcement>            tableAnnonces;
    @FXML private TableColumn<Announcement, String>  colAnimal;
    @FXML private TableColumn<Announcement, String>  colTypeGarde;
    @FXML private TableColumn<Announcement, String>  colVisites;
    @FXML private TableColumn<Announcement, String>  colHoraires;
    @FXML private TableColumn<Announcement, String>  colDateDebut;
    @FXML private TableColumn<Announcement, String>  colDateFin;
    @FXML private TableColumn<Announcement, String>  colRemuMin;
    @FXML private TableColumn<Announcement, String>  colRemuMax;
    @FXML private TableColumn<Announcement, String>  colAdresse;
    @FXML private TableColumn<Announcement, String>  colServices;
    @FXML private TableColumn<Announcement, Void>    colActions;

    private final ServiceAnnouncement          serviceAnnouncement = new ServiceAnnouncement();
    private final ObservableList<Announcement> allAnnonces         = FXCollections.observableArrayList();
    private FilteredList<Announcement>         filteredAnnonces;
    private final Map<Integer, String>         petMap              = new HashMap<>();
    private final DateTimeFormatter            fmt                 = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        colAnimal   .setPrefWidth(100); colAnimal   .setMinWidth(80);
        colTypeGarde.setPrefWidth(90);  colTypeGarde.setMinWidth(70);
        colVisites  .setPrefWidth(60);  colVisites  .setMinWidth(50);
        colHoraires .setPrefWidth(90);  colHoraires .setMinWidth(70);
        colDateDebut.setPrefWidth(90);  colDateDebut.setMinWidth(80);
        colDateFin  .setPrefWidth(90);  colDateFin  .setMinWidth(80);
        colRemuMin  .setPrefWidth(80);  colRemuMin  .setMinWidth(60);
        colRemuMax  .setPrefWidth(80);  colRemuMax  .setMinWidth(60);
        colAdresse  .setPrefWidth(100); colAdresse  .setMinWidth(80);
        colServices .setPrefWidth(100); colServices .setMinWidth(80);
        colActions.setPrefWidth(110); colActions.setMinWidth(110);
        colActions.setMaxWidth(110);  colActions.setResizable(false);

        tableAnnonces.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        chargerPets();
        initColumns();
        initLiveFilters();
        loadData();
    }

    private void chargerPets() {
        for (Pet p : new ServicePet().getAll())
            petMap.put(p.getId(), p.getName() + " (" + p.getTypePet().toLowerCase() + ")");
    }

    private void initLiveFilters() {
        filteredAnnonces = new FilteredList<>(allAnnonces, a -> true);
        searchAdresse.textProperty().addListener((obs, o, n) -> applyFilters());
        filterDateDebut.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterDateFin.valueProperty().addListener((obs, o, n) -> applyFilters());
        tableAnnonces.setItems(filteredAnnonces);
    }

    private void applyFilters() {
        filteredAnnonces.setPredicate(a -> {
            String adresse  = searchAdresse.getText().trim().toLowerCase();
            LocalDate debut = filterDateDebut.getValue();
            LocalDate fin   = filterDateFin.getValue();
            if (!adresse.isEmpty() && (a.getAddress() == null ||
                    !a.getAddress().toLowerCase().contains(adresse))) return false;
            if (debut != null && a.getDateDebut() != null &&
                    a.getDateDebut().toLocalDate().isBefore(debut)) return false;
            if (fin != null && a.getDateFin() != null &&
                    a.getDateFin().toLocalDate().isAfter(fin)) return false;
            return true;
        });
    }

    private void loadData() {
        allAnnonces.setAll(serviceAnnouncement.getAll());
        applyFilters();
    }

    private void initColumns() {
        colAnimal.setCellValueFactory(data ->
                new SimpleStringProperty(petMap.getOrDefault(
                        data.getValue().getPetId(), "Animal #" + data.getValue().getPetId())));
        colTypeGarde.setCellValueFactory(data ->
                new SimpleStringProperty("CHEZ_MOI".equals(data.getValue().getCareType()) ? "Chez moi" : "En chenil"));
        colVisites.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getVisitPerDay() + " /j"));
        colHoraires.setCellValueFactory(data ->
                new SimpleStringProperty(parseHoraires(data.getValue().getVisitHours())));
        colDateDebut.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDateDebut() != null
                        ? data.getValue().getDateDebut().toLocalDate().format(fmt) : ""));
        colDateFin.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDateFin() != null
                        ? data.getValue().getDateFin().toLocalDate().format(fmt) : ""));
        colRemuMin.setCellValueFactory(data ->
                new SimpleStringProperty((int) data.getValue().getRemunerationMin() + " €"));
        colRemuMax.setCellValueFactory(data ->
                new SimpleStringProperty((int) data.getValue().getRemunerationMax() + " €"));
        colAdresse.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAddress() != null ? data.getValue().getAddress() : ""));
        colServices.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getServices() != null ? data.getValue().getServices() : ""));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final HBox   box       = new HBox(6, btnEdit, btnDelete);
            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color:#9b72e8;-fx-text-fill:white;-fx-background-radius:6px;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:4 8;");
                btnDelete.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-background-radius:6px;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:4 8;");
                btnEdit.setOnAction(e -> { Announcement a = getTableRow().getItem(); if (a != null) handleModifier(a); });
                btnDelete.setOnAction(e -> { Announcement a = getTableRow().getItem(); if (a != null) handleSupprimer(a); });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML private void handleRechercher() { applyFilters(); }

    @FXML
    private void handleReinitialiser() {
        searchAdresse.clear();
        filterDateDebut.setValue(null);
        filterDateFin.setValue(null);
    }

    @FXML
    private void handleNouvelle() { naviguer("/CreerAnnonce.fxml", "Créer une annonce"); }

    private void handleModifier(Announcement annonce) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierAnnonce.fxml"));
            Parent root = loader.load();
            ModifierAnnonceController ctrl = loader.getController();
            ctrl.setAnnonce(annonce, petMap);
            Stage stage = (Stage) tableAnnonces.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier une annonce");
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleSupprimer(Announcement annonce) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer"); confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous supprimer cette annonce ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) { serviceAnnouncement.delete(annonce); loadData(); }
        });
    }

    // ── Navigation ────────────────────────────────────────────
    @FXML private void handleNavDashboard()    { naviguer("/Dashboard.fxml",         "Dashboard"); }
    @FXML private void handleNavAnnonces()     { naviguer("/AfficherAnnonces.fxml",  "Mes Annonces"); }
    @FXML private void handleNavEvenements()   { naviguer("/AfficherEvenements.fxml","Événements"); }
    @FXML private void handleNavAnimaux()      { naviguer("/AfficherAnimales.fxml",  "Mes Animaux"); }
    @FXML private void handleNavReclamations() { naviguer("/AfficherReclamations.fxml","Réclamations"); }

    private void naviguer(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) tableAnnonces.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titre);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String parseHoraires(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) return "—";
        return json.replaceAll("[\\[\\]\"]", "").trim().replace(",", ", ");
    }
}