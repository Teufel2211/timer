# Best Automation Setup (Recommended)

For fully automatic uploads to **Modrinth + CurseForge**, use the included GitHub workflow:
- `.github/workflows/release.yml`

## Why this is best
1. One click from GitHub UI (`Run workflow`) or automatic on tag push (`v*`).
2. Uploads to both platforms in one run.
3. Tokens stay in GitHub Secrets, never in local files.
4. Full audit trail in Actions logs.

## Required GitHub settings
1. Repository Secrets:
   - `MODRINTH_TOKEN`
   - `CURSEFORGE_TOKEN`
2. Repository Variables:
   - `MODRINTH_PROJECT_ID`
   - `CURSEFORGE_PROJECT_ID`

## One-click release (CI)
1. Ensure `mod_version` is correct in `gradle.properties`.
   - Version scheme: `1.0.0 -> ... -> 1.0.9 -> 1.1.0 -> ... -> 1.9.9 -> 2.0.0`
2. Ensure `release/changelogs/<mod_version>.md` exists.
3. Trigger release:
   - Manual: GitHub -> Actions -> `Release` -> `Run workflow`
   - Auto: push a tag like `v1.0.0`

## Local one-click commands
- Build + bundle with current version:
  - `powershell -ExecutionPolicy Bypass -File .\scripts\release\one-click-release.ps1`
- Bump version + build + bundle:
  - `powershell -ExecutionPolicy Bypass -File .\scripts\release\one-click-release.ps1 -BumpVersion`
- Local Modrinth upload (optional):
  - `powershell -ExecutionPolicy Bypass -File .\scripts\release\one-click-release.ps1 -BumpVersion -UploadModrinth`
