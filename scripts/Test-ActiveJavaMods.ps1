param(
  [string]$ServerJar = (Join-Path $env:APPDATA "Hytale\install\pre-release\package\game\latest\Server\HytaleServer.jar"),
  [string]$AssetsZip = (Join-Path $env:APPDATA "Hytale\install\pre-release\package\game\latest\Assets.zip")
)

$ErrorActionPreference = "Stop"
$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$buildScript = Join-Path $PSScriptRoot "Build-JavaMod.ps1"

if (-not (Test-Path -LiteralPath $ServerJar -PathType Leaf)) {
  throw "HytaleServer.jar not found: $ServerJar"
}

function Invoke-ProjectScript {
  param(
    [Parameter(Mandatory = $true)]
    [string]$RelativePath,
    [hashtable]$Parameters = @{}
  )

  $path = Join-Path $repo $RelativePath
  & $path @Parameters
  if ($null -ne $LASTEXITCODE -and $LASTEXITCODE -ne 0) {
    throw "Command failed with exit code ${LASTEXITCODE}: $RelativePath"
  }
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$serverArchive = [IO.Compression.ZipFile]::OpenRead($ServerJar)
try {
  $manifestEntry = $serverArchive.GetEntry("META-INF/MANIFEST.MF")
  if ($null -eq $manifestEntry) {
    throw "Server JAR does not contain META-INF/MANIFEST.MF."
  }
  $reader = [IO.StreamReader]::new($manifestEntry.Open())
  try {
    $serverManifest = $reader.ReadToEnd()
  } finally {
    $reader.Dispose()
  }
} finally {
  $serverArchive.Dispose()
}
$serverVersion = [regex]::Match($serverManifest, '(?m)^Implementation-Version:\s*(.+)$').Groups[1].Value.Trim()
$serverRevision = [regex]::Match($serverManifest, '(?m)^Implementation-Revision-Id:\s*(.+)$').Groups[1].Value.Trim()
Write-Host "Testing against Hytale $serverVersion ($serverRevision)"

Invoke-ProjectScript "mods\java\more-triggers\tools\Test-PluginLocalization.ps1"
Invoke-ProjectScript "mods\java\more-triggers\tools\Test-TagTemplateResolver.ps1"
Invoke-ProjectScript "mods\java\more-triggers\tools\Test-NoMoveExceptionFilter.ps1"
Invoke-ProjectScript "mods\java\more-triggers\tools\Test-RandomItemCandidateFilter.ps1"
Invoke-ProjectScript "mods\java\more-triggers\tools\Test-TimerMath.ps1"
Invoke-ProjectScript "mods\java\more-triggers\tools\Test-SignalLoopSchedule.ps1"
& $buildScript -ProjectPath (Join-Path $repo "mods\java\more-triggers") -SourceRoot src -PackageRoot src -ArtifactName More_Triggers-1_10_0.jar -ServerJar $ServerJar
Invoke-ProjectScript "mods\java\more-triggers\tools\Test-Package.ps1" @{ ArchivePath = (Join-Path $repo "mods\java\more-triggers\.build\dist\More_Triggers-1_10_0.jar") }

& $buildScript -ProjectPath (Join-Path $repo "mods\java\entity-motion-triggers") -SourceRoot src -PackageRoot src -ArtifactName Entity_Motion_Triggers-1_3_1.jar -ServerJar $ServerJar

Invoke-ProjectScript "mods\java\particle-shape-vfx\tools\Test-PluginLocalization.ps1"
Invoke-ProjectScript "mods\java\particle-shape-vfx\tools\Test-Geometry.ps1"
& $buildScript -ProjectPath (Join-Path $repo "mods\java\particle-shape-vfx") -SourceRoot src -PackageRoot src -ArtifactName Particle_Shape_VFX-0_1_0.jar -ServerJar $ServerJar
Invoke-ProjectScript "mods\java\particle-shape-vfx\tools\Test-Package.ps1" @{ ArchivePath = (Join-Path $repo "mods\java\particle-shape-vfx\.build\dist\Particle_Shape_VFX-0_1_0.jar") }

Invoke-ProjectScript "mods\java\scoreboards\tools\Test-PluginLocalization.ps1"
Invoke-ProjectScript "mods\java\scoreboards\tools\Test-AssetThreading.ps1"
Invoke-ProjectScript "mods\java\scoreboards\tools\Test-DynamicTranslations.ps1"
& $buildScript -ProjectPath (Join-Path $repo "mods\java\scoreboards") -SourceRoot src -PackageRoot . -AssetsRoot assets -ArtifactName Scoreboards-2_0_10.jar -ServerJar $ServerJar
Invoke-ProjectScript "mods\java\scoreboards\tools\Test-Package.ps1" @{ ArchivePath = (Join-Path $repo "mods\java\scoreboards\.build\dist\Scoreboards-2_0_10.jar") }

Invoke-ProjectScript "mods\java\build-battle\tools\Test-PluginLocalization.ps1"
& $buildScript -ProjectPath (Join-Path $repo "mods\java\build-battle") -SourceRoot src -PackageRoot . -ArtifactName Build_Battle-0_2_3.jar -ServerJar $ServerJar

& $buildScript -ProjectPath (Join-Path $repo "mods\asset-packs\raynor-npcs") -SourceRoot src -PackageRoot . -ArtifactName Raynor_NPCs-1_1_0.jar -ServerJar $ServerJar

Invoke-ProjectScript "mods\java\configurable-mob-spawners\tools\Test-PluginLocalization.ps1"
Invoke-ProjectScript "mods\java\configurable-mob-spawners\tools\Test-LightLevelMath.ps1"
Invoke-ProjectScript "mods\java\configurable-mob-spawners\tools\Test-ConfigString.ps1"
Invoke-ProjectScript "mods\java\configurable-mob-spawners\tools\web-configurator\Test-WebConfigurator.ps1"
& $buildScript -ProjectPath (Join-Path $repo "mods\java\configurable-mob-spawners") -SourceRoot src -PackageRoot . -AssetsRoot assets -VanillaCustomUiAssetsZip $AssetsZip -ArtifactName ConfigurableMobSpawners-0.4.0.jar -ServerJar $ServerJar
Invoke-ProjectScript "mods\java\configurable-mob-spawners\tools\Test-Package.ps1" @{ ArchivePath = (Join-Path $repo "mods\java\configurable-mob-spawners\.build\dist\ConfigurableMobSpawners-0.4.0.jar"); AssetsZip = $AssetsZip }

Write-Host "All active Java mods passed compatibility checks."
