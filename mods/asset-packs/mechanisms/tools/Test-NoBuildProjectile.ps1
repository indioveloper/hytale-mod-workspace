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
if ($interaction.Parent -ne "Rubble_Throw" -or
    $interaction.Config -ne "Projectile_Config_OrbGenesis_NoBuild_Stone") {
  throw "No-build throw interaction is not wired to its projectile config."
}

$rules = @($effect.Rules)
if ($rules.Count -ne 1 -or $rules[0].Type -ne "NoBuild") {
  throw "Trigger effect asset must retain one canonical NoBuild rule."
}

$modifyRules = @($effect.Effects | Where-Object { $_.Type -eq "ModifyRules" })
if ($modifyRules.Count -ne 1 -or
    $modifyRules[0].Event -ne "VOLUME_CREATE" -or
    $modifyRules[0].Operation -ne "Set" -or
    $modifyRules[0].Rule.Type -ne "NoBuild") {
  throw "VOLUME_CREATE must SET the NoBuild rule to work around the 0.6.8 spawn bug."
}

$vfx = @($effect.Effects | Where-Object { $_.Type -eq "PlayVfx" })
if ($vfx.Count -ne 12) {
  throw "VOLUME_CREATE must contain one vanilla PlayVfx effect per cube edge."
}
foreach ($edge in $vfx) {
  if ($edge.Event -ne "VOLUME_CREATE" -or
      $edge.ParticleSystem -ne "Beam_Heal_Red3" -or
      $edge.Anchor -ne "Volume" -or
      [double]$edge.Scale -ne 5.0 -or
      [double]$edge.Duration -ne 10.0) {
    throw "Every edge must use the vanilla Beam_Heal_Red3 VFX at scale 5 for 10 seconds."
  }
}

$xEdges = @($vfx | Where-Object {
    [double]$_.Rotation.X -eq 0.0 -and
    [double]$_.Rotation.Y -eq 90.0 -and
    [double]$_.Rotation.Z -eq 0.0
  })
$yEdges = @($vfx | Where-Object {
    [double]$_.Rotation.X -eq -90.0 -and
    [double]$_.Rotation.Y -eq 0.0 -and
    [double]$_.Rotation.Z -eq 0.0
  })
$zEdges = @($vfx | Where-Object {
    [double]$_.Rotation.X -eq 0.0 -and
    [double]$_.Rotation.Y -eq 0.0 -and
    [double]$_.Rotation.Z -eq 0.0
  })
if ($xEdges.Count -ne 4 -or $yEdges.Count -ne 4 -or $zEdges.Count -ne 4) {
  throw "Perimeter must contain four correctly rotated effects for each axis."
}

foreach ($fixedA in @(-2.5, 2.5)) {
  foreach ($fixedB in @(-2.5, 2.5)) {
    if (@($xEdges | Where-Object {
          [double]$_.Offset.X -eq -2.5 -and
          [double]$_.Offset.Y -eq $fixedA -and
          [double]$_.Offset.Z -eq $fixedB
        }).Count -ne 1) {
      throw "Missing X edge starting at (-2.5, $fixedA, $fixedB)."
    }
    if (@($yEdges | Where-Object {
          [double]$_.Offset.X -eq $fixedA -and
          [double]$_.Offset.Y -eq -2.5 -and
          [double]$_.Offset.Z -eq $fixedB
        }).Count -ne 1) {
      throw "Missing Y edge starting at ($fixedA, -2.5, $fixedB)."
    }
    if (@($zEdges | Where-Object {
          [double]$_.Offset.X -eq $fixedA -and
          [double]$_.Offset.Y -eq $fixedB -and
          [double]$_.Offset.Z -eq -2.5
        }).Count -ne 1) {
      throw "Missing Z edge starting at ($fixedA, $fixedB, -2.5)."
    }
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
  if ([double]$spawn.LifetimeS -ne 10.0 -or $spawn.RequireHitLocation -ne $true) {
    throw "$eventName must require a hit location and expire after 10 seconds."
  }
  if ($spawn.Shape.Type -ne "Box") {
    throw "$eventName must spawn a box-shaped volume."
  }

  foreach ($axis in @("X", "Y", "Z")) {
    $size = [double]$spawn.Shape.Max.$axis - [double]$spawn.Shape.Min.$axis
    if ($size -ne 5.0) {
      throw "$eventName volume size on $axis is $size instead of 5."
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
