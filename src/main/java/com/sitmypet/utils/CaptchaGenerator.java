package com.sitmypet.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CaptchaGenerator {

    public static class CaptchaResult {
        private final String code;
        private final Image image;

        public CaptchaResult(String code, Image image) {
            this.code = code;
            this.image = image;
        }

        public String getCode() {
            return code;
        }

        public Image getImage() {
            return image;
        }
    }

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789"; // Removed confusing chars like 1, I, l, 0, O
    private static final int WIDTH = 160;
    private static final int HEIGHT = 50;
    private static final int LENGTH = 5;

    public static CaptchaResult generateCaptcha() {
        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Anti-aliasing for smoother text and lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2d.setColor(new Color(240, 240, 245));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        Random random = new Random();

        // Add Noise (Lines)
        for (int i = 0; i < 6; i++) {
            g2d.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150), 100));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g2d.setStroke(new BasicStroke(random.nextInt(3) + 1));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Add Noise (Dots)
        for (int i = 0; i < 40; i++) {
            g2d.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g2d.fillOval(x, y, 3, 3);
        }

        // Generate Code and draw text
        StringBuilder codeBuilder = new StringBuilder();
        int startX = 15;

        for (int i = 0; i < LENGTH; i++) {
            // Pick a random char
            char ch = CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
            codeBuilder.append(ch);

            // Randomize font size and style
            int fontSize = 28 + random.nextInt(8);
            int fontStyle = random.nextBoolean() ? Font.BOLD : Font.ITALIC | Font.BOLD;
            g2d.setFont(new Font("Arial", fontStyle, fontSize));

            // Randomize color
            g2d.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));

            // Randomize rotation
            AffineTransform originalTransform = g2d.getTransform();
            double angle = (random.nextDouble() - 0.5) * 0.5; // -0.25 to 0.25 radians
            int y = 35 + random.nextInt(10) - 5;
            
            g2d.rotate(angle, startX, y);
            g2d.drawString(String.valueOf(ch), startX, y);
            g2d.setTransform(originalTransform);

            // Move to next character position
            startX += 25 + random.nextInt(5);
        }

        g2d.dispose();

        // Convert BufferedImage to JavaFX Image
        WritableImage fxImage = new WritableImage(WIDTH, HEIGHT);
        PixelWriter pw = fxImage.getPixelWriter();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                pw.setArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }

        return new CaptchaResult(codeBuilder.toString(), fxImage);
    }
}
