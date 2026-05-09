package com.sitmypet.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class AppTexts {

    private static Locale locale = Locale.FRENCH;

    private AppTexts() {}

    public static Locale getLocale() {
        return locale;
    }

    /** {@code true} lorsque la langue active est l’anglais (nav principale FR/EN). */
    public static boolean isEnglish() {
        return locale.getLanguage().startsWith("en");
    }

    public static void setFrench() {
        locale = Locale.FRENCH;
    }

    public static void setEnglish() {
        locale = Locale.ENGLISH;
    }

    public static ResourceBundle bundle() {
        return ResourceBundle.getBundle("com.sitmypet.i18n.messages", locale);
    }

    public static String t(String key) {
        try {
            return bundle().getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
