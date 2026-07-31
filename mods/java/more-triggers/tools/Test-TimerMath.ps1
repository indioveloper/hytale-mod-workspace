$ErrorActionPreference = "Stop"

$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$classes = Join-Path $project ".build\classes"
if (-not (Test-Path -LiteralPath (Join-Path $classes "gg\orbgenesis\moretriggers\timer\TimerManager.class"))) {
  throw "Build the mod before running TimerMathTest."
}
$testSource = Join-Path $PSScriptRoot "TimerMathTest.java"
$testClasses = Join-Path $project ".build\test-classes"
if (Test-Path -LiteralPath $testClasses) {
  Remove-Item -LiteralPath $testClasses -Recurse -Force
}
New-Item -ItemType Directory -Path $testClasses -Force | Out-Null

& javac -encoding UTF-8 -cp $classes -d $testClasses $testSource
if ($LASTEXITCODE -ne 0) {
  throw "TimerMathTest compilation failed."
}
& java -ea -cp "$classes;$testClasses" gg.orbgenesis.moretriggers.timer.TimerMathTest
if ($LASTEXITCODE -ne 0) {
  throw "TimerMathTest failed."
}
