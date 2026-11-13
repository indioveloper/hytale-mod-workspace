param(
  [string]$ServerJar = (Join-Path $env:APPDATA 'Hytale\install\release\package\game\latest\Server\HytaleServer.jar')
)
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$out = Join-Path $project '.build\tests\core'
New-Item -ItemType Directory -Force -Path $out | Out-Null
$sources = @(
  (Join-Path $project 'src\gg\orbgenesis\buildbattle\BuildBattleConfig.java'),
  (Join-Path $project 'src\gg\orbgenesis\buildbattle\BuildBattleMode.java'),
  (Join-Path $project 'src\gg\orbgenesis\buildbattle\BuildBattlePlot.java'),
  (Join-Path $project 'src\gg\orbgenesis\buildbattle\BuildBattleScoring.java'),
  (Join-Path $project 'src\gg\orbgenesis\buildbattle\BuildBattleTeamPlanner.java'),
  (Join-Path $project 'src\gg\orbgenesis\buildbattle\BuildBattleThemeCatalog.java'),
  (Join-Path $PSScriptRoot 'tests\BuildBattleCoreTest.java')
)
& javac -cp $ServerJar -d $out @sources
if ($LASTEXITCODE -ne 0) { throw 'BuildBattleCoreTest compilation failed.' }
& java -ea -cp "$out;$ServerJar" gg.orbgenesis.buildbattle.BuildBattleCoreTest
if ($LASTEXITCODE -ne 0) { throw 'BuildBattleCoreTest failed.' }
