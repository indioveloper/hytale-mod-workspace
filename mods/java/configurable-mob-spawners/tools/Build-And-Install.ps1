<#
.SYNOPSIS
  Compila Configurable Mob Spawners y lo instala en la partida indicada de la
  pre-release local.

.DESCRIPTION
  Ejecuta las tres pruebas del plugin, invoca scripts\Build-JavaMod.ps1 con los
  parametros documentados en README.md, valida el paquete resultante e instala
  el JAR en la carpeta mods de la partida destino. Los JAR previos del mismo
  paquete en esa partida se mueven a una subcarpeta de respaldo.

  La version del artefacto se lee de manifest.json para no duplicarla aqui.

.EXAMPLE
  .\tools\Build-And-Install.ps1

.EXAMPLE
  .\tools\Build-And-Install.ps1 -SaveName "mod spawners" -SkipTests
#>
[CmdletBinding()]
param(
  [string]$SaveName = "mod spawners",

  [string]$DataRoot = "$env:APPDATA\Hytale\data\pre-release",

  [string]$ServerJar = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Server\HytaleServer.jar",

  [string]$VanillaCustomUiAssetsZip = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Assets.zip",

  [switch]$SkipTests,

  [switch]$NoInstall
)

$ErrorActionPreference = "Stop"

$project = Split-Path -Parent $PSScriptRoot
$workspace = (Resolve-Path -LiteralPath (Join-Path $project "..\..\..")).Path
$buildScript = Join-Path $workspace "scripts\Build-JavaMod.ps1"

if (-not (Test-Path -LiteralPath $buildScript -PathType Leaf)) {
  throw "No se encuentra Build-JavaMod.ps1: $buildScript"
}

foreach ($required in @($ServerJar, $VanillaCustomUiAssetsZip)) {
  if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
    throw "Falta un archivo de la pre-release instalada: $required"
  }
}

foreach ($tool in @("javac", "jar")) {
  if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
    throw "'$tool' no esta en PATH. El JRE incluido con Hytale no basta: hace falta un JDK."
  }
}

$manifest = Get-Content -LiteralPath (Join-Path $project "manifest.json") -Raw | ConvertFrom-Json
$version = $manifest.Version
$artifactName = "ConfigurableMobSpawners-$version.jar"
Write-Host "Version segun manifest.json: $version" -ForegroundColor Cyan

Push-Location $project
try {
  if (-not $SkipTests) {
    foreach ($test in @(
      "tools\Test-PluginLocalization.ps1",
      "tools\Test-LightLevelMath.ps1",
      "tools\Test-ConfigString.ps1"
    )) {
      Write-Host "== $test" -ForegroundColor Cyan
      # Los Test-*.ps1 senalan el fallo con throw, no con codigo de salida.
      & (Join-Path $project $test)
    }
  }

  Write-Host "== Build" -ForegroundColor Cyan
  & $buildScript `
    -ProjectPath $project `
    -SourceRoot src `
    -PackageRoot . `
    -AssetsRoot assets `
    -ServerJar $ServerJar `
    -VanillaCustomUiAssetsZip $VanillaCustomUiAssetsZip `
    -ArtifactName $artifactName

  $artifact = Join-Path $project ".build\dist\$artifactName"
  if (-not (Test-Path -LiteralPath $artifact -PathType Leaf)) {
    throw "El build no produjo el artefacto esperado: $artifact"
  }

  Write-Host "== Test-Package" -ForegroundColor Cyan
  & (Join-Path $project "tools\Test-Package.ps1") -ArchivePath $artifact
}
finally {
  Pop-Location
}

if ($NoInstall) {
  Write-Host "Instalacion omitida (-NoInstall). Artefacto: $artifact" -ForegroundColor Yellow
  return
}

$targetMods = Join-Path $DataRoot "Saves\$SaveName\mods"
if (-not (Test-Path -LiteralPath (Split-Path -Parent $targetMods) -PathType Container)) {
  throw "No existe la partida '$SaveName' en $DataRoot\Saves"
}
if (-not (Test-Path -LiteralPath $targetMods -PathType Container)) {
  New-Item -ItemType Directory -Path $targetMods -Force | Out-Null
}

# Un mismo paquete cargado dos veces rompe el arranque del servidor: retirar
# cualquier version previa antes de copiar la nueva.
$previous = @(Get-ChildItem -LiteralPath $targetMods -Filter "ConfigurableMobSpawners-*.jar" -File -ErrorAction SilentlyContinue)
if ($previous.Count -gt 0) {
  $backup = Join-Path $targetMods ("backup-" + (Get-Date -Format "yyyyMMdd-HHmmss"))
  New-Item -ItemType Directory -Path $backup -Force | Out-Null
  foreach ($old in $previous) {
    Move-Item -LiteralPath $old.FullName -Destination $backup -Force
    Write-Host "Retirado: $($old.Name) -> $(Split-Path -Leaf $backup)" -ForegroundColor DarkGray
  }
}

Copy-Item -LiteralPath $artifact -Destination $targetMods -Force
$installed = Join-Path $targetMods $artifactName
$hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $installed).Hash

Write-Host ""
Write-Host "Instalado: $installed" -ForegroundColor Green
Write-Host "SHA256:    $hash" -ForegroundColor Green
Write-Host "Reinicia la partida '$SaveName' para cargar el mod." -ForegroundColor Green
