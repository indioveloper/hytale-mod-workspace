$ErrorActionPreference = "Stop"
$projectPath = Split-Path -Parent $PSScriptRoot
$packPath = Join-Path $projectPath "behavior_pack"
$manifest = Get-Content -LiteralPath (Join-Path $packPath "manifest.json") -Raw | ConvertFrom-Json
$item = Get-Content -LiteralPath (Join-Path $packPath "items/mob_bucket.json") -Raw | ConvertFrom-Json
$script = Get-Content -LiteralPath (Join-Path $packPath "scripts/main.js") -Raw
$uuids = @($manifest.header.uuid, $manifest.modules[0].uuid, $manifest.modules[1].uuid)
if (($uuids | Select-Object -Unique).Count -ne 3) { throw "Los UUID deben ser distintos." }
if ($item.'minecraft:item'.description.identifier -ne "raynor:mob_bucket") { throw "ID de objeto incorrecto." }
if ($item.'minecraft:item'.components.'minecraft:max_stack_size' -ne 1) { throw "El cubo debe ser no apilable." }
if ($script -notmatch 'playerInteractWithEntity') { throw "Falta el evento de captura." }
if ($script -notmatch 'registerCustomComponent\("raynor:release_mob"') { throw "Falta el componente de liberacion." }
& node (Join-Path $PSScriptRoot "Test-Core.mjs")
if ($LASTEXITCODE -ne 0) { throw "Fallaron las pruebas de logica." }
Write-Host "OK: manifest, objeto, receta y script validados."
