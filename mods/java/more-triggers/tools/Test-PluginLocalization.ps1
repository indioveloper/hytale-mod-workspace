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

$required = @(
  "customUI.triggerVolumeEffectEditor.effectType.GiveRandomItem"
  "customUI.triggerVolumeEffectEditor.field.GiveRandomItem.Quantity"
  "customUI.triggerVolumeEffectEditor.field.GiveRandomItem.Quantity.tooltip"
  "customUI.triggerVolumeEffectEditor.field.GiveRandomItem.Quantity.placeholder"
  "customUI.triggerVolumeEffectEditor.field.GiveRandomItem.OverflowBehavior"
  "customUI.triggerVolumeEffectEditor.field.GiveRandomItem.OverflowBehavior.tooltip"
  "customUI.triggerVolumeEffectEditor.effectType.SendTagMessage"
  "customUI.triggerVolumeEffectEditor.field.SendTagMessage.Message"
  "customUI.triggerVolumeEffectEditor.field.SendTagMessage.Message.tooltip"
  "customUI.triggerVolumeEffectEditor.field.SendTagMessage.Message.placeholder"
  "customUI.triggerVolumeEffectEditor.field.SendTagMessage.Recipient"
  "customUI.triggerVolumeEffectEditor.field.SendTagMessage.TagSource"
  "customUI.triggerVolumeEffectEditor.field.SendTagMessage.TagSource.tooltip"
  "customUI.triggerVolumeEffectEditor.field.SendTagMessage.TagRadius"
  "customUI.triggerVolumeEffectEditor.field.SendTagMessage.TagRadius.tooltip"
  "customUI.triggerVolumeEffectEditor.effectType.ShowTagEventTitle"
  "customUI.triggerVolumeEffectEditor.field.ShowTagEventTitle.PrimaryTitle"
  "customUI.triggerVolumeEffectEditor.field.ShowTagEventTitle.PrimaryTitle.tooltip"
  "customUI.triggerVolumeEffectEditor.field.ShowTagEventTitle.PrimaryTitle.placeholder"
  "customUI.triggerVolumeEffectEditor.field.ShowTagEventTitle.SecondaryTitle"
  "customUI.triggerVolumeEffectEditor.field.ShowTagEventTitle.SecondaryTitle.tooltip"
  "customUI.triggerVolumeEffectEditor.field.ShowTagEventTitle.SecondaryTitle.placeholder"
  "customUI.triggerVolumeEffectEditor.field.ShowTagEventTitle.TagSource"
  "customUI.triggerVolumeEffectEditor.field.ShowTagEventTitle.TagSource.tooltip"
  "customUI.triggerVolumeEffectEditor.field.ShowTagEventTitle.TagRadius"
  "customUI.triggerVolumeEffectEditor.field.ShowTagEventTitle.TagRadius.tooltip"
  "customUI.triggerVolumeEffectEditor.effectType.ControlTimer"
  "customUI.triggerVolumeEffectEditor.field.ControlTimer.Action"
  "customUI.triggerVolumeEffectEditor.field.ControlTimer.Action.tooltip"
  "customUI.triggerVolumeEffectEditor.field.ControlTimer.DurationSeconds"
  "customUI.triggerVolumeEffectEditor.field.ControlTimer.DurationSeconds.tooltip"
  "customUI.triggerVolumeEffectEditor.field.ControlTimer.DurationSeconds.placeholder"
  "customUI.triggerVolumeEffectEditor.field.ControlTimer.Recipient"
  "customUI.triggerVolumeEffectEditor.field.ControlTimer.Recipient.tooltip"
  "customUI.triggerVolumeEffectEditor.effectType.ControlSignalLoop"
  "customUI.triggerVolumeEffectEditor.effectType.ExecuteCommand"
  "customUI.triggerVolumeEffectEditor.field.ExecuteCommand.Command"
  "customUI.triggerVolumeEffectEditor.field.ExecuteCommand.Command.tooltip"
  "customUI.triggerVolumeEffectEditor.field.ExecuteCommand.Command.placeholder"
  "customUI.triggerVolumeEffectEditor.field.ExecuteCommand.Executor"
  "customUI.triggerVolumeEffectEditor.field.ExecuteCommand.Executor.tooltip"
  "customUI.triggerVolumeEffectEditor.ruleType.NoMove"
  "customUI.triggerVolumeEffectEditor.ruleType.NoMove.tooltip"
  "customUI.triggerVolumeEffectEditor.field.NoMove.ExcludePlayers"
  "customUI.triggerVolumeEffectEditor.field.NoMove.ExcludePlayers.tooltip"
  "customUI.triggerVolumeEffectEditor.field.NoMove.ExcludedNpcRoles"
  "customUI.triggerVolumeEffectEditor.field.NoMove.ExcludedNpcRoles.tooltip"
)

