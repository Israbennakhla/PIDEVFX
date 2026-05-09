package com.sitmypet.controllers;

import com.sitmypet.SessionContext;
import com.sitmypet.i18n.AppTexts;
import com.sitmypet.model.Reclamation;
import com.sitmypet.model.Reponse;
import com.sitmypet.services.ServiceReclamation;
import com.sitmypet.services.ServiceReponse;
import com.sitmypet.utils.TraductionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


public class MesReclamationsController {

    @FXML private Label lblPageTitle;
    @FXML private Button btnNouvelle;
    @FXML private Label lblStatWaitLbl;
    @FXML private Label lblStatWait;
    @FXML private Label lblStatCoursLbl;
    @FXML private Label lblStatCours;
    @FXML private Label lblStatDoneLbl;
    @FXML private Label lblStatDone;
    @FXML private TextField rechercheNom;
    @FXML private Label lblFiltStatut;
    @FXML private ComboBox<String> filtreStatut;
    @FXML private Label lblFiltPriorite;
    @FXML private ComboBox<String> filtrePriorite;
    @FXML private Button btnFiltrer;
    @FXML private Button btnReset;
    @FXML private ScrollPane scroll;
    @FXML private GridPane gridCartes;

    private final ServiceReclamation serviceRec = new ServiceReclamation();
    private final ServiceReponse serviceRep = new ServiceReponse();

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        filtreStatut.setItems(FXCollections.observableArrayList("Tous", "en_attente", "en_cours", "resolue"));
        filtreStatut.setValue("Tous");
        filtrePriorite.setItems(FXCollections.observableArrayList("Tous", "basse", "moyenne", "haute"));
        filtrePriorite.setValue("Tous");

