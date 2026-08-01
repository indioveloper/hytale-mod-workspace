param(
  [Parameter(Mandatory = $true)]
  [string]$ArchivePath
)

$ErrorActionPreference = "Stop"
$archive = (Resolve-Path -LiteralPath $ArchivePath).Path
$entries = @(jar tf $archive)
if ($LASTEXITCODE -ne 0) {
  throw "Could not inspect archive: $archive"
}

$required = @(
  "manifest.json"
  "icon-256.png"
  "gg/orbgenesis/scoreboards/ScoreboardsPlugin.class"
  "gg/orbgenesis/scoreboards/ManualCountObjectiveTask.class"
  "gg/orbgenesis/scoreboards/ControlScoreboardEffect.class"
  "gg/orbgenesis/scoreboards/ModifyScoreboardTaskEffect.class"
  "Common/UI/Custom/Pages/Scoreboards/ScoreboardList.ui"
  "Common/UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui"
  "Server/Languages/en-US/server.lang"
  "Server/Languages/es-ES/server.lang"
)
foreach ($entry in $required) {
  if ($entries -notcontains $entry) {
    throw "Required archive entry is missing: $entry"
  }
}

if ($entries -contains "Common/UI/Custom/Common.ui") {
  throw "Dangerous vanilla Common.ui override found in archive."
}

Write-Output "Scoreboards package check passed: $archive"
