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
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.io.IOException;

import model.Evenement;
import model.EventParticipant;
import model.User;
import services.ServiceEvenement;
import services.ServiceEventParticipant;
import services.ServiceUser;

public class ClientController {

    @FXML private ComboBox<User> cbMockUser;
    @FXML private TextField tfSearch;

    // View Components
    @FXML private TableView<Evenement> tableEvenements;
    @FXML private ScrollPane gridScrollPane;
    @FXML private FlowPane gridPane;
    @FXML private Button btnToggleView;
    private boolean isGridView = false;

    // Columns
    @FXML private TableColumn<Evenement, String> colNom;
    @FXML private TableColumn<Evenement, String> colDate;
    @FXML private TableColumn<Evenement, String> colHeure;
    @FXML private TableColumn<Evenement, String> colAdresse;
    @FXML private TableColumn<Evenement, String> colDescription;
    @FXML private TableColumn<Evenement, Void>   colParticipation;
    @FXML private TableColumn<Evenement, Void>   colActions;

    // Services
    private final ServiceEvenement          serviceEvenement  = new ServiceEvenement();
    private final ServiceEventParticipant   serviceParticipant = new ServiceEventParticipant();
    private final ServiceUser               serviceUser        = new ServiceUser();

    private ObservableList<Evenement> evenementsList = FXCollections.observableArrayList();
    private FilteredList<Evenement> filteredData;
    private SortedList<Evenement> sortedData;

    @FXML
    public void initialize() {
        ObservableList<User> users = FXCollections.observableArrayList(serviceUser.getAll());
        cbMockUser.setItems(users);
        if (!users.isEmpty()) cbMockUser.getSelectionModel().selectFirst();

        tableEvenements.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupColumns();

        filteredData = new FilteredList<>(evenementsList, b -> true);
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(ev -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return ev.getName().toLowerCase().contains(lower) || 
                           ev.getAddresse().toLowerCase().contains(lower) ||
                           ev.getDescription().toLowerCase().contains(lower);
                });
            });
        }

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableEvenements.comparatorProperty());
        
        // Listen to any changes in sortedData to rebuild Grid dynamically 
        sortedData.addListener((javafx.collections.ListChangeListener.Change<? extends Evenement> c) -> updateGridView());

        tableEvenements.setItems(sortedData);

        loadData();
    }

    private void setupColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("addresse"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colParticipation.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Evenement ev = (Evenement) getTableRow().getItem();
                int userId = getCurrentUserId();
                boolean enrolled = userId >= 0 && serviceParticipant.isEnrolled(ev.getId(), userId);

                badge.setText(enrolled ? "✔ Inscrit" : "✖ Non inscrit");
                badge.getStyleClass().setAll(enrolled ? "badge-enrolled" : "badge-not-enrolled");
                setGraphic(badge);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setOnAction(e -> {
                    Evenement ev = (Evenement) getTableRow().getItem();
                    if (ev != null) handleAction(ev, getCurrentUserId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Evenement ev = (Evenement) getTableRow().getItem();
                boolean enrolled = getCurrentUserId() >= 0 && serviceParticipant.isEnrolled(ev.getId(), getCurrentUserId());

                btn.setText(enrolled ? "Se désinscrire" : "Participer");
                btn.getStyleClass().setAll(enrolled ? "btn-unregister" : "btn-participate");
                setGraphic(btn);
            }
        });
    }

    @FXML
    public void toggleView(ActionEvent event) {
        isGridView = !isGridView;
        if (isGridView) {
            btnToggleView.setText("📋");
            tableEvenements.setVisible(false);
            gridScrollPane.setVisible(true);
            updateGridView();
        } else {
            btnToggleView.setText("🔲");
            tableEvenements.setVisible(true);
            gridScrollPane.setVisible(false);
        }
    }

    private void updateGridView() {
        if (!isGridView) return; // Save resources if grid is hidden
        gridPane.getChildren().clear();

        for (Evenement ev : sortedData) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4); -fx-border-color: #EDE9FF; -fx-border-width: 1px;");
            card.setPrefWidth(280);
            card.setMinHeight(220);
            
            Label lblName = new Label(ev.getName());
            lblName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
            
            int userId = getCurrentUserId();
            boolean enrolled = userId >= 0 && serviceParticipant.isEnrolled(ev.getId(), userId);

            Label lblBadge = new Label(enrolled ? "✔ Inscrit" : "✖ Non inscrit");
            lblBadge.getStyleClass().add(enrolled ? "badge-enrolled" : "badge-not-enrolled");

            Label lblDate = new Label("📅 " + ev.getDate() + " à " + ev.getHeure());
            lblDate.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");
            
            Label lblLocation = new Label("📍 " + ev.getAddresse());
            lblLocation.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");
            
            Label lblDesc = new Label(ev.getDescription());
            lblDesc.setWrapText(true);
            lblDesc.setMaxHeight(50);
            lblDesc.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-padding: 5 0;");

            Button btnAction = new Button(enrolled ? "Se désinscrire" : "Participer");
            btnAction.getStyleClass().add(enrolled ? "btn-unregister" : "btn-participate");
            btnAction.setMaxWidth(Double.MAX_VALUE);
            btnAction.setOnAction(e -> handleAction(ev, userId));
            
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            card.getChildren().addAll(lblName, lblBadge, new Separator(), lblDate, lblLocation, lblDesc, spacer, btnAction);
            gridPane.getChildren().add(card);
        }
    }

    private void handleAction(Evenement ev, int userId) {
        if (userId < 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un utilisateur !");
            return;
        }
        EventParticipant ep = new EventParticipant(ev.getId(), userId);

        if (serviceParticipant.isEnrolled(ev.getId(), userId)) {
            serviceParticipant.delete(ep);
            showAlert(Alert.AlertType.INFORMATION, "Désinscription", "Vous êtes désinscrit de : " + ev.getName());
        } else {
            serviceParticipant.add(ep);
            showAlert(Alert.AlertType.INFORMATION, "Inscription", "Félicitations, vous êtes inscrit à : " + ev.getName());
        }
        tableEvenements.refresh();
        updateGridView();
    }

    private void loadData() {
        evenementsList.setAll(serviceEvenement.getAll());
    }

    private int getCurrentUserId() {
        User selected = cbMockUser.getSelectionModel().getSelectedItem();
        return (selected == null) ? -1 : selected.getId();
    }

    @FXML
    public void onUserChanged(ActionEvent event) {
        tableEvenements.refresh();
        updateGridView();
    }

    @FXML
    public void retourAdmin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/EvenementView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("PI-DEV : Mode Administrateur");
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
