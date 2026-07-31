# Player Trigger Tags v1

> Documento historico. El proyecto esta deprecated y
> `ConvertBlocksToEntities` vive ahora en Entity Motion Triggers.

## Goal

`Player Trigger Tags` gives players persistent string tags and lets Trigger Volumes react when
those player tags change.

## Types

### Effect: `ModifyPlayerTag`

Fields:

- `Event` - inherited Trigger Volume event.
- `Operation` - `SET`, `REMOVE`, `INCREMENT`, `TOGGLE`, or `APPEND`.
- `TagKey` - player tag key. Keys are trimmed and lowercased.
- `TagValue` - string value used by the operation.
- `DispatchMode` - `NONE`, `CURRENT_VOLUME`, or `TAGGED_VOLUMES`.
- `MatchKey` / `MatchValue` - target Trigger Volume tag filter for `TAGGED_VOLUMES`.
- `Radius` - target search radius for `TAGGED_VOLUMES`.
- `Center` - target search center, usually `ENTITY` for the triggering player.

When a non-remove operation changes the player's tag value, the mod dispatches `TAG_ADDED`.
When `REMOVE` removes an existing tag, the mod dispatches `TAG_REMOVED`.
The event carries `TagKey` and `TagValue`, so vanilla `TagCondition` with `Source: EVENT`
can filter the event.

### Condition: `PlayerTagCondition`

Fields:

- `Event` - inherited Trigger Volume event.
- `TagKey`
- `Comparison` - `EQUALS`, `NOT_EQUALS`, `EXISTS`, `MISSING`, `GREATER_THAN`,
  `GREATER_OR_EQUAL`, `LESS_THAN`, or `LESS_OR_EQUAL`.
- `TagValue`
- `CaseSensitive`

## Minigame Example

Obstacle Trigger Volume:

- Effect: `ModifyPlayerTag`
- Event: `Enter` or the relevant activation event.
- `Operation`: `SET`
- `TagKey`: `salto`
- `TagValue`: `true`
- `DispatchMode`: `TAGGED_VOLUMES`
- `MatchKey`: `listener`
- `MatchValue`: `jump_trap`
- `Radius`: `100`
- `Center`: `ENTITY`

Receiver Trigger Volume:

- Tag: `listener=jump_trap`
- Effect: `SetVelocity`
- Event: `TagAdded`
- Condition 1: vanilla `TagCondition`, `Source: EVENT`, `TagKey: salto`, `TagValue: true`
- Condition 2: `PlayerTagCondition`, `TagKey: minigame_role`, `Comparison: EQUALS`, `TagValue: runner`

This applies the effect to the player whose tag changed, while allowing the receiving volume
to live elsewhere and listen by its own Trigger Volume tags.
