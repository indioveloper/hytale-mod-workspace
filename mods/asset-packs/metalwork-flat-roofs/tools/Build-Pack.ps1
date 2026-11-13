param(
  [string]$ArtifactName = "Raynor_Metalwork_Flat_Roofs-0_1_3.zip"
)

$ErrorActionPreference = "Stop"

$project = Split-Path -Parent $PSScriptRoot
$workspace = (Resolve-Path -LiteralPath (Join-Path $project "..\..\..")).Path

& (Join-Path $PSScriptRoot "Test-Pack.ps1") -ProjectPath $project

$buildOutput = & (Join-Path $workspace "scripts\Build-AssetPack.ps1") `
  -ProjectPath $project `
  -ArtifactName $ArtifactName

$artifact = Join-Path $project ".build\dist\$ArtifactName"
& jar uf $artifact `
  -C $project LICENSE `
  -C $project THIRD_PARTY_NOTICES.md `
  -C $project README.md
if ($LASTEXITCODE -ne 0) {
  throw "Could not add license and attribution files to the artifact"
}

& jar --validate --file $artifact
if ($LASTEXITCODE -ne 0) {
  throw "Final archive validation failed"
}

$buildOutput | Where-Object { $_ -notmatch '^SHA256:' }
$hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $artifact).Hash
Write-Output "Final SHA256: $hash"
