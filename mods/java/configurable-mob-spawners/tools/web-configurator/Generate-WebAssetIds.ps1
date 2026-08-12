param(
  [string]$AssetsZip = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Assets.zip"
)

$ErrorActionPreference = "Stop"
if (-not (Test-Path -LiteralPath $AssetsZip -PathType Leaf)) {
  throw "Hytale Assets.zip not found: $AssetsZip"
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $AssetsZip).Path)
try {
  $ids = @($archive.Entries |
    Where-Object { $_.FullName.StartsWith("Server/Item/Items/", [StringComparison]::Ordinal) -and $_.FullName.EndsWith(".json", [StringComparison]::OrdinalIgnoreCase) } |
    ForEach-Object { [System.IO.Path]::GetFileNameWithoutExtension($_.Name) } |
    Where-Object { $_ } |
    Sort-Object -Unique)

  $idSet = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
  foreach ($id in $ids) { [void]$idSet.Add($id) }
  $iconEntries = @($archive.Entries | Where-Object {
      $_.FullName.StartsWith("Common/Icons/ItemsGenerated/", [StringComparison]::Ordinal) -and
      $_.FullName.EndsWith(".png", [StringComparison]::OrdinalIgnoreCase) -and
      $idSet.Contains([System.IO.Path]::GetFileNameWithoutExtension($_.Name))
    })

  $previewDirectory = Join-Path $PSScriptRoot "item-previews.generated"
  New-Item -ItemType Directory -Force -Path $previewDirectory | Out-Null
  Get-ChildItem -LiteralPath $previewDirectory -File -ErrorAction SilentlyContinue | Remove-Item -Force
  $previews = [ordered]@{}
  foreach ($entry in $iconEntries) {
    $id = [System.IO.Path]::GetFileNameWithoutExtension($entry.Name)
    $outputPath = Join-Path $previewDirectory "$id.png"
    $input = $entry.Open()
    $output = [System.IO.File]::Create($outputPath)
    try { $input.CopyTo($output) }
    finally { $input.Dispose(); $output.Dispose() }
    $previews[$id] = "item-previews.generated/$id.png"
  }
}
finally {
  $archive.Dispose()
}

$json = ConvertTo-Json -InputObject $ids -Compress
$previewJson = ConvertTo-Json -InputObject $previews -Compress
$target = Join-Path $PSScriptRoot "asset-ids.generated.js"
[System.IO.File]::WriteAllText(
  $target,
  "window.HYTALE_ITEM_IDS=$json;window.HYTALE_ITEM_PREVIEWS=$previewJson;",
  [System.Text.UTF8Encoding]::new($false))
Write-Output "Generated $($ids.Count) item IDs and $($previews.Count) item previews: $target"
