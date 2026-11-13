$ErrorActionPreference = "Stop"
$projectPath = Split-Path -Parent $PSScriptRoot
$buildPath = Join-Path $projectPath ".build"
& (Join-Path $PSScriptRoot "Test-Pack.ps1")
New-Item -ItemType Directory -Force -Path $buildPath | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$version = ((Get-Content (Join-Path $projectPath "behavior_pack/manifest.json") -Raw | ConvertFrom-Json).header.version -join "_")
$behavior = Join-Path $buildPath "Menu_Magico_BP_$version.mcpack"
$resources = Join-Path $buildPath "Menu_Magico_RP_$version.mcpack"
$addon = Join-Path $buildPath "Menu_Magico_$version.mcaddon"
foreach ($path in @($behavior, $resources, $addon)) {
    if (Test-Path -LiteralPath $path) { Remove-Item -LiteralPath $path }
}
[System.IO.Compression.ZipFile]::CreateFromDirectory((Join-Path $projectPath "behavior_pack"), $behavior)
[System.IO.Compression.ZipFile]::CreateFromDirectory((Join-Path $projectPath "resource_pack"), $resources)
$archive = [System.IO.Compression.ZipFile]::Open($addon, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($path in @($behavior, $resources)) {
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $path, [System.IO.Path]::GetFileName($path)) | Out-Null
    }
} finally { $archive.Dispose() }
Write-Host "Creado: $addon"
Get-FileHash -LiteralPath $addon -Algorithm SHA256
