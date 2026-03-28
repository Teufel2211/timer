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

# Custom scheme:
# 1.0.0 -> ... -> 1.0.10 -> 1.1.0 -> ... -> 1.1.10 -> 1.2.0 -> ...
if ($patch -lt 10) {
    $patch++
} else {
    $patch = 0
    $minor++
}

$newVersion = "$major.$minor.$patch"
$updated = [regex]::Replace($content, '(?m)^mod_version=\d+\.\d+\.\d+\s*$', "mod_version=$newVersion")
Set-Content -LiteralPath $PropertiesPath -Value $updated -Encoding UTF8

Write-Host "Version bumped to $newVersion"
