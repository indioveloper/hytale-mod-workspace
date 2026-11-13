# Research tree MVP

## Shape

- Header: current available Intel and a custom Intel texture.
- Five sectors: Water, Combat, Construction, Exploration and Vehicles.
- Three material tiers: Scrap (0), Copper (1), Iron (2).
- 48 explicit nodes. Zero-cost nodes still require an intentional click.
- A fixed horizontal sector row opens a focused three-column table rather than
  rendering every node at once. Each tier column scrolls independently.

The stable IDs use `AG`, `CO`, `CN`, `EX` and `VE`. Display names can change;
save data and future recipe checks must continue using the IDs.

## Intel contract

Intel is a character-bound currency, not an inventory item after collection.
Dungeon designers place a world pickup at the end of an enemy base or dungeon.
On contact it disappears and adds its configured amount to:

- `AvailableIntel`: spendable balance;
- `LifetimeIntel`: total ever recovered, never reduced.

Research validates prerequisites and balance, then atomically subtracts
the cost and adds the node ID to the persistent unlocked set. Collecting a
stack scales the reward by stack quantity. The prototype asset is worth one
Intel per item.

Material access is stored independently from Intel, but does not gate research.
A player may plan and unlock a future recipe before reaching its biome; actual
crafting remains impossible until the required material is available. Explicit
prerequisites still enforce the order between lower and higher-tier nodes. The
prototype command simulates material access; the final progression manager must
own it. The persisted key remains `MaxResearchTier` for save compatibility.

## Review points before recipe integration

1. Confirm every placeholder dependency in `ResearchCatalog.java`.
2. Decide whether free nodes are visible immediately or revealed by narrative
   milestones.
3. Decide whether Intel is strictly personal, party-shared or awarded once to
   every eligible player in a completed dungeon.
4. Define duplicate-completion behavior for resettable bases.
5. Replace prototype dev commands with permissions or remove them.
