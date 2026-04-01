# Modrinth Release

## Required Metadata
- Title: `TimerWave`
- Summary: `Server-side timer and countdown with smooth wave colors, death pause, and world-persistent progress.`
- Description source: `release/metadata/description.md`
- Icon: `release/assets/timerwave-logo-512.png`
- Client support: `Optional`
- Server support: `Required`

## Recommended Tags (DE UI)
- `Werkzeug`
- `Spielmechaniken`
- `Optimierung`

## Upload Steps
One-click CI route (recommended):
- Trigger GitHub Actions workflow `Release` (see `release/AUTOMATION.md`).

Manual/local route:
1. Set `modrinth_project_id` in `gradle.properties`.
2. Set `MODRINTH_TOKEN` in your environment.
3. Update `CHANGELOG.md` and `release/changelogs/<version>.md`.
4. Build and upload:
   - `gradlew.bat build`
   - `gradlew.bat modrinth`

If `modrinth` task is skipped, check token and project ID.
