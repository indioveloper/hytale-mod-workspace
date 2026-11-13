param(
  [Parameter(Mandatory = $true)][string]$ServerJar,
  [Parameter(Mandatory = $true)][string]$JarPath
)
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$classes = Join-Path $project '.build/selftest'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
& javac -cp "$JarPath;$ServerJar" -d $classes (Join-Path $project 'tests/java/gg/raynormods/holograms/RaynorHologramsSelfTest.java')
if ($LASTEXITCODE -ne 0) { throw 'Holograms self-test compilation failed' }
& java -cp "$classes;$JarPath;$ServerJar" gg.raynormods.holograms.RaynorHologramsSelfTest
if ($LASTEXITCODE -ne 0) { throw 'Holograms self-test failed' }
