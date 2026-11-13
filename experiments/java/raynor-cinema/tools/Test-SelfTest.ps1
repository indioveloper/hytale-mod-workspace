param(
  [string]$ServerJar = "$env:APPDATA\Hytale\install\release\package\game\latest\Server\HytaleServer.jar",
  [string]$JarPath = (Join-Path (Split-Path $PSScriptRoot -Parent) ".build\dist\Raynor_Cinema-0.1.0.jar")
)

$ErrorActionPreference = "Stop"
$project = Split-Path $PSScriptRoot -Parent
$testSource = Join-Path $project "tests\java\gg\raynormods\cinema\CinemaCoreSelfTest.java"
$testClasses = Join-Path $project ".build\test-classes"

if (-not (Test-Path -LiteralPath $ServerJar -PathType Leaf)) { throw "Server JAR not found: $ServerJar" }
if (-not (Test-Path -LiteralPath $JarPath -PathType Leaf)) { throw "Cinema JAR not found: $JarPath" }
if (Test-Path -LiteralPath $testClasses) { Remove-Item -LiteralPath $testClasses -Recurse -Force }
New-Item -ItemType Directory -Path $testClasses -Force | Out-Null

& javac -encoding UTF-8 -cp "$ServerJar;$JarPath" -d $testClasses $testSource
if ($LASTEXITCODE -ne 0) { throw "Cinema self-test compilation failed." }
& java -cp "$ServerJar;$JarPath;$testClasses" gg.raynormods.cinema.CinemaCoreSelfTest
if ($LASTEXITCODE -ne 0) { throw "Cinema self-test failed." }
