$ErrorActionPreference = "Stop"
$languageRoot = Join-Path $PSScriptRoot "..\src\Server\Languages"
$files = @(
  Join-Path $languageRoot "en-US\server.lang"
  Join-Path $languageRoot "es-ES\server.lang"
)

function Read-LanguageFile([string]$path) {
  $entries = @{}
  foreach ($line in Get-Content -LiteralPath $path) {
    $trimmed = $line.Trim()
    if (-not $trimmed -or $trimmed.StartsWith("#")) { continue }
    if ($trimmed.StartsWith("server.")) {
      throw "Language keys must not include the automatic server. namespace: $trimmed"
    }
    $parts = $trimmed -split "\s*=\s*", 2
    if ($parts.Count -ne 2 -or -not $parts[1]) {
      throw "Invalid or empty language entry in ${path}: $trimmed"
    }
    $entries[$parts[0]] = $parts[1]
  }
  return $entries
}

$english = Read-LanguageFile $files[0]
$spanish = Read-LanguageFile $files[1]
$missingSpanish = @($english.Keys | Where-Object { -not $spanish.ContainsKey($_) })
$missingEnglish = @($spanish.Keys | Where-Object { -not $english.ContainsKey($_) })
if ($missingSpanish.Count -or $missingEnglish.Count) {
  throw "Language key mismatch. Missing es-ES: $($missingSpanish -join ', '); missing en-US: $($missingEnglish -join ', ')"
}

$effect = "SpawnParticleShape"
$requiredFields = @("Shape", "ParticleSystem", "CoordinateMode", "Center", "Start", "End", "Size", "Spacing", "ParticleScale", "Duration", "MaxPoints")
if (-not $english.ContainsKey("customUI.triggerVolumeEffectEditor.effectType.$effect")) {
  throw "Missing effect name localization."
}
foreach ($field in $requiredFields) {
  foreach ($suffix in @("", ".tooltip")) {
    $key = "customUI.triggerVolumeEffectEditor.field.$effect.$field$suffix"
    if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
      throw "Missing localized field key: $key"
    }
  }
}
foreach ($option in @("CubeEdges", "SphereSurface", "Line")) {
  $key = "customUI.triggerVolumeEffectEditor.field.$effect.Shape.option.$option"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing shape option localization: $key"
  }
}
foreach ($option in @("RelativeToVolume", "Absolute")) {
  $key = "customUI.triggerVolumeEffectEditor.field.$effect.CoordinateMode.option.$option"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing coordinate option localization: $key"
  }
}
Write-Output "Particle Shape VFX localization check passed for 2 files."
