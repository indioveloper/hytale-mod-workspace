param(
  [string]$ArtifactName = "Raynor_WorldGen_V2_Lab-0_3_0-u7pre1.zip"
)

$ErrorActionPreference = "Stop"

$project = Split-Path -Parent $PSScriptRoot
$workspace = (Resolve-Path -LiteralPath (Join-Path $project "..\..\..")).Path

& (Join-Path $workspace "scripts\Build-AssetPack.ps1") `
  -ProjectPath $project `
  -ArtifactName $ArtifactName

if ($LASTEXITCODE -ne 0) {
  throw "WorldGen V2 asset-pack build failed with exit code $LASTEXITCODE"
}
