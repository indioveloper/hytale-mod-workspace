$ErrorActionPreference = 'Stop'

$languageRoot = Join-Path $PSScriptRoot '..\src\Server\Languages'
$files = Get-ChildItem -LiteralPath $languageRoot -Recurse -Filter *.lang
if ($files.Count -eq 0) {
  throw "No se encontraron archivos .lang en $languageRoot"
}

foreach ($file in $files) {
  $lines = Get-Content -LiteralPath $file.FullName
  $invalidKeys = $lines |
      Where-Object { $_ -match '^\s*server\.' }
  if ($invalidKeys) {
    throw "'$($file.FullName)' no debe incluir el prefijo 'server.'. El cargador lo anade automaticamente."
  }

  $missingTooltip = $lines |
      Where-Object { $_ -match '^customUI\.triggerVolumeEffectEditor\.field\.' -and $_ -notmatch '\.(tooltip|placeholder|option\.)' } |
      ForEach-Object {
        $tooltipKey = ($_ -split '\s*=')[0] + '.tooltip'
        if (-not ($lines | Where-Object { $_ -match ('^' + [regex]::Escape($tooltipKey) + '\s*=') })) {
          $tooltipKey
        }
      }
  if ($missingTooltip) {
    throw "'$($file.FullName)' tiene campos de Trigger Volume sin tooltip: $($missingTooltip -join ', ')"
  }
}

Write-Host "Comprobacion de localizacion superada: $($files.Count) archivo(s) sin prefijos duplicados y con ayudas de campos."
