param(
  [string]$ServerJar = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Server\HytaleServer.jar"
)

$ErrorActionPreference = "Stop"
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$source = Join-Path $project "src\gg\orbgenesis\particleshapevfx\ParticleShapePointGenerator.java"
$effectSource = Join-Path $project "src\gg\orbgenesis\particleshapevfx\SpawnParticleShapeEffect.java"
$test = Join-Path $PSScriptRoot "TestParticleShapePointGenerator.java"
$classes = Join-Path $project ".build\geometry-tests"

if (-not (Test-Path -LiteralPath $ServerJar -PathType Leaf)) {
  throw "HytaleServer.jar not found: $ServerJar"
}
if (Test-Path -LiteralPath $classes) {
  Remove-Item -LiteralPath $classes -Recurse -Force
}
New-Item -ItemType Directory -Path $classes -Force | Out-Null

& javac -encoding UTF-8 -cp $ServerJar -d $classes $source $effectSource $test
if ($LASTEXITCODE -ne 0) {
  throw "Geometry test compilation failed with exit code $LASTEXITCODE"
}
& java -cp "$classes;$ServerJar" gg.orbgenesis.particleshapevfx.TestParticleShapePointGenerator
if ($LASTEXITCODE -ne 0) {
  throw "Geometry tests failed with exit code $LASTEXITCODE"
}
