# Cutterray T3 prototype

First in-game visual prototype for the industrial tier 3 cutterray. The design
uses an original low-poly silhouette, a graphite/copper shell, cyan energy
elements and a dedicated `Origin_Projectile` bone at the emitter.

## Current scope

- Playable presentation prototype: custom model/icon, vanilla drill animation,
  short-range block breaking and a temporary beam to the targeted block.
- The beam is implemented by two custom interactions registered by the Java
  plugin plus a short server-side expiry refreshed by every extraction pulse.
  This keeps the beam continuous while use is held without leaving it attached
  after release. Heat/energy and final tier gating remain outside this prototype.
- Includes `Resource_Node_Copper_Prototype` and
  `Resource_Node_Iron_Prototype`, large single-anchor blocks used to test beam
  targeting, material gating and cutter-only gathering. Iron requires a T2 or
  better cutterray; the current T3 prototype can extract both. Sustained
  contact grants material directly at seconds 1, 2, 3 and 4; at second 5 the
  node breaks and grants a final randomized quantity. Native item
  pickup notifications keep a single accumulating pop-up visible. Losing
  contact for roughly 0.65 seconds resets progress.
  It is intentionally a stand-in for the documented block-anchor plus visual
  entity architecture.
- Prototype item ID: `Tool_Cutterray_T3_Prototype`.
- Keeps the vanilla `Drill` pose only as a temporary holding animation, but
  removes its drill audio and tip dust. Holding use against any block produces
  the same brighter 30% wider cyan beam, continuous energy loop and cyan contact
  light. Resource nodes add moving flow pulses plus separate rock-melt and final
  break SFX.
- Targets `>=0.7.0 <0.8.0` and intentionally avoids Update 8-only model fields.

Regenerate the cutterray and resource-node assets:

```powershell
.\tools\Generate-CutterrayAssets.ps1
.\tools\Generate-CopperNodeAssets.ps1
.\tools\Generate-IronNodeAssets.ps1
```

Build the test mod from the workspace root against the runtime being tested:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\games\desert-survival\prototypes\cutterray-t3 `
  -SourceRoot src `
  -PackageRoot . `
  -ArtifactName Raynor_Desert_Survival_Cutterray_T3_Prototype-0_1_8.jar
```

The next in-game pass must verify that the PNG icon is used, that holding use
repeats the drill animation, that the beam starts near the emitter and that it
stays visually continuous until shortly after release. Give both nodes with
`/give Resource_Node_Copper_Prototype 1` and
`/give Resource_Node_Iron_Prototype 1`, place them on flat ground and verify
that the T3 cutterray deposits the corresponding vanilla ore directly into the
inventory.

## Deferred analysis mode

A future cutterray analysis mode should scan a resource node, reveal vulnerable
zones and reward a precise cut with substantially faster extraction. This is a
documented design direction only; version 0.1.5 does not implement scanning,
weak-point targeting or precision-cut bonuses. A later visual pass must also
make each resource node progressively look depleted as minerals are extracted.
Version 0.1.8 presents extraction damage as short-lived native nameplates above
the node instead of relying on combat text for a bodyless helper entity.

## Provisional tier balance

Every tier completes a valid node extraction in five seconds. Higher tiers show
larger damage numbers and extract more material from the same compatible node:

| Tier | Damage each second | Yield at seconds 1-4 | Final yield | Total |
| --- | ---: | ---: | ---: | ---: |
| 1 | 10 | 1 | 1-2 | 5-6 |
| 2 | 20 | 1 | 2-3 | 6-7 |
| 3 | 30 | 2 | 2-4 | 10-12 |
| 4 | 40 | 2 | 4-6 | 12-14 |
| 5 | 50 | 3 | 5-7 | 17-19 |
| 6 | 60 | 4 | 6-8 | 22-24 |

Only tier 3 has an item asset in this prototype; the shared profile table is
already implemented so later cutterray items can reuse the same timing loop.
