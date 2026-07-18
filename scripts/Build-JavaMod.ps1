param(
  [Parameter(Mandatory = $true)]
  [string]$ProjectPath,

  [string]$SourceRoot = "src",
  [string]$PackageRoot = ".",
  [string]$AssetsRoot,

  [Parameter(Mandatory = $true)]
  [string]$ArtifactName,

  [string]$ServerJar = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Server\HytaleServer.jar"
)

$ErrorActionPreference = "Stop"

$project = (Resolve-Path -LiteralPath $ProjectPath).Path
$source = (Resolve-Path -LiteralPath (Join-Path $project $SourceRoot)).Path
$package = (Resolve-Path -LiteralPath (Join-Path $project $PackageRoot)).Path

if (-not (Test-Path -LiteralPath $ServerJar -PathType Leaf)) {
  throw "HytaleServer.jar not found: $ServerJar"
}

$javaSources = @(Get-ChildItem -LiteralPath $source -Recurse -Filter *.java -File)
if ($javaSources.Count -eq 0) {
  throw "No Java sources found under $source"
}

$buildRoot = Join-Path $project ".build"
$classes = Join-Path $buildRoot "classes"
$stage = Join-Path $buildRoot "stage"
$dist = Join-Path $buildRoot "dist"

foreach ($temporaryPath in @($classes, $stage)) {
  $fullTemporaryPath = [System.IO.Path]::GetFullPath($temporaryPath)
  if (-not $fullTemporaryPath.StartsWith($project + [System.IO.Path]::DirectorySeparatorChar)) {
    throw "Refusing to clean a path outside the project: $fullTemporaryPath"
  }
  if (Test-Path -LiteralPath $fullTemporaryPath) {
    Remove-Item -LiteralPath $fullTemporaryPath -Recurse -Force
  }
}
New-Item -ItemType Directory -Path $classes, $stage, $dist -Force | Out-Null

& javac -encoding UTF-8 -cp $ServerJar -d $classes $javaSources.FullName
if ($LASTEXITCODE -ne 0) {
  throw "javac failed with exit code $LASTEXITCODE"
}

Get-ChildItem -LiteralPath $classes -Force | ForEach-Object {
  Copy-Item -LiteralPath $_.FullName -Destination $stage -Recurse -Force
}

$manifest = Join-Path $package "manifest.json"
if (-not (Test-Path -LiteralPath $manifest -PathType Leaf)) {
  throw "manifest.json not found under package root: $package"
}
Copy-Item -LiteralPath $manifest -Destination $stage -Force

foreach ($assetDirectory in @("Common", "Server")) {
  $candidate = Join-Path $package $assetDirectory
  if (Test-Path -LiteralPath $candidate -PathType Container) {
    Copy-Item -LiteralPath $candidate -Destination $stage -Recurse -Force
  }
}

if ($AssetsRoot) {
  $assets = (Resolve-Path -LiteralPath (Join-Path $project $AssetsRoot)).Path
  foreach ($assetDirectory in @("Common", "Server")) {
    $candidate = Join-Path $assets $assetDirectory
    if (Test-Path -LiteralPath $candidate -PathType Container) {
      Copy-Item -LiteralPath $candidate -Destination $stage -Recurse -Force
    }
  }
}

$artifact = Join-Path $dist $ArtifactName
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
