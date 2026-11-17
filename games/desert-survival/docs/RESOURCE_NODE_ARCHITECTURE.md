# Resource node architecture

Status (2026-11-17): **the working baseline is one modeled block per node**.
The playable 0.1.9 prototype and WorldGen 0.4.1 already use this approach;
WorldGen places it with a `Column` prop. Keep the server authoritative for
contact, rewards and depletion. Model swaps or block states can show damage
without adding an entity per deposit. Five seconds is a prototype timing rule;
the exact T0–T6 yield and energy matrices are still missing.

## Baseline and escalation criterion

Use the modeled block as both anchor and visible node for the current MVP.
Only add a separate visual entity if a validated design needs independent
animation, movable lobes or hit regions that a modeled block cannot provide.
That change requires chunk reload and networking tests before replacing the
existing node assets.

The following hybrid architecture is a **contingency**, not the current MVP:
one technical anchor block plus one visual model entity.

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

The current modeled block can gain `intact`, `damaged` and `depleted` variants.
If per-lobe cutting later improves play enough to justify more network state,
an entity or attachment may be added. This remains outside the stable 0.7.x
prototype.

Suggested authoritative state owned by the block or its persistent companion:

- resource type and required cutter tier;
- remaining yield and extraction progress;
- intact, damaged, depleted or regenerating state;
- respawn timestamp and randomized next yield;
- visual state or, only if the contingency is adopted, UUID of the visual entity;
- optional biome/quality modifiers.

If an entity is introduced later, it needs a deliberately authored hitbox,
explicit size tests and a recovery path when the entity is missing on reload.

## Deferred cutterray analysis mode

After the baseline extraction loop is stable, the cutterray should gain an
analysis mode inspired by precision resource cutting: scan the node, expose one
or more vulnerable zones, then reward keeping the cutting beam on a vulnerable
zone with substantially faster extraction. The authoritative node state should
select and validate those zones; the client-side highlight is presentation
only. This is deliberately outside the current prototype and must not be
implemented until the five-second sustained-contact loop is validated.
