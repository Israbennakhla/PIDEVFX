package com.sitmypet.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.sitmypet.model.Message;
import com.sitmypet.model.Postulation;
import com.sitmypet.services.ServiceMessage;
import com.sitmypet.services.ServicePostulation;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessagesController {

    @FXML private HBox     navbarHBox;
    @FXML private ListView<Postulation> listConversations;
    @FXML private Label    labelInterlocuteur;
    @FXML private Label    labelStatut;
    @FXML private ScrollPane scrollMessages;
    @FXML private VBox     messagesBox;
    @FXML private TextArea inputField;

    private final ServiceMessage    serviceMsg  = new ServiceMessage();
    private final ServicePostulation servicePost = new ServicePostulation();

    private final ObservableList<Postulation> conversations = FXCollections.observableArrayList();

    // Utilisateur connecté — adapter selon le rôle
    // Pour le gardien : CURRENT_USER_ID = 2, AUTRE_USER_ID = 1
    // Pour le proprio : CURRENT_USER_ID = 1, AUTRE_USER_ID = 2
    private static final int CURRENT_USER_ID = 2;
    private static final int AUTRE_USER_ID   = 1;
    private static final String MON_NOM      = "Gardien #2";
    private static final String AUTRE_NOM    = "Proprietaire #1";

    private Postulation conversationActive = null;
    private int         dernierIdConnu     = 0;
    private Timer       pollingTimer;

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    // Postulation passée depuis NotificationBadge
    private static int postulationIdAOuvrir = -1;

    // Méthode statique pour pré-sélectionner une conversation
    public static void ouvrirConversation(int postulationId) {
        postulationIdAOuvrir = postulationId;
    }

    @FXML
    public void initialize() {
        serviceMsg.createTableIfNotExists();
        initListConversations();
        chargerConversations();

        // Envoyer avec Entrée
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                handleEnvoyer();
            }
        });

        // Si une conversation est pré-sélectionnée (depuis NotificationBadge)
        if (postulationIdAOuvrir >= 0) {
            int idAChercher = postulationIdAOuvrir;
            postulationIdAOuvrir = -1;
            Platform.runLater(() -> {
                for (Postulation p : conversations) {
                    if (p.getId() == idAChercher) {
                        listConversations.getSelectionModel().select(p);
                        ouvrirChat(p);
                        break;
                    }
                }
            });
        }
    }

    // ── Liste des conversations (postulations acceptées) ──────
    private void initListConversations() {
        listConversations.setItems(conversations);
        listConversations.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        listConversations.setCellFactory(lv -> new ListCell<Postulation>() {
            @Override
            protected void updateItem(Postulation p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setGraphic(null); setText(null);
                    setStyle("-fx-background-color:transparent;"); return;
                }
                setGraphic(buildConvItem(p));
                setText(null);
                setStyle("-fx-background-color:transparent;");
            }
        });

        listConversations.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) ouvrirChat(selected);
                });
    }

    private VBox buildConvItem(Postulation p) {
        Label nomLabel = new Label(AUTRE_NOM);
        nomLabel.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        Label postLabel = new Label("Postulation #" + p.getId());
        postLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#9b72e8;");

        Label statutLabel = new Label(p.getStatut());
        statutLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#4caf50;-fx-font-weight:bold;");

        VBox item = new VBox(3, nomLabel, postLabel, statutLabel);
        item.setPadding(new Insets(12, 16, 12, 16));

        boolean isActive = conversationActive != null && conversationActive.getId() == p.getId();
        item.setStyle("-fx-background-color:" + (isActive ? "#f0ebff" : "white") + ";"
                + "-fx-border-color:#f0f0f0 transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;-fx-cursor:hand;");

        item.setOnMouseEntered(e -> {
            if (conversationActive == null || conversationActive.getId() != p.getId())
                item.setStyle("-fx-background-color:#f8f5ff;-fx-border-color:#f0f0f0 transparent transparent transparent;-fx-border-width:1 0 0 0;-fx-cursor:hand;");
        });
        item.setOnMouseExited(e -> {
            if (conversationActive == null || conversationActive.getId() != p.getId())
                item.setStyle("-fx-background-color:white;-fx-border-color:#f0f0f0 transparent transparent transparent;-fx-border-width:1 0 0 0;-fx-cursor:hand;");
        });

        return item;
    }

    private void chargerConversations() {
        // Charger toutes les postulations acceptées du gardien
        List<Postulation> posts = servicePost.getByGardien(CURRENT_USER_ID);
        List<Postulation> acceptees = new ArrayList<>();
        for (Postulation p : posts) {
            if ("ACCEPTE".equals(p.getStatut())) acceptees.add(p);
        }
        conversations.setAll(acceptees);

        if (conversations.isEmpty()) {
            labelInterlocuteur.setText("Aucune conversation disponible");
            labelStatut.setText("");
        }
    }

    // ── Ouvrir une conversation ───────────────────────────────
    private void ouvrirChat(Postulation p) {
        // Arrêter le polling précédent
        arreterPolling();

        conversationActive = p;
        dernierIdConnu     = 0;

        labelInterlocuteur.setText("Chat avec " + AUTRE_NOM);
        labelStatut.setText("● En ligne");

        // Charger l'historique
        messagesBox.getChildren().clear();
        List<Message> historique = serviceMsg.getConversation(p.getId());
        for (Message m : historique) {
            ajouterBubble(m);
            dernierIdConnu = Math.max(dernierIdConnu, m.getId());
        }
        scrollerEnBas();

        // Activer la saisie
        inputField.setDisable(false);
        inputField.requestFocus();

        // Démarrer le polling pour cette conversation
        demarrerPolling(p.getId());

        // Rafraîchir la liste pour mettre en évidence la conversation active
        listConversations.refresh();
    }

    // ── Polling nouveaux messages ─────────────────────────────
    private void demarrerPolling(int postulationId) {
        pollingTimer = new Timer(true);
        pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<Message> nouveaux = serviceMsg.getNouveauxDepuis(postulationId, dernierIdConnu);
                if (!nouveaux.isEmpty()) {
                    Platform.runLater(() -> {
                        for (Message m : nouveaux) {
                            // Ne pas ré-afficher les messages déjà affichés
                            if (m.getExpediteurId() != CURRENT_USER_ID) {
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
        if (pollingTimer != null) { pollingTimer.cancel(); pollingTimer = null; }
    }

    // ── Envoyer un message ────────────────────────────────────
    @FXML
    private void handleEnvoyer() {
        if (conversationActive == null) return;
        String texte = inputField.getText().trim();
        if (texte.isEmpty()) return;

        Message msg = new Message(CURRENT_USER_ID, AUTRE_USER_ID,
                conversationActive.getId(), texte);
        serviceMsg.envoyer(msg);
        inputField.clear();

        // Afficher immédiatement le message envoyé
        List<Message> nouveaux = serviceMsg.getNouveauxDepuis(
                conversationActive.getId(), dernierIdConnu);
        for (Message m : nouveaux) {
            ajouterBubble(m);
            dernierIdConnu = Math.max(dernierIdConnu, m.getId());
        }
        scrollerEnBas();
    }

    // ── Bulle de message ──────────────────────────────────────
    private void ajouterBubble(Message m) {
        boolean estMoi = m.getExpediteurId() == CURRENT_USER_ID;

        // Texte du message
        Label msgLabel = new Label(m.getContenu());
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(460);
        msgLabel.setStyle("-fx-font-size:13px;-fx-text-fill:" + (estMoi ? "white" : "#1a1a2e") + ";");

        // Heure
        String heure = m.getDateEnvoi() != null
                ? m.getDateEnvoi().toLocalDateTime().format(dateFmt) : "";
        Label heureLabel = new Label(heure);
        heureLabel.setStyle("-fx-font-size:10px;-fx-text-fill:" + (estMoi ? "#e0d0ff" : "#aaaaaa") + ";");

        VBox bubble;
        if (estMoi) {
            bubble = new VBox(4, msgLabel, heureLabel);
            heureLabel.setAlignment(Pos.CENTER_RIGHT);
        } else {
            Label nomLabel = new Label(AUTRE_NOM);
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

    // ── Navigation ────────────────────────────────────────────
    private void stop() {
        arreterPolling();
    }

    @FXML private void handleNavDashboard()    {  nav("/com/sitmypet/fxml/Dashboard.fxml",           "Dashboard"); }
    @FXML private void handleNavAccueil()      {  nav("/com/sitmypet/fxml/Accueil.fxml",             "Accueil"); }
    @FXML private void handleNavPostulations() {  nav("/com/sitmypet/fxml/Postulations.fxml",        "Mes Postulations"); }
    @FXML private void handleNavAnnonces()     { nav("/com/sitmypet/fxml/AfficherAnnonces.fxml",    "Mes Annonces"); }
    @FXML private void handleNavEvenements()   {  nav("/com/sitmypet/fxml/AfficherEvenements.fxml",  "Evenements"); }
    @FXML private void handleNavAnimaux()      {  nav("/com/sitmypet/fxml/AfficherAnimales.fxml",    "Mes Animaux"); }
    @FXML private void handleNavMessages()     {  nav("/com/sitmypet/fxml/Messages.fxml",            "Messages"); }
    @FXML private void handleNavReclamations() {  nav("/com/sitmypet/fxml/AfficherReclamations.fxml","Reclamations"); }

    private void nav(String fxml, String titre) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) { System.out.println("FXML introuvable : " + fxml); return; }
            Stage stage = (Stage) messagesBox.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(resource)));
            stage.setTitle(titre); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}