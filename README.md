# TimerWave (Fabric 1.21.11)

TimerWave is a command-driven timer mod with animated wave colors, countdown support, per-player persistence, and multiplayer-safe behavior.

## Feature Overview
- Per-player timer: start, stop, pause, resume, reset
- Countdown mode: set, start, stop, reset, status
- Death pause and disconnect pause
- Synchronized `/timer start` for all online players when run from server console
- Lap/split tracking and statistics
- Player comparison and online leaderboard
- Persistent save per world/server
- Display format: `Xd Xh Xm Xs`

## Requirements
- Java 21
- Gradle (or generated Gradle Wrapper)

## Setup
1. Open the project root (`timer`) in your IDE.
2. If wrapper files are missing, generate them once:
   - `gradle wrapper`
3. Start the dev client:
   - `./gradlew runClient` (Linux/macOS)
   - `gradlew.bat runClient` (Windows)

## Commands
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

## Time Format
- Timer text is always formatted as `Xd Xh Xm Xs` (example: `1d 1h 1m 1s`).

## Versioning Scheme
- Start version: `1.0.0`
- Bump path: `1.0.0 -> 1.0.1 -> ... -> 1.0.9 -> 1.1.0 -> ... -> 1.9.9 -> 2.0.0`
- Auto-bump script:
  - `powershell -ExecutionPolicy Bypass -File scripts/release/bump-version.ps1`
- One-click release script:
  - `powershell -ExecutionPolicy Bypass -File scripts/release/one-click-release.ps1`

## Modrinth Release
1. Set environment variable: `MODRINTH_TOKEN`
2. Set `modrinth_project_id` in `gradle.properties`
3. Update `CHANGELOG.md` and `release/changelogs/<version>.md`
4. Publish with:
   - `gradlew.bat modrinth` (Windows)
   - `./gradlew modrinth` (Linux/macOS)

## CurseForge Release
1. Set `curseforge_project_id` in `gradle.properties`
2. Build release bundle:
   - `gradlew.bat prepareReleaseBundle` (Windows)
   - `./gradlew prepareReleaseBundle` (Linux/macOS)
3. Upload jar from `build/release-bundle` manually to CurseForge.

## Upload Metadata Files
- Modrinth metadata: `release/metadata/modrinth-metadata.json`
- CurseForge metadata: `release/metadata/curseforge-metadata.json`
- Shared description: `release/metadata/description.md`
- Logo: `release/assets/timerwave-logo-512.png`

## Automation Recommendation
- Best setup for automatic uploads: see `release/AUTOMATION.md`

## Config
Generated at: `config/timerwave.json`
