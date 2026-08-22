$ErrorActionPreference = "Stop"

$languageRoot = Join-Path $PSScriptRoot "..\assets\Server\Languages"
$paths = @(
  (Join-Path $languageRoot "en-US\server.lang")
  (Join-Path $languageRoot "es-ES\server.lang")
)

function Read-LanguageFile([string]$Path) {
  $entries = @{}
  foreach ($line in Get-Content -LiteralPath $Path) {
    $trimmed = $line.Trim()
    if (-not $trimmed -or $trimmed.StartsWith("#")) { continue }
    if ($trimmed.StartsWith("server.")) {
      throw "Language keys must not include the automatic server. namespace: $trimmed"
    }
    $parts = $trimmed -split "\s*=\s*", 2
    if ($parts.Count -ne 2 -or -not $parts[1]) {
      throw "Invalid or empty language entry in ${Path}: $trimmed"
    }
    $entries[$parts[0]] = $parts[1]
  }
  return $entries
}

$english = Read-LanguageFile $paths[0]
$spanish = Read-LanguageFile $paths[1]
$missingSpanish = @($english.Keys | Where-Object { -not $spanish.ContainsKey($_) })
$missingEnglish = @($spanish.Keys | Where-Object { -not $english.ContainsKey($_) })
if ($missingSpanish.Count -or $missingEnglish.Count) {
  throw "Language key mismatch. Missing es-ES: $($missingSpanish -join ', '); missing en-US: $($missingEnglish -join ', ')"
}

$requiredSuffixes = @("tag.tooltip", "timing.tooltip", "space.tooltip", "mob.tooltip", "loot.tooltip")
foreach ($suffix in $requiredSuffixes) {
  $key = "customUI.configurableSpawners.$suffix"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing required tooltip localization: $key"
  }
}

foreach ($suffix in @(
    "quickStep1",
    "quickStep2",
    "quickStep3",
    "copyUrl",
    "copyUrlReady",
    "copyUrlChat",
    "quickImport",
    "quickExport",
    "configure",
    "importedSaved",
    "exportReady"
  )) {
  $key = "customUI.configurableSpawners.$suffix"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing landing page localization: $key"
  }
}

foreach ($option in @("default", "hostile", "passive", "retaliate")) {
  $key = "customUI.configurableSpawners.aggression.$option"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing aggression option localization: $key"
  }
}
foreach ($option in @("default", "none", "add", "replace")) {
  $key = "customUI.configurableSpawners.loot.$option"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing loot option localization: $key"
  }
}

Write-Output "Configurable Mob Spawners localization check passed for 2 files."
