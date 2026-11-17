# Desert Survival Research Prototype

Isolated prototype for the MVP technology tree and its Intel economy. It is not
part of the public Raynor Mods release matrix.

Design reconciliation (2026-11-17): the [master plan](../../docs/DESERT_SURVIVAL_MASTER_PLAN_2026-11-17.md)
proposes seven Codex tabs (global, five branches, Uniques) and normal tiers
0–6. This 0.2.8 build still has one fixed five-sector wheel with 48 nodes in
tiers 0–2. Unique blueprint discovery and per-recipe permits do not exist in
this plugin. Some recipes are taught by Research, but their game visibility
requires the smoke test recorded in [STATUS](../../STATUS.md). Later biome
missions may gate access to regions; they do not currently gate research.
Display names in local sources now map `CN-0.2` to T0, `CN-1.2` to T1 and
`CN-2.2` to T2. A local JAR compiles and passes the package check, but has not
been installed or smoke tested. The existing
`Tool_Cutterray_T3_Prototype` output linked to `CN-2.2` is an experimental
copper/iron test item, not a working T2 or canonical T3. Node IDs, Intel costs
and prerequisites have not changed. The planned outputs now use T0/T1/T2 item
IDs; Research will teach their recipes on join to previously researched
characters once those assets exist.

## Player loop

1. Enemy bases and dungeons place `Intel_Pickup_Prototype` as an end reward.
2. Walking over the world item runs its `Pickup` interaction. The engine removes
   that entity. Copies inserted directly into an inventory (for example from
   creative) are removed and converted by `IntelInventoryConversionSystem`.
3. Its value is added to the persistent `Raynor_DesertResearchProgress`
   component on that character.
4. `/research` (or `/research open`) opens the tree. A node is researched explicitly even when
   its cost is zero.
5. Paid nodes subtract one available Intel. Lifetime Intel is retained as a
   statistic; spent Intel is not refunded.

The component stores the available/lifetime Intel counters, unlocked node IDs
and the highest material tier reached. Tier 0/1/2 correspond to Scrap, Copper
and Iron. Material access does not block research: it will gate crafting and
use of recipes that require resources the player has not reached. Higher-tier
nodes are gated only by their explicit lower-tier prerequisites and Intel.
For this isolated prototype `/research tier <0|1|2>` simulates material access;
the final game must set this value from its progression system.

## Interface

The MVP shows all five sectors and three tier bands on one wheel. Its 48 nodes
are 32x32 icon buttons with one generated placeholder PNG: blue means ready to
research, gray means blocked, and green means researched. A generated overlay
draws the catalog's 37 prerequisite links behind them. The right panel
shows the selected node's full name, Intel cost and research action. The
header shows spendable Intel and the researched count; lifetime Intel and
material access remain stored for future systems but are hidden. Research
remains independent of material access; that value gates future crafting.

Node states are resolved server-side in this order: already researched,
prerequisites, Intel balance, available. Costs and dependency links are
MVP placeholders deliberately kept in `ResearchCatalog.java` for review.

The 0.7.7 Custom UI protocol exposes activation, hover and input value events,
but no pointer-down/move coordinates for drag-to-pan. Vanilla's `TopScrolling`
supports a vertical scrolling panel, not a freely draggable two-axis canvas.
The wheel is therefore fixed for this version. If later tiers exceed its
space, use explicit mouse-click navigation between wheel regions or a larger
scrollable layout until the planned Noesis UI becomes available to server mods.

Research 0.2.8 teaches item recipes when a node is researched and again on
join for existing characters. The first real recipe is the scrap knife from
`CO-0.2`: 3 scrap at Fieldcraft/Tools, with `KnowledgeRequired: true`.
`AG-0.1` also teaches the improvised extractor (20 scrap) and the empty small
blood bag (2 plastic, 3 plant fibre) at Fieldcraft/Tools. The full bag is a
creative test item until corpse extraction is implemented.
The improvised extractor now inherits a native crude hammer item contract and
uses a distinct shears icon/model placeholder so it appears as a tool recipe.
Most target items still do not exist, so their listed unlocks remain future
contracts. Custom machine processes must check the stable node ID on the
server; the native crafting check only covers native recipes.
The [research-to-item map](../../docs/RESEARCH_UNLOCKS_AND_ITEMS.md) lists all
48 unlocks and the proposed item implementation. Visible text uses names only;
the internal IDs remain stable for existing saves. Version 0.2.3 also shows
missing prerequisite names instead of internal IDs in validation feedback.

## Commands

- `/research`: open the tree directly.
- `/research open`: explicit alias for opening the tree.
- `/research status`: show spendable Intel and completion.
- `/research grant <amount>`: prototype-only Intel helper.
- `/research tier <0|1|2>`: prototype-only material-access bridge; it does not
  block research.
- `/research reset`: development helper that clears Intel, researched nodes and
  recipes learned from this tree.

To test the pickup, find it provisionally under `Items.Tools` in creative, or
give/spawn `Intel_Pickup_Prototype`, drop it and walk
over it. A stack is converted at one Intel per item.

## Build

Run from the repository root against the exact server JAR being tested:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\games\desert-survival\prototypes\research-tree `
  -ArtifactName DesertSurvival-Research-0.2.8.jar `
  -ServerJar C:\path\to\HytaleServer.jar
```

Build before replacing an installed copy, verify duplicate `Group:Name`, copy
the JAR, compare SHA-256 and restart the world. Replacing a running plugin does
not reload its classes.

After building, run the catalog and Intel contract test with:

```powershell
.\games\desert-survival\prototypes\research-tree\tools\Test-ResearchPrototype.ps1 `
  -ServerJar C:\path\to\HytaleServer.jar
```

Validate that Research packages only its own UI assets:

```powershell
.\games\desert-survival\prototypes\research-tree\tools\Test-Package.ps1 `
  -ArchivePath .\games\desert-survival\prototypes\research-tree\.build\dist\DesertSurvival-Research-0.2.8.jar
```

This is an isolated UI package paired with the last known working
`survival-vitals` 0.2.2. It does not hydrate or override vanilla UI files.

Versions 0.1.2 to 0.1.6 failed the `dune1` join smoke test with the vanilla
CreativeHub error `Could not find document Hud/ReturnToHubButton.ui`. Two
hypotheses were tested in order:

1. 0.1.6 removed `Common/UI/Custom/Pages/DesertResearch/IntelIcon.png`, a byte
   for byte copy of the Intel item icon present in every failing build. The
   join still failed, so duplicate asset content is not the cause (the
   package test keeps rejecting it as hygiene).
2. 0.1.7 removes `ResearchNodeRow.ui` from the package (moved to `legacy/`).
   Its 0.1.2 revision, shipped by every failing build, uses
   `HorizontalAlignment: Right`; vanilla documents only use `Start`, `Center`
   and `End`. The working 0.1.1 had no such value. The hypothesis is that one
   unparseable document invalidates the whole client-side Custom UI document
   set, so the first vanilla `Append` reports a missing document.

0.1.7 ships exactly two documents (`ResearchTree.ui`, `ResearchRingNode.ui`),
draws the rings with the vanilla `$C.@CircularProgressBar` template and
`tools/Test-Package.ps1` now rejects non-`.ui` files under `Common/UI/Custom`,
duplicate Common content and alignment or layout enum values absent from
vanilla. It compiles and passes both tests against the installed 0.7.7 server;
the join smoke test passed on 2026-11-12 (client log `22-17-20`, second
attempt at 22:22); 0.1.7 then disconnected on `/research` because ring nodes were addressed as
`#RingNodes[i] #RingNode`; the appended root is `#RingNodes[i]` itself, as
vanilla pages do. 0.1.8 fixes the selector and `/research` opens in game. 0.2.0 replaces the
per-sector rings with a single wheel (`ResearchWheel.png`, generated by
`tools/Generate-ResearchWheel.ps1`): five 72-degree wedges, one per sector,
three tier bands and all 48 nodes at once, labelled with a short key until
icons exist. Version 0.2.1 replaces those text buttons with square image nodes,
converts inventory copies of Intel, and hides lifetime/material counters.
Its in-game smoke test is pending.

Version 0.2.2 adds the three node states and 37 generated dependency links,
corrects the Combat label position and Spanish accents, and reduces the detail
panel metadata to Intel cost. The selected-node confirmation and tier legend
are removed. In-game visual review is pending.

## Placeholder limits

- The Intel world model temporarily inherits a potion silhouette; its custom
  icon communicates the intended data-shard/orb shape. The final object and VFX
  belong to the dungeon reward art pass.
- Most recipes, the research station, shared/team research and respecs remain
  outside this prototype.
- Basic construction kit, construction gun, beacon behavior, simplified
  suspensor and surveying kit still require separate design decisions.
