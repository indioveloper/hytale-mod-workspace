param([string]$SourceImage = (Join-Path $PSScriptRoot "../art/Void_Ornament_Source.png"))
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing
$project = Split-Path -Parent $PSScriptRoot
$source = [System.Drawing.Bitmap]::new((Resolve-Path $SourceImage).Path)
$atlas = [System.Drawing.Bitmap]::new(160,352)
$g = [System.Drawing.Graphics]::FromImage($atlas)
$g.Clear([System.Drawing.ColorTranslator]::FromHtml("#281839"))
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
# Vanilla roof top and underside sample this 32x32 UV island.
$g.DrawImage($source,[System.Drawing.Rectangle]::new(68,284,32,32))
# Base underside; all other pixels remain opaque purple metal.
$g.DrawImage($source,[System.Drawing.Rectangle]::new(0,32,32,32))
$g.Dispose()
$atlas.Save((Join-Path $project "Common/Blocks/Raynor/MetalworkFlatRoofs/Textures/Void_Ornament.png"),[System.Drawing.Imaging.ImageFormat]::Png)
$icon = [System.Drawing.Bitmap]::new(64,64)
$ig = [System.Drawing.Graphics]::FromImage($icon)
$ig.DrawImage($source,0,0,64,64)
$ig.Dispose()
$icon.Save((Join-Path $project "Common/Icons/ItemsGenerated/Raynor_Metal_Void_Roof_Flat.png"),[System.Drawing.Imaging.ImageFormat]::Png)
$icon.Dispose()
$atlas.Dispose()
$source.Dispose()
