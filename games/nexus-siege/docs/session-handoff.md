# Nexus Siege Handoff

Last updated: 2026-07-18

## Current State

The dedicated workspace has been created at:

`workspace/nexus-siege/`

The workspace has been split into separate work areas for NPCs, custom assets,
trigger volumes, and server plugin work. Server plugin work is intentionally
reserved and has not been touched.

The first active work area is `npcs/`, with a vanilla-only humanoid runner NPC
documented under `npcs/vanilla/avatar-sword-runner/`.

Current NPC asset candidate:

- Role source: `mods/asset-packs/raynor-npcs/Server/NPC/Roles/Nexus_Avatar_Sword_Runner.json`
- Pack/plugin manifest: `mods/asset-packs/raynor-npcs/manifest.json`
- Runtime contract: the NPC expects a stored position slot named `NexusTarget`.
- Behavior: reads `NexusTarget`, uses `Seek` with `Walk`, and runs primary
  `Attack` plus `PlayAnimation` on the `Action` animation slot.

## Decisions

- Use `Nexus Siege` as the working title for now.
- Keep the new minigame isolated from older experiments in the repository root.
- Split work into NPCs, custom assets, trigger volumes, server plugin, maps,
  tests, research, and docs.
- Build a small playable prototype before attempting broad feature scope.
- Do not edit plugin/server logic yet; another developer will prepare it.
- Start NPC work with vanilla assets and vanilla behavior systems.
- Keep reusable NPC assets and Trigger Volume NPC effects in `Raynor NPCs`
  instead of project-specific Nexus Siege packs.
- Treat the Hytale source code on GitHub as the technical source of truth for
  vanilla APIs, schemas, NPC behavior, trigger volumes, and server internals.

## Suggested Next Steps

1. Confirm the exact vanilla asset IDs for the humanoid model, sword item, and
   sword swing animation.
2. Confirm the simplest vanilla-only way to write a fixed destination into the
   NPC stored position slot `NexusTarget`.
3. Test the NPC role with a fixed target point in a small flat arena.
4. Keep any future plugin handoff notes in `server-plugin/README.md`.
