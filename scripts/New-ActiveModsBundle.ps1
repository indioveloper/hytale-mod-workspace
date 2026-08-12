param(
  [string]$OutputDirectory = (Join-Path $PSScriptRoot "..\.build\bundles")
)

$ErrorActionPreference = "Stop"
$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$zipPath = Join-Path $OutputDirectory "OrbGenesis_5_Mods_Source_Jars_Hytale_0.6.0-pre.11_$stamp.zip"

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
    "more-triggers"
    "entity-motion-triggers"
    "particle-shape-vfx"
    "scoreboards"
    "build-battle"
  )
  foreach ($project in $projects) {
    $projectRoot = Join-Path $repo "mods\java\$project"
    Get-ChildItem -LiteralPath $projectRoot -Recurse -File |
        Where-Object {
          $relative = $_.FullName.Substring($projectRoot.Length).TrimStart("\")
          $relative -notmatch '(^|\\)(\.build|build|dist|target)(\\|$)' -and
              $_.Extension -notin ".jar", ".class", ".zip"
        } |
        ForEach-Object {
          $relative = $_.FullName.Substring($projectRoot.Length).TrimStart("\")
          Add-BundleFile $_.FullName "source/$project/$relative"
        }
  }

  $jars = @(
    "mods\java\more-triggers\.build\dist\More_Triggers-1_9_1.jar"
    "mods\java\entity-motion-triggers\.build\dist\Entity_Motion_Triggers-1_3_0.jar"
    "mods\java\particle-shape-vfx\.build\dist\Particle_Shape_VFX-0_1_0.jar"
    "mods\java\scoreboards\.build\dist\Scoreboards-2_0_10.jar"
    "mods\java\build-battle\.build\dist\Build_Battle-0_2_2.jar"
  )
  $index = @(
    "# OrbGenesis active mods bundle"
    ""
    "Built and tested: 2026-08-11"
    "Hytale: 0.6.0-pre.11"
    "Server revision: 00cf2e930ab404ea983cb709c3e0a6deb45fda7a"
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

  $hotfixSource = "C:\Users\andre\Downloads\orbgenesis-core-main\orbgenesis-core-main\common\src\main\java\com\orbgenesis\common\ui\DynamicPngAsset.java"
  $hotfixJar = Join-Path $env:APPDATA "Hytale\data\pre-release\Saves\buildbattle2\mods\common-1.0-SNAPSHOT.jar"
  Add-BundleFile $hotfixSource "hotfix/orb-common-pre11/source/com/orbgenesis/common/ui/DynamicPngAsset.java"
  Add-BundleFile $hotfixJar "hotfix/orb-common-pre11/common-1.0-SNAPSHOT-patched.jar"
  $hotfixHash = (Get-FileHash -LiteralPath $hotfixJar -Algorithm SHA256).Hash
  $index += @(
    ""
    "## Additional pre.11 hotfix"
    "- Fixes the dynamic PNG asset hash mismatch in orb-common."
    "- common-1.0-SNAPSHOT-patched.jar  SHA-256: $hotfixHash"
    ""
    "## Notes"
    "- ExecuteCommand is included in More Triggers 1.9.1."
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
