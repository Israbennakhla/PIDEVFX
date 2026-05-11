package com.sitmypet.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.sitmypet.model.Notification;
import com.sitmypet.services.NotificationPoller;
import com.sitmypet.services.ServiceNotification;

import java.util.List;
import java.util.function.Consumer;

public class NotificationBadge {

    private final int                    userId;
    private final Consumer<Notification> onNotifClick;
    private final ServiceNotification    service = new ServiceNotification();
    private NotificationPoller           poller;

    private final Button    btnCloche  = new Button();
    private final Label     badgeLabel = new Label("0");
    private final StackPane badge      = new StackPane();
    private final StackPane view       = new StackPane();
    private Stage popupStage;

    // Pour la navigation vers Messages.fxml
    private Stage  mainStage  = null;
    private int    autreUserId = 0;
    private String monNom     = "Moi";
    private String autreNom   = "Interlocuteur";

    public NotificationBadge(int userId, Consumer<Notification> onNotifClick) {
        this.userId       = userId;
        this.onNotifClick = onNotifClick;
        buildUI();
    }

    public void setChatInfo(int autreUserId, String monNom, String autreNom) {
        this.autreUserId = autreUserId;
        this.monNom      = monNom;
        this.autreNom    = autreNom;
    }

    public void setMainStage(Stage stage) {
        this.mainStage = stage;
    }

    private void buildUI() {
        Label cloche = new Label("\uD83D\uDD14");
        cloche.setStyle("-fx-font-size:18px;");
        btnCloche.setGraphic(cloche);
        btnCloche.setStyle(
                "-fx-background-color:#2d1050;-fx-background-radius:50%;" +
                        "-fx-min-width:38px;-fx-min-height:38px;" +
                        "-fx-max-width:38px;-fx-max-height:38px;" +
                        "-fx-cursor:hand;-fx-padding:0;");

        Circle circle = new Circle(10, Color.web("#e87272"));
        badgeLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:white;");
        badge.getChildren().addAll(circle, badgeLabel);
        badge.setMaxSize(20, 20);
        badge.setVisible(false);

        view.getChildren().addAll(btnCloche, badge);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        badge.setTranslateX(8);
        badge.setTranslateY(-8);
        view.setMinSize(46, 46);
        view.setMaxSize(46, 46);

        btnCloche.setOnAction(e -> {
            if (popupStage != null && popupStage.isShowing()) {
                popupStage.close(); popupStage = null;
            } else {
                ouvrirPopup();
            }
        });
    }

