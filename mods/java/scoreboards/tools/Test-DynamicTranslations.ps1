$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$managerPath = Join-Path $projectRoot "src\gg\orbgenesis\scoreboards\ScoreboardManager.java"
$source = Get-Content -LiteralPath $managerPath -Raw

if ($source -notmatch "new UpdateTranslations\(UpdateType\.AddOrUpdate, translations\)") {
  throw "Editable Objective title/description translations are not sent to the client."
}

$startInstance = [regex]::Match(
  $source,
  "(?s)private Objective startInstance.*?(?=\r?\n  private void applyInitialValues)"
).Value
if (-not $startInstance) {
  throw "Could not locate startInstance for the dynamic-translation check."
}

$translationIndex = $startInstance.IndexOf("sendDefinitionTranslations")
$nativeStartIndex = $startInstance.IndexOf("plugin.startObjective")
if ($translationIndex -lt 0 -or $nativeStartIndex -lt 0 -or $translationIndex -gt $nativeStartIndex) {
  throw "Objective translations must be sent before ObjectivePlugin.startObjective."
}

$sendDisplay = [regex]::Match(
  $source,
  "(?s)private void sendDisplay.*?(?=\r?\n  private void sendDefinitionTranslations)"
).Value
if ($sendDisplay -match "objectiveTitleKey\s*=\s*Message\.raw") {
  throw "sendDisplay bypasses the native translation flow with a late title override."
}

Write-Output "Scoreboards dynamic-translation check passed."
