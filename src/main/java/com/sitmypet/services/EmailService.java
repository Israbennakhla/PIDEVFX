package com.sitmypet.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Properties;

import com.sitmypet.model.User;
import com.sitmypet.model.Evenement;

public class EmailService {

    // ⚠️ IMPORTANT : Remplacez par une vraie adresse Gmail et un mot de passe d'application.
    // Le mot de passe d'application doit être généré depuis les paramètres de sécurité du compte Google (Authentification à deux facteurs).
    private static final String SMTP_USER = "radhouaniyassine17@gmail.com";
    private static final String SMTP_PASSWORD = "ytvgywyudoxxasbz";

    public boolean envoyerNouveauMotDePasse(String destinataire, String nouveauMdp) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.ssl.enable", "true"); //SSL
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER, "L'équipe SitMyPet"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(destinataire)
            );
            message.setSubject("Réinitialisation de votre mot de passe - SitMyPet");
            
            String contenuHtml = "<h2>Bonjour,</h2>"
                    + "<p>Vous avez demandé la réinitialisation de votre mot de passe sur SitMyPet.</p>"
                    + "<p>Voici votre nouveau mot de passe temporaire : <b>" + nouveauMdp + "</b></p>"
                    + "<p>Nous vous conseillons de vous connecter avec ce mot de passe puis de le modifier dans votre espace personnel.</p>"
                    + "<br><p>À très bientôt,<br>L'équipe SitMyPet 🐾</p>";

            message.setContent(contenuHtml, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès à " + destinataire);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
            
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Erreur Technique SMTP");
                alert.setHeaderText("L'envoi a échoué ! Voici l'erreur technique :");
                alert.setContentText(e.toString() + "\n\nCause : " + (e.getCause() != null ? e.getCause().toString() : "Aucune"));
                alert.showAndWait();
            });
            
            return false;
        }
    }

    public boolean envoyerAlerteBlocage(String destinataire) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.ssl.enable", "true"); //SSL
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER, "Sécurité SitMyPet"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("🚨 ALERTE DE SÉCURITÉ : Votre compte a été bloqué");
            
            String contenuHtml = "<div style='font-family: Arial, sans-serif; color: #333;'>"
                    + "<h2 style='color: #e53e3e;'>Alerte de Sécurité</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Nous avons détecté un très grand nombre de tentatives de connexion échouées sur votre compte SitMyPet (8 tentatives consécutives).</p>"
                    + "<p>Par mesure de sécurité stricte, <b>votre compte a été complètement verrouillé.</b></p>"
                    + "<p>Si ce n'était pas vous, quelqu'un essaie peut-être d'accéder à votre compte.</p>"
                    + "<br><p>Pour débloquer votre compte, veuillez contacter le support technique ou procéder à une réinitialisation de votre mot de passe depuis l'application.</p>"
                    + "<br><p>L'équipe de Sécurité SitMyPet 🐾</p>"
                    + "</div>";

            message.setContent(contenuHtml, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("✅ Email de blocage envoyé à " + destinataire);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'alerte de blocage : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean envoyerRapportIA(String destinataire, String nomPrenom, boolean estAccepte, String contenuHtmlIA) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.ssl.enable", "true"); //SSL
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER, "Intelligence SitMyPet"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            
            if (estAccepte) {
                message.setSubject("✅ Votre inscription Gardien Validée - Rapport SitMyPet");
            } else {
                message.setSubject("⚠️ Problème avec votre certificat - Rapport SitMyPet");
            }
            
            String contenuComplet = "<div style='font-family: Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;'>"
                    + "<div style='text-align: center; margin-bottom: 20px;'>"
                    + "<h1 style='color: #8e5bd6; margin: 0;'>SitMyPet</h1>"
                    + "<p style='color: #666; margin: 5px 0 0 0;'>Rapport d'Analyse Automatisée</p>"
                    + "</div>"
                    + "<p>Bonjour <b>" + nomPrenom + "</b>,</p>"
                    + contenuHtmlIA
                    + "<hr style='border: 0; border-top: 1px solid #eee; margin: 30px 0;'>"
                    + "<p style='font-size: 12px; color: #999; text-align: center;'>"
                    + "Ce rapport a été généré automatiquement par l'Intelligence Artificielle de SitMyPet.<br>"
                    + "Si vous pensez qu'il s'agit d'une erreur, un administrateur procédera à une vérification manuelle."
                    + "</p>"
                    + "</div>";

            message.setContent(contenuComplet, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("✅ Email de rapport IA envoyé à " + destinataire);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de rapport IA : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean envoyerEmailInscriptionEvenement(User user, Evenement event) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER, "Événements SitMyPet"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
            message.setSubject("🎟️ Confirmation d'inscription : " + event.getName());

            // Formatage QR Code data
            String eventDetails = "Événement: " + event.getName() + "\n" +
                                  "Date: " + event.getDate() + " à " + event.getHeure() + "\n" +
                                  "Lieu: " + event.getAddresse();
            String qrCodeDataEncoded = URLEncoder.encode(eventDetails, StandardCharsets.UTF_8.toString());
            String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" + qrCodeDataEncoded;

            // Formatage lien Maps
            String addressEncoded = URLEncoder.encode(event.getAddresse(), StandardCharsets.UTF_8.toString());
            String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" + addressEncoded;

            String contenuHtml = "<div style='font-family: \"Inter\", Arial, sans-serif; color: #1e293b; max-width: 600px; margin: 0 auto; padding: 30px; background-color: #f8fafc; border-radius: 12px; border: 1px solid #e2e8f0;'>"
                    + "<div style='text-align: center; margin-bottom: 25px;'>"
                    + "<h1 style='color: #8e5bd6; margin: 0; font-size: 28px;'>SitMyPet</h1>"
                    + "<p style='color: #64748b; font-size: 16px; margin-top: 5px;'>Votre Billet Électronique</p>"
                    + "</div>"
                    
                    + "<div style='background-color: white; padding: 25px; border-radius: 10px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); text-align: center;'>"
                    + "<h2 style='color: #0f172a; margin-top: 0;'>" + event.getName() + "</h2>"
                    + "<p style='font-size: 15px; color: #475569;'><b>Date :</b> " + event.getDate() + " à " + event.getHeure() + "</p>"
                    + "<p style='font-size: 15px; color: #475569;'><b>Lieu :</b> " + event.getAddresse() + "</p>"
                    
                    + "<div style='margin: 30px 0;'>"
                    + "<p style='font-size: 13px; color: #94a3b8; margin-bottom: 10px;'>PRÉSENTEZ CE QR CODE À L'ENTRÉE</p>"
                    + "<img src='" + qrCodeUrl + "' alt='QR Code' style='border: 10px solid white; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); width: 200px; height: 200px;'/>"
                    + "</div>"
                    
                    + "<a href='" + mapsUrl + "' style='display: inline-block; background-color: #10b981; color: white; text-decoration: none; padding: 12px 24px; border-radius: 8px; font-weight: bold; font-size: 15px; box-shadow: 0 4px 6px rgba(16, 185, 129, 0.3);'>📍 Voir sur Google Maps</a>"
                    + "</div>"
                    
                    + "<p style='margin-top: 30px; font-size: 13px; color: #94a3b8; text-align: center;'>"
                    + "Bonjour <b>" + user.getNom() + "</b>, votre inscription a bien été prise en compte. "
                    + "Si vous ne pouvez plus assister à l'événement, merci de vous désinscrire depuis l'application.<br><br>"
                    + "À très bientôt,<br>L'équipe SitMyPet 🐾"
                    + "</p>"
                    + "</div>";

            message.setContent(contenuHtml, "text/html; charset=utf-8");
            Transport.send(message);

            System.out.println("✅ Email d'inscription envoyé avec succès à " + user.getEmail());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email d'inscription : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
