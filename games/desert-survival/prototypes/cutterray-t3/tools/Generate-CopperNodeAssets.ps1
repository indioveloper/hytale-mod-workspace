$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$modelDirectory = Join-Path $projectRoot "Common/Blocks/Raynor/ResourceNodes"
$iconDirectory = Join-Path $projectRoot "Common/Icons/Items/Raynor"
New-Item -ItemType Directory -Path $modelDirectory, $iconDirectory -Force | Out-Null

function New-Vector([double]$x, [double]$y, [double]$z) {
  return [ordered]@{ x = $x; y = $y; z = $z }
}

function New-QuaternionY([double]$degrees) {
  $halfRadians = $degrees * [Math]::PI / 360.0
  return [ordered]@{
    x = 0
    y = [Math]::Round([Math]::Sin($halfRadians), 5)
    z = 0
    w = [Math]::Round([Math]::Cos($halfRadians), 5)
  }
}

function New-TextureLayout([int]$tileX, [int]$tileY) {
  $face = {
    param($x, $y)
    [ordered]@{
      offset = [ordered]@{ x = $x; y = $y }
      mirror = [ordered]@{ x = $false; y = $false }
      angle = 0
    }
  }
  return [ordered]@{
    back = & $face $tileX $tileY
    right = & $face $tileX ($tileY + 8)
    front = & $face ($tileX + 8) $tileY
    left = & $face ($tileX + 8) ($tileY + 8)
    top = & $face ($tileX + 16) $tileY
    bottom = & $face ($tileX + 16) ($tileY + 8)
  }
}

function New-Node(
  [string]$id,
  [string]$name,
  [double[]]$position,
  [double[]]$size,
  [string]$material,
  [double]$rotationY = 0
) {
  $tiles = @{
    Sandstone = @(0, 0)
    Basalt = @(32, 0)
    Copper = @(64, 0)
    Patina = @(96, 0)
  }
  $tile = $tiles[$material]
  if ($null -eq $tile) {
    throw "Unknown material: $material"
  }
  return [ordered]@{
    id = $id
    name = $name
    position = New-Vector $position[0] $position[1] $position[2]
    orientation = New-QuaternionY $rotationY
    shape = [ordered]@{
      type = "box"
      offset = New-Vector 0 0 0
      stretch = New-Vector 1 1 1
      settings = [ordered]@{
        size = New-Vector $size[0] $size[1] $size[2]
      }
      textureLayout = New-TextureLayout $tile[0] $tile[1]
      unwrapMode = "custom"
      visible = $true
      doubleSided = $false
      shadingMode = "standard"
    }
  }
}

$nodes = @(
  (New-Node "1" "Rock_Core" @(0, 13, 0) @(62, 26, 48) "Sandstone" 5),
  (New-Node "2" "Rock_Left" @(-27, 21, -4) @(34, 31, 38) "Basalt" -18),
  (New-Node "3" "Rock_Right" @(27, 18, 5) @(36, 27, 40) "Sandstone" 22),
  (New-Node "4" "Rock_Back" @(0, 10, -25) @(50, 20, 24) "Basalt" 8),
  (New-Node "5" "Rock_Crown" @(2, 34, -5) @(42, 21, 32) "Basalt" -7),
  (New-Node "6" "Rock_Front" @(1, 18, 24) @(45, 22, 21) "Sandstone" 11),
  (New-Node "10" "Copper_Lobe_1" @(-27, 31, 10) @(10, 22, 9) "Copper" -14),
  (New-Node "11" "Copper_Lobe_2" @(-10, 42, -1) @(9, 22, 10) "Copper" 12),
  (New-Node "12" "Copper_Lobe_3" @(11, 36, 14) @(11, 20, 9) "Patina" -8),
  (New-Node "13" "Copper_Lobe_4" @(30, 27, -7) @(9, 18, 10) "Copper" 18),
  (New-Node "14" "Copper_Lobe_5" @(7, 24, 29) @(12, 17, 8) "Copper" -22),
  (New-Node "15" "Copper_Lobe_6" @(-32, 18, -20) @(8, 16, 9) "Patina" 26),
  (New-Node "20" "Patina_1" @(-27, 39, 10) @(7, 6, 10) "Patina" -14),
  (New-Node "21" "Patina_2" @(30, 33, -7) @(7, 6, 11) "Patina" 18),
  (New-Node "22" "Copper_Vein" @(0, 17, -28) @(30, 4, 3) "Copper" 8)
)

$model = [ordered]@{
  lod = "auto"
  nodes = $nodes
}
$modelPath = Join-Path $modelDirectory "Copper_Node_Prototype.blockymodel"
$model | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $modelPath -Encoding utf8NoBOM

