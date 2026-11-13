# Loot and blueprints - first design draft

## What Dune: Awakening does

Dune separates several progression channels that should not be collapsed into
one generic loot table:

- Intel pickups are found mainly in hostile NPC bases. Intel points buy
  permanent entries in the research tree for items, equipment, weapons and
  vehicles. The total Intel cap currently matches the cost of the complete
  tree.
- Equipment, consumables, resources and components can also appear directly
  as loot.
- Unique schematics are physical discoveries associated with particular
  location/security-zone pools, such as Testing Stations and shipwrecks.
  Current Dune patches keep those pools available instead of rotating most of
  them weekly. PvP locations offer better drops, while loot is personal.
- Community documentation describes unique schematics as limited crafting
  permissions rather than ordinary permanent research unlocks. This detail is
  useful as inspiration, but should be treated as community-documented rather
  than as an immutable rule of Dune.
- Repeatable dungeons use scaling difficulty: harder versions improve the
  value/quality of loot. Some progression items, such as Augment schematics,
  are exclusive to those dungeons.
- Dune also uses the word blueprint for a saved holographic base layout. That
  is a different system from an item schematic.

Primary references:

- [Intel pickups and the research cap](https://funcom.helpshift.com/hc/en/4-dune-awakening/faq/55-unable-to-gather-more-intel-points/)
- [Location/security-zone schematic pools and no weekly rotation](https://duneawakening.com/fr/news/dune-awakening-1-2-10-0-patch-notes/)
- [Personal Deep Desert loot and PvP reward advantage](https://duneawakening.com/news/new-update-now-live-with-vehicle-recovery-system-and-more/)
- [Scaling dungeons and exclusive Augment schematics](https://duneawakening.com/fr/news/dune-awakening-chapter-3-patch-notes/)

## Proposed system for Desert Survival

Use four loot layers:

1. **Supplies:** water, healing, ammunition and common resources. Frequent and
   primarily there to sustain the expedition.
2. **Components:** electronics, alloys, filters and vehicle/base parts. These
   connect exploration to crafting without replacing resource gathering.
3. **Direct equipment:** uncommon weapons, armor or tools, normally damaged or
   below the best craftable quality. It creates immediate excitement without
   making crafting irrelevant.
4. **Schematics:** the jackpot and the main horizontal progression reward.

Every container should resolve a data-driven loot context:

```text
biome tier + POI family + difficulty + container class + boss/event flags
```

Pools remain stable and discoverable. Difficulty raises quantity, quality and
the chance of rare results; it should not make essential progression depend on
an extreme random drop.

## Recommended schematic model

Use a hybrid of permanent unlocks and limited permissions:

- **Essential schematic:** permanently unlocks the recipe. The first
  cutterray, stillsuit, basic fabricator, base utilities and first vehicle
  parts are guaranteed by a journey, dungeon milestone or pity-protected
  chest. These must never hard-lock a player through bad luck.
- **Unique schematic:** the first copy reveals the recipe and grants one craft
  permission. Further copies add permissions, so duplicate drops remain
  useful. A permission is consumed only after crafting completes successfully.
- The physical schematic is tradable until learned. Learned recipes and their
  permission balance are character-bound for the MVP.
- Final cooperative/boss chests use personal loot so one player cannot take
  another player's progression drop.

The Codex should show recipe state, remaining permissions, material tier and a
broad source hint such as `Testing Station - Zone 2`; it should not reveal the
exact chest or percentage by default.

For naming in Spanish:

- **Esquema:** item recipe/unlock and its crafting permissions.
- **Plano de base:** saved prefab/layout used to reconstruct a building.
- **Intel:** future research currency, separate from both.

## MVP boundary

Factions and the research tree are outside the MVP, so Intel should not yet be
a spendable progression system. Either omit it or store Intel pickups as a
future-proof counter without exposing a shop. The MVP can be carried by
guaranteed essential schematics plus rare limited-use unique schematics.

First implementation slice:

- JSON definitions for loot pools, schematic definitions and POI contexts;
- persistent per-player permanent unlocks and permission counts;
- one guaranteed essential schematic in an early hostile base;
- one rare, tradable unique schematic in a repeatable dungeon;
- personal final chest, duplicate protection for the essential schematic and
  atomic permission consumption when the craft output is delivered.
