param(
  [Parameter(Mandatory = $true)]
  [string]$ArchivePath
)

$ErrorActionPreference = "Stop"
$archive = (Resolve-Path -LiteralPath $ArchivePath).Path

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($archive)
try {
  $entries = @($zip.Entries | ForEach-Object { $_.FullName.Replace("\", "/") })
  $required = @(
    "manifest.json"
    "icon-256.png"
    "gg/orbgenesis/particleshapevfx/ParticleShapeVfxPlugin.class"
    "gg/orbgenesis/particleshapevfx/SpawnParticleShapeEffect.class"
    "gg/orbgenesis/particleshapevfx/ParticleShapePointGenerator.class"
    "Server/Particles/OrbGenesis/Shapes/OrbGenesis_Shape_Point_Red.particlesystem"
    "Server/Particles/OrbGenesis/Shapes/Spawners/OrbGenesis_Shape_Point_Red.particlespawner"
    "Server/Particles/OrbGenesis/Shapes/OrbGenesis_Shape_Point_YellowOrange.particlesystem"
    "Server/Particles/OrbGenesis/Shapes/Spawners/OrbGenesis_Shape_Point_YellowOrange.particlespawner"
    "Server/Particles/OrbGenesis/Shapes/OrbGenesis_Shape_Point_Green.particlesystem"
    "Server/Particles/OrbGenesis/Shapes/Spawners/OrbGenesis_Shape_Point_Green.particlespawner"
    "Server/Languages/en-US/server.lang"
    "Server/Languages/es-ES/server.lang"
  )
  foreach ($entry in $required) {
    if ($entries -notcontains $entry) {
      throw "Required archive entry is missing: $entry"
    }
  }

  $manifestEntry = $zip.GetEntry("manifest.json")
  $reader = New-Object System.IO.StreamReader($manifestEntry.Open())
  try {
    $manifest = $reader.ReadToEnd() | ConvertFrom-Json
  } finally {
    $reader.Dispose()
  }
  if ($manifest.Group -ne "OrbGenesis" -or
      $manifest.Name -ne "Particle Shape VFX" -or
      $manifest.Version -ne "0.1.1" -or
      $manifest.IncludesAssetPack -ne $true) {
    throw "Archive manifest does not identify Particle Shape VFX 0.1.1."
  }
} finally {
  $zip.Dispose()
}

Write-Output "Particle Shape VFX package check passed: $archive"
