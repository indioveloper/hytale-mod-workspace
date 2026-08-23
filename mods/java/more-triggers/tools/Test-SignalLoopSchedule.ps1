$ErrorActionPreference = "Stop"

$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$classes = Join-Path $project ".build\classes"
if (-not (Test-Path -LiteralPath (Join-Path $classes "gg\orbgenesis\moretriggers\signalloop\SignalLoopSchedule.class"))) {
  throw "Build the mod before running SignalLoopScheduleTest."
}
$testSource = Join-Path $PSScriptRoot "SignalLoopScheduleTest.java"
$testClasses = Join-Path $project ".build\signal-loop-test-classes"
if (Test-Path -LiteralPath $testClasses) {
  Remove-Item -LiteralPath $testClasses -Recurse -Force
}
New-Item -ItemType Directory -Path $testClasses -Force | Out-Null

& javac -encoding UTF-8 -cp $classes -d $testClasses $testSource
if ($LASTEXITCODE -ne 0) {
  throw "SignalLoopScheduleTest compilation failed."
}
& java -ea -cp "$classes;$testClasses" gg.orbgenesis.moretriggers.signalloop.SignalLoopScheduleTest
if ($LASTEXITCODE -ne 0) {
  throw "SignalLoopScheduleTest failed."
}
