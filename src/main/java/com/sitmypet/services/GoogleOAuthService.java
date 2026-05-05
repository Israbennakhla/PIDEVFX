package com.sitmypet.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GoogleOAuthService {

    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static final String REDIRECT_URI = "http://localhost:8085/Callback";

    static {
        java.util.Properties props = new java.util.Properties();
        try (java.io.InputStream is = new java.io.FileInputStream("config.properties")) {
            props.load(is);
            CLIENT_ID = props.getProperty("GOOGLE_OAUTH_CLIENT_ID");
            CLIENT_SECRET = props.getProperty("GOOGLE_OAUTH_CLIENT_SECRET");
        } catch (Exception e) {
            System.err.println("Erreur chargement config.properties : " + e.getMessage());
        }
    }
    
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private static HttpServer server;

    public interface GoogleAuthCallback {
        void onSuccess(GoogleUser user);
        void onError(String error);
    }

    public static class GoogleUser {
        public String email;
        public String givenName; // Prénom
        public String familyName; // Nom
        public String picture; // Photo URL
    }

    public void authenticate(GoogleAuthCallback callback) {
        try {
            startLocalServer(callback);
            openBrowserForAuth();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> callback.onError("Erreur lors de l'initialisation de Google Auth : " + e.getMessage()));
        }
    }

    private void startLocalServer(GoogleAuthCallback callback) throws IOException {
        // Arrêter le serveur précédent s'il existe
        if (server != null) {
            server.stop(0);
        }

        server = HttpServer.create(new InetSocketAddress(8085), 0);
        server.createContext("/Callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            
            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1].split("&")[0];
                
                // Réponse HTTP pour le navigateur
                String response = "<html><body><h2>Authentification reussie !</h2><p>Vous pouvez fermer cette fenetre et retourner a SitMyPet.</p></body></html>";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();

                // Processer le token dans un thread séparé pour ne pas bloquer le serveur
                new Thread(() -> processAuthCode(code, callback)).start();
            } else {
                String response = "Erreur d'authentification ou requete annulee.";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
                Platform.runLater(() -> callback.onError("Authentification annulée."));
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("✅ Serveur local démarré sur le port 8085 en attente du callback Google...");
    }

    private void openBrowserForAuth() throws Exception {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            String url = AUTH_URL + "?" +
                    "client_id=" + CLIENT_ID +
                    "&redirect_uri=" + REDIRECT_URI +
                    "&response_type=code" +
                    "&scope=email%20profile";
            Desktop.getDesktop().browse(new URI(url));
        } else {
            throw new Exception("L'ouverture automatique du navigateur n'est pas supportée par votre système.");
        }
    }

    private void processAuthCode(String code, GoogleAuthCallback callback) {
        try {
            String accessToken = exchangeCodeForToken(code);
            if (accessToken != null) {
                GoogleUser user = fetchUserInfo(accessToken);
                if (user != null) {
                    Platform.runLater(() -> {
                        callback.onSuccess(user);
                    });
                    if (server != null) {
                        new Thread(() -> server.stop(0)).start();
                    }
                } else {
                    Platform.runLater(() -> callback.onError("Impossible de récupérer les informations de l'utilisateur Google."));
                }
            } else {
                Platform.runLater(() -> callback.onError("Impossible d'échanger le code contre un token. Vérifiez le Client ID et Secret."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> callback.onError("Erreur réseau : " + e.getMessage()));
        }
    }

    private String exchangeCodeForToken(String code) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String requestBody = "code=" + java.net.URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&client_id=" + java.net.URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                "&client_secret=" + java.net.URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8) +
                "&redirect_uri=" + java.net.URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                "&grant_type=authorization_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            return jsonObject.get("access_token").getAsString();
        } else {
            System.err.println("Erreur Token : " + response.body());
            return null;
        }
    }

    private GoogleUser fetchUserInfo(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USER_INFO_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            GoogleUser user = new GoogleUser();
            user.email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : "";
            user.givenName = jsonObject.has("given_name") ? jsonObject.get("given_name").getAsString() : "Google";
            user.familyName = jsonObject.has("family_name") ? jsonObject.get("family_name").getAsString() : "User";
            user.picture = jsonObject.has("picture") ? jsonObject.get("picture").getAsString() : "default.png";
            return user;
        } else {
            System.err.println("Erreur UserInfo : " + response.body());
            return null;
        }
    }
}
