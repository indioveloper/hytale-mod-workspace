$ErrorActionPreference = "Stop"
$projectPath = Split-Path -Parent $PSScriptRoot
$buildPath = Join-Path $projectPath ".build"
& (Join-Path $PSScriptRoot "Test-Pack.ps1")
New-Item -ItemType Directory -Force -Path $buildPath | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$version = ((Get-Content (Join-Path $projectPath "behavior_pack/manifest.json") -Raw | ConvertFrom-Json).header.version -join "_")
$behavior = Join-Path $buildPath "Cubos_Para_Mobs_BP_$version.mcpack"
$addon = Join-Path $buildPath "Cubos_Para_Mobs_$version.mcaddon"
foreach ($path in @($behavior, $addon)) {
    if (Test-Path -LiteralPath $path) { Remove-Item -LiteralPath $path }
}
[System.IO.Compression.ZipFile]::CreateFromDirectory((Join-Path $projectPath "behavior_pack"), $behavior)
$archive = [System.IO.Compression.ZipFile]::Open($addon, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $behavior, [System.IO.Path]::GetFileName($behavior)) | Out-Null
} finally { $archive.Dispose() }
Write-Host "Creado: $addon"
Get-FileHash -LiteralPath $addon -Algorithm SHA256
