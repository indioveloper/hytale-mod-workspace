param(
  [string]$AssetsZip = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Assets.zip",
  [string]$OutputPath,
  [switch]$UseExistingCatalogs
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
if (-not $OutputPath) {
  $OutputPath = Join-Path $projectRoot ".build\offline-configurator\Configurable-Mob-Spawner-Configurator-offline.html"
}
$OutputPath = [System.IO.Path]::GetFullPath($OutputPath)
$outputDirectory = Split-Path -Parent $OutputPath

$htmlPath = Join-Path $PSScriptRoot "Configurador Spawner de Mobs.html"
$themePath = Join-Path $PSScriptRoot "theme-soft-light.css"
$itemCatalogPath = Join-Path $PSScriptRoot "asset-ids.generated.js"
$mobCatalogPath = Join-Path $PSScriptRoot "mob-preview.generated.js"
$playerReferencePath = Join-Path $PSScriptRoot "player-reference.png"
$itemGenerator = Join-Path $PSScriptRoot "Generate-WebAssetIds.ps1"
$mobGenerator = Join-Path $PSScriptRoot "Generate-WebMobPreviews.ps1"
$webTest = Join-Path $PSScriptRoot "Test-WebConfigurator.ps1"

if (-not $UseExistingCatalogs) {
  if (-not (Test-Path -LiteralPath $AssetsZip -PathType Leaf)) {
    throw "Hytale Assets.zip not found: $AssetsZip"
  }
  & $itemGenerator -AssetsZip $AssetsZip
  & $mobGenerator -AssetsZip $AssetsZip
}

foreach ($requiredPath in @(
    $htmlPath,
    $themePath,
    $itemCatalogPath,
    $mobCatalogPath,
    $playerReferencePath
  )) {
  if (-not (Test-Path -LiteralPath $requiredPath -PathType Leaf)) {
    throw "Required configurator input is missing: $requiredPath"
  }
}

& $webTest

$html = Get-Content -Encoding UTF8 -Raw -LiteralPath $htmlPath
$theme = Get-Content -Encoding UTF8 -Raw -LiteralPath $themePath
$itemCatalog = Get-Content -Encoding UTF8 -Raw -LiteralPath $itemCatalogPath
$mobCatalog = Get-Content -Encoding UTF8 -Raw -LiteralPath $mobCatalogPath
$base64Cache = @{}
$previewTokens = @{}
$previewData = [ordered]@{}
$embeddedImageCount = 0

function Convert-ImageToDataUri {
  param(
    [Parameter(Mandatory = $true)]
    [string]$ImagePath
  )

  $fullPath = [System.IO.Path]::GetFullPath($ImagePath)
  if (-not $fullPath.StartsWith($PSScriptRoot + [System.IO.Path]::DirectorySeparatorChar)) {
    throw "Refusing to embed an image outside the configurator directory: $fullPath"
  }
  if (-not (Test-Path -LiteralPath $fullPath -PathType Leaf)) {
    throw "Generated preview image is missing: $fullPath"
  }
  if ($base64Cache.ContainsKey($fullPath)) {
    return $base64Cache[$fullPath]
  }

  $dataUri = "data:image/png;base64," + [Convert]::ToBase64String(
      [System.IO.File]::ReadAllBytes($fullPath))
  $base64Cache[$fullPath] = $dataUri
  $script:embeddedImageCount++
  return $dataUri
}

$previewPattern = [regex]'"(?<path>(?:item|mob)-previews\.generated/[^"/]+\.png)"'
$embedPreview = [System.Text.RegularExpressions.MatchEvaluator]{
  param($match)
  $catalogPath = $match.Groups["path"].Value
  if ($previewTokens.ContainsKey($catalogPath)) {
    return '"' + $previewTokens[$catalogPath] + '"'
  }
  $relativePath = $catalogPath.Replace(
      '/', [System.IO.Path]::DirectorySeparatorChar)
  $dataUri = Convert-ImageToDataUri (Join-Path $PSScriptRoot $relativePath)
  $token = "p$($previewData.Count)"
  $previewTokens[$catalogPath] = $token
  $previewData[$token] = $dataUri
  return '"' + $token + '"'
}
$itemCatalog = $previewPattern.Replace($itemCatalog, $embedPreview)
$mobCatalog = $previewPattern.Replace($mobCatalog, $embedPreview)
$previewDataJson = ConvertTo-Json -InputObject $previewData -Compress
$previewHydrator = @"
<script id="embedded-preview-data">window.HYTALE_EMBEDDED_PREVIEWS=$previewDataJson;
for(const [id,token] of Object.entries(window.HYTALE_ITEM_PREVIEWS||{}))window.HYTALE_ITEM_PREVIEWS[id]=window.HYTALE_EMBEDDED_PREVIEWS[token]||token;
for(const preview of Object.values(window.HYTALE_MOB_PREVIEWS||{}))if(preview.image)preview.image=window.HYTALE_EMBEDDED_PREVIEWS[preview.image]||preview.image;
delete window.HYTALE_EMBEDDED_PREVIEWS;</script>
"@

# Asset names are translated strings. Prevent an unexpected value from closing
# the script element in the generated standalone document.
$itemCatalog = [regex]::Replace($itemCatalog, '(?i)</script', '<\/script')
$mobCatalog = [regex]::Replace($mobCatalog, '(?i)</script', '<\/script')

$playerReferenceUri = Convert-ImageToDataUri $playerReferencePath
$html = $html.Replace(
  '<link rel="stylesheet" href="theme-soft-light.css">',
  "<style id=`"embedded-soft-light-theme`">$theme</style>")
$html = $html.Replace(
  '<script src="asset-ids.generated.js"></script>',
  "<script id=`"embedded-item-catalog`">$itemCatalog</script>")
$html = $html.Replace(
  '<script src="mob-preview.generated.js"></script>',
  "<script id=`"embedded-mob-catalog`">$mobCatalog</script>$previewHydrator")
$html = $html.Replace(
  'src="player-reference.png"',
  'src="' + $playerReferenceUri + '"')
$html = $html.Replace(
  '<!doctype html>',
  "<!doctype html>`n<!-- Generated for internal testing from the local Hytale Assets.zip. Do not commit this file. -->")

foreach ($forbidden in @(
    'asset-ids.generated.js',
    'mob-preview.generated.js',
    'item-previews.generated/',
    'mob-previews.generated/',
    'src="player-reference.png"',
    'href="theme-soft-light.css"'
  )) {
  if ($html.Contains($forbidden)) {
    throw "Offline configurator still references an external asset: $forbidden"
  }
}
if (-not $html.Contains('data:image/png;base64,')) {
  throw "Offline configurator does not contain embedded PNG data."
}

New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
[System.IO.File]::WriteAllText(
  $OutputPath,
  $html,
  [System.Text.UTF8Encoding]::new($false))

$hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $OutputPath).Hash
$size = (Get-Item -LiteralPath $OutputPath).Length
Write-Output "Offline configurator: $OutputPath"
Write-Output "Embedded PNG files: $embeddedImageCount"
Write-Output ("Size: {0:N2} MiB" -f ($size / 1MB))
Write-Output "SHA256: $hash"
