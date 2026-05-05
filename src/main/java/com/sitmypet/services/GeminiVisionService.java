package com.sitmypet.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;

public class GeminiVisionService {

    // IMPORTANT: Remplacez cette clé par votre propre clé API Google Gemini
    private static final String API_KEY = "YOUR_GEMINI_API_KEY";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public static class VisionResult {
        public boolean isValid;
        public int score;
        public String reason;
        public String emailContent;
    }

    public VisionResult analyserCertificat(File imageFile, String nom, String prenom) {
        VisionResult result = new VisionResult();
        result.isValid = false;
        result.score = 0;
        result.reason = "Erreur de communication avec l'IA.";
        result.emailContent = "<p>Erreur interne lors de la validation du certificat.</p>";

        // Sécurité si la clé n'est pas configurée
        if (API_KEY.equals("YOUR_GEMINI_API_KEY")) {
             result.reason = "⚠️ Veuillez configurer votre clé API Gemini dans GeminiVisionService.java pour activer l'IA.";
             return result;
        }

        try {
            // Lecture et encodage de l'image en Base64
            byte[] fileContent = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(fileContent);

            String mimeType = "image/jpeg";
            if (imageFile.getName().toLowerCase().endsWith(".png")) {
                mimeType = "image/png";
            } else if (imageFile.getName().toLowerCase().endsWith(".webp")) {
                mimeType = "image/webp";
            }

            // Construction du prompt pour l'IA
            String prompt = String.format("Tu es un expert strict en validation de documents officiels pour une plateforme de pet-sitting nommée SitMyPet. " +
                "Voici un document uploadé par l'utilisateur affirmant s'appeler '%s %s'. " +
                "Analyse ce document avec précision. Vérifie s'il s'agit bien d'un certificat, diplôme ou attestation lié à la garde d'animaux, au domaine vétérinaire ou à l'éducation canine. " +
                "Vérifie la correspondance du nom et prénom avec le document. " +
                "Retourne UNIQUEMENT un objet JSON valide (sans aucun bloc de code markdown autour) avec ces 4 champs exacts : " +
                "1) 'isValid' : true si le document semble valide et correspond au nom, false sinon. " +
                "2) 'score' : une note de crédibilité de 0 à 10 sous forme d'entier (0 si illisible/faux, 10 si parfait et officiel). " +
                "3) 'reason' : une courte phrase en français expliquant précisément pourquoi tu as accepté ou refusé le document. " +
                "4) 'emailContent' : Rédige le corps complet d'un email en code HTML (sans balise html ou body, juste div, p, ul, strong, etc.). " +
                "Si 'isValid' est true, rédige un rapport complet d'analyse (points forts du diplôme) et ajoute des conseils professionnels pour être un excellent gardien. " +
                "Si 'isValid' est false, rédige un rapport expliquant le refus et liste les raisons exactes pour lesquelles le document est invalide.",
                nom, prenom);

            // Construction de la requête JSON pour l'API Gemini
            JsonObject requestJson = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject contentObj = new JsonObject();
            JsonArray parts = new JsonArray();

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", prompt);
            parts.add(textPart);

            JsonObject inlineDataPart = new JsonObject();
            JsonObject inlineDataObj = new JsonObject();
            inlineDataObj.addProperty("mimeType", mimeType);
            inlineDataObj.addProperty("data", base64Image);
            inlineDataPart.add("inlineData", inlineDataObj);
            parts.add(inlineDataPart);

            contentObj.add("parts", parts);
            contents.add(contentObj);
            requestJson.add("contents", contents);

            // Appel HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Parsing de la réponse de l'API
            Gson gson = new Gson();
            JsonObject responseObj = gson.fromJson(response.body(), JsonObject.class);

            if (responseObj != null && responseObj.has("candidates")) {
                String aiText = responseObj.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
                
                // Nettoyage des balises markdown si l'IA en a ajouté par erreur
                aiText = aiText.replaceAll("```json", "").replaceAll("```", "").trim();

                // Parsing du JSON renvoyé par l'IA
                VisionResult parsedResult = gson.fromJson(aiText, VisionResult.class);
                if (parsedResult != null) {
                    return parsedResult;
                }
            } else {
                System.err.println("Réponse Gemini inattendue: " + response.body());
                result.reason = "Erreur API : " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.reason = "Exception technique lors de l'analyse : " + e.getMessage();
        }
        
        return result;
    }
}
