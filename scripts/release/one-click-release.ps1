param(
    [switch]$BumpVersion,
    [switch]$UploadModrinth
)

$ErrorActionPreference = "Stop"

function Get-PropertyValue {
    param(
        [string]$Path,
        [string]$Key
    )
    $line = Get-Content -LiteralPath $Path | Where-Object { $_ -match "^\s*$Key=" } | Select-Object -First 1
    if (-not $line) { return $null }
    return ($line -split "=", 2)[1].Trim()
}

$root = Resolve-Path (Join-Path $PSScriptRoot "..\\..")
Set-Location $root

if ($BumpVersion) {
    & powershell -ExecutionPolicy Bypass -File ".\\scripts\\release\\bump-version.ps1"
}

$version = Get-PropertyValue -Path ".\\gradle.properties" -Key "mod_version"
if (-not $version) {
    throw "mod_version not found in gradle.properties"
}

$versionedChangelog = ".\\release\\changelogs\\$version.md"
if (-not (Test-Path -LiteralPath $versionedChangelog)) {
    throw "Missing changelog for version $version at $versionedChangelog"
}

Copy-Item -LiteralPath $versionedChangelog -Destination ".\\CHANGELOG.md" -Force
Write-Host "Using changelog: $versionedChangelog"

& .\\gradlew.bat clean build prepareReleaseBundle

if ($UploadModrinth) {
    $modrinthProjectId = Get-PropertyValue -Path ".\\gradle.properties" -Key "modrinth_project_id"
    if ([string]::IsNullOrWhiteSpace($env:MODRINTH_TOKEN)) {
        throw "MODRINTH_TOKEN is missing."
    }
    if ([string]::IsNullOrWhiteSpace($modrinthProjectId) -or $modrinthProjectId -eq "replace_with_modrinth_project_id") {
        throw "Set a real modrinth_project_id in gradle.properties."
    }
    & .\\gradlew.bat modrinth
}

Write-Host ""
Write-Host "Release flow complete for version $version"
Write-Host "Bundle: .\\build\\release-bundle"
Write-Host "For CurseForge upload use: .\\release\\curseforge\\RELEASE.md"
