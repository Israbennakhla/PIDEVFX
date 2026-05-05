package com.sitmypet.utils;

import javafx.scene.Parent;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String THEME_PREF_KEY = "dark_mode_enabled";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    
    private static boolean isDarkMode = prefs.getBoolean(THEME_PREF_KEY, false);

    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        prefs.putBoolean(THEME_PREF_KEY, isDarkMode);
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void applyTheme(Parent root) {
        if (root == null) return;
        
        if (isDarkMode) {
            if (!root.getStyleClass().contains("dark-theme")) {
                root.getStyleClass().add("dark-theme");
            }
        } else {
            root.getStyleClass().remove("dark-theme");
        }
    }

    public static void toggleTheme(Parent root) {
        setDarkMode(!isDarkMode); // Utilise setDarkMode pour sauvegarder
        applyTheme(root);
    }
}
