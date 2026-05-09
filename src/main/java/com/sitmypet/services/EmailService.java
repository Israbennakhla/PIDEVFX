package com.sitmypet.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

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

    /** Notifie un gardien après réponse admin à sa réclamation (même SMTP que le reste du produit). */
    public boolean envoyerReponseReclamation(String destinataire, String sujetReclamation, String contenuReponse) {
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
            message.setFrom(new InternetAddress(SMTP_USER, "SitMyPet Support"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("Réponse à votre réclamation : " + sujetReclamation);

            String html = "<div style='font-family:Arial,sans-serif;color:#333;line-height:1.5;'>"
                    + "<h2 style='color:#8e5bd6;'>Votre réclamation a été traitée</h2>"
                    + "<p><b>Sujet :</b> " + escapeHtml(sujetReclamation) + "</p>"
                    + "<hr style='border:none;border-top:1px solid #eee;margin:16px 0;'/>"
                    + "<div>" + escapeHtml(contenuReponse).replace("\n", "<br/>") + "</div>"
                    + "<br/><p>Cordialement,<br/><b>L'équipe SitMyPet</b> 🐾</p>"
                    + "</div>";
            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("✅ Email réclamation envoyé à " + destinataire);
            return true;
        } catch (Exception e) {
            System.err.println("❌ envoyerReponseReclamation : " + e.getMessage());
            return false;
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
