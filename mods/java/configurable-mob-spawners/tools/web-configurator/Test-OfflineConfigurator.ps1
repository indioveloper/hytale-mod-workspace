param(
  [Parameter(Mandatory = $true)]
  [string]$HtmlPath
)

$ErrorActionPreference = "Stop"
$resolved = (Resolve-Path -LiteralPath $HtmlPath).Path
$html = Get-Content -Encoding UTF8 -Raw -LiteralPath $resolved
$size = (Get-Item -LiteralPath $resolved).Length

foreach ($required in @(
    'id="embedded-item-catalog"',
    'id="embedded-mob-catalog"',
    'id="embedded-preview-data"',
    'id="embedded-soft-light-theme"',
    'window.HYTALE_ITEM_PREVIEWS=',
    'window.HYTALE_ITEM_STATS=',
    'window.HYTALE_MOB_PREVIEWS=',
    'data:image/png;base64,',
    'Mob Spawner Customizable'
    'class="mob-stat-strip"'
  )) {
  if (-not $html.Contains($required)) {
    throw "Offline configurator content is missing: $required"
  }
}

foreach ($forbidden in @(
    '<script src=',
    '<link rel="stylesheet" href=',
    'asset-ids.generated.js',
    'mob-preview.generated.js',
    'item-previews.generated/',
    'mob-previews.generated/',
    'src="player-reference.png"'
  )) {
  if ($html.Contains($forbidden)) {
    throw "Offline configurator contains an external dependency: $forbidden"
  }
}

$dataUriCount = [regex]::Matches($html, 'data:image/png;base64,').Count
if ($dataUriCount -lt 3200) {
  throw "Offline configurator contains too few embedded previews: $dataUriCount"
}
if ($size -lt 15MB -or $size -gt 30MB) {
  throw ("Unexpected offline configurator size: {0:N2} MiB" -f ($size / 1MB))
}

$scriptMatches = [regex]::Matches(
  $html,
  '<script(?![^>]*\bsrc=)[^>]*>([\s\S]*?)</script>')
if ($scriptMatches.Count -lt 4) {
  throw "Offline configurator inline scripts could not be extracted."
}
$scriptSource = ($scriptMatches | ForEach-Object { $_.Groups[1].Value }) -join ";`n"
$temporaryScript = Join-Path $env:TEMP (
    "configurable-spawner-offline-check-" + [guid]::NewGuid().ToString("N") + ".js")
try {
  [System.IO.File]::WriteAllText(
    $temporaryScript,
    $scriptSource,
    [System.Text.UTF8Encoding]::new($false))
  & node --check $temporaryScript
  if ($LASTEXITCODE -ne 0) {
    throw "Offline configurator JavaScript syntax check failed."
  }
}
finally {
  if (Test-Path -LiteralPath $temporaryScript) {
    Remove-Item -LiteralPath $temporaryScript -Force
  }
}

Write-Output (
  "Offline configurator check passed: {0} embedded PNGs, {1:N2} MiB." -f
    $dataUriCount, ($size / 1MB))
