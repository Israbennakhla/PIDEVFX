package com.sitmypet.components;

import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import com.sitmypet.utils.ThemeManager;

public class ThemeToggle extends StackPane {

    private boolean isDark;
    private StackPane thumb;
    private SVGPath iconPath;

    // SVG paths pour un rendu parfait (zéro bug d'émoji sur Windows)
    private static final String SUN_SVG = "M12 2.25a.75.75 0 01.75.75v2.25a.75.75 0 01-1.5 0V3a.75.75 0 01.75-.75zM7.5 12a4.5 4.5 0 119 0 4.5 4.5 0 01-9 0zM18.894 6.166a.75.75 0 00-1.06-1.06l-1.591 1.59a.75.75 0 101.06 1.061l1.591-1.59zM21.75 12a.75.75 0 01-.75.75h-2.25a.75.75 0 010-1.5H21a.75.75 0 01.75.75zM17.834 18.894a.75.75 0 001.06-1.06l-1.59-1.591a.75.75 0 10-1.061 1.06l1.59 1.591zM12 18a.75.75 0 01.75.75V21a.75.75 0 01-1.5 0v-2.25A.75.75 0 0112 18zM7.758 17.303a.75.75 0 00-1.061-1.06l-1.591 1.59a.75.75 0 001.06 1.061l1.591-1.59zM6 12a.75.75 0 01-.75.75H3a.75.75 0 010-1.5h2.25A.75.75 0 016 12zM6.697 7.757a.75.75 0 001.06-1.06l-1.59-1.591a.75.75 0 00-1.061 1.06l1.59 1.591z";
    private static final String MOON_SVG = "M21.752 15.002A9.718 9.718 0 0118 15.75c-5.385 0-9.75-4.365-9.75-9.75 0-1.33.266-2.597.748-3.752A9.753 9.753 0 003 11.25C3 16.635 7.365 21 12.75 21a9.753 9.753 0 009.002-5.998z";

    public ThemeToggle() {
        this.isDark = ThemeManager.isDarkMode();
        
        // Taille premium et compacte
        this.setPrefSize(64, 32);
        this.setMaxSize(64, 32);
        this.setMinSize(64, 32);
        
        iconPath = new SVGPath();
        iconPath.setContent(isDark ? MOON_SVG : SUN_SVG);
        
        // Redimensionner légèrement l'icône
        iconPath.setScaleX(0.7);
        iconPath.setScaleY(0.7);

        thumb = new StackPane(iconPath);
        thumb.setPrefSize(26, 26);
        thumb.setMaxSize(26, 26);
        
        updateStyles();

        StackPane.setAlignment(thumb, Pos.CENTER_LEFT);
        if (isDark) {
            thumb.setTranslateX(34); // 64 - 26 - 4
        } else {
            thumb.setTranslateX(4);
        }

        this.getChildren().addAll(thumb);
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
        
        // Animation fluide
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), thumb);
        tt.setToX(isDark ? 34 : 4);
        tt.play();

        iconPath.setContent(isDark ? MOON_SVG : SUN_SVG);
        updateStyles();
        
        if (this.getScene() != null) {
            ThemeManager.setDarkMode(isDark);
            ThemeManager.applyTheme(this.getScene().getRoot());
        }
    }

    private void updateStyles() {
        if (isDark) {
            // Style Nuit
            this.setStyle("-fx-background-color: #1e1e2f; -fx-background-radius: 32; -fx-border-color: #2a2a40; -fx-border-radius: 32; -fx-border-width: 1; -fx-effect: innershadow(gaussian, rgba(0,0,0,0.4), 5, 0, 0, 2);");
            thumb.setStyle("-fx-background-color: #2d1354; -fx-background-radius: 32; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 6, 0, 0, 2);");
            iconPath.setFill(Color.web("#a78bfa")); // Violet doux
        } else {
            // Style Jour
            this.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 32; -fx-border-color: #cbd5e0; -fx-border-radius: 32; -fx-border-width: 1; -fx-effect: innershadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
            thumb.setStyle("-fx-background-color: white; -fx-background-radius: 32; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);");
            iconPath.setFill(Color.web("#f59e0b")); // Jaune ambré
        }
    }
}
