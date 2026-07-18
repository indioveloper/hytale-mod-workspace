# Trigger Volumes Workspace Index

Last updated: 2026-06-26

## Purpose

This workspace is the clean development area for future Trigger Volume extension mods.
Historical artifacts remain in place at the repository root and are referenced here instead
of being moved.

## Clean Workspace Layout

- `docs/` - planning notes, handoffs, compatibility notes, and decisions.
- `src/` - source for the next Trigger Volume mod.
- `build/` - local compilation output.
- `dist/` - packaged jars and release artifacts.
- `research/` - API comparisons, patch-note summaries, and pre-release findings.

## Historical References

## Active Work

### Player Trigger Tags

- Source: `workspace/trigger-volumes-extensions/src/`
- Docs: `workspace/trigger-volumes-extensions/docs/player-trigger-tags-v1.md`
- Dist: `workspace/trigger-volumes-extensions/dist/Player_Trigger_Tags-1_0_0.jar`
- Version: `1.0.0`
- Server range: `>=0.6.0-pre <0.7.0`

Known features:

- Persistent player string tags.
- `ModifyPlayerTag` Trigger Volume effect.
- `PlayerTagCondition` Trigger Volume condition.
- Dispatch player tag changes as Trigger Volume `TAG_ADDED` / `TAG_REMOVED` events.
- Dispatch targets: current volume or tagged volumes within radius.

Verification:

- Compiled against installed pre-release `HytaleServer.jar`.
- `javac -Xlint:all -Werror` passed.
- `jar --validate` passed.
- Includes `Server/Languages/en-US/server.lang` and `Server/Languages/es-ES/server.lang`.

### Player Triggers

- Source: `player-triggers-src/`
- Stage: `player-triggers-stage/`
- Build output: `player-triggers-build/`
- Latest reviewed jar: `Player_Triggers-1_3_2.jar`
- Source archive: `Player_Triggers-1_3_2-source.zip`
- Review notes: `PLAYER_TRIGGERS_REVIEW.md`
- Session notes: `SESSION_HANDOFF.md`

Known features:

- Trigger Volume follows the triggering player with configurable XYZ offset.
- Stop following through a Trigger Volume effect.
- Optional stop-following behavior when the tracked player exits.
- Persistent player string tags.
- Player tag condition.
- Set, remove, increment, toggle, and append player tag operations.
- Global NPC kill counter in `mobs_killed`.
- Configurable filtered NPC kill listener.
- Kill counter HUD.
- Programmable timer HUD.
- Timer completion emits a Trigger Volume `TAG_ADDED` event.

Compatibility note:

- Reviewed against Hytale Server 0.5.4.
- Manifest currently targets `>=0.5.2 <0.6.0`.
- Needs review before reuse on `0.6.0-pre5`.

### More Triggers

- Source: `more-triggers-src/`
- Stage: `more-triggers-stage/`
- Build output: `more-triggers-build/`
- Jars: `More_Triggers-1_0_0.jar` through `More_Triggers-1_0_3.jar`

Known features:

- `RemoveEventTitle`
- `RandomTagSelection`
- `PasteRandomPrefab`

Compatibility note:

- Manifest uses dated server version `2026.05.07-5efa15f6d`.
- Needs semver/server compatibility review for `0.6.0-pre5`.

### Trigger Remove Event Title

- Source: `trigger-random-tag-src/`
- Stage: `trigger-random-tag-stage/`
- Build output: `trigger-random-tag-build/`
- Jar: `Trigger_Remove_Event_Title-1_1_0-random-tag.jar`

Known status:

- Historical/partial predecessor to `More Triggers`.
- Includes `RemoveEventTitle` and `RandomTagSelection`.

### Hytale Shared Source

- Local path: `hytale-shared-source/`
- Remote: `https://github.com/HypixelStudios/hytale-shared-source.git`
- Branch: `pre-release`
- Local commit: `14edb97b5721bf26140d692f2f144ebcb1fcd7e2` (`Sync 2026-06-22`)
- Remote tracking status: behind `origin/pre-release` by 1 commit.
- Remote update observed: `4b5139ce70f5b1db95373a91fb91811ca2528e45` (`Sync 2026-06-25`)

Important `0.6.0-pre5` Trigger Volume/API changes observed:

- Protocol build changed from `117` to `123`.
- New event: `SIGNAL_RECEIVED`.
- New Trigger Volume effect: `SendSignal`.
- New interaction: `SpawnTriggerVolume`.
- New events in schemas: `BlockUsed`, `EntityDied`.
- New effects in schemas include `SpawnNpc` and `PlayAnimation`.
- New builtin `Points` plugin and point tool packets.

## Working Rules

- Keep historical artifacts intact unless explicitly approved.
- For medium or large tasks, present a brief plan before implementation.
- Do not implement medium or large work until the plan is confirmed.
- Inspection, diagnosis, and non-mutating research may proceed without prior approval.
- Before editing files, state which files will change and why.
- Keep `docs/session-handoff.md` updated after meaningful work.
