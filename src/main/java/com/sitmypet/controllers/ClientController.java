package com.sitmypet.controllers;

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
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.io.IOException;

import com.sitmypet.utils.QRCodeGenerator;
import com.sitmypet.utils.EmailService;

import com.sitmypet.model.Evenement;
import com.sitmypet.model.EventParticipant;
import com.sitmypet.model.User;
import com.sitmypet.services.ServiceEvenement;
import com.sitmypet.services.ServiceEventParticipant;
import com.sitmypet.services.ServiceUser;

public class ClientController {

    @FXML private ComboBox<User> cbMockUser;
    @FXML private TextField tfSearch;

    // View Components
    @FXML private ScrollPane gridScrollPane;
    @FXML private FlowPane gridPane;

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
        // Listen to any changes in sortedData to rebuild Grid dynamically 
        sortedData.addListener((javafx.collections.ListChangeListener.Change<? extends Evenement> c) -> updateGridView());

        loadData();
        updateGridView();
    }

    private void updateGridView() {
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

            Label lblDate = new Label(" " + ev.getDate() + " à " + ev.getHeure());
            lblDate.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");
            
            Label lblLocation = new Label(" " + ev.getAddresse());
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

            // ── Make card clickable → open event detail overlay ──────────
            card.setOnMouseClicked(mouseEvent -> {
                // Don't trigger if a button was clicked (check the whole parent chain)
                javafx.scene.Node target = mouseEvent.getPickResult().getIntersectedNode();
                while (target != null) {
                    if (target instanceof javafx.scene.control.ButtonBase) return;
                    target = target.getParent();
                }
                showEventDetail(ev);
            });
            card.setCursor(javafx.scene.Cursor.HAND);

            gridPane.getChildren().add(card);
        }
    }

    /**
     * Opens the premium Event Detail overlay with map for the given event.
     */
    private void showEventDetail(Evenement ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/EventDetailOverlay.fxml"));
            Parent root = loader.load();

            EventDetailOverlayController ctrl = loader.getController();
            ctrl.initData(ev, cbMockUser.getSelectionModel().getSelectedItem());

            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.initStyle(StageStyle.TRANSPARENT);
            detailStage.setTitle(" " + ev.getName());
            detailStage.setScene(new Scene(root));
            detailStage.getScene().setFill(null);

            // Force map resize AFTER the stage is visible on screen
            detailStage.setOnShown(e -> ctrl.forceMapResize());

            // Refresh grid if user registered/unregistered
            detailStage.setOnHidden(e -> {
                if (ctrl.isActionTaken()) {
                    updateGridView();
                }
            });

            detailStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails de l'événement.");
        }
    }

    private void handleAction(Evenement ev, int userId) {
        if (userId < 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un utilisateur !");
            return;
        }
        EventParticipant ep = new EventParticipant(ev.getId(), userId);

        if (serviceParticipant.isEnrolled(ev.getId(), userId)) {
            // ── Désinscription (comportement inchangé) ────────────────────
            serviceParticipant.delete(ep);
            showAlert(Alert.AlertType.INFORMATION, "Désinscription",
                    "Vous êtes désinscrit de : " + ev.getName());
        } else {
            // ── Inscription ───────────────────────────────────────────────
            serviceParticipant.add(ep);

            // Récupérer l'utilisateur sélectionné
            User user = cbMockUser.getSelectionModel().getSelectedItem();
            String fullName  = user.getPrenom() + " " + user.getNom();
            String dateStr   = ev.getDate()  != null ? ev.getDate().toString()  : "N/A";
            String heureStr  = ev.getHeure() != null ? ev.getHeure()            : "N/A";

            // ── Étape 1 : Construire le JSON du billet ────────────────────
            String ticketJson = QRCodeGenerator.buildTicketJson(
                    user.getId(), user.getNom(), user.getPrenom(), user.getEmail(),
                    ev.getId(), ev.getName(), dateStr, heureStr);

            // ── Étape 2 : Générer le QR Code ──────────────────────────────
            Image qrFXImage = null;
            java.awt.image.BufferedImage qrBufferedImage = null;
            try {
                qrBufferedImage = QRCodeGenerator.generateQRCodeImage(ticketJson, 300, 300);
                qrFXImage       = QRCodeGenerator.generateQRCodeFXImage(ticketJson, 300, 300);
            } catch (Exception ex) {
                System.err.println(" Erreur génération QR Code : " + ex.getMessage());
            }

            // ── Étape 3 : Ouvrir le popup TicketDialog ────────────────────
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/sitmypet/fxml/TicketDialog.fxml"));
                Parent root = loader.load();

                TicketDialogController ticketCtrl = loader.getController();
                ticketCtrl.initData(
                        ev.getName(), fullName, user.getEmail(),
                        dateStr, heureStr, ev.getAddresse(),
                        user.getId(), ev.getId(), qrFXImage);

                Stage ticketStage = new Stage();
                ticketStage.initModality(Modality.APPLICATION_MODAL);
                ticketStage.initStyle(StageStyle.TRANSPARENT);
                ticketStage.setTitle("🎟️ Billet Électronique");
                ticketStage.setScene(new Scene(root));
                ticketStage.getScene().setFill(null); // transparent background
                ticketStage.show();

                // ── Étape 4 : Envoyer l'email de façon asynchrone ─────────
                final java.awt.image.BufferedImage finalQrImage = qrBufferedImage;
                final TicketDialogController finalCtrl = ticketCtrl;
                if (finalQrImage != null) {
                    EmailService.sendTicketEmail(
                            user.getEmail(), fullName,
                            ev.getName(), dateStr, heureStr, ev.getAddresse(),
                            user.getId(), ev.getId(), finalQrImage)
                        .thenAccept(success -> finalCtrl.setEmailStatus(success));
                } else {
                    ticketCtrl.setEmailStatus(false);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.INFORMATION, "Inscription",
                        "Félicitations, vous êtes inscrit à : " + ev.getName());
            }
        }
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
        updateGridView();
    }

    @FXML
    public void retourAdmin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/EvenementView.fxml"));
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

