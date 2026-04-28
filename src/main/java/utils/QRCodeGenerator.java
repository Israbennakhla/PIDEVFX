package utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * QRCodeGenerator  –  Métier Avancé : Génération de QR Code
 * ─────────────────────────────────────────────────────────────────────────────
 * Converts a JSON ticket string into a QR Code image using Google's ZXing
 * library.  The result can be used both as an embedded email image AND as a
 * JavaFX Image displayed in the ticket-confirmation popup.
 */
public class QRCodeGenerator {

    /** Dark navy colour used for QR modules (matches the app's colour scheme). */
    private static final int COLOR_DARK  = 0xFF1a1a2e;
    /** Pure white background. */
    private static final int COLOR_LIGHT = 0xFFFFFFFF;

    // ─── Core generator ──────────────────────────────────────────────────────

    /**
     * Generates a QR Code as a {@link BufferedImage}.
     *
     * @param data   JSON string to encode
     * @param width  image width in pixels
     * @param height image height in pixels
     */
    public static BufferedImage generateQRCodeImage(String data, int width, int height)
            throws WriterException {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // high error correction
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix   = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, matrix.get(x, y) ? COLOR_DARK : COLOR_LIGHT);
            }
        }
        return img;
    }

    // ─── Convenience helpers ─────────────────────────────────────────────────

    /**
     * Returns the QR Code as a raw PNG byte array (used for email embedding).
     */
    public static byte[] generateQRCodeBytes(String data, int width, int height)
            throws WriterException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(generateQRCodeImage(data, width, height), "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Returns the QR Code as a JavaFX {@link Image} (used in the ticket popup).
     */
    public static Image generateQRCodeFXImage(String data, int width, int height)
            throws WriterException, IOException {
        return new Image(new ByteArrayInputStream(generateQRCodeBytes(data, width, height)));
    }

    // ─── JSON builder ────────────────────────────────────────────────────────

    /**
     * Builds the JSON payload that will be encoded into the QR Code.
     * Mirrors the same structure used in the Symfony version.
     */
    public static String buildTicketJson(int userId, String nom, String prenom,
                                         String email, int eventId, String eventName,
                                         String eventDate, String eventHeure) {
        return "{"
            + "\"ticket_id\":\"EVENT-" + eventId + "-USER-" + userId + "\","
            + "\"utilisateur\":{"
            +   "\"id\":" + userId + ","
            +   "\"nom\":\"" + nom + "\","
            +   "\"prenom\":\"" + prenom + "\","
            +   "\"email\":\"" + email + "\""
            + "},"
            + "\"evenement\":{"
            +   "\"id\":" + eventId + ","
            +   "\"nom\":\"" + eventName + "\","
            +   "\"date\":\"" + eventDate + "\","
            +   "\"heure\":\"" + eventHeure + "\""
            + "}"
            + "}";
    }
}
