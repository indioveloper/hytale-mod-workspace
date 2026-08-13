$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$itemPath = Join-Path $projectRoot "Server\Item\Items\OrbGenesis\NoBuildStone\OrbGenesis_NoBuild_Stone.json"
$interactionPath = Join-Path $projectRoot "Server\Item\Interactions\OrbGenesis\NoBuildStone\OrbGenesis_NoBuild_Stone_Throw.json"
$projectilePath = Join-Path $projectRoot "Server\ProjectileConfigs\Weapons\Throwables\Projectile_Config_OrbGenesis_NoBuild_Stone.json"
$effectPath = Join-Path $projectRoot "Server\TriggerVolumes\Effects\OrbGenesis_NoBuild_5x5x5_10s.json"

$item = Get-Content -Raw -LiteralPath $itemPath | ConvertFrom-Json
$interaction = Get-Content -Raw -LiteralPath $interactionPath | ConvertFrom-Json
$projectile = Get-Content -Raw -LiteralPath $projectilePath | ConvertFrom-Json
$effect = Get-Content -Raw -LiteralPath $effectPath | ConvertFrom-Json

if ($item.Parent -ne "Rubble_Stone") {
  throw "No-build stone must inherit the vanilla Rubble_Stone item."
}
if ($item.Icon -ne "Icons/ItemsGenerated/Ingredient_Void_Essence.png" -or
    $item.Model -ne "Resources/Ingredients/Essence.blockymodel" -or
    $item.Texture -ne "Resources/Ingredients/Essence_Textures/Void_Essence_Texture.png" -or
    [double]$item.Scale -ne 0.8 -or
    [double]$item.IconProperties.Scale -ne 0.6) {
  throw "No-build stone must use the vanilla Ingredient_Void_Essence model and icon."
}
if ($interaction.Parent -ne "Rubble_Throw" -or
    $interaction.Config -ne "Projectile_Config_OrbGenesis_NoBuild_Stone") {
  throw "No-build throw interaction is not wired to its projectile config."
}

$rules = @($effect.Rules)
if ($rules.Count -ne 1 -or $rules[0].Type -ne "NoBuild") {
  throw "Trigger effect asset must retain one canonical NoBuild rule."
}

$modifyRules = @($effect.Effects | Where-Object { $_.Type -eq "ModifyRules" })
if ($modifyRules.Count -ne 2) {
  throw "VOLUME_CREATE must contain one rule activation and one delayed removal."
}
$setRule = @($modifyRules | Where-Object { $_.Operation -eq "Set" })
$removeRule = @($modifyRules | Where-Object { $_.Operation -eq "Remove" })
if ($setRule.Count -ne 1 -or
    $setRule[0].Event -ne "VOLUME_CREATE" -or
    [double]$setRule[0].Delay -ne 0.0 -or
    $setRule[0].Rule.Type -ne "NoBuild") {
  throw "VOLUME_CREATE must immediately SET the NoBuild rule."
}
if ($removeRule.Count -ne 1 -or
    $removeRule[0].Event -ne "VOLUME_CREATE" -or
    [double]$removeRule[0].Delay -ne 10.0 -or
    $removeRule[0].Rule.Type -ne "NoBuild") {
  throw "VOLUME_CREATE must REMOVE the NoBuild rule after 10 seconds."
}

$shapes = @($effect.Effects | Where-Object { $_.Type -eq "SpawnParticleShape" })
if ($shapes.Count -ne 3) {
  throw "VOLUME_CREATE must contain red, yellow-orange, and green particle spheres."
}

