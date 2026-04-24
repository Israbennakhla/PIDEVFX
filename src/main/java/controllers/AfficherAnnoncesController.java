package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Announcement;
import model.Pet;
import services.ServiceAnnouncement;
import services.ServicePet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfficherAnnoncesController {

    @FXML private TextField          searchAdresse;
    @FXML private DatePicker         filterDateDebut;
    @FXML private DatePicker         filterDateFin;
    @FXML private ListView<Announcement> listAnnonces;
    @FXML private Label              labelPagination;
    @FXML private Button             btnPrev;
    @FXML private Button             btnNext;
    @FXML private HBox               pageButtons;
    @FXML private ComboBox<Integer>  comboPerPage;

    private final ServiceAnnouncement              serviceAnnouncement = new ServiceAnnouncement();
    private final ObservableList<Announcement>     allAnnonces         = FXCollections.observableArrayList();
    private FilteredList<Announcement>             filteredAnnonces;
    private final ObservableList<Announcement>     pageData            = FXCollections.observableArrayList();
    private final Map<Integer, Pet>                petById             = new HashMap<>();
    private final DateTimeFormatter                fmt                 = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int currentPage  = 1;
    private int itemsPerPage = 5;

    @FXML
    public void initialize() {
        chargerPets();
        initListView();
        initPaginationControls();
        initLiveFilters();
        loadData();
    }

    private void chargerPets() {
        for (Pet p : new ServicePet().getAll()) {
            petById.put(p.getId(), p);
        }
    }

    // ── ListView ──────────────────────────────────────────────
    private void initListView() {
        listAnnonces.setItems(pageData);
        listAnnonces.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        listAnnonces.setCellFactory(lv -> new ListCell<Announcement>() {
            @Override
            protected void updateItem(Announcement a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) {
                    setGraphic(null); setText(null);
                    setStyle("-fx-background-color:transparent;");
                    return;
                }
                setGraphic(buildCard(a));
                setText(null);
                setStyle("-fx-background-color:transparent;-fx-padding:5 0;");
            }
        });
    }

    // ── Construction carte ────────────────────────────────────
    private HBox buildCard(Announcement a) {
        Pet pet = petById.get(a.getPetId());
        String petName = pet != null
                ? pet.getName() + " (" + capitalize(pet.getTypePet()) + ")"
                : "Animal #" + a.getPetId();

        // ── Photo de l'animal ─────────────────────────────────
        ImageView iv = new ImageView();
        iv.setFitWidth(64);
        iv.setFitHeight(64);
        iv.setPreserveRatio(false);

        if (pet != null && pet.getImageName() != null && !pet.getImageName().isEmpty()) {
            Image img = loadPetImage(pet.getImageName());
            if (img != null && !img.isError()) {
                iv.setImage(img);
                // Clip circulaire
                Rectangle clip = new Rectangle(64, 64);
                clip.setArcWidth(64);
                clip.setArcHeight(64);
                iv.setClip(clip);
            } else {
                iv.setImage(null);
            }
        }

        // Conteneur photo avec fond coloré si pas d'image
        javafx.scene.layout.StackPane photoPane = new javafx.scene.layout.StackPane();
        photoPane.setPrefSize(64, 64);
        photoPane.setMinSize(64, 64);
        photoPane.setMaxSize(64, 64);
        photoPane.setStyle("-fx-background-color:#ede9f8;-fx-background-radius:32px;");

        if (iv.getImage() != null) {
            photoPane.getChildren().add(iv);
        } else {
            Label emoji = new Label("🐾");
            emoji.setStyle("-fx-font-size:26px;");
            photoPane.getChildren().add(emoji);
        }

        // ── Infos principales ─────────────────────────────────
        Label nomLabel = new Label(petName);
        nomLabel.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        String typeGarde = "CHEZ_MOI".equals(a.getCareType())
                ? "Chez proprietaire" : "En chenil";

        String debutStr = a.getDateDebut() != null
                ? a.getDateDebut().toLocalDate().format(fmt) : "—";
        String finStr   = a.getDateFin() != null
                ? a.getDateFin().toLocalDate().format(fmt) : "—";

        String horaires = parseHoraires(a.getVisitHours());
        String[] creneaux = horaires.equals("—") ? new String[0] : horaires.split(",");

        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.TOP_LEFT);

        infoBox.getChildren().add(infoLigne("Type de garde", typeGarde));
        infoBox.getChildren().add(infoLigne("Passages / jour", String.valueOf(a.getVisitPerDay())));

        // Créneaux horaires
        for (int i = 0; i < creneaux.length; i++) {
            String creneau = creneaux[i].trim();
            if (!creneau.isEmpty()) {
                infoBox.getChildren().add(infoLigne("Creneau " + (i + 1), creneau));
            }
        }

        infoBox.getChildren().add(infoLigne("Periode", debutStr + " - " + finStr));
        infoBox.getChildren().add(infoLigne("Prix",
                "€" + (int) a.getRemunerationMin() + ".00 - €" + (int) a.getRemunerationMax() + ".00"));

        // Adresse cliquable
        HBox adresseRow = buildAdresseRow(a.getAddress() != null ? a.getAddress() : "—");
        infoBox.getChildren().add(adresseRow);

        if (a.getServices() != null && !a.getServices().isEmpty()) {
            infoBox.getChildren().add(infoLigne("Services attendus", a.getServices()));
        }

        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // ── Boutons icônes ────────────────────────────────────
        Button btnEdit = new Button();
        btnEdit.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:#9b72e8;" +
                        "-fx-font-size:16px;-fx-cursor:hand;-fx-padding:4 8;");
        // Icone crayon unicode
        Label editIcon = new Label("\u270F");
        editIcon.setStyle("-fx-font-size:18px;-fx-text-fill:#9b72e8;");
        btnEdit.setGraphic(editIcon);
        btnEdit.setOnAction(e -> handleModifier(a));

        Button btnDel = new Button();
        Label delIcon = new Label("\uD83D\uDDD1");
        delIcon.setStyle("-fx-font-size:18px;-fx-text-fill:#e87272;");
        btnDel.setGraphic(delIcon);
        btnDel.setStyle(
                "-fx-background-color:transparent;-fx-font-size:16px;-fx-cursor:hand;-fx-padding:4 8;");
        btnDel.setOnAction(e -> handleSupprimer(a));

        VBox actionsBox = new VBox(8, btnEdit, btnDel);
        actionsBox.setAlignment(Pos.TOP_CENTER);
        actionsBox.setPadding(new Insets(4, 0, 0, 0));

        // ── Carte finale ──────────────────────────────────────
        HBox card = new HBox(16, photoPane, infoBox, actionsBox);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:16px;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ── Ligne label gras + valeur ─────────────────────────────
    private HBox infoLigne(String label, String value) {
        Label lbl = new Label(label + " :");
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;-fx-min-width:160px;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size:13px;-fx-text-fill:#5a5a8a;");
        val.setWrapText(true);
        HBox row = new HBox(6, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── Ligne adresse avec bouton carte ──────────────────────
    private HBox buildAdresseRow(String adresse) {
        Label lbl = new Label("Adresse :");
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;-fx-min-width:160px;");
        Label val = new Label(adresse.equals("—") ? "—" : adresse);
        val.setStyle("-fx-font-size:13px;-fx-text-fill:#5a5a8a;");
        val.setWrapText(true);
        val.setMaxWidth(340);

        HBox row = new HBox(6, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);

        if (!adresse.equals("—") && !adresse.isEmpty()) {
            Button btnMap = new Button("Voir carte");
            btnMap.setStyle(
                    "-fx-background-color:#e8f0fe;-fx-text-fill:#1a73e8;-fx-font-size:11px;" +
                            "-fx-font-weight:bold;-fx-background-radius:6px;-fx-padding:3 10;-fx-cursor:hand;" +
                            "-fx-border-color:#c5d8fc;-fx-border-radius:6px;");
            btnMap.setOnAction(e -> ouvrirCarte(adresse));
            row.getChildren().add(btnMap);
        }
        return row;
    }

    // ── Chargement image animal ───────────────────────────────
    private Image loadPetImage(String imageName) {
        if (imageName == null || imageName.isEmpty()) return null;
        String[] paths = {
                "images/" + imageName,
                System.getProperty("user.dir") + "/images/" + imageName,
                "src/main/resources/images/" + imageName
        };
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) {
                try { return new Image(f.toURI().toString(), 64, 64, false, true); }
                catch (Exception ignored) {}
            }
        }
        for (String rp : new String[]{"/images/" + imageName, "/" + imageName}) {
            try (InputStream is = getClass().getResourceAsStream(rp)) {
                if (is != null) return new Image(is, 64, 64, false, true);
            } catch (Exception ignored) {}
        }
        return null;
    }

    // ── Carte OpenStreetMap popup ─────────────────────────────
    private void ouvrirCarte(String adresse) {
        Stage mapStage = new Stage();
        mapStage.initModality(Modality.APPLICATION_MODAL);
        mapStage.initStyle(StageStyle.DECORATED);
        mapStage.setTitle("Carte - " + adresse);
        mapStage.setWidth(820);
        mapStage.setHeight(570);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        String encoded = URLEncoder.encode(adresse, StandardCharsets.UTF_8);

        String html = "<!DOCTYPE html><html><head>" +
                "<meta charset='UTF-8'/>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>* {margin:0;padding:0;} #map {width:100%;height:100vh;}</style>" +
                "</head><body><div id='map'></div><script>" +
                "var map = L.map('map').setView([36.8,10.18],7);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'," +
                "{attribution:'© OpenStreetMap',maxZoom:19}).addTo(map);" +
                "fetch('https://nominatim.openstreetmap.org/search?format=json&q=" + encoded + "&limit=1'," +
                "{headers:{'User-Agent':'SitMyPetApp/1.0'}})" +
                ".then(r=>r.json()).then(data=>{" +
                "  if(!data||!data.length) return;" +
                "  var lat=parseFloat(data[0].lat),lon=parseFloat(data[0].lon);" +
                "  map.setView([lat,lon],15);" +
                "  L.marker([lat,lon]).addTo(map).bindPopup('<b>" +
                adresse.replace("'", "\\'").replace("\"", "\\\"") +
                "</b>').openPopup();" +
                "}).catch(()=>{});" +
                "</script></body></html>";

        engine.loadContent(html, "text/html");

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-background-color:#9b72e8;-fx-text-fill:white;-fx-font-size:13px;" +
                "-fx-font-weight:bold;-fx-background-radius:8px;-fx-padding:8 24;-fx-cursor:hand;");
        btnFermer.setOnAction(e -> mapStage.close());

        Label lblAdr = new Label("  " + adresse);
        lblAdr.setStyle("-fx-font-size:12px;-fx-text-fill:#555;");

        HBox footer = new HBox(lblAdr, new Region(), btnFermer);
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10, 16, 10, 16));
        footer.setStyle("-fx-background-color:#f8f8f8;-fx-border-color:#e0e0e0;-fx-border-width:1 0 0 0;");

        VBox root = new VBox(webView, footer);
        VBox.setVgrow(webView, Priority.ALWAYS);
        mapStage.setScene(new Scene(root));
        mapStage.show();
    }

    // ── Pagination ────────────────────────────────────────────
    private void initPaginationControls() {
        comboPerPage.setItems(FXCollections.observableArrayList(5, 10, 15, 20));
        comboPerPage.setValue(itemsPerPage);
        comboPerPage.valueProperty().addListener((obs, o, n) -> {
            if (n != null) { itemsPerPage = n; currentPage = 1; updatePage(); }
        });
    }

    private void initLiveFilters() {
        filteredAnnonces = new FilteredList<>(allAnnonces, a -> true);
        searchAdresse.textProperty()   .addListener((obs, o, n) -> { currentPage = 1; applyFilters(); });
        filterDateDebut.valueProperty().addListener((obs, o, n) -> { currentPage = 1; applyFilters(); });
        filterDateFin.valueProperty()  .addListener((obs, o, n) -> { currentPage = 1; applyFilters(); });
    }

    private void applyFilters() {
        String adresse  = searchAdresse.getText() == null ? "" : searchAdresse.getText().trim().toLowerCase();
        LocalDate debut = filterDateDebut.getValue();
        LocalDate fin   = filterDateFin.getValue();
        filteredAnnonces.setPredicate(a -> {
            if (!adresse.isEmpty() && (a.getAddress() == null ||
                    !a.getAddress().toLowerCase().contains(adresse))) return false;
            if (debut != null && a.getDateDebut() != null &&
                    a.getDateDebut().toLocalDate().isBefore(debut)) return false;
            if (fin != null && a.getDateFin() != null &&
                    a.getDateFin().toLocalDate().isAfter(fin)) return false;
            return true;
        });
        updatePage();
    }

    private void loadData() {
        allAnnonces.setAll(serviceAnnouncement.getAll());
        currentPage = 1;
        applyFilters();
    }

    private void updatePage() {
        List<Announcement> filtered = filteredAnnonces;
        int total      = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / itemsPerPage));
        if (currentPage > totalPages) currentPage = totalPages;
        int from = (currentPage - 1) * itemsPerPage;
        int to   = Math.min(from + itemsPerPage, total);
        pageData.setAll(filtered.subList(from, to));

        labelPagination.setText(total == 0
                ? "Aucun resultat"
                : "Affichage " + (from + 1) + " - " + to + " sur " + total + " annonces");

        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);

        pageButtons.getChildren().clear();
        for (int i = 1; i <= totalPages; i++) {
            final int page = i;
            Button btn = new Button(String.valueOf(i));
            btn.setStyle(i == currentPage
                    ? "-fx-background-color:#9b72e8;-fx-text-fill:white;-fx-font-size:12px;" +
                    "-fx-background-radius:6px;-fx-padding:4 10;-fx-cursor:hand;-fx-font-weight:bold;"
                    : "-fx-background-color:#eeeeee;-fx-text-fill:#333;-fx-font-size:12px;" +
                    "-fx-background-radius:6px;-fx-padding:4 10;-fx-cursor:hand;");
            btn.setOnAction(e -> { currentPage = page; updatePage(); });
            pageButtons.getChildren().add(btn);
        }
    }

    @FXML private void handlePrevPage()   { if (currentPage > 1) { currentPage--; updatePage(); } }
    @FXML private void handleNextPage()   { currentPage++; updatePage(); }
    @FXML private void handleRechercher() { applyFilters(); }

    @FXML
    private void handleReinitialiser() {
        searchAdresse.clear();
        filterDateDebut.setValue(null);
        filterDateFin.setValue(null);
    }

    @FXML private void handleNouvelle() { naviguer("/CreerAnnonce.fxml", "Creer une annonce"); }

    private void handleModifier(Announcement annonce) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierAnnonce.fxml"));
            Parent root = loader.load();
            ModifierAnnonceController ctrl = loader.getController();
            // Construire petMap pour le controller modifier
            Map<Integer, String> petMap = new HashMap<>();
            petById.forEach((id, p) -> petMap.put(id, p.getName() + " (" + p.getTypePet().toLowerCase() + ")"));
            ctrl.setAnnonce(annonce, petMap);
            Stage stage = (Stage) listAnnonces.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier une annonce");
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleSupprimer(Announcement annonce) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous supprimer cette annonce ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) { serviceAnnouncement.delete(annonce); loadData(); }
        });
    }

    @FXML private void handleNavDashboard()    { naviguer("/Dashboard.fxml",           "Dashboard"); }
    @FXML private void handleNavAnnonces()     { naviguer("/AfficherAnnonces.fxml",    "Mes Annonces"); }
    @FXML private void handleNavEvenements()   { naviguer("/AfficherEvenements.fxml",  "Evenements"); }
    @FXML private void handleNavAnimaux()      { naviguer("/AfficherAnimales.fxml",    "Mes Animaux"); }
    @FXML private void handleNavReclamations() { naviguer("/AfficherReclamations.fxml","Reclamations"); }

    private void naviguer(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Parent root  = FXMLLoader.load(resource);
            Stage  stage = (Stage) listAnnonces.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.setTitle(titre); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String parseHoraires(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) return "—";
        return json.replaceAll("[\\[\\]\"]", "").trim();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}