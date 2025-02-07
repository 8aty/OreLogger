# Changelog

All notable changes to **OreLogger** are documented in this file.

---

## [1.2.2] - 2025-02-07

### Added
- Added `orelogger.ignores` permission - any ores mined by players with this permission will not be logged, allowing staff to bypass ore logging functionality.

### Fixed
- Message on teleport not containing anymore the old world name. 

---

## [1.2.1] - 2025-02-06

### Added
- Fixed an issue where teleportation messages were not properly resetting after a session ended.
- Improved handling of continuous mining sessions so that new sessions start correctly after the delay.
- Enhanced error logging for invalid ores specified in the `config.yml`.

### Changed
- Updated the `/orelogger tp` command to handle teleportation programmatically, reducing potential permission conflicts with server commands.
- Refactored the `scheduleSessionFlush` method to better manage ongoing mining activity.

### Fixed
- Resolved a bug where alerts were sent immediately (without waiting for a new session) when the player continued mining.
- Corrected an issue with custom world names not being handled properly during teleportation.

---

## [1.2] - 2025-02-01

### Added
- Added support for cross-world teleportation using dimension names (`overworld`, `the_nether`, `the_end`) instead of folder names (`world`, `world_nether`, `world_the_end`).
- Introduced a helper method to map world folder names to dimension names for consistency.
- Implemented the `/orelogger tp` command to ensure seamless teleportation across all dimensions.

### Changed
- Updated Discord embeds to display the correct dimension names (`overworld`, `the_nether`, `the_end`) rather than folder names.

### Fixed
- Fixed a bug where teleportation commands failed on custom servers due to incorrect world name handling.

---

## [1.1] - 2025-01-25

### Added
- Introduced clickable teleport messages in admin alerts, enabling admins to quickly teleport to ore mining locations.
- Added the `/orelogger togglealerts` command to allow admins to enable or disable alerts individually.
- Enabled configuration reloading without restarting the server using `/orelogger reload`.

### Changed
- Switched Discord webhook messages to use an embed format.
- Improved the formatting of admin alert messages with colors and hover effects for enhanced readability.

### Fixed
- Resolved an issue where admin alerts were not displaying due to missing permissions or configuration errors.

---

## [1.0] - 2025-01-19

### Added
- **Initial Release:**
  - Configurable webhook URL.
  - Real-time alerts for ore mining events via in-game chat and Discord.
  - Basic support for Minecraft 1.21.x.
