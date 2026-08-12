$ErrorActionPreference = "Stop"
$htmlPath = Join-Path $PSScriptRoot "index.html"
$previewCatalogPath = Join-Path $PSScriptRoot "mob-preview.generated.js"
$previewDirectory = Join-Path $PSScriptRoot "mob-previews.generated"
$itemCatalogPath = Join-Path $PSScriptRoot "asset-ids.generated.js"
$itemPreviewDirectory = Join-Path $PSScriptRoot "item-previews.generated"
$lightThemePath = Join-Path $PSScriptRoot "theme-soft-light.css"
$lightEntryPath = Join-Path $PSScriptRoot "index-light.html"

$html = Get-Content -Encoding UTF8 -Raw -LiteralPath $htmlPath
foreach ($required in @("mobPreview", "playerReference", "equipmentDock", "Modelo NPC", "roleOptions", "roleToggle", "openRoleOptions", "mob-preview.generated.js", "updatePreviewGeometry")) {
  if (-not $html.Contains($required)) { throw "Web preview hook is missing: $required" }
}
if ($html.Contains('list="roleIds"') -or $html.Contains('<datalist id="roleIds"')) {
  throw "NPC selection must use the custom autocomplete instead of the filtering native datalist."
}
if (-not (Test-Path -LiteralPath $lightThemePath -PathType Leaf) -or
    -not (Test-Path -LiteralPath $lightEntryPath -PathType Leaf)) {
  throw "Alternative soft-light theme files are missing."
}
$lightTheme = Get-Content -Encoding UTF8 -Raw -LiteralPath $lightThemePath
foreach ($required in @('data-theme="soft-light"', '#6661f8', '#49a9ba', '#d574a1')) {
  if (-not $lightTheme.Contains($required)) { throw "Soft-light theme token is missing: $required" }
}
if ($html.Contains('id="lightMin"')) { throw "Minimum light must not be exposed by the web configurator." }

$scriptMatches = [regex]::Matches($html, '<script(?![^>]*\bsrc=)[^>]*>([\s\S]*?)</script>')
if ($scriptMatches.Count -eq 0) { throw "Inline web configurator script not found." }
$scriptSource = ($scriptMatches | ForEach-Object { $_.Groups[1].Value }) -join ";`n"
$temporaryScript = Join-Path $env:TEMP "configurable-spawner-web-check.js"
[System.IO.File]::WriteAllText($temporaryScript, $scriptSource, [System.Text.UTF8Encoding]::new($false))
& node --check $temporaryScript
if ($LASTEXITCODE -ne 0) { throw "Web configurator JavaScript syntax check failed." }

if (-not (Test-Path -LiteralPath $previewCatalogPath -PathType Leaf)) {
  throw "Generate the mob preview catalog first: Generate-WebMobPreviews.ps1"
}
$raw = Get-Content -Raw -LiteralPath $previewCatalogPath
$prefix = "window.HYTALE_MOB_PREVIEWS="
if (-not $raw.StartsWith($prefix)) { throw "Invalid mob preview catalog." }
$catalog = $raw.Substring($prefix.Length).TrimEnd(';') | ConvertFrom-Json
$roleCount = @($catalog.PSObject.Properties).Count
if ($roleCount -lt 700 -or $roleCount -gt 900) { throw "Unexpected spawnable role count: $roleCount" }
foreach ($internalRole in @("Component_ActionList_Sleep", "Component_Goblin_Instruction_Pre_Checks", "Template_Goblin")) {
  if ($catalog.PSObject.Properties[$internalRole]) { throw "Internal NPC builder leaked into the role catalog: $internalRole" }
}
foreach ($role in @("Skeleton", "Skeleton_Fighter")) {
  $preview = $catalog.$role
  if (-not $preview -or -not $preview.image) { throw "Missing preview mapping for $role." }
  if ($preview.health -le 0 -or $preview.height -le 0) { throw "Missing baseline stats for $role." }
  $imagePath = Join-Path $PSScriptRoot $preview.image.Replace('/', '\')
  if (-not (Test-Path -LiteralPath $imagePath -PathType Leaf)) { throw "Missing preview image: $imagePath" }
}
if ($catalog.Skeleton.health -ne 92 -or [Math]::Abs($catalog.Skeleton.height - 1.8) -gt 0.001) {
  throw "Skeleton baseline stats do not match the installed pre-release assets."
}

$previewCount = @(Get-ChildItem -LiteralPath $previewDirectory -Filter *.png -File).Count
if ($previewCount -lt 200) { throw "Expected at least 200 generated mob previews; found $previewCount." }
if (-not (Test-Path -LiteralPath $itemCatalogPath -PathType Leaf)) { throw "Generate the item preview catalog first." }
$itemPreviewCount = @(Get-ChildItem -LiteralPath $itemPreviewDirectory -Filter *.png -File).Count
if ($itemPreviewCount -lt 3000) { throw "Expected at least 3000 generated item previews; found $itemPreviewCount." }
Write-Output "Web configurator checks passed: $roleCount spawnable roles, $previewCount mob previews and $itemPreviewCount item previews."
