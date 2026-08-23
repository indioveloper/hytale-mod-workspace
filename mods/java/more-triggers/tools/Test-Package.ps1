param(
  [Parameter(Mandatory = $true)]
  [string]$ArchivePath
)

$ErrorActionPreference = "Stop"
$archive = (Resolve-Path -LiteralPath $ArchivePath).Path
if ((Split-Path -Leaf $archive) -ne "More_Triggers-1_10_2.jar") {
  throw "Artifact name must match version 1.10.2: $archive"
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
  "gg/orbgenesis/moretriggers/PasteRandomPrefabEffect.class"
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
  "gg/orbgenesis/moretriggers/signalloop/ControlSignalLoopEffect.class"
  "gg/orbgenesis/moretriggers/signalloop/SignalLoopManager.class"
  "gg/orbgenesis/moretriggers/signalloop/SignalLoopSchedule.class"
  "gg/orbgenesis/moretriggers/signalloop/SignalLoopTickingSystem.class"
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
if ($manifest.Version -ne "1.10.2") {
  throw "Manifest version must be 1.10.2, found $($manifest.Version)."
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
$pluginBytecode = & javap -classpath $archive -c gg.orbgenesis.moretriggers.MoreTriggersPlugin
if ($LASTEXITCODE -ne 0) {
  throw "Could not inspect MoreTriggersPlugin bytecode."
}
if (-not ($pluginBytecode | Select-String -SimpleMatch "ExecuteCommand")) {
  throw "MoreTriggersPlugin does not register the ExecuteCommand effect ID."
}
if (-not ($pluginBytecode | Select-String -SimpleMatch "ControlSignalLoop")) {
  throw "MoreTriggersPlugin does not register the ControlSignalLoop effect ID."
}
$prefabBytecode = & javap -classpath $archive -c -p gg.orbgenesis.moretriggers.PasteRandomPrefabEffect
if ($LASTEXITCODE -ne 0 -or -not ($prefabBytecode | Select-String -SimpleMatch 'Field yaw:Lcom/hypixel/hytale/server/core/asset/type/blocktype/config/Rotation;')) {
  throw "PasteRandomPrefab does not expose or apply its Yaw rotation field."
}
if (-not ($prefabBytecode | Select-String -SimpleMatch 'extends com.hypixel.hytale.builtin.triggervolumes.effect.builtin.PastePrefabEffect')) {
  throw "PasteRandomPrefab must extend vanilla PastePrefabEffect so the inspector exposes Show/Hide Preview."
}
foreach ($previewMethod in @('getPrefabRelPath', 'getPosition', 'getOrigin', 'getRotation')) {
  if (-not ($prefabBytecode | Select-String -SimpleMatch $previewMethod)) {
    throw "PasteRandomPrefab does not expose vanilla preview method: $previewMethod"
  }
}
Write-Output "More Triggers package check passed: $archive"
