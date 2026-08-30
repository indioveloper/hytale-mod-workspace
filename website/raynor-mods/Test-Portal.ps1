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
$app = Get-Content -Raw -LiteralPath (Join-Path $root 'app.js')

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
if ($index -match 'GitHub' -or $app -match 'GitHub' -or $config -match 'github|releases|source:|build-battle|Build Battle') {
    throw 'The public portal must not expose GitHub links or the internal Build Battle project.'
}
if ($index -match '\bTODO\b|\bPLACEHOLDER\b') {
  throw 'The page contains unfinished placeholder copy.'
}
if ($index -notmatch 'Hytale 0\.6\.x') {
    throw 'The portal does not advertise the supported stable compatibility line.'
}
if ($index -match 'Más posibilidades|Menos límites|Prueba\. Construye|descargas') {
    throw 'Removed promotional sections returned to the minimal portal.'
}
if ($index -notmatch 'Asset packs <span>WIP</span>' -or $config -notmatch 'packs:\s*\[\]') {
    throw 'Private asset packs must remain hidden behind a WIP label.'
}
if ($index -notmatch 'data-language="es"' -or $index -notmatch 'data-language="en"' -or $app -notmatch 'params\.get\("lang"\)') {
    throw 'The bilingual ES/EN selector or shareable language query is missing.'
}
foreach ($version in @('0.5.3', '1.10.5', '1.3.2', '0.1.1', '2.0.11')) {
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
if ($icons.Count -ne 5) {
    throw "Expected 5 public mod icons, found $($icons.Count)."
}
foreach ($icon in $icons) {
    if (-not (Test-Path -LiteralPath (Join-Path $root $icon) -PathType Leaf)) {
        throw "Missing catalog icon: $icon"
    }
}

$modSlugs = [regex]::Matches($config, 'slug:\s*"([^"]+)"')
if ($modSlugs.Count -ne 5) {
    throw "Expected 5 public mods, found $($modSlugs.Count)."
}

$curseforgeLinks = [regex]::Matches($config, 'curseforge:\s*"https://www\.curseforge\.com/members/raynor_hytale/projects"')
if ($curseforgeLinks.Count -ne 6 -or $app -notmatch 'mod\.curseforge') {
    throw 'Every public mod must expose an editable CurseForge placeholder link.'
}
$englishSummaries = [regex]::Matches($config, 'summaryEn:\s*"[^"]+"')
$englishStatuses = [regex]::Matches($config, 'statusEn:\s*"[^"]+"')
if ($englishSummaries.Count -ne 5 -or $englishStatuses.Count -ne 5 -or $app -notmatch 'en:\s*\{') {
    throw 'Every public mod and interface section must have English copy.'
}

Write-Host 'Raynor Mods bilingual portal OK: ES/EN, 5 public mods, CurseForge placeholders present, private asset packs hidden.'
