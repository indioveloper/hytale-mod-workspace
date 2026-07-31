$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$managerPath = Join-Path $projectRoot "src\gg\orbgenesis\scoreboards\ScoreboardManager.java"
$source = Get-Content -LiteralPath $managerPath -Raw

if ($source -notmatch "HytaleServer\.SCHEDULED_EXECUTOR") {
  throw "Objective asset mutations are not assigned to HytaleServer.SCHEDULED_EXECUTOR."
}

if ($source -notmatch "\.thenRunAsync\s*\(") {
  throw "Objective asset mutations are not chained asynchronously."
}

$upsert = [regex]::Match(
  $source,
  "(?s)public synchronized CompletableFuture<Boolean> upsertDefinition.*?(?=\r?\n  public synchronized boolean deleteDefinition)"
).Value
if (-not $upsert) {
  throw "Could not locate upsertDefinition for the thread-safety check."
}
if ($upsert -match "getAssetStore\(\)\.(loadAssets|removeAssets)") {
  throw "upsertDefinition mutates the asset store directly on its caller thread."
}

$delete = [regex]::Match(
  $source,
  "(?s)public synchronized boolean deleteDefinition.*?(?=\r?\n  public List<Objective> start)"
).Value
if (-not $delete) {
  throw "Could not locate deleteDefinition for the thread-safety check."
}
if ($delete -match "getAssetStore\(\)\.(loadAssets|removeAssets)") {
  throw "deleteDefinition mutates the asset store directly on its caller thread."
}

Write-Output "Scoreboards asset-threading check passed."
