Add-Type -AssemblyName System.Drawing

$output = Join-Path $PSScriptRoot '..\Common\UI\Custom\Icons'
New-Item -ItemType Directory -Path $output -Force | Out-Null

function New-Icon([string]$name, [scriptblock]$draw) {
  $bitmap = [System.Drawing.Bitmap]::new(48, 48)
  $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
  $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $graphics.Clear([System.Drawing.Color]::Transparent)
  & $draw $graphics
  $bitmap.Save((Join-Path $output ($name + '.png')), [System.Drawing.Imaging.ImageFormat]::Png)
  $graphics.Dispose()
  $bitmap.Dispose()
}

New-Icon 'Loot' {
  param($g)
  $g.FillRectangle([System.Drawing.Brushes]::SaddleBrown, 8, 15, 32, 24)
  $g.DrawRectangle([System.Drawing.Pens]::Peru, 8, 15, 32, 24)
  $g.FillRectangle([System.Drawing.Brushes]::Goldenrod, 20, 25, 8, 11)
}
New-Icon 'Star' {
  param($g)
  $points = [System.Drawing.PointF[]]@(
    [System.Drawing.PointF]::new(24,4), [System.Drawing.PointF]::new(29,18),
    [System.Drawing.PointF]::new(44,18), [System.Drawing.PointF]::new(32,27),
    [System.Drawing.PointF]::new(37,42), [System.Drawing.PointF]::new(24,33),
    [System.Drawing.PointF]::new(11,42), [System.Drawing.PointF]::new(16,27),
    [System.Drawing.PointF]::new(4,18), [System.Drawing.PointF]::new(19,18)
  )
  $g.FillPolygon([System.Drawing.Brushes]::Gold, $points)
  $g.DrawPolygon([System.Drawing.Pens]::Orange, $points)
}
New-Icon 'Key' {
  param($g)
  $pen = [System.Drawing.Pen]::new([System.Drawing.Color]::Goldenrod, 7)
  $g.DrawEllipse($pen, 6, 7, 18, 18)
  $g.DrawLine($pen, 20, 21, 41, 42)
  $g.DrawLine($pen, 32, 33, 38, 27)
  $pen.Dispose()
}
New-Icon 'Check' {
  param($g)
  $pen = [System.Drawing.Pen]::new([System.Drawing.Color]::MediumSeaGreen, 7)
  $g.DrawLines($pen, [System.Drawing.Point[]]@(
    [System.Drawing.Point]::new(7,25), [System.Drawing.Point]::new(19,37), [System.Drawing.Point]::new(42,11)
  ))
  $pen.Dispose()
}
New-Icon 'Warning' {
  param($g)
  $points = [System.Drawing.Point[]]@(
    [System.Drawing.Point]::new(24,5), [System.Drawing.Point]::new(44,42), [System.Drawing.Point]::new(4,42)
  )
  $g.FillPolygon([System.Drawing.Brushes]::Orange, $points)
  $g.DrawPolygon([System.Drawing.Pens]::DarkOrange, $points)
  $font = [System.Drawing.Font]::new('Arial', 26, [System.Drawing.FontStyle]::Bold)
  $g.DrawString('!', $font, [System.Drawing.Brushes]::Black, 18, 10)
  $font.Dispose()
}
