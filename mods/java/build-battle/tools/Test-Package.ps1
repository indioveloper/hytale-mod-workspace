param(
  [string]$ArchivePath = (Join-Path $PSScriptRoot '..\.build\dist\Build_Battle-0_3_0.jar')
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $ArchivePath))
try {
  $required = @(
    'manifest.json',
    'icon-256.png',
    'gg/orbgenesis/buildbattle/BuildBattleManager.class',
    'gg/orbgenesis/buildbattle/BuildBattleTeamPlanner.class',
    'gg/orbgenesis/buildbattle/BuildBattleCommand.class',
    'gg/orbgenesis/buildbattle/BuildBattleBlockSystems$Place.class',
    'gg/orbgenesis/buildbattle/BuildBattleBlockSystems$Break.class',
    'Common/UI/Custom/Pages/BuildBattle/ThemeVote.ui',
    'Common/UI/Custom/Pages/BuildBattle/ThemeVoteRow.ui',
    'Common/UI/Custom/Pages/BuildBattle/BuildVote.ui',
    'Server/Languages/en-US/server.lang',
    'Server/Languages/es-ES/server.lang'
  )
  foreach ($path in $required) {
    if ($null -eq $archive.GetEntry($path)) { throw "Missing package entry: $path" }
  }

  $reader = [IO.StreamReader]::new($archive.GetEntry('manifest.json').Open())
  try { $manifest = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
  if ($manifest.Version -ne '0.3.0') { throw "Unexpected version: $($manifest.Version)" }
  if ($manifest.ServerVersion -ne '>=0.6.2 <0.7.0') {
    throw "Unexpected server range: $($manifest.ServerVersion)"
  }
  if (-not $manifest.IncludesAssetPack) { throw 'IncludesAssetPack must be true.' }
} finally {
  $archive.Dispose()
}
Write-Host "Build Battle package check passed: $ArchivePath"
