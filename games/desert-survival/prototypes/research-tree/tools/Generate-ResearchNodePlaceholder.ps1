# Generates the temporary 24x24 research glyph used by every square node.
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing
$project = Split-Path -Parent $PSScriptRoot
$output = Join-Path $project "Common\UI\Custom\Pages\DesertResearch\ResearchNodePlaceholder.png"
$bitmap = [System.Drawing.Bitmap]::new(24, 24, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$g = [System.Drawing.Graphics]::FromImage($bitmap)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.Clear([System.Drawing.Color]::Transparent)
$cyan = [System.Drawing.Color]::FromArgb(255, 119, 225, 231)
$gold = [System.Drawing.Color]::FromArgb(255, 246, 201, 94)
$pen = [System.Drawing.Pen]::new($cyan, 2)
$g.DrawRectangle($pen, 3, 3, 17, 17)
$g.DrawLine($pen, 8, 11, 12, 6)
$g.DrawLine($pen, 12, 6, 16, 11)
$g.DrawLine($pen, 8, 11, 12, 17)
$g.DrawLine($pen, 16, 11, 12, 17)
$brush = [System.Drawing.SolidBrush]::new($gold)
$g.FillEllipse($brush, 10, 9, 4, 4)
$brush.Dispose()
$pen.Dispose()
$g.Dispose()
$bitmap.Save($output, [System.Drawing.Imaging.ImageFormat]::Png)
$bitmap.Dispose()
Write-Output "Wrote $output"
