param(
  [string]$ServerJar = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Server\HytaleServer.jar",
  [string]$HyUiJar = "$env:APPDATA\Hytale\UserData\Mods\HyUI-0.9.8-all.jar"
)

$ErrorActionPreference = "Stop"

$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$source = Join-Path $project "src"
$buildRoot = Join-Path $project ".build"
$classes = Join-Path $buildRoot "classes"
$stage = Join-Path $buildRoot "stage"
$dist = Join-Path $buildRoot "dist"
$artifact = Join-Path $dist "DungeonCore-1.3.9-pre11.jar"

foreach ($dependency in @($ServerJar, $HyUiJar)) {
  if (-not (Test-Path -LiteralPath $dependency -PathType Leaf)) {
    throw "Required dependency not found: $dependency"
  }
}

foreach ($temporaryPath in @($classes, $stage)) {
  $fullPath = [System.IO.Path]::GetFullPath($temporaryPath)
  if (-not $fullPath.StartsWith($project + [System.IO.Path]::DirectorySeparatorChar)) {
    throw "Refusing to clean a path outside the project: $fullPath"
  }
  if (Test-Path -LiteralPath $fullPath) {
    Remove-Item -LiteralPath $fullPath -Recurse -Force
  }
}
New-Item -ItemType Directory -Path $classes, $stage, $dist -Force | Out-Null

$sources = @(Get-ChildItem -LiteralPath $source -Recurse -Filter *.java -File)
& javac -encoding UTF-8 -source 25 -target 25 -cp "$ServerJar;$HyUiJar" -d $classes $sources.FullName
if ($LASTEXITCODE -ne 0) {
  throw "javac failed with exit code $LASTEXITCODE"
}

Get-ChildItem -LiteralPath $classes -Force | ForEach-Object {
  Copy-Item -LiteralPath $_.FullName -Destination $stage -Recurse -Force
}
Copy-Item -LiteralPath (Join-Path $project "manifest.json") -Destination $stage -Force
Copy-Item -LiteralPath (Join-Path $project "icon-256.png") -Destination $stage -Force
Copy-Item -LiteralPath (Join-Path $project "LICENSE") -Destination $stage -Force
foreach ($assetDirectory in @("Common", "Server")) {
  Copy-Item -LiteralPath (Join-Path $project $assetDirectory) -Destination $stage -Recurse -Force
}

& jar --create --file $artifact -C $stage .
if ($LASTEXITCODE -ne 0) {
  throw "jar failed with exit code $LASTEXITCODE"
}
& jar --validate --file $artifact
if ($LASTEXITCODE -ne 0) {
  throw "jar validation failed with exit code $LASTEXITCODE"
}

$hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $artifact).Hash
Write-Output "Built: $artifact"
Write-Output "SHA256: $hash"
