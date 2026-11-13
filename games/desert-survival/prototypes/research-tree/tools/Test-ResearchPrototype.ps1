param(
  [string]$ServerJar = "C:\tmp\HytaleServer-0.7.7-survival-vitals.jar"
)

$ErrorActionPreference = "Stop"
$project = Split-Path -Parent $PSScriptRoot
$testClasses = Join-Path $project ".build\test-classes"
$testSource = Join-Path $project "testsrc\gg\raynormods\desertsurvival\research\ResearchCatalogTest.java"
$sourceDirectory = Join-Path $project "src\gg\raynormods\desertsurvival\research"
$testedSources = @(
  (Join-Path $sourceDirectory "ResearchSector.java"),
  (Join-Path $sourceDirectory "ResearchNode.java"),
  (Join-Path $sourceDirectory "ResearchCatalog.java"),
  (Join-Path $sourceDirectory "ResearchProgressComponent.java"),
  (Join-Path $sourceDirectory "ResearchService.java"),
  $testSource
)

if (-not (Test-Path -LiteralPath $ServerJar -PathType Leaf)) {
  throw "HytaleServer.jar not found: $ServerJar"
}

New-Item -ItemType Directory -Force -Path $testClasses | Out-Null
$separator = [System.IO.Path]::PathSeparator

& javac -encoding UTF-8 -cp $ServerJar -d $testClasses $testedSources
if ($LASTEXITCODE -ne 0) {
  throw "Test compilation failed with exit code $LASTEXITCODE"
}

$testClassPath = @($testClasses, $ServerJar) -join $separator
& java -ea -cp $testClassPath gg.raynormods.desertsurvival.research.ResearchCatalogTest
if ($LASTEXITCODE -ne 0) {
  throw "Research prototype tests failed with exit code $LASTEXITCODE"
}
