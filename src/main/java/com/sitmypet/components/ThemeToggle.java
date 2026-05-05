package com.sitmypet.components;

import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import com.sitmypet.utils.ThemeManager;

public class ThemeToggle extends StackPane {

    private boolean isDark;
    private StackPane thumb;
    private Label lblText;
    private Label lblIcon;

    public ThemeToggle() {
        this.isDark = ThemeManager.isDarkMode();
        
        this.setPrefSize(140, 44);
        this.setMaxSize(140, 44);
        this.setMinSize(140, 44);
        
        lblText = new Label(isDark ? "NIGHTMODE" : "DAYMODE");
        
        lblIcon = new Label(isDark ? "🌙" : "☀️");
        lblIcon.setStyle("-fx-font-size: 18px;");

        thumb = new StackPane(lblIcon);
        thumb.setPrefSize(34, 34);
        thumb.setMaxSize(34, 34);
        
        updateStyles();

        StackPane.setAlignment(thumb, Pos.CENTER_LEFT);
        if (isDark) {
            thumb.setTranslateX(100);
            StackPane.setAlignment(lblText, Pos.CENTER_LEFT);
            lblText.setTranslateX(15);
        } else {
            thumb.setTranslateX(5);
            StackPane.setAlignment(lblText, Pos.CENTER_RIGHT);
            lblText.setTranslateX(-15);
        }

        this.getChildren().addAll(lblText, thumb);
        this.setCursor(javafx.scene.Cursor.HAND);

        this.setOnMouseClicked(e -> toggle());
        
        // Auto-apply theme on new scene
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.rootProperty().addListener((obsRoot, oldRoot, newRoot) -> {
                    if (newRoot != null) {
                        ThemeManager.applyTheme(newRoot);
                    }
                });
                if (newScene.getRoot() != null) {
                    ThemeManager.applyTheme(newScene.getRoot());
                }
            }
        });
    }

    private void toggle() {
        isDark = !isDark;
        
        // Animation
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), thumb);
        tt.setToX(isDark ? 100 : 5);
        tt.play();

        // Update Text
        lblText.setText(isDark ? "NIGHTMODE" : "DAYMODE");
        lblIcon.setText(isDark ? "🌙" : "☀️");
        
        if (isDark) {
            StackPane.setAlignment(lblText, Pos.CENTER_LEFT);
            lblText.setTranslateX(15);
        } else {
            StackPane.setAlignment(lblText, Pos.CENTER_RIGHT);
            lblText.setTranslateX(-15);
        }

        updateStyles();
        
        if (this.getScene() != null) {
            ThemeManager.setDarkMode(isDark);
            ThemeManager.applyTheme(this.getScene().getRoot());
        }
    }

    private void updateStyles() {
        if (isDark) {
            this.setStyle("-fx-background-color: #2d3748; -fx-background-radius: 40; -fx-border-color: #1a202c; -fx-border-radius: 40; -fx-border-width: 2;");
            thumb.setStyle("-fx-background-color: white; -fx-background-radius: 40; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);");
            lblText.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 12px; -fx-font-family: 'Segoe UI', sans-serif;");
        } else {
            this.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 40; -fx-border-color: #e2e8f0; -fx-border-radius: 40; -fx-border-width: 2;");
            thumb.setStyle("-fx-background-color: white; -fx-background-radius: 40; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);");
            lblText.setStyle("-fx-text-fill: #1a202c; -fx-font-weight: 900; -fx-font-size: 12px; -fx-font-family: 'Segoe UI', sans-serif;");
        }
    }
}
