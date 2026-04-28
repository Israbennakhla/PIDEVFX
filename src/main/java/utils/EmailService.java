package utils;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * EmailService  –  Métier Avancé : Envoi de Billet par Email
 * ─────────────────────────────────────────────────────────────────────────────
 * Sends a fully-styled HTML email containing the ticket details and the QR
 * Code embedded directly in the message body (cid:qrcode), exactly like the
 * Symfony Mailer version. Sending is done asynchronously so the UI never
 * freezes.
 *
 * ⚠️ CONFIGURATION : Replace FROM_EMAIL and APP_PASSWORD with your Gmail
 *    address and a Gmail App Password (not your normal password).
 *    Guide : https://support.google.com/accounts/answer/185833
 */
public class EmailService {

    // ── SMTP credentials – edit these ────────────────────────────────────────
    private static final String FROM_EMAIL   = "israbennakhla12@gmail.com";
    private static final String APP_PASSWORD = "xkyu jxyt zsoy ugzz";
    private static final String FROM_NAME    = "PI-DEV Events 🎫";

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int    SMTP_PORT = 587;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sends the ticket email asynchronously and returns a CompletableFuture
     * that resolves to {@code true} on success or {@code false} on failure.
     */
    public static CompletableFuture<Boolean> sendTicketEmail(
            String toEmail,
            String fullName,
            String eventName,
            String eventDate,
            String eventHeure,
            String eventAddress,
            int    userId,
            int    eventId,
            BufferedImage qrCodeImage) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                // ── 1. Build Jakarta Mail session ─────────────────────────
                Properties props = new Properties();
                props.put("mail.smtp.auth",            "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host",            SMTP_HOST);
                props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
                props.put("mail.smtp.ssl.trust",       SMTP_HOST);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                    }
                });

                // ── 2. Build the MimeMessage ──────────────────────────────
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME, "UTF-8"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("🎟️ Votre Billet Électronique – " + eventName);

                // ── 3. Convert BufferedImage to PNG bytes for embedding ───
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(qrCodeImage, "PNG", baos);
                byte[] qrBytes = baos.toByteArray();

                // ── 4. Multipart/related (HTML + inline image) ───────────
                MimeMultipart multipart = new MimeMultipart("related");

                // HTML part
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(
                    buildHtmlBody(fullName, eventName, eventDate, eventHeure,
                                  eventAddress, userId, eventId),
                    "text/html; charset=UTF-8");
                multipart.addBodyPart(htmlPart);

                // Inline QR Code image (Content-ID: qrcode)
                MimeBodyPart imagePart = new MimeBodyPart();
                DataSource ds = new ByteArrayDataSource(qrBytes, "image/png");
                imagePart.setDataHandler(new DataHandler(ds));
                imagePart.setHeader("Content-ID", "<qrcode>");
                imagePart.setDisposition(MimeBodyPart.INLINE);
                multipart.addBodyPart(imagePart);

                message.setContent(multipart);

                // ── 5. Send ───────────────────────────────────────────────
                Transport.send(message);
                System.out.println("✅ Email envoyé avec succès à : " + toEmail);
                return true;

            } catch (Exception e) {
                System.err.println("❌ Erreur envoi email : " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTML template
    // ─────────────────────────────────────────────────────────────────────────

    private static String buildHtmlBody(String fullName, String eventName,
                                         String eventDate, String eventHeure,
                                         String eventAddress,
                                         int userId, int eventId) {
        String ticketId = "EVENT-" + eventId + "-USER-" + userId;
        return "<!DOCTYPE html>" +
        "<html lang='fr'><head><meta charset='UTF-8'>" +
        "<style>" +
          "*{margin:0;padding:0;box-sizing:border-box;}" +
          "body{font-family:Arial,sans-serif;background:#f0f4f8;padding:30px;}" +
          ".wrap{max-width:620px;margin:0 auto;border-radius:20px;overflow:hidden;" +
                "box-shadow:0 10px 40px rgba(0,0,0,.18);}" +
          /* Header */
          ".hdr{background:linear-gradient(135deg,#1a1a2e 0%,#16213e 50%,#0f3460 100%);" +
               "color:#fff;padding:42px 30px;text-align:center;}" +
          ".hdr .icon{font-size:48px;margin-bottom:12px;}" +
          ".hdr h1{font-size:24px;letter-spacing:1px;margin-bottom:6px;}" +
          ".hdr .sub{opacity:.75;font-size:13px;}" +
          /* Body card */
          ".body{background:#fff;padding:30px 36px;}" +
          ".badge{display:inline-block;background:linear-gradient(135deg,#0f3460,#533483);" +
                 "color:#fff;padding:6px 18px;border-radius:20px;font-size:12px;" +
                 "font-weight:700;letter-spacing:.5px;margin-bottom:24px;}" +
          /* Info table */
          "table.info{width:100%;border-collapse:collapse;margin-bottom:20px;}" +
          "table.info td{padding:10px 6px;border-bottom:1px solid #f0f0f0;font-size:13px;}" +
          "table.info td:first-child{color:#0f3460;font-weight:700;width:38%;}" +
          "table.info td:last-child{color:#333;}" +
          /* Dashed divider */
          ".divider{text-align:center;color:#aaa;font-size:11px;letter-spacing:2px;" +
                   "margin:20px 0;border-top:2px dashed #e0e0e0;padding-top:20px;}" +
          /* QR section */
          ".qr-box{text-align:center;background:#f8f9ff;border-radius:16px;padding:28px;}" +
          ".qr-box img{width:200px;height:200px;border:8px solid #1a1a2e;border-radius:12px;}" +
          ".qr-box .tip{margin-top:12px;color:#666;font-size:12px;}" +
          ".ticket-id{font-family:monospace;background:#f0f4f8;color:#0f3460;" +
                     "padding:3px 10px;border-radius:4px;font-size:11px;}" +
          /* Footer */
          ".ftr{background:#1a1a2e;color:rgba(255,255,255,.55);padding:18px 30px;" +
               "text-align:center;font-size:11px;line-height:1.8;}" +
        "</style></head><body>" +
        "<div class='wrap'>" +
          "<div class='hdr'>" +
            "<div class='icon'>🎟️</div>" +
            "<h1>" + eventName + "</h1>" +
            "<div class='sub'>Votre inscription a été confirmée avec succès !</div>" +
          "</div>" +
          "<div class='body'>" +
            "<span class='badge'>✅ INSCRIPTION CONFIRMÉE</span>" +
            "<table class='info'>" +
              "<tr><td>👤 Participant</td><td>" + fullName + "</td></tr>" +
              "<tr><td>📅 Date</td><td>" + eventDate + "</td></tr>" +
              "<tr><td>🕐 Heure</td><td>" + eventHeure + "</td></tr>" +
              "<tr><td>📍 Adresse</td><td>" + eventAddress + "</td></tr>" +
              "<tr><td>🎫 Ticket ID</td><td><span class='ticket-id'>" + ticketId + "</span></td></tr>" +
            "</table>" +
            "<div class='divider'>QR CODE D'ACCÈS</div>" +
            "<div class='qr-box'>" +
              "<img src='cid:qrcode' alt='QR Code Billet'/>" +
              "<div class='tip'>Présentez ce QR Code à l&apos;entrée de l&apos;événement</div>" +
            "</div>" +
          "</div>" +
          "<div class='ftr'>" +
            "PI-DEV Event Management System — ESPRIT School of Engineering<br/>" +
            "Ce billet est personnel et non transférable." +
          "</div>" +
        "</div>" +
        "</body></html>";
    }
}
