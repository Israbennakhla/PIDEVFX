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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Pet;
import services.ServicePet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ListeAnimauxController {

    @FXML private TextField            searchNom;
    @FXML private ComboBox<String>     filterType;
    @FXML private ComboBox<String>     filterGenre;
    @FXML private ComboBox<String>     filterVaccine;
    @FXML private ComboBox<String>     filterCritique;

    @FXML private TableView<Pet>            tableAnimaux;
    @FXML private TableColumn<Pet, Void>    colPhoto;
    @FXML private TableColumn<Pet, String>  colNom;
    @FXML private TableColumn<Pet, String>  colDate;
    @FXML private TableColumn<Pet, String>  colType;
    @FXML private TableColumn<Pet, String>  colRace;
    @FXML private TableColumn<Pet, String>  colPoids;
    @FXML private TableColumn<Pet, String>  colDescription;
    @FXML private TableColumn<Pet, String>  colGenre;
    @FXML private TableColumn<Pet, Void>    colVaccine;
    @FXML private TableColumn<Pet, Void>    colMaladie;
    @FXML private TableColumn<Pet, Void>    colDossier;
    @FXML private TableColumn<Pet, Void>    colCritique;
    @FXML private TableColumn<Pet, Void>    colActions;

    private final ServicePet        servicePet   = new ServicePet();
    private ObservableList<Pet>     allPets      = FXCollections.observableArrayList();
    private FilteredList<Pet>       filteredPets;

    @FXML
    public void initialize() {
        initComboBoxes();
        initColumns();
        initLiveFilters();
        loadData();
    }

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

    private void initLiveFilters() {
        filteredPets = new FilteredList<>(allPets, p -> true);
        searchNom.textProperty().addListener((obs, o, n) -> applyFilters());
        filterType.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterGenre.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterVaccine.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterCritique.valueProperty().addListener((obs, o, n) -> applyFilters());
        tableAnimaux.setItems(filteredPets);
    }

    private void applyFilters() {
        filteredPets.setPredicate(pet -> {
            String nom = searchNom.getText().trim().toLowerCase();
            String type = filterType.getValue(), genre = filterGenre.getValue();
            String vaccine = filterVaccine.getValue(), critique = filterCritique.getValue();
            if (!nom.isEmpty() && !pet.getName().toLowerCase().contains(nom)) return false;
            if (type != null && !type.equals("Tous") && !pet.getTypePet().equals(type)) return false;
            if (genre != null && !genre.equals("Tous") && !pet.getGender().equals(genre)) return false;
            if (vaccine != null && !vaccine.equals("Tous") && pet.isVaccinated() != vaccine.equals("Oui")) return false;
            if (critique != null && !critique.equals("Tous") && pet.isHasCriticalCondition() != critique.equals("Oui")) return false;
            return true;
        });
    }

    private void loadData() {
        List<Pet> pets = servicePet.getAll();
        allPets.setAll(pets);
        applyFilters();
    }

    @FXML private void handleRechercher() { applyFilters(); }

    @FXML
    private void handleReinitialiser() {
        searchNom.clear();
        filterType.setValue("Tous"); filterGenre.setValue("Tous");
        filterVaccine.setValue("Tous"); filterCritique.setValue("Tous");
    }

    private Image loadPetImage(String imageName) {
        if (imageName == null || imageName.isEmpty()) return null;
        String[] filePaths = { "images/" + imageName, "src/main/resources/images/" + imageName,
                System.getProperty("user.dir") + "/images/" + imageName };
        for (String path : filePaths) {
            File f = new File(path);
            if (f.exists()) { try { return new Image(f.toURI().toString(), 45, 45, true, true); } catch (Exception ignored) {} }
        }
        for (String rPath : new String[]{ "/images/" + imageName, "/" + imageName }) {
            try (InputStream is = getClass().getResourceAsStream(rPath)) {
                if (is != null) return new Image(is, 45, 45, true, true);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void initColumns() {
        colPhoto.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            { iv.setFitWidth(45); iv.setFitHeight(45); iv.setPreserveRatio(true); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null); setText(null);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) return;
                Pet pet = (Pet) getTableRow().getItem();
                Image img = loadPetImage(pet.getImageName());
                if (img != null && !img.isError()) { iv.setImage(img); setGraphic(iv); }
                else { Label l = new Label("🐾"); l.setStyle("-fx-font-size:24px;"); setGraphic(l); }
                setAlignment(Pos.CENTER);
            }
        });

        colNom.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getBirthDate() != null
                        ? data.getValue().getBirthDate().toLocalDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""));
        colType.setCellValueFactory(new PropertyValueFactory<>("typePet"));
        colRace.setCellValueFactory(new PropertyValueFactory<>("breed"));
        colPoids.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWeight() + " kg"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("gender"));

        colVaccine.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                Pet pet = (Pet) getTableRow().getItem();
                Label l = new Label(pet.isVaccinated() ? "✔" : "—");
                l.setStyle(pet.isVaccinated() ? "-fx-text-fill:#9b72e8;-fx-font-size:16px;" : "-fx-text-fill:#aaaaaa;-fx-font-size:16px;");
                setGraphic(l); setText(null); setAlignment(Pos.CENTER);
            }
        });

        colMaladie.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                Pet pet = (Pet) getTableRow().getItem();
                Label l = new Label(pet.isHasContagiousDisease() ? "✔" : "—");
                l.setStyle(pet.isHasContagiousDisease() ? "-fx-text-fill:#e87272;-fx-font-size:16px;" : "-fx-text-fill:#aaaaaa;-fx-font-size:16px;");
                setGraphic(l); setText(null); setAlignment(Pos.CENTER);
            }
        });

        colDossier.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                Pet pet = (Pet) getTableRow().getItem();
                Label l = new Label(pet.isHasMedicalRecord() ? "✔" : "—");
                l.setStyle(pet.isHasMedicalRecord() ? "-fx-text-fill:#72b8e8;-fx-font-size:16px;" : "-fx-text-fill:#aaaaaa;-fx-font-size:16px;");
                setGraphic(l); setText(null); setAlignment(Pos.CENTER);
            }
        });

        colCritique.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                Pet pet = (Pet) getTableRow().getItem();
                Label l = new Label(pet.isHasCriticalCondition() ? "✔" : "—");
                l.setStyle(pet.isHasCriticalCondition() ? "-fx-text-fill:#e8a272;-fx-font-size:16px;" : "-fx-text-fill:#aaaaaa;-fx-font-size:16px;");
                setGraphic(l); setText(null); setAlignment(Pos.CENTER);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final HBox   box       = new HBox(6, btnEdit, btnDelete);
            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color:#f5a623;-fx-text-fill:white;-fx-background-radius:6px;-fx-font-size:13px;-fx-cursor:hand;-fx-padding:5 10;");
                btnDelete.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-background-radius:6px;-fx-font-size:13px;-fx-cursor:hand;-fx-padding:5 10;");
                btnEdit.setOnAction(e -> { Pet pet = getTableRow().getItem(); if (pet != null) handleModifier(pet); });
                btnDelete.setOnAction(e -> { Pet pet = getTableRow().getItem(); if (pet != null) handleSupprimer(pet); });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterAnimal.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un animal");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleModifier(Pet pet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierAnimal.fxml"));
            Parent root = loader.load();
            ModifierAnimalController ctrl = loader.getController();
            ctrl.setPet(pet);
            Stage stage = new Stage();
            stage.setTitle("Modifier - " + pet.getName());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleSupprimer(Pet pet) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer"); confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous supprimer \"" + pet.getName() + "\" ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) { servicePet.delete(pet); loadData(); }
        });
    }

    // ── Navigation ────────────────────────────────────────────
    @FXML private void handleNavDashboard()    { naviguer("/Dashboard.fxml",          "Dashboard"); }
    @FXML private void handleNavAnnonces()     { naviguer("/AfficherAnnonces.fxml",   "Mes Annonces"); }
    @FXML private void handleNavEvenements()   { naviguer("/AfficherEvenements.fxml", "Événements"); }
    @FXML private void handleNavAnimaux()      { naviguer("/AfficherAnimales.fxml",   "Mes Animaux"); }
    @FXML private void handleNavReclamations() { naviguer("/AfficherReclamations.fxml","Réclamations"); }

    private void naviguer(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) tableAnimaux.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titre);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}