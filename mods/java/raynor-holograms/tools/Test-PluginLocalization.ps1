$ErrorActionPreference = "Stop"

$languageRoot = Join-Path $PSScriptRoot "..\Server\Languages"
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

Write-Output "Raynor Holograms localization check passed for 2 files and $($english.Count) keys."
