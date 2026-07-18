# Nexus Siege Project Brief

Last updated: 2026-07-11

## Premise

Two teams compete to destroy the opposing nexus while protecting their own.
Each team can contain up to 4 players.

## Baseline Loop

1. Players spawn in their team base.
2. Teams push across the arena, contest space, and pressure the enemy side.
3. Attacks or triggered mechanics damage the enemy nexus.
4. The first team to destroy the opposing nexus wins the match.

## Questions To Resolve

- How much direct damage can players deal to a nexus?
- Does the nexus become vulnerable only after meeting conditions?
- Are there lanes, generators, upgrades, or summonable units?
- Is inventory persistent across deaths or partially reset?
- How long should a standard match last?
- What comeback tools exist if one team is behind?

## First Prototype Goal

Create a very small playable slice with:

- team assignment
- one arena
- one nexus per side
- visible nexus health
- respawn flow
- match start and match end handling

## Definition Of Done For Prototype

- A local server host can start a match.
- Players are assigned to two teams.
- Each nexus can take damage and broadcast its remaining health.
- Destroying a nexus ends the match cleanly.
- A short test checklist exists in `tests/manual/prototype-smoke-test.md`.
