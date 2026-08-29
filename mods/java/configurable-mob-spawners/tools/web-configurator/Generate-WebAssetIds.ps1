param(
  [string]$AssetsZip = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Assets.zip"
)

$ErrorActionPreference = "Stop"
if (-not (Test-Path -LiteralPath $AssetsZip -PathType Leaf)) {
  throw "Hytale Assets.zip not found: $AssetsZip"
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $AssetsZip).Path)
try {
  $translations = @{}
  $languageEntry = $archive.GetEntry("Server/Languages/en-US/server.lang")
  if ($languageEntry) {
    $reader = [System.IO.StreamReader]::new($languageEntry.Open())
    try {
      foreach ($line in $reader.ReadToEnd() -split "`r?`n") {
        if ($line -match '^\s*([^=]+?)\s*=\s*(.+?)\s*$') { $translations[$matches[1]] = $matches[2] }
      }
    }
    finally { $reader.Dispose() }
  }

  $items = [ordered]@{}
  $itemStats = [ordered]@{}
  $itemRecords = @{}
  $itemPaths = @{}
  $itemGroups = [ordered]@{
    item = [Collections.Generic.List[string]]::new()
    hand = [Collections.Generic.List[string]]::new()
    weapon = [Collections.Generic.List[string]]::new()
    shield = [Collections.Generic.List[string]]::new()
    armor = [Collections.Generic.List[string]]::new()
    tool = [Collections.Generic.List[string]]::new()
    consumable = [Collections.Generic.List[string]]::new()
    utility = [Collections.Generic.List[string]]::new()
    other = [Collections.Generic.List[string]]::new()
    block = [Collections.Generic.List[string]]::new()
    blockRocks = [Collections.Generic.List[string]]::new()
    blockWood = [Collections.Generic.List[string]]::new()
    blockMetal = [Collections.Generic.List[string]]::new()
    blockCloth = [Collections.Generic.List[string]]::new()
    blockRoofs = [Collections.Generic.List[string]]::new()
    blockSoils = [Collections.Generic.List[string]]::new()
    blockOres = [Collections.Generic.List[string]]::new()
    blockPlants = [Collections.Generic.List[string]]::new()
    blockFluids = [Collections.Generic.List[string]]::new()
    blockPortals = [Collections.Generic.List[string]]::new()
    blockDeco = [Collections.Generic.List[string]]::new()
    head = [Collections.Generic.List[string]]::new()
    chest = [Collections.Generic.List[string]]::new()
    hands = [Collections.Generic.List[string]]::new()
    legs = [Collections.Generic.List[string]]::new()
  }
  $itemEntries = @($archive.Entries | Where-Object {
      $_.FullName.StartsWith("Server/Item/Items/", [StringComparison]::Ordinal) -and
      $_.FullName.EndsWith(".json", [StringComparison]::OrdinalIgnoreCase)
    })
  foreach ($entry in $itemEntries) {
    $id = [System.IO.Path]::GetFileNameWithoutExtension($entry.Name)
    if (-not $id -or $items.Contains($id)) { continue }
    $reader = [System.IO.StreamReader]::new($entry.Open())
    try { $item = $reader.ReadToEnd() | ConvertFrom-Json }
    finally { $reader.Dispose() }
    $translationKey = [string]$item.TranslationProperties.Name -replace '^server\.', ''
    $name = if ($translationKey -and $translations.ContainsKey($translationKey)) { $translations[$translationKey] } else { $id -replace '_', ' ' }
    $items[$id] = $name
    $itemRecords[$id] = $item
    $itemPaths[$id] = $entry.FullName
  }
  $ids = @($items.Keys | Sort-Object)

  function Get-BaseDamageValues([object]$Node) {
    if ($null -eq $Node -or $Node -is [string] -or $Node -is [ValueType]) { return @() }
    $values = [Collections.Generic.List[double]]::new()
    if ($Node -is [System.Collections.IEnumerable] -and $Node -isnot [pscustomobject]) {
      foreach ($entry in $Node) {
        foreach ($value in @(Get-BaseDamageValues $entry)) { $values.Add([double]$value) }
      }
      return $values.ToArray()
    }
    foreach ($property in $Node.PSObject.Properties) {
      if ($property.Name -eq "BaseDamage" -and $property.Value) {
        foreach ($damage in $property.Value.PSObject.Properties) {
          if ($damage.Value -is [ValueType] -and [double]$damage.Value -gt 0) {
            $values.Add([double]$damage.Value)
          }
        }
        continue
      }
      foreach ($value in @(Get-BaseDamageValues $property.Value)) { $values.Add([double]$value) }
    }
    return $values.ToArray()
  }

  $idSet = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
  foreach ($id in $ids) { [void]$idSet.Add($id) }
  $iconEntries = @($archive.Entries | Where-Object {
      $_.FullName.StartsWith("Common/Icons/ItemsGenerated/", [StringComparison]::Ordinal) -and
      $_.FullName.EndsWith(".png", [StringComparison]::OrdinalIgnoreCase) -and
      $idSet.Contains([System.IO.Path]::GetFileNameWithoutExtension($_.Name))
    })

  $previewDirectory = Join-Path $PSScriptRoot "item-previews.generated"
  New-Item -ItemType Directory -Force -Path $previewDirectory | Out-Null
  Get-ChildItem -LiteralPath $previewDirectory -File -ErrorAction SilentlyContinue | Remove-Item -Force
  $previews = [ordered]@{}
  foreach ($entry in $iconEntries) {
    $id = [System.IO.Path]::GetFileNameWithoutExtension($entry.Name)
    $outputPath = Join-Path $previewDirectory "$id.png"
    $input = $entry.Open()
    $output = [System.IO.File]::Create($outputPath)
    try { $input.CopyTo($output) }
    finally { $input.Dispose(); $output.Dispose() }
    $previews[$id] = "item-previews.generated/$id.png"
  }

  foreach ($id in $ids) {
    if (-not $previews.Contains($id)) { continue }
    $types = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    $categories = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    $currentId = $id
    $visited = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    $hasBlock = $false
    $hasTool = $false
    $hasConsumable = $false
    $armorSlot = ""
    $armorRecord = $null
    $damageValues = [Collections.Generic.List[double]]::new()
    $quality = [string]$itemRecords[$id].Quality
    $paths = [Collections.Generic.List[string]]::new()
    while ($currentId -and $itemRecords.ContainsKey($currentId) -and $visited.Add($currentId)) {
      $record = $itemRecords[$currentId]
      $paths.Add([string]$itemPaths[$currentId])
      foreach ($type in @($record.Tags.Type)) { if ($type) { [void]$types.Add([string]$type) } }
      foreach ($category in @($record.Categories)) { if ($category) { [void]$categories.Add([string]$category) } }
      if ($record.BlockType) { $hasBlock = $true }
      if ($record.Tool) { $hasTool = $true }
      if ($record.Consumable) { $hasConsumable = $true }
      if (-not $armorSlot -and $record.Armor) {
        $armorSlot = [string]$record.Armor.ArmorSlot
        $armorRecord = $record.Armor
      }
      if ($damageValues.Count -eq 0 -and $record.InteractionVars) {
        foreach ($value in @(Get-BaseDamageValues $record.InteractionVars)) {
          $damageValues.Add([double]$value)
        }
      }
      $currentId = [string]$record.Parent
    }
    $path = $paths -join ";"
    $name = [string]$items[$id]
    $isInternal = "$id;$name;$($itemPaths[$id])" -match '(?i)(^|[_/; ])(debug|test|template|prototype)([_/; ]|$)' -or
      $quality -match '^(?i:Debug|Developer|Template)$' -or $types.Contains("Debug") -or $types.Contains("Editor")
    $isRecipeVariant = [bool]$itemRecords[$id].Variant -and [bool]$itemRecords[$id].Recipe -and
      [string]$itemPaths[$id] -match '(?i)Recipes?/'
    if ($isInternal -or $isRecipeVariant) { continue }

    $stats = [ordered]@{}
    if ($damageValues.Count -gt 0) {
      $stats.damage = [Math]::Round(($damageValues | Measure-Object -Minimum).Minimum, 2)
    }
    if ($armorRecord) {
      $flatDefense = [double]$armorRecord.BaseDamageResistance
      $percentDefense = 0.0
      foreach ($modifier in @($armorRecord.DamageResistance.Physical)) {
        if (-not $modifier) { continue }
        if ([string]$modifier.CalculationType -match '(?i)Percent') {
          $percentDefense += [double]$modifier.Amount * 100.0
        }
        elseif ([string]$modifier.CalculationType -match '(?i)Flat') {
          $flatDefense += [double]$modifier.Amount
        }
      }
      $stats.defenseFlat = [Math]::Round($flatDefense, 2)
      $stats.defensePercent = [Math]::Round($percentDefense, 2)
    }
    if ($stats.Count -gt 0) { $itemStats[$id] = $stats }

    $nativeItem = @($categories | Where-Object { $_ -like 'Items.*' }).Count -gt 0 -or
      $path -match '(?i)/Items/(Food|Potion|Weapon|Tool|Armor|Ingredient|Recipe)/'
    if ($hasBlock -and -not $nativeItem) {
      $itemGroups.block.Add($id)
      if ($types.Contains("Ore") -or "$id;$path" -match '(?i)(^|[_/])Ore([_/]|$)') { $itemGroups.blockOres.Add($id) }
      elseif ($types.Contains("Wood") -or $types.Contains("Tree") -or $types.Contains("Trunk") -or
          "$id;$path" -match '(?i)(Wood|Tree|Trunk|Log|Branch)') { $itemGroups.blockWood.Add($id) }
      elseif ($types.Contains("Metal") -or "$id;$path" -match '(?i)(^|[_/])Metal([_/]|$)') { $itemGroups.blockMetal.Add($id) }
      elseif ("$id;$path" -match '(?i)(^|[_/;])(Roof|Thatch)([_/;]|$)') { $itemGroups.blockRoofs.Add($id) }
      elseif ($types.Contains("Cloth") -or "$id;$path" -match '(?i)(Cloth|Fabric)') { $itemGroups.blockCloth.Add($id) }
      elseif ($types.Contains("Soil") -or "$id;$path" -match '(?i)(Soil|Dirt|Grass|Clay|Sand)') { $itemGroups.blockSoils.Add($id) }
      elseif ($types.Contains("Plant") -or "$id;$path" -match '(?i)(Plant|Flower|Leaves|Sapling|Roots|Kelp|Mushroom)') { $itemGroups.blockPlants.Add($id) }
      elseif ($path -match '(?i)/Fluid/' -or $id -match '(?i)Fluid|Water|Lava') { $itemGroups.blockFluids.Add($id) }
      elseif ($types.Contains("Portal")) { $itemGroups.blockPortals.Add($id) }
      elseif ($types.Contains("Rock") -or "$id;$path" -match '(?i)(Rock|Stone|Brick|Cobble|Marble|Granite|Basalt)') { $itemGroups.blockRocks.Add($id) }
      else { $itemGroups.blockDeco.Add($id) }
      continue
    }

    $itemGroups.item.Add($id)
    if ($armorSlot) { $itemGroups.armor.Add($id); $itemGroups.hand.Add($id) }
    if ($armorSlot -eq "Head") { $itemGroups.head.Add($id); continue }
    if ($armorSlot -eq "Chest") { $itemGroups.chest.Add($id); continue }
    if ($armorSlot -eq "Hands") { $itemGroups.hands.Add($id); continue }
    if ($armorSlot -eq "Legs") { $itemGroups.legs.Add($id); continue }
    $itemGroups.hand.Add($id)
    if ($path -match '(?i)/Weapon/Shield/' -or $id -match '(?i)^Weapon_Shield_') { $itemGroups.shield.Add($id) }
    elseif ($types.Contains("Weapon") -or $path -match '(?i)/Weapon/') { $itemGroups.weapon.Add($id) }
    elseif ($hasTool -or $types.Contains("Tool") -or $path -match '(?i)/Tool/' -or $id -match '(?i)^Tool_') { $itemGroups.tool.Add($id) }
    elseif ($categories.Contains("Items.Recipes") -or $id -match '(?i)^Recipe_') { $itemGroups.other.Add($id) }
    elseif ($hasConsumable -or $types.Contains("Consumable") -or $types.Contains("Food") -or $types.Contains("Potion") -or
        $categories.Contains("Items.Foods") -or $categories.Contains("Items.Potions") -or $path -match '(?i)/(Food|Potion)/') { $itemGroups.consumable.Add($id) }
    elseif ($types.Contains("Utility") -or $types.Contains("Glider") -or $types.Contains("Explosive") -or $types.Contains("Container")) { $itemGroups.utility.Add($id) }
    else { $itemGroups.other.Add($id) }
  }
}
finally {
  $archive.Dispose()
}

$json = ConvertTo-Json -InputObject $ids -Compress
$catalogJson = ConvertTo-Json -InputObject $items -Compress
$groupsJson = ConvertTo-Json -InputObject $itemGroups -Compress
$statsJson = ConvertTo-Json -InputObject $itemStats -Compress
$previewJson = ConvertTo-Json -InputObject $previews -Compress
$target = Join-Path $PSScriptRoot "asset-ids.generated.js"
[System.IO.File]::WriteAllText(
  $target,
  "window.HYTALE_ITEM_IDS=$json;window.HYTALE_ITEM_CATALOG=$catalogJson;window.HYTALE_ITEM_GROUPS=$groupsJson;window.HYTALE_ITEM_STATS=$statsJson;window.HYTALE_ITEM_PREVIEWS=$previewJson;",
  [System.Text.UTF8Encoding]::new($false))
Write-Output "Generated $($ids.Count) item IDs, filtered equipment groups and $($previews.Count) item previews: $target"
