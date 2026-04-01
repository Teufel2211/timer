# TimerWave

TimerWave is a Fabric mod for Minecraft 1.21.11 with a modern animated timer, per-player persistence, and multiplayer-safe command control.

## Core Features
- Player timer with `start`, `stop`, `pause`, `resume`, `reset`
- Countdown with `set`, `start`, `stop`, `reset`, `status`
- Smooth wave color animation with style and color presets
- Fixed readable time format: `Xd Xh Xm Xs` (example: `1d 1h 1m 1s`)

## Multiplayer and Persistence
- Per-player independent timer state on dedicated servers
- Auto-pause on death and stays paused after respawn
- Auto-pause on disconnect (no offline progression)
- `/timer start` from server console triggers synchronized start for all online players
- Persistent world save/load via server world state

## Extra Gameplay Features
- Lap/split system:
  - `/timer lap`
  - `/timer laps`
  - `/timer lap clear`
- Stats and comparison:
  - `/timer best`
  - `/timer compare <player>`
  - `/timer top [limit]`
- Countdown finish behavior:
  - no sound
  - red blinking timer text
  - blue countdown bossbar that shrinks down to zero

## Full Command List
- `/timer start`
- `/timer stop`
- `/timer pause`
- `/timer resume`
- `/timer reset`
- `/timer lap`
- `/timer lap clear`
- `/timer laps`
- `/timer best`
- `/timer compare <player>`
- `/timer top [limit]`
- `/timer set <seconds>`
- `/timer set <days> <hours> <minutes> <seconds>`
- `/timer style <minimal|smooth|pulse|mono|retro>`
- `/timer color <blue|blue_cyan_white|violet_blue|turquoise_blue|rainbow|gray|graphite|silver_blue|fire|forest|sunset|ice|pink_purple>`
- `/timer status`
- `/timer config`
- `/timer help`
- `/timer countdown set <seconds>`
- `/timer countdown start`
- `/timer countdown stop`
- `/timer countdown reset`
- `/timer countdown status`

## Compatibility
- Loader: Fabric
- Minecraft: 1.21.11
- Environment:
  - Client: optional
  - Server: required
