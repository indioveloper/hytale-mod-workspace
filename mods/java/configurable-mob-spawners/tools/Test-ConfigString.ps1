$ErrorActionPreference = "Stop"
$project = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$serverJar = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Server\HytaleServer.jar"
if (-not (Test-Path -LiteralPath $serverJar -PathType Leaf)) { throw "HytaleServer.jar not found: $serverJar" }
$output = Join-Path $project ".build\test-config-string"
New-Item -ItemType Directory -Force -Path $output | Out-Null
$sources = @(
  (Join-Path $project "src\gg\orbgenesis\configurablespawners\AggressionMode.java")
  (Join-Path $project "src\gg\orbgenesis\configurablespawners\LootMode.java")
  (Join-Path $project "src\gg\orbgenesis\configurablespawners\SpawnerLootEntry.java")
  (Join-Path $project "src\gg\orbgenesis\configurablespawners\SpawnerMobProfile.java")
  (Join-Path $project "src\gg\orbgenesis\configurablespawners\ConfigurableSpawnerComponent.java")
  (Join-Path $project "src\gg\orbgenesis\configurablespawners\SpawnerConfigString.java")
  (Join-Path $project "tools\tests\SpawnerConfigStringTest.java")
)
& javac -cp $serverJar -d $output @sources
if ($LASTEXITCODE -ne 0) { throw "CMS1 test compilation failed." }
& java -cp "$output;$serverJar" gg.orbgenesis.configurablespawners.SpawnerConfigStringTest
if ($LASTEXITCODE -ne 0) { throw "CMS1 test failed." }
