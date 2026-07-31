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
  "gg/orbgenesis/moretriggers/MoreTriggersPlugin.class"
  "gg/orbgenesis/moretriggers/GiveRandomItemEffect.class"
  "gg/orbgenesis/moretriggers/ExecuteCommandEffect.class"
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
)
foreach ($entry in $retiredClasses) {
  if ($entries -contains $entry) {
    throw "Retired effect class must not be packaged: $entry"
  }
}
Write-Output "More Triggers package check passed: $archive"
