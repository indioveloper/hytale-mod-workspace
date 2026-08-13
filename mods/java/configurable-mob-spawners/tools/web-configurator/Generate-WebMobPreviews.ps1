param(
  [string]$AssetsZip = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Assets.zip"
)

$ErrorActionPreference = "Stop"
if (-not (Test-Path -LiteralPath $AssetsZip -PathType Leaf)) {
  throw "Hytale Assets.zip not found: $AssetsZip"
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.Drawing
$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $AssetsZip).Path)
try {
  $roles = @{}
  $rolePaths = @{}
  $models = @{}
  foreach ($entry in $archive.Entries) {
    $isRole = $entry.FullName.StartsWith("Server/NPC/Roles/", [StringComparison]::Ordinal)
    $isModel = $entry.FullName.StartsWith("Server/Models/", [StringComparison]::Ordinal)
    if ((-not $isRole -and -not $isModel) -or
        -not $entry.FullName.EndsWith(".json", [StringComparison]::OrdinalIgnoreCase)) { continue }
    $reader = [System.IO.StreamReader]::new($entry.Open())
    try {
      $json = $reader.ReadToEnd() | ConvertFrom-Json
    }
    catch {
      continue
    }
    finally {
      $reader.Dispose()
    }
    $id = [System.IO.Path]::GetFileNameWithoutExtension($entry.Name)
    if ($isRole) { $roles[$id] = $json; $rolePaths[$id] = $entry.FullName }
    else { $models[$id] = $json }
  }

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

  $icons = @{}
  foreach ($entry in $archive.Entries) {
    if ($entry.FullName.StartsWith("Common/Icons/ModelsGenerated/", [StringComparison]::Ordinal) -and
        $entry.FullName.EndsWith(".png", [StringComparison]::OrdinalIgnoreCase)) {
      $icons[[System.IO.Path]::GetFileNameWithoutExtension($entry.Name)] = $entry
    }
  }

  function Get-RoleChain([string]$RoleId) {
    $chain = [Collections.Generic.List[object]]::new()
    $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    while ($RoleId -and $seen.Add($RoleId) -and $roles.ContainsKey($RoleId)) {
      $role = $roles[$RoleId]
      $chain.Add($role)
      $RoleId = if ($role.Reference -is [string]) { $role.Reference } else { $null }
    }
    return $chain
  }

  function Get-ParameterValue([Collections.Generic.List[object]]$Chain, [string]$Name) {
    foreach ($role in $Chain) {
      if (-not $role.Parameters) { continue }
      $property = $role.Parameters.PSObject.Properties[$Name]
      if ($property -and $property.Value.PSObject.Properties["Value"]) {
        return $property.Value.Value
      }
    }
    return $null
  }

  function Resolve-RoleValue([string]$RoleId, [string]$Name) {
    $chain = Get-RoleChain $RoleId
    foreach ($role in $chain) {
      $value = $null
      $modified = if ($role.Modify) { $role.Modify.PSObject.Properties[$Name] } else { $null }
      $direct = $role.PSObject.Properties[$Name]
      if ($modified) { $value = $modified.Value }
      elseif ($direct) { $value = $direct.Value }
      else { continue }
      if ($value -is [int] -or $value -is [long] -or $value -is [double] -or $value -is [decimal]) {
        return $value
      }
      if ($value -is [string]) { return $value }
      $compute = $value.PSObject.Properties["Compute"]
      if ($compute) { return Get-ParameterValue $chain ([string]$compute.Value) }
    }
    return $null
  }

  function Resolve-ModelHeight([string]$ModelId) {
    $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    while ($ModelId -and $seen.Add($ModelId) -and $models.ContainsKey($ModelId)) {
      $model = $models[$ModelId]
      if ($model.HitBox -and $model.HitBox.Max -and $model.HitBox.Min) {
        return [Math]::Max(0.1, [double]$model.HitBox.Max.Y - [double]$model.HitBox.Min.Y)
      }
      $ModelId = if ($model.Parent -is [string]) { $model.Parent } else { $null }
    }
    return 1.85
  }

  function Get-RoleCategory([string]$RoleId, [string]$Appearance, [string]$DisplayName) {
    $parts = $rolePaths[$RoleId] -split '/'
    $family = if ($parts.Count -gt 3) { $parts[3] } else { "" }
    if ($family -eq "Boss" -or $DisplayName -match 'Hedera') { return "Bosses" }
    if ($RoleId -match 'Klops|Bramblekin|Slothian|Quest_Master') { return "Neutrales" }
    if ($family -in @("Aquatic", "Avian", "Creature") -or $Appearance -match 'Antelope|Armadillo|Bear|Bison|Boar|Bunny|Camel|Chicken|Cow|Deer|Fox|Frog|Goat|Horse|Mouflon|Pig|Rabbit|Ram|Sheep|Skrill|Turkey|Warthog') { return "Animales" }
    if ($RoleId -match 'Kweebec|Feran|Trork|Goblin|Scarak|Outlander|Tuluk') { return "Facciones" }
    if ($family -in @("Undead", "Elemental", "Void")) {
      if ($RoleId -match 'Sand|Sandswept|Desert') { return "Enemigos zona 2" }
      if ($RoleId -match 'Frost|Ice|Snow') { return "Enemigos zona 3" }
      if ($RoleId -match 'Incandescent|Fire|Flame|Void|Lava') { return "Enemigos zona 4" }
      return "Enemigos zona 1"
    }
    return "Otros"
  }

  function Get-RoleFaction([string]$RoleId) {
    foreach ($faction in @("Kweebec", "Feran", "Trork", "Goblin", "Scarak", "Outlander", "Tuluk")) {
      if ($RoleId -match $faction) { return $faction }
    }
    return $null
  }

  function Is-InternalRole([string]$RoleId) {
    return $RoleId -match '^(Test|Template|Empty|Component_)' -or $rolePaths[$RoleId] -match '/_Core/'
  }

  function Get-LegacyRoleCategory([string]$RoleId) {
    # Kept separate from the public category so role folders can still be inspected while debugging.
    $parts = $rolePaths[$RoleId] -split '/'
    $family = if ($parts.Count -gt 3) { $parts[3] } else { "" }
    $subfamily = if ($parts.Count -gt 4) { $parts[4] } else { "" }
    if ($family -in @("Aquatic", "Avian", "Creature")) { return "Animales" }
    if ($family -eq "Intelligent") {
      $group = if ($subfamily -in @("Aggressive", "Neutral", "Passive") -and $parts.Count -gt 5) { $parts[5] } else { $subfamily }
      $group = $group -replace '\.json$', ''
      if ($group -and $group -notmatch '^_' -and $group -notmatch 'Template|Component') { return $group -replace '_', ' ' }
    }
    if ($family -in @("Undead", "Elemental", "Void", "Boss")) { return "Enemigos" }
    return "Otros"
  }

  function Normalize-Attitude([string]$Attitude) {
    if ($Attitude -match 'Hostile') { return "HOSTILE" }
    if ($Attitude -match 'Neutral|Retaliate') { return "RETALIATE" }
    return "PASSIVE"
  }

  function Model-InheritsFromPlayer([string]$ModelId) {
    $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    while ($ModelId -and $seen.Add($ModelId) -and $models.ContainsKey($ModelId)) {
      if ($ModelId.Equals("Player", [StringComparison]::OrdinalIgnoreCase)) { return $true }
      $parent = $models[$ModelId].Parent
      $ModelId = if ($parent -is [string]) { $parent } else { $null }
    }
    return $false
  }

  function Get-VisibleBounds([string]$Path) {
    $bitmap = [System.Drawing.Bitmap]::FromFile($Path)
    try {
      $minX = $bitmap.Width
      $minY = $bitmap.Height
      $maxX = -1
      $maxY = -1
      for ($y = 0; $y -lt $bitmap.Height; $y++) {
        for ($x = 0; $x -lt $bitmap.Width; $x++) {
          if ($bitmap.GetPixel($x, $y).A -le 5) { continue }
          if ($x -lt $minX) { $minX = $x }
          if ($x -gt $maxX) { $maxX = $x }
          if ($y -lt $minY) { $minY = $y }
          if ($y -gt $maxY) { $maxY = $y }
        }
      }
      if ($maxX -lt 0) { return [ordered]@{ width = 128; height = 128; bottom = 0; canvas = 128 } }
      return [ordered]@{
        width = $maxX - $minX + 1
        height = $maxY - $minY + 1
        bottom = $bitmap.Height - 1 - $maxY
        canvas = $bitmap.Height
      }
    }
    finally {
      $bitmap.Dispose()
    }
  }

  $targetDir = Join-Path $PSScriptRoot "mob-previews.generated"
  New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
  Get-ChildItem -LiteralPath $targetDir -File -ErrorAction SilentlyContinue | Remove-Item -Force

  $mapping = [ordered]@{}
  $usedIcons = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
  $spawnableRoles = @($roles.Keys | Where-Object {
      $type = [string]$roles[$_].Type
      ($type -eq "Generic" -or $type -eq "Variant") -and -not (Is-InternalRole $_)
    } | Sort-Object)
  foreach ($roleId in $spawnableRoles) {
    $appearance = [string](Resolve-RoleValue $roleId "Appearance")
    $health = Resolve-RoleValue $roleId "MaxHealth"
    $nameKey = [string](Resolve-RoleValue $roleId "NameTranslationKey") -replace '^server\.', ''
    $displayName = if ($nameKey -and $translations.ContainsKey($nameKey)) { $translations[$nameKey] } else { $roleId -replace '_', ' ' }
    $attitude = Normalize-Attitude ([string](Resolve-RoleValue $roleId "DefaultPlayerAttitude"))
    $iconId = $null
    foreach ($candidate in @($roleId, $appearance, ($roleId -replace '_(Patrol|Wander)$', ''))) {
      if ($candidate -and $icons.ContainsKey($candidate)) { $iconId = $candidate; break }
    }
    if (-not $iconId -and $appearance) {
      $iconId = @($icons.Keys | Where-Object { $_.StartsWith("$appearance`_", [StringComparison]::OrdinalIgnoreCase) } | Sort-Object)[0]
    }
    if ($iconId) { [void]$usedIcons.Add($iconId) }
    $mapping[$roleId] = [ordered]@{
      image = if ($iconId) { "mob-previews.generated/$iconId.png" } else { $null }
      name = $displayName
      category = Get-RoleCategory $roleId $appearance $displayName
      faction = Get-RoleFaction $roleId
      attitude = $attitude
      appearance = $appearance
      health = if ($null -ne $health) { [double]$health } else { $null }
      height = Resolve-ModelHeight $appearance
      equipment = Model-InheritsFromPlayer $appearance
      alpha = $null
    }
  }

  $boundsByIcon = @{}
  foreach ($iconId in $usedIcons) {
    $entry = $icons[$iconId]
    $path = Join-Path $targetDir "$iconId.png"
    $input = $entry.Open()
    $output = [System.IO.File]::Create($path)
    try { $input.CopyTo($output) }
    finally { $input.Dispose(); $output.Dispose() }
    $boundsByIcon[$iconId] = Get-VisibleBounds $path
  }
  foreach ($roleId in $mapping.Keys) {
    $image = $mapping[$roleId].image
    if ($image) {
      $iconId = [System.IO.Path]::GetFileNameWithoutExtension($image)
      $mapping[$roleId].alpha = $boundsByIcon[$iconId]
    }
  }
}
finally {
  $archive.Dispose()
}

$json = ConvertTo-Json -InputObject $mapping -Compress -Depth 5
$target = Join-Path $PSScriptRoot "mob-preview.generated.js"
[System.IO.File]::WriteAllText($target, "window.HYTALE_MOB_PREVIEWS=$json;", [System.Text.UTF8Encoding]::new($false))
Write-Output "Generated $($mapping.Count) spawnable role mappings and $($usedIcons.Count) model previews: $targetDir"
