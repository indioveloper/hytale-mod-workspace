$ErrorActionPreference = "Stop"

$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$source = Join-Path $project "src\gg\orbgenesis\moretriggers\NoMoveExceptionFilter.java"
$test = Join-Path $PSScriptRoot "TestNoMoveExceptionFilter.java"
$classes = Join-Path $project ".build\no-move-tests"

if (Test-Path -LiteralPath $classes) {
  Remove-Item -LiteralPath $classes -Recurse -Force
}
New-Item -ItemType Directory -Path $classes -Force | Out-Null

& javac -encoding UTF-8 -d $classes $source $test
if ($LASTEXITCODE -ne 0) {
  throw "No Move exception filter test compilation failed with exit code $LASTEXITCODE"
}

& java -cp $classes gg.orbgenesis.moretriggers.TestNoMoveExceptionFilter
if ($LASTEXITCODE -ne 0) {
  throw "No Move exception filter tests failed with exit code $LASTEXITCODE"
}
