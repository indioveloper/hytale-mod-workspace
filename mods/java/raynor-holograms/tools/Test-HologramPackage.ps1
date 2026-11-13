param(
  [string]$ProjectPath = (Split-Path -Parent $PSScriptRoot),
  [string]$JarPath
)

$ErrorActionPreference = "Stop"
$manifestPath = Join-Path $ProjectPath "manifest.json"
$manifest = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json
if ($manifest.Group -ne "RaynorMods" -or $manifest.Name -ne "RaynorHolograms") {
  throw "Unexpected plugin identity in $manifestPath"
}
if ($manifest.ServerVersion -ne ">=0.6.2 <0.7.0" -or $manifest.Version -ne "0.8.3") {
  throw "Unexpected compatibility/version: $($manifest.ServerVersion), $($manifest.Version)"
}

$editorRelative = "Common/UI/Custom/Pages/RaynorHolograms/HologramEditorV081.ui"
$requiredAssets = @(
  $editorRelative,
  "Common/NPC/RaynorHolograms/ColorAtlas.png",
  "Common/NPC/RaynorHolograms/MediaAtlas.png",
  "Common/NPC/RaynorHolograms/Transparent.png",
  "Common/Items/Raynor/Holograms/Placeholder_Orb.blockymodel",
  "Server/Models/Raynor/Holograms/Raynor_Hologram_Invisible_Anchor.json",
  "Server/Models/Raynor/Holograms/Raynor_Hologram_Media_Selection_Proxy.json",
  "Server/Models/Raynor/Holograms/Raynor_Hologram_Pixel_Selection_Proxy.json",
  "Samples/prueba.gif",
  "Server/Languages/en-US/server.lang",
  "Server/Languages/es-ES/server.lang"
)
foreach ($relative in $requiredAssets) {
  if (-not (Test-Path -LiteralPath (Join-Path $ProjectPath $relative) -PathType Leaf)) {
    throw "Missing hologram asset: $relative"
  }
}

$javaPath = Join-Path $ProjectPath "src/gg/raynormods/holograms/HologramEditorPage.java"
$java = Get-Content -LiteralPath $javaPath -Raw
$imageService = Get-Content -LiteralPath (Join-Path $ProjectPath "src/gg/raynormods/holograms/ImageHologramService.java") -Raw
$imageSystem = Get-Content -LiteralPath (Join-Path $ProjectPath "src/gg/raynormods/holograms/HologramEntitySupportSystems.java") -Raw
$particleAssets = Get-Content -LiteralPath (Join-Path $ProjectPath "src/gg/raynormods/holograms/ParticleHologramAssets.java") -Raw
$editor = Get-Content -LiteralPath (Join-Path $ProjectPath $editorRelative) -Raw
if (-not $java.Contains('commands.append("Pages/RaynorHolograms/HologramEditorV081.ui")')) {
  throw "HologramEditorPage does not use the versioned 0.8.1 UI document"
}

# Every selector referenced by Java must exist in the appended document. This
# catches the exact mismatch that disconnects clients on a CustomUI Set command.
$selectorMatches = [regex]::Matches($java, '"(#[A-Za-z][A-Za-z0-9_]*)')
$selectors = @($selectorMatches | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
foreach ($selector in $selectors) {
  if (-not $editor.Contains($selector)) {
    throw "Java references missing Custom UI selector: $selector"
  }
}
if (-not $selectors.Contains("#NativePreview")) {
  throw "Native preview selector is not covered by the UI contract test"
}
if ($java -notmatch 'private\s+static\s+String\s+nativePreview\s*\(' -or
    $java -match 'set\("#NativePreview\.Text",\s*Message') {
  throw "NativePreview.Text must receive a String in Update 6"
}
if ($imageService -match 'ParticleHologramAssets\.ensure\s*\(') {
  throw "Image creation must not decode or refresh GIF assets on the world thread"
}
if ($imageSystem -notmatch 'beginParticlePreparation\s*\(' -or
    $particleAssets -notmatch 'CompletableFuture\.supplyAsync') {
  throw "External image preparation is not delegated to the server executor"
}
if ($editor -match "VerticalAlignment\s*:\s*(Top|Bottom)" -or
    $editor -match "HorizontalAlignment\s*:\s*(Left|Right)") {
  throw "Unsupported LabelAlignment value in $editorRelative"
}

Get-ChildItem -LiteralPath (Join-Path $ProjectPath "Server/Models/Raynor/Holograms") -Filter *.json -Recurse |
  ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw | ConvertFrom-Json | Out-Null }

if ($JarPath) {
  if (-not (Test-Path -LiteralPath $JarPath -PathType Leaf)) { throw "JAR not found: $JarPath" }
  $entries = & jar tf $JarPath
  foreach ($relative in @("manifest.json", $editorRelative, "Samples/prueba.gif")) {
    if ($entries -notcontains $relative) { throw "Built JAR is missing: $relative" }
  }
  if ($entries -contains "Common/UI/Custom/Pages/RaynorHolograms/HologramEditor.ui") {
    throw "Built JAR still contains the cache-colliding 0.8.0 UI path"
  }
}

Write-Output "Raynor Holograms 0.8.3 package, UI contract and async GIF pipeline passed."
