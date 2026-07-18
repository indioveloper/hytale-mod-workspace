# Implementation Notes

## Local Reference Findings

The local Hytale shared source indicates that vanilla NPC roles are loaded from:

`Server/NPC/Roles`

Useful vanilla builder fields found in `BuilderRole`:

- `Appearance`
- `HotbarItems`
- `MotionControllerList`
- `InitialMotionController`
- `Instructions`
- `SpawnLockTime`

Useful vanilla components found:

- `Walk` motion controller via `MotionControllerWalk.TYPE`.
- `Seek` body motion for moving toward a target.
- `ReadPosition` sensor for reading a stored position and providing it to body motion.
- `Path` sensor for exposing the current path as an `IPathProvider`.
- `Path` body motion for following the transient path.
- `PlayAnimation` action for playing an animation by slot and ID.
- `Attack` action for primary/secondary attack behavior.

## Current Assumption

The desired A-to-B behavior can be done vanilla with a transient path assigned
after spawn. The role follows the current path in the NPC `PathManager`; it does
not create or hardcode movement nodes.

The local source shows `/npc path` accepts comma-separated
`rotation,distance` pairs. If no `--entity` argument is provided, the command
targets the NPC the player is looking at.

## Asset IDs To Resolve

- Humanoid model fallback: `Player`.
- Random humanoid model: use `/npc spawn ... --randomModel`; `randomModel` is a command flag, not a role `Appearance` asset.
- Sword item: `Weapon_Sword_Iron`.
- Swing animation: `SwingRight`.
- Animation slot: `Action` is valid according to `NPCAnimationSlot`.
