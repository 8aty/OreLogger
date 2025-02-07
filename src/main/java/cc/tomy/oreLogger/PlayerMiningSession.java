package cc.tomy.oreLogger;

import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlayerMiningSession {

    private final Player player;
    private final Map<Material, Integer> oresMined = new HashMap<>();
    private final Set<String> coordinates = new HashSet<>();
    private final LocalDateTime startTime;
    private final String pluginVersion;

    public PlayerMiningSession(Player player) {
        this.player = player;
        this.startTime = LocalDateTime.now();

        // Get the plugin instance and retrieve its version
        Plugin plugin = player.getServer().getPluginManager().getPlugin("OreLogger");
        this.pluginVersion = plugin != null ? plugin.getDescription().getVersion() : "Unknown";
    }

    public void addMinedOre(Material material, int x, int y, int z) {
        oresMined.put(material, oresMined.getOrDefault(material, 0) + 1);
        coordinates.add(x + ", " + y + ", " + z);
    }

    public void sendToDiscord(WebhookManager webhookManager) {
        LocalDateTime endTime = LocalDateTime.now();
        String timeRange = formatTime(startTime) + " - " + formatTime(endTime);
        String playerName = player.getName();
        String playerHeadUrl = "https://mc-heads.net/avatar/" + playerName;
        String playerProfileUrl = "https://namemc.com/profile/" + playerName;

        // Build ores mined description
        StringBuilder oresDescription = new StringBuilder("```");
        for (Map.Entry<Material, Integer> entry : oresMined.entrySet()) {
            oresDescription.append(entry.getValue()).append("x ").append(entry.getKey().name()).append("\n");
        }
        oresDescription.append("```");

        // Build coordinates field
        String coordsField = "```\n" + String.join("\n", coordinates) + "\n```";

        // Get the world name where the ores were mined
        String worldName = player.getWorld().getName();
        String dimensionName = ((OreLogger) Bukkit.getPluginManager().getPlugin("OreLogger")).getDimensionName(worldName);;

        // Send the embed to Discord
        webhookManager.sendMessage(
                playerName,
                oresDescription.toString(),
                timeRange,
                coordsField,
                playerHeadUrl,
                playerProfileUrl,
                getDescription(),
                dimensionName // Pass the world name here
        );

        // Send in-game admin alerts
        sendAdminAlert();
    }

    public void sendAdminAlert() {
        if (!coordinates.isEmpty()) {
            // Get the first set of coordinates from the set
            String[] firstCoords = coordinates.iterator().next().split(", ");
            int x = Integer.parseInt(firstCoords[0]);
            int y = Integer.parseInt(firstCoords[1]);
            int z = Integer.parseInt(firstCoords[2]);

            // Get the world name where the ores were mined
            String worldName = player.getWorld().getName();
            String dimensionName = ((OreLogger) Bukkit.getPluginManager().getPlugin("OreLogger")).getDimensionName(worldName);

            // Build ores mined summary with colors
            StringBuilder oresSummary = new StringBuilder("§a");
            for (Map.Entry<Material, Integer> entry : oresMined.entrySet()) {
                oresSummary.append(entry.getValue()).append("x ").append(entry.getKey().name()).append(", ");
            }
            if (oresSummary.length() > 2) {
                oresSummary.setLength(oresSummary.length() - 2);
            }

            // Create a clickable message with direct teleportation
            TextComponent message = new TextComponent("§6[OreLogger] §6" + player.getName() + " §7has mined: §a" + oresSummary + " §7at §b" + x + ", " + y + ", " + z + " §7in dimension §b" + dimensionName + "§7.");
            message.setColor(net.md_5.bungee.api.ChatColor.GOLD);

            // Add click event to trigger teleportation via plugin
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/orelogger tp " + x + " " + y + " " + z + " " + worldName));

            // Add hover event to show coordinates and dimension
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§bTeleport to §7" + x + ", " + y + ", " + z + " §bin dimension §7" + dimensionName).create()));

            // Send the message to all online admins with the 'orelogger.alerts' permission
            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("orelogger.alerts") && ((OreLogger) Bukkit.getPluginManager().getPlugin("OreLogger")).shouldSendAlert(admin.getUniqueId())) {
                    admin.spigot().sendMessage(message);
                }
            }
        }
    }

    private String formatTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private String getDescription() {
        return "OreLogger - Version " + pluginVersion;
    }
}