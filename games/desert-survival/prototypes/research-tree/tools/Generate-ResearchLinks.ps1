# Render every ResearchCatalog prerequisite edge behind the wheel buttons.
# Keep these radii synchronized with ResearchTreePage.ringAnchor().
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing
$project = Split-Path -Parent $PSScriptRoot
$source = Join-Path $project "src\gg\raynormods\desertsurvival\research\ResearchCatalog.java"
$output = Join-Path $project "Common\UI\Custom\Pages\DesertResearch\ResearchLinks.png"
$sectorNames = @("WATER", "COMBAT", "CONSTRUCTION", "EXPLORATION", "VEHICLES")
$radii = @(
  @(@(104), @(84, 124), @(80, 115, 145)),
  @(@(188), @(168, 208), @(160, 188, 214)),
  @(@(256), @(240, 272), @(234, 256, 278))
)

$nodes = [System.Collections.Generic.List[object]]::new()
foreach ($line in Get-Content -LiteralPath $source) {
  if ($line -notmatch 'add\(nodes, "([^"]+)", ResearchSector\.([A-Z]+), ([0-2]),') {
    continue
  }
  $id = $Matches[1]
  $sector = [array]::IndexOf($sectorNames, $Matches[2])
  $tier = [int]$Matches[3]
  if ($sector -lt 0) { throw "Unknown sector on $id" }
  $quoted = @([regex]::Matches($line, '"([^"]+)"') | ForEach-Object { $_.Groups[1].Value })
  $prerequisites = if ($quoted.Count -gt 3) { @($quoted[3..($quoted.Count - 1)]) } else { @() }
  $nodes.Add([pscustomobject]@{ Id=$id; Sector=$sector; Tier=$tier; Prerequisites=$prerequisites })
}
if ($nodes.Count -ne 48) { throw "Expected 48 research nodes, found $($nodes.Count)" }

$points = @{}
for ($sector = 0; $sector -lt $sectorNames.Count; $sector++) {
  for ($tier = 0; $tier -le 2; $tier++) {
    $group = @($nodes | Where-Object { $_.Sector -eq $sector -and $_.Tier -eq $tier })
    $count = $group.Count
    if ($count -eq 0) { continue }
    $layout = if ($count -le 2) { 0 } elseif ($count -le 4) { 1 } else { 2 }
    $band = $radii[$tier][$layout]
    for ($position = 0; $position -lt $count; $position++) {
      $radius = $band[$position % $band.Count]
      $angle = (-90.0 + 72.0 * $sector + 72.0 * ($position + 0.5) / $count) * [Math]::PI / 180.0
      $x = 290.0 + $radius * [Math]::Cos($angle)
      $y = 290.0 + $radius * [Math]::Sin($angle)
      $points[$group[$position].Id] = [System.Drawing.PointF]::new([single]$x, [single]$y)
    }
  }
}

$bitmap = [System.Drawing.Bitmap]::new(580, 580, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.Clear([System.Drawing.Color]::Transparent)
$outline = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(185, 7, 25, 37), 5)
$linePen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(225, 208, 237, 239), 2)
$outline.StartCap = $outline.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$linePen.StartCap = $linePen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$links = 0
foreach ($node in $nodes) {
  foreach ($prerequisite in $node.Prerequisites) {
    if (-not $points.ContainsKey($prerequisite)) { throw "Missing point for $prerequisite" }
    $graphics.DrawLine($outline, $points[$prerequisite], $points[$node.Id])
    $graphics.DrawLine($linePen, $points[$prerequisite], $points[$node.Id])
    $links++
  }
}
$linePen.Dispose()
$outline.Dispose()
$graphics.Dispose()
$bitmap.Save($output, [System.Drawing.Imaging.ImageFormat]::Png)
$bitmap.Dispose()
Write-Output "Wrote $output ($links prerequisite links)"
