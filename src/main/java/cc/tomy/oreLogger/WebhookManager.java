package cc.tomy.oreLogger;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class WebhookManager {

    private final String webhookUrl;
    private final Logger logger;

    public WebhookManager(String webhookUrl, Logger logger) {
        this.webhookUrl = webhookUrl;
        this.logger = logger;
    }

    public void sendMessage(String playerName, String ores, String timeRange, String coords, String playerHeadUrl, String playerProfileUrl, String footerText, String worldName) {
        try {
            // Remove commas from coords so that it becomes usable for the tp command.
            coords = coords.replace(",", "");
            // Escape special characters in JSON strings.
            ores = escapeJson(ores);
            coords = escapeJson(coords);
            footerText = escapeJson(footerText);
            timeRange = escapeJson(timeRange);
            playerName = escapeJson(playerName);
            playerHeadUrl = escapeJson(playerHeadUrl);
            playerProfileUrl = escapeJson(playerProfileUrl);
            worldName = escapeJson(worldName);

            // Create JSON payload with embed formatting.
            String jsonPayload = "{"
                    + "\"content\": \"New alert from OreLogger:\","
                    + "\"embeds\": ["
                    + "{"
                    + "\"title\": \"Player has mined:\","
                    + "\"description\": \"" + ores + "\","
                    + "\"color\": 3447003,"  // Blue color
                    + "\"fields\": ["
                    + "{"
                    + "\"name\": \"Time: " + timeRange + "\","
                    + "\"value\": \"" + coords + "\""
                    + "},"
                    + "{"
                    + "\"name\": \"World\","
                    + "\"value\": \"" + worldName + "\","
                    + "\"inline\": true"
                    + "}"
                    + "],"
                    + "\"author\": {"
                    + "\"name\": \"" + playerName + "\","
                    + "\"url\": \"" + playerProfileUrl + "\","
                    + "\"icon_url\": \"" + playerHeadUrl + "\""
                    + "},"
                    + "\"footer\": {\"text\": \"" + footerText + "\"},"
                    + "\"timestamp\": \"" + java.time.Instant.now().toString() + "\""
                    + "}"
                    + "]"
                    + "}";

            // Open connection to Discord webhook.
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Write payload to the connection.
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            // Check response code (Discord returns 204 No Content on success).
            int responseCode = connection.getResponseCode();
            if (responseCode != 204) {
                throw new RuntimeException("Failed to send message to Discord. Response code: " + responseCode);
            }
        } catch (Exception e) {
            logger.severe("Error sending message to Discord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendStartupMessage(String version, Logger logger) {
        try {
            version = escapeJson(version);

            // Create JSON payload for startup message.
            String jsonPayload = "{"
                    + "\"content\": \"OreLogger has started!\","
                    + "\"embeds\": ["
                    + "{"
                    + "\"title\": \"Plugin Started\","
                    + "\"description\": \"OreLogger v" + version + " is now active.\","
                    + "\"color\": 3447003,"  // Blue color
                    + "\"footer\": {\"text\": \"OreLogger - Version " + version + "\"},"
                    + "\"timestamp\": \"" + java.time.Instant.now().toString() + "\""
                    + "}"
                    + "]"
                    + "}";

            // Open connection to Discord webhook.
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Write payload to the connection.
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            // Check response code.
            int responseCode = connection.getResponseCode();
            if (responseCode != 204) {
                throw new RuntimeException("Failed to send startup message to Discord. Response code: " + responseCode);
            }
        } catch (Exception e) {
            logger.severe("Error sending startup message to Discord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}