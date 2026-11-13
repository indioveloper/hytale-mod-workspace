# Desert Survival

Pre-production workspace for a closed-map survival mode inspired by desert
survival games: progressive resource regions, hostile bases, heat, thirst,
sandstorms, large environmental threats, base construction and vehicles.

Current work focuses on the asset bottleneck:

- six cutterray tiers and their extraction beams;
- large resource nodes with progressive cutter requirements;
- construction pieces, base machinery, weapons, armor and stillsuits.

The first usable asset is the
[`cutterray-t3`](prototypes/cutterray-t3/README.md) visual prototype. Resource
progression research and the chosen hybrid node implementation live under
[`docs`](docs).

The first loot and blueprint proposal is documented in
[`docs/LOOT_AND_BLUEPRINTS_DRAFT.md`](docs/LOOT_AND_BLUEPRINTS_DRAFT.md). It
separates permanent essential recipes, limited-use unique schematics and saved
base layouts so the three systems can evolve independently.

The [`survival-vitals`](prototypes/survival-vitals/README.md) prototype adds a
first playable heat/hydration loop: direct-sun detection, four environment heat
tiers, a compact HUD, water flasks and a stillsuit reservoir.

The [`research-tree`](prototypes/research-tree/README.md) prototype adds the
five-sector MVP technology tree, persistent character Intel and a dungeon
pickup that converts directly into research currency. Its interface and save
contract are summarized in [`RESEARCH_TREE_MVP.md`](docs/RESEARCH_TREE_MVP.md).

No general gameplay plugin exists yet. The cutterray prototype includes only
the isolated beam, direct-inventory pickup, combat text and five-second copper
extraction loop needed for in-game testing. Prototype packs must remain isolated from the public Raynor Mods
release matrix until their IDs and save-data contracts are stable.
