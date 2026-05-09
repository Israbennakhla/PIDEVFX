package com.sitmypet;

import com.sitmypet.model.User;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Utilisateur connecté et fenêtre principale pour les écrans intégrés (PIDEVFX-nourb → SitMyPet).
 */
public final class SessionContext {

    private static User currentUser;
    private static Stage primaryStage;
    private static Runnable openMessageriePanelAction;

    private SessionContext() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /** Depuis la cloche (ex. ouvrir le chat). */
    public static void setOpenMessageriePanelAction(Runnable action) {
        openMessageriePanelAction = action;
    }

    public static void triggerOpenMessageriePanel() {
        if (openMessageriePanelAction != null) {
            Platform.runLater(openMessageriePanelAction);
        }
    }
}
