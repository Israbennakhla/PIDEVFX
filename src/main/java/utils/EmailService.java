package utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String SENDER_EMAIL    = "inespidev@gmail.com";
    private static final String SENDER_PASSWORD = "eppprmoksbdqfiko"; // App Password Gmail

    public static void envoyerReponse(String destinataire, String sujet, String contenu) {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SENDER_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            msg.setSubject("Réponse à votre réclamation : " + sujet);
            msg.setContent(
                    "<h2>Votre réclamation a été traitée</h2>" +
                            "<p><b>Sujet :</b> " + sujet + "</p><hr/>" +
                            "<p>" + contenu + "</p>" +
                            "<br/><p>Cordialement,<br/>L'équipe SitMyPet</p>",
                    "text/html; charset=utf-8"
            );
            Transport.send(msg);
            System.out.println("✅ Email envoyé à : " + destinataire);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur envoi email : " + e.getMessage());
        }
    }
}