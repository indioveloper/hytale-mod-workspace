$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$project = Split-Path -Parent $PSScriptRoot
$iconPath = Join-Path $project "Common\Icons\Items\Raynor\Intel_Pickup_Prototype.png"
$modIconPath = Join-Path $project "icon-256.png"

function New-IntelIcon([string]$Path, [int]$Size) {
  $bitmap = New-Object System.Drawing.Bitmap $Size, $Size
  $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
  try {
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.Clear([System.Drawing.Color]::Transparent)

    $scale = $Size / 64.0
    $outer = New-Object System.Drawing.Drawing2D.GraphicsPath
    $points = [System.Drawing.PointF[]]@(
      [System.Drawing.PointF]::new(32 * $scale, 4 * $scale),
      [System.Drawing.PointF]::new(55 * $scale, 18 * $scale),
      [System.Drawing.PointF]::new(50 * $scale, 48 * $scale),
      [System.Drawing.PointF]::new(32 * $scale, 60 * $scale),
      [System.Drawing.PointF]::new(14 * $scale, 48 * $scale),
      [System.Drawing.PointF]::new(9 * $scale, 18 * $scale)
    )
    $outer.AddPolygon($points)
    $gradient = New-Object System.Drawing.Drawing2D.PathGradientBrush $outer
    $gradient.CenterColor = [System.Drawing.Color]::FromArgb(255, 255, 241, 166)
    $gradient.SurroundColors = [System.Drawing.Color[]]@([System.Drawing.Color]::FromArgb(255, 176, 103, 32))
    $graphics.FillPath($gradient, $outer)

    $darkPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255, 43, 51, 56)), (3 * $scale)
    $graphics.DrawPath($darkPen, $outer)
    $cyanPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255, 65, 218, 232)), (2 * $scale)
    $graphics.DrawEllipse($cyanPen, 21 * $scale, 21 * $scale, 22 * $scale, 22 * $scale)
    $graphics.DrawLine($cyanPen, 32 * $scale, 14 * $scale, 32 * $scale, 50 * $scale)
    $graphics.DrawLine($cyanPen, 14 * $scale, 32 * $scale, 50 * $scale, 32 * $scale)

    $bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
  }
  finally {
    $graphics.Dispose()
    $bitmap.Dispose()
  }
}

New-IntelIcon -Path $iconPath -Size 64
New-IntelIcon -Path $modIconPath -Size 256

Write-Output "Generated: $iconPath"
Write-Output "Generated: $modIconPath"
