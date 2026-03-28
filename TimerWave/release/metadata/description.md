# TimerWave

TimerWave is a lightweight Fabric mod for Minecraft 1.21.11 that gives players a world-persistent timer and countdown with clean animated colors.

## Summary
- Player timer: start, stop, pause, resume, reset
- Countdown: set, start, stop, reset, status
- Death handling: timer auto-pauses on death
- Persistent save: timer/countdown states are stored per world and restored on rejoin
- Multiplayer ready: each player has an independent state on server

## Commands
- `/timer start`
- `/timer stop`
- `/timer pause`
- `/timer resume`
- `/timer reset`
- `/timer status`
- `/timer style <minimal|smooth|pulse|mono|retro>`
- `/timer color <blue|blue_cyan_white|violet_blue|turquoise_blue|rainbow|gray|graphite|silver_blue|fire|forest|sunset|ice|pink_purple>`
- `/timer countdown set <seconds>`
- `/timer countdown start`
- `/timer countdown stop`
- `/timer countdown reset`
- `/timer countdown status`

## Display
- Time format: `Xd Xh Xm Xs`
- Countdown runs with a top blue bossbar and red blink at finish (no sound)

## Compatibility
- Loader: Fabric
- Minecraft: 1.21.11
- Client: optional
- Server: required
