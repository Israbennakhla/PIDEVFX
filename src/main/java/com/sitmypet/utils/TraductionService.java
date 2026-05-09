package com.sitmypet.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Traduction en ligne via MyMemory (même logique que PIDEVFX-ines-desktop).
 * Source suppose le texte en français ({@code langpair=fr|…}).
 */
public final class TraductionService {

    private TraductionService() {}

    public static String traduire(String texte, String langCible) {
        try {
            String src = texte != null ? texte : "";
            String encoded = URLEncoder.encode(src, StandardCharsets.UTF_8);
            String urlStr = "https://api.mymemory.translated.net/get?q="
                    + encoded + "&langpair=fr|" + langCible;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                String json = response.toString();
                int start = json.indexOf("\"translatedText\":\"") + 18;
                int end = json.indexOf('"', start);
                if (start < 18 || end <= start) throw new IllegalStateException("JSON");
                String brut = json.substring(start, end);
                return decodeUnicode(brut);
            }
        } catch (Exception e) {
            throw new RuntimeException("Traduction : " + e.getMessage(), e);
        }
    }

    private static String decodeUnicode(String texte) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < texte.length()) {
            if (texte.charAt(i) == '\\' && i + 5 < texte.length()
                    && texte.charAt(i + 1) == 'u') {
                String hex = texte.substring(i + 2, i + 6);
                sb.append((char) Integer.parseInt(hex, 16));
                i += 6;
            } else {
                sb.append(texte.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
}
