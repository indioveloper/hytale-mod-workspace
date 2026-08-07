$ErrorActionPreference = "Stop"

$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$source = Join-Path $project "src\gg\orbgenesis\moretriggers\RandomItemCandidateFilter.java"
$test = Join-Path $PSScriptRoot "TestRandomItemCandidateFilter.java"
$classes = Join-Path $project ".build\random-item-tests"

if (Test-Path -LiteralPath $classes) {
  Remove-Item -LiteralPath $classes -Recurse -Force
}
New-Item -ItemType Directory -Path $classes -Force | Out-Null

& javac -encoding UTF-8 -d $classes $source $test
if ($LASTEXITCODE -ne 0) {
  throw "Random item candidate filter test compilation failed with exit code $LASTEXITCODE"
}

& java -cp $classes gg.orbgenesis.moretriggers.TestRandomItemCandidateFilter
if ($LASTEXITCODE -ne 0) {
  throw "Random item candidate filter tests failed with exit code $LASTEXITCODE"
}
