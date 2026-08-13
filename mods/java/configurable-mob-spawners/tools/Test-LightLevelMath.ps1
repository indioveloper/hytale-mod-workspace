$ErrorActionPreference = "Stop"
$project = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$output = Join-Path $project ".build\test-light-level"
New-Item -ItemType Directory -Path $output -Force | Out-Null

$calculator = Join-Path $project "src\gg\orbgenesis\configurablespawners\SpawnerLightLevel.java"
$test = Join-Path $PSScriptRoot "tests\SpawnerLightLevelTest.java"

& javac -d $output $calculator $test
if ($LASTEXITCODE -ne 0) { throw "Could not compile light-level checks." }

& java -cp $output gg.orbgenesis.configurablespawners.SpawnerLightLevelTest
if ($LASTEXITCODE -ne 0) { throw "Light-level checks failed." }
