package com.sitmypet.controllers;

import com.sitmypet.model.Pet;
import com.sitmypet.services.ServicePet;
import com.sitmypet.services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

import java.io.File;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Liste lecture seule de tous les animaux (vue administrateur).
 */
public class AdminAnimauxOverviewController {

    @FXML private TextField searchNom;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterGenre;
    @FXML private ComboBox<String> filterVaccine;
    @FXML private ComboBox<String> filterCritique;
    @FXML private ListView<Pet> listAnimaux;
    @FXML private Label labelPagination;
    @FXML private Label labelCount;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private HBox pageButtons;
    @FXML private ComboBox<Integer> comboPerPage;

    private final ServicePet servicePet = new ServicePet();
    private final ServiceUser serviceUser = new ServiceUser();

    private ObservableList<Pet> allPets = FXCollections.observableArrayList();
    private FilteredList<Pet> filteredPets;
    private ObservableList<Pet> pageData = FXCollections.observableArrayList();

    private int currentPage = 1;
    private int itemsPerPage = 5;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        filterType.setItems(FXCollections.observableArrayList("Tous", "CHAT", "CHIEN", "TORTUE", "LAPIN", "OISEAU", "POISSON", "SOURIS"));
        filterType.setValue("Tous");
        filterGenre.setItems(FXCollections.observableArrayList("Tous", "MALE", "FEMALE"));
        filterGenre.setValue("Tous");
        filterVaccine.setItems(FXCollections.observableArrayList("Tous", "Oui", "Non"));
        filterVaccine.setValue("Tous");
        filterCritique.setItems(FXCollections.observableArrayList("Tous", "Oui", "Non"));
        filterCritique.setValue("Tous");

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

        filteredPets = new FilteredList<>(allPets, p -> true);
        searchNom.textProperty().addListener((o, a, n) -> { currentPage = 1; applyFilters(); });
        filterType.valueProperty().addListener((o, a, n) -> { currentPage = 1; applyFilters(); });
        filterGenre.valueProperty().addListener((o, a, n) -> { currentPage = 1; applyFilters(); });
        filterVaccine.valueProperty().addListener((o, a, n) -> { currentPage = 1; applyFilters(); });
        filterCritique.valueProperty().addListener((o, a, n) -> { currentPage = 1; applyFilters(); });

