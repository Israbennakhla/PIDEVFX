package com.sitmypet.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    // ⚠️ IMPORTANT : Remplacez par une vraie adresse Gmail et un mot de passe d'application.
    // Le mot de passe d'application doit être généré depuis les paramètres de sécurité du compte Google (Authentification à deux facteurs).
    private static final String SMTP_USER = "radhouaniyassine17@gmail.com";
    private static final String SMTP_PASSWORD = "rbqgzlfokvjaaxac";

    public boolean envoyerNouveauMotDePasse(String destinataire, String nouveauMdp) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

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
            return false;
        }
    }

    public boolean envoyerAlerteBlocage(String destinataire) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

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
}
