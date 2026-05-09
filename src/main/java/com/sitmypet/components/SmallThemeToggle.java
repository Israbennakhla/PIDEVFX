package com.sitmypet.components;

import javafx.scene.control.Button;
import com.sitmypet.utils.ThemeManager;

public class SmallThemeToggle extends Button {
    
    public SmallThemeToggle() {
        super();
        updateUI();
        
        this.setOnAction(e -> {
            ThemeManager.setDarkMode(!ThemeManager.isDarkMode());
            updateUI();
            if (this.getScene() != null && this.getScene().getRoot() != null) {
                ThemeManager.applyTheme(this.getScene().getRoot());
            }
        });
        
        // S'assurer que le thème global s'applique à la nouvelle scène quand le composant y est ajouté
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
    
    private void updateUI() {
        boolean isDark = ThemeManager.isDarkMode();
        if (isDark) {
            this.setText("☀️");
            this.setStyle("-fx-background-radius: 50; -fx-cursor: hand; -fx-padding: 8 12; -fx-font-size: 16px; -fx-background-color: #374151; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        } else {
            this.setText("🌙");
            this.setStyle("-fx-background-radius: 50; -fx-cursor: hand; -fx-padding: 8 12; -fx-font-size: 16px; -fx-background-color: #edf2f7; -fx-text-fill: #2d3748; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        }
    }
}
