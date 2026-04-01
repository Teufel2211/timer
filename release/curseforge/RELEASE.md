# CurseForge Release

## Required Metadata
- Title: `TimerWave`
- Summary: `Server-side timer and countdown with smooth wave colors, death pause, and world-persistent progress.`
- Description source: `release/metadata/description.md`
- Icon: `release/assets/timerwave-logo-512.png`
- Environment:
  - Client: `Optional`
  - Server: `Required`

## Recommended Categories (DE UI)
- `Werkzeug`
- `Spielmechaniken`
- `Optimierung`

## Upload Steps
One-click CI route (recommended):
- Trigger GitHub Actions workflow `Release` (see `release/AUTOMATION.md`).

Manual/local route:
1. Set `curseforge_project_id` in `gradle.properties`.
2. Update `CHANGELOG.md` and `release/changelogs/<version>.md`.
3. Build release artifacts:
   - `gradlew.bat prepareReleaseBundle`
4. Upload from `build/release-bundle` manually to CurseForge:
   - Loader: Fabric
   - Game Version: 1.21.11
   - Release Type: use `curseforge_release_type` from `gradle.properties`
   - Changelog: use the matching file in `release/changelogs/`
