param(
  [Parameter(Mandatory = $true)]
  [string]$ProjectPath,

  [string]$SourceRoot = "src",
  [string]$PackageRoot = ".",
  [string]$AssetsRoot,

  [string]$VanillaCustomUiAssetsZip,

  [string[]]$AdditionalCustomUiRoots,

  [string[]]$ForceCustomUiTransfer,

  [switch]$ExcludeCustomUi,

  [Parameter(Mandatory = $true)]
  [string]$ArtifactName,

  [string]$ServerJar = "$env:APPDATA\Hytale\install\release\package\game\latest\Server\HytaleServer.jar"
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
$source = (Resolve-Path -LiteralPath (Join-Path $project $SourceRoot)).Path
$package = (Resolve-Path -LiteralPath (Join-Path $project $PackageRoot)).Path

if ($ExcludeCustomUi -and ($VanillaCustomUiAssetsZip -or $AdditionalCustomUiRoots -or $ForceCustomUiTransfer)) {
  throw "-ExcludeCustomUi cannot be combined with hydrated or additional Custom UI roots."
}

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

# javac can exit 0 after an internal compiler exception (for example when the
# sandbox denies access to a classpath JAR). Never package an empty classes tree.
$compiledClasses = @(Get-ChildItem -LiteralPath $classes -Recurse -Filter *.class -File)
if ($compiledClasses.Count -eq 0) {
  throw "javac produced no .class files; refusing to package an invalid plugin."
}

Get-ChildItem -LiteralPath $classes -Force | ForEach-Object {
  Copy-Item -LiteralPath $_.FullName -Destination $stage -Recurse -Force
}

$manifest = Join-Path $package "manifest.json"
if (-not (Test-Path -LiteralPath $manifest -PathType Leaf)) {
  throw "manifest.json not found under package root: $package"
}
Copy-Item -LiteralPath $manifest -Destination $stage -Force

$iconSource = Join-Path $package "icon-256.png"
if (Test-Path -LiteralPath $iconSource -PathType Leaf) {
  Assert-ModIcon $iconSource
  Copy-Item -LiteralPath $iconSource -Destination $stage -Force
}

if ($VanillaCustomUiAssetsZip) {
  $vanillaAssets = (Resolve-Path -LiteralPath $VanillaCustomUiAssetsZip).Path
  Add-Type -AssemblyName System.IO.Compression.FileSystem
  $zip = [System.IO.Compression.ZipFile]::OpenRead($vanillaAssets)
  try {
    $customUiEntries = @($zip.Entries | Where-Object {
      $_.FullName.StartsWith("Common/UI/Custom/", [System.StringComparison]::Ordinal) -and $_.Name
    })
    if ($customUiEntries.Count -lt 100) {
      throw "Assets archive does not contain a complete vanilla Custom UI tree: $vanillaAssets"
    }

    foreach ($entry in $customUiEntries) {
      $relativePath = $entry.FullName.Replace("/", [System.IO.Path]::DirectorySeparatorChar)
      $target = [System.IO.Path]::GetFullPath((Join-Path $stage $relativePath))
      if (-not $target.StartsWith($stage + [System.IO.Path]::DirectorySeparatorChar)) {
        throw "Refusing to extract an asset outside the build stage: $($entry.FullName)"
      }
      $targetDirectory = Split-Path -Parent $target
      if (-not (Test-Path -LiteralPath $targetDirectory)) {
        New-Item -ItemType Directory -Path $targetDirectory -Force | Out-Null
      }
      [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $true)
    }
  }
  finally {
    $zip.Dispose()
  }
}

foreach ($assetDirectory in @("Common", "Server", "Samples")) {
  $candidate = Join-Path $package $assetDirectory
  if (Test-Path -LiteralPath $candidate -PathType Container) {
    Copy-Item -LiteralPath $candidate -Destination $stage -Recurse -Force
  }
}

if ($ExcludeCustomUi) {
  $customUiToRemove = Join-Path $stage "Common\UI\Custom"
  if (Test-Path -LiteralPath $customUiToRemove -PathType Container) {
    $resolvedCustomUiToRemove = [System.IO.Path]::GetFullPath($customUiToRemove)
    if (-not $resolvedCustomUiToRemove.StartsWith($stage + [System.IO.Path]::DirectorySeparatorChar)) {
      throw "Refusing to remove Custom UI outside the build stage: $resolvedCustomUiToRemove"
    }
    Remove-Item -LiteralPath $resolvedCustomUiToRemove -Recurse -Force
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

if ($AdditionalCustomUiRoots) {
  $customUiStage = Join-Path $stage "Common\UI\Custom"
  New-Item -ItemType Directory -Path $customUiStage -Force | Out-Null
  foreach ($customUiRoot in $AdditionalCustomUiRoots) {
    $resolvedCustomUiRoot = (Resolve-Path -LiteralPath $customUiRoot).Path
    if (-not (Test-Path -LiteralPath $resolvedCustomUiRoot -PathType Container)) {
      throw "Additional Custom UI root not found: $customUiRoot"
    }
    Get-ChildItem -LiteralPath $resolvedCustomUiRoot -Force | ForEach-Object {
      Copy-Item -LiteralPath $_.FullName -Destination $customUiStage -Recurse -Force
    }
  }
}

if ($ForceCustomUiTransfer) {
  $customUiStage = [System.IO.Path]::GetFullPath((Join-Path $stage "Common\UI\Custom"))
  foreach ($document in $ForceCustomUiTransfer) {
    if ([System.IO.Path]::IsPathRooted($document)) {
      throw "Forced Custom UI document must be relative to Common/UI/Custom: $document"
    }
    $normalizedDocument = $document.Replace("/", [System.IO.Path]::DirectorySeparatorChar)
    $target = [System.IO.Path]::GetFullPath((Join-Path $customUiStage $normalizedDocument))
    if (-not $target.StartsWith($customUiStage + [System.IO.Path]::DirectorySeparatorChar)) {
      throw "Refusing to modify a Custom UI document outside the build stage: $document"
    }
    if (-not (Test-Path -LiteralPath $target -PathType Leaf)) {
      throw "Forced Custom UI document not found in build stage: $document"
    }
    if ([System.IO.Path]::GetExtension($target) -ne ".ui") {
      throw "Only .ui documents can be forced into Custom UI transfer: $document"
    }

    # Update 7.7 de-duplicates byte-identical vanilla documents before sending
    # RequiredAssets. A trailing whitespace-only line preserves the markup while
    # giving selected compatibility documents a distinct transport hash.
    [System.IO.File]::AppendAllText(
      $target,
      "`r`n ",
      [System.Text.UTF8Encoding]::new($false)
    )
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
