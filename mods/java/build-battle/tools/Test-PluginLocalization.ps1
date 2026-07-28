$ErrorActionPreference = 'Stop'

$languageRoot = Join-Path $PSScriptRoot '..\Server\Languages'
$files = @(Get-ChildItem -LiteralPath $languageRoot -Recurse -Filter *.lang)
if ($files.Count -lt 2) {
  throw "Expected at least en-US and es-ES localization files under $languageRoot"
}

$requiredKeys = @(
  'customUI.triggerVolumeEffectEditor.effectType.SuggestBuildTheme',
  'customUI.buildBattle.theme.title',
  'customUI.buildBattle.theme.prompt',
  'customUI.buildBattle.theme.placeholder',
  'customUI.buildBattle.theme.error',
  'customUI.buildBattle.theme.suggest',
  'customUI.buildBattle.theme.cancel'
)

foreach ($file in $files) {
  $lines = Get-Content -LiteralPath $file.FullName
  $invalidKeys = @($lines | Where-Object { $_ -match '^\s*server\.' })
  if ($invalidKeys.Count -gt 0) {
    throw "'$($file.FullName)' must not include the automatic 'server.' prefix."
  }

  foreach ($requiredKey in $requiredKeys) {
    if (-not ($lines | Where-Object { $_ -match ('^' + [regex]::Escape($requiredKey) + '\s*=') })) {
      throw "'$($file.FullName)' is missing localization key '$requiredKey'."
    }
  }
}

Write-Host "Build Battle localization check passed for $($files.Count) files."
