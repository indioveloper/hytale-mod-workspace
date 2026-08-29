$ErrorActionPreference = "Stop"
$htmlPath = Join-Path $PSScriptRoot "Configurador Spawner de Mobs.html"
$legacyEntryPath = Join-Path $PSScriptRoot "index.html"
$previewCatalogPath = Join-Path $PSScriptRoot "mob-preview.generated.js"
$previewDirectory = Join-Path $PSScriptRoot "mob-previews.generated"
$itemCatalogPath = Join-Path $PSScriptRoot "asset-ids.generated.js"
$itemPreviewDirectory = Join-Path $PSScriptRoot "item-previews.generated"
$playerReferencePath = Join-Path $PSScriptRoot "player-reference.png"
$lightThemePath = Join-Path $PSScriptRoot "theme-soft-light.css"
$lightEntryPath = Join-Path $PSScriptRoot "index-light.html"

$html = Get-Content -Encoding UTF8 -Raw -LiteralPath $htmlPath
$legacyEntry = Get-Content -Encoding UTF8 -Raw -LiteralPath $legacyEntryPath
if (-not $legacyEntry.Contains('Configurador%20Spawner%20de%20Mobs.html')) {
  throw "Legacy index.html does not target the renamed configurator."
}
foreach ($required in @("mobPreview", "mobName", "speedValue", 'id="speed"', "speed:n('speed')", "o.speed??1", "updateSpeed", "playerReference", "Modelo NPC", "identity-row", "tuning-row", "weight-row", "Nombre del mob", 'customName?`"${customName}"`:name', "roleOptions", "roleToggle", "roleModal", "roleCategories", "mob-preview.generated.js", "updatePreviewGeometry", "updateCombatStats", "previewTitle", "previewDescriptor", "previewHealth", "previewDefense", "previewAttack", "mob-stat-strip")) {
  if (-not $html.Contains($required)) { throw "Web preview hook is missing: $required" }
}
if (-not (Test-Path -LiteralPath $playerReferencePath -PathType Leaf) -or -not $html.Contains('player-reference.png')) {
  throw "Base player reference asset is missing."
}
foreach ($removed in @('playerReferenceId', 'loadPlayerReference', 'player-preview.generated.js', 'equipmentDock', 'updateEquipmentPreview', 'raynor')) {
  if ($html.Contains($removed)) { throw "Player UUID flow must not remain in the web configurator: $removed" }
}
if ($html.Contains('list="roleIds"') -or $html.Contains('<datalist id="roleIds"')) {
  throw "NPC selection must use the custom autocomplete instead of the filtering native datalist."
}
foreach ($required in @('itemModal', 'itemSearch', 'itemCategories', 'HYTALE_ITEM_GROUPS', 'data-item-filter="hand"', 'data-item-filter="block"', 'data-item-filter="head"', 'Escudos', 'Armaduras', 'Tejados', 'Bloque en la mano', 'Rango de spawn', 'has-item-selection', "classList.toggle('has-item-selection'", 'equipment-notes', 'son excluyentes entre s', 'solo puede mostrar en la mano', 'syncSpawnCounts', 'resetSpawnSimulation', 'animateSpawnerMap', 'activationLegend', 'lightLegend', 'spawnLegend', 'describeLoot', 'addTag', 'tagList', 'lootEditor', 'addLoot', 'clearItemSearch')) {
  if (-not $html.Contains($required)) { throw "Filtered item picker hook is missing: $required" }
}
foreach ($required in @('Mob Spawner Customizable', 'helpOpen', 'helpModal', 'helpClose', 'themeToggle', 'Modo oscuro', 'Modo claro', 'data-color-mode', 'cms-color-mode', 'applyColorMode', 'dentro del radio de activaci', 'dentro de ese radio', '1) Crea tu Mob', '2) Objetos equipados', '3) Loot', '4) Configura el Spawner', 'summary-section', 'Carga en el configurador un c', 'previamente de un Spawner.', '>Ejemplo<', 'Exportar Spawner', 'export-string', 'updateExport', 'Importar Spawner', 'Importar spawner desde el portapapeles', 'navigator.clipboard.readText()', 'Se va a sobreescribir esta configuraci', 'por el spawner que tienes en el portapapeles.', 'window.confirm(warning)', 'syncLootRow', 'syncAllLoot')) {
  if (-not $html.Contains($required)) { throw "Automatic export hook is missing: $required" }
}
foreach ($required in @('id="langEs"', 'id="langEn"', 'Idioma / Language', 'cms-language', 'applyLanguage', "url.searchParams.set('lang','en')", 'wip-badge', 'WIP · En desarrollo', 'WIP · Work in progress', '1) Create your Mob', '4) Configure the Spawner', 'Import spawner from clipboard', 'How does it work?')) {
  if (-not $html.Contains($required)) { throw "Bilingual interface hook is missing: $required" }
}
if (-not $html.Contains('a cualquier mob: aunque no se visualicen')) {
  throw "The armor visibility explanation is outdated."
}
foreach ($required in @('id="mobTabs"', 'addMobProfile', 'removeMobProfile', 'id="mobWeight"',
    'mobs:mobProfiles.map', 'profileFromLegacy', 'switchMobProfile', 'mobProfiles.length>=12',
    'id="eliteEnabled"', 'id="eliteChance"', 'id="elitePrefix"',
    'id="eliteHealthMultiplier"', 'id="eliteScaleMultiplier"',
    'id="eliteSpeedMultiplier"', 'id="eliteOverrideEquipment"',
    'id="eliteLoot"', 'id="eliteSettings"', 'id="eliteEquipment"')) {
  if (-not $html.Contains($required)) { throw "Compound/elite editor hook is missing: $required" }
}
if ($html.Contains('No utiliza conexi')) {
  throw "The removed offline subtitle must not remain visible."
}
foreach ($removed in @('id="enabled"', 'enabled-control', 'enabledHelp', 'enabled:$(''enabled'').checked')) {
  if ($html.Contains($removed)) { throw "Manual spawner toggle must not remain in the web configurator: $removed" }
}
if (-not [regex]::IsMatch($html, '(?s)<h2>1\) Crea tu Mob</h2>.*<h2>2\) Objetos equipados</h2>.*<h2>3\) Loot</h2>.*<h2>4\) Configura el Spawner</h2>')) {
  throw "Configurator sections are not in the expected Mob, Equipment, Loot, Spawner order."
}
$exportSection = [regex]::Match($html, '(?s)<section><h2>Exportar Spawner</h2>.*?</section>').Value
if (-not $exportSection -or $exportSection.Contains('id="load"')) {
  throw "Import Spawner must live in its own section, outside Export Spawner."
}
if ($html.Contains('id="generate"') -or $html.Contains('>Generar</button>') -or $html.Contains('<textarea id="output"')) {
  throw "Spawner export must be automatic and use a single-line field."
}
if ($html.Contains('list="itemIds"')) { throw "Item selection must use the lazy custom picker." }
if ($html.Contains('id="armorEnabled"')) { throw "Custom armor must be inferred from the selected slots." }
if ($html.Contains('id="mobNameplate"')) { throw "The HTML preview must not duplicate the mob name above the model." }
if ($html -match "blockCategoryDefinitions=.*\['Líquidos'" -or $html -match "blockCategoryDefinitions=.*\['Portales'") {
  throw "Fluid and portal tabs must not be exposed by the block picker."
}
if (-not (Test-Path -LiteralPath $lightThemePath -PathType Leaf) -or
    -not (Test-Path -LiteralPath $lightEntryPath -PathType Leaf)) {
  throw "Alternative soft-light theme files are missing."
}
$lightTheme = Get-Content -Encoding UTF8 -Raw -LiteralPath $lightThemePath
$lightEntry = Get-Content -Encoding UTF8 -Raw -LiteralPath $lightEntryPath
foreach ($required in @('data-theme="soft-light"', '#6661f8', '#49a9ba', '#d574a1')) {
  if (-not $lightTheme.Contains($required)) { throw "Soft-light theme token is missing: $required" }
}
if (-not $lightEntry.Contains('Configurador%20Spawner%20de%20Mobs.html?theme=soft-light')) {
  throw "Soft-light entry point does not target the renamed configurator."
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
if ($roleCount -lt 400 -or $roleCount -gt 500) { throw "Unexpected public role count: $roleCount" }
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
if ($catalog.Zombie_Burnt.attack -ne 30) {
  throw "Zombie Burnt base attack was not extracted from the installed role."
}
if ($catalog.Skeleton.name -ne "Skeleton" -or $catalog.Skeleton.category -ne "Enemigos zona 1" -or $catalog.Skeleton.attitude -ne "HOSTILE") {
  throw "Skeleton display metadata does not match the installed pre-release assets."
}

$previewCount = @(Get-ChildItem -LiteralPath $previewDirectory -Filter *.png -File).Count
if ($previewCount -lt 200) { throw "Expected at least 200 generated mob previews; found $previewCount." }
if (-not (Test-Path -LiteralPath $itemCatalogPath -PathType Leaf)) { throw "Generate the item preview catalog first." }
$itemRaw = Get-Content -Raw -LiteralPath $itemCatalogPath
foreach ($required in @('window.HYTALE_ITEM_CATALOG=', 'window.HYTALE_ITEM_GROUPS=', 'window.HYTALE_ITEM_STATS=', 'window.HYTALE_ITEM_PREVIEWS=')) {
  if (-not $itemRaw.Contains($required)) { throw "Item catalog section is missing: $required" }
}
$itemCatalogMatch = [regex]::Match($itemRaw, 'window\.HYTALE_ITEM_CATALOG=(\{.*?\});window\.HYTALE_ITEM_GROUPS=')
if (-not $itemCatalogMatch.Success) { throw "Could not parse item display names." }
$itemNames = $itemCatalogMatch.Groups[1].Value | ConvertFrom-Json
if ($itemRaw -match '"weapon":\[[^\]]*"Template_[^"]*"') { throw "Template items leaked into the weapon picker." }
$groupMatch = [regex]::Match($itemRaw, 'window\.HYTALE_ITEM_GROUPS=(\{.*?\});window\.HYTALE_ITEM_STATS=')
if (-not $groupMatch.Success) { throw "Could not parse item groups." }
$groups = $groupMatch.Groups[1].Value | ConvertFrom-Json
$statsMatch = [regex]::Match($itemRaw, 'window\.HYTALE_ITEM_STATS=(\{.*?\});window\.HYTALE_ITEM_PREVIEWS=')
if (-not $statsMatch.Success) { throw "Could not parse item combat stats." }
$itemStats = $statsMatch.Groups[1].Value | ConvertFrom-Json
if ($itemStats.Armor_Cloth_Cindercloth_Head.defensePercent -ne 5) {
  throw "Cindercloth Hood physical defense was not extracted."
}
if ($itemStats.Weapon_Longsword_Adamantite_Saurian.damage -ne 31) {
  throw "Adamantite Saurian Longsword basic damage was not extracted."
}
if (@($groups.shield).Count -lt 10) { throw "Shield category is unexpectedly small." }
if (@($groups.armor).Count -lt 50) { throw "Armor category is unexpectedly small." }
if (@($groups.armor | Where-Object { $_ -notin @($groups.hand) }).Count) { throw "Armor must be selectable as a held item." }
if (@($groups.tool | Where-Object { $_ -like 'Tool_Pickaxe_*' }).Count -lt 10) { throw "Inherited pickaxes are missing from the tool category." }
if (@($groups.consumable).Count -lt 50) { throw "Food and potion inheritance is missing from the consumable category." }
foreach ($food in @('Food_Bread', 'Food_Beef_Raw')) {
  if ($food -notin @($groups.consumable)) { throw "Expected food is missing from consumables: $food" }
}
if (@($groups.blockRoofs).Count -lt 100) { throw "Roof blocks are missing from their dedicated category." }
$roofInCloth = @($groups.blockCloth | Where-Object { $_ -match '(?i)(^|_)Roof(_|$)' })
if ($roofInCloth.Count) { throw "Roof block leaked into the cloth category: $($roofInCloth[0])" }
$lifeEssences = @($groups.item | Where-Object { $itemNames.$_ -eq 'Essence of Life' })
if ($lifeEssences.Count -ne 1) { throw "Expected one public Essence of Life; found $($lifeEssences.Count)." }
if (@($groups.block).Count -lt 2000) { throw "Block-only picker catalog is unexpectedly small." }
foreach ($group in @('item', 'block')) {
  $internal = @($groups.$group | Where-Object { $_ -match '(?i)(^|_)(Debug|Test|Template|Prototype)(_|$)' })
  if ($internal.Count) { throw "Internal assets leaked into $group picker: $($internal[0])" }
}
$blockSet = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
foreach ($id in @($groups.block)) { [void]$blockSet.Add($id) }
$overlap = @($groups.item | Where-Object { $blockSet.Contains($_) })
if ($overlap.Count) { throw "Block leaked into the regular item picker: $($overlap[0])" }
$itemPreviewCount = @(Get-ChildItem -LiteralPath $itemPreviewDirectory -Filter *.png -File).Count
if ($itemPreviewCount -lt 3000) { throw "Expected at least 3000 generated item previews; found $itemPreviewCount." }
Write-Output "Web configurator checks passed: $roleCount spawnable roles, $previewCount mob previews and $itemPreviewCount item previews."
