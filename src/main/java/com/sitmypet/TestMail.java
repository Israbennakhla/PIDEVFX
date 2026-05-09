package com.sitmypet;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class TestMail {
    public static void main(String[] args) {
        String SMTP_USER = "radhouaniyassine17@gmail.com";
        String SMTP_PASSWORD = "ytvgywyudoxxasbz";
        
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
            message.setFrom(new InternetAddress(SMTP_USER, "Test SitMyPet"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(SMTP_USER)
            );
            message.setSubject("Test Connexion");
            message.setText("Ceci est un test");

            Transport.send(message);
            System.out.println("✅ SUCCES !");
        } catch (Exception e) {
            System.err.println("❌ ERREUR :");
            e.printStackTrace();
        }
    }
}
