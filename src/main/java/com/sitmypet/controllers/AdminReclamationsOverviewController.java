package com.sitmypet.controllers;

import com.sitmypet.i18n.AppTexts;
import com.sitmypet.model.Reclamation;
import com.sitmypet.model.Reponse;
import com.sitmypet.services.ServiceReclamation;
import com.sitmypet.services.ServiceReponse;
import com.sitmypet.utils.TraductionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AdminReclamationsOverviewController {

    @FXML private Label lblPageTitle;
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
    @FXML private VBox gridCartes;

    private final ServiceReclamation serviceRec = new ServiceReclamation();
    private final ServiceReponse serviceRep = new ServiceReponse();

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        filtreStatut.setItems(FXCollections.observableArrayList("Tous", "en_attente", "en_cours", "resolue"));
        filtreStatut.setValue("Tous");
        filtrePriorite.setItems(FXCollections.observableArrayList("Tous", "basse", "moyenne", "haute"));
        filtrePriorite.setValue("Tous");

        lblFiltStatut.setText(AppTexts.isEnglish() ? "Status" : "Statut");
        lblFiltPriorite.setText(AppTexts.isEnglish() ? "Priority" : "Priorité");
        rechercheNom.setPromptText(AppTexts.t("admin.rec.search"));
        btnFiltrer.setText(AppTexts.isEnglish() ? "Apply" : "Filtrer");
        btnReset.setText(AppTexts.isEnglish() ? "Reset" : "Réinitialiser");

        lblPageTitle.setText(AppTexts.t("admin.rec.title"));
        lblStatWaitLbl.setText(AppTexts.t("admin.rec.stats.wait"));
        lblStatCoursLbl.setText(AppTexts.t("admin.rec.stats.progress"));
        lblStatDoneLbl.setText(AppTexts.t("admin.rec.stats.done"));

        rechercheNom.textProperty().addListener((o, a, b) -> appliquerFiltre());

        reloadFromDb();
    }

    public void reloadFromDb() {
        chargerStats();
        appliquerFiltre();
    }

    private void chargerStats() {
        Map<String, Integer> m = serviceRec.countByStatut();
        lblStatWait.setText(String.valueOf(m.getOrDefault("en_attente", 0)));
        lblStatCours.setText(String.valueOf(m.getOrDefault("en_cours", 0)));
        lblStatDone.setText(String.valueOf(m.getOrDefault("resolue", 0)));
    }

    @FXML
    private void appliquerFiltre() {
        String nom = rechercheNom.getText() != null ? rechercheNom.getText().trim() : "";
        List<Reclamation> list = serviceRec.filter(nom, filtreStatut.getValue(), filtrePriorite.getValue());

        gridCartes.getChildren().clear();
        if (list.isEmpty()) {
            Label vide = new Label(AppTexts.isEnglish() ? "No complaints found." : "Aucune réclamation trouvée.");
            vide.setStyle("-fx-text-fill:#888;-fx-font-size:15px;");
            gridCartes.getChildren().add(vide);
            return;
        }

        for (Reclamation r : list) {
            gridCartes.getChildren().add(creerCarte(r));
        }
        Platform.runLater(() -> scroll.setVvalue(0));
    }

    @FXML
    private void reinitialiserFiltre() {
        rechercheNom.clear();
        filtreStatut.setValue("Tous");
        filtrePriorite.setValue("Tous");
        appliquerFiltre();
    }

    private VBox creerCarte(Reclamation r) {
        boolean cloturee = serviceRep.existsForReclamation(r.getId());

        Label sujet = new Label(r.getSujet() != null ? r.getSujet() : "—");
        sujet.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:-color-text-main;");
        sujet.setWrapText(true);

        Label deposantLbl = new Label("👤  " + (r.getNomClient() != null ? r.getNomClient() : ""));
        deposantLbl.setStyle("-fx-text-fill:-color-text-secondary;");

        Label email = new Label("✉️  " + (r.getEmailClient() != null ? r.getEmailClient() : ""));
        email.setStyle("-fx-text-fill:-color-text-secondary;");
        email.setWrapText(true);

        String desc = r.getDescription() != null ? r.getDescription() : "";
        if (desc.length() > 180) desc = desc.substring(0, 177) + "…";
        Label description = new Label(desc);
        description.setWrapText(true);
        description.setStyle("-fx-font-size:12px;-fx-text-fill:#888888;");
        description.setMaxWidth(900);

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        Label badgeStatut = badgeLabel(formatStatut(r.getStatut()), classeStatut(r.getStatut()));
        Label badgePriorite = badgeLabel(r.getPriorite() != null ? r.getPriorite().toUpperCase() : "?",
                classePriorite(r.getPriorite()));
        badges.getChildren().addAll(badgeStatut, badgePriorite);

        String dateTxt = r.getDateReclamation() != null ? r.getDateReclamation().format(DF) : "—";
        Label dateLbl = new Label("📅  " + dateTxt);
        dateLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#666;");

        VBox carte = new VBox(10);
        carte.setPadding(new Insets(16, 18, 18, 18));
        carte.setMaxWidth(Double.MAX_VALUE);
        carte.setStyle("-fx-background-color:-color-bg-card;-fx-background-radius:16px;"
                + "-fx-border-color:-color-border;-fx-border-radius:16px;");
        carte.getChildren().addAll(sujet, deposantLbl, email, description, badges, dateLbl);

        if (r.getPhotoUrl() != null && !r.getPhotoUrl().isBlank()) {
            Label loadLbl = new Label("…");
            loadLbl.setStyle("-fx-font-size:11px;");
            ImageView iv = new ImageView();
            iv.setFitWidth(260);
            iv.setFitHeight(170);
            iv.setPreserveRatio(true);
            Thread imgTh = new Thread(() -> {
                Image img = new Image(r.getPhotoUrl(), 260, 170, true, true, false);
                Platform.runLater(() -> {
                    if (img.isError()) loadLbl.setText("⚠️ " + (AppTexts.isEnglish() ? "Image unavailable" : "Image indisponible"));
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
        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER_LEFT);

        Button btnRep = styledBtn("💬 " + (AppTexts.isEnglish() ? "View replies" : "Voir réponses"), "#6e48c9");
        btnRep.setOnAction(e -> ouvrirReponses(r));

        Button btnReply = styledBtn(AppTexts.t("admin.rec.btn.reply"), "#8e5bd6");
        if (cloturee) {
            btnReply.setDisable(true);
            btnReply.setStyle("-fx-background-color:#3a3350;-fx-text-fill:#8877aa;-fx-background-radius:8;-fx-padding:8 16;");
            Tooltip.install(btnReply, new Tooltip(AppTexts.t("admin.rec.closed")));
        } else {
            btnReply.setOnAction(e -> ouvrirDialogReponse(r));
        }

        Button btnDel = styledBtn(AppTexts.isEnglish() ? "Delete" : "Supprimer", "#c0392b");
        btnDel.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            styliserAlerte(confirm);
            confirm.setContentText(AppTexts.isEnglish()
                    ? "Delete this complaint and its replies permanently?"
                    : "Supprimer cette réclamation et ses réponses ?");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == javafx.scene.control.ButtonType.OK) {
                    serviceRec.delete(r);
                    reloadFromDb();
                }
            });
        });

        boutons.getChildren().addAll(btnRep, btnReply, new Region(), btnDel);
        HBox.setHgrow(boutons.getChildren().get(2), Priority.ALWAYS);
        carte.getChildren().addAll(sep, boutons);
        return carte;
    }

    private Button styledBtn(String txt, String color) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 14;-fx-cursor:hand;");
        return b;
    }

    private Label badgeLabel(String text, String styleClass) {
        Label l = new Label(text);
        l.setStyle(styleClass);
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
        return switch (s.toLowerCase()) {
            case "en_attente" -> "En attente";
            case "en_cours" -> "En cours";
            case "resolue" -> "Résolue";
            default -> s;
        };
    }

    private void ouvrirReponses(Reclamation r) {
        List<Reponse> liste = serviceRep.getByReclamationId(r.getId());

        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.setTitle(AppTexts.isEnglish() ? "Replies" : "Réponses");
        al.setHeaderText(r.getSujet());

        VBox vb = new VBox(14);
        vb.setPadding(new Insets(4, 0, 0, 0));
        if (liste.isEmpty()) {
            vb.getChildren().add(new Label(AppTexts.isEnglish() ? "No replies yet." : "Aucune réponse pour l’instant."));
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
        al.showAndWait();
    }

    private static final class LangSel {
        final String label;
        final String code;

        LangSel(String label, String code) {
            this.label = label;
            this.code = code;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private VBox creerBlocReponseAvecTraduction(Reponse rep) {
        String when = rep.getDateReponse() != null ? rep.getDateReponse().format(DF) : "";
        Label h = new Label((rep.getAuteur() != null ? rep.getAuteur() : "Admin") + " · " + when);
        h.setStyle("-fx-font-weight:bold;");

        TextArea taOriginal = new TextArea(rep.getContenu() != null ? rep.getContenu() : "");
        taOriginal.setEditable(false);
        taOriginal.setWrapText(true);
        taOriginal.setPrefRowCount(Math.min(7, Math.max(2,
                rep.getContenu() != null ? rep.getContenu().split("\n").length + 3 : 3)));

        Label lblTrad = new Label(AppTexts.t("admin.rec.translation.section"));
        lblTrad.setStyle("-fx-font-weight:bold;-fx-text-fill:-color-text-secondary;");

        ComboBox<LangSel> comboLang = new ComboBox<>(FXCollections.observableArrayList(
                new LangSel(AppTexts.t("admin.rec.translation.lang_en"), "en"),
                new LangSel(AppTexts.t("admin.rec.translation.lang_ar"), "ar")
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
            LangSel sel = comboLang.getSelectionModel().getSelectedItem();
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
            }, "mymemory-reponse");
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

    private void ouvrirDialogReponse(Reclamation r) {
        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dlg = new javafx.scene.control.Dialog<>();
        dlg.setTitle(AppTexts.t("admin.rec.btn.reply"));
        dlg.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        dlg.setHeaderText(r.getSujet());

        TextArea ta = new TextArea();
        ta.setPromptText(AppTexts.t("admin.rec.reply.prompt"));
        ta.setWrapText(true);
        ta.setPrefRowCount(8);
        ta.setPrefWidth(480);
        VBox rootBox = new VBox(12, ta);
        rootBox.setPadding(new Insets(10));
        dlg.getDialogPane().setContent(rootBox);

        DialogPane pane = dlg.getDialogPane();
        try {
            var ss = getClass().getResource("/style.css");
            if (ss != null) pane.getStylesheets().add(ss.toExternalForm());
        } catch (Exception ignored) {}

        dlg.showAndWait().ifPresent(bt -> {
            if (bt == javafx.scene.control.ButtonType.OK && ta.getText() != null && !ta.getText().isBlank()) {
                Reponse rep = new Reponse();
                rep.setContenu(ta.getText().trim());
                rep.setDateReponse(java.time.LocalDateTime.now());
                rep.setAuteur(AppTexts.isEnglish() ? "SitMyPet Administrator" : "Administrateur SitMyPet");
                rep.setReclamationId(r.getId());
                try {
                    serviceRep.add(rep);
                    Alert ok = new Alert(Alert.AlertType.INFORMATION, AppTexts.t("admin.rec.reply.success"));
                    styliserAlerte(ok);
                    ok.showAndWait();
                    reloadFromDb();
                } catch (RuntimeException ex) {
                    Alert err = new Alert(Alert.AlertType.ERROR, ex.getMessage());
                    styliserAlerte(err);
                    err.showAndWait();
                }
            }
        });
    }

    private void styliserAlerte(Alert alert) {
        try {
            var ss = getClass().getResource("/style.css");
            if (ss != null) alert.getDialogPane().getStylesheets().add(ss.toExternalForm());
        } catch (Exception ignored) {}
    }
}
