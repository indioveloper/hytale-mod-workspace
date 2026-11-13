param(
  [string]$ProjectPath = (Split-Path -Parent $PSScriptRoot),
  [string]$ServerJar = (Join-Path $env:APPDATA "Hytale\install\pre-release\package\game\latest\Server\HytaleServer.jar")
)

$ErrorActionPreference = "Stop"

$project = (Resolve-Path -LiteralPath $ProjectPath).Path
$server = (Resolve-Path -LiteralPath $ServerJar).Path
$source = Join-Path $PSScriptRoot "ValidateWorldGenV2Codec.java"
$output = Join-Path $project ".build\codec-validator"
$biomes = @(Get-ChildItem -File -Filter "*.json" -LiteralPath (Join-Path $project "Server\HytaleGenerator\Biomes"))

New-Item -ItemType Directory -Force -Path $output | Out-Null

& javac -encoding UTF-8 -cp $server -d $output $source
if ($LASTEXITCODE -ne 0) {
  throw "Codec validator compilation failed with exit code $LASTEXITCODE"
}

foreach ($biome in $biomes) {
  & java -cp "$output;$server" ValidateWorldGenV2Codec $biome.FullName
  if ($LASTEXITCODE -ne 0) {
    throw "HytaleGenerator codec validation failed for '$($biome.Name)' with exit code $LASTEXITCODE"
  }
}
