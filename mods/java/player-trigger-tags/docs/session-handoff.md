# Player Trigger Tags Handoff

Last updated: 2026-07-18

## Current State

`Player Trigger Tags` is the canonical mod for persistent player tags and tag
conditions/effects. It is now under `mods/java/player-trigger-tags`.

## Decisions

- Historical `Player Triggers` snapshots were removed as separate projects.
- Entity movement and platform collision effects were extracted to
  `mods/java/entity-motion-triggers`.
- `Player Trigger Tags` keeps `ConvertBlocksToEntities`, because it creates
  entities from blocks but does not move entities.

## Hytale Reference

- Use `hytale-shared-source` `origin/pre-release` as the source reference for `0.6.0-pre5`.
- Local `hytale-shared-source` is on `pre-release` at `14edb97b5721bf26140d692f2f144ebcb1fcd7e2`.
- Local branch is behind `origin/pre-release` by 1 commit.
- The observed remote update is `4b5139ce70f5b1db95373a91fb91811ca2528e45`.

## Current Mod: Player Trigger Tags 1.5.6

- Source: `mods/java/player-trigger-tags/src/`.
- Documentation: `mods/java/player-trigger-tags/docs/player-trigger-tags-v1.md`.
- Build command: see root `README.md`.

Included behavior:

- Persistent string tags stored on player entities.
- Trigger effect `ModifyPlayerTag`.
- Trigger condition `PlayerTagCondition`.
- Tag change dispatch modes: `NONE`, `CURRENT_VOLUME`, `TAGGED_VOLUMES`.
- Non-remove changes dispatch `TAG_ADDED` with event `TagKey` / `TagValue`.
- Remove changes dispatch `TAG_REMOVED` with event `TagKey` / previous `TagValue`.
- `ConvertBlocksToEntities` with item asset selector and optional collision.
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
5. Keep movement/platform work in `mods/java/entity-motion-triggers`.
