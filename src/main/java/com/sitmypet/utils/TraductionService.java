package com.sitmypet.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TraductionService {

    public static String traduire(String texte, String langCible) {
        try {
            String encoded = URLEncoder.encode(texte, "UTF-8");
            String urlStr = "https://api.mymemory.translated.net/get?q="
                    + encoded + "&langpair=fr|" + langCible;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // ✅ Lire en UTF-8 correctement
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            String json = response.toString();

            // ✅ Extraire et décoder les séquences Unicode
            int start = json.indexOf("\"translatedText\":\"") + 18;
            int end   = json.indexOf("\"", start);
            String brut = json.substring(start, end);

            return decodeUnicode(brut);

        } catch (Exception e) {
            throw new RuntimeException("Erreur traduction : " + e.getMessage());
        }
    }

    // ✅ Convertit \u0646\u0623... en vrais caractères arabes
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