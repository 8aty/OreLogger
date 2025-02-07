package cc.tomy.oreLogger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OreLogger extends JavaPlugin implements Listener, TabCompleter {

    private WebhookManager webhookManager;
    private final Map<UUID, PlayerMiningSession> miningSessions = new ConcurrentHashMap<>();
    private Set<Material> trackedOres = new HashSet<>();
    private int sessionDelay; // Configurable delay in seconds
    private final Map<UUID, Boolean> playerAlerts = new ConcurrentHashMap<>();
    private final Set<UUID> scheduledFlushes = ConcurrentHashMap.newKeySet();// In-memory storage for player alert preferences

    @Override
    public void onEnable() {
        // Save the default config if it doesn't exist
        saveDefaultConfig();

        // Load the configuration
        reloadConfiguration();

        // Send a startup message to Discord
        webhookManager.sendStartupMessage(getDescription().getVersion(), getLogger());

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Log that the plugin has been enabled
        getLogger().info("OreLogger has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("OreLogger has been disabled!");
    }

    public String getDimensionName(String worldName) {
        return switch (worldName.toLowerCase()) {
            case "world" -> "overworld";
            case "world_nether" -> "the_nether";
            case "world_the_end" -> "the_end";
            default -> worldName; // Fallback for custom worlds
        };
    }

    private void reloadConfiguration() {
        // Reload the config file from disk
        reloadConfig();
        FileConfiguration config = getConfig();

        // Load the webhook URL
        String webhookUrl = config.getString("webhook-url");
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            getLogger().severe("Webhook URL is not set in config.yml! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Reinitialize the webhook manager with the updated webhook URL
        webhookManager = new WebhookManager(webhookUrl, getLogger());

        // Load tracked ores from the config
        List<String> oreNames = config.getStringList("tracked-ores");
        trackedOres.clear(); // Clear existing ores
        for (String oreName : oreNames) {
            try {
                Material material = Material.valueOf(oreName.toUpperCase());
                trackedOres.add(material);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid material in config: " + oreName);
            }
        }

        if (trackedOres.isEmpty()) {
            getLogger().severe("No valid ores found in config.yml! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
        }

        // Load session delay from the config (default to 15 seconds if not set)
        sessionDelay = config.getInt("session-delay", 15);
        if (sessionDelay <= 0) {
            getLogger().warning("Invalid session-delay in config.yml. Using default value of 15 seconds.");
            sessionDelay = 15;
        }

        // Log successful reload
        getLogger().info("Configuration reloaded successfully.");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check if the block is an ore
        Material material = block.getType();
        if (isOre(material)) {
            UUID playerId = player.getUniqueId();

            // Get or create a mining session for the player
            miningSessions.putIfAbsent(playerId, new PlayerMiningSession(player));
            PlayerMiningSession session = miningSessions.get(playerId);

            // Add the mined ore to the session
            session.addMinedOre(material, block.getX(), block.getY(), block.getZ());

            // Only schedule a flush if one is not already scheduled for this player
            if (!scheduledFlushes.contains(playerId)) {
                scheduledFlushes.add(playerId);
                scheduleSessionFlush(playerId, session);
            }
        }
    }

    private boolean isOre(Material material) {
        return trackedOres.contains(material);
    }

    private void scheduleSessionFlush(UUID playerId, PlayerMiningSession session) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Remove the session and send the embed
            PlayerMiningSession removedSession = miningSessions.remove(playerId);
            // Mark that the flush is no longer scheduled for this player
            scheduledFlushes.remove(playerId);
            if (removedSession != null) {
                removedSession.sendToDiscord(webhookManager);
            }
        }, 20 * sessionDelay); // Convert seconds to ticks (20 ticks per second)
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("orelogger")) {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        // Check if the sender has the required permission
                        if (!sender.hasPermission("orelogger.reload")) {
                            sender.sendMessage("§cYou do not have permission to use this command.");
                            return true;
                        }

                        // Reload the configuration
                        reloadConfiguration();
                        sender.sendMessage("§aOreLogger configuration reloaded successfully!");
                        return true;

                    case "togglealerts":
                        // Check if the sender is a player
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("§cThis command can only be used by players.");
                            return true;
                        }

                        Player player = (Player) sender;
                        UUID playerId = player.getUniqueId();

                        // Toggle the player's alert preference
                        boolean isEnabled = !playerAlerts.getOrDefault(playerId, true); // Default to true if not set
                        playerAlerts.put(playerId, isEnabled);

                        // Notify the player
                        sender.sendMessage(isEnabled ? "§aOreLogger alerts enabled." : "§cOreLogger alerts disabled.");
                        return true;

                    case "tp":
                        // Check if the sender is a player
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("§cThis command can only be used by players.");
                            return true;
                        }

                        // Ensure the sender has permission
                        if (!sender.hasPermission("orelogger.tp")) {
                            sender.sendMessage("§cYou do not have permission to use this command.");
                            return true;
                        }

                        // Parse arguments
                        if (args.length != 5) {
                            sender.sendMessage("§cInvalid usage. Use /orelogger tp <x> <y> <z> <world>");
                            return true;
                        }

                        try {
                            int x = Integer.parseInt(args[1]);
                            int y = Integer.parseInt(args[2]);
                            int z = Integer.parseInt(args[3]);
                            String worldName = args[4];

                            // Get the target world
                            World targetWorld = Bukkit.getWorld(worldName);
                            if (targetWorld == null) {
                                sender.sendMessage("§cWorld not found: " + worldName);
                                return true;
                            }

                            // Teleport the player
                            Player teleportingPlayer = (Player) sender;
                            Location teleportLocation = new Location(targetWorld, x, y, z);
                            teleportingPlayer.teleport(teleportLocation);

                            sender.sendMessage("§aTeleported to " + x + ", " + y + ", " + z + " in world " + worldName);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("§cInvalid coordinates. Use integers for x, y, and z.");
                        }
                        return true;

                    default:
                        sender.sendMessage("§eUsage: /orelogger <reload|togglealerts|tp>");
                        return true;
                }
            }

            // Show usage if no valid arguments are provided
            sender.sendMessage("§eUsage: /orelogger <reload|togglealerts|tp>");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("orelogger")) {
            List<String> suggestions = new ArrayList<>();

            if (args.length == 1) {
                // Suggest subcommands based on permissions
                if (sender.hasPermission("orelogger.reload")) {
                    suggestions.add("reload");
                }
                if (sender.hasPermission("orelogger.alerts")) {
                    suggestions.add("togglealerts");
                }
            }

            // Filter suggestions based on the current input
            return filterSuggestions(suggestions, args[0]);
        }

        return null;
    }

    private List<String> filterSuggestions(List<String> suggestions, String input) {
        List<String> filtered = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(suggestion);
            }
        }
        return filtered;
    }

    public boolean shouldSendAlert(UUID playerId) {
        return playerAlerts.getOrDefault(playerId, true); // Default to true if not set
    }
}