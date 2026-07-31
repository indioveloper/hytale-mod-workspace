# Scoreboards / Editable Objectives

Mod para crear y controlar `Objectives` editables sobre el motor nativo de
Hytale pre-release 0.6.8.

Estado: prototipo funcional `2.0.10`, pendiente de smoke test dentro del juego.

## Funciones

- Definiciones persistentes en la configuracion del plugin.
- Hasta cinco tareas numericas por Objective desde la UI.
- IDs estables separados del titulo y del texto visible.
- Instancias y progreso persistidos por `ObjectiveDataStore`.
- Texto libre en titulo, descripcion y filas del HUD.
- Progreso individual, compartido por mundo o compartido por Trigger Volume.
- Restauracion visual al reconectar.
- Comandos, editor nativo y efectos/condiciones de Trigger Volumes.

El mod registra la tarea nativa personalizada `OrbGenesisManualCount`. Las
definiciones se publican como assets dinamicos con IDs:

```text
OrbGenesis_Scoreboard_<definition_id>
```

Una `ObjectiveLine` de Hytale es una cadena de Objectives. Las filas visibles
de estos scoreboards son `ObjectiveTask`; no se crean `ObjectiveLineAsset`.

## Comandos

- `/scoreboard`: abre la lista y el editor.
- `/scoreboards ui`: abre la misma interfaz.
- `/scoreboards list`
- `/scoreboards info <id>`
- `/scoreboards create <id> <titulo>`
- `/scoreboards edit <id>`
- `/scoreboards delete <id>`
- `/scoreboards start <id>`
- `/scoreboards set <id> <taskId> <valor>`
- `/scoreboards add <id> <taskId> <valor>`
- `/scoreboards show <id>`
- `/scoreboards hide <id>`
- `/scoreboards complete <id>`
- `/scoreboards cancel <id>`

Los comandos de control trabajan sobre la instancia individual del jugador que
los ejecuta. Los modos compartidos se configuran desde Trigger Volumes.

## Trigger Volumes

Efectos:

- `ControlScoreboard`: `START`, `SHOW`, `HIDE`, `COMPLETE` o `CANCEL`.
- `ModifyScoreboardTask`: `SET`, `ADD` o `SUBTRACT` sobre una tarea por ID.

Condiciones:

- `ScoreboardState`: `ACTIVE`, `INACTIVE` o `COMPLETED`.
- `ScoreboardTaskValue`: compara el valor actual de una tarea.

Destinatarios:

- jugador activador;
- jugador mas cercano;
- jugadores dentro del volumen;
- todos los jugadores.

Ambitos:

- `INDIVIDUAL`;
- `WORLD_SHARED`;
- `VOLUME_SHARED`.

El ambito usado para modificar, mostrar u ocultar debe coincidir con el usado
al iniciar la instancia.

## Build y pruebas

```powershell
.\mods\java\scoreboards\tools\Test-PluginLocalization.ps1

.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\scoreboards `
  -SourceRoot src `
  -PackageRoot . `
  -AssetsRoot assets `
  -ArtifactName Scoreboards-2_0_10.jar

.\mods\java\scoreboards\tools\Test-Package.ps1 `
  -ArchivePath .\mods\java\scoreboards\.build\dist\Scoreboards-2_0_10.jar

.\mods\java\scoreboards\tools\Test-AssetThreading.ps1

.\mods\java\scoreboards\tools\Test-DynamicTranslations.ps1
```

El paquete no debe contener `Common/UI/Custom/Common.ui`: ese override puede
romper la UI completa del cliente.

Las altas, ediciones y eliminaciones de `ObjectiveAsset` se ejecutan fuera del
hilo del mundo. El tick mantiene el `ASSET_LOCK` de lectura y una mutacion
sincrona del asset store desde un evento de UI bloquearia el servidor.

Antes de iniciar o resincronizar una Objective, el mod envia al cliente las
traducciones dinamicas de su `TitleId` y `DescriptionId`. Esto permite usar el
titulo y la descripcion editables sin mostrar las claves
`server.objectives.*`.

## Legacy

`CONTEXTO_SCOREBOARDS.md` conserva la investigacion de la version 0.5.x. Las
clases sustituidas se guardan como texto bajo `legacy/`; no se compilan ni son
fuente de verdad para 0.6.8.

## Pendiente de validar en juego

- Apertura y bindings de la lista dinamica.
- Creacion, edicion y arranque desde la UI.
- Persistencia tras reiniciar el save.
- Resync del texto libre tras reconectar.
- Los tres ambitos desde Trigger Volumes.
- Finalizacion automatica al alcanzar todas las metas.
