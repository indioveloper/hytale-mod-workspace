$ErrorActionPreference = "Stop"

$projectPath = Split-Path -Parent $PSScriptRoot
$packPath = Join-Path $projectPath "behavior_pack"

$manifest = Get-Content -LiteralPath (Join-Path $packPath "manifest.json") -Raw | ConvertFrom-Json
$item = Get-Content -LiteralPath (Join-Path $packPath "items/admin_menu.json") -Raw | ConvertFrom-Json
$script = Get-Content -LiteralPath (Join-Path $packPath "scripts/main.js") -Raw

if ($manifest.header.uuid -eq $manifest.modules[0].uuid -or
    $manifest.header.uuid -eq $manifest.modules[1].uuid -or
    $manifest.modules[0].uuid -eq $manifest.modules[1].uuid) {
    throw "Los UUID del manifest deben ser distintos."
}

if ($item.'minecraft:item'.description.identifier -ne "raynor:admin_menu") {
    throw "El identificador del objeto no coincide."
}

if ($script -notmatch 'registerCustomComponent\("raynor:open_admin_menu"') {
    throw "El componente del objeto no se registra en el script."
}

Write-Host "OK: manifest, objeto y registro del componente validados."
& node (Join-Path $PSScriptRoot "Test-Menu.mjs")
if ($LASTEXITCODE -ne 0) { throw "Fallo en las pruebas del menu." }