foreach ($field in @(
  "Action", "LoopId", "IntervalSeconds", "FirstPulse", "StartBehavior",
  "DurationSeconds", "MaxPulses", "MatchKey", "MatchValue", "Radius", "Center",
  "SignalKeys", "SignalValues", "ContinueTagKey", "ContinueTagValue"
)) {
  $required += "customUI.triggerVolumeEffectEditor.field.ControlSignalLoop.$field"
  $required += "customUI.triggerVolumeEffectEditor.field.ControlSignalLoop.$field.tooltip"
}

foreach ($field in @(
  "LoopId", "IntervalSeconds", "DurationSeconds", "MaxPulses", "MatchKey", "MatchValue",
  "Radius", "ContinueTagKey", "ContinueTagValue"
)) {
  $required += "customUI.triggerVolumeEffectEditor.field.ControlSignalLoop.$field.placeholder"
}

foreach ($key in $required) {
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing required localized key: $key"
  }
}

foreach ($option in @("DROP_REMAINDER", "IGNORE_REMAINDER", "REQUIRE_FULL_STACK")) {
  $key = "customUI.triggerVolumeEffectEditor.field.GiveRandomItem.OverflowBehavior.option.$option"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing random item overflow option localization: $key"
  }
}

foreach ($option in @("TRIGGERING_PLAYER", "NEAREST_PLAYER", "PLAYERS_IN_VOLUME", "ALL_PLAYERS")) {
  $key = "customUI.triggerVolumeEffectEditor.field.SendTagMessage.Recipient.option.$option"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing recipient option localization: $key"
  }
}

foreach ($effect in @("SendTagMessage", "ShowTagEventTitle")) {
  foreach ($option in @("SELF", "EVENT", "RADIUS")) {
    $key = "customUI.triggerVolumeEffectEditor.field.$effect.TagSource.option.$option"
    if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
      throw "Missing tag source option localization: $key"
    }
  }
}

foreach ($action in @("START", "PAUSE", "RESUME", "SHOW", "HIDE", "CANCEL")) {
  $key = "customUI.triggerVolumeEffectEditor.field.ControlTimer.Action.option.$action"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing timer action option localization: $key"
  }
}

foreach ($recipient in @("TRIGGERING_PLAYER", "NEAREST_PLAYER", "PLAYERS_IN_VOLUME", "ALL_PLAYERS")) {
  $key = "customUI.triggerVolumeEffectEditor.field.ControlTimer.Recipient.option.$recipient"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing timer recipient option localization: $key"
  }
}

foreach ($action in @("START", "STOP", "PAUSE", "RESUME", "PULSE_NOW")) {
  $key = "customUI.triggerVolumeEffectEditor.field.ControlSignalLoop.Action.option.$action"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing signal loop action option localization: $key"
  }
}

foreach ($firstPulse in @("IMMEDIATE", "AFTER_INTERVAL")) {
  $key = "customUI.triggerVolumeEffectEditor.field.ControlSignalLoop.FirstPulse.option.$firstPulse"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing signal loop first-pulse option localization: $key"
  }
}

foreach ($behavior in @("IGNORE_IF_RUNNING", "RESTART", "REPLACE")) {
  $key = "customUI.triggerVolumeEffectEditor.field.ControlSignalLoop.StartBehavior.option.$behavior"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing signal loop start behavior localization: $key"
  }
}

foreach ($center in @("VOLUME", "ENTITY", "EVENT")) {
  $key = "customUI.triggerVolumeEffectEditor.field.ControlSignalLoop.Center.option.$center"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing signal loop center localization: $key"
  }
}

foreach ($executor in @("SERVER", "PLAYER")) {
  $key = "customUI.triggerVolumeEffectEditor.field.ExecuteCommand.Executor.option.$executor"
  if (-not $english.ContainsKey($key) -or -not $spanish.ContainsKey($key)) {
    throw "Missing ExecuteCommand executor option localization: $key"
  }
}

Write-Output "More Triggers localization check passed for 2 files."
