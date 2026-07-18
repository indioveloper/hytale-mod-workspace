# Trigger Volumes Extensions Handoff

Last updated: 2026-06-26

## Current State

The clean workspace has been created at:

`workspace/trigger-volumes-extensions/`

Historical mod artifacts remain at the repository root and have not been moved,
rebuilt, renamed, or deleted.

`Player Trigger Tags 1.0.0` has been implemented and packaged inside the clean
workspace.

## Decisions

- Use a clean development area for the new Trigger Volume mod.
- Keep historical work as read-only reference until a specific migration or cleanup plan is approved.
- Require a brief plan and user confirmation before medium or large implementation work.
- Small inspection, diagnosis, and research tasks can proceed without mutating files.

## Hytale Reference

- Use `hytale-shared-source` `origin/pre-release` as the source reference for `0.6.0-pre5`.
- Local `hytale-shared-source` is on `pre-release` at `14edb97b5721bf26140d692f2f144ebcb1fcd7e2`.
- Local branch is behind `origin/pre-release` by 1 commit.
- The observed remote update is `4b5139ce70f5b1db95373a91fb91811ca2528e45`.

## Existing Trigger Volume Mods

- `Player Triggers 1.3.2`: follow player, player tags, kill counter, timer HUD.
- `More Triggers 1.0.3`: remove event title, random tag selection, paste random prefab.
- `Trigger Remove Event Title 1.1.0`: historical predecessor for remove title/random tag behavior.

## New Mod: Player Trigger Tags 1.0.0

- Source: `workspace/trigger-volumes-extensions/src/`
- Documentation: `workspace/trigger-volumes-extensions/docs/player-trigger-tags-v1.md`
- Jar: `workspace/trigger-volumes-extensions/dist/Player_Trigger_Tags-1_0_0.jar`
- SHA-256: `54D643ED274BFBD3B90098DDFEF8643F4C168CC018384A5482434250DDE521CA`

Included behavior:

- Persistent string tags stored on player entities.
- Trigger effect `ModifyPlayerTag`.
- Trigger condition `PlayerTagCondition`.
- Tag change dispatch modes: `NONE`, `CURRENT_VOLUME`, `TAGGED_VOLUMES`.
- Non-remove changes dispatch `TAG_ADDED` with event `TagKey` / `TagValue`.
- Remove changes dispatch `TAG_REMOVED` with event `TagKey` / previous `TagValue`.
- Includes `en-US` and `es-ES` Trigger Volume editor localizations for field labels,
  tooltips, placeholders, and enum dropdown options.

## Compatibility Watchlist For 0.6.0-pre5

- `SIGNAL_RECEIVED` and `SendSignal` may replace or simplify timer/tag event patterns.
- Trigger Volume schemas now include new events such as `BlockUsed` and `EntityDied`.
- Trigger Volume schemas now include effects such as `SpawnNpc` and `PlayAnimation`.
- Protocol build changed to `123`, so protocol-facing code and UI tools need review.
- Old manifests using dated server versions should be updated to semver ranges if reused.

## Suggested Next Steps

1. Before packaging, run `tools/Test-PluginLocalization.ps1`. It rejects language keys that include the automatic `server.` prefix and Trigger Volume fields without a tooltip.
2. Before installing a new JAR, run `tools/Test-PluginDeployment.ps1` against the target save. It rejects a second `OrbGenesis:Player Trigger Tags` package in either the global pre-release Mods folder or the save's mods folder.
2. Smoke test server startup and confirm the plugin registers.
3. Create the minigame test case: obstacle uses `ModifyPlayerTag`, receiver listens via `TAG_ADDED`.
4. If the test works, decide whether to add `VolumesContainingPlayer` dispatch as a v1.1 feature.
5. Separately review old `Player Triggers` features for possible migration.
