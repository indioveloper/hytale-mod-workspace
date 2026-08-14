param(
  [Parameter(Mandatory = $true)]
  [string]$ArchivePath,

  [string]$AssetsZip = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Assets.zip"
)

$ErrorActionPreference = "Stop"
$archive = (Resolve-Path -LiteralPath $ArchivePath).Path
$entries = @(jar tf $archive)
if ($LASTEXITCODE -ne 0) { throw "Could not inspect archive: $archive" }

$required = @(
  "manifest.json"
  "icon-256.png"
  "gg/orbgenesis/configurablespawners/ConfigurableMobSpawnersPlugin.class"
  "gg/orbgenesis/configurablespawners/SpawnerEditorPage.class"
  "gg/orbgenesis/configurablespawners/SpawnerTickSystem.class"
  "gg/orbgenesis/configurablespawners/SpawnerVisualState.class"
  "gg/orbgenesis/configurablespawners/SpawnerLightLevel.class"
  "gg/orbgenesis/configurablespawners/SpawnerConfigString.class"
  "gg/orbgenesis/configurablespawners/SpawnerAttitudeSystem.class"
  "gg/orbgenesis/configurablespawners/SpawnerLootSystem.class"
  "Common/UI/Custom/Common.ui"
  "Common/UI/Custom/Hud/ReturnToHubButton.ui"
  "Common/UI/Custom/Pages/ConfigurableSpawners/SpawnerEditor.ui"
  "Common/Blocks/OrbGenesis/Configurable_Mob_Spawner.blockymodel"
  "Common/Items/OrbGenesis/Configurable_Mob_Spawner_Texture.png"
  "Common/Items/OrbGenesis/Configurable_Mob_Spawner_Texture_Off.png"
  "Server/Item/Items/OrbGenesis/OrbGenesis_Configurable_Mob_Spawner.json"
  "Server/Languages/en-US/server.lang"
  "Server/Languages/es-ES/server.lang"
)
foreach ($entry in $required) {
  if ($entries -notcontains $entry) { throw "Required archive entry is missing: $entry" }
}
if (-not (Test-Path -LiteralPath $AssetsZip -PathType Leaf)) {
  throw "Hytale Assets.zip not found: $AssetsZip"
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$vanillaArchive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $AssetsZip).Path)
$modArchive = [System.IO.Compression.ZipFile]::OpenRead($archive)
try {
  $vanillaEntries = @($vanillaArchive.Entries | Where-Object {
    $_.FullName.StartsWith("Common/UI/Custom/", [System.StringComparison]::Ordinal) -and $_.Name
  })
  if ($vanillaEntries.Count -lt 100) {
    throw "Assets.zip does not contain a complete vanilla Custom UI tree."
  }

  $sha256 = [System.Security.Cryptography.SHA256]::Create()
  try {
    foreach ($vanillaEntry in $vanillaEntries) {
      $modEntry = $modArchive.GetEntry($vanillaEntry.FullName)
      if ($null -eq $modEntry) {
        throw "Vanilla Custom UI dependency is missing from archive: $($vanillaEntry.FullName)"
      }
      $vanillaStream = $vanillaEntry.Open()
      $modStream = $modEntry.Open()
      try {
        $vanillaHash = [BitConverter]::ToString($sha256.ComputeHash($vanillaStream)).Replace("-", "")
        $modHash = [BitConverter]::ToString($sha256.ComputeHash($modStream)).Replace("-", "")
      }
      finally {
        $vanillaStream.Dispose()
        $modStream.Dispose()
      }
      if ($vanillaHash -ne $modHash) {
        throw "Packaged vanilla Custom UI asset differs from Assets.zip: $($vanillaEntry.FullName)"
      }
    }
  }
  finally {
    $sha256.Dispose()
  }
}
finally {
  $vanillaArchive.Dispose()
  $modArchive.Dispose()
}

$assetPath = Join-Path $PSScriptRoot "..\assets\Server\Item\Items\OrbGenesis\OrbGenesis_Configurable_Mob_Spawner.json"
$asset = Get-Content -Raw -LiteralPath $assetPath | ConvertFrom-Json
if ($asset.BlockType.BlockEntity.Components.PSObject.Properties.Name -notcontains "OrbGenesis_ConfigurableMobSpawner") {
  throw "Spawner block component is missing from the item asset."
}
if ($asset.BlockType.Interactions.Use.Interactions[0].Page.Id -ne "OrbGenesis_ConfigurableMobSpawner") {
  throw "Spawner editor page ID does not match the registered page."
}
if ($asset.BlockType.DrawType -ne "Model" -or -not $asset.BlockType.CustomModel) {
  throw "Spawner block must use its custom model."
}
if ($asset.BlockType.BlockSoundSetId -ne "Metal" -or
    $asset.BlockType.BlockParticleSetId -ne "Metal" -or
    $asset.BlockType.PhysicalMaterialId -ne "Metal") {
  throw "Spawner block must use the metal impact material, sound and particles."
}
if ($asset.BlockType.State.Definitions.Off.CustomModelTexture[0].Texture -ne
    "Items/OrbGenesis/Configurable_Mob_Spawner_Texture_Off.png") {
  throw "Disabled spawners must use the red texture variant."
}
if ($asset.BlockType.State.Definitions.On.CustomModelTexture[0].Texture -ne
    "Items/OrbGenesis/Configurable_Mob_Spawner_Texture.png") {
  throw "Enabled spawners must use the cyan texture variant."
}

$uiPath = Join-Path $PSScriptRoot "..\assets\Common\UI\Custom\Pages\ConfigurableSpawners\SpawnerEditor.ui"
$uiSource = Get-Content -Raw -LiteralPath $uiPath
if ($uiSource -match '(?s)\$C\.@TextButton\s+#[^{]+\{[^}]*@Text\s*=') {
  throw "TextButton instances must set Text: directly; @Text is not an override declared by the vanilla template."
}

$pageSourcePath = Join-Path $PSScriptRoot "..\src\gg\orbgenesis\configurablespawners\SpawnerEditorPage.java"
$pageSource = Get-Content -Raw -LiteralPath $pageSourcePath
if ($pageSource -match 'set\("#[^"]+\.Text",\s*Message\.raw\(') {
  throw "Raw label text must be sent as String; Message.raw is not assignable to Custom UI Text in pre.11."
}
if ($pageSource -match 'static final String ACTION\s*=\s*"@Action"') {
  throw "Literal Custom UI event keys must not start with @; that prefix gathers a selector property."
}

Write-Output "Configurable Mob Spawners package check passed: $archive"
