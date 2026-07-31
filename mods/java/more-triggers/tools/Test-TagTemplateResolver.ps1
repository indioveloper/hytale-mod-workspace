param(
  [string]$ServerJar = "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Server\HytaleServer.jar"
)

$ErrorActionPreference = "Stop"

$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$source = Join-Path $project "src\gg\orbgenesis\moretriggers\TagTemplateResolver.java"
$tagSource = Join-Path $project "src\gg\orbgenesis\moretriggers\TagSource.java"
$test = Join-Path $PSScriptRoot "TestTagTemplateResolver.java"
$classes = Join-Path $project ".build\template-tests"

if (-not (Test-Path -LiteralPath $ServerJar -PathType Leaf)) {
  throw "HytaleServer.jar not found: $ServerJar"
}

if (Test-Path -LiteralPath $classes) {
  Remove-Item -LiteralPath $classes -Recurse -Force
}
New-Item -ItemType Directory -Path $classes -Force | Out-Null

& javac -encoding UTF-8 -cp $ServerJar -d $classes $tagSource $source $test
if ($LASTEXITCODE -ne 0) {
  throw "Resolver test compilation failed with exit code $LASTEXITCODE"
}

& java -cp "$classes;$ServerJar" gg.orbgenesis.moretriggers.TestTagTemplateResolver
if ($LASTEXITCODE -ne 0) {
  throw "Resolver tests failed with exit code $LASTEXITCODE"
}
