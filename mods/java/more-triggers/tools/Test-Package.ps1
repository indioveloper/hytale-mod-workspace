param(
  [Parameter(Mandatory = $true)]
  [string]$ArchivePath
)

$ErrorActionPreference = "Stop"
$archive = (Resolve-Path -LiteralPath $ArchivePath).Path
if ((Split-Path -Leaf $archive) -ne "More_Triggers-1_9_1.jar") {
  throw "Artifact name must match version 1.9.1: $archive"
}
$entries = @(jar tf $archive)
if ($LASTEXITCODE -ne 0) {
  throw "Could not inspect archive: $archive"
}

$required = @(
  "manifest.json"
  "icon-256.png"
  "gg/orbgenesis/moretriggers/MoreTriggersPlugin.class"
  "gg/orbgenesis/moretriggers/GiveRandomItemEffect.class"
  "gg/orbgenesis/moretriggers/RandomItemCandidateFilter.class"
  "gg/orbgenesis/moretriggers/ExecuteCommandEffect.class"
  "gg/orbgenesis/moretriggers/NoMoveRule.class"
  "gg/orbgenesis/moretriggers/NoMoveRuleSystem.class"
  "gg/orbgenesis/moretriggers/NoMoveExceptionFilter.class"
  "gg/orbgenesis/moretriggers/SendTagMessageEffect.class"
  "gg/orbgenesis/moretriggers/ShowTagEventTitleEffect.class"
  "gg/orbgenesis/moretriggers/timer/ControlTimerEffect.class"
  "gg/orbgenesis/moretriggers/timer/TimerCommand.class"
  "gg/orbgenesis/moretriggers/timer/CircularTimerHud.class"
  "Common/UI/Custom/HUD/CircularTimer.ui"
  "Common/UI/Custom/HUD/CircularTimer/CenterBackdrop.png"
  "Common/UI/Custom/HUD/CircularTimer/Frames/Ring00.png"
  "Common/UI/Custom/HUD/CircularTimer/Frames/Ring60.png"
  "Server/Languages/en-US/server.lang"
  "Server/Languages/es-ES/server.lang"
)
foreach ($entry in $required) {
  if ($entries -notcontains $entry) {
    throw "Required archive entry is missing: $entry"
  }
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($archive)
try {
  $manifestEntry = $zip.GetEntry("manifest.json")
  $reader = New-Object System.IO.StreamReader($manifestEntry.Open())
  try {
    $manifest = $reader.ReadToEnd() | ConvertFrom-Json
  } finally {
    $reader.Dispose()
  }
} finally {
  $zip.Dispose()
}
if ($manifest.Group -ne "OrbGenesis" -or $manifest.Name -ne "More Triggers") {
  throw "Unexpected plugin identity in manifest.json."
}
if ($manifest.Version -ne "1.9.1") {
  throw "Manifest version must be 1.9.1, found $($manifest.Version)."
}

$frames = @($entries | Where-Object { $_ -match '^Common/UI/Custom/HUD/CircularTimer/Frames/Ring\d{2}\.png$' })
if ($frames.Count -ne 61) {
  throw "Expected 61 timer ring frames, found $($frames.Count)."
}
if ($entries -contains "Common/UI/Custom/Common.ui") {
  throw "Dangerous vanilla Common.ui override found in archive."
}
$retiredClasses = @(
  "gg/orbgenesis/moretriggers/RemoveEventTitleEffect.class"
  "gg/orbgenesis/moretriggers/RandomTagSelectionEffect.class"
  "gg/orbgenesis/moretriggers/NoMoveEffect.class"
  "gg/orbgenesis/moretriggers/SetPlayerGravityViewEffect.class"
  "gg/orbgenesis/moretriggers/GravityViewController.class"
  "gg/orbgenesis/moretriggers/GravityViewCommand.class"
  "gg/orbgenesis/moretriggers/GravityMovementCompensationSystem.class"
)
foreach ($entry in $retiredClasses) {
  if ($entries -contains $entry) {
    throw "Retired effect class must not be packaged: $entry"
  }
}
Write-Output "More Triggers package check passed: $archive"
