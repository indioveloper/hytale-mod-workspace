param(
  [Parameter(Mandatory = $true)]
  [string]$ArchivePath
)

$ErrorActionPreference = "Stop"
$archive = (Resolve-Path -LiteralPath $ArchivePath).Path
$entries = @(jar tf $archive)
if ($LASTEXITCODE -ne 0) {
  throw "Could not inspect archive: $archive"
}

$required = @(
  "manifest.json",
  "gg/raynormods/desertsurvival/research/ResearchPrototypePlugin.class",
  "gg/raynormods/desertsurvival/research/IntelInventoryConversionSystem.class",
  "Common/Icons/Items/Raynor/Intel_Pickup_Prototype.png",
  "Server/Item/Items/Research/Intel_Pickup_Prototype.json",
  "Common/UI/Custom/Pages/DesertResearch/ResearchTree.ui",
  "Common/UI/Custom/Pages/DesertResearch/ResearchRingNode.ui",
  "Common/UI/Custom/Pages/DesertResearch/ResearchRingNodeBlocked.ui",
  "Common/UI/Custom/Pages/DesertResearch/ResearchRingNodeResearched.ui",
  "Common/UI/Custom/Pages/DesertResearch/ResearchWheel.png",
  "Common/UI/Custom/Pages/DesertResearch/ResearchLinks.png",
  "Common/UI/Custom/Pages/DesertResearch/ResearchNodePlaceholder.png"
)
foreach ($path in $required) {
  if ($entries -notcontains $path) {
    throw "Required archive entry is missing: $path"
  }
}

$customUiEntries = @($entries | Where-Object {
  $_.StartsWith("Common/UI/Custom/", [System.StringComparison]::Ordinal) -and
  -not $_.EndsWith("/", [System.StringComparison]::Ordinal)
})
if ($customUiEntries.Count -ne 7) {
  throw "Research should contain four UI documents and three images, found $($customUiEntries.Count)."
}

# Only .ui documents and the pre-rendered wheel image live under Common/UI/Custom.
$nonDocumentUi = @($customUiEntries | Where-Object {
  -not $_.EndsWith(".ui", [System.StringComparison]::Ordinal) -and
  $_ -ne "Common/UI/Custom/Pages/DesertResearch/ResearchWheel.png" -and
  $_ -ne "Common/UI/Custom/Pages/DesertResearch/ResearchLinks.png" -and
  $_ -ne "Common/UI/Custom/Pages/DesertResearch/ResearchNodePlaceholder.png"
})
if ($nonDocumentUi.Count -ne 0) {
  throw "Common/UI/Custom must only contain .ui documents and approved images, found: $($nonDocumentUi -join ', ')"
}

$extract = Join-Path ([System.IO.Path]::GetTempPath()) ("research-package-" + [System.Guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $extract | Out-Null
try {
  Push-Location $extract
  try {
    & jar xf $archive
    if ($LASTEXITCODE -ne 0) {
      throw "Could not extract archive: $archive"
    }
  } finally {
    Pop-Location
  }

  # Enum values must be ones vanilla documents use. Research 0.1.2-0.1.6 shipped
  # `HorizontalAlignment: Right` (vanilla only uses Start, Center and End) and
  # every one of those builds failed the dune1 join with
  # "Could not find document Hud/ReturnToHubButton.ui".
  $allowed = @{
    "HorizontalAlignment" = @("Start", "Center", "End")
    "VerticalAlignment" = @("Center", "End")
    "LayoutMode" = @("Bottom", "Center", "CenterMiddle", "Full", "Left", "LeftCenterWrap",
      "LeftWrap", "Middle", "MiddleCenter", "Right", "Top", "TopScrolling")
  }
  foreach ($entry in $customUiEntries) {
    $text = Get-Content -LiteralPath (Join-Path $extract $entry) -Raw
    foreach ($key in $allowed.Keys) {
      foreach ($match in [regex]::Matches($text, "$key\s*:\s*([A-Za-z]+)")) {
        $value = $match.Groups[1].Value
        if ($allowed[$key] -notcontains $value) {
          throw "$entry uses $key`: $value, which no vanilla document uses (allowed: $($allowed[$key] -join ', '))."
        }
      }
    }
  }

  # No two Common assets may share identical bytes (client asset cache is
  # content-addressed).
  $commonRoot = Join-Path $extract "Common"
  $hashes = @{}
  foreach ($file in Get-ChildItem -LiteralPath $commonRoot -Recurse -File) {
    $hash = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash
    $relative = $file.FullName.Substring($extract.Length + 1).Replace("\", "/")
    if ($hashes.ContainsKey($hash)) {
      throw "Duplicate Common asset content: $($hashes[$hash]) and $relative share SHA-256 $hash"
    }
    $hashes[$hash] = $relative
  }
} finally {
  Remove-Item -LiteralPath $extract -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Output "Research package check passed with isolated radial UI: $archive"
