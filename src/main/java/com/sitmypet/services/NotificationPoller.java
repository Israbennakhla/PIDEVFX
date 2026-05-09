package com.sitmypet.services;

import com.sitmypet.model.Notification;
import javafx.application.Platform;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class NotificationPoller {

    private final int userId;
    private final Consumer<List<Notification>> onNouvellesNotifs;
    private final ServiceNotification service = new ServiceNotification();
    private Timer timer;

    public NotificationPoller(int userId, Consumer<List<Notification>> onNouvellesNotifs) {
        this.userId = userId;
        this.onNouvellesNotifs = onNouvellesNotifs;
    }

    public void start() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<Notification> nonLues = service.getNonLues(userId);
                if (!nonLues.isEmpty()) {
                    Platform.runLater(() -> onNouvellesNotifs.accept(nonLues));
                }
            }
        }, 0, 3000);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