Add-Type -AssemblyName System.Drawing
$texture = [System.Drawing.Bitmap]::new(128, 64)
$graphics = [System.Drawing.Graphics]::FromImage($texture)
try {
  $graphics.Clear([System.Drawing.Color]::FromArgb(255, 30, 25, 22))
  $materials = @(
    @{ X = 0; Base = [Drawing.Color]::FromArgb(255, 132, 98, 65); Light = [Drawing.Color]::FromArgb(255, 181, 139, 87); Dark = [Drawing.Color]::FromArgb(255, 78, 61, 50) },
    @{ X = 32; Base = [Drawing.Color]::FromArgb(255, 54, 53, 55); Light = [Drawing.Color]::FromArgb(255, 91, 86, 79); Dark = [Drawing.Color]::FromArgb(255, 27, 30, 34) },
    @{ X = 64; Base = [Drawing.Color]::FromArgb(255, 180, 83, 39); Light = [Drawing.Color]::FromArgb(255, 247, 157, 70); Dark = [Drawing.Color]::FromArgb(255, 103, 44, 31) },
    @{ X = 96; Base = [Drawing.Color]::FromArgb(255, 39, 126, 104); Light = [Drawing.Color]::FromArgb(255, 94, 190, 145); Dark = [Drawing.Color]::FromArgb(255, 21, 69, 66) }
  )
  $random = [System.Random]::new(3107)
  foreach ($material in $materials) {
    $base = [Drawing.SolidBrush]::new($material.Base)
    $light = [Drawing.Pen]::new($material.Light)
    $dark = [Drawing.Pen]::new($material.Dark)
    try {
      $graphics.FillRectangle($base, $material.X, 0, 32, 32)
      for ($mark = 0; $mark -lt 34; $mark++) {
        $x = $material.X + $random.Next(1, 29)
        $y = $random.Next(1, 29)
        $length = $random.Next(1, 5)
        if (($mark % 3) -eq 0) {
          $graphics.DrawLine($light, $x, $y, [Math]::Min($material.X + 31, $x + $length), $y)
        } else {
          $graphics.DrawLine($dark, $x, $y, [Math]::Min($material.X + 31, $x + $length), $y + 1)
        }
      }
    } finally {
      $base.Dispose()
      $light.Dispose()
      $dark.Dispose()
    }
  }
  $texture.Save((Join-Path $modelDirectory "Copper_Node_Prototype_Texture.png"), [Drawing.Imaging.ImageFormat]::Png)
} finally {
  $graphics.Dispose()
  $texture.Dispose()
}

$icon = [System.Drawing.Bitmap]::new(128, 128)
$iconGraphics = [System.Drawing.Graphics]::FromImage($icon)
try {
  $iconGraphics.Clear([Drawing.Color]::Transparent)
  $iconGraphics.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::None
  $outline = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255, 22, 25, 27))
  $rock = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255, 126, 91, 62))
  $darkRock = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255, 54, 53, 55))
  $copper = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255, 230, 123, 55))
  $patina = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255, 52, 151, 119))
  try {
    $iconGraphics.FillPolygon($outline, [Drawing.Point[]]@(
      [Drawing.Point]::new(8, 99), [Drawing.Point]::new(18, 57), [Drawing.Point]::new(40, 37),
      [Drawing.Point]::new(73, 28), [Drawing.Point]::new(104, 46), [Drawing.Point]::new(120, 93),
      [Drawing.Point]::new(107, 111), [Drawing.Point]::new(25, 112)))
    $iconGraphics.FillRectangle($rock, 19, 62, 87, 38)
    $iconGraphics.FillRectangle($darkRock, 28, 51, 32, 48)
    $iconGraphics.FillRectangle($rock, 61, 45, 38, 54)
    $iconGraphics.FillRectangle($copper, 31, 39, 15, 49)
    $iconGraphics.FillRectangle($copper, 57, 34, 16, 56)
    $iconGraphics.FillRectangle($copper, 83, 48, 17, 43)
    $iconGraphics.FillRectangle($patina, 34, 39, 12, 9)
    $iconGraphics.FillRectangle($patina, 86, 48, 14, 8)
  } finally {
    $outline.Dispose()
    $rock.Dispose()
    $darkRock.Dispose()
    $copper.Dispose()
    $patina.Dispose()
  }
  $finalIcon = [System.Drawing.Bitmap]::new(64, 64)
  $finalGraphics = [System.Drawing.Graphics]::FromImage($finalIcon)
  try {
    $finalGraphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $finalGraphics.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
    $finalGraphics.DrawImage($icon, 0, 0, 64, 64)
    $finalIcon.Save((Join-Path $iconDirectory "Resource_Node_Copper_Prototype.png"), [Drawing.Imaging.ImageFormat]::Png)
  } finally {
    $finalGraphics.Dispose()
    $finalIcon.Dispose()
  }
} finally {
  $iconGraphics.Dispose()
  $icon.Dispose()
}

Write-Output "Generated: $modelPath"
