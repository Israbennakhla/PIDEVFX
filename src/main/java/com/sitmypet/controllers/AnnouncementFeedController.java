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
import com.sitmypet.model.Announcement;
import com.sitmypet.model.Notification;
import com.sitmypet.model.Pet;
import com.sitmypet.model.Postulation;
import com.sitmypet.services.ServiceAnnouncement;
import com.sitmypet.services.ServiceNotification;
import com.sitmypet.services.ServicePet;
import com.sitmypet.services.ServicePostulation;
import com.sitmypet.services.ServiceUser;

import javafx.scene.Scene;

import java.io.File;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Liste d'annonces pour le gardien : soit les siennes uniquement (accueil), soit le marché (postuler).
 * Contrôlé par {@link #marketBrowseMode} avant le chargement du FXML.
 */
public class AnnouncementFeedController {

    /** Si true : toutes les annonces ouvertes + bouton postuler. Si false : annonces de l'utilisateur connecté uniquement. */
    public static boolean marketBrowseMode = false;

    @FXML private HBox                   navbarHBox;
    @FXML private TextField              searchField;
    @FXML private ListView<Announcement> listAnnonces;
    @FXML private Label                  labelCount;

    private final ServiceAnnouncement  serviceAnn    = new ServiceAnnouncement();
    private final ServicePet           servicePet    = new ServicePet();
    private final ServicePostulation   servicePost   = new ServicePostulation();
    private final ServiceNotification  serviceNotif = new ServiceNotification();
    private final ServiceUser          serviceUser   = new ServiceUser();

    private final ObservableList<Announcement> allAnnonces = FXCollections.observableArrayList();
    private final ObservableList<Announcement> pageData    = FXCollections.observableArrayList();
    private final Map<Integer, Pet>            petById     = new HashMap<>();
    private final Map<Integer, String>       ownerNameById = new HashMap<>();
    private final DateTimeFormatter            fmt         = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private NotificationBadge notifBadge;

    private int currentUserId() {
        User u = SessionContext.getCurrentUser();
        return u != null ? u.getId() : 0;
    }

    @FXML
    public void initialize() {
        chargerPets();
        initListView();
        loadData();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applySearch(n));
        }

        int uid = currentUserId();
        if (navbarHBox != null && uid > 0) {
            notifBadge = new NotificationBadge(uid, this::onNotifCliquee);
            notifBadge.setChatInfo(0, "Moi", "Contact");
            notifBadge.setMainStage(SessionContext.getPrimaryStage());
            int idx = navbarHBox.getChildren().size() - 1;
            navbarHBox.getChildren().add(idx, notifBadge.getView());
            notifBadge.startPolling();
        }
    }

    // ── Callback : gardien clique OK ou "Ouvrir chat" ─────────
    private void onNotifCliquee(Notification n) {
        if ("POSTULATION_ACCEPTEE".equals(n.getType())) {
            loadData(); // annonce disparaît
            showAlert(Alert.AlertType.INFORMATION,
                    "Postulation acceptee !",
                    n.getMessage());
        }
    }

    private void chargerPets() {
        petById.clear();
        for (Pet p : servicePet.getAll()) {
            petById.put(p.getId(), p);
        }
    }

    private void loadData() {
        int uid = currentUserId();
        if (uid <= 0) {
            allAnnonces.clear();
            applySearch(searchField == null ? "" : searchField.getText());
            return;
        }
        if (marketBrowseMode) {
            allAnnonces.setAll(serviceAnn.getOpenForGardien(uid));
        } else {
            allAnnonces.setAll(serviceAnn.getByUserId(uid));
        }
        applySearch(searchField == null ? "" : searchField.getText());
        if (labelCount != null) {
            labelCount.setText(allAnnonces.size() + " annonce(s)");
        }
    }

    private void applySearch(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        pageData.setAll(q.isEmpty() ? allAnnonces : allAnnonces.filtered(a -> {
            Pet p = petById.get(a.getPetId());
            String name = p != null ? p.getName().toLowerCase() : "";
            String adr  = a.getAddress() != null ? a.getAddress().toLowerCase() : "";
            return name.contains(q) || adr.contains(q);
        }));
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

        StackPane avatarPane = buildAvatar(60);
        String proprioNom = ownerNameById.computeIfAbsent(a.getUserId(), serviceUser::getDisplayNameById);
        Label owner = new Label(proprioNom);
        owner.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        String typeGarde = "CHEZ_MOI".equals(a.getCareType()) ? "Chez proprietaire" : "En chenil";
        String deb = a.getDateDebut() != null ? a.getDateDebut().toLocalDate().format(fmt) : "—";
        String fin = a.getDateFin()   != null ? a.getDateFin().toLocalDate().format(fmt)   : "—";
        String adr = a.getAddress()   != null && !a.getAddress().isEmpty() ? a.getAddress() : "—";

        Label adrLabel = sl("Adresse : " + adr);
        adrLabel.setWrapText(true);
        adrLabel.setMaxWidth(420);

        VBox infoBox = new VBox(4, owner,
                sl("Animal : " + petName),
                sl("Type de garde : " + typeGarde),
                sl("Periode : " + deb + " - " + fin),
                sl("Prix : €" + (int) a.getRemunerationMin() + " - €" + (int) a.getRemunerationMax()),
                adrLabel);
        infoBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Bouton 🐾
        Label pawI = new Label("\uD83D\uDC3E"); pawI.setStyle("-fx-font-size:20px;");
        Button btnAnimal = new Button(); btnAnimal.setGraphic(pawI);
        btnAnimal.setStyle("-fx-background-color:#f0ebff;-fx-background-radius:50%;"
                + "-fx-padding:8;-fx-cursor:hand;-fx-min-width:40;-fx-min-height:40;");
        btnAnimal.setOnAction(e -> ouvrirPopupAnimal(pet));

        int gid = currentUserId();
        boolean canPostulate = marketBrowseMode && gid > 0 && a.getUserId() != gid;

        VBox actions = new VBox(10);
        actions.setAlignment(Pos.TOP_CENTER);
        actions.getChildren().add(btnAnimal);

        if (canPostulate) {
            boolean dejaPostule = servicePost.hasPostule(gid, a.getId());
            Label heartI = new Label(dejaPostule ? "\u2665" : "\u2661");
            heartI.setStyle("-fx-font-size:20px;-fx-text-fill:" + (dejaPostule ? "#e87272" : "#aaaaaa") + ";");
            Button btnCoeur = new Button();
            btnCoeur.setGraphic(heartI);
            btnCoeur.setStyle("-fx-background-color:" + (dejaPostule ? "#fff0f0" : "#f8f8f8")
                    + ";-fx-background-radius:50%;-fx-padding:8;-fx-cursor:hand;"
                    + "-fx-min-width:40;-fx-min-height:40;");

            btnCoeur.setOnAction(e -> {
                if (servicePost.hasPostule(gid, a.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Déjà postulé",
                            "Vous avez déjà postulé sur cette annonce.");
                    return;
                }
                Postulation post = new Postulation(a.getId(), gid,
                        Date.valueOf(LocalDate.now()), "EN_ATTENTE");
                int postulationId = servicePost.add(post);
                if (postulationId < 0) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur création postulation.");
                    return;
                }
                String gardienNom = serviceUser.getDisplayNameById(gid);
                String msg = gardienNom + " a postulé pour :\nAnimal : " + petName
                        + "\nPériode : " + deb + " -> " + fin
                        + "\nPrix : €" + (int) a.getRemunerationMin() + " - €" + (int) a.getRemunerationMax();
                serviceNotif.envoyer(new Notification(
                        a.getUserId(),
                        gid,
                        postulationId,
                        msg,
                        "NOUVELLE_POSTULATION"
                ));
                heartI.setText("\u2665");
                heartI.setStyle("-fx-font-size:20px;-fx-text-fill:#e87272;");
                btnCoeur.setStyle("-fx-background-color:#fff0f0;-fx-background-radius:50%;"
                        + "-fx-padding:8;-fx-cursor:hand;-fx-min-width:40;-fx-min-height:40;");
                showAlert(Alert.AlertType.INFORMATION, "Postulation envoyée",
                        "Votre postulation a été envoyée !");
            });
            actions.getChildren().add(btnCoeur);
        }

        HBox card = new HBox(14, avatarPane, infoBox, actions);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16px;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private Label sl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5a8a;");
        return l;
    }

    private StackPane buildAvatar(double size) {
        StackPane p = new StackPane();
        p.setPrefSize(size, size); p.setMinSize(size, size); p.setMaxSize(size, size);
        p.setStyle("-fx-background-color:#ede9f8;-fx-background-radius:" + (size/2) + "px;");
        Label i = new Label("\uD83D\uDC64");
        i.setStyle("-fx-font-size:" + (size*0.45) + "px;");
        p.getChildren().add(i);
        return p;
    }

    private void ouvrirPopupAnimal(Pet pet) {
        if (pet == null) { showAlert(Alert.AlertType.INFORMATION, "Animal", "Animal inconnu."); return; }
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.UNDECORATED);
        StackPane photo = buildPhotoCircle(pet, 90);
        Label nom = new Label(pet.getName());
        nom.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");
        VBox details = new VBox(8,
                dl("Type",     capitalize(pet.getTypePet())),
                dl("Race",     pet.getBreed()),
                dl("Poids",    pet.getWeight() + " kg"),
                dl("Genre",    pet.getGender()),
                dl("Vaccine",  pet.isVaccinated() ? "Oui" : "Non"),
                dl("Maladie",  pet.isHasContagiousDisease() ? "Oui" : "Non"),
                dl("Dossier",  pet.isHasMedicalRecord() ? "Oui" : "Non"),
                dl("Critique", pet.isHasCriticalCondition() ? "Oui" : "Non")
        );
        Button btnF = new Button("Fermer");
        btnF.setStyle("-fx-background-color:#9b72e8;-fx-text-fill:white;-fx-font-size:13px;"
                + "-fx-font-weight:bold;-fx-background-radius:10px;-fx-padding:9 30;-fx-cursor:hand;");
        btnF.setOnAction(e -> popup.close());
        VBox content = new VBox(14, photo, nom, details, btnF);
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

    private HBox dl(String label, String value) {
        Label lbl = new Label(label + " :");
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;-fx-min-width:110px;");
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-font-size:13px;-fx-text-fill:#5a5a8a;");
        HBox row = new HBox(8, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private StackPane buildPhotoCircle(Pet pet, double size) {
        StackPane pane = new StackPane();
        pane.setPrefSize(size, size); pane.setMinSize(size, size); pane.setMaxSize(size, size);
        pane.setStyle("-fx-background-color:#ede9f8;-fx-background-radius:" + (size/2) + "px;");
        if (pet != null && pet.getImageName() != null && !pet.getImageName().isEmpty()) {
            Image img = loadPetImage(pet.getImageName(), size);
            if (img != null && !img.isError()) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size); iv.setFitHeight(size); iv.setPreserveRatio(false);
                Rectangle clip = new Rectangle(size, size);
                clip.setArcWidth(size); clip.setArcHeight(size); iv.setClip(clip);
                pane.getChildren().add(iv);
                return pane;
            }
        }
        Label e = new Label("\uD83D\uDC3E");
        e.setStyle("-fx-font-size:" + (size/2.5) + "px;");
        pane.getChildren().add(e);
        return pane;
    }

    private Image loadPetImage(String name, double size) {
        if (name == null || name.isEmpty()) return null;
        if (name.startsWith("http")) {
            try { return new Image(name, size, size, false, true); }
            catch (Exception ignored) { return null; }
        }
        for (String p : new String[]{"images/"+name,
                System.getProperty("user.dir")+"/images/"+name,
                "src/main/resources/images/"+name}) {
            File f = new File(p);
            if (f.exists()) {
                try { return new Image(f.toURI().toString(), size, size, false, true); }
                catch (Exception ignored) {}
            }
        }
        for (String rp : new String[]{"/images/"+name, "/"+name}) {
            try (InputStream is = getClass().getResourceAsStream(rp)) {
                if (is != null) return new Image(is, size, size, false, true);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    /** Arrête le polling du badge (ex. avant de quitter l’écran). */
    public void stopNotificationPolling() {
        if (notifBadge != null) {
            notifBadge.stopPolling();
        }
    }
}