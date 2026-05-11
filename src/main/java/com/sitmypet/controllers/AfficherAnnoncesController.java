package com.sitmypet.controllers;

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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.sitmypet.model.Announcement;
import com.sitmypet.model.Notification;
import com.sitmypet.model.Pet;
import com.sitmypet.model.Postulation;
import com.sitmypet.services.ServiceAnnouncement;
import com.sitmypet.services.ServiceNotification;
import com.sitmypet.services.ServicePet;
import com.sitmypet.services.ServicePostulation;

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

    @FXML private HBox                   navbarHBox;
    @FXML private TextField              searchAdresse;
    @FXML private DatePicker             filterDateDebut;
    @FXML private DatePicker             filterDateFin;
    @FXML private ListView<Announcement> listAnnonces;
    @FXML private Label                  labelPagination;
    @FXML private Button                 btnPrev;
    @FXML private Button                 btnNext;
    @FXML private HBox                   pageButtons;
    @FXML private ComboBox<Integer>      comboPerPage;

    private final ServiceAnnouncement          serviceAnn   = new ServiceAnnouncement();
    private final ServiceNotification          serviceNotif = new ServiceNotification();
    private final ServicePostulation           servicePost  = new ServicePostulation();
    private final ObservableList<Announcement> allAnnonces  = FXCollections.observableArrayList();
    private FilteredList<Announcement>         filteredAnnonces;
    private final ObservableList<Announcement> pageData     = FXCollections.observableArrayList();
    private final Map<Integer, Pet>            petById      = new HashMap<>();
    private final DateTimeFormatter            fmt          = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int currentPage  = 1;
    private int itemsPerPage = 5;

    private static final int CURRENT_PROPRIETAIRE_ID = 1;
    private static final int CURRENT_GARDIEN_ID      = 2;

    private NotificationBadge notifBadge;

    @FXML
    public void initialize() {
        serviceNotif.createTableIfNotExists();
        chargerPets();
        initListView();
        initPaginationControls();
        initLiveFilters();
        loadData();

        // ── Badge 🔔 propriétaire avec infos chat ─────────────
        notifBadge = new NotificationBadge(CURRENT_PROPRIETAIRE_ID, this::onNotifCliquee);
        notifBadge.setChatInfo(
                CURRENT_GARDIEN_ID,
                "Proprietaire #" + CURRENT_PROPRIETAIRE_ID,
                "Gardien #" + CURRENT_GARDIEN_ID
        );
        if (navbarHBox != null) {
            int idx = navbarHBox.getChildren().size() - 1;
            navbarHBox.getChildren().add(idx, notifBadge.getView());
        }
        notifBadge.startPolling();
    }

    // ── Callback badge propriétaire ───────────────────────────
    private void onNotifCliquee(Notification n) {

        if ("NOUVELLE_POSTULATION".equals(n.getType())) {
            int postulationId = n.getPostulationId();
            Postulation post  = servicePost.getById(postulationId);

            if (post == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Postulation introuvable."); return;
            }

            Announcement ann = trouverAnnonce(post.getAnnouncementId());
            Pet pet = ann != null ? petById.get(ann.getPetId()) : null;
            String petName = pet != null
                    ? pet.getName() + " (" + capitalize(pet.getTypePet()) + ")"
                    : "Animal inconnu";
            String deb = ann != null && ann.getDateDebut() != null
                    ? ann.getDateDebut().toLocalDate().format(fmt) : "—";
            String fin = ann != null && ann.getDateFin() != null
                    ? ann.getDateFin().toLocalDate().format(fmt) : "—";
            String prix = ann != null
                    ? "€" + (int) ann.getRemunerationMin() + " - €" + (int) ann.getRemunerationMax()
                    : "—";

            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setTitle("Nouvelle postulation !");
            dialog.setHeaderText("Gardien #" + n.getExpediteurId() + " veut garder " + petName);
            dialog.setContentText(
                    "Animal  : " + petName + "\n" +
                            "Periode : " + deb + " -> " + fin + "\n" +
                            "Prix    : " + prix + "\n\n" +
                            "Accepter cette postulation ?");

            ButtonType accepter = new ButtonType("Accepter");
            ButtonType refuser  = new ButtonType("Refuser");
            dialog.getButtonTypes().setAll(accepter, refuser);

            dialog.showAndWait().ifPresent(result -> {
                if (result == accepter) {
                    servicePost.updateStatut(postulationId, "ACCEPTE");

                    String msgGardien = "Votre postulation a ete acceptee !\n\n"
                            + "Animal  : " + petName + "\n"
                            + "Periode : " + deb + " -> " + fin + "\n"
                            + "Prix    : " + prix + "\n\n"
                            + "Le proprietaire #" + CURRENT_PROPRIETAIRE_ID + " vous attend.";

                    serviceNotif.envoyer(new Notification(
                            n.getExpediteurId(),
                            CURRENT_PROPRIETAIRE_ID,
                            postulationId,
                            msgGardien,
                            "POSTULATION_ACCEPTEE"
                    ));

                    showAlert(Alert.AlertType.INFORMATION, "Accepte",
                            "La postulation a ete acceptee. Le gardien sera notifie.");

                } else if (result == refuser) {
                    servicePost.updateStatut(postulationId, "REFUSE");
                    showAlert(Alert.AlertType.INFORMATION, "Refuse", "La postulation a ete refusee.");
                }
            });

        } else if ("REFUSER".equals(n.getType())) {
            servicePost.updateStatut(n.getPostulationId(), "REFUSE");
        }
    }

    private Announcement trouverAnnonce(int id) {
        for (Announcement a : allAnnonces) if (a.getId() == id) return a;
        for (Announcement a : serviceAnn.getAll()) if (a.getId() == id) return a;
        return null;
    }

    private void chargerPets() {
        for (Pet p : new ServicePet().getAll()) petById.put(p.getId(), p);
    }

    private void initListView() {
        listAnnonces.setItems(pageData);
        listAnnonces.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        listAnnonces.setCellFactory(lv -> new ListCell<Announcement>() {
            @Override
            protected void updateItem(Announcement a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) {
                    setGraphic(null); setText(null);
                    setStyle("-fx-background-color:transparent;"); return;
                }
                setGraphic(buildCard(a));
                setText(null);
                setStyle("-fx-background-color:transparent;-fx-padding:5 0;");
            }
        });
    }

    private HBox buildCard(Announcement a) {
        Pet pet = petById.get(a.getPetId());
        String petName = pet != null
                ? pet.getName() + " (" + capitalize(pet.getTypePet()) + ")"
                : "Animal #" + a.getPetId();

        StackPane photoPane = buildPhotoCircle(pet, 64);
        Label nomLabel = new Label(petName);
        nomLabel.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        String typeGarde = "CHEZ_MOI".equals(a.getCareType()) ? "Chez proprietaire" : "En chenil";
        String deb = a.getDateDebut() != null ? a.getDateDebut().toLocalDate().format(fmt) : "—";
        String fin = a.getDateFin()   != null ? a.getDateFin().toLocalDate().format(fmt)   : "—";

        VBox infoBox = new VBox(5, nomLabel,
                il("Type de garde",   typeGarde),
                il("Passages / jour", String.valueOf(a.getVisitPerDay())),
                il("Periode",         deb + " - " + fin),
                il("Prix",            "€"+(int)a.getRemunerationMin()+" - €"+(int)a.getRemunerationMax()),
                buildAdresseRow(a.getAddress() != null ? a.getAddress() : "—")
        );
        if (a.getServices() != null && !a.getServices().isEmpty())
            infoBox.getChildren().add(il("Services", a.getServices()));
        infoBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Button btnEdit = new Button();
        Label ei = new Label("\u270F"); ei.setStyle("-fx-font-size:18px;-fx-text-fill:#9b72e8;");
        btnEdit.setGraphic(ei);
        btnEdit.setStyle("-fx-background-color:transparent;-fx-cursor:hand;-fx-padding:4 8;");
        btnEdit.setOnAction(e -> handleModifier(a));

        Button btnDel = new Button();
        Label di = new Label("\uD83D\uDDD1"); di.setStyle("-fx-font-size:18px;-fx-text-fill:#e87272;");
        btnDel.setGraphic(di);
        btnDel.setStyle("-fx-background-color:transparent;-fx-cursor:hand;-fx-padding:4 8;");
        btnDel.setOnAction(e -> handleSupprimer(a));

        VBox actionsBox = new VBox(8, btnEdit, btnDel);
        actionsBox.setAlignment(Pos.TOP_CENTER);
        actionsBox.setPadding(new Insets(4, 0, 0, 0));

        HBox card = new HBox(16, photoPane, infoBox, actionsBox);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16px;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private HBox il(String label, String value) {
        Label lbl = new Label(label + " :");
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;-fx-min-width:160px;");
        Label val = new Label(value); val.setStyle("-fx-font-size:13px;-fx-text-fill:#5a5a8a;");
        val.setWrapText(true);
        HBox row = new HBox(6, lbl, val); row.setAlignment(Pos.CENTER_LEFT); return row;
    }

    private HBox buildAdresseRow(String adresse) {
        Label lbl = new Label("Adresse :");
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;-fx-min-width:160px;");
        Label val = new Label(adresse); val.setStyle("-fx-font-size:13px;-fx-text-fill:#5a5a8a;");
        val.setWrapText(true); val.setMaxWidth(340);
        HBox row = new HBox(6, lbl, val); row.setAlignment(Pos.CENTER_LEFT);
        if (!"—".equals(adresse) && !adresse.isEmpty()) {
            Button btnMap = new Button("Voir carte");
            btnMap.setStyle("-fx-background-color:#e8f0fe;-fx-text-fill:#1a73e8;-fx-font-size:11px;"
                    + "-fx-font-weight:bold;-fx-background-radius:6px;-fx-padding:3 10;-fx-cursor:hand;");
            btnMap.setOnAction(e -> ouvrirCarte(adresse));
            row.getChildren().add(btnMap);
        }
        return row;
    }

    private StackPane buildPhotoCircle(Pet pet, double size) {
        StackPane pane = new StackPane();
        pane.setPrefSize(size, size); pane.setMinSize(size, size); pane.setMaxSize(size, size);
        pane.setStyle("-fx-background-color:#ede9f8;-fx-background-radius:" + (size/2) + "px;");
        if (pet != null && pet.getImageName() != null && !pet.getImageName().isEmpty()) {
            Image img = loadPetImage(pet.getImageName());
            if (img != null && !img.isError()) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size); iv.setFitHeight(size); iv.setPreserveRatio(false);
                Rectangle clip = new Rectangle(size, size);
                clip.setArcWidth(size); clip.setArcHeight(size); iv.setClip(clip);
                pane.getChildren().add(iv); return pane;
            }
        }
        Label emoji = new Label("\uD83D\uDC3E"); emoji.setStyle("-fx-font-size:26px;");
        pane.getChildren().add(emoji); return pane;
    }

    private Image loadPetImage(String imageName) {
        if (imageName == null || imageName.isEmpty()) return null;
        if (imageName.startsWith("http")) {
            try { return new Image(imageName, 64, 64, false, true); }
            catch (Exception ignored) { return null; }
        }
        for (String p : new String[]{"images/"+imageName,
                System.getProperty("user.dir")+"/images/"+imageName,
                "src/main/resources/images/"+imageName}) {
            File f = new File(p);
            if (f.exists()) { try { return new Image(f.toURI().toString(), 64, 64, false, true); } catch (Exception ignored) {} }
        }
        for (String rp : new String[]{"/images/"+imageName, "/"+imageName}) {
            try (InputStream is = getClass().getResourceAsStream(rp)) {
                if (is != null) return new Image(is, 64, 64, false, true);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void ouvrirCarte(String adresse) {
        Stage s = new Stage(); s.initModality(Modality.APPLICATION_MODAL);
        s.initStyle(StageStyle.DECORATED); s.setTitle("Carte"); s.setWidth(820); s.setHeight(570);
        WebView wv = new WebView(); WebEngine eng = wv.getEngine();
        String enc = URLEncoder.encode(adresse, StandardCharsets.UTF_8);
        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'/>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<style>*{margin:0;padding:0;}#map{width:100%;height:100vh;}</style>"
                + "</head><body><div id='map'></div><script>"
                + "var map=L.map('map').setView([36.8,10.18],7);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{attribution:'© OpenStreetMap',maxZoom:19}).addTo(map);"
                + "fetch('https://nominatim.openstreetmap.org/search?format=json&q="+enc+"&limit=1',{headers:{'User-Agent':'SitMyPetApp/1.0'}})"
                + ".then(r=>r.json()).then(d=>{if(!d||!d.length)return;"
                + "var lat=parseFloat(d[0].lat),lon=parseFloat(d[0].lon);"
                + "map.setView([lat,lon],15);"
                + "L.marker([lat,lon]).addTo(map).bindPopup('<b>"+adresse.replace("'","\\'")+"></b>').openPopup();"
                + "}).catch(()=>{});"
                + "</script></body></html>";
        eng.loadContent(html,"text/html");
        Button btnF = new Button("Fermer");
        btnF.setStyle("-fx-background-color:#9b72e8;-fx-text-fill:white;-fx-font-size:13px;"
                + "-fx-font-weight:bold;-fx-background-radius:8px;-fx-padding:8 24;-fx-cursor:hand;");
        btnF.setOnAction(e -> s.close());
        HBox footer = new HBox(new Label("  "+adresse), new Region(), btnF);
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS);
        footer.setAlignment(Pos.CENTER_LEFT); footer.setPadding(new Insets(10,16,10,16));
        footer.setStyle("-fx-background-color:#f8f8f8;-fx-border-color:#e0e0e0;-fx-border-width:1 0 0 0;");
        VBox root = new VBox(wv, footer); VBox.setVgrow(wv, Priority.ALWAYS);
        s.setScene(new Scene(root)); s.show();
    }

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
        String adr = searchAdresse.getText() == null ? "" : searchAdresse.getText().trim().toLowerCase();
        LocalDate deb = filterDateDebut.getValue(), fin = filterDateFin.getValue();
        filteredAnnonces.setPredicate(a -> {
            if (!adr.isEmpty() && (a.getAddress()==null||!a.getAddress().toLowerCase().contains(adr))) return false;
            if (deb!=null&&a.getDateDebut()!=null&&a.getDateDebut().toLocalDate().isBefore(deb)) return false;
            if (fin!=null&&a.getDateFin()!=null&&a.getDateFin().toLocalDate().isAfter(fin)) return false;
            return true;
        });
        updatePage();
    }

    private void loadData() {
        allAnnonces.setAll(serviceAnn.getAll()); currentPage = 1; applyFilters();
    }

    private void updatePage() {
        List<Announcement> filtered = filteredAnnonces;
        int total = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / itemsPerPage));
        if (currentPage > totalPages) currentPage = totalPages;
        int from = (currentPage-1)*itemsPerPage, to = Math.min(from+itemsPerPage, total);
        pageData.setAll(filtered.subList(from, to));
        labelPagination.setText(total==0 ? "Aucun resultat"
                : "Affichage "+(from+1)+" - "+to+" sur "+total+" annonces");
        btnPrev.setDisable(currentPage<=1); btnNext.setDisable(currentPage>=totalPages);
        pageButtons.getChildren().clear();
        for (int i=1; i<=totalPages; i++) {
            final int page=i; Button btn=new Button(String.valueOf(i));
            btn.setStyle(i==currentPage
                    ? "-fx-background-color:#9b72e8;-fx-text-fill:white;-fx-font-size:12px;"
                    + "-fx-background-radius:6px;-fx-padding:4 10;-fx-cursor:hand;-fx-font-weight:bold;"
                    : "-fx-background-color:#eeeeee;-fx-text-fill:#333;-fx-font-size:12px;"
                    + "-fx-background-radius:6px;-fx-padding:4 10;-fx-cursor:hand;");
            btn.setOnAction(e->{currentPage=page;updatePage();});
            pageButtons.getChildren().add(btn);
        }
    }

    @FXML private void handlePrevPage()      { if(currentPage>1){currentPage--;updatePage();} }
    @FXML private void handleNextPage()      { currentPage++; updatePage(); }
    @FXML private void handleRechercher()    { applyFilters(); }
    @FXML private void handleReinitialiser() {
        searchAdresse.clear();
        filterDateDebut.setValue(null);
        filterDateFin.setValue(null);
    }
    @FXML private void handleNouvelle() { stop(); nav("/com/sitmypet/fxml/CreerAnnonce.fxml","Creer une annonce"); }

    private void handleModifier(Announcement a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/ModifierAnnonce.fxml"));
            Parent root = loader.load();
            ModifierAnnonceController ctrl = loader.getController();
            Map<Integer,String> petMap = new HashMap<>();
            petById.forEach((id,p)->petMap.put(id,p.getName()+" ("+p.getTypePet().toLowerCase()+")"));
            ctrl.setAnnonce(a, petMap);
            Stage stage = (Stage) listAnnonces.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.setTitle("Modifier"); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleSupprimer(Announcement a) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Supprimer"); c.setHeaderText(null); c.setContentText("Supprimer cette annonce ?");
        c.showAndWait().ifPresent(r -> { if(r==ButtonType.OK){ serviceAnn.delete(a); loadData(); } });
    }

    private String capitalize(String s) {
        if(s==null||s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase()+s.substring(1).toLowerCase();
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void stop() { if (notifBadge != null) notifBadge.stopPolling(); }

    @FXML private void handleNavDashboard()    { stop(); nav("/com/sitmypet/fxml/Dashboard.fxml",           "Dashboard"); }
    @FXML private void handleNavAccueil()      { stop(); nav("/com/sitmypet/fxml/Accueil.fxml",             "Accueil"); }
    @FXML private void handleNavPostulations() { stop(); nav("/com/sitmypet/fxml/Postulations.fxml",        "Mes Postulations"); }
    @FXML private void handleNavMessagerie()   { stop(); nav("/com/sitmypet/fxml/Messagerie.fxml",          "Messagerie"); }
    @FXML private void handleNavAnnonces()     { stop(); nav("/com/sitmypet/fxml/AfficherAnnonces.fxml",    "Mes Annonces"); }
    @FXML private void handleNavEvenements()   { stop(); nav("/com/sitmypet/fxml/AfficherEvenements.fxml",  "Evenements"); }
    @FXML private void handleNavAnimaux()      { stop(); nav("/com/sitmypet/fxml/AfficherAnimales.fxml",    "Mes Animaux"); }
    @FXML private void handleNavReclamations() { stop(); nav("/com/sitmypet/fxml/AfficherReclamations.fxml","Reclamations"); }

    private void nav(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource==null){ System.out.println("FXML introuvable : "+fxml); return; }
            Stage stage = (Stage) listAnnonces.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(resource))); stage.setTitle(titre); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}