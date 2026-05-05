package com.sitmypet.controllers;

import com.sitmypet.SessionContext;
import com.sitmypet.model.Announcement;
import com.sitmypet.model.Message;
import com.sitmypet.model.Postulation;
import com.sitmypet.model.User;
import com.sitmypet.services.ServiceAnnouncement;
import com.sitmypet.services.ServiceMessage;
import com.sitmypet.services.ServicePostulation;
import com.sitmypet.services.ServiceUser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessagerieController {

    private static int pendingPostulationId = -1;

    public static void setPendingPostulationId(int id) {
        pendingPostulationId = id;
    }

    @FXML private ListView<Postulation> listConversations;
    @FXML private Label labelInterlocuteur;
    @FXML private Label labelStatut;
    @FXML private ScrollPane scrollMessages;
    @FXML private VBox messagesBox;
    @FXML private TextArea inputField;

    private final ServiceMessage serviceMsg = new ServiceMessage();
    private final ServicePostulation servicePost = new ServicePostulation();
    private final ServiceAnnouncement serviceAnn = new ServiceAnnouncement();
    private final ServiceUser serviceUser = new ServiceUser();

    private final ObservableList<Postulation> conversations = FXCollections.observableArrayList();

    private Postulation conversationActive;
    private int dernierIdConnu;
    private Timer pollingTimer;

    private int otherUserId;
    private String otherDisplayName = "";

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @FXML
    public void initialize() {
        initListConversations();
        chargerConversations();

        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                handleEnvoyer();
            }
        });

        int toOpen = pendingPostulationId;
        pendingPostulationId = -1;
        if (toOpen >= 0) {
            int idSeek = toOpen;
            Platform.runLater(() -> {
                for (Postulation p : conversations) {
                    if (p.getId() == idSeek) {
                        listConversations.getSelectionModel().select(p);
                        ouvrirChat(p);
                        break;
                    }
                }
            });
        }
    }

    private void initListConversations() {
        listConversations.setItems(conversations);
        listConversations.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        listConversations.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Postulation p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setGraphic(null);
                    return;
                }
                User u = SessionContext.getCurrentUser();
                if (u == null) return;
                Announcement ann = serviceAnn.getById(p.getAnnouncementId());
                int other = (p.getGardienId() == u.getId()) ? ann.getUserId() : p.getGardienId();
                String nom = serviceUser.getDisplayNameById(other);
                Label nomLabel = new Label(nom);
                nomLabel.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");
                String nomPostulant = serviceUser.getDisplayNameById(p.getGardienId());
                Label statutLabel = new Label(p.getStatut());
                statutLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#4caf50;-fx-font-weight:bold;");
                VBox item;
                if (nomPostulant != null && !nomPostulant.isBlank() && !nomPostulant.equals(nom)) {
                    Label postulantLabel = new Label("Postulant : " + nomPostulant);
                    postulantLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#9b72e8;");
                    item = new VBox(3, nomLabel, postulantLabel, statutLabel);
                } else {
                    item = new VBox(3, nomLabel, statutLabel);
                }
                item.setPadding(new Insets(12, 16, 12, 16));
                item.setStyle("-fx-background-color:white;-fx-border-color:#f0f0f0 transparent transparent transparent;"
                        + "-fx-border-width:1 0 0 0;-fx-cursor:hand;");
                setGraphic(item);
            }
        });

        listConversations.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) ouvrirChat(selected);
        });
    }

    private void chargerConversations() {
        User u = SessionContext.getCurrentUser();
        if (u == null) return;
        conversations.setAll(servicePost.getAccepteesInvolvingUser(u.getId()));
        if (conversations.isEmpty()) {
            labelInterlocuteur.setText("Aucune conversation (postulation acceptée requise)");
            labelStatut.setText("");
        }
    }

    private void ouvrirChat(Postulation p) {
        arreterPolling();
        User u = SessionContext.getCurrentUser();
        if (u == null) return;

        Announcement ann = serviceAnn.getById(p.getAnnouncementId());
        if (ann == null) return;

        if (p.getGardienId() == u.getId()) {
            otherUserId = ann.getUserId();
        } else {
            otherUserId = p.getGardienId();
        }
        otherDisplayName = serviceUser.getDisplayNameById(otherUserId);

        conversationActive = p;
        dernierIdConnu = 0;
        labelInterlocuteur.setText("Chat avec " + otherDisplayName);
        labelStatut.setText("● Connecté");

        messagesBox.getChildren().clear();
        List<Message> historique = serviceMsg.getConversation(p.getId());
        for (Message m : historique) {
            ajouterBubble(m);
            dernierIdConnu = Math.max(dernierIdConnu, m.getId());
        }
        scrollerEnBas();

        inputField.setDisable(false);
        inputField.requestFocus();
        demarrerPolling(p.getId());
        listConversations.refresh();
    }

    private void demarrerPolling(int postulationId) {
        pollingTimer = new Timer(true);
        pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<Message> nouveaux = serviceMsg.getNouveauxDepuis(postulationId, dernierIdConnu);
                if (!nouveaux.isEmpty()) {
                    User u = SessionContext.getCurrentUser();
                    Platform.runLater(() -> {
                        for (Message m : nouveaux) {
                            if (u != null && m.getExpediteurId() != u.getId()) {
                                ajouterBubble(m);
                            }
                            dernierIdConnu = Math.max(dernierIdConnu, m.getId());
                        }
                        scrollerEnBas();
                    });
                }
            }
        }, 2000, 2000);
    }

    private void arreterPolling() {
        if (pollingTimer != null) {
            pollingTimer.cancel();
            pollingTimer = null;
        }
    }

    @FXML
    private void handleEnvoyer() {
        if (conversationActive == null) return;
        User u = SessionContext.getCurrentUser();
        if (u == null) return;
        String texte = inputField.getText().trim();
        if (texte.isEmpty()) return;

        Message msg = new Message(u.getId(), otherUserId, conversationActive.getId(), texte);
        serviceMsg.envoyer(msg);
        inputField.clear();

        List<Message> nouveaux = serviceMsg.getNouveauxDepuis(conversationActive.getId(), dernierIdConnu);
        for (Message m : nouveaux) {
            ajouterBubble(m);
            dernierIdConnu = Math.max(dernierIdConnu, m.getId());
        }
        scrollerEnBas();
    }

    private void ajouterBubble(Message m) {
        User u = SessionContext.getCurrentUser();
        boolean estMoi = u != null && m.getExpediteurId() == u.getId();

        Label msgLabel = new Label(m.getContenu());
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(460);
        msgLabel.setStyle("-fx-font-size:13px;-fx-text-fill:" + (estMoi ? "white" : "#1a1a2e") + ";");

        String heure = m.getDateEnvoi() != null
                ? m.getDateEnvoi().toLocalDateTime().format(dateFmt) : "";
        Label heureLabel = new Label(heure);
        heureLabel.setStyle("-fx-font-size:10px;-fx-text-fill:" + (estMoi ? "#e0d0ff" : "#aaaaaa") + ";");

        VBox bubble;
        if (estMoi) {
            bubble = new VBox(4, msgLabel, heureLabel);
        } else {
            Label nomLabel = new Label(otherDisplayName);
            nomLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#9b72e8;");
            bubble = new VBox(4, nomLabel, msgLabel, heureLabel);
        }

        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setStyle(
                "-fx-background-color:" + (estMoi ? "#9b72e8" : "white") + ";"
                        + "-fx-background-radius:" + (estMoi ? "18 4 18 18" : "4 18 18 18") + ";"
                        + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),4,0,0,2);");
        bubble.setMaxWidth(500);

        HBox row = new HBox(bubble);
        row.setAlignment(estMoi ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(row, Priority.ALWAYS);
        messagesBox.getChildren().add(row);
    }

    private void scrollerEnBas() {
        Platform.runLater(() -> scrollMessages.setVvalue(1.0));
    }

    public void refreshList() {
        chargerConversations();
    }
}
