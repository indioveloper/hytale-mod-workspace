[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$requiredFiles = @(
    'index.html',
    'styles.css',
    'portal-config.js',
    'app.js',
    'README.md'
)

foreach ($file in $requiredFiles) {
    $path = Join-Path $root $file
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Missing portal file: $file"
    }
}

$index = Get-Content -Raw -LiteralPath (Join-Path $root 'index.html')
$config = Get-Content -Raw -LiteralPath (Join-Path $root 'portal-config.js')

if ($index -notmatch '<title>Raynor Mods') {
    throw 'The public title is not branded as Raynor Mods.'
}
if ($index -notmatch 'data-link="curseforge"') {
    throw 'The CurseForge profile link is missing from the page.'
}
if ($config -notmatch 'https://www\.curseforge\.com/members/raynor_hytale/projects') {
    throw 'The official Raynor CurseForge profile is not configured.'
}
if ($index -match 'indioveloper') {
    throw 'A technical account name leaked into the public HTML.'
}
if ($index -match '\bTODO\b|\bPLACEHOLDER\b') {
  throw 'The page contains unfinished placeholder copy.'
}
if ($index -notmatch 'Hytale Update 6' -or $index -notmatch 'Hytale 0\.6\.2\+') {
    throw 'The portal does not advertise the current stable compatibility line.'
}
foreach ($version in @('0.5.3', '1.10.5', '1.3.2', '0.2.4', '0.1.1', '2.0.11', '1.1.2', '1.0.2', '1.0.1')) {
    if ($config -notmatch [regex]::Escape("version: `"$version`"")) {
        throw "Updated mod version is missing from the portal: $version"
    }
}

foreach ($script in @('portal-config.js', 'app.js')) {
    & node --check (Join-Path $root $script)
    if ($LASTEXITCODE -ne 0) {
        throw "JavaScript syntax validation failed: $script"
    }
}

$icons = [regex]::Matches($config, 'icon:\s*"([^"]+)"') | ForEach-Object { $_.Groups[1].Value }
if ($icons.Count -ne 6) {
    throw "Expected 6 mod icons, found $($icons.Count)."
}
foreach ($icon in $icons) {
    if (-not (Test-Path -LiteralPath (Join-Path $root $icon) -PathType Leaf)) {
        throw "Missing catalog icon: $icon"
    }
}

$sourcePaths = [regex]::Matches($config, 'source:\s*"([^"]+)"') | ForEach-Object { $_.Groups[1].Value }
foreach ($sourcePath in $sourcePaths) {
    $workspacePath = Join-Path (Split-Path (Split-Path $root -Parent) -Parent) $sourcePath
    if (-not (Test-Path -LiteralPath $workspacePath)) {
        throw "Catalog source does not exist: $sourcePath"
    }
}

$packSlugs = [regex]::Matches($config, 'slug:\s*"([^"]+)"') | ForEach-Object { $_.Groups[1].Value } | Select-Object -Last 5
if ($packSlugs.Count -ne 5 -or (Get-Content -Raw -LiteralPath (Join-Path $root 'app.js')) -notmatch 'id="pack-\$\{pack\.slug\}"') {
    throw 'Asset packs must expose stable public anchors.'
}

Write-Host "Raynor Mods portal OK: 6 mods, $($sourcePaths.Count - 6) asset packs, all local assets and source links present."
