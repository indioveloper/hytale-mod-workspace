param(
  [string]$Channel = "pre-release",
  [string]$HytaleRoot = (Join-Path $env:APPDATA "Hytale")
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

$gameRoot = Join-Path $HytaleRoot "install\$Channel\package\game\latest"
$assetsZip = Join-Path $gameRoot "Assets.zip"
$nodeEditor = Join-Path $gameRoot "Client\NodeEditor\NodeEditor.exe"
$workspace = Join-Path $gameRoot "Client\NodeEditor\Workspaces\HytaleGenerator Java\_Workspace.json"

if (-not (Test-Path -LiteralPath $assetsZip -PathType Leaf)) {
  throw "Assets.zip not found: $assetsZip"
}

$zip = [System.IO.Compression.ZipFile]::OpenRead($assetsZip)
try {
  $entries = @($zip.Entries | Where-Object { $_.FullName -like "Server/HytaleGenerator/*" -and $_.Name })
  $groups = $entries | ForEach-Object {
    $parts = $_.FullName.Split('/')
    if ($parts.Length -ge 3) { $parts[2] }
  } | Group-Object | Sort-Object Name

  Write-Output "Assets: $assetsZip"
  Write-Output "Node Editor: $nodeEditor"
  Write-Output "Node workspace: $workspace"
  Write-Output "HytaleGenerator assets: $($entries.Count)"
  foreach ($group in $groups) {
    Write-Output ("  {0}: {1}" -f $group.Name, $group.Count)
  }
} finally {
  $zip.Dispose()
}

if (-not (Test-Path -LiteralPath $nodeEditor -PathType Leaf)) {
  Write-Warning "NodeEditor.exe was not found for channel '$Channel'."
}
if (-not (Test-Path -LiteralPath $workspace -PathType Leaf)) {
  Write-Warning "The HytaleGenerator Java node workspace was not found."
}
