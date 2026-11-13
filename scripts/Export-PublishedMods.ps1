param(
  [string]$OutputDirectory = (Join-Path $PSScriptRoot '..\.build\public-releases')
)
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$artifacts = @(
  'mods/java/raynor-holograms/.build/dist/Raynor_Holograms-0.8.3.jar',
  'mods/java/more-triggers/.build/dist/More_Triggers-1_10_6.jar',
  'mods/java/entity-motion-triggers/.build/dist/Entity_Motion_Triggers-1_3_3.jar',
  'mods/java/particle-shape-vfx/.build/dist/Particle_Shape_VFX-0_1_2.jar',
  'mods/java/scoreboards/.build/dist/Scoreboards-2_0_12.jar',
  'mods/java/configurable-mob-spawners/.build/dist/ConfigurableMobSpawners-0.5.4.jar'
)
foreach ($path in $artifacts) {
  if (-not (Test-Path -LiteralPath (Join-Path $repo $path))) { throw "Build missing: $path" }
}
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$zipPath = Join-Path $OutputDirectory ('Raynor_Public_Mods_Hytale_0.6.3_' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.zip')
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::Open($zipPath, [IO.Compression.ZipArchiveMode]::Create)
try {
  $hashes = @()
  $ids = @{}
  foreach ($relative in $artifacts) {
    $path = Join-Path $repo $relative
    $jar = [IO.Compression.ZipFile]::OpenRead($path)
    try {
      $reader = [IO.StreamReader]::new($jar.GetEntry('manifest.json').Open())
      try { $manifest = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
      $id = "$($manifest.Group):$($manifest.Name)"
      if ($ids.ContainsKey($id)) { throw "Duplicate plugin: $id" }
      $ids[$id] = $true
    } finally { $jar.Dispose() }
    $name = Split-Path -Leaf $path
    [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $path, $name) | Out-Null
    $hashes += (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash + '  ' + $name
  }
  $entry = $archive.CreateEntry('SHA256SUMS.txt')
  $writer = [IO.StreamWriter]::new($entry.Open())
  try { $writer.WriteLine(($hashes -join "`n")) } finally { $writer.Dispose() }
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, (Join-Path $repo 'docs/UPDATE_6_3_PUBLIC_MODS.md'), 'RELEASE_NOTES.md') | Out-Null
} finally { $archive.Dispose() }
Write-Output $zipPath
