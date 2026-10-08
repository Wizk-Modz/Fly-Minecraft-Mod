# Wizk Fly Mod

A lightweight Fabric mod for Minecraft 26.2 that adds per-player flight with
commands, a configurable keybind and a simple config file.

## Features

- Toggle flight for yourself with `/fly` or a keybind.
- Toggle flight for another player with `/fly <player>` (admin only).
- Per-player fly state, kept in sync across respawn, dimension change and game mode change.
- Configurable permission mode and default keybind via `wizk-fly-mod.toml`.
- English and Vietnamese in-game translations.
- Creative and Spectator modes are never affected.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.5 or newer
- Fabric API 0.161.0+26.2
- Java 25

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Put `fabric-api` and `wizk-fly-mod-<version>.jar` into your `mods` folder.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/fly` | Toggle flight for yourself | depends on `permission` in the config |
| `/fly <player>` | Toggle flight for another player | admin (op level 2 / allow cheats) |

## Keybind

The default key is `G`, taken from `toggle_fly_key` in the config file.
Players can rebind it in **Options → Controls → Key Binds**.

## Configuration

The mod creates `config/wizk-fly-mod.toml` on first launch:

```toml
# permission: "all" for every player, "owner" for admins only
permission = "all"
# toggle_fly_key: GLFW key name, e.g. "key.keyboard.g"
toggle_fly_key = "key.keyboard.g"
```

### Permission modes

- `all` — every player can use `/fly` and the keybind. Commands that target
  other players (`/fly <player>`) still require admin.
- `owner` — only admins can use any fly feature. A player counts as admin when
  the world has cheats enabled and they can use the command, or when they are op
  on a server. This uses Minecraft's own permission system.

## Behavior

- Enabling fly grants the flight ability (`mayfly`). Double-tap Space to fly,
  just like in Creative.
- Creative and Spectator modes are never touched, so their default flight keeps
  working.
- Fly state is stored per player and cleared when the player leaves the server.
- When you leave Creative or Spectator, your flight ability is kept but flying
  is stopped immediately.

## Building

```sh
./gradlew build
```

The mod jar is written to `build/libs/`.

## License

This project is available under the CC0 license.
