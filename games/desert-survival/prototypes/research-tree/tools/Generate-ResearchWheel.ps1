# Renders Common/UI/Custom/Pages/DesertResearch/ResearchWheel.png: one disc split
# into five 72-degree sectors (one per research branch) and three concentric
# bands (tier 0 inner, tier 1 middle, tier 2 outer). The Custom UI engine has no
# rotation primitive, so the wheel is a static image and the nodes are placed
# over it by ResearchTreePage.ringAnchor().
param(
  [int]$Size = 580,
  [int]$OuterRadius = 288,
  [int]$Tier1Radius = 225,
  [int]$Tier0Radius = 150,
  [int]$HoleRadius = 58
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$project = Split-Path -Parent $PSScriptRoot
$output = Join-Path $project "Common\UI\Custom\Pages\DesertResearch\ResearchWheel.png"

$bitmap = [System.Drawing.Bitmap]::new($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$g = [System.Drawing.Graphics]::FromImage($bitmap)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.Clear([System.Drawing.Color]::Transparent)
$center = $Size / 2.0

function Disc([System.Drawing.Color]$color, [double]$radius) {
  $brush = [System.Drawing.SolidBrush]::new($color)
  $g.FillEllipse($brush, [single]($center - $radius), [single]($center - $radius), [single](2 * $radius), [single](2 * $radius))
  $brush.Dispose()
}
function Ring([System.Drawing.Color]$color, [double]$radius, [single]$width) {
  $pen = [System.Drawing.Pen]::new($color, $width)
  $g.DrawEllipse($pen, [single]($center - $radius), [single]($center - $radius), [single](2 * $radius), [single](2 * $radius))
  $pen.Dispose()
}

# Bands, outer to inner, so each inner disc covers the previous one.
Disc ([System.Drawing.Color]::FromArgb(70, 158, 187, 208)) $OuterRadius
Disc ([System.Drawing.Color]::FromArgb(90, 84, 200, 200)) $Tier1Radius
Disc ([System.Drawing.Color]::FromArgb(110, 223, 170, 87)) $Tier0Radius

# Sector dividers: five lines from the hole to the rim, first boundary at -90 deg.
$dividerPen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(230, 8, 25, 35), 3)
for ($k = 0; $k -lt 5; $k++) {
  $angle = (-90 + 72 * $k) * [Math]::PI / 180.0
  $x1 = $center + $HoleRadius * [Math]::Cos($angle)
  $y1 = $center + $HoleRadius * [Math]::Sin($angle)
  $x2 = $center + $OuterRadius * [Math]::Cos($angle)
  $y2 = $center + $OuterRadius * [Math]::Sin($angle)
  $g.DrawLine($dividerPen, [single]$x1, [single]$y1, [single]$x2, [single]$y2)
}
$dividerPen.Dispose()

# Band edges and rim.
$edge = [System.Drawing.Color]::FromArgb(140, 225, 236, 240)
Ring $edge $OuterRadius 2
Ring $edge $Tier1Radius 2
Ring $edge $Tier0Radius 2

# Transparent hub in the middle.
$g.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
Disc ([System.Drawing.Color]::FromArgb(0, 0, 0, 0)) $HoleRadius
$g.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceOver
Ring $edge $HoleRadius 2

$g.Dispose()
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $output) | Out-Null
$bitmap.Save($output, [System.Drawing.Imaging.ImageFormat]::Png)
$bitmap.Dispose()
Write-Output "Wrote $output ($Size x $Size, outer radius $OuterRadius)"