        rechercheNom.textProperty().addListener((o, a, b) -> appliquerFiltre());
        bindTexts();
        rechargerPourUtilisateurCourant();
    }

    /** Réutilisable si SessionContext utilisateur inchangé (ex. fermeture modale ajout). */
    public void rechargerPourUtilisateurCourant() {
        bindTexts();
        chargerStats();
        appliquerFiltre();
    }

    private void bindTexts() {
        lblPageTitle.setText(AppTexts.t("mes.rec.title"));
        btnNouvelle.setText(AppTexts.t("mes.rec.new"));
        lblStatWaitLbl.setText(AppTexts.t("mes.rec.stats.wait"));
        lblStatCoursLbl.setText(AppTexts.t("mes.rec.stats.progress"));
        lblStatDoneLbl.setText(AppTexts.t("mes.rec.stats.done"));
        lblFiltStatut.setText(AppTexts.t("mes.rec.lbl.status"));
        lblFiltPriorite.setText(AppTexts.t("mes.rec.lbl.priority"));
        rechercheNom.setPromptText(AppTexts.t("mes.rec.search"));
        btnFiltrer.setText(AppTexts.t("mes.rec.apply"));
        btnReset.setText(AppTexts.t("mes.rec.reset"));
    }

    private void chargerStats() {
        int uid = utilisateurCourantNonNullSafeId();
        if (uid <= 0) {
            lblStatWait.setText("0");
            lblStatCours.setText("0");
            lblStatDone.setText("0");
            return;
        }
        Map<String, Integer> m = serviceRec.countByStatutForUser(uid);
        lblStatWait.setText(String.valueOf(m.getOrDefault("en_attente", 0)));
        lblStatCours.setText(String.valueOf(m.getOrDefault("en_cours", 0)));
        lblStatDone.setText(String.valueOf(m.getOrDefault("resolue", 0)));
    }

    private int utilisateurCourantNonNullSafeId() {
        var u = SessionContext.getCurrentUser();
        return u != null ? u.getId() : 0;
    }

    @FXML
    private void appliquerFiltre() {
        int uid = utilisateurCourantNonNullSafeId();
        if (uid <= 0) return;

        String nom = rechercheNom.getText() != null ? rechercheNom.getText().trim() : "";
        List<Reclamation> list = serviceRec.filterForUser(uid, nom, filtreStatut.getValue(), filtrePriorite.getValue());
        chargerCartes(list);
        Platform.runLater(() -> scroll.setVvalue(0));
    }

    @FXML
    private void reinitialiserFiltre() {
        rechercheNom.clear();
        filtreStatut.setValue("Tous");
        filtrePriorite.setValue("Tous");
        chargerStats();
        appliquerFiltre();
    }

    private void chargerCartes(List<Reclamation> list) {
        gridCartes.getChildren().clear();
        gridCartes.getColumnConstraints().clear();

        for (int i = 0; i < 3; i++) {
            var cc = new javafx.scene.layout.ColumnConstraints();
            cc.setPercentWidth(33.33);
            cc.setHgrow(Priority.ALWAYS);
            gridCartes.getColumnConstraints().add(cc);
        }

        if (list.isEmpty()) {
            Label vide = new Label(AppTexts.t("mes.rec.empty"));
            vide.setStyle("-fx-font-size:15px;-fx-text-fill:#888;");
            gridCartes.add(vide, 0, 0);
            return;
        }

        int col = 0;
        int row = 0;
        for (Reclamation r : list) {
            VBox carte = creerCarte(r);
            carte.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(carte, Priority.ALWAYS);
            gridCartes.add(carte, col, row);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    private VBox creerCarte(Reclamation r) {
        boolean cloturee = serviceRep.existsForReclamation(r.getId());

        Label sujet = new Label(r.getSujet() != null ? r.getSujet() : "—");
        sujet.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:-color-text-main;");
        sujet.setWrapText(true);

        Label nm = new Label("👤  " + (r.getNomClient() != null ? r.getNomClient() : ""));
        nm.setStyle("-fx-text-fill:-color-text-secondary;");

        Label email = new Label("✉️  " + (r.getEmailClient() != null ? r.getEmailClient() : ""));
        email.setStyle("-fx-text-fill:-color-text-secondary;");
        email.setWrapText(true);

        String desc = r.getDescription() != null ? r.getDescription() : "";
        if (desc.length() > 100) desc = desc.substring(0, 97) + "…";
        Label description = new Label(desc);
        description.setWrapText(true);
        description.setStyle("-fx-font-size:12px;-fx-text-fill:#888888;");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.getChildren().addAll(
                badgeLabel(formatStatut(r.getStatut()), classeStatut(r.getStatut())),
                badgeLabel(r.getPriorite() != null ? r.getPriorite().toUpperCase() : "?",
                        classePriorite(r.getPriorite())));
        if (cloturee) {
            Label clo = new Label(AppTexts.isEnglish() ? "🔒 Closed" : "🔒 Clôturée");
            clo.setStyle("-fx-background-color:#555;-fx-text-fill:#eee;-fx-font-size:10px;-fx-padding:3 10;-fx-background-radius:8;");
            badges.getChildren().add(clo);
        }

        String dateTxt = r.getDateReclamation() != null ? r.getDateReclamation().format(DF) : "—";
        Label dateLbl = new Label("📅  " + dateTxt);
        dateLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#666;");

        VBox carte = new VBox(10);
        carte.setPadding(new Insets(16));
        carte.setMaxWidth(Double.MAX_VALUE);
        carte.setStyle("-fx-background-color:-color-bg-card;-fx-background-radius:14px;"
                + "-fx-border-color:-color-border;-fx-border-radius:14px;");

        carte.getChildren().addAll(sujet, nm, email, description, badges, dateLbl);

        if (r.getPhotoUrl() != null && !r.getPhotoUrl().isBlank()) {
            Label loadLbl = new Label("…");
            loadLbl.setStyle("-fx-font-size:11px;");
            ImageView iv = new ImageView();
            iv.setFitWidth(240);
            iv.setFitHeight(150);
            iv.setPreserveRatio(true);
            iv.setStyle("-fx-cursor:hand;");
            iv.setOnMouseClicked(e -> ouvrirPhotoEnGrand(r.getPhotoUrl()));

            Thread imgTh = new Thread(() -> {
                Image img = new Image(r.getPhotoUrl(), 240, 150, true, true, false);
                Platform.runLater(() -> {
                    if (img.isError())
                        loadLbl.setText(AppTexts.isEnglish() ? "⚠ Image unavailable" : "⚠ Image indisponible");
                    else {
                        iv.setImage(img);
                        loadLbl.setManaged(false);
                        loadLbl.setVisible(false);
                    }
                });
            });
            imgTh.setDaemon(true);
            imgTh.start();
            carte.getChildren().addAll(loadLbl, iv);
        }

        Separator sep = new Separator();
        HBox boutons = new HBox(8);
        boutons.setAlignment(Pos.CENTER_LEFT);

        Button btnMod = styled(AppTexts.t("mes.rec.modify"), "#7c66c4");
        Button btnDel = styled(AppTexts.t("mes.rec.delete"), "#c0392b");
        Button btnRep = styled(AppTexts.t("mes.rec.replies"), "#d4af37");

        if (cloturee) {
            btnMod.setDisable(true);
            btnDel.setDisable(true);
            btnMod.setOpacity(0.5);
            btnDel.setOpacity(0.5);
            Tooltip.install(btnMod, new Tooltip(AppTexts.t("mes.rec.tooltip.closed")));
            Tooltip.install(btnDel, new Tooltip(AppTexts.t("mes.rec.tooltip.closed")));
        } else {
            btnMod.setOnAction(e -> ouvrirModification(r));
            btnDel.setOnAction(e -> confirmerSupprimer(r));
        }
        btnRep.setOnAction(e -> voirReponses(r));

        boutons.getChildren().addAll(btnMod, btnDel, new Region(), btnRep);
        HBox.setHgrow(boutons.getChildren().get(2), Priority.ALWAYS);
        carte.getChildren().addAll(sep, boutons);
        return carte;
    }

    private Button styled(String txt, String bg) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:7 12;-fx-cursor:hand;");
        return b;
    }

    private Label badgeLabel(String text, String inlineStyle) {
        Label l = new Label(text);
        l.setStyle(inlineStyle);
        return l;
    }

    private static String classeStatut(String raw) {
        String s = raw != null ? raw.toLowerCase().trim() : "";
        String bg = switch (s) {
            case "en_attente" -> "#9b72e8";
            case "en_cours" -> "#2980b9";
            case "resolue" -> "#27ae60";
            default -> "#7f8c8d";
        };
        return "-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-font-size:10px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:3 10;";
    }

    private static String classePriorite(String p) {
        if (p == null) return "-fx-background-color:#95a5a6;-fx-text-fill:white;-fx-font-size:10px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:3 10;";
        return switch (p.toLowerCase()) {
            case "haute" -> "-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-font-size:10px;-fx-font-weight:bold;"
                    + "-fx-background-radius:8;-fx-padding:3 10;";
            case "moyenne" -> "-fx-background-color:#e67e22;-fx-text-fill:white;-fx-font-size:10px;-fx-font-weight:bold;"
                    + "-fx-background-radius:8;-fx-padding:3 10;";
            default -> "-fx-background-color:#27ae60;-fx-text-fill:white;-fx-font-size:10px;-fx-font-weight:bold;"
                    + "-fx-background-radius:8;-fx-padding:3 10;";
        };
    }

    private static String formatStatut(String s) {
        if (s == null) return "—";
        return s.trim().toUpperCase().replace(' ', '_');
    }

    @FXML
    private void ouvrirAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/AjouterReclamation.fxml"));
            Parent root = loader.load();
            Stage dlg = new Stage();
            dlg.initModality(Modality.APPLICATION_MODAL);
            if (gridCartes.getScene() != null && gridCartes.getScene().getWindow() != null)
                dlg.initOwner(gridCartes.getScene().getWindow());
            dlg.setTitle(AppTexts.t("rec.dialog.title"));
            dlg.setScene(new Scene(root));
            dlg.centerOnScreen();
            dlg.showAndWait();
            chargerStats();
            appliquerFiltre();
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void ouvrirModification(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/ModifierMaReclamation.fxml"));
            Parent root = loader.load();
            ModifierMaReclamationController ctrl = loader.getController();
            ctrl.setReclamation(r);
            Stage dlg = new Stage();
            dlg.initModality(Modality.APPLICATION_MODAL);
            if (gridCartes.getScene() != null)
                dlg.initOwner(gridCartes.getScene().getWindow());
            dlg.setTitle(AppTexts.t("mes.rec.modify.title"));
            dlg.setScene(new Scene(root, 540, 520));
            dlg.centerOnScreen();
            dlg.showAndWait();
            chargerStats();
            appliquerFiltre();
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void confirmerSupprimer(Reclamation r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        styliserAlerte(confirm);
        confirm.setTitle(AppTexts.t("mes.rec.delete"));
        confirm.setHeaderText(null);
        confirm.setContentText(String.format(AppTexts.t("mes.rec.delete.confirm"),
                r.getSujet() != null ? r.getSujet() : "—"));
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    int uid = utilisateurCourantNonNullSafeId();
                    if (uid <= 0) return;
                    serviceRec.deleteForUser(r.getId(), uid);
                    chargerStats();
                    appliquerFiltre();
                } catch (RuntimeException ex) {
                    alert(Alert.AlertType.ERROR, ex.getMessage());
                }
            }
        });
    }

    private void voirReponses(Reclamation r) {
        List<Reponse> liste = serviceRep.getByReclamationId(r.getId());

        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.setTitle(AppTexts.t("mes.rec.replies.title"));
        al.setHeaderText(r.getSujet());

        VBox vb = new VBox(14);
        vb.setPadding(new Insets(4, 0, 0, 0));
        if (liste.isEmpty()) {
            vb.getChildren().add(new Label(AppTexts.t("mes.rec.replies.empty")));
        } else {
            for (Reponse rep : liste) {
                vb.getChildren().add(creerBlocReponseAvecTraduction(rep));
            }
        }
        ScrollPane sp = new ScrollPane(vb);
        sp.setFitToWidth(true);
        sp.setPrefViewportWidth(540);
        sp.setPrefViewportHeight(440);
        al.getDialogPane().setContent(sp);
        styliserAlerte(al);

        javafx.scene.control.Button okBtn =
                (javafx.scene.control.Button) al.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.setText(AppTexts.t("mes.rec.replies.ok"));
        }
        al.showAndWait();
    }

    /** Même mécanisme MyMemory que l’interface admin ({@link AdminReclamationsOverviewController}). */
    private static final class LangSelMes {
        final String label;
        final String code;

        LangSelMes(String label, String code) {
            this.label = label;
            this.code = code;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /** Affiche les libellés d’admin stockés en base dans la langue actuelle du client. */
    private static String libelleAuteurReponse(String auteurBd) {
        if (auteurBd == null || auteurBd.isBlank()) {
            return AppTexts.t("mes.rec.reply.author");
        }
        String t = auteurBd.trim();
        if (t.equalsIgnoreCase("Admin")
                || t.equalsIgnoreCase("Administrateur SitMyPet")
                || t.equalsIgnoreCase("SitMyPet Administrator")) {
            return AppTexts.t("mes.rec.reply.author");
        }
        return t;
    }

    private VBox creerBlocReponseAvecTraduction(Reponse rep) {
        String when = rep.getDateReponse() != null ? rep.getDateReponse().format(DF) : "";
        Label h = new Label(libelleAuteurReponse(rep.getAuteur()) + " · " + when);
        h.setStyle("-fx-font-weight:bold;");

        TextArea taOriginal = new TextArea(rep.getContenu() != null ? rep.getContenu() : "");
        taOriginal.setEditable(false);
        taOriginal.setWrapText(true);
        taOriginal.setPrefRowCount(Math.min(7, Math.max(2,
                rep.getContenu() != null ? rep.getContenu().split("\n").length + 3 : 3)));

        Label lblTrad = new Label(AppTexts.t("admin.rec.translation.section"));
        lblTrad.setStyle("-fx-font-weight:bold;-fx-text-fill:-color-text-secondary;");

        ComboBox<LangSelMes> comboLang = new ComboBox<>(FXCollections.observableArrayList(
                new LangSelMes(AppTexts.t("admin.rec.translation.lang_en"), "en"),
                new LangSelMes(AppTexts.t("admin.rec.translation.lang_ar"), "ar")
        ));
        comboLang.getSelectionModel().selectFirst();

        Button btnTraduire = new Button(AppTexts.t("admin.rec.translation.translate"));

        TextArea taTraduite = new TextArea();
        taTraduite.setEditable(false);
        taTraduite.setWrapText(true);
        taTraduite.setPrefRowCount(4);
        taTraduite.setPromptText(AppTexts.t("admin.rec.translation.hint"));

        btnTraduire.setOnAction(e -> {
            String texte = taOriginal.getText();
            if (texte == null || texte.isBlank()) return;
            LangSelMes sel = comboLang.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            taTraduite.setText("");
            taTraduite.setPromptText(AppTexts.t("admin.rec.translation.wait"));
            btnTraduire.setDisable(true);

            Thread th = new Thread(() -> {
                try {
                    String out = TraductionService.traduire(texte, sel.code);
                    Platform.runLater(() -> {
                        taTraduite.setText(out);
                        taTraduite.setPromptText(null);
                        btnTraduire.setDisable(false);
                    });
                } catch (RuntimeException ex) {
                    Platform.runLater(() -> {
                        taTraduite.setText("");
                        taTraduite.setPromptText(AppTexts.t("admin.rec.translation.error"));
                        btnTraduire.setDisable(false);
                    });
                }
            }, "mymemory-user-reponse");
            th.setDaemon(true);
            th.start();
        });

        HBox ligneTrad = new HBox(10, comboLang, btnTraduire);
        ligneTrad.setAlignment(Pos.CENTER_LEFT);

        VBox bloc = new VBox(8, h, taOriginal);
        Separator sep = new Separator();
        bloc.getChildren().addAll(sep, lblTrad, ligneTrad, taTraduite);
        bloc.setPadding(new Insets(0, 0, 12, 0));
        return bloc;
    }

    private void ouvrirPhotoEnGrand(String url) {
        try {
            Stage stage = new Stage();
            ImageView iv = new ImageView(new Image(url, 720, 560, true, true, true));
            iv.setPreserveRatio(true);
            stage.setTitle(AppTexts.t("mes.rec.photo.title"));
            stage.setScene(new Scene(new StackPane(iv), 760, 520));
            stage.show();
        } catch (Exception ignored) {
            alert(Alert.AlertType.ERROR, AppTexts.isEnglish() ? "Could not open image." : "Impossible d’ouvrir l’image.");
        }
    }

    private static void styliserAlerte(Alert alert) {
        try {
            var ss = MesReclamationsController.class.getResource("/style.css");
            if (ss != null) alert.getDialogPane().getStylesheets().add(ss.toExternalForm());
        } catch (Exception ignored) {}
    }

    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        styliserAlerte(a);
        a.showAndWait();
    }
}
