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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Announcement;
import model.Pet;
import model.Postulation;
import services.ServiceAnnouncement;
import services.ServicePet;
import services.ServicePostulation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AccueilController {

    @FXML private TextField              searchField;
    @FXML private ListView<Announcement> listAnnonces;
    @FXML private Label                  labelCount;

    private final ServiceAnnouncement  serviceAnn  = new ServiceAnnouncement();
    private final ServicePet           servicePet  = new ServicePet();
    private final ServicePostulation   servicePost = new ServicePostulation();

    private final ObservableList<Announcement> allAnnonces = FXCollections.observableArrayList();
    private final ObservableList<Announcement> pageData    = FXCollections.observableArrayList();
    private final Map<Integer, Pet>            petById     = new HashMap<>();
    private final DateTimeFormatter            fmt         = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int CURRENT_GARDIEN_ID = 2;

    @FXML
    public void initialize() {
        servicePost.createTableIfNotExists();
        chargerPets();
        initListView();
        loadData();
        if (searchField != null)
            searchField.textProperty().addListener((obs, o, n) -> applySearch(n));
    }

    private void chargerPets() {
        for (Pet p : servicePet.getAll()) petById.put(p.getId(), p);
    }

    private void loadData() {
        allAnnonces.setAll(serviceAnn.getAll());
        applySearch(searchField == null ? "" : searchField.getText());
        if (labelCount != null)
            labelCount.setText(allAnnonces.size() + " annonce(s) disponible(s)");
    }

    private void applySearch(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) {
            pageData.setAll(allAnnonces);
        } else {
            pageData.setAll(allAnnonces.filtered(a -> {
                Pet p = petById.get(a.getPetId());
                String petName = p != null ? p.getName().toLowerCase() : "";
                String adr = a.getAddress() != null ? a.getAddress().toLowerCase() : "";
                return petName.contains(q) || adr.contains(q);
            }));
        }
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
                    setStyle("-fx-background-color:transparent;");
                    return;
                }
                setGraphic(buildCard(a));
                setText(null);
                setStyle("-fx-background-color:transparent;-fx-padding:5 0;");
            }
        });
    }

    // ── Carte annonce ─────────────────────────────────────────
    private HBox buildCard(Announcement a) {
        Pet pet = petById.get(a.getPetId());
        String petName = pet != null
                ? pet.getName() + " (" + capitalize(pet.getTypePet()) + ")"
                : "Animal #" + a.getPetId();

        // ── Avatar profil par défaut (pas la photo de l'animal) ──
        StackPane avatarPane = buildAvatarProfil(60);

        // ── Infos ─────────────────────────────────────────────
        Label owner = new Label("Proprietaire #" + a.getUserId());
        owner.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        Label petLabel    = new Label("Animal : " + petName);
        String typeGarde  = "CHEZ_MOI".equals(a.getCareType()) ? "Chez proprietaire" : "En chenil";
        Label typeLabel   = new Label("Type de garde : " + typeGarde);
        String debutStr   = a.getDateDebut() != null ? a.getDateDebut().toLocalDate().format(fmt) : "—";
        String finStr     = a.getDateFin()   != null ? a.getDateFin().toLocalDate().format(fmt)   : "—";
        Label periodeLabel = new Label("Periode : " + debutStr + " - " + finStr);
        Label prixLabel   = new Label("Prix : €" + (int) a.getRemunerationMin()
                + ".00 - €" + (int) a.getRemunerationMax() + ".00");
        String adr = a.getAddress() != null && !a.getAddress().isEmpty() ? a.getAddress() : "—";
        Label adrLabel    = new Label("Adresse : " + adr);
        adrLabel.setWrapText(true); adrLabel.setMaxWidth(420);

        String infoStyle = "-fx-font-size:12px;-fx-text-fill:#5a5a8a;";
        petLabel.setStyle(infoStyle); typeLabel.setStyle(infoStyle);
        periodeLabel.setStyle(infoStyle); prixLabel.setStyle(infoStyle);
        adrLabel.setStyle(infoStyle);

        VBox infoBox = new VBox(4, owner, petLabel, typeLabel, periodeLabel, prixLabel, adrLabel);
        infoBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // ── Bouton 🐾 voir animal ─────────────────────────────
        Button btnAnimal = new Button();
        Label pawIcon = new Label("\uD83D\uDC3E");
        pawIcon.setStyle("-fx-font-size:20px;");
        btnAnimal.setGraphic(pawIcon);
        btnAnimal.setStyle("-fx-background-color:#f0ebff;-fx-background-radius:50%;"
                + "-fx-padding:8;-fx-cursor:hand;-fx-min-width:40;-fx-min-height:40;");
        btnAnimal.setOnAction(e -> ouvrirPopupAnimal(pet));

        // ── Bouton ❤ postuler ─────────────────────────────────
        boolean dejaPostule = servicePost.hasPostule(CURRENT_GARDIEN_ID, a.getId());
        Button btnCoeur = new Button();
        Label heartIcon = new Label(dejaPostule ? "\u2665" : "\u2661");
        heartIcon.setStyle("-fx-font-size:20px;-fx-text-fill:" + (dejaPostule ? "#e87272" : "#aaaaaa") + ";");
        btnCoeur.setGraphic(heartIcon);
        btnCoeur.setStyle("-fx-background-color:" + (dejaPostule ? "#fff0f0" : "#f8f8f8") + ";"
                + "-fx-background-radius:50%;-fx-padding:8;-fx-cursor:hand;"
                + "-fx-min-width:40;-fx-min-height:40;");
        btnCoeur.setOnAction(e -> {
            if (servicePost.hasPostule(CURRENT_GARDIEN_ID, a.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Deja postule",
                        "Vous avez deja postule sur cette annonce."); return;
            }
            servicePost.add(new Postulation(a.getId(), CURRENT_GARDIEN_ID,
                    Date.valueOf(LocalDate.now()), "EN_ATTENTE"));
            heartIcon.setText("\u2665");
            heartIcon.setStyle("-fx-font-size:20px;-fx-text-fill:#e87272;");
            btnCoeur.setStyle("-fx-background-color:#fff0f0;-fx-background-radius:50%;"
                    + "-fx-padding:8;-fx-cursor:hand;-fx-min-width:40;-fx-min-height:40;");
            showAlert(Alert.AlertType.INFORMATION, "Postulation envoyee",
                    "Votre postulation a ete enregistree !");
        });

        VBox actionsBox = new VBox(10, btnAnimal, btnCoeur);
        actionsBox.setAlignment(Pos.TOP_CENTER);

        HBox card = new HBox(14, avatarPane, infoBox, actionsBox);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16px;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ── Avatar profil par défaut ──────────────────────────────
    // Cercle avec fond violet clair + icône utilisateur unicode
    private StackPane buildAvatarProfil(double size) {
        StackPane pane = new StackPane();
        pane.setPrefSize(size, size);
        pane.setMinSize(size, size);
        pane.setMaxSize(size, size);
        pane.setStyle("-fx-background-color:#ede9f8;-fx-background-radius:" + (size / 2) + "px;");

        // Icône personne unicode (U+1F464 = 👤)
        Label icon = new Label("\uD83D\uDC64");
        icon.setStyle("-fx-font-size:" + (size * 0.45) + "px;");

        pane.getChildren().add(icon);
        return pane;
    }

    // ── Popup détails animal ──────────────────────────────────
    private void ouvrirPopupAnimal(Pet pet) {
        if (pet == null) { showAlert(Alert.AlertType.INFORMATION, "Animal", "Animal inconnu."); return; }

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.UNDECORATED);

        // Photo de l'animal dans le popup
        StackPane photoPane = buildPhotoCircle(pet, 90);

        Label nom = new Label(pet.getName());
        nom.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        VBox details = new VBox(8,
                detailLigne("Type",        capitalize(pet.getTypePet())),
                detailLigne("Race",        pet.getBreed()),
                detailLigne("Poids",       pet.getWeight() + " kg"),
                detailLigne("Genre",       pet.getGender()),
                detailLigne("Vaccine",     pet.isVaccinated() ? "Oui" : "Non"),
                detailLigne("Maladie",     pet.isHasContagiousDisease() ? "Oui" : "Non"),
                detailLigne("Dossier",     pet.isHasMedicalRecord() ? "Oui" : "Non"),
                detailLigne("Critique",    pet.isHasCriticalCondition() ? "Oui" : "Non")
        );
        if (pet.getDescription() != null && !pet.getDescription().isEmpty())
            details.getChildren().add(detailLigne("Description", pet.getDescription()));

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-background-color:#9b72e8;-fx-text-fill:white;-fx-font-size:13px;"
                + "-fx-font-weight:bold;-fx-background-radius:10px;-fx-padding:9 30;-fx-cursor:hand;");
        btnFermer.setOnAction(e -> popup.close());

        VBox content = new VBox(14, photoPane, nom, details, btnFermer);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(28, 32, 28, 32));
        content.setStyle("-fx-background-color:white;-fx-background-radius:20px;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),20,0,0,6);");
        content.setMaxWidth(360);

        StackPane root = new StackPane(content);
        root.setStyle("-fx-background-color:rgba(0,0,0,0.4);");
        root.setPrefSize(420, 560);

        popup.setScene(new Scene(root, 420, 560));
        popup.show();
    }

    private HBox detailLigne(String label, String value) {
        Label lbl = new Label(label + " :");
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;-fx-min-width:110px;");
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-font-size:13px;-fx-text-fill:#5a5a8a;");
        HBox row = new HBox(8, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── Photo circulaire (utilisée dans le popup animal) ──────
    private StackPane buildPhotoCircle(Pet pet, double size) {
        StackPane pane = new StackPane();
        pane.setPrefSize(size, size); pane.setMinSize(size, size); pane.setMaxSize(size, size);
        pane.setStyle("-fx-background-color:#ede9f8;-fx-background-radius:" + (size / 2) + "px;");
        if (pet != null && pet.getImageName() != null && !pet.getImageName().isEmpty()) {
            Image img = loadPetImage(pet.getImageName(), size);
            if (img != null && !img.isError()) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size); iv.setFitHeight(size); iv.setPreserveRatio(false);
                Rectangle clip = new Rectangle(size, size);
                clip.setArcWidth(size); clip.setArcHeight(size);
                iv.setClip(clip);
                pane.getChildren().add(iv);
                return pane;
            }
        }
        Label emoji = new Label("\uD83D\uDC3E");
        emoji.setStyle("-fx-font-size:" + (size / 2.5) + "px;");
        pane.getChildren().add(emoji);
        return pane;
    }

    private Image loadPetImage(String name, double size) {
        if (name == null || name.isEmpty()) return null;

        // ← AJOUT 1 : si c'est une URL Cloudinary, charger directement
        if (name.startsWith("http")) {
            try { return new Image(name, size, size, false, true); }
            catch (Exception ignored) { return null; }
        }
        String[] paths = {"images/" + name, System.getProperty("user.dir") + "/images/" + name,
                "src/main/resources/images/" + name};
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) { try { return new Image(f.toURI().toString(), size, size, false, true); } catch (Exception ignored) {} }
        }
        for (String rp : new String[]{"/images/" + name, "/" + name}) {
            try (InputStream is = getClass().getResourceAsStream(rp)) {
                if (is != null) return new Image(is, size, size, false, true);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }

    // ── Navigation ────────────────────────────────────────────
    @FXML private void handleNavDashboard()    { nav("/Dashboard.fxml",           "Dashboard"); }
    @FXML private void handleNavAccueil()      { nav("/Accueil.fxml",             "Accueil"); }
    @FXML private void handleNavPostulations() { nav("/Postulations.fxml",        "Mes Postulations"); }
    @FXML private void handleNavAnnonces()     { nav("/AfficherAnnonces.fxml",    "Mes Annonces"); }
    @FXML private void handleNavEvenements()   { nav("/AfficherEvenements.fxml",  "Evenements"); }
    @FXML private void handleNavAnimaux()      { nav("/AfficherAnimales.fxml",    "Mes Animaux"); }
    @FXML private void handleNavReclamations() { nav("/AfficherReclamations.fxml","Reclamations"); }

    private void nav(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Parent root  = FXMLLoader.load(resource);
            Stage  stage = (Stage) listAnnonces.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.setTitle(titre); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}