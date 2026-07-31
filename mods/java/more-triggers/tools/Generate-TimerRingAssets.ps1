param(
  [int]$FrameCount = 60,
  [int]$Size = 128
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$hudRoot = Join-Path $project "src\Common\UI\Custom\HUD"
$timerRoot = Join-Path $hudRoot "CircularTimer"
$framesRoot = Join-Path $timerRoot "Frames"
New-Item -ItemType Directory -Path $framesRoot -Force | Out-Null

$platePath = Join-Path $timerRoot "CenterBackdrop.png"
$plate = [System.Drawing.Bitmap]::new($Size, $Size)
$graphics = [System.Drawing.Graphics]::FromImage($plate)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.Clear([System.Drawing.Color]::Transparent)
$plateBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(118, 8, 15, 20))
$graphics.FillEllipse($plateBrush, 12, 12, $Size - 24, $Size - 24)
$plate.Save($platePath, [System.Drawing.Imaging.ImageFormat]::Png)
$plateBrush.Dispose()
$graphics.Dispose()
$plate.Dispose()

for ($frame = 0; $frame -le $FrameCount; $frame++) {
  $bitmap = [System.Drawing.Bitmap]::new($Size, $Size)
  $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
  $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $graphics.Clear([System.Drawing.Color]::Transparent)
  if ($frame -gt 0) {
    $ratio = $frame / [double]$FrameCount
    $startAngle = -90.0 + ((1.0 - $ratio) * 360.0)
    $sweepAngle = $ratio * 360.0
    $pen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(235, 196, 202, 202), 7.0)
    $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $graphics.DrawArc($pen, 8, 8, $Size - 16, $Size - 16, [single]$startAngle, [single]$sweepAngle)
    $pen.Dispose()
  }
  $path = Join-Path $framesRoot ("Ring{0:D2}.png" -f $frame)
  $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
  $graphics.Dispose()
  $bitmap.Dispose()
}

$ui = [System.Collections.Generic.List[string]]::new()
$ui.Add("Group #CircularTimerRoot {")
$ui.Add("  Anchor: (Right: 36, Top: 36, Width: 128, Height: 128);")
$ui.Add("")
$ui.Add("  Panel #TimerBackdrop {")
$ui.Add("    Anchor: (Width: 128, Height: 128);")
$ui.Add('    Background: (TexturePath: "CircularTimer/CenterBackdrop.png");')
$ui.Add("  }")
$ui.Add("")
$ui.Add("  Group #RingStack {")
$ui.Add("    Anchor: (Width: 128, Height: 128);")
for ($frame = 0; $frame -le $FrameCount; $frame++) {
  $visible = if ($frame -eq $FrameCount) { "true" } else { "false" }
  $ui.Add(("    Panel #Ring{0:D2} {{" -f $frame))
  $ui.Add("      Anchor: (Width: 128, Height: 128);")
  $ui.Add(('      Background: (TexturePath: "CircularTimer/Frames/Ring{0:D2}.png");' -f $frame))
  $ui.Add(("      Visible: {0};" -f $visible))
  $ui.Add("    }")
}
$ui.Add("  }")
$ui.Add("")
$ui.Add("  Label #TimerText {")
$ui.Add("    Anchor: (Width: 128, Height: 128);")
$ui.Add('    Text: "01:00";')
$ui.Add("    Style: (")
$ui.Add("      FontSize: 25,")
$ui.Add("      RenderBold: true,")
$ui.Add("      HorizontalAlignment: Center,")
$ui.Add("      VerticalAlignment: Center,")
$ui.Add("      TextColor: #f2f5f5")
$ui.Add("    );")
$ui.Add("  }")
$ui.Add("}")

$uiPath = Join-Path $hudRoot "CircularTimer.ui"
[System.IO.File]::WriteAllLines($uiPath, $ui, [System.Text.UTF8Encoding]::new($false))
Write-Output "Generated $($FrameCount + 1) ring frames and $uiPath"
