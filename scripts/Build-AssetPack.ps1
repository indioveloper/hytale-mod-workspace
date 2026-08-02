param(
  [Parameter(Mandatory = $true)]
  [string]$ProjectPath,

  [Parameter(Mandatory = $true)]
  [string]$ArtifactName
)

$ErrorActionPreference = "Stop"

function Assert-ModIcon([string]$IconPath) {
  $bytes = [System.IO.File]::ReadAllBytes($IconPath)
  $signature = [byte[]](137, 80, 78, 71, 13, 10, 26, 10)
  $signatureMatches = $bytes.Length -ge 8
  for ($index = 0; $signatureMatches -and $index -lt $signature.Length; $index++) {
    $signatureMatches = $bytes[$index] -eq $signature[$index]
  }
  if ($bytes.Length -lt 24 -or -not $signatureMatches) {
    throw "Mod icon must be a valid PNG: $IconPath"
  }

  $widthBytes = [byte[]]$bytes[16..19]
  $heightBytes = [byte[]]$bytes[20..23]
  [Array]::Reverse($widthBytes)
  [Array]::Reverse($heightBytes)
  $width = [BitConverter]::ToUInt32($widthBytes, 0)
  $height = [BitConverter]::ToUInt32($heightBytes, 0)
  if ($width -ne 256 -or $height -ne 256) {
    throw "Mod icon must be 256x256 pixels, found ${width}x${height}: $IconPath"
  }
}

$project = (Resolve-Path -LiteralPath $ProjectPath).Path
$manifest = Join-Path $project "manifest.json"
if (-not (Test-Path -LiteralPath $manifest -PathType Leaf)) {
  throw "manifest.json not found: $manifest"
}

$buildRoot = Join-Path $project ".build"
$stage = Join-Path $buildRoot "package"
$dist = Join-Path $buildRoot "dist"

$fullStage = [System.IO.Path]::GetFullPath($stage)
if (-not $fullStage.StartsWith($project + [System.IO.Path]::DirectorySeparatorChar)) {
  throw "Refusing to clean a path outside the project: $fullStage"
}
if (Test-Path -LiteralPath $fullStage) {
  Remove-Item -LiteralPath $fullStage -Recurse -Force
}
New-Item -ItemType Directory -Path $stage, $dist -Force | Out-Null

Copy-Item -LiteralPath $manifest -Destination $stage
$icon = Join-Path $project "icon-256.png"
if (Test-Path -LiteralPath $icon -PathType Leaf) {
  Assert-ModIcon $icon
  Copy-Item -LiteralPath $icon -Destination $stage
}

foreach ($assetDirectory in @("Common", "Server")) {
  $source = Join-Path $project $assetDirectory
  if (Test-Path -LiteralPath $source -PathType Container) {
    Copy-Item -LiteralPath $source -Destination $stage -Recurse
  }
}

$artifact = Join-Path $dist $ArtifactName
if (Test-Path -LiteralPath $artifact) {
  Remove-Item -LiteralPath $artifact -Force
}

# jar always writes portable forward-slash entry names. Compress-Archive on
# Windows writes backslashes, which Hytale mounts but cannot resolve as assets.
& jar --create --file $artifact --no-manifest -C $stage .
if ($LASTEXITCODE -ne 0) {
  throw "jar failed with exit code $LASTEXITCODE"
}

& jar --validate --file $artifact
if ($LASTEXITCODE -ne 0) {
  throw "archive validation failed with exit code $LASTEXITCODE"
}

$entries = @(jar tf $artifact)
if ($LASTEXITCODE -ne 0) {
  throw "Could not inspect archive: $artifact"
}
if (@($entries | Where-Object { $_ -match "\\" }).Count -gt 0) {
  throw "Archive contains Windows-style entry names."
}

$hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $artifact).Hash
Write-Output "Built: $artifact"
Write-Output "SHA256: $hash"
