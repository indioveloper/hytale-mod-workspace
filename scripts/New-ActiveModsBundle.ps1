param(
  [string]$OutputDirectory = (Join-Path $PSScriptRoot "..\.build\bundles")
)

$ErrorActionPreference = "Stop"
$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$zipPath = Join-Path $OutputDirectory "OrbGenesis_7_Mods_Source_Jars_Hytale_0.6.0-pre.12_$stamp.zip"

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::Open($zipPath, [IO.Compression.ZipArchiveMode]::Create)
try {
  function Add-BundleFile([string]$Source, [string]$EntryName) {
    [IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
        $archive,
        $Source,
        $EntryName.Replace("\", "/"),
        [IO.Compression.CompressionLevel]::Optimal) | Out-Null
  }

  function Add-BundleText([string]$EntryName, [string]$Content) {
    $entry = $archive.CreateEntry($EntryName, [IO.Compression.CompressionLevel]::Optimal)
    $writer = [IO.StreamWriter]::new($entry.Open(), [Text.UTF8Encoding]::new($false))
    try {
      $writer.Write($Content)
    } finally {
      $writer.Dispose()
    }
  }

  $projects = @(
    @{ Name = "more-triggers"; Path = "mods\java\more-triggers" }
    @{ Name = "entity-motion-triggers"; Path = "mods\java\entity-motion-triggers" }
    @{ Name = "particle-shape-vfx"; Path = "mods\java\particle-shape-vfx" }
    @{ Name = "scoreboards"; Path = "mods\java\scoreboards" }
    @{ Name = "build-battle"; Path = "mods\java\build-battle" }
    @{ Name = "configurable-mob-spawners"; Path = "mods\java\configurable-mob-spawners" }
    @{ Name = "raynor-npcs"; Path = "mods\asset-packs\raynor-npcs" }
  )
  foreach ($project in $projects) {
    $projectRoot = Join-Path $repo $project.Path
    Get-ChildItem -LiteralPath $projectRoot -Recurse -File |
        Where-Object {
          $relative = $_.FullName.Substring($projectRoot.Length).TrimStart("\")
          $relative -notmatch '(^|\\)(\.build|build|dist|target)(\\|$)' -and
              $relative -notmatch '(^|\\)(item-previews\.generated|mob-previews\.generated)(\\|$)' -and
              $_.Name -notin "asset-ids.generated.js", "mob-preview.generated.js" -and
              $_.Extension -notin ".jar", ".class", ".zip"
        } |
        ForEach-Object {
          $relative = $_.FullName.Substring($projectRoot.Length).TrimStart("\")
          Add-BundleFile $_.FullName "source/$($project.Name)/$relative"
        }
  }

  $jars = @(
    "mods\java\more-triggers\.build\dist\More_Triggers-1_9_2.jar"
    "mods\java\entity-motion-triggers\.build\dist\Entity_Motion_Triggers-1_3_1.jar"
    "mods\java\particle-shape-vfx\.build\dist\Particle_Shape_VFX-0_1_0.jar"
    "mods\java\scoreboards\.build\dist\Scoreboards-2_0_10.jar"
    "mods\java\build-battle\.build\dist\Build_Battle-0_2_3.jar"
    "mods\java\configurable-mob-spawners\.build\dist\ConfigurableMobSpawners-0.4.0.jar"
    "mods\asset-packs\raynor-npcs\.build\dist\Raynor_NPCs-1_1_0.jar"
  )
  $index = @(
    "# OrbGenesis active mods bundle"
    ""
    "Built and tested: 2026-08-13"
    "Hytale: 0.6.0-pre.12"
    "Server revision: f57d3e0abf0c2d47a7c839cb33a88aaa7a0daed2"
    "Java: 25"
    ""
    "## JARs"
  )
  foreach ($jarRelative in $jars) {
    $jarPath = (Resolve-Path -LiteralPath (Join-Path $repo $jarRelative)).Path
    $name = Split-Path -Leaf $jarPath
    Add-BundleFile $jarPath "jars/$name"
    $hash = (Get-FileHash -LiteralPath $jarPath -Algorithm SHA256).Hash
    $index += "- $name  SHA-256: $hash"
  }

  $index += @(
    ""
    "## Notes"
    "- ExecuteCommand is included in More Triggers 1.9.2."
    "- Build outputs, logs, saves, HytaleServer.jar, and extracted vanilla assets are excluded."
    "- workspace-changes.patch records the current uncommitted repository changes."
  )

  Add-BundleFile (Join-Path $repo "scripts\Build-JavaMod.ps1") "workspace-support/scripts/Build-JavaMod.ps1"
  Add-BundleFile (Join-Path $repo "scripts\Test-ActiveJavaMods.ps1") "workspace-support/scripts/Test-ActiveJavaMods.ps1"
  Add-BundleFile (Join-Path $repo "README.md") "workspace-support/README.md"
  Add-BundleFile (Join-Path $repo "AGENTS.md") "workspace-support/AGENTS.md"
  Add-BundleText "workspace-support/workspace-changes.patch" ((& git -C $repo diff --no-color) | Out-String)
  Add-BundleText "CONTENTS.md" ($index -join "`n")
} finally {
  $archive.Dispose()
}

$zipItem = Get-Item -LiteralPath $zipPath
[PSCustomObject]@{
  Path = $zipItem.FullName
  SizeBytes = $zipItem.Length
  SHA256 = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash
}
