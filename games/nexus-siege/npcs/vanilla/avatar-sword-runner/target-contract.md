# Path Target Contract

## Current Vanilla Contract

This NPC does not hardcode movement nodes. It follows the named vanilla world
path `Nexus_Avatar_Sword_Runner_Path`, whose nodes are authored in-game.

## Meaning

Each node stores the exact position and facing at which it was added with
`/worldpath builder add`.

## Role Behavior

The role uses:

- `Sensor.Type`: `Path`
- `Sensor.PathType`: `WorldPath`
- `Sensor.Path`: `Nexus_Avatar_Sword_Runner_Path`
- `Sensor.Range`: `128`
- `BodyMotion.Type`: `Path`
- `BodyMotion.Shape`: `LINE`

The role follows the named world path. It does not
create a path itself.

## Vanilla Command

Use:

```text
/npc path <rotation1,distance1,rotation2,distance2,...>
```

If no `--entity` argument is provided, the command targets the NPC the player is
looking at. To target a specific NPC, use:

```text
/npc path <instructions> --entity=<npcEntityId>
```

## Absolute A to B Conversion

For two map points:

- Spawn or stand the NPC at A.
- Face the NPC toward B.
- Use `0,<distance>` as the first path segment.

For turns, append more pairs. Example: `0,12,90,5,-45,8`.
