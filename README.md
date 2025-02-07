# OreLogger

[![License](https://img.shields.io/badge/License-AGPL_3-4B0082?style=flat&logo=gnu&logoColor=white)](LICENSE)
[![Version](https://img.shields.io/badge/Release-v1.2.1-9400D3?style=flat)](https://github.com/8aty/OreLogger/releases)
[![JDK](https://img.shields.io/badge/JDK-21-0000FF?style=flat&logo=openjdk&logoColor=white)](https://github.com/8aty/OreLogger/releases)
[![Maven](https://img.shields.io/badge/Built_with-Maven-00BFFF?style=flat&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![MC Support](https://img.shields.io/badge/Supports-1.21.x-87CEEB?style=flat)](https://github.com/8aty/OreLogger)


OreLogger is a lightweight Minecraft plugin designed to track ore mining activity on your server. It logs ores mined by players and sends real-time alerts to Discord and in-game chat (for admins). With customizable settings and cross-world teleportation support, OreLogger is perfect for monitoring resource gathering and preventing griefing or cheating.

---

## Features

- **Discord Integration**: Sends detailed embeds to a Discord webhook when ores are mined.
- **Admin Alerts**: Sends clickable teleport messages to admins in-game for quick investigation.
- **Configurable Ores**: Customize which ores are tracked via the `config.yml` file.
- **Session Delay**: Group ore mining events into sessions with a configurable delay (default: 15 seconds).
- **Cross-World Teleportation**: Admins can teleport directly to the world and coordinates where ores were mined.
- **Alert Toggle**: Admins can enable/disable alerts using `/orelogger togglealerts`.
- **Reload Command**: Reload the configuration dynamically with `/orelogger reload`.

---

## Changelog

See the full changelog [here](changelog.md).

---

## Requirements

- Java 21 or higher
- Minecraft Server 1.21.x
- Permission plugin ([LuckyPerms](https://luckperms.net/) recommended)

---

## Installation

1. **Download the Plugin**:
   - Download the latest JAR file from the [Releases](https://github.com/8aty/OreLogger/releases) section.

2. **Install the Plugin**:
   - Place the JAR file in your server's `plugins/` folder.

3. **Restart the Server**:
   - Restart your server to generate the default `config.yml` file.

4. **Configure the Plugin**:
   - Edit the `config.yml` file to set up your Discord webhook URL, tracked ores, and session delay.

---

## Configuration

The `config.yml` file contains all configurable options for OreLogger. Here’s an example:

```yaml
# Discord webhook URL for sending alerts
webhook-url: "https://discord.com/api/webhooks/..."

# List of ores to be tracked by the OreLogger
tracked-ores:
  - IRON_ORE
  - DEEPSLATE_IRON_ORE
  - GOLD_ORE
  - DEEPSLATE_GOLD_ORE
  - DIAMOND_ORE
  - DEEPSLATE_DIAMOND_ORE
  - EMERALD_ORE
  - DEEPSLATE_EMERALD_ORE
  - LAPIS_ORE
  - DEEPSLATE_LAPIS_ORE
  - REDSTONE_ORE
  - DEEPSLATE_REDSTONE_ORE
  - NETHER_GOLD_ORE
  - ANCIENT_DEBRIS

# Delay in seconds before a mining session is processed
# Note: Setting this value too low may cause performance issues due to frequent processing.
# It is recommended to use a reasonable delay to ensure optimal server performance.
session-delay: 15 #Default is 15 seconds
```

---

## Commands

| Command                  | Permission          | Description                                   |
|--------------------------|---------------------|-----------------------------------------------|
| `/orelogger reload`      | `orelogger.reload`  | Reload the configuration without restarting. |
| `/orelogger togglealerts`| `orelogger.alerts`  | Toggle admin alerts on/off.                  |
| `/orelogger tp`          | `orelogger.tp`      | Teleport to ore mining locations.            |

---

## Permissions

| Permission           | Description                                                       | Default Value |
|-----------------------|-------------------------------------------------------------------|---------------|
| `orelogger.reload`    | Allows reloading the configuration.                               | op            |
| `orelogger.alerts`    | Allows receiving admin alerts in-game.                            | op            |
| `orelogger.tp`        | Allows teleporting to ore mining locations.                       | op            |
| `orelogger.ignores`   | Prevents logging of ores mined by players with this permission.   | false         |

---

## Usage

### Discord Webhook Alert

When a player mines ores, the following embed will be sent to Discord:

![Discord Webhook Embed](https://i.imgur.com/0WCTys0.png)

---

### In-Game Alert

Admins will see the following message in-game:

![In-Game Alert](https://i.imgur.com/rXei8nX.gif)

Clicking the message will teleport the admin directly to the specified coordinates in the correct world.

---


## Support

For support or questions, contact the author or open an issue on GitHub.

---

## License

This project is licensed under the AGPL 3.0 License. See the [LICENSE](LICENSE) file for details.

---

### Developed by 8aty, personal website: [VISIT](https://tomy.cc/) 
