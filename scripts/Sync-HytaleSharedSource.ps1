param(
  [string]$Destination = (Join-Path (Split-Path $PSScriptRoot -Parent) "..\hytale-shared-source"),
  [string]$Branch = "pre-release"
)

$ErrorActionPreference = "Stop"
$resolvedParent = Split-Path -Parent ([System.IO.Path]::GetFullPath($Destination))
New-Item -ItemType Directory -Path $resolvedParent -Force | Out-Null

if (Test-Path -LiteralPath (Join-Path $Destination ".git")) {
  & git -C $Destination fetch origin $Branch
  if ($LASTEXITCODE -ne 0) { throw "git fetch failed" }
  & git -C $Destination switch $Branch
  if ($LASTEXITCODE -ne 0) { throw "git switch failed" }
  & git -C $Destination pull --ff-only origin $Branch
  if ($LASTEXITCODE -ne 0) { throw "git pull failed" }
} else {
  & git clone --branch $Branch --single-branch `
    https://github.com/HypixelStudios/hytale-shared-source.git $Destination
  if ($LASTEXITCODE -ne 0) { throw "git clone failed" }
}

& git -C $Destination log -1 --oneline