        comboPerPage.setItems(FXCollections.observableArrayList(5, 10, 15, 20));
        comboPerPage.setValue(itemsPerPage);
        comboPerPage.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                itemsPerPage = n;
                currentPage = 1;
                updatePage();
            }
        });

        loadData();
    }

    public void reloadFromDb() {
        loadData();
    }

    private void loadData() {
        allPets.setAll(servicePet.getAll());
        currentPage = 1;
        if (labelCount != null) {
            labelCount.setText(allPets.size() + " animal(aux) au total");
        }
        applyFilters();
    }

    private HBox buildPetCard(Pet pet) {
        final double SIZE = 80;
        Image img = loadPetImage(pet.getImageName());

        StackPane photoPane = new StackPane();
        photoPane.setPrefSize(SIZE, SIZE);
        photoPane.setMinSize(SIZE, SIZE);
        photoPane.setMaxSize(SIZE, SIZE);
        photoPane.setStyle("-fx-background-color: #ede9f8; -fx-background-radius: 12px;");

        if (img != null && !img.isError()) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(SIZE);
            iv.setFitHeight(SIZE);
            iv.setPreserveRatio(false);
            Rectangle clip = new Rectangle(SIZE, SIZE);
            clip.setArcWidth(24);
            clip.setArcHeight(24);
            iv.setClip(clip);
            photoPane.getChildren().add(iv);
        } else {
            Label emoji = new Label("🐾");
            emoji.setStyle("-fx-font-size: 30px;");
            photoPane.getChildren().add(emoji);
        }

        Label nom = new Label(pet.getName());
        nom.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2d2d2d;");

        String dateStr = (pet.getBirthDate() != null)
                ? pet.getBirthDate().toLocalDate().format(FMT) : "—";

        Label details = new Label(pet.getTypePet() + " · " + pet.getBreed() + " · né(e) le " + dateStr);
        details.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        String proprioLabel = serviceUser.getDisplayNameById(pet.getOwnerId());
        Label proprio = new Label("Propriétaire : " + proprioLabel);
        proprio.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7c5cbf;");

        Label poids = new Label("⚖ " + pet.getWeight() + " kg  |  " + pet.getGender());
        poids.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        Label description = new Label(pet.getDescription() != null ? pet.getDescription() : "");
        description.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");
        description.setWrapText(true);
        description.setMaxWidth(280);

        VBox infoBox = new VBox(3, nom, proprio, details, poids, description);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        HBox badges = new HBox(6);
        badges.setAlignment(Pos.CENTER);
        badges.getChildren().addAll(
                badge(pet.isVaccinated() ? "💉 Vacciné" : "💉 Non vacciné",
                        pet.isVaccinated() ? "#9b72e8" : "#cccccc",
                        pet.isVaccinated() ? "white" : "#555555"),
                badge(pet.isHasContagiousDisease() ? "🦠 Maladie" : "🦠 Sain",
                        pet.isHasContagiousDisease() ? "#e87272" : "#72c88a", "white")
        );

        VBox badgeBox = new VBox(6, badges);
        badgeBox.setAlignment(Pos.CENTER);

        Region spacerRight = new Region();
        spacerRight.setMinWidth(8);

        HBox card = new HBox(16, photoPane, infoBox, badgeBox, spacerRight);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14px;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private Label badge(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 11px;"
                        + "-fx-background-radius: 20px; -fx-padding: 4 10; -fx-font-weight: bold;",
                bg, fg));
        return l;
    }

    private void applyFilters() {
        String nom = searchNom.getText() == null ? "" : searchNom.getText().trim().toLowerCase();
        String type = filterType.getValue();
        String genre = filterGenre.getValue();
        String vaccine = filterVaccine.getValue();
        String critique = filterCritique.getValue();

        filteredPets.setPredicate(pet -> {
            if (!nom.isEmpty() && (pet.getName() == null || !pet.getName().toLowerCase().contains(nom)))
                return false;
            if (type != null && !type.equals("Tous")
                    && (pet.getTypePet() == null || !pet.getTypePet().equalsIgnoreCase(type)))
                return false;
            if (genre != null && !genre.equals("Tous")
                    && (pet.getGender() == null || !pet.getGender().equalsIgnoreCase(genre)))
                return false;
            if (vaccine != null && !vaccine.equals("Tous")) {
                boolean wantVaccinated = vaccine.equals("Oui");
                if (pet.isVaccinated() != wantVaccinated) return false;
            }
            if (critique != null && !critique.equals("Tous")) {
                boolean wantCritique = critique.equals("Oui");
                if (pet.isHasCriticalCondition() != wantCritique) return false;
            }
            return true;
        });
        updatePage();
    }

    private void updatePage() {
        List<Pet> filtered = filteredPets;
        int total = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / itemsPerPage));
        if (currentPage > totalPages) currentPage = totalPages;

        int from = (currentPage - 1) * itemsPerPage;
        int to = Math.min(from + itemsPerPage, total);

        pageData.setAll(filtered.subList(from, to));

        labelPagination.setText(total == 0
                ? "Aucun résultat"
                : "Affichage " + (from + 1) + " – " + to + " sur " + total + " animaux");

        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);

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

    @FXML private void handlePrevPage() { if (currentPage > 1) { currentPage--; updatePage(); } }
    @FXML private void handleNextPage() { currentPage++; updatePage(); }
    @FXML private void handleRechercher() { applyFilters(); }
    @FXML
    private void handleReinitialiser() {
        searchNom.clear();
        filterType.setValue("Tous");
        filterGenre.setValue("Tous");
        filterVaccine.setValue("Tous");
        filterCritique.setValue("Tous");
    }

    private Image loadPetImage(String imageName) {
        if (imageName == null || imageName.isEmpty()) return null;
        if (imageName.startsWith("http")) {
            try { return new Image(imageName, 64, 64, false, true); } catch (Exception ignored) { return null; }
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
}
