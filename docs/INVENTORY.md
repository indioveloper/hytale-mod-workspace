# Inventario de migracion

Inventario creado el 2026-07-18 al consolidar los workspaces locales. Las rutas
de origen se conservan aqui solo para poder auditar la migracion; la copia del
monorepo pasa a ser la fuente canonica despues de publicarla.

| Ruta en el monorepo | Origen local | Estado |
| --- | --- | --- |
| `mods/java/player-trigger-tags` | `workspace/trigger-volumes-extensions` | Activo; v1.5.6; compila contra 0.6.8. |
| `mods/java/chest-labels` | `workspace/chest-labels-mod` | Activo/prototipo. |
| `mods/java/scoreboards` | `mod-interfaces/Mod interfaces/scoreboards_fixed` | Legacy 0.5.x. |
| `mods/java/legacy/player-triggers` | `player-triggers-src` + `player-triggers-stage` | Legacy; fuentes y assets reunidos. |
| `mods/java/legacy/player-triggers-1.3.2-local` | `Downloads/Player_Triggers-1_3_2-source` | Snapshot local de este PC; conserva fuentes, UI, localizaciones y notas sin JARs ni `build`. |
| `mods/java/legacy/more-triggers` | `more-triggers-src` + `more-triggers-stage` | Legacy; fuentes y localizaciones reunidas. |
| `mods/java/legacy/trigger-execute-command` | `Desktop/mods/trigger execute command mod` | Mod standalone local; copiado sin JARs, clases ni carpetas de build. |
| `mods/java/legacy/trigger-remove-event-title` | `Documents/App guarde` | Mod standalone local; copiado sin `dist`, JARs ni clases compiladas. |
| `mods/java/legacy/scoreboards-mod-local` | `Desktop/mods/scoreboards mod` | Variante local de Scoreboards Mod; se conserva separada porque difiere de `mods/java/scoreboards`. |
| `mods/java/legacy/trigger-random-tag` | `trigger-random-tag-src` + `trigger-random-tag-stage` | Legacy e incompleto. |
| `games/nexus-siege` | `workspace/nexus-siege` | Activo; incluye NPC assets y `Triggers_NPCs`. |
| `mods/asset-packs/visible-barrier-block` | `Barrier_Block_Texture` | Pack independiente. |
| `mods/asset-packs/essence-buttons` | `essence-pulsadores-stage` | Pack independiente. |
| `mods/asset-packs/real-lever-animation` | `lever-real-stage` | Pack independiente. |
| `mods/asset-packs/ghost-outline-blocks` | pack global `ghost-outline-blocks` | Pack independiente. |
| `mods/asset-packs/raynor-npcs` | pack global `raynor-npcs` | Assets Raynor. |
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
