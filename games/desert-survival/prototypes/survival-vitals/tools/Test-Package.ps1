param(
  [Parameter(Mandatory = $true)]
  [string]$ArchivePath,

  [string]$AssetsZip = "$env:APPDATA\Hytale\install\release\package\game\latest\Assets.zip"
)

$ErrorActionPreference = "Stop"
$archive = (Resolve-Path -LiteralPath $ArchivePath).Path
$assets = (Resolve-Path -LiteralPath $AssetsZip).Path

Add-Type -AssemblyName System.IO.Compression.FileSystem
$vanillaArchive = [System.IO.Compression.ZipFile]::OpenRead($assets)
$modArchive = [System.IO.Compression.ZipFile]::OpenRead($archive)
try {
  $required = @(
    "manifest.json",
    "gg/raynormods/desertsurvival/vitals/SurvivalVitalsPlugin.class",
    "Common/UI/Custom/Common.ui",
    "Common/UI/Custom/Hud/ReturnToHubButton.ui",
    "Common/UI/Custom/Hud/SurvivalVitals.ui",
    "Common/UI/Custom/Pages/DesertResearch/ResearchTree.ui",
    "Common/UI/Custom/Pages/DesertResearch/ResearchNodeRow.ui",
    "Common/UI/Custom/Pages/DesertResearch/IntelIcon.png",
    "Server/Item/Items/Armor/Armor_Stillsuit_Prototype.json",
    "Server/Item/Items/Consumable/Water_Flask_Prototype.json"
  )
  foreach ($path in $required) {
    if ($null -eq $modArchive.GetEntry($path)) {
      throw "Required archive entry is missing: $path"
    }
  }

  $vanillaEntries = @($vanillaArchive.Entries | Where-Object {
    $_.FullName.StartsWith("Common/UI/Custom/", [System.StringComparison]::Ordinal) -and $_.Name
  })
  if ($vanillaEntries.Count -lt 100) {
    throw "Assets.zip does not contain a complete vanilla Custom UI tree."
  }
  foreach ($entry in $vanillaEntries) {
    if ($null -eq $modArchive.GetEntry($entry.FullName)) {
      throw "Vanilla Custom UI dependency is missing from archive: $($entry.FullName)"
    }
  }

  $vanillaReturnToHub = $vanillaArchive.GetEntry("Common/UI/Custom/Hud/ReturnToHubButton.ui")
  $modReturnToHub = $modArchive.GetEntry("Common/UI/Custom/Hud/ReturnToHubButton.ui")
  $vanillaStream = $vanillaReturnToHub.Open()
  $modStream = $modReturnToHub.Open()
  try {
    $vanillaMemory = [System.IO.MemoryStream]::new()
    $modMemory = [System.IO.MemoryStream]::new()
    $vanillaStream.CopyTo($vanillaMemory)
    $modStream.CopyTo($modMemory)
    if ([System.Linq.Enumerable]::SequenceEqual(
      [byte[]]$vanillaMemory.ToArray(),
      [byte[]]$modMemory.ToArray()
    )) {
      throw "ReturnToHubButton.ui is byte-identical to vanilla and may be omitted from RequiredAssets."
    }
  }
  finally {
    $vanillaStream.Dispose()
    $modStream.Dispose()
    if ($null -ne $vanillaMemory) { $vanillaMemory.Dispose() }
    if ($null -ne $modMemory) { $modMemory.Dispose() }
  }
}
finally {
  $vanillaArchive.Dispose()
  $modArchive.Dispose()
}

Write-Output "Survival Vitals package check passed with complete Custom UI tree: $archive"
