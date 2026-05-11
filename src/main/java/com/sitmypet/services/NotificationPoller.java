package com.sitmypet.services;

import javafx.application.Platform;
import com.sitmypet.model.Notification;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Polling temps réel : vérifie les nouvelles notifications
 * toutes les 3 secondes et appelle le callback si nouvelles notifs.
 *
 * Utilisation :
 *   NotificationPoller poller = new NotificationPoller(userId, notifs -> {
 *       // appelé sur le JavaFX thread avec les nouvelles notifs
 *       afficherNotification(notifs.get(0));
 *   });
 *   poller.start();
 *   // Pour arrêter : poller.stop();
 */
public class NotificationPoller {

    private final int                       userId;
    private final Consumer<List<Notification>> onNouvellesNotifs;
    private final ServiceNotification      service = new ServiceNotification();
    private Timer                          timer;

    public NotificationPoller(int userId, Consumer<List<Notification>> onNouvellesNotifs) {
        this.userId            = userId;
        this.onNouvellesNotifs = onNouvellesNotifs;
    }

    public void start() {
        timer = new Timer(true); // daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<Notification> nonLues = service.getNonLues(userId);
                if (!nonLues.isEmpty()) {
                    Platform.runLater(() -> onNouvellesNotifs.accept(nonLues));
                }
            }
        }, 0, 3000); // toutes les 3 secondes
    }

    public void stop() {
        if (timer != null) { timer.cancel(); timer = null; }
    }
}