# Resource node architecture

## Decision for the MVP

Use a hybrid node: one technical anchor block plus one visual model entity.

The anchor block owns placement, persistence, world-generation occupancy,
respawn timing and a stable position in the chunk. The entity owns the large,
irregular node silhouette and can swap between `intact`, `damaged` and
`depleted` models without turning the deposit into dozens of normal blocks.

The cutterray will raycast from its `Origin_Projectile` bone. A server-side
node component validates range, cutter tier and line of sight, accumulates
extraction progress, grants resources in pulses and changes the visual state.
The beam itself is presentation; it must never be trusted as the authority for
rewards.

Baseline extraction duration is five seconds for every cutterray tier. Tier is
an efficiency axis rather than a speed axis: higher tiers display larger damage
numbers and grant more material per pulse and per depleted node. Compatible
material gating remains independent from this yield curve.

## Why not only blocks

A block cluster is excellent for native mining and world editing, but a large
organic deposit needs many blocks, exposes seams, is awkward to damage in
stages and complicates deterministic regeneration. It also encourages players
to mine it with ordinary tools unless every block is specially guarded.

## Why not only an entity

An entity gives the best silhouette and hit target, but it is a weaker source
of truth for world-generation placement, chunk persistence and respawn. A
technical block makes those responsibilities explicit and lets the visual
entity be rebuilt if it is ever missing.

## Evolution path

The MVP uses one entity and three whole-model states to keep networking and
asset work small. Later, a node can expose independent lobes as child entities
or attachments when per-lobe cutting materially improves play. Update 8 adds
conditional model attachments, but the first implementation must not depend on
that pre-release feature while the target runtime remains stable 0.7.x.

Suggested state owned by the anchor:

- resource type and required cutter tier;
- remaining yield and extraction progress;
- intact, damaged, depleted or regenerating state;
- respawn timestamp and randomized next yield;
- UUID of the current visual entity;
- optional biome/quality modifiers.

The entity should use `ModelComponent` with a deliberately authored hitbox.
Update 7 makes this safer for later size variants because entity hitboxes now
follow the entity scale component, but the MVP should still prefer fixed
model sizes and explicit hitbox dimensions.

## Deferred cutterray analysis mode

After the baseline extraction loop is stable, the cutterray should gain an
analysis mode inspired by precision resource cutting: scan the node, expose one
or more vulnerable zones, then reward keeping the cutting beam on a vulnerable
zone with substantially faster extraction. The authoritative node state should
select and validate those zones; the client-side highlight is presentation
only. This is deliberately outside the current prototype and must not be
implemented until the five-second sustained-contact loop is validated.
