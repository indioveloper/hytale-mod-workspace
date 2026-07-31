# Inventario de migracion

Inventario creado el 2026-07-18 al consolidar los workspaces locales. Las rutas
de origen se conservan aqui solo para poder auditar la migracion; la copia del
monorepo pasa a ser la fuente canonica despues de publicarla.

Actualizado el 2026-07-31 tras la consolidacion de Trigger Volumes.

| Ruta en el monorepo | Origen local | Estado |
| --- | --- | --- |
| `mods/java/more-triggers` | Utilidades activas y antiguo `trigger-execute-command` | Activo/prototipo; v1.6.0; integra `ExecuteCommand` conservando su ID. |
| `mods/java/entity-motion-triggers` | Extraido desde `mods/java/player-trigger-tags` | Activo/prototipo; v1.3.0; agrupa creacion, conversion, movimiento, colision y particulas ancladas. |
| `mods/java/deprecated/player-entity-tags` | `workspace/trigger-volumes-extensions`; funciones utiles de `player-triggers-src` y `Downloads/Player_Triggers-1_3_2-source` revisadas | Deprecated; conserva `ModifyPlayerTag`, `PlayerTagCondition` y datos legacy. |
| `mods/java/deprecated/trigger-execute-command-standalone` | `Desktop/mods/trigger execute command mod` | Deprecated; sustituido por More Triggers 1.6.0. |
| `mods/java/deprecated/chest-labels` | `workspace/chest-labels-mod` | Deprecated; UI sin portar a pre-release 0.6.8. |
| `mods/java/scoreboards` | `mod-interfaces/Mod interfaces/scoreboards_fixed` + `Desktop/mods/scoreboards mod` | Legacy 0.5.x unificado; la carpeta local separada fue absorbida/eliminada. Pendiente de migrar `Common.ui`: usa `PageOverlay`, nodo no reconocido por pre-release 0.6.8. |
| `games/nexus-siege` | `workspace/nexus-siege` | Activo; los assets/plugins NPC reutilizables se movieron a `mods/asset-packs/raynor-npcs`. |
| `mods/asset-packs/blocks` | `Barrier_Block_Texture` + pack global `ghost-outline-blocks` | Pack fusionado de barrera visible y ghost outline blocks. |
| `mods/asset-packs/mechanisms` | `essence-pulsadores-stage` + `lever-real-stage` | Pack fusionado de botones de esencia y palancas animadas. |
| `mods/asset-packs/raynor-npcs` | pack global `raynor-npcs` + `Triggers_NPCs` + `Nexus Siege NPCs` | Mod/asset pack unificado de NPCs reutilizables. |
| `mods/asset-packs/nexus-siege-props` | pack global `Raynor.Nexus Siege Props` | Props Nexus Siege. |
| `experiments/asset-packs/tests-0.6.3` | pack global `0.6.3.tests` | Snapshot experimental. |
| `experiments/asset-packs/tests-0.6.4` | `disabled-pack-test/0.6.4.Tests` | Flechazo, outlines, ventanas y prefabs. |
| `experiments/asset-packs/tests-0.6.5` | pack global `tests.0.6.5` | Snapshot experimental. |
| `experiments/asset-packs/tests-0.6.7` | pack global `tests.0.6.7` | Snapshot experimental. |
| `experiments/asset-packs/archerygame` | pack global `tests.archerygame` | Prototipo de minijuego de arqueria. |
| `experiments/asset-packs/raynor-tests` | pack global `tests.raynor tests` | Pruebas Raynor. |
| `experiments/asset-packs/raynor-test-commands` | pack global `raynor.test commands` | Comandos/assets de prueba. |
| `experiments/asset-packs/trigger-assets` | pack global `triggers.triggers` | Assets de triggers. |
| `experiments/asset-packs/prefab-tests` | pack global `tests prefabs.caja` | Prefab minimo de prueba. |

## Excluido deliberadamente

- JARs y ZIPs compilados: se regeneran o se publican mediante GitHub Releases.
- `build`, `dist`, `target`, clases y carpetas de staging con bytecode.
- Logs, backups y archivos temporales `.bak`/`.lpf`.
- Saves y regiones de mundos: son binarios grandes; deben compartirse como
  snapshots externos cuando haga falta continuar un mapa concreto.
- `HytaleServer.jar`, assets extraidos y `hytale-shared-source`: dependencias
  externas que no pertenecen a este repositorio.
- Mods de terceros instalados, como MultipleHUD, Connect4 o extensiones ajenas.
