# Inventario de la instalacion pre-release

Fecha de revision: 2026-07-29.

Ruta revisada:
`%APPDATA%\Hytale\data\pre-release`.

Runtime de referencia: Hytale pre-release 0.6.8.

## Criterio

Se revisaron las 27 partidas, `Mods`, `Disabled Mods`, los manifests de
directorios y archivos, las configuraciones de cada save y los logs recientes.

Las carpetas `Saves\<save>\mods\Hytale_*` y otras carpetas sin
`manifest.json` son datos persistentes de plugins. No son instalaciones
cargables. Los JAR, ZIP, RAR, regiones, BSON, backups y saves completos no se
copian al historial Git.

## Contenido global

### Mods Java

| Mod | Version | Estado |
| --- | --- | --- |
| Build Battle | 0.2.2 | Prototipo 0.6.x. Activo en `Build Battle tests 2`. |
| Trigger Execute Command | 1.1.0 | Actualizado a 0.6.x; revisar comandos destructivos en juego. |
| Triggers NPCs | 1.2.0 | Funciones incorporadas posteriormente a Raynor NPCs; el manifest instalado usa el formato de version antiguo. |
| MovementCommands | 1.0.3 | Mod externo con `ServerVersion: "*"`. Fuente no localizada en este workspace. |
| SimpleCommandInteractionPlugin | 1.0.2 | Mod externo con `ServerVersion: "*"`. Fuente no localizada. |
| EffectShowcase | 1.0.3 | Declara `<0.6.0`; incompatible con 0.6.8. |
| Scoreboards legacy | 1.0.0 | Codigo 0.5.x, pendiente de adaptar y revalidar. |
| Trigger Volumes Camera | 1.1.1 | Construido para 0.5.0-pre.8; pendiente de revalidar. |

`Entity Motion Triggers` 1.0.8 estaba en `Disabled Mods`. `dungeon temporal`
tenia una copia binariamente identica. `test roguelike` tenia otra compilacion
con manifest 1.1.1 aunque el archivo conservaba el nombre `1_0_8.jar`.

### Packs de assets

Packs 0.6.x relativamente limpios:

- OrbGenesis Mechanisms 1.0.3.
- Ghost Outline Blocks 1.0.0.
- Visible Barrier Block 1.0.0.
- Essence Pulsadores 1.0.0, ya integrado en Mechanisms.
- Real Lever Animation 1.0.0, ya integrado en Mechanisms.
- Nexus Siege Props 1.0.0.
- Nexus Siege NPCs 0.1.0, reemplazado por Raynor NPCs.
- `raynor-npcs` 1.0.0, reemplazado por Raynor NPCs 1.1.0.

Packs experimentales:

- `0.6.3:tests`, `tests:0.6.5`, `tests:0.6.7`.
- `tests:archerygame`, `tests:raynor tests`, `tests:tests`.
- `tests prefabs:caja`, `raynor:test commands`, `triggers:triggers`.
- `juego pilares:juego pilares`.
- Connect4 1.0.0, compuesto por assets y una instancia de mundo.

Los archivos `OrbGenesis-Mechanisms.rar`,
`real-lever-animation.rar` y `Visible_Barrier_Block-1_0_0.zip.bak` no son
formatos cargables y los logs confirman que Hytale los omite.

## Contenido local por save

### `dungeon temporal`

- OrbGenesis Blocks 1.1.3.
- OrbGenesis Mechanisms 1.0.3.
- Chest Labels 0.1.0.
- Entity Motion Triggers 1.0.8.
- Player Trigger Tags 1.5.6.
- Raynor NPCs 1.1.0.
- Scoreboards Mod 1.0.0.
- Trigger Execute Command 1.1.0.
- Nexus Siege Props global activado.

Mechanisms estaba instalado globalmente y localmente con el mismo
`Group:Name`; no deben habilitarse ambas copias simultaneamente.

### `test roguelike`

- Entity Motion Triggers 1.1.1, activo.
- More Triggers 1.1.0, activo.
- Map Selector 0.1.1, instalado pero desactivado.
- `tests:tests`, activo como pack de mapas y prefabs.

## Estado por partida

| Partida | Componentes configurados como activos y presentes |
| --- | --- |
| `0.6.0-pre2` | Ninguno. |
| `0.6.0-pre3` | `0.6.3:tests`. |
| `0.6.4 2` | Ninguno. |
| `0.6.5` | Camera, Trigger Execute Command, `tests:0.6.5`, Real Lever Animation, Essence Pulsadores y `raynor-npcs`. Player Trigger Tags y Extended estan referenciados pero ausentes. |
| `0.6.6 aventura` | Ninguno. |
| `0.6pre4` | Ninguno. |
| `4` | `raynor:test commands`, SimpleCommandInteraction y Trigger Execute Command. |
| `Andrew` | Solo referencias y datos historicos de mods externos ya ausentes. |
| `Build Battle tests` | Sin `config.json` ni carpeta de mods. |
| `Build Battle tests 2` | Build Battle y Trigger Execute Command. |
| `connect4` | Connect4 1.0.0. |
| `Dia 1` | Referencias historicas; archivos cargables ausentes. |
| `dungeon temporal` | Conjunto local descrito arriba. |
| `FayBelle trigger thing` | `tests:raynor tests`, `raynor-npcs` y Trigger Execute Command. |
| `GDD` | Nexus Siege NPCs, Nexus Siege Props, Trigger Execute Command y Triggers NPCs. |
| `New World` | MovementCommands, SimpleCommandInteraction, `raynor:test commands`, Trigger Execute Command, Camera y scoreboards legacy. |
| `New World 1` | Referencia obsoleta a `Scoreboards Mod`. |
| `New World 2` | Ninguno. |
| `New World 2.1` | Ninguno. |
| `Pruebas Triggers` | `tests prefabs:caja`, Camera, Trigger Execute Command y `raynor:test commands`. |
| `test infinity pillars` | `juego pilares` y Entity Motion Triggers. |
| `test roguelike` | `tests:tests`, More Triggers y Entity Motion Triggers. |
| `test5` | Trigger Execute Command y Camera. |
| `test6` | Referencias a Camera, MovementCommands, SimpleCommandInteraction, Trigger Execute Command y scoreboards; no tiene carpeta local de mods. |
| `test6 (pre-release)` | Todo desactivado. |
| `test7` | MovementCommands, SimpleCommandInteraction, Trigger Execute Command, Camera y scoreboards. |
| `triggers 2` | Camera; Extended esta referenciado pero ausente. |

## Estado de recuperacion

Se importaron las fuentes editables que faltaban:

- OrbGenesis Blocks 1.1.3 y los tally marks.
- La version instalada de Nexus Siege Props.
- Ghost Outline Blocks y Visible Barrier Block como distribuciones
  independientes.
- Las distribuciones legacy de Essence Buttons, Real Lever Animation y Nexus
  Siege NPCs.
- Infinity Pillars.
- Los assets editables de Connect4.

No se importaron:

- JAR externos o propios sin fuente diferenciada.
- ZIP, RAR, archivos `.bak`, builds o clases.
- La instancia de Connect4, regiones `.region.bin` y BSON.
- Datos persistentes de plugins bajo los saves.

Los binarios propios recuperables deben publicarse como GitHub Release
artifacts. Los binarios de terceros no deben republicarse sin comprobar antes
su licencia.
