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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Pet;
import services.ServicePet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListeAnimauxController {

    // ── Filtres ───────────────────────────────────────────────
    @FXML private TextField        searchNom;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterGenre;
    @FXML private ComboBox<String> filterVaccine;
    @FXML private ComboBox<String> filterCritique;

    // ── ListView ──────────────────────────────────────────────
    @FXML private ListView<Pet> listAnimaux;

    // ── Pagination ────────────────────────────────────────────
    @FXML private Label             labelPagination;
    @FXML private Button            btnPrev;
    @FXML private Button            btnNext;
    @FXML private HBox              pageButtons;
    @FXML private ComboBox<Integer> comboPerPage;

    // ── Données ───────────────────────────────────────────────
    private final ServicePet        servicePet   = new ServicePet();
    private ObservableList<Pet>     allPets      = FXCollections.observableArrayList();
    private FilteredList<Pet>       filteredPets;
    private ObservableList<Pet>     pageData     = FXCollections.observableArrayList();

    private int currentPage  = 1;
    private int itemsPerPage = 5;

    // ──────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        initComboBoxes();
        initListView();
        initLiveFilters();
        initPaginationControls();
        loadData();
    }

    // ── Init ComboBoxes filtres ───────────────────────────────
    private void initComboBoxes() {
        filterType.setItems(FXCollections.observableArrayList("Tous","CHAT","CHIEN","TORTUE","LAPIN","OISEAU","POISSON","SOURIS"));
        filterType.setValue("Tous");
        filterGenre.setItems(FXCollections.observableArrayList("Tous","MALE","FEMALE"));
        filterGenre.setValue("Tous");
        filterVaccine.setItems(FXCollections.observableArrayList("Tous","Oui","Non"));
        filterVaccine.setValue("Tous");
        filterCritique.setItems(FXCollections.observableArrayList("Tous","Oui","Non"));
        filterCritique.setValue("Tous");
    }

    // ── Init ListView avec cellule personnalisée ──────────────
    private void initListView() {
        listAnimaux.setItems(pageData);
        listAnimaux.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        listAnimaux.setCellFactory(lv -> new ListCell<Pet>() {
            @Override
            protected void updateItem(Pet pet, boolean empty) {
                super.updateItem(pet, empty);
                if (empty || pet == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                setGraphic(buildPetCard(pet));
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
            }
        });
    }

    // ── Construction d'une carte animal ──────────────────────
    private HBox buildPetCard(Pet pet) {
        // ---- Photo : remplit tout le cadre 80x80 avec clip arrondi ----
        final double SIZE = 80;
        Image img = loadPetImage(pet.getImageName());

        StackPane photoPane = new StackPane();
        photoPane.setPrefSize(SIZE, SIZE);
        photoPane.setMinSize(SIZE, SIZE);
        photoPane.setMaxSize(SIZE, SIZE);
        photoPane.setStyle("-fx-background-color: #ede9f8; -fx-background-radius: 12px;");

        if (img != null && !img.isError()) {
            // ImageView sans preserveRatio pour couvrir tout le cadre
            ImageView iv = new ImageView(img);
            iv.setFitWidth(SIZE);
            iv.setFitHeight(SIZE);
            iv.setPreserveRatio(false);   // ← couvre tout le carré

            // Clip arrondi pour suivre le background-radius du StackPane
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(SIZE, SIZE);
            clip.setArcWidth(24);
            clip.setArcHeight(24);
            iv.setClip(clip);

            photoPane.getChildren().add(iv);
        } else {
            Label emoji = new Label("🐾");
            emoji.setStyle("-fx-font-size: 30px;");
            photoPane.getChildren().add(emoji);
        }

        // ---- Infos principales ----
        Label nom = new Label(pet.getName());
        nom.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2d2d2d;");

        String dateStr = (pet.getBirthDate() != null)
                ? pet.getBirthDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "—";

        Label details = new Label(pet.getTypePet() + " · " + pet.getBreed() + " · " + dateStr);
        details.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        Label poids = new Label("⚖ " + pet.getWeight() + " kg  |  " + pet.getGender());
        poids.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        Label description = new Label(pet.getDescription() != null ? pet.getDescription() : "");
        description.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");
        description.setWrapText(true);
        description.setMaxWidth(260);

        VBox infoBox = new VBox(3, nom, details, poids, description);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // ---- Badges statuts ----
        HBox badges = new HBox(6);
        badges.setAlignment(Pos.CENTER);

        badges.getChildren().add(makeBadge(
                pet.isVaccinated() ? "💉 Vacciné" : "💉 Non vacciné",
                pet.isVaccinated() ? "#9b72e8" : "#cccccc",
                pet.isVaccinated() ? "white" : "#555555"
        ));
        badges.getChildren().add(makeBadge(
                pet.isHasContagiousDisease() ? "🦠 Maladie" : "🦠 Sain",
                pet.isHasContagiousDisease() ? "#e87272" : "#72c88a",
                "white"
        ));
        badges.getChildren().add(makeBadge(
                pet.isHasMedicalRecord() ? "📋 Dossier" : "📋 Sans dossier",
                pet.isHasMedicalRecord() ? "#72b8e8" : "#cccccc",
                pet.isHasMedicalRecord() ? "white" : "#555555"
        ));
        if (pet.isHasCriticalCondition()) {
            badges.getChildren().add(makeBadge("⚠ Critique", "#e8a272", "white"));
        }

        VBox badgeBox = new VBox(6, badges);
        badgeBox.setAlignment(Pos.CENTER);

        // ---- Boutons Actions ----
        Button btnEdit = new Button("✏ Modifier");
        btnEdit.setStyle("-fx-background-color: #9b72e8; -fx-text-fill: white; -fx-font-size: 12px;"
                + "-fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 7 16; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> handleModifier(pet));

        Button btnDelete = new Button("🗑 Supprimer");
        btnDelete.setStyle("-fx-background-color: #f5f0ff; -fx-text-fill: #9b72e8; -fx-font-size: 12px;"
                + "-fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 7 16; -fx-cursor: hand;"
                + "-fx-border-color: #9b72e8; -fx-border-radius: 8px;");
        btnDelete.setOnAction(e -> handleSupprimer(pet));

        VBox actionsBox = new VBox(8, btnEdit, btnDelete);
        actionsBox.setAlignment(Pos.CENTER);

        // ---- Carte complète ----
        HBox card = new HBox(16, photoPane, infoBox, badgeBox, actionsBox);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14px;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        return card;
    }

    private Label makeBadge(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 11px;"
                        + "-fx-background-radius: 20px; -fx-padding: 4 10; -fx-font-weight: bold;",
                bg, fg));
        return l;
    }

    // ── Init contrôles pagination ─────────────────────────────
    private void initPaginationControls() {
        comboPerPage.setItems(FXCollections.observableArrayList(5, 10, 15, 20));
        comboPerPage.setValue(itemsPerPage);
        comboPerPage.valueProperty().addListener((obs, o, n) -> {
            if (n != null) { itemsPerPage = n; currentPage = 1; updatePage(); }
        });
    }

    // ── Filtrage temps réel ───────────────────────────────────
    private void initLiveFilters() {
        filteredPets = new FilteredList<>(allPets, p -> true);
        searchNom.textProperty()   .addListener((obs, o, n) -> { currentPage = 1; applyFilters(); });
        filterType.valueProperty() .addListener((obs, o, n) -> { currentPage = 1; applyFilters(); });
        filterGenre.valueProperty().addListener((obs, o, n) -> { currentPage = 1; applyFilters(); });
        filterVaccine.valueProperty() .addListener((obs, o, n) -> { currentPage = 1; applyFilters(); });
        filterCritique.valueProperty().addListener((obs, o, n) -> { currentPage = 1; applyFilters(); });
    }

    private void applyFilters() {
        String nom      = searchNom.getText() == null ? "" : searchNom.getText().trim().toLowerCase();
        String type     = filterType.getValue();
        String genre    = filterGenre.getValue();
        String vaccine  = filterVaccine.getValue();
        String critique = filterCritique.getValue();

        filteredPets.setPredicate(pet -> {
            // Filtre nom
            if (!nom.isEmpty() && (pet.getName() == null || !pet.getName().toLowerCase().contains(nom)))
                return false;
            // Filtre type
            if (type != null && !type.equals("Tous")
                    && (pet.getTypePet() == null || !pet.getTypePet().equalsIgnoreCase(type)))
                return false;
            // Filtre genre
            if (genre != null && !genre.equals("Tous")
                    && (pet.getGender() == null || !pet.getGender().equalsIgnoreCase(genre)))
                return false;
            // Filtre vacciné
            if (vaccine != null && !vaccine.equals("Tous")) {
                boolean wantVaccinated = vaccine.equals("Oui");
                if (pet.isVaccinated() != wantVaccinated) return false;
            }
            // Filtre critique
            if (critique != null && !critique.equals("Tous")) {
                boolean wantCritique = critique.equals("Oui");
                if (pet.isHasCriticalCondition() != wantCritique) return false;
            }
            return true;
        });
        updatePage();
    }

    // ── Chargement données ────────────────────────────────────
    private void loadData() {
        allPets.setAll(servicePet.getAll());
        currentPage = 1;
        applyFilters();
    }

    // ── Mise à jour de la page affichée ──────────────────────
    private void updatePage() {
        List<Pet> filtered = filteredPets;
        int total      = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / itemsPerPage));
        if (currentPage > totalPages) currentPage = totalPages;

        int from = (currentPage - 1) * itemsPerPage;
        int to   = Math.min(from + itemsPerPage, total);

        pageData.setAll(filtered.subList(from, to));

        labelPagination.setText(total == 0
                ? "Aucun résultat"
                : "Affichage " + (from + 1) + " – " + to + " sur " + total + " animaux");

        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);

        // Numéros de pages
        pageButtons.getChildren().clear();
        for (int i = 1; i <= totalPages; i++) {
            final int page = i;
            Button btn = new Button(String.valueOf(i));
            if (i == currentPage) {
                btn.setStyle("-fx-background-color:#9b72e8;-fx-text-fill:white;-fx-font-size:12px;"
                        + "-fx-background-radius:6px;-fx-padding:4 10;-fx-cursor:hand;-fx-font-weight:bold;");
            } else {
                btn.setStyle("-fx-background-color:#eeeeee;-fx-text-fill:#333333;-fx-font-size:12px;"
                        + "-fx-background-radius:6px;-fx-padding:4 10;-fx-cursor:hand;");
            }
            btn.setOnAction(e -> { currentPage = page; updatePage(); });
            pageButtons.getChildren().add(btn);
        }
    }

    // ── Boutons Précédent / Suivant ───────────────────────────
    @FXML private void handlePrevPage() { if (currentPage > 1) { currentPage--; updatePage(); } }
    @FXML private void handleNextPage() { currentPage++; updatePage(); }

    @FXML private void handleRechercher()    { applyFilters(); }

    @FXML
    private void handleReinitialiser() {
        searchNom.clear();
        filterType.setValue("Tous");
        filterGenre.setValue("Tous");
        filterVaccine.setValue("Tous");
        filterCritique.setValue("Tous");
        // les listeners déclenchent applyFilters automatiquement
    }

    // ── Image ─────────────────────────────────────────────────
    private Image loadPetImage(String imageName) {
        if (imageName == null || imageName.isEmpty()) return null;
        if (imageName.startsWith("http")) {
            try { return new Image(imageName, 64, 64, false, true); }            catch (Exception ignored) { return null; }
        }
        String[] filePaths = {
                "images/" + imageName,
                System.getProperty("user.dir") + "/images/" + imageName,
                "src/main/resources/images/" + imageName
        };
        for (String path : filePaths) {
            File f = new File(path);
            if (f.exists()) {
                try { return new Image(f.toURI().toString(), 70, 70, true, true); } catch (Exception ignored) {}
            }
        }
        for (String rPath : new String[]{ "/images/" + imageName, "/" + imageName }) {
            try (InputStream is = getClass().getResourceAsStream(rPath)) {
                if (is != null) return new Image(is, 70, 70, true, true);
            } catch (Exception ignored) {}
        }
        return null;
    }

    // ── CRUD ──────────────────────────────────────────────────
    @FXML
    private void handleAjouter() {
        naviguer("/AjouterAnimal.fxml", "Ajouter un animal");
    }

    private void handleModifier(Pet pet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierAnimal.fxml"));
            Parent root = loader.load();
            ModifierAnimalController ctrl = loader.getController();
            ctrl.setPet(pet);
            Stage stage = (Stage) listAnimaux.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier - " + pet.getName());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSupprimer(Pet pet) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous supprimer \"" + pet.getName() + "\" ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                servicePet.delete(pet);
                loadData();
            }
        });
    }

    // ── Navigation ────────────────────────────────────────────
    @FXML private void handleNavDashboard()    { naviguer("/Dashboard.fxml",           "Dashboard"); }
    @FXML private void handleNavAnnonces()     { naviguer("/AfficherAnnonces.fxml",    "Mes Annonces"); }
    @FXML private void handleNavEvenements()   { naviguer("/AfficherEvenements.fxml",  "Événements"); }
    @FXML private void handleNavAnimaux()      { naviguer("/AfficherAnimales.fxml",    "Mes Animaux"); }
    @FXML private void handleNavReclamations() { naviguer("/AfficherReclamations.fxml","Réclamations"); }
    @FXML private void handleNavAccueil()      { naviguer("/Accueil.fxml",      "Accueil"); }
    @FXML private void handleNavPostulations() { naviguer("/Postulations.fxml", "Mes Postulations"); }
    private void naviguer(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) listAnimaux.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titre);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}