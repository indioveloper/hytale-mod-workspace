$ErrorActionPreference = "Stop"
$projectPath = Split-Path -Parent $PSScriptRoot
$packPath = Join-Path $projectPath "behavior_pack"
$manifest = Get-Content -LiteralPath (Join-Path $packPath "manifest.json") -Raw | ConvertFrom-Json
$script = Get-Content -LiteralPath (Join-Path $packPath "scripts/main.js") -Raw
$core = Get-Content -LiteralPath (Join-Path $packPath "scripts/core.js") -Raw
$command = Get-Content -LiteralPath (Join-Path $packPath "functions/diamantes.mcfunction") -Raw
$uuids = @($manifest.header.uuid, $manifest.modules[0].uuid, $manifest.modules[1].uuid)
if (($uuids | Select-Object -Unique).Count -ne 3) { throw "Los UUID deben ser distintos." }
if ($manifest.header.name -notmatch 'v1\.0\.2') { throw "El nombre debe mostrar la version." }
if ($command.Trim() -ne 'tag @s add raynor_diamond_walker_toggle') { throw "La funcion no marca al jugador." }
if ($script -notmatch 'hasTag\(TOGGLE_TAG\)') { throw "Falta el receptor de la marca." }
if ($core -notmatch '!block\.isAir') { throw "El script debe excluir el aire." }
if ($script -notmatch 'minecraft:diamond_block') { throw "Falta el bloque de diamante." }
& node (Join-Path $PSScriptRoot "Test-Core.mjs")
if ($LASTEXITCODE -ne 0) { throw "Fallaron las pruebas de logica." }
Write-Host "OK: manifest, funcion, marca y transformacion validados."
