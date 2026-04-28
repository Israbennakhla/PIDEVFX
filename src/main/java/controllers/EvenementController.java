package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Evenement;
import services.ServiceEvenement;

public class EvenementController {
    public static Evenement evenementSelectionneToEdit = null;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbSort;
    @FXML private FlowPane gridPane;

    private ServiceEvenement service = new ServiceEvenement();
    
    private ObservableList<Evenement> evenementsList = FXCollections.observableArrayList();
    private FilteredList<Evenement> filteredData;
    private SortedList<Evenement> sortedData;

    @FXML
    public void initialize() {
        if (cbSort != null) {
            cbSort.getItems().addAll("Nom (A-Z)", "Nom (Z-A)", "Date (Croissante)", "Date (Décroissante)");
            cbSort.getSelectionModel().selectFirst();
        }

        filteredData = new FilteredList<>(evenementsList, b -> true);
        
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(ev -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return ev.getName().toLowerCase().contains(lower) || 
                           ev.getAddresse().toLowerCase().contains(lower);
                });
            });
        }

        sortedData = new SortedList<>(filteredData);
        if (cbSort != null) {
            cbSort.valueProperty().addListener((obs, oldVal, newVal) -> {
                actuateSort(newVal);
                updateGridView();
            });
            actuateSort(cbSort.getValue());
        }

        sortedData.addListener((javafx.collections.ListChangeListener.Change<? extends Evenement> c) -> updateGridView());
        
        afficherEvenements();
        updateGridView();
    }
    
    private void actuateSort(String newVal) {
        if (newVal == null) return;
        sortedData.setComparator((e1, e2) -> {
            switch (newVal) {
                case "Nom (Z-A)": return e2.getName().compareToIgnoreCase(e1.getName());
                case "Date (Croissante)": 
                    if (e1.getDate() == null) return 1;
                    if (e2.getDate() == null) return -1;
                    return e1.getDate().compareTo(e2.getDate());
                case "Date (Décroissante)": 
                    if (e1.getDate() == null) return 1;
                    if (e2.getDate() == null) return -1;
                    return e2.getDate().compareTo(e1.getDate());
                case "Nom (A-Z)":
                default: 
                    return e1.getName().compareToIgnoreCase(e2.getName());
            }
        });
    }

    private void afficherEvenements() {
        evenementsList.setAll(service.getAll());
    }

    private void updateGridView() {
        if (gridPane == null) return;
        gridPane.getChildren().clear();

        for (Evenement ev : sortedData) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4); -fx-border-color: #EDE9FF; -fx-border-width: 1px;");
            card.setPrefWidth(280);
            card.setMinHeight(220);
            
            Label lblName = new Label(ev.getName());
            lblName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
            
            Label lblDate = new Label("📅 " + ev.getDate() + " à " + ev.getHeure());
            lblDate.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");
            
            Label lblLocation = new Label("📍 " + ev.getAddresse());
            lblLocation.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");
            
            Label lblDesc = new Label(ev.getDescription());
            lblDesc.setWrapText(true);
            lblDesc.setMaxHeight(50);
            lblDesc.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-padding: 5 0;");

            Button btnEdit = new Button("✎ Modifier");
            btnEdit.getStyleClass().add("btn-edit");
            btnEdit.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btnEdit, Priority.ALWAYS);
            btnEdit.setOnAction(e -> {
                evenementSelectionneToEdit = ev;
                ouvrirVueFormulaire(e);
            });

            Button btnDelete = new Button("🗑 Supprimer");
            btnDelete.getStyleClass().add("btn-delete");
            btnDelete.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btnDelete, Priority.ALWAYS);
            btnDelete.setOnAction(e -> {
                service.delete(ev);
                afficherEvenements();
            });

            HBox actionsBox = new HBox(10, btnEdit, btnDelete);
            
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            card.getChildren().addAll(lblName, new Separator(), lblDate, lblLocation, lblDesc, spacer, actionsBox);
            gridPane.getChildren().add(card);
        }
    }

    @FXML
    public void allerVersAjout(ActionEvent event) {
        evenementSelectionneToEdit = null;
        ouvrirVueFormulaire(event);
    }

    private void ouvrirVueFormulaire(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EvenementFormView.fxml"));
            Parent root = loader.load();

            EvenementFormController formCtrl = loader.getController();

            Stage overlayStage = new Stage();
            overlayStage.initModality(Modality.APPLICATION_MODAL);
            overlayStage.initStyle(StageStyle.TRANSPARENT);
            overlayStage.setTitle("📋 Formulaire Événement");
            Scene scene = new Scene(root);
            scene.setFill(null);
            overlayStage.setScene(scene);

            // When the overlay closes, refresh data if something was saved
            overlayStage.setOnHidden(e -> {
                EvenementController.evenementSelectionneToEdit = null;
                if (formCtrl.isSaved()) {
                    afficherEvenements();
                    updateGridView();
                }
            });

            overlayStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void ouvrirParticipants(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ParticipantView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("PI-DEV : Gestion des Participants");
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirClient(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ClientView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("PI-DEV : Espace Client (Front-Office)");
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}