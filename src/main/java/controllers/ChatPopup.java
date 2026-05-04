package controllers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Message;
import services.ServiceMessage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Popup de messagerie temps réel entre gardien et propriétaire.
 *
 * Utilisation :
 *   ChatPopup chat = new ChatPopup(
 *       postulationId,
 *       moi_id,          // gardien ou proprio
 *       autre_id,        // l'autre personne
 *       "Gardien #2",    // mon nom affiché
 *       "Propriétaire #1" // nom de l'autre
 *   );
 *   chat.show();
 */
public class ChatPopup {

    private final int            postulationId;
    private final int            moiId;
    private final int            autreId;
    private final String         monNom;
    private final String         autreNom;
    private final ServiceMessage serviceMsg = new ServiceMessage();

    private Stage      stage;
    private VBox       messagesBox;
    private ScrollPane scrollPane;
    private TextArea   inputField;
    private Timer      pollingTimer;
    private int        dernierIdConnu = 0;

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    public ChatPopup(int postulationId, int moiId, int autreId,
                     String monNom, String autreNom) {
        this.postulationId = postulationId;
        this.moiId         = moiId;
        this.autreId       = autreId;
        this.monNom        = monNom;
        this.autreNom      = autreNom;
        serviceMsg.createTableIfNotExists();
    }

    public void show() {
        if (stage != null && stage.isShowing()) { stage.toFront(); return; }

        stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Messagerie — " + monNom + " & " + autreNom);
        stage.setWidth(480);
        stage.setHeight(580);
        stage.setResizable(true);

        // ── En-tête ───────────────────────────────────────────
        HBox header = new HBox();
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#1a0a2e;");

        Label title = new Label("Chat avec " + autreNom);
        title.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:white;");

        Label statusDot = new Label("● En ligne");
        statusDot.setStyle("-fx-font-size:11px;-fx-text-fill:#4caf50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnFermer = new Button("✕");
        btnFermer.setStyle("-fx-background-color:transparent;-fx-text-fill:#cccccc;"
                + "-fx-font-size:16px;-fx-cursor:hand;-fx-padding:0;");
        btnFermer.setOnAction(e -> fermer());

        header.getChildren().addAll(title, spacer, statusDot, new Label("  "), btnFermer);

        // ── Zone messages ─────────────────────────────────────
        messagesBox = new VBox(8);
        messagesBox.setPadding(new Insets(14));
        messagesBox.setFillWidth(true);

        scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color:#f0f0f8;-fx-border-color:transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // ── Zone saisie ───────────────────────────────────────
        inputField = new TextArea();
        inputField.setPromptText("Ecrivez votre message...");
        inputField.setPrefRowCount(2);
        inputField.setWrapText(true);
        inputField.setStyle(
                "-fx-background-color:white;-fx-border-color:#dddddd;"
                        + "-fx-border-radius:0;-fx-font-size:13px;-fx-padding:10;");

        // Envoyer avec Entrée (Shift+Entrée pour saut de ligne)
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                envoyerMessage();
            }
        });

        Button btnEnvoyer = new Button("Envoyer ▶");
        btnEnvoyer.setStyle(
                "-fx-background-color:#9b72e8;-fx-text-fill:white;"
                        + "-fx-font-size:13px;-fx-font-weight:bold;"
                        + "-fx-background-radius:0;-fx-cursor:hand;"
                        + "-fx-padding:10 20;");
        btnEnvoyer.setOnAction(e -> envoyerMessage());

        HBox inputBar = new HBox(0, inputField, btnEnvoyer);
        inputBar.setAlignment(Pos.CENTER);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputBar.setStyle("-fx-border-color:#dddddd transparent transparent transparent;-fx-border-width:1 0 0 0;");

        // ── Layout ────────────────────────────────────────────
        VBox root = new VBox(0, header, scrollPane, inputBar);
        root.setStyle("-fx-background-color:#f0f0f8;");

        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> fermer());
        stage.show();

        // Charger l'historique existant
        chargerHistorique();

        // Démarrer le polling (nouveaux messages toutes les 2 secondes)
        demarrerPolling();
    }

    // ── Charger l'historique ──────────────────────────────────
    private void chargerHistorique() {
        List<Message> msgs = serviceMsg.getConversation(postulationId);
        messagesBox.getChildren().clear();
        for (Message m : msgs) {
            ajouterBubble(m);
            dernierIdConnu = Math.max(dernierIdConnu, m.getId());
        }
        scrollerEnBas();
    }

    // ── Polling nouveaux messages ─────────────────────────────
    private void demarrerPolling() {
        pollingTimer = new Timer(true);
        pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<Message> nouveaux = serviceMsg.getNouveauxDepuis(postulationId, dernierIdConnu);
                if (!nouveaux.isEmpty()) {
                    Platform.runLater(() -> {
                        for (Message m : nouveaux) {
                            ajouterBubble(m);
                            dernierIdConnu = Math.max(dernierIdConnu, m.getId());
                        }
                        scrollerEnBas();
                    });
                }
            }
        }, 2000, 2000); // toutes les 2 secondes
    }

    // ── Envoyer un message ────────────────────────────────────
    private void envoyerMessage() {
        String texte = inputField.getText().trim();
        if (texte.isEmpty()) return;

        Message msg = new Message(moiId, autreId, postulationId, texte);
        serviceMsg.envoyer(msg);
        inputField.clear();

        // Recharger pour avoir l'ID et la date du message envoyé
        List<Message> nouveaux = serviceMsg.getNouveauxDepuis(postulationId, dernierIdConnu);
        for (Message m : nouveaux) {
            ajouterBubble(m);
            dernierIdConnu = Math.max(dernierIdConnu, m.getId());
        }
        scrollerEnBas();
    }

    // ── Construire une bulle de message ───────────────────────
    private void ajouterBubble(Message m) {
        boolean estMoi = m.getExpediteurId() == moiId;

        // Contenu
        Label msgLabel = new Label(m.getContenu());
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(300);
        msgLabel.setStyle("-fx-font-size:13px;-fx-text-fill:" + (estMoi ? "white" : "#1a1a2e") + ";");

        // Heure
        String heure = m.getDateEnvoi() != null
                ? m.getDateEnvoi().toLocalDateTime().format(timeFmt)
                : "";
        Label heureLabel = new Label(heure);
        heureLabel.setStyle("-fx-font-size:10px;-fx-text-fill:" + (estMoi ? "#e0d0ff" : "#aaaaaa") + ";");

        // Nom expéditeur (seulement si message de l'autre)
        VBox bubble;
        if (estMoi) {
            bubble = new VBox(3, msgLabel, heureLabel);
        } else {
            Label nomLabel = new Label(autreNom);
            nomLabel.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#9b72e8;");
            bubble = new VBox(3, nomLabel, msgLabel, heureLabel);
        }

        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setStyle(
                "-fx-background-color:" + (estMoi ? "#9b72e8" : "white") + ";"
                        + "-fx-background-radius:" + (estMoi ? "18 4 18 18" : "4 18 18 18") + ";"
                        + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),4,0,0,2);");
        bubble.setMaxWidth(320);

        // Aligner à droite si moi, à gauche si autre
        HBox row = new HBox(bubble);
        row.setAlignment(estMoi ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);

        messagesBox.getChildren().add(row);
    }

    // ── Scroller en bas ───────────────────────────────────────
    private void scrollerEnBas() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    // ── Fermer ────────────────────────────────────────────────
    public void fermer() {
        if (pollingTimer != null) { pollingTimer.cancel(); pollingTimer = null; }
        if (stage != null)        { stage.close(); stage = null; }
    }
}
