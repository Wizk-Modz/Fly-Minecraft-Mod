# Wizk Fly Mod

A lightweight Fabric mod for Minecraft 26.3 that adds per-player flight with
commands, configurable keybind, safe fall protection, speed control, persistence, and real-time client sync.

## Features

- **Personal Flight:** Toggle flight for yourself anytime with `/fly` or configurable keybind (default `G`).
- **Admin Controls:** Toggle flight for other players with `/fly <player>`.
- **Flight Speed Customization:** Adjust personal or other players' flight speed with `/fly speed <value> [player]`.
- **Safe Fall Protection:** Cancels fall damage when toggling off flight mid-air until you safely touch the ground.
- **Smart Activation:** Instantly starts flying if toggled while airborne without needing to double-tap jump.
- **State Persistence:** Flight state and speed persist across relogs and server restarts (`persist_state = true`).
- **Live Sync & Action Bar Feedback:** Real-time two-way client-server synchronization with clean Action Bar and chat notifications.
- **Live Config Reload:** Reload configuration instantly via `/fly reload`.
- **Bilingual Support:** Full English and Vietnamese in-game translations.

## Requirements

- Minecraft 26.3
- Fabric Loader 0.20.1 or newer
- Fabric API 0.167.0+26.3
- Java 25

## Installation

1. Install Fabric Loader for Minecraft 26.3.
2. Put `fabric-api` and `wizk-fly-mod-<version>.jar` into your `mods` folder.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/fly` | Toggle flight for yourself | Everyone |
| `/fly <player>` | Toggle flight for another player | Admin (OP level 2 / allow cheats) |
| `/fly speed <value>` | Set personal flight speed (0.01 - 0.5) | Everyone |
| `/fly speed <value> <player>` | Set another player's flight speed | Admin (OP level 2 / allow cheats) |
| `/fly reload` | Reload configuration from file | Admin (OP level 2 / allow cheats) |

## Keybind

The default key is `G`, taken from `toggle_fly_key` in the config file.
Players can rebind it in **Options → Controls → Key Binds**.

## Configuration

The mod creates `config/wizk-fly-mod.toml` on first launch:

```toml
# Wizk Fly Mod Configuration
# Client-side toggle key (GLFW key name)
toggle_fly_key = "key.keyboard.g"

# Persist flight state across reconnects and server restarts (true/false)
persist_state = true

# Protect player from fall damage when toggled off mid-air (true/false)
safe_fall = true

# Maximum fall damage protection duration in seconds
safe_fall_timeout_seconds = 10

# Automatically start flying when toggled while in the air (true/false)
auto_flight_on_toggle = true

# Default flight speed (0.01 to 0.5, vanilla default is 0.05)
default_fly_speed = 0.05

# Where to display toggle feedback ("actionbar", "chat", "both", "none")
message_display = "actionbar"
```

## Behavior

- Enabling fly grants flight ability (`mayfly`). If enabled while airborne and `auto_flight_on_toggle = true`, flight starts immediately.
- When turning off flight mid-air, `safe_fall` prevents fall damage until the player safely touches the ground or the timeout expires.
- Creative and Spectator modes are never interfered with; standard vanilla flight behavior remains intact.
- When returning from Creative or Spectator to Survival or Adventure, the player's mod flight state is cleanly restored.

## Building

```sh
./gradlew build
```

The mod jar is output to `build/libs/`.

## License

This project is available under the CC0 license.