    private void ouvrirPopup() {
        List<Notification> notifs = service.getNonLues(userId);

        VBox content = new VBox(0);
        content.setStyle("-fx-background-color:white;-fx-border-color:#dddddd;-fx-border-width:1;");
        content.setPrefWidth(400);

        // En-tête
        HBox header = new HBox();
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#f8f5ff;-fx-border-color:transparent transparent #e8e0f5 transparent;-fx-border-width:0 0 1 0;");
        Label titre = new Label("Notifications (" + notifs.size() + ")");
        titre.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");
        header.getChildren().add(titre);

        if (!notifs.isEmpty()) {
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Button btnTout = new Button("Tout marquer lu");
            btnTout.setStyle("-fx-background-color:transparent;-fx-text-fill:#9b72e8;-fx-font-size:11px;-fx-cursor:hand;-fx-padding:0;");
            btnTout.setOnAction(e -> {
                service.marquerToutesLues(userId);
                mettreAJourBadge(0);
                fermerPopup();
            });
            header.getChildren().addAll(sp, btnTout);
        }
        content.getChildren().add(header);

        if (notifs.isEmpty()) {
            Label vide = new Label("Aucune nouvelle notification");
            vide.setStyle("-fx-font-size:13px;-fx-text-fill:#888888;-fx-padding:24 16;");
            content.getChildren().add(vide);
        } else {
            ScrollPane scroll = new ScrollPane();
            scroll.setFitToWidth(true);
            scroll.setPrefHeight(Math.min(notifs.size() * 160, 480));
            scroll.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
            VBox liste = new VBox(0);
            for (Notification n : notifs) liste.getChildren().add(buildItem(n));
            scroll.setContent(liste);
            content.getChildren().add(scroll);
        }

        popupStage = new Stage(StageStyle.UNDECORATED);
        popupStage.setAlwaysOnTop(true);
        popupStage.setResizable(false);
        popupStage.setScene(new Scene(content));
        popupStage.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) fermerPopup();
        });

        javafx.geometry.Bounds b = btnCloche.localToScreen(btnCloche.getBoundsInLocal());
        if (b != null) {
            popupStage.setX(Math.max(0, b.getMaxX() - 400));
            popupStage.setY(b.getMaxY() + 4);
        }
        popupStage.show();
        popupStage.requestFocus();
    }

    private VBox buildItem(Notification n) {
        boolean isPost = "NOUVELLE_POSTULATION".equals(n.getType());
        boolean isAcc  = "POSTULATION_ACCEPTEE".equals(n.getType());

        Label iconLabel = new Label(isPost ? "\uD83D\uDCE8" : "\u2705");
        iconLabel.setStyle("-fx-font-size:22px;");
        iconLabel.setMinWidth(36);

        Label msgLabel = new Label(n.getMessage());
        msgLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#222222;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(310);

        String dateStr = n.getDateCreation() != null
                ? n.getDateCreation().toLocalDateTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                : "";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#aaaaaa;");

        VBox textBox = new VBox(4, msgLabel, dateLabel);
        HBox row = new HBox(10, iconLabel, textBox);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(12, 16, 8, 16));

        VBox item = new VBox(0, row);
        item.setStyle("-fx-background-color:" + (isPost ? "#fdf8ff" : "#f0fff4") + ";"
                + "-fx-border-color:#eeeeee transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");

        if (isPost) {
            // Propriétaire : Accepter / Refuser
            Button btnAccepter = new Button("Accepter");
            btnAccepter.setStyle(
                    "-fx-background-color:#9b72e8;-fx-text-fill:white;" +
                            "-fx-font-size:12px;-fx-font-weight:bold;" +
                            "-fx-background-radius:8px;-fx-padding:7 18;-fx-cursor:hand;");
            Button btnRefuser = new Button("Refuser");
            btnRefuser.setStyle(
                    "-fx-background-color:white;-fx-text-fill:#e87272;" +
                            "-fx-font-size:12px;-fx-font-weight:bold;" +
                            "-fx-background-radius:8px;-fx-padding:7 18;-fx-cursor:hand;" +
                            "-fx-border-color:#e87272;-fx-border-radius:8px;");

            btnAccepter.setOnAction(e -> {
                service.marquerLue(n.getId());
                fermerPopup();
                rafraichirBadge();
                if (onNotifClick != null) onNotifClick.accept(n);
            });
            btnRefuser.setOnAction(e -> {
                service.marquerLue(n.getId());
                n.setType("REFUSER");
                fermerPopup();
                rafraichirBadge();
                if (onNotifClick != null) onNotifClick.accept(n);
            });

            HBox actions = new HBox(10, btnAccepter, btnRefuser);
            actions.setPadding(new Insets(4, 16, 14, 52));
            actions.setAlignment(Pos.CENTER_LEFT);
            item.getChildren().add(actions);

        } else if (isAcc) {
            // Gardien : OK + Ouvrir le chat (navigue vers Messages.fxml)
            Button btnOk = new Button("OK");
            btnOk.setStyle(
                    "-fx-background-color:#4caf50;-fx-text-fill:white;" +
                            "-fx-font-size:12px;-fx-font-weight:bold;" +
                            "-fx-background-radius:8px;-fx-padding:7 14;-fx-cursor:hand;");

            Button btnChat = new Button("\uD83D\uDCAC Ouvrir le chat");
            btnChat.setStyle(
                    "-fx-background-color:#9b72e8;-fx-text-fill:white;" +
                            "-fx-font-size:12px;-fx-font-weight:bold;" +
                            "-fx-background-radius:8px;-fx-padding:7 14;-fx-cursor:hand;");

            btnOk.setOnAction(e -> {
                service.marquerLue(n.getId());
                fermerPopup();
                rafraichirBadge();
                if (onNotifClick != null) onNotifClick.accept(n);
            });

            btnChat.setOnAction(e -> {
                service.marquerLue(n.getId());
                fermerPopup();
                rafraichirBadge();
                if (onNotifClick != null) onNotifClick.accept(n);
                // Naviguer vers la page Messages avec la conversation pré-sélectionnée
                naviguerVersMessages(n.getPostulationId());
            });

            HBox actions = new HBox(10, btnOk, btnChat);
            actions.setPadding(new Insets(4, 16, 14, 52));
            actions.setAlignment(Pos.CENTER_LEFT);
            item.getChildren().add(actions);
        }

        return item;
    }

    // ── Naviguer vers /Messages.fxml ──────────────────────────
    private void naviguerVersMessages(int postulationId) {
        try {
            // Pré-sélectionner la conversation
            MessagesController.ouvrirConversation(postulationId);

            var resource = getClass().getResource("/com/sitmypet/fxml/Messages.fxml");
            if (resource == null) {
                System.out.println("Messages.fxml introuvable");
                return;
            }

            // Récupérer la fenêtre principale depuis le bouton cloche
            Stage stage = (Stage) btnCloche.getScene().getWindow();
            stage.setScene(new Scene(javafx.fxml.FXMLLoader.load(resource)));
            stage.setTitle("Messages");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ── Badge ─────────────────────────────────────────────────
    public void mettreAJourBadge(int count) {
        javafx.application.Platform.runLater(() -> {
            if (count > 0) {
                badgeLabel.setText(count > 9 ? "9+" : String.valueOf(count));
                badge.setVisible(true);
            } else {
                badge.setVisible(false);
            }
        });
    }

    public void rafraichirBadge() {
        mettreAJourBadge(service.getNonLues(userId).size());
    }

    private void fermerPopup() {
        if (popupStage != null) { popupStage.close(); popupStage = null; }
    }

    public void startPolling() {
        rafraichirBadge();
        poller = new NotificationPoller(userId, notifs -> mettreAJourBadge(notifs.size()));
        poller.start();
    }

    public void stopPolling() {
        if (poller != null) { poller.stop(); poller = null; }
        fermerPopup();
    }

    public StackPane getView() { return view; }
}