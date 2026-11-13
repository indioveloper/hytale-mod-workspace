$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$modelDirectory = Join-Path $projectRoot "Common/Items/Raynor/Tools/Cutterray"
$iconDirectory = Join-Path $projectRoot "Common/Icons/Items/Raynor"
$trailDirectory = Join-Path $projectRoot "Common/Trails/Raynor"
New-Item -ItemType Directory -Path $modelDirectory, $iconDirectory, $trailDirectory -Force | Out-Null

function New-Vector([double]$x, [double]$y, [double]$z) {
  return [ordered]@{ x = $x; y = $y; z = $z }
}

function New-QuaternionX([double]$degrees) {
  $halfRadians = $degrees * [Math]::PI / 360.0
  return [ordered]@{
    x = [Math]::Round([Math]::Sin($halfRadians), 5)
    y = 0
    z = 0
    w = [Math]::Round([Math]::Cos($halfRadians), 5)
  }
}

function New-EmptyNode([string]$id, [string]$name, [double[]]$position, [object[]]$children) {
  return [ordered]@{
    id = $id
    name = $name
    position = New-Vector $position[0] $position[1] $position[2]
    orientation = New-QuaternionX 0
    shape = [ordered]@{
      type = "none"
      offset = New-Vector 0 0 0
      stretch = New-Vector 1 1 1
      settings = [ordered]@{ isPiece = $true }
      textureLayout = [ordered]@{}
      unwrapMode = "custom"
      visible = $true
      doubleSided = $false
      shadingMode = "flat"
    }
    children = $children
  }
}

function New-TextureLayout([int]$tileX, [int]$tileY) {
  $face = { param($x, $y, $angle) [ordered]@{ offset = [ordered]@{ x = $x; y = $y }; mirror = [ordered]@{ x = $false; y = $false }; angle = $angle } }
  return [ordered]@{
    back = & $face $tileX $tileY 0
    right = & $face $tileX ($tileY + 8) 0
    front = & $face ($tileX + 8) $tileY 0
    left = & $face ($tileX + 8) ($tileY + 8) 0
    top = & $face ($tileX + 16) $tileY 0
    bottom = & $face ($tileX + 16) ($tileY + 8) 0
  }
}

function New-BoxNode(
  [string]$id,
  [string]$name,
  [double[]]$position,
  [double[]]$size,
  [string]$material,
  [double]$rotationX = 0
) {
  $tiles = @{
    Graphite = @(0, 0)
    Copper = @(32, 0)
    Energy = @(64, 0)
    Grip = @(96, 0)
  }
  $tile = $tiles[$material]
  if ($null -eq $tile) {
    throw "Unknown material: $material"
  }
  return [ordered]@{
    id = $id
    name = $name
    position = New-Vector $position[0] $position[1] $position[2]
    orientation = New-QuaternionX $rotationX
    shape = [ordered]@{
      type = "box"
      offset = New-Vector 0 0 0
      stretch = New-Vector 1 1 1
      settings = [ordered]@{
        isPiece = $false
        size = New-Vector $size[0] $size[1] $size[2]
        isStaticBox = $true
      }
      textureLayout = New-TextureLayout $tile[0] $tile[1]
      unwrapMode = "custom"
      visible = $true
      doubleSided = $false
      shadingMode = "flat"
    }
  }
}

$pieces = @(
  (New-BoxNode "10" "Main_Housing" @(0, 0, 0) @(16, 14, 28) "Graphite"),
  (New-BoxNode "11" "Upper_Spine" @(0, 9, 0) @(10, 5, 28) "Copper"),
  (New-BoxNode "12" "Rear_Power_Cell" @(0, 0, -20) @(12, 12, 14) "Energy"),
  (New-BoxNode "13" "Rear_Cap" @(0, 0, -29) @(14, 14, 4) "Copper"),
  (New-BoxNode "14" "Left_Armor_Plate" @(-9, 0, 0) @(2, 10, 20) "Copper"),
  (New-BoxNode "15" "Right_Armor_Plate" @(9, 0, 0) @(2, 10, 20) "Copper"),
  (New-BoxNode "16" "Rear_Grip" @(0, -16, -7) @(9, 20, 9) "Grip" -15),
  (New-BoxNode "17" "Trigger" @(0, -9, 6) @(4, 6, 3) "Copper" -15),
  (New-BoxNode "18" "Barrel_Housing" @(0, 0, 23) @(10, 10, 22) "Graphite"),
  (New-BoxNode "19" "Upper_Energy_Coil" @(0, 7, 24) @(8, 3, 14) "Energy"),
  (New-BoxNode "20" "Lower_Energy_Coil" @(0, -7, 24) @(8, 3, 14) "Energy"),
  (New-BoxNode "21" "Left_Emitter_Prong" @(-7, 0, 36) @(3, 5, 16) "Copper"),
  (New-BoxNode "22" "Right_Emitter_Prong" @(7, 0, 36) @(3, 5, 16) "Copper"),
  (New-BoxNode "23" "Emitter_Core" @(0, 0, 39) @(8, 8, 8) "Graphite"),
  (New-BoxNode "24" "Emitter_Lens" @(0, 0, 44) @(6, 6, 2) "Energy"),
  (New-BoxNode "25" "Top_Sight" @(0, 12, 8) @(4, 5, 9) "Energy"),
  (New-BoxNode "26" "Side_Power_Conduit" @(8, -5, -7) @(3, 3, 20) "Energy")
)

$itemOrigin = New-EmptyNode "3" "Origin_Item" @(0, 0, 0) $pieces
$beamOrigin = New-EmptyNode "2" "Origin_Projectile" @(0, 0, 46) @()
$root = New-EmptyNode "1" "R-Attachment" @(0, 3, 10) @($beamOrigin, $itemOrigin)
$model = [ordered]@{
  nodes = @($root)
  format = "character"
  lod = "auto"
}

$modelPath = Join-Path $modelDirectory "T3_Industrial.blockymodel"
$model | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $modelPath -Encoding utf8NoBOM

