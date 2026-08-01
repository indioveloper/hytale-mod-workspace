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
    "Server/Item/Items/OrbGenesis/NoBuildStone/OrbGenesis_NoBuild_Stone.json"
    "Server/Item/Interactions/OrbGenesis/NoBuildStone/OrbGenesis_NoBuild_Stone_Throw.json"
    "Server/ProjectileConfigs/Weapons/Throwables/Projectile_Config_OrbGenesis_NoBuild_Stone.json"
    "Server/TriggerVolumes/Effects/OrbGenesis_NoBuild_5x5x5_10s.json"
    "Server/Languages/en-US/server.lang"
    "Server/Languages/es-ES/server.lang"
  )

  foreach ($entry in $required) {
    if ($entries -notcontains $entry) {
      throw "Missing archive entry: $entry"
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
      $manifest.Name -ne "OrbGenesis Mechanisms" -or
      $manifest.Version -ne "1.0.5" -or
      $manifest.IncludesAssetPack -ne $true) {
    throw "Archive manifest does not identify OrbGenesis Mechanisms 1.0.5."
  }
} finally {
  $zip.Dispose()
}

Write-Output "OrbGenesis Mechanisms package check passed: $archive"
