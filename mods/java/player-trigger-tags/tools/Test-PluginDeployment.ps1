param(
  [Parameter(Mandatory = $true)]
  [string]$GameDataRoot,
  [Parameter(Mandatory = $true)]
  [string]$SaveName,
  [Parameter(Mandatory = $true)]
  [string]$ArchivePath
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Get-PluginIdentity([string]$Path) {
  $zip = [System.IO.Compression.ZipFile]::OpenRead($Path)
  try {
    $entry = $zip.Entries | Where-Object { $_.FullName -eq 'manifest.json' } | Select-Object -First 1
    if ($null -eq $entry) { return $null }
    $reader = [System.IO.StreamReader]::new($entry.Open())
    try { return $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
  } finally { $zip.Dispose() }
}

$candidate = Get-PluginIdentity $ArchivePath
if ($null -eq $candidate) {
  throw "El paquete no contiene manifest.json: $ArchivePath"
}

$scanRoots = @(
  (Join-Path $GameDataRoot 'Mods'),
  (Join-Path $GameDataRoot (Join-Path 'Saves' (Join-Path $SaveName 'mods')))
)
$installedArchives = @(foreach ($root in $scanRoots) {
  if (-not (Test-Path -LiteralPath $root)) { continue }
  Get-ChildItem -LiteralPath $root -File | Where-Object { $_.Extension -in '.jar', '.zip' } | ForEach-Object {
    $manifest = Get-PluginIdentity $_.FullName
    if ($null -ne $manifest -and $manifest.Group -eq $candidate.Group -and $manifest.Name -eq $candidate.Name) {
      $_.FullName
    }
  }
})

$archiveHash = (Get-FileHash -LiteralPath $ArchivePath -Algorithm SHA256).Hash
if ($installedArchives.Count -eq 0) {
  Write-Host "Comprobacion superada: no hay versiones instaladas de $($candidate.Group):$($candidate.Name)."
  exit 0
}

if ($installedArchives.Count -eq 1) {
  $installedHash = (Get-FileHash -LiteralPath $installedArchives[0] -Algorithm SHA256).Hash
  if ($installedHash -eq $archiveHash) {
    Write-Host "Comprobacion superada: hay una sola instalacion y coincide con el paquete desplegado."
    exit 0
  }
}

if ($installedArchives) {
  $locations = $installedArchives -join [Environment]::NewLine
  throw "Despliegue cancelado: ya existe $($candidate.Group):$($candidate.Name). Retira o archiva estos paquetes antes de instalar una nueva version:`n$locations"
}

Write-Host "Comprobacion superada: no hay duplicados de $($candidate.Group):$($candidate.Name)."
