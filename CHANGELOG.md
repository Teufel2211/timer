# TimerWave 1.0.1

## Added
- Lap/Split system: `/timer lap`, `/timer laps`, `/timer lap clear`
- Player stats commands: `/timer best`, `/timer compare <player>`, `/timer top [limit]`
- Dedicated CI workflow coverage for:
  - unit tests
  - dedicated server smoke test
  - client smoke test
  - version + one-click-release flow checks

## Changed
- `/timer start` from server console now starts all online players synchronously
- Release/versioning scheme updated to:
  - `1.0.0 -> ... -> 1.0.9 -> 1.1.0 -> ... -> 1.9.9 -> 2.0.0`

## Fixed
- Timer now auto-pauses when a player disconnects
- Best-time tracking corrected to avoid incorrect stop-time calculations
- General multiplayer persistence stability improvements
