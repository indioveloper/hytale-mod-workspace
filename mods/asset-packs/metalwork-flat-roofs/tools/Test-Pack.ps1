param(
  [string]$ProjectPath = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = "Stop"
$project = (Resolve-Path -LiteralPath $ProjectPath).Path
$manifest = Get-Content -Raw -LiteralPath (Join-Path $project "manifest.json") | ConvertFrom-Json

if ($manifest.Group -ne "RaynorMods" -or $manifest.Name -ne "Metalwork Flat Roofs") {
  throw "Unexpected Group:Name in manifest.json"
}
if ($manifest.ServerVersion -ne ">=0.6.2 <0.7.0" -or -not $manifest.IncludesAssetPack) {
  throw "Unexpected compatibility or IncludesAssetPack value"
}

$modelPath = Join-Path $project "Common\Blocks\Raynor\MetalworkFlatRoofs\Metalwork_Roof_Flat.blockymodel"
$null = Get-Content -Raw -LiteralPath $modelPath | ConvertFrom-Json

function Get-PngDimensions([string]$Path) {
  $bytes = [System.IO.File]::ReadAllBytes($Path)
  $signature = [byte[]](137, 80, 78, 71, 13, 10, 26, 10)
  if ($bytes.Length -lt 24) {
    throw "PNG is too short: $Path"
  }
  for ($index = 0; $index -lt $signature.Length; $index++) {
    if ($bytes[$index] -ne $signature[$index]) {
      throw "Invalid PNG signature: $Path"
    }
  }
  $widthBytes = [byte[]]$bytes[16..19]
  $heightBytes = [byte[]]$bytes[20..23]
  [Array]::Reverse($widthBytes)
  [Array]::Reverse($heightBytes)
  return @([BitConverter]::ToUInt32($widthBytes, 0), [BitConverter]::ToUInt32($heightBytes, 0))
}

$items = @(Get-ChildItem -File -Filter "*.json" -LiteralPath (Join-Path $project "Server\Item\Items\Raynor\MetalworkFlatRoofs"))
if ($items.Count -ne 12) {
  throw "Expected 12 roof items, found $($items.Count)"
}

$en = Get-Content -Raw -LiteralPath (Join-Path $project "Server\Languages\en-US\server.lang")
$es = Get-Content -Raw -LiteralPath (Join-Path $project "Server\Languages\es-ES\server.lang")

foreach ($file in $items) {
  $asset = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json
  $isVoid = $file.BaseName -eq "Raynor_Metal_Void_Roof_Flat"
  $isVanillaCopy = $file.BaseName -in @("Raynor_Metal_Iron_Roof_Flat", "Raynor_Metal_Copper_Roof_Flat")
  if ($asset.Parent -ne "Rock_Stone_Brick_Roof_Flat" -and -not (($isVoid -or $isVanillaCopy) -and $asset.Parent -in @("Metal_Iron_Roof_Flat","Metal_Copper_Roof_Flat"))) {
    throw "$($file.Name) does not inherit the flat-roof behavior"
  }
  if ($asset.BlockType.VariantRotation -ne "UpDownNESW") {
    throw "$($file.Name) does not preserve flat-roof rotation"
  }
  $model = Join-Path $project ("Common\" + $asset.BlockType.CustomModel.Replace('/', '\'))
  $texture = Join-Path $project ("Common\" + $asset.BlockType.CustomModelTexture[0].Texture.Replace('/', '\'))
  $icon = Join-Path $project ("Common\" + $asset.Icon.Replace('/', '\'))
  $paths = @($model)
  if (-not $isVanillaCopy) { $paths += @($texture, $icon) }
  foreach ($path in $paths) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
      throw "$($file.Name) references missing asset: $path"
    }
  }
  $textureSize = if ($isVanillaCopy) { @(160,352) } else { Get-PngDimensions $texture }
  if ($isVoid) {
    if ($textureSize[0] -ne 160 -or $textureSize[1] -ne 352) { throw "Void atlas must match vanilla 160x352 UV layout" }
  } elseif (-not $isVanillaCopy -and ($textureSize[0] -ne 32 -or $textureSize[1] -ne 32)) {
    throw "$($file.Name) texture must remain 32x32, found $($textureSize[0])x$($textureSize[1])"
  }
  $key = $asset.TranslationProperties.Name -replace '^server\.', ''
  if ($en -notmatch [regex]::Escape($key) -or $es -notmatch [regex]::Escape($key)) {
    throw "$($file.Name) is missing en-US or es-ES localization"
  }
}

if (Get-ChildItem -Recurse -File -LiteralPath $project | Where-Object { $_.Name -match '^Metal_Roof_Flat\.blockymodel$' }) {
  throw "Vanilla Metal_Roof_Flat.blockymodel must not be copied into this repository"
}

Write-Output "Metalwork Flat Roofs validation passed."
$fixed = Get-Content -Raw (Join-Path $project "Common/Blocks/Raynor/MetalworkFlatRoofs/SeamSafe_Roof_Flat.blockymodel") | ConvertFrom-Json
if ($fixed.nodes.Count -ne 1) { throw "Seam-safe roof must have one solid shape" }
$node = $fixed.nodes[0]
foreach ($axis in @("x","z")) {
  $extent = $node.shape.settings.size.$axis * $node.shape.stretch.$axis
  if ($extent -ge 32 -or $extent -lt 31.9 -or $node.position.$axis -ne 0) { throw "Roof overlaps adjacent grid cells or has an excessive gap" }
}
if ($node.orientation.x -ne 0 -or $node.orientation.y -ne 0 -or $node.orientation.z -ne 0 -or $node.orientation.w -ne 1) { throw "Roof must be axis aligned" }
if ($node.shape.settings.size.y * $node.shape.stretch.y -ne 6 -or $node.position.y -ne 3) { throw "Roof must retain Y=0..6" }
Write-Output "Seam-safe geometry: single solid, no overhang, no tilt; bounds validated."
Write-Output "Roof items: $($items.Count)"
