$ErrorActionPreference = "Stop"

$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$classes = Join-Path $project ".build\classes"
if (-not (Test-Path -LiteralPath (Join-Path $classes "gg\orbgenesis\moretriggers\RoomOccupancyGeometry.class"))) {
  throw "Build the mod before running RoomOccupancyGeometryTest."
}
$testSource = Join-Path $PSScriptRoot "RoomOccupancyGeometryTest.java"
$testClasses = Join-Path $project ".build\room-occupancy-test-classes"
if (Test-Path -LiteralPath $testClasses) {
  Remove-Item -LiteralPath $testClasses -Recurse -Force
}
New-Item -ItemType Directory -Path $testClasses -Force | Out-Null

& javac -encoding UTF-8 -cp $classes -d $testClasses $testSource
if ($LASTEXITCODE -ne 0) {
  throw "RoomOccupancyGeometryTest compilation failed."
}
& java -ea -cp "$classes;$testClasses" gg.orbgenesis.moretriggers.RoomOccupancyGeometryTest
if ($LASTEXITCODE -ne 0) {
  throw "RoomOccupancyGeometryTest failed."
}
