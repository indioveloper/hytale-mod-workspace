# Nexus Siege Workspace Index

Last updated: 2026-07-11

## Purpose

This workspace is the clean development area for the new team-vs-team minigame.
The core loop is defending your own nexus while coordinating attacks on the
enemy nexus in teams of up to 4 players.

## Clean Workspace Layout

- `docs/` - planning notes, game rules, handoffs, and design decisions.
- `npcs/` - vanilla NPC roles, spawn definitions, behavior specs, and NPC tests.
- `custom-assets/` - custom blocks, items, textures, models, UI, audio, and exports.
- `trigger-volumes/` - vanilla Trigger Volume definitions, experiments, and integration notes.
- `server-plugin/` - reserved area for server plugin work owned by another developer.
- `maps/` - arena layouts, spawn logic notes, and prefab planning.
- `tests/` - smoke tests, gameplay test scripts, and regression notes.
- `research/` - API findings, mechanic references, and technical experiments.

## Active Vision

Working title: `Nexus Siege`

Core pillars:

- Team competition with clear offense and defense roles.
- Fast match readability: players should always understand how close each nexus
  is to destruction.
- Room for progression inside a match through control, upgrades, or pressure.
- Strong feedback through UI, VFX, SFX, and map state changes.

## Initial Production Tracks

### Game Design

- Define win condition and nexus health model.
- Decide respawn rules and anti-stomp protections.
- Specify team size support from `1v1` up to `4v4`.
- Define match flow: early, mid, and final push.

### NPCs

- Start with vanilla-only NPC behavior.
- First NPC: a humanoid avatar using `randommodel`, equipped with a sword, that
  moves toward a configured point while swinging.
- Keep plugin-dependent orchestration out of this track for now.

### Server Plugin

- Identify required systems: team assignment, nexus state, scoring, respawn,
  events, HUD, and match lifecycle.
- Reserved for a different developer. Do not modify until ownership is clear.

### Custom Assets

- Create visual language for each team and both nexuses.
- Plan HUD assets for nexus health, team status, and alerts.
- Track required block, model, texture, icon, and sound additions.

### Trigger Volumes

- Document vanilla event flows separately from plugin logic.
- Use trigger volumes for map-driven tests where possible.
- Avoid encoding long-term server rules here until the plugin boundary is agreed.

### Map

- Sketch one small test arena first.
- Define base layout, lanes or routes, safe spawn positions, and objective area.
- Document sightline and travel-time balance assumptions.

## Working Rules

- Keep historical artifacts intact unless explicitly approved.
- Prefer implementing new minigame work inside this workspace.
- Before medium or large edits, outline the intended files and goal.
- Keep `docs/session-handoff.md` current after meaningful milestones.
