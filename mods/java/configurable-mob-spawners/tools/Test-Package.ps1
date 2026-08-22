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
  "gg/orbgenesis/configurablespawners/SpawnerMobProfile.class"
  "gg/orbgenesis/configurablespawners/SpawnerMobScale.class"
  "gg/orbgenesis/configurablespawners/SpawnerElitePresentationSystem.class"
  "gg/orbgenesis/configurablespawners/SpawnerAttitudeSystem.class"
  "gg/orbgenesis/configurablespawners/SpawnerLootSystem.class"
  "Common/UI/Custom/Common.ui"
  "Common/UI/Custom/Hud/ReturnToHubButton.ui"
  "Common/UI/Custom/Pages/ConfigurableSpawners/SpawnerLanding.ui"
  "Common/UI/Custom/Pages/ConfigurableSpawners/SpawnerEditor.ui"
  "Common/Blocks/OrbGenesis/Configurable_Mob_Spawner.blockymodel"
  "Common/Items/OrbGenesis/Configurable_Mob_Spawner_Texture.png"
  "Common/Items/OrbGenesis/Configurable_Mob_Spawner_Texture_Off.png"
  "Server/Item/Items/OrbGenesis/OrbGenesis_Configurable_Mob_Spawner.json"
  "Server/Languages/en-US/server.lang"
  "Server/Languages/es-ES/server.lang"
  "Server/Entity/Effects/OrbGenesis/SpawnerSpeed/OrbGenesis_SpawnerSpeed_000.json"
  "Server/Entity/Effects/OrbGenesis/SpawnerSpeed/OrbGenesis_SpawnerSpeed_030.json"
  "Server/Entity/Effects/OrbGenesis/OrbGenesis_Spawner_Elite.json"
  "Server/Entity/ModelVFX/OrbGenesis_Spawner_Elite.json"
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
if ($asset.Quality -eq "Developer" -or $asset.Quality -eq "Debug") {
  throw "Spawner Block must be visible in the normal creative inventory."
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

foreach ($uiName in @("SpawnerLanding.ui", "SpawnerEditor.ui")) {
  $uiPath = Join-Path $PSScriptRoot "..\assets\Common\UI\Custom\Pages\ConfigurableSpawners\$uiName"
  $uiSource = Get-Content -Raw -LiteralPath $uiPath
  if ($uiSource -match '(?s)\$C\.@TextButton\s+#[^{]+\{[^}]*@Text\s*=') {
    throw "TextButton instances must set Text: directly; @Text is not an override declared by the vanilla template."
  }
}

$pageSourcePath = Join-Path $PSScriptRoot "..\src\gg\orbgenesis\configurablespawners\SpawnerEditorPage.java"
$pageSource = Get-Content -Raw -LiteralPath $pageSourcePath
if ($pageSource -match 'set\("#[^"]+\.Text",\s*Message\.raw\(') {
  throw "Raw label text must be sent as String; Message.raw is not assignable to Custom UI Text in pre.11."
}
if ($pageSource -match 'static final String ACTION\s*=\s*"@Action"') {
  throw "Literal Custom UI event keys must not start with @; that prefix gathers a selector property."
}
foreach ($requiredSource in @(
    'Pages/ConfigurableSpawners/SpawnerLanding.ui',
    '"QUICK_IMPORT"',
    '"COPY_URL"',
    '"EXPORT"',
    '"CONFIGURE"'
  )) {
  if (-not $pageSource.Contains($requiredSource)) {
    throw "Landing page behavior is missing from SpawnerEditorPage: $requiredSource"
  }
}
$landingUiPath = Join-Path $PSScriptRoot "..\assets\Common\UI\Custom\Pages\ConfigurableSpawners\SpawnerLanding.ui"
$landingUi = Get-Content -Raw -LiteralPath $landingUiPath
if ($landingUi.Contains('#QuickExportButton')) {
  throw "Export must only be available from the full configuration editor."
}
if (-not $landingUi.Contains('#ConfiguratorUrl') -or
    -not $landingUi.Contains('#CopyUrlButton') -or
    -not $landingUi.Contains('IsReadOnly: true')) {
  throw "The landing page must show the configurator URL beside its copy helper."
}
$editorUiPath = Join-Path $PSScriptRoot "..\assets\Common\UI\Custom\Pages\ConfigurableSpawners\SpawnerEditor.ui"
$editorUi = Get-Content -Raw -LiteralPath $editorUiPath
if (-not $editorUi.Contains('#ExportButton')) {
  throw "The full configuration editor must expose Export Spawner."
}
foreach ($requiredSpeedUi in @('#SpeedSlider', '#SpeedValue', 'mobSpeed.tooltip')) {
  if (-not $editorUi.Contains($requiredSpeedUi)) {
    throw "The full editor is missing the speed control: $requiredSpeedUi"
  }
}
$speedEffects = @($entries | Where-Object {
  $_ -like 'Server/Entity/Effects/OrbGenesis/SpawnerSpeed/OrbGenesis_SpawnerSpeed_*.json'
})
if ($speedEffects.Count -ne 30) {
  throw "Expected 30 discrete spawner speed effects; found $($speedEffects.Count)."
}
foreach ($requiredSource in @('SpawnerMobScale.apply', 'addInfiniteEffect', 'invalidateCachedHorizontalSpeedMultiplier')) {
  $setupSourcePath = Join-Path $PSScriptRoot "..\src\gg\orbgenesis\configurablespawners\SpawnerMobSetupSystem.java"
  $setupSource = Get-Content -Raw -LiteralPath $setupSourcePath
  if (-not $setupSource.Contains($requiredSource)) {
    throw "Runtime mob setup is missing: $requiredSource"
  }
}
$scaleSourcePath = Join-Path $PSScriptRoot "..\src\gg\orbgenesis\configurablespawners\SpawnerMobScale.java"
$scaleSource = Get-Content -Raw -LiteralPath $scaleSourcePath
foreach ($requiredScaleSource in @('Model.createScaledModel', 'setInitialModelScale', 'updateMotionControllers', 'legacyScale.setScale(1.0f)')) {
  if (-not $scaleSource.Contains($requiredScaleSource)) {
    throw "Physical model scaling is missing: $requiredScaleSource"
  }
}
if ($pageSource.Contains('EntityScaleComponent')) {
  throw "The preview must use the same native scaled model as spawned NPCs."
}
$componentSource = Get-Content -Raw -LiteralPath (Join-Path $PSScriptRoot "..\src\gg\orbgenesis\configurablespawners\ConfigurableSpawnerComponent.java")
$tickSource = Get-Content -Raw -LiteralPath (Join-Path $PSScriptRoot "..\src\gg\orbgenesis\configurablespawners\SpawnerTickSystem.java")
if (-not $componentSource.Contains('String roleId = "";') -or
    -not $tickSource.Contains('SpawnerVisualState.synchronize(world, blockPosition, configured)')) {
  throw "New spawner blocks must remain unconfigured and visually disabled until a role is saved."
}
if ($editorUi.Contains('#EnabledCheck') -or $pageSource.Contains('PageData.ENABLED') -or
    $tickSource.Contains('!config.enabled') -or $tickSource.Contains('config.enabled && configured')) {
  throw "The redundant manual enabled toggle must not remain in the editor or runtime gate."
}
foreach ($requiredRadiusLimit in @('countTrackedWithinRadius', 'TransformComponent.getComponentType()',
    'config.maxAlive - countTrackedWithinRadius(config, world, center)',
    'transform.getPosition().distanceSquared(center) <= radiusSquared')) {
  if (-not $tickSource.Contains($requiredRadiusLimit)) {
    throw "Max alive must count this spawner's living mobs inside its activation radius: $requiredRadiusLimit"
  }
}
foreach ($requiredCompoundSource in @('config.selectProfile(', 'profile.rollElite(',
    'new SpawnedBySpawnerComponent(config.spawnerId, profile, elite)',
    'profile.elitePrefix.toLowerCase')) {
  if (-not $tickSource.Contains($requiredCompoundSource)) {
    throw "Compound/elite spawning is missing: $requiredCompoundSource"
  }
}
$presentationSource = Get-Content -Raw -LiteralPath (Join-Path $PSScriptRoot "..\src\gg\orbgenesis\configurablespawners\SpawnerElitePresentationSystem.java")
foreach ($requiredPresentationSource in @('RoleSystems.PostBehaviourSupportTickSystem.class',
    'DisplayNameSupport.setDisplayName', 'EventTitleUtil.showEventTitleToPlayer',
    'server.configurableSpawners.elite.nearby')) {
  if (-not $presentationSource.Contains($requiredPresentationSource)) {
    throw "Elite presentation is missing: $requiredPresentationSource"
  }
}
if (-not $pageSource.Contains('case "QUICK_IMPORT" -> importAndSave(data.config)') -or
    -not $pageSource.Contains('Message.translation("server.customUI.configurableSpawners.importedSaved"));') -or
    -not $pageSource.Contains('close();')) {
  throw "The landing save action must persist the CMS1 configuration and close the page."
}

$pluginSourcePath = Join-Path $PSScriptRoot "..\src\gg\orbgenesis\configurablespawners\ConfigurableMobSpawnersPlugin.java"
$pluginSource = Get-Content -Raw -LiteralPath $pluginSourcePath
if (-not $pluginSource.Contains('Busca en creativo el bloque \"Spawner Block\"') -or
    -not $pluginSource.Contains('https://hytale-mob-spawner-configurator.vercel.app')) {
  throw "Startup guidance must name Spawner Block and the web configurator URL."
}
if (-not $pluginSource.Contains('registerGlobal(PlayerReadyEvent.class, this::onPlayerReady)') -or
    -not $pluginSource.Contains('Message.raw(CONFIGURATOR_URL).link(CONFIGURATOR_URL)')) {
  throw "Each ready player must receive the clickable install guidance in chat."
}
if (-not $pageSource.Contains('new OpenChatWithCommand(ConfigurableMobSpawnersPlugin.CONFIGURATOR_URL)')) {
  throw "Copy URL must prepare the client chat field because server Custom UI cannot access the client clipboard."
}

Write-Output "Configurable Mob Spawners package check passed: $archive"
