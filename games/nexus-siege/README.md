# Nexus Siege Workspace

Minijuego competitivo por equipos de hasta 4 jugadores donde cada bando debe
defender su nexo mientras intenta destruir el del enemigo.

## Objetivo

Este workspace separa el desarrollo del proyecto en varias áreas para poder
trabajar con claridad en:

- lógica del mod
- asset pack
- diseño del modo de juego
- pruebas y validación

## Estructura

- `docs/` - visión del proyecto, reglas, roadmap y handoff.
- `npcs/` - trabajo vanilla de NPCs: roles, spawns, comportamiento y pruebas.
- `custom-assets/` - bloques, items, modelos, texturas, UI, audio e iconos propios.
- `trigger-volumes/` - lógica vanilla basada en trigger volumes y notas de integración.
- `server-plugin/` - espacio reservado para la lógica del servidor/plugin.
- `maps/` - diseño de arena, prefabs y notas de layout.
- `tests/` - planes de prueba, casos manuales y resultados.
- `research/` - referencias del API, sistemas similares y notas técnicas.

## Convención de trabajo

- Mantener aquí el trabajo nuevo del minijuego.
- Dejar los artefactos históricos del repositorio raíz como referencia.
- Documentar decisiones de diseño antes de cambios grandes.
- Registrar pruebas y hallazgos importantes en `docs/session-handoff.md`.
- No tocar `server-plugin/` hasta que el desarrollador encargado del plugin lo prepare.
