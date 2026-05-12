package com.sitmypet.controllers;

import com.sitmypet.SessionContext;
import com.sitmypet.i18n.AppTexts;
import com.sitmypet.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class FrontAccueilController {

    @FXML private javafx.scene.layout.HBox boxProprietaire;
    @FXML private javafx.scene.layout.HBox boxGardien;
    @FXML private javafx.scene.layout.HBox boxReclamationNav;

    @FXML private BorderPane mainContainer;
    @FXML private ScrollPane accueilScroll;
    @FXML private VBox accueilContent;

    @FXML private VBox gardienFeedSection;
    @FXML private StackPane announcementFeedHost;

    @FXML private Button btnAccueil;
    @FXML private Button btnMesAnimaux;
    @FXML private Button btnMesAnnonces;
    @FXML private Button btnMesPostulations;
    @FXML private Button btnReclamation;
    @FXML private Button btnMessagerie;
    @FXML private Button btnArticles;
    @FXML private Button btnDeconnexion;
    @FXML private Button btnLangFr;
    @FXML private Button btnLangEn;

    @FXML private Label lblWelcomeTitle;
    @FXML private Label lblRoleDisplay;

    private User currentUser;

    private static final String STYLE_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #4a5568; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 5 10;";
    private static final String STYLE_ACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #8e5bd6; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: transparent transparent #8e5bd6 transparent; -fx-border-width: 0 0 3 0; -fx-padding: 5 10;";

    @FXML
    public void initialize() {
        if (accueilScroll != null && accueilContent != null) {
            accueilContent.prefWidthProperty().bind(accueilScroll.widthProperty());
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            SessionContext.setCurrentUser(user);
        }

        if (user != null) {
            String role = user.getRole() != null ? user.getRole().toUpperCase() : "";

            lblWelcomeTitle.setText("Bienvenue, " + user.getPrenom() + " !");

            String displayRole = role.replace("ROLE_", "");
            lblRoleDisplay.setText(displayRole);

            boxProprietaire.setVisible(false);
            boxProprietaire.setManaged(false);
            boxGardien.setVisible(false);
            boxGardien.setManaged(false);

            if (role.contains("PROPRIETAIRE") || role.contains("PROPRIÉTAIRE")) {
                boxProprietaire.setVisible(true);
                boxProprietaire.setManaged(true);
                if (gardienFeedSection != null) {
                    gardienFeedSection.setVisible(false);
                    gardienFeedSection.setManaged(false);
                }
            } else if (role.contains("GARDIEN")) {
                boxGardien.setVisible(true);
                boxGardien.setManaged(true);
                if (gardienFeedSection != null) {
                    gardienFeedSection.setVisible(true);
                    gardienFeedSection.setManaged(true);
                    embedGardienAnnouncementFeed();
                }
            }

            if (boxReclamationNav != null) {
                boolean showRec = rolePeutReclamation(user);
                boxReclamationNav.setVisible(showRec);
                boxReclamationNav.setManaged(showRec);
            }
        } else if (boxReclamationNav != null) {
            boxReclamationNav.setVisible(false);
            boxReclamationNav.setManaged(false);
        }

        refreshNavLanguage();

        SessionContext.setOpenMessageriePanelAction(() ->
                Platform.runLater(this::showMessagerieInternal));
    }

    private void refreshNavLanguage() {
        if (btnAccueil != null) btnAccueil.setText(AppTexts.t("nav.accueil"));
        if (btnMesAnimaux != null) btnMesAnimaux.setText(AppTexts.t("nav.animaux"));
        if (btnMesAnnonces != null) btnMesAnnonces.setText(AppTexts.t("nav.annonces"));
        if (btnMesPostulations != null) btnMesPostulations.setText(AppTexts.t("nav.postulations"));
        if (btnReclamation != null) btnReclamation.setText(AppTexts.t("nav.reclamation"));
        if (btnMessagerie != null) btnMessagerie.setText(AppTexts.t("nav.messagerie"));
        if (btnArticles != null) btnArticles.setText(AppTexts.t("nav.blog"));
        if (btnDeconnexion != null) btnDeconnexion.setText(AppTexts.t("nav.deconnexion"));
        if (btnLangFr != null) btnLangFr.setText(AppTexts.t("lang.fr"));
        if (btnLangEn != null) btnLangEn.setText(AppTexts.t("lang.en"));

        if (currentUser != null) {
            lblWelcomeTitle.setText(
                    (AppTexts.isEnglish() ? "Welcome, " : "Bienvenue, ") + currentUser.getPrenom() + " !");
        }
        if (isGardienRole()) {
            embedGardienAnnouncementFeed();
        }
    }

    @FXML
    private void handleLangFr(ActionEvent event) {
        AppTexts.setFrench();
        refreshNavLanguage();
        updateNavStyles(btnAccueil);
    }

    @FXML
    private void handleLangEn(ActionEvent event) {
        AppTexts.setEnglish();
        refreshNavLanguage();
        updateNavStyles(btnAccueil);
    }

    @FXML
    private void handleReclamation(ActionEvent event) {
        if (!rolePeutReclamation(currentUser)) return;
        loadCenter("/com/sitmypet/fxml/MesReclamations.fxml", btnReclamation);
        if (isGardienRole()) {
            embedGardienAnnouncementFeed();
        }
    }

    private boolean isGardienRole() {
        if (currentUser == null || currentUser.getRole() == null) return false;
        return currentUser.getRole().toUpperCase().contains("GARDIEN");
    }

    /** Propriétaire ou gardien peut ouvrir le formulaire réclamation. */
    private static boolean rolePeutReclamation(User u) {
        if (u == null || u.getRole() == null) return false;
        String r = u.getRole().toUpperCase();
        return r.contains("GARDIEN")
                || r.contains("PROPRIETAIRE")
                || r.contains("PROPRIÉTAIRE");
    }

    /** Accueil gardien : annonces ouvertes (postuler), pas uniquement celles du compte. */
    private void embedGardienAnnouncementFeed() {
        if (announcementFeedHost == null) return;
        try {
            AnnouncementFeedController.marketBrowseMode = true;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/AnnouncementFeed.fxml"));
            Node root = loader.load();
            StackPane.setAlignment(root, Pos.TOP_CENTER);
            VBox.setVgrow(root, Priority.ALWAYS);
            announcementFeedHost.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCenter(String fxmlPath, Button activeBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            mainContainer.setCenter(node);
            updateNavStyles(activeBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMessagerieInternal() {
        loadCenter("/com/sitmypet/fxml/Messagerie.fxml", btnMessagerie);
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        SessionContext.setCurrentUser(null);
        SessionContext.setOpenMessageriePanelAction(null);
        SessionContext.setPrimaryStage(null);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("SitMyPet - Connexion");
            stage.setScene(new Scene(root, 900, 600));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowAccueil(ActionEvent event) {
        if (mainContainer != null && accueilContent != null) {
            mainContainer.setCenter(accueilScroll != null ? accueilScroll : accueilContent);
            updateNavStyles(btnAccueil);
            if (isGardienRole()) {
                embedGardienAnnouncementFeed();
            }
        }
    }

    @FXML
    private void handleShowProfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/UserProfile.fxml"));
            Node profileView = loader.load();

            Object controller = loader.getController();
            if (controller instanceof com.sitmypet.controllers.UserProfileController) {
                ((com.sitmypet.controllers.UserProfileController) controller).setUser(currentUser, mainContainer);
            }

            mainContainer.setCenter(profileView);
            updateNavStyles(null);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du profil: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowProfilClick(javafx.scene.input.MouseEvent event) {
        handleShowProfil(null);
    }

    @FXML
    private void handleMesAnimaux(ActionEvent event) {
        loadCenter("/com/sitmypet/fxml/ListeAnimaux.fxml", btnMesAnimaux);
    }

    @FXML
    private void handleMesAnnonces(ActionEvent event) {
        loadCenter("/com/sitmypet/fxml/MesAnnonces.fxml", btnMesAnnonces);
    }

    @FXML
    private void handleMesPostulations(ActionEvent event) {
        loadCenter("/com/sitmypet/fxml/PostulationsGardien.fxml", btnMesPostulations);
    }

    @FXML
    private void handleMessagerie(ActionEvent event) {
        showMessagerieInternal();
    }

    @FXML
    private void handleBlog(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Blog");
        a.setHeaderText(null);
        a.setContentText("La section Blog arrive bientôt.");
        a.showAndWait();
        updateNavStyles(btnArticles);
    }

    private void updateNavStyles(Button activeBtn) {
        Button[] navButtons = {
                btnAccueil, btnMesAnimaux, btnMesAnnonces,
                btnMesPostulations, btnReclamation, btnMessagerie, btnArticles
        };
        for (Button b : navButtons) {
            if (b != null) {
                b.setStyle(b == activeBtn ? STYLE_ACTIVE : STYLE_INACTIVE);
            }
        }
    }
}