Add-Type -AssemblyName System.Drawing
$bitmap = [System.Drawing.Bitmap]::new(128, 128)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
try {
  $graphics.Clear([System.Drawing.Color]::FromArgb(255, 18, 20, 23))
  $materials = @(
    @{ X = 0; Base = [System.Drawing.Color]::FromArgb(255, 43, 48, 52); Light = [System.Drawing.Color]::FromArgb(255, 70, 78, 82); Dark = [System.Drawing.Color]::FromArgb(255, 24, 29, 32) },
    @{ X = 32; Base = [System.Drawing.Color]::FromArgb(255, 173, 91, 47); Light = [System.Drawing.Color]::FromArgb(255, 225, 145, 75); Dark = [System.Drawing.Color]::FromArgb(255, 102, 48, 31) },
    @{ X = 64; Base = [System.Drawing.Color]::FromArgb(255, 25, 156, 184); Light = [System.Drawing.Color]::FromArgb(255, 116, 244, 255); Dark = [System.Drawing.Color]::FromArgb(255, 8, 70, 93) },
    @{ X = 96; Base = [System.Drawing.Color]::FromArgb(255, 74, 49, 35); Light = [System.Drawing.Color]::FromArgb(255, 122, 82, 51); Dark = [System.Drawing.Color]::FromArgb(255, 38, 28, 24) }
  )
  foreach ($material in $materials) {
    $baseBrush = [System.Drawing.SolidBrush]::new($material.Base)
    $lightPen = [System.Drawing.Pen]::new($material.Light)
    $darkPen = [System.Drawing.Pen]::new($material.Dark)
    try {
      $graphics.FillRectangle($baseBrush, $material.X, 0, 32, 32)
      for ($line = 2; $line -lt 32; $line += 8) {
        $graphics.DrawLine($lightPen, $material.X, $line, $material.X + 31, $line)
        $graphics.DrawLine($darkPen, $material.X, $line + 3, $material.X + 31, $line + 3)
      }
      for ($pixel = 0; $pixel -lt 32; $pixel += 7) {
        $bitmap.SetPixel($material.X + (($pixel * 3) % 31), (($pixel * 5) % 31), $material.Light)
      }
    } finally {
      $baseBrush.Dispose()
      $lightPen.Dispose()
      $darkPen.Dispose()
    }
  }
  $texturePath = Join-Path $modelDirectory "T3_Industrial_Texture.png"
  $bitmap.Save($texturePath, [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
  $graphics.Dispose()
  $bitmap.Dispose()
}

$icon = [System.Drawing.Bitmap]::new(128, 128)
$iconGraphics = [System.Drawing.Graphics]::FromImage($icon)
try {
  $iconGraphics.Clear([System.Drawing.Color]::Transparent)
  $iconGraphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
  $outline = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 12, 18, 22))
  $dark = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 38, 43, 47))
  $copper = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 211, 116, 58))
  $copperLight = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 246, 169, 83))
  $energy = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 74, 226, 244))
  $grip = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 84, 55, 38))
  try {
    # A bold side silhouette remains readable after the hotbar downsamples it.
    $iconGraphics.TranslateTransform(2, 82)
    $iconGraphics.RotateTransform(-20)
    $iconGraphics.FillRectangle($outline, 13, -35, 84, 42)
    $iconGraphics.FillRectangle($copper, 18, -30, 74, 32)
    $iconGraphics.FillRectangle($dark, 27, -24, 56, 21)
    $iconGraphics.FillRectangle($copperLight, 31, -30, 47, 5)

    $iconGraphics.FillRectangle($outline, 90, -30, 31, 34)
    $iconGraphics.FillRectangle($dark, 94, -25, 23, 24)
    $iconGraphics.FillRectangle($copper, 108, -29, 13, 32)
    $iconGraphics.FillRectangle($outline, 118, -25, 9, 25)
    $iconGraphics.FillRectangle($energy, 120, -22, 7, 19)

    $iconGraphics.FillRectangle($outline, 0, -29, 21, 31)
    $iconGraphics.FillRectangle($energy, 3, -25, 17, 23)
    $iconGraphics.FillRectangle($outline, 34, 0, 23, 42)
    $iconGraphics.FillRectangle($grip, 39, 3, 14, 34)
    $iconGraphics.FillRectangle($copperLight, 41, 4, 10, 5)
  } finally {
    $outline.Dispose()
    $dark.Dispose()
    $copper.Dispose()
    $copperLight.Dispose()
    $energy.Dispose()
    $grip.Dispose()
  }
  $finalIcon = [System.Drawing.Bitmap]::new(64, 64)
  $finalGraphics = [System.Drawing.Graphics]::FromImage($finalIcon)
  try {
    $finalGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $finalGraphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $finalGraphics.DrawImage($icon, 0, 0, 64, 64)
    $finalIcon.Save((Join-Path $iconDirectory "Tool_Cutterray_T3_Prototype.png"), [System.Drawing.Imaging.ImageFormat]::Png)
  } finally {
    $finalGraphics.Dispose()
    $finalIcon.Dispose()
  }
} finally {
  $iconGraphics.Dispose()
  $icon.Dispose()
}

$trail = [System.Drawing.Bitmap]::new(256, 64)
$trailGraphics = [System.Drawing.Graphics]::FromImage($trail)
try {
  $trailGraphics.Clear([System.Drawing.Color]::Transparent)
  $glowWide = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(72, 20, 205, 248))
  $glowInner = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(178, 75, 235, 255))
  $core = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 235, 255, 255))
  try {
    $trailGraphics.FillRectangle($glowWide, 0, 25, 256, 14)
    $trailGraphics.FillRectangle($glowInner, 0, 29, 256, 6)
    $trailGraphics.FillRectangle($core, 0, 31, 256, 3)
  } finally {
    $glowWide.Dispose()
    $glowInner.Dispose()
    $core.Dispose()
  }
  $trail.Save(
    (Join-Path $trailDirectory "Cutterray_Cyan_Laser.png"),
    [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
  $trailGraphics.Dispose()
  $trail.Dispose()
}

Write-Output "Generated: $modelPath"
