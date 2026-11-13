$ErrorActionPreference = "Stop"

$project = Split-Path -Parent $PSScriptRoot
$en = Get-Content -LiteralPath (Join-Path $project "assets\Server\Languages\en-US\server.lang")
$es = Get-Content -LiteralPath (Join-Path $project "assets\Server\Languages\es-ES\server.lang")

function Get-Keys([string[]]$lines) {
  return @($lines | Where-Object { $_ -match "^\s*[^#][^=]+=" } | ForEach-Object {
    ($_ -split "=", 2)[0].Trim()
  })
}

$enKeys = Get-Keys $en
$esKeys = Get-Keys $es
$missingEs = @($enKeys | Where-Object { $_ -notin $esKeys })
$missingEn = @($esKeys | Where-Object { $_ -notin $enKeys })
if ($missingEs.Count -or $missingEn.Count) {
  throw "Localization mismatch. Missing ES: $($missingEs -join ', '); missing EN: $($missingEn -join ', ')"
}

$invalid = @($enKeys + $esKeys | Where-Object { $_ -like "server.*" })
if ($invalid.Count) {
  throw "server.lang keys must not start with server.: $($invalid -join ', ')"
}

$pageSource = Get-Content -LiteralPath (Join-Path $project "src\gg\raynor\triggervolumebrowser\EffectBrowserInspectorPage.java") -Raw
if ($pageSource -match 'append\("@EffectType"') {
  throw "Effect rows must never gather @EffectType from the client. Send the type ID as literal event data instead."
}
if ($pageSource -notmatch 'append\("Id", ADD_MARKER \+ type\.id\(\)\)') {
  throw "Effect rows must carry their type ID as literal event data."
}
if ($pageSource -notmatch 'data\.effectType = data\.id\.substring\(ADD_MARKER\.length\(\)\)') {
  throw "The page must restore the literal type ID before delegating to the vanilla AddEffect handler."
}

$panelSource = Get-Content -LiteralPath (Join-Path $project "assets\Common\UI\Custom\Pages\TriggerVolumeEffectBrowser\EffectBrowserPanel.ui") -Raw
$headerSource = Get-Content -LiteralPath (Join-Path $project "assets\Common\UI\Custom\Pages\TriggerVolumeEffectBrowser\EffectBrowserHeader.ui") -Raw
$filterButtonSource = Get-Content -LiteralPath (Join-Path $project "assets\Common\UI\Custom\Pages\TriggerVolumeEffectBrowser\EffectBrowserFilterButton.ui") -Raw
if ($panelSource -match 'Top:\s*-') {
  throw "Interactive browser controls must not be shifted outside their parent with a negative top anchor."
}
if ($pageSource -notmatch 'cmd\.append\("#TitleTagChips", HEADER\)') {
  throw "The catalogue header must be placed in the vanilla title row."
}
if ($panelSource -match '#EffectBrowserModeAll' -or $headerSource -notmatch '#EffectBrowserModeAll') {
  throw "Browser mode buttons must live in the title header so they align with the vanilla tabs."
}
if ($panelSource -notmatch '#EffectBrowserSubFilters[\s\S]*Height:\s*140' -or
    $filterButtonSource -notmatch 'Width:\s*250') {
  throw "Category and mod filters must reserve four complete, wide rows without scrolling."
}
if ($pageSource -notmatch 'cmd\.set\(row \+ " #EffectId\.Text", descriptionFor\(type\)\)') {
  throw "Catalogue rows must show a short description instead of the technical ID."
}
$descriptionBlock = [regex]::Match(
  $pageSource,
  'KNOWN_DESCRIPTION_IDS\s*=\s*java\.util\.Set\.of\(([\s\S]*?)\);'
)
if (-not $descriptionBlock.Success) {
  throw "Could not find the known-description ID registry."
}
$descriptionIds = @([regex]::Matches($descriptionBlock.Groups[1].Value, '"([^"]+)"') | ForEach-Object {
  $_.Groups[1].Value
})
foreach ($descriptionId in $descriptionIds) {
  $key = "customUI.triggerEffectBrowser.description.$descriptionId"
  if ($key -notin $enKeys -or $key -notin $esKeys) {
    throw "Missing localized one-line catalogue description for $descriptionId."
  }
}
if ($pageSource -match '#EffectBrowserSubFilters\[" \+ index \+ "\] #FilterButton') {
  throw "Dynamic filter entries are root buttons; do not target a nonexistent nested #FilterButton."
}
if ($pageSource -notmatch 'cmd\.set\(selector \+ "\.Text", label\)' -or
    $pageSource -notmatch 'CustomUIEventBindingType\.Activating,\s*selector,') {
  throw "Dynamic filter properties and events must target the appended root element."
}

$toolPath = Join-Path $project "assets\Server\Item\Items\EditorTool\Raynor_TriggerVolumeBrowserTool.json"
$tool = Get-Content -LiteralPath $toolPath -Raw | ConvertFrom-Json
if ($tool.BuilderTool.Id -ne "TriggerVolume") {
  throw "The companion item must preserve BuilderTool.Id=TriggerVolume."
}
if ($null -ne $tool.Interactions.SwapTo) {
  throw "The companion item must not open a page on SwapTo."
}
if ($null -ne $tool.Interactions.Use) {
  throw "The TriggerVolume client context owns E; the clone must not declare an unreachable Use interaction."
}
if ($tool.Interactions.Primary -ne "Builder_Tool" -or $tool.Interactions.Secondary -ne "Builder_Tool") {
  throw "Primary and Secondary must preserve the vanilla Builder_Tool interactions."
}

$pluginSource = Get-Content -LiteralPath (Join-Path $project "src\gg\raynor\triggervolumebrowser\TriggerVolumeEffectBrowserPlugin.java") -Raw
$bridgeSource = Get-Content -LiteralPath (Join-Path $project "src\gg\raynor\triggervolumebrowser\TriggerVolumeEffectsBridgeCommand.java") -Raw
if ($pluginSource -notmatch 'subCommands\.put\("effects", bridge\)' -or
    $bridgeSource -notmatch 'CLONE_ITEM_ID\.equals\(heldItem\.getItemId\(\)\)') {
  throw "E must route /triggervolume effects to the custom inspector only while the cloned item is held."
}

Write-Output "Localization OK: $($enKeys.Count) keys in en-US and es-ES."
