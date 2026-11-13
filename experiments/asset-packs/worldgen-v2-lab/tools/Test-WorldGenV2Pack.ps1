param(
  [string]$ProjectPath = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = "Stop"

function Read-JsonFile([string]$Path) {
  try {
    return Get-Content -Raw -LiteralPath $Path | ConvertFrom-Json
  } catch {
    throw "Invalid JSON/BSON document '$Path': $($_.Exception.Message)"
  }
}

$project = (Resolve-Path -LiteralPath $ProjectPath).Path
$manifestPath = Join-Path $project "manifest.json"
$manifest = Read-JsonFile $manifestPath

if (-not $manifest.IncludesAssetPack) {
  throw "manifest.json must set IncludesAssetPack to true."
}
if ($manifest.ServerVersion -ne "=0.7.0-pre.1") {
  throw "This experiment is pinned to =0.7.0-pre.1, found '$($manifest.ServerVersion)'."
}

$biomeFiles = @(Get-ChildItem -File -Filter "*.json" -LiteralPath (Join-Path $project "Server\HytaleGenerator\Biomes"))
$structureFiles = @(Get-ChildItem -File -Filter "*.json" -LiteralPath (Join-Path $project "Server\HytaleGenerator\WorldStructures"))
$instanceFiles = @(Get-ChildItem -Recurse -File -Filter "instance.bson" -LiteralPath (Join-Path $project "Server\Instances"))

if ($biomeFiles.Count -eq 0 -or $structureFiles.Count -eq 0 -or $instanceFiles.Count -eq 0) {
  throw "The pack needs at least one Biome, WorldStructure and instance.bson."
}

$biomes = @{}
foreach ($file in $biomeFiles) {
  $asset = Read-JsonFile $file.FullName
  if ([string]::IsNullOrWhiteSpace($asset.Name)) {
    throw "Biome has no Name: $($file.FullName)"
  }
  if ($asset.Name -ne $file.BaseName) {
    throw "Biome Name '$($asset.Name)' must match filename '$($file.BaseName)'."
  }
  if ($asset.Terrain.Type -ne "DAOTerrain") {
    throw "Biome '$($asset.Name)' must currently use DAOTerrain."
  }
  if ($null -eq $asset.MaterialProvider -or $null -eq $asset.EnvironmentProvider -or $null -eq $asset.TintProvider) {
    throw "Biome '$($asset.Name)' is missing a required provider."
  }
  if ($asset.Name -eq "Raynor_Sky_Gardens") {
    if (@($asset.Props).Count -eq 0) {
      throw "Sky Gardens must include procedural surface detail."
    }
    if ((Get-Content -Raw -LiteralPath $file.FullName) -match '"Type"\s*:\s*"Prefab"') {
      throw "Sky Gardens must remain prefab-free during this iteration."
    }
  }
  $biomes[$asset.Name] = $asset
}

$structures = @{}
foreach ($file in $structureFiles) {
  $asset = Read-JsonFile $file.FullName
  if ($asset.Type -ne "NoiseRange") {
    throw "WorldStructure '$($file.BaseName)' uses unsupported Type '$($asset.Type)'."
  }
  if (-not $biomes.ContainsKey($asset.DefaultBiome)) {
    throw "WorldStructure '$($file.BaseName)' references missing local biome '$($asset.DefaultBiome)'."
  }
  if ($null -eq $asset.SpawnPositions) {
    throw "WorldStructure '$($file.BaseName)' needs explicit SpawnPositions for safe tests."
  }
  if ($file.BaseName -eq "Raynor_Sky_Gardens" -and $asset.SpawnPositions.Type -ne "Bound") {
    throw "Sky Gardens spawn must use a finite Bound over natural procedural island positions."
  }
  $structures[$file.BaseName] = $asset
}

foreach ($file in $instanceFiles) {
  $asset = Read-JsonFile $file.FullName
  if ($asset.WorldGen.Type -ne "HytaleGenerator") {
    throw "Instance '$($file.Directory.Name)' must use WorldGen.Type=HytaleGenerator."
  }
  if (-not $structures.ContainsKey($asset.WorldGen.WorldStructure)) {
    throw "Instance '$($file.Directory.Name)' references missing WorldStructure '$($asset.WorldGen.WorldStructure)'."
  }
  if ($asset.IsSavingChunks -ne $false -or $asset.SaveNewChunks -ne $false) {
    throw "Lab instances must not save chunks; stale chunks make iterations misleading."
  }
}

Write-Output "WorldGen V2 pack validation passed."
Write-Output "Biomes: $($biomeFiles.Count)"
Write-Output "WorldStructures: $($structureFiles.Count)"
Write-Output "Instances: $($instanceFiles.Count)"
