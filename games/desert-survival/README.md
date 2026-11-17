# Desert Survival

Current status (sources vs installed vs tested) and short handoff: [STATUS.md](STATUS.md).
Narrative handoff: [2026-11-16 handoff](docs/HANDOFF_2026-11-16.md).
It distinguishes installed JARs, source-only work and design proposals.

Pre-production workspace for a desert survival mode inspired by desert
survival games: progressive resource regions, hostile bases, heat, thirst,
sandstorms, large environmental threats, base construction and vehicles.
The current WorldGen prototype ends in `Void` beyond its fourth zone; the
[2026-11-17 master plan](docs/DESERT_SURVIVAL_MASTER_PLAN_2026-11-17.md)
proposes four additional outer rings, eventually an infinite deep desert.
That expansion is design only.

Current work focuses on the asset bottleneck:

- an improvised cutterray T0 and normal T1–T6, plus proposed unique variants;
- large resource nodes with progressive cutter requirements;
- construction pieces, base machinery, weapons, armor and stillsuits.

The first usable asset is the
[`cutterray-t3`](prototypes/cutterray-t3/README.md) visual prototype. Resource
progression research and the proposed hybrid node architecture live under
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
The [research-to-item map](docs/RESEARCH_UNLOCKS_AND_ITEMS.md) lists every
unlock and the proposed implementation of its objects.
The [materials, recipes and building draft](docs/CRAFTING_MATERIALS_ZONES_AND_BUILDING.md)
assigns suggested ingredients and workstations to those unlocks, places
materials in the four zones and compares block-based building with prefabs.
The [custom building selector proposal](docs/BUILD_SELECTOR_RESEARCH.md) covers
the catalog, 3D preview and placement workflow for pieces and prefab modules.

The version-by-version notes below are historical. For the current installed
version and game validation, use [STATUS.md](STATUS.md); for adopted future
design, use the [master plan](docs/DESERT_SURVIVAL_MASTER_PLAN_2026-11-17.md).
An isolated [build-selector prototype](prototypes/build-selector/README.md)
implements seven multi-block prefabs with a picker, 3D preview and in-world
placement ghost. Version 0.3.0 adds a fixed controls HUD, a 40 x 40 x 100
base claim, placeholder basalt costs, inventory and base chest payment,
registered-piece dismantling and pending refunds claimable from the menu.
Research gates, final materials and full creative-tool protection remain
design work. Q/E cycle pieces, R cycles categories and F switches placement
and dismantling modes; chat only confirms placement, removal and category changes.
Version 0.3.1 uses the native Builder Tools legend style, shows material
availability, an owned-base border and dismantle hover outline. Shield status
is factual placeholder data because shield mechanics do not exist yet. It
needs an in-game smoke test.
Version 0.4.0 uses Q to traverse all seven pieces, since E/R did not dispatch
the requested item interactions. It removes placement/removal chat confirmations,
adds a red dismantle HUD and a separate base-status HUD. The console opens a
four-slot owner-only fuel inventory and consumes `Fuel_Cell_Prototype` from the
cutterray 0.1.9 asset pack at one cell per five minutes. The state persists in
the world ledger; the shield indicator is informational until shield gameplay
is implemented. Existing 0.3.1 consoles migrate their central block in place.
Version 0.4.1 separates navigation: Q advances within the current category;
crouching while pressing Q switches to the next category. The server reads the
player's movement state during the same working item interaction.
Version 0.5.0 starts with a base-console ghost as soon as the planner is held,
shows the proposed claim border, blocks block interaction while the planner is
active, fixes both HUD panel heights and requires a visible two-click warning
before removing a console. Removing it abandons its registered pieces in place;
the site can be claimed by placing a new console at the old origin.
Version 0.5.1 uses the native middle-mouse icon for the rotate hint, fixing the
blank, clipped PickBlock hint shown in the prototype HUD.
Version 0.5.2 shows nearby existing base borders in red while positioning a
new console. Right click changes category while placing and exits dismantle
mode; Q advances pieces in the current category. The console core explicitly
accepts block use, and its fuel window activates the native Bench page. Open
the console with another item or an empty hand; the planner blocks block use.
Version 0.5.3 makes the console a native placeable block and adds native water
purifier and power generator stations with placeholder vanilla models. Their
interfaces open, but water processing and power generation are not implemented.
It also removes category chat messages and colors the placement HUD brown for
base pieces and blue for machinery. Research 0.2.5 maps the generator to
`CN-0.3`; none of these three blocks has a recipe yet.
Version 0.6.0 moves the build HUD to the lower right and simplifies the
selector action to Select. A separate blood purifier processes 250 ml bags into
50 ml water and fills flasks in 250 ml steps. The power generator accepts fuel
cells, reports runtime and has fixed provisional output with no storage or
connected consumers yet. Research 0.2.6 adds the extractor and small blood bag
recipes to the basic exsanguination node. Their models and ingredient sources
remain placeholders; corpse extraction is not implemented yet.
Both JARs are installed for the `dune1` smoke test; in-game validation is
still pending. Previous versions are archived beside the installed JARs.

Version 0.6.1 routes the existing water purifier block to the blood purifier
controls, removes generator chat spam, and shows fuel, output and runtime in
a separate generator panel. A running generator now powers the purifier in
the same claim (or within 12 blocks without a claim), and its output appears
in the base HUD. Research 0.2.7 gives the improvised extractor a native tool
parent and a distinct placeholder icon. The current versions still need an
in-game smoke test; the extractor recipe visibility is not yet confirmed.
Research 0.2.7 is installed globally (SHA-256
`C514E880B5E676533F5F4D20F29881BC7320D5AA796F5720E1930AC447835E48`),
and Build Selector 0.6.1 is installed in `dune1/mods` (SHA-256
`85C4BE6DC72EF3155FCF773421CBAA6E5AA96006E6A5BA631D92E34C5794CCE9`).

Build Selector 0.6.2 restores the generator's native four-slot fuel inventory;
the custom-only status page from 0.6.1 hid the inventory in the client. The
construction catalog now scrolls and reveals the blood purifier, generator,
ore refinery and chemical refinery when their research is unlocked. The old
blood recycler remains loadable for existing builds but is no longer offered.
Generator and purifier recipes were removed from Fieldcraft; existing
improvised items remain pocket-craftable. Selected vanilla recipes are hidden
per client and blocked on the server without mutating global recipe assets.
The refinery blocks and fabricator are visual prototypes. All station prefab
costs are temporary basalt values pending the material economy.
Installed in `dune1/mods` with matching build/install SHA-256
`AE6BA4801923C5E95A5D37251DF8074D1C52987122DF621A6977474DB0EC55E5`.
Build Selector 0.6.1 is archived as `.jar.replaced-20261116`; in-game smoke
test remains pending.

No general gameplay plugin exists yet. The cutterray prototype includes only
the isolated beam, direct-inventory pickup, combat text and five-second copper
extraction loop needed for in-game testing. Prototype packs must remain isolated from the public Raynor Mods
release matrix until their IDs and save-data contracts are stable.