function Assert-ParticleSphere(
  [object]$Shape,
  [string]$ParticleSystem,
  [double]$Delay,
  [double]$Duration
) {
  if ($Shape.Event -ne "VOLUME_CREATE" -or
      $Shape.Shape -ne "SphereSurface" -or
      $Shape.ParticleSystem -ne $ParticleSystem -or
      $Shape.CoordinateMode -ne "RelativeToVolume" -or
      [double]$Shape.Center.X -ne 0.0 -or
      [double]$Shape.Center.Y -ne 0.0 -or
      [double]$Shape.Center.Z -ne 0.0 -or
      [double]$Shape.Size -ne 10.0 -or
      [double]$Shape.Spacing -ne 1.0 -or
      [double]$Shape.ParticleScale -ne 2.0 -or
      [double]$Shape.Delay -ne $Delay -or
      [double]$Shape.Duration -ne $Duration -or
      [int]$Shape.MaxPoints -ne 1500) {
    throw "Invalid particle sphere stage for $ParticleSystem."
  }
}

Assert-ParticleSphere ($shapes | Where-Object { $_.ParticleSystem -eq "OrbGenesis_Shape_Point_Red" }) "OrbGenesis_Shape_Point_Red" 0.0 9.0
Assert-ParticleSphere ($shapes | Where-Object { $_.ParticleSystem -eq "OrbGenesis_Shape_Point_YellowOrange" }) "OrbGenesis_Shape_Point_YellowOrange" 9.0 1.0
Assert-ParticleSphere ($shapes | Where-Object { $_.ParticleSystem -eq "OrbGenesis_Shape_Point_Green" }) "OrbGenesis_Shape_Point_Green" 10.0 1.0
if (@($effect.Effects | Where-Object { $_.Type -eq "PlayVfx" }).Count -ne 0) {
  throw "Legacy offset PlayVfx emitters must not remain in the no-build effect."
}

$sounds = @($effect.Effects | Where-Object { $_.Type -eq "PlaySound" })
if ($sounds.Count -ne 2) {
  throw "VOLUME_CREATE must play the portal sound at seconds 0 and 10."
}
foreach ($delay in @(0.0, 10.0)) {
  $sound = @($sounds | Where-Object { [double]$_.Delay -eq $delay })
  if ($sound.Count -ne 1 -or
      $sound[0].Event -ne "VOLUME_CREATE" -or
      $sound[0].SoundEvent -ne "SFX_PORTAL_NEUTRAL_OPEN" -or
      [double]$sound[0].Volume -ne 1.0 -or
      [double]$sound[0].Pitch -ne 1.0 -or
      $sound[0].Location -ne "VolumeCenter" -or
      [double]$sound[0].Offset.X -ne 0.0 -or
      [double]$sound[0].Offset.Y -ne 0.0 -or
      [double]$sound[0].Offset.Z -ne 0.0) {
    throw "Portal sound stage at second $delay is invalid."
  }
}

foreach ($eventName in @("ProjectileHit", "ProjectileMiss")) {
  $eventConfig = $projectile.Interactions.$eventName
  $spawns = @($eventConfig.Interactions | Where-Object { $_.Type -eq "SpawnTriggerVolume" })
  if ($spawns.Count -ne 1) {
    throw "$eventName must contain exactly one SpawnTriggerVolume interaction."
  }

  $spawn = $spawns[0]
  if ($spawn.EffectAsset -ne "OrbGenesis_NoBuild_5x5x5_10s") {
    throw "$eventName references the wrong Trigger Volume effect asset."
  }
  if ([double]$spawn.LifetimeS -ne 11.25 -or $spawn.RequireHitLocation -ne $true) {
    throw "$eventName must survive long enough to show the green unlocked phase."
  }
  if ($spawn.Shape.Type -ne "Box") {
    throw "$eventName must spawn a box-shaped volume."
  }

  foreach ($axis in @("X", "Y", "Z")) {
    $size = [double]$spawn.Shape.Max.$axis - [double]$spawn.Shape.Min.$axis
    if ($size -ne 10.0) {
      throw "$eventName volume size on $axis is $size instead of 10."
    }
  }

  $removals = @($eventConfig.Interactions | Where-Object {
      $_.Type -eq "RemoveEntity" -and $_.Entity -eq "User"
    })
  if ($removals.Count -ne 1) {
    throw "$eventName must remove the projectile after spawning the volume."
  }
}

Write-Output "OrbGenesis Mechanisms no-build projectile tests passed."
