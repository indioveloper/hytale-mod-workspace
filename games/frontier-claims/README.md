# Frontier Claims (working title)

Evento temporal de conquista y farmeo territorial. No es un minijuego PvP como
Nexus Siege ni es supervivencia: es un "algo que hacer" diario con progresión
por temporada, pensado para dar un motivo para conectarse cada día durante una
ventana corta (7 días) y competitiva por grupo.

Nombre provisional. Cambiar antes de fijar namespaces/IDs persistentes si se
decide otro (ver [Higiene Git y persistencia](../../AGENTS.md), regla de no
renombrar IDs sin migración).

## Premisa

Hasta 10 jugadores comparten una copia de un mapa de 80 puntos de interés (POI)
repartidos en tiers de coste/recompensa. Cada día pueden reclamar territorios
nuevos con un token de conquista (acumulable, con bonus por racha de conexión).
Los territorios dan minas autónomas, spawners de mobs, parcelas construibles o
entradas a mini-dungeons. NPCs contratables farmean territorios propios incluso
con el jugador desconectado. A los 7 días la instancia se cierra; solo persiste
un ranking histórico del "poder" alcanzado.

## Documentos

- [`docs/project-brief.md`](docs/project-brief.md) — diseño completo: bucle,
  economía, POIs, NPCs, reglas de temporada, catálogo de recompensas.
- [`docs/technical-architecture.md`](docs/technical-architecture.md) —
  mapeo a la API real de Hytale (sistema `Instances` del Shared Source),
  decisiones de arquitectura y riesgos verificados contra el código fuente.
- [`docs/session-handoff.md`](docs/session-handoff.md) — estado y próximos
  pasos para retomar con otro agente/sesión.

## Estructura planeada (se crea bajo demanda, no todavía)

Siguiendo el patrón de `games/nexus-siege`: `maps/` (instancia base y
colocación de POIs), `trigger-volumes/` (lógica de territorio sobre
Trigger Volumes de `more-triggers`), `npcs/` (roles Recolector/Guerrero sobre
`mods/asset-packs/raynor-npcs`), `server-plugin/` (economía, temporada,
ranking persistente), `custom-assets/`, `tests/`, `research/`.

No tocar `server-plugin/` hasta que exista un plan de datos aprobado (ver
technical-architecture.md, sección de persistencia).
