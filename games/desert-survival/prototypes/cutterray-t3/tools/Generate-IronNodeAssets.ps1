$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$modelDirectory = Join-Path $projectRoot "Common/Blocks/Raynor/ResourceNodes"
$iconDirectory = Join-Path $projectRoot "Common/Icons/Items/Raynor"
New-Item -ItemType Directory -Path $modelDirectory, $iconDirectory -Force | Out-Null

$copperModel = Join-Path $modelDirectory "Copper_Node_Prototype.blockymodel"
$ironModel = Join-Path $modelDirectory "Iron_Node_Prototype.blockymodel"
if (-not (Test-Path -LiteralPath $copperModel -PathType Leaf)) {
  throw "Generate the copper node before deriving the iron prototype."
}

$modelText = [IO.File]::ReadAllText($copperModel)
$modelText = $modelText.Replace("Copper_Lobe", "Iron_Plate")
$modelText = $modelText.Replace("Copper_Vein", "Iron_Vein")
$modelText = $modelText.Replace("Patina_", "Oxide_")
[IO.File]::WriteAllText($ironModel, $modelText, [Text.UTF8Encoding]::new($false))

Add-Type -AssemblyName System.Drawing

function Write-MaterialTile(
  [Drawing.Graphics]$Graphics,
  [int]$X,
  [Drawing.Color]$Base,
  [Drawing.Color]$Light,
  [Drawing.Color]$Dark,
  [Random]$Random
) {
  $baseBrush = [Drawing.SolidBrush]::new($Base)
  $lightPen = [Drawing.Pen]::new($Light)
  $darkPen = [Drawing.Pen]::new($Dark)
  try {
    $Graphics.FillRectangle($baseBrush, $X, 0, 32, 32)
    for ($mark = 0; $mark -lt 34; $mark++) {
      $x1 = $X + $Random.Next(1, 29)
      $y1 = $Random.Next(1, 29)
      $x2 = [Math]::Min($X + 31, $x1 + $Random.Next(1, 5))
      $Graphics.DrawLine($(if (($mark % 3) -eq 0) { $lightPen } else { $darkPen }), $x1, $y1, $x2, $y1)
    }
  } finally {
    $baseBrush.Dispose()
    $lightPen.Dispose()
    $darkPen.Dispose()
  }
}

$texture = [Drawing.Bitmap]::new(128, 64)
$graphics = [Drawing.Graphics]::FromImage($texture)
try {
  $graphics.Clear([Drawing.Color]::FromArgb(255, 28, 27, 25))
  $random = [Random]::new(5813)
  Write-MaterialTile $graphics 0 `
    ([Drawing.Color]::FromArgb(255, 126, 95, 67)) `
    ([Drawing.Color]::FromArgb(255, 178, 139, 91)) `
    ([Drawing.Color]::FromArgb(255, 72, 57, 48)) $random
  Write-MaterialTile $graphics 32 `
    ([Drawing.Color]::FromArgb(255, 51, 52, 54)) `
    ([Drawing.Color]::FromArgb(255, 91, 88, 82)) `
    ([Drawing.Color]::FromArgb(255, 25, 28, 31)) $random
  Write-MaterialTile $graphics 64 `
    ([Drawing.Color]::FromArgb(255, 116, 124, 128)) `
    ([Drawing.Color]::FromArgb(255, 205, 216, 218)) `
    ([Drawing.Color]::FromArgb(255, 55, 61, 65)) $random
  Write-MaterialTile $graphics 96 `
    ([Drawing.Color]::FromArgb(255, 109, 65, 45)) `
    ([Drawing.Color]::FromArgb(255, 180, 103, 60)) `
    ([Drawing.Color]::FromArgb(255, 55, 37, 31)) $random
  $texture.Save(
    (Join-Path $modelDirectory "Iron_Node_Prototype_Texture.png"),
    [Drawing.Imaging.ImageFormat]::Png)
} finally {
  $graphics.Dispose()
  $texture.Dispose()
}

$icon = [Drawing.Bitmap]::new(64, 64)
$iconGraphics = [Drawing.Graphics]::FromImage($icon)
try {
  $iconGraphics.Clear([Drawing.Color]::Transparent)
  $iconGraphics.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::None
  $outline = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255, 20, 23, 25))
  $rock = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255, 111, 82, 58))
  $darkRock = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255, 51, 52, 54))
  $iron = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255, 183, 196, 200))
  $rust = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255, 166, 87, 48))
  try {
    $iconGraphics.FillPolygon($outline, [Drawing.Point[]]@(
      [Drawing.Point]::new(4, 50), [Drawing.Point]::new(9, 29),
      [Drawing.Point]::new(20, 19), [Drawing.Point]::new(37, 14),
      [Drawing.Point]::new(52, 23), [Drawing.Point]::new(60, 47),
      [Drawing.Point]::new(54, 56), [Drawing.Point]::new(13, 56)))
    $iconGraphics.FillRectangle($rock, 10, 31, 43, 19)
    $iconGraphics.FillRectangle($darkRock, 14, 26, 16, 24)
    $iconGraphics.FillRectangle($rock, 31, 23, 19, 27)
    $iconGraphics.FillRectangle($iron, 16, 20, 8, 25)
    $iconGraphics.FillRectangle($iron, 29, 17, 8, 28)
    $iconGraphics.FillRectangle($iron, 42, 24, 9, 22)
    $iconGraphics.FillRectangle($rust, 17, 20, 6, 4)
    $iconGraphics.FillRectangle($rust, 43, 24, 7, 4)
  } finally {
    $outline.Dispose()
    $rock.Dispose()
    $darkRock.Dispose()
    $iron.Dispose()
    $rust.Dispose()
  }
  $icon.Save(
    (Join-Path $iconDirectory "Resource_Node_Iron_Prototype.png"),
    [Drawing.Imaging.ImageFormat]::Png)
} finally {
  $iconGraphics.Dispose()
  $icon.Dispose()
}

Write-Output "Generated: $ironModel"
