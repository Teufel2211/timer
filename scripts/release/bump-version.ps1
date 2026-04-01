param(
    [string]$PropertiesPath = "gradle.properties"
)

if (-not (Test-Path -LiteralPath $PropertiesPath)) {
    throw "File not found: $PropertiesPath"
}

$content = Get-Content -LiteralPath $PropertiesPath -Raw
$match = [regex]::Match($content, '(?m)^mod_version=(\d+)\.(\d+)\.(\d+)\s*$')
if (-not $match.Success) {
    throw "mod_version not found in $PropertiesPath"
}

$major = [int]$match.Groups[1].Value
$minor = [int]$match.Groups[2].Value
$patch = [int]$match.Groups[3].Value

# Guard invalid versions for this custom scheme.
if ($minor -gt 9 -or $patch -gt 9) {
    throw "Invalid mod_version '$major.$minor.$patch'. Allowed range is X.0.0 to X.9.9 (patch 0-9, minor 0-9)."
}

# Custom scheme:
# 1.0.0 -> ... -> 1.0.9 -> 1.1.0 -> ... -> 1.9.9 -> 2.0.0 -> ...
if ($patch -lt 9) {
    $patch++
} else {
    $patch = 0
    if ($minor -lt 9) {
        $minor++
    } else {
        $minor = 0
        $major++
    }
}

$newVersion = "$major.$minor.$patch"
$updated = [regex]::Replace($content, '(?m)^mod_version=\d+\.\d+\.\d+\s*$', "mod_version=$newVersion")
Set-Content -LiteralPath $PropertiesPath -Value $updated -Encoding UTF8

Write-Host "Version bumped to $newVersion"
