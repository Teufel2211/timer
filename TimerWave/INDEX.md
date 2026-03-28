# TimerWave

TimerWave is a Fabric 1.21.11 timer mod focused on command-driven control and animated wave coloring.

## Main Features
- Player-bound timer with start, stop, pause, resume and reset.
- Auto-pause on death and paused state preserved after respawn.
- Command-based style and color preset switching at runtime.
- Animated wave gradient in actionbar/bossbar output.
- JSON config at `config/timerwave.json`.

## Commands
- `/timer start`
- `/timer stop`
- `/timer pause`
- `/timer resume`
- `/timer reset`
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

## Time Format
- Timer output uses a fixed duration style: `Xd Xh Xm Xs`
