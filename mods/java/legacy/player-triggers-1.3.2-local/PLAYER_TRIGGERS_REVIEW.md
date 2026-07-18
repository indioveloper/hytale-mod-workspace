# Player Triggers 1.3.2

Reviewed against Hytale Server 0.5.4.

## Included features

- Trigger Volume follows the triggering player.
- Trigger effect stops following the player.
- Optional stop-following behavior when the tracked player exits.
- Persistent string tags on players.
- Player tag condition and tag modification effect.
- Global NPC kill counter with HUD.
- Configurable filtered NPC kill listener.
- Programmable player timer with HUD and success tag.

## Timer success setup

The timer emits a Trigger Volume `TAG_ADDED` event when it finishes.

For effects that must only run when this timer succeeds:

1. Set the effect event to `TAG_ADDED`.
2. Add the vanilla `Tag` condition.
3. Set `Source` to `EVENT`.
4. Set `TagKey` to the timer's `SuccessTag`.
5. Optionally set `TagValue` to `success`.

Without the event tag condition, any tag added to the same volume may satisfy
the `TAG_ADDED` event.

## Known limits

- A Trigger Volume can follow only one player at a time. The latest activation
  becomes its owner.
- Each player has one visible timer slot. Starting another timer replaces the
  previous timer when `RestartIfRunning` is enabled.
- Each player has one configurable filtered kill-listener slot, plus the
  built-in total `mobs_killed` counter.
- Active timers are runtime state and do not survive a server restart.
- `StopFollowingOnExit` is exposed on the follow effect because the vanilla
  Behavior panel has no public extension API in Hytale 0.5.4.

## Verification

- Compiled with JDK 25 using `-Xlint:all -Werror`.
- Linked against the Hytale 0.5.4 server API.
- JAR structure validated with `jar --validate`.
- Isolated server startup confirmed the plugin registers and enables.
