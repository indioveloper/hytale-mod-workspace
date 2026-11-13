# Frontier Claims — Session Handoff

## 2026-09-05 — Diseño inicial

- Origen: idea del usuario (juego de mesa de conquista de territorios sobre
  Hytale) + boceto Excalidraw con el mapa de 80 POIs, precios por tier y
  catálogo de recompensas.
- Se cerraron con el usuario: escala (instancias de hasta 10 jugadores),
  duración (7 días, no 2 semanas), mecánica de racha (más tokens, no acceso a
  tiers), reset de temporada (todo se pierde salvo ranking histórico),
  monetización (solo hook de datos, sin backend), y origen del mapa
  (worldgen vanilla + colocación manual de POIs, no mapa 100% a mano).
- Se investigó el Shared Source y se confirmó que `HytaleServer/builtin/Instances`
  ya da un sistema de instancias por copia/por grupo listo para usar
  (`InstancesPlugin.spawnInstance`, `RemovalCondition`s). Hallazgo importante:
  `WorldEmptyCondition` cerraría la instancia en cuanto el grupo se
  desconectara, lo cual rompería el farmeo offline con NPCs que pedía el
  usuario — el diseño usa `TimeoutCondition` (7 días) en su lugar.
- Entregables de esta sesión: `docs/project-brief.md` (diseño completo) y
  `docs/technical-architecture.md` (mapeo a la API real, con rutas de archivo
  exactas en el Shared Source).
- **No se ha escrito código todavía.** Nada de esto está verificado con
  `javap` contra el `HytaleServer.jar` de 0.6.3 estable.

## 2026-09-05 — Segunda pasada: dudas de diseño cerradas

Se resolvieron las cinco preguntas abiertas del brief §13 original:

- **Disputa de territorio**: solo POIs libres, sin combate para arrebatar uno
  ya reclamado (riesgo aceptado: un territorio de un jugador inactivo queda
  bloqueado toda la temporada — no hay mitigación en v1, ver brief §4).
- **Formación de instancia**: cola automática (matchmaking simple), no
  grupos/party ni instancia por gremio (brief §2.1).
- **Curva de racha**: lineal suave con tope, +1 extra en racha 3-4, +2 en
  racha 5-6, +3 (tope) en racha 7; un hueco de un día resetea a 1 token/día
  (brief §6.1). Cifras de partida, no definitivas.
- **Fórmula de poder acumulado**: suma de recursos totales producidos durante
  la temporada, no territorios poseídos al cierre (brief §11).

Único punto de diseño que sigue abierto: **balance económico real** (costes
por tier, tasas de producción, tamaños de buffer) — es una pasada de números,
no una decisión de sistema; ver brief §13 para cuándo abordarla.

## 2026-09-05 — Tercera pasada: mapa procedural en vez de colocación a mano

El usuario pidió replantear cómo se crea el mundo: en vez de un mapa
hecho a mano una sola vez, generación procedural con biomas/zonas por tipo de
POI, al estilo de cómo vanilla reparte biomas y coloca pueblos/dungeons.

Investigación en el Shared Source (módulo `HytaleGenerator` **clásico**,
compatible con 0.6.3 estable — no hace falta WorldGen V2): el pipeline
`WorldStructure → Biome → Props → PrefabPropAsset` ya existe y es el mismo
que usa vanilla. Hallazgo importante: `VolumeEntry.FromWorldGen` y
`TriggerVolumeManager.removeWorldGenVolumesInChunk` confirman que una Trigger
Volume puede vivir **dentro de un prefab** y registrarse sola cuando el
generador la coloca — cada tipo de POI puede traer su propia Trigger Volume
embebida en el prefab, sin pegarla aparte a mano.

Decisión de semilla (dada por el usuario): todas las instancias abiertas la
misma semana usan la misma semilla (igualdad de condiciones entre grupos
paralelos); cada nueva temporada (7 días) usa una semilla distinta
(variedad). Mecanismo: reescribir `WorldConfig.Seed` en el `instance.bson`
del asset maestro entre temporadas, antes de que `spawnInstance` abra la
primera copia de la semana — ver `technical-architecture.md` §3.3 para el
detalle y el punto pendiente de cómo persistir ese cambio de semilla (el
flujo de edición en vivo de instancias no guarda cambios de configuración).

Quedan tres puntos de generación procedural sin resolver (§13 del brief,
no bloquean seguir diseñando pero sí bloquean construir las 6 zonas
completas): cómo tallar el HUB como región fija, si el conteo de POIs por
zona puede ser aproximado o debe forzarse exacto, y si el bug de rotación de
prefabs en runtime (documentado en la GUIA del workspace) también afecta al
pegado en tiempo de generación.

Efecto en el roadmap: la fase 1 (vertical slice) creció — ya no es solo
lógica de plugin sobre una Trigger Volume colocada a mano, sino también un
`WorldStructureAsset` mínimo de una sola zona con un prefab de mina que trae
su Trigger Volume embebida, para validar que el registro `FromWorldGen`
funciona de verdad antes de construir las 6 zonas completas.

Se creó `games/frontier-claims/server-plugin/src/manifest.json` (Group
OrbGenesis, Name "Frontier Claims", package `gg.orbgenesis.frontierclaims`)
como punto de partida del plugin Java — el resto de la fase 1 (efectos
`ClaimTerritory`/`CollectMine`, wallet del jugador, sistema de producción de
minas, y el `WorldStructureAsset` mínimo) no se ha escrito todavía.

## Próximos pasos sugeridos

1. Validar con `javap` las clases de `builtin.instances` citadas en
   `technical-architecture.md` contra el JAR de 0.6.3 estable instalado
   localmente.
2. Elegir un nombre definitivo antes de fijar cualquier namespace/ID
   persistente (el actual, "Frontier Claims", es provisional).
3. Empezar por la fase 1 del roadmap (vertical slice: una mina + un
   territorio + reclamar con token) antes de construir NPCs o dungeons. La
   pasada de balance económico puede esperar a tener datos reales de esa
   fase.
