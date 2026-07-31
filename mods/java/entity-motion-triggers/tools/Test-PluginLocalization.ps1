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
    if (-not $trimmed -or $trimmed.StartsWith("#")) {
      continue
    }
    if ($trimmed.StartsWith("server.")) {
      throw "Language keys must not include the automatic server. namespace: $trimmed"
    }
    $parts = $trimmed -split "\s*=\s*", 2
    if ($parts.Count -ne 2 -or -not $parts[1]) {
      throw "Invalid or empty language entry in ${path}: $trimmed"
    }
    if ($entries.ContainsKey($parts[0])) {
      throw "Duplicate language key in ${path}: $($parts[0])"
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

$effects = @(
  "ApplyHorizontalPlatformMotion"
  "StopHorizontalPlatformMotion"
  "ApplyPlayerPlatformCollision"
  "RemovePlayerPlatformCollision"
  "SpawnItems"
  "ConvertBlocksToEntities"
  "AttachMovingParticles"
)
foreach ($effect in $effects) {
  $key = "customUI.triggerVolumeEffectEditor.effectType.$effect"
  if (-not $english.ContainsKey($key)) {
    throw "Missing localized effect label: $key"
  }
}

$required = @(
  "customUI.triggerVolumeEffectEditor.field.ConvertBlocksToEntities.Item"
  "customUI.triggerVolumeEffectEditor.field.ConvertBlocksToEntities.Item.tooltip"
  "customUI.triggerVolumeEffectEditor.field.ConvertBlocksToEntities.Collision"
  "customUI.triggerVolumeEffectEditor.field.ConvertBlocksToEntities.Collision.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.ParticleSystem"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.ParticleSystem.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OffsetX"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OffsetX.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OffsetX.placeholder"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OffsetY"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OffsetY.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OffsetY.placeholder"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OffsetZ"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OffsetZ.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OffsetZ.placeholder"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.RotationX"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.RotationX.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.RotationY"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.RotationY.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.RotationZ"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.RotationZ.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.Scale"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.Scale.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.TargetNodeName"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.TargetNodeName.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.DetachEmittedParticles"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.DetachEmittedParticles.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.ClearOnEntityRemove"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.ClearOnEntityRemove.tooltip"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OnlyFirstMatch"
  "customUI.triggerVolumeEffectEditor.field.AttachMovingParticles.OnlyFirstMatch.tooltip"
)
foreach ($key in $required) {
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing required localized key: $key"
  }
}

Write-Output "Entity Motion Triggers localization check passed for 2 files."
