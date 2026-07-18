# Hytale Mod Workspace

Monorepo de desarrollo para los mods, asset packs y prototipos de Hytale de
OrbGenesis/Raynor. Reune las fuentes que hasta julio de 2026 estaban repartidas
entre workspaces de Codex, carpetas de staging y packs instalados localmente.

El repositorio contiene fuentes editables. No incluye JARs compilados, logs,
backups, instalaciones del juego, el Hytale Shared Source ni mundos completos.

## Contenido

### Mods Java

| Proyecto | Estado | Descripcion |
| --- | --- | --- |
| `mods/java/player-trigger-tags` | Activo, v1.5.6 | Tags de jugador, condiciones y efectos de Trigger Volumes, movimiento de entidades, colision para plataformas y conversion de bloques en entidades. |
| `mods/java/chest-labels` | Prototipo, v0.1.0 | Nombres e iconos persistentes para cofres, con HUD y editor. |
| `mods/java/scoreboards` | Legacy, Hytale 0.5 | Editor y seguimiento de scoreboards mediante UI nativa. Requiere migracion antes de usarlo en 0.6.x. |
| `mods/java/legacy/player-triggers` | Legacy | Seguimiento de jugadores, tags, contadores, temporizadores y HUD. |
| `mods/java/legacy/player-triggers-1.3.2-local` | Legacy/local | Snapshot encontrado en este PC con HUDs de player count/tag value y efecto de permisos. |
| `mods/java/legacy/more-triggers` | Legacy | Efectos `RemoveEventTitle`, `RandomTagSelection` y `PasteRandomPrefab`. |
| `mods/java/legacy/trigger-execute-command` | Legacy/local | Mod standalone `Trigger Execute Command` encontrado en este PC. |
| `mods/java/legacy/trigger-remove-event-title` | Legacy/local | Mod standalone `Trigger Remove Event Title` encontrado en este PC. |
| `mods/java/legacy/scoreboards-mod-local` | Legacy/local | Variante local de `Scoreboards Mod` encontrada en este PC; se conserva aparte de `mods/java/scoreboards`. |
| `mods/java/legacy/trigger-random-tag` | Legacy/incompleto | Predecesor de More Triggers; se conserva como referencia historica. |

`games/nexus-siege` contiene el proyecto del minijuego, sus NPCs vanilla, el
plugin `Triggers_NPCs`, documentacion, pruebas y contratos de comportamiento.

### Asset packs

`mods/asset-packs` contiene los packs independientes que siguen siendo utiles:

- `visible-barrier-block`
- `ghost-outline-blocks`
- `essence-buttons`
- `real-lever-animation`
- `raynor-npcs`
- `nexus-siege-props`

`experiments/asset-packs` conserva prototipos y material de investigacion:
flechas con `SpawnTriggerVolume`, outlines, ventanas, prefabs, archery game,
pruebas Raynor y packs usados durante las migraciones 0.6.x. Pueden contener
overrides vanilla y no deben instalarse todos a la vez sin revisar sus IDs.

## Preparar otro PC

Requisitos:

- Hytale pre-release instalada.
- JDK compatible con el servidor actual; los builds recientes usan Java 25.
- Git y PowerShell 7 o Windows PowerShell.
- Maven solo para proyectos que tengan `pom.xml`.

Clona también el Shared Source como dependencia de consulta, no dentro de este
repositorio:

```powershell
.\scripts\Sync-HytaleSharedSource.ps1
```

Por defecto se crea junto al monorepo, en `..\hytale-shared-source`, usando la
rama `pre-release` oficial.

El JAR del servidor suele estar en:

```text
%APPDATA%\Hytale\install\pre-release\package\game\latest\Server\HytaleServer.jar
```

## Compilar un mod Java

El script comun compila todos los `.java`, copia `manifest.json`, `Common` y
`Server`, y genera el JAR bajo `.build/dist`.

Ejemplo para Player Trigger Tags, cuyo package root es `src`:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\player-trigger-tags `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName Player_Trigger_Tags-1_5_6.jar
```

Ejemplo para Chest Labels:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\chest-labels `
  -SourceRoot src `
  -PackageRoot . `
  -ArtifactName ChestLabels-0_1_0.jar
```

Para Scoreboards, los assets estan en una raiz distinta:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\scoreboards `
  -SourceRoot src `
  -PackageRoot . `
  -AssetsRoot assets `
  -ArtifactName Scoreboards-1_0_0.jar
```

Los mods legacy pueden necesitar cambios de API antes de compilar contra una
pre-release moderna. Sus manifests y rangos de servidor documentan el punto de
partida historico.

## Instalar y probar

Los mods pueden instalarse globalmente en `...\pre-release\mods` o solo en una
partida bajo `...\Saves\<save>\mods`. No instales simultaneamente dos JARs con
el mismo `Group:Name`: Hytale rechazara la conexion por plugin duplicado.

Para `player-trigger-tags`, ejecuta siempre antes del despliegue:

```powershell
.\mods\java\player-trigger-tags\tools\Test-PluginLocalization.ps1
.\mods\java\player-trigger-tags\tools\Test-PluginDeployment.ps1 `
  -GameDataRoot "$env:APPDATA\Hytale\data\pre-release" `
  -SaveName "0.6.8" `
  -ArchivePath ".\.build\dist\Player_Trigger_Tags-1_5_6.jar"
```

La partida de prueba usada al crear este repo era `0.6.8`. El archivo instalado
se mantenia deliberadamente como `Player_Trigger_Tags-1_1_1.jar`; el nombre del
archivo no determina la version, el `manifest.json` interior si.

## Mundos y builds

Los mundos no se versionan aqui porque sus regiones binarias son grandes y no
generan diffs utiles. En el PC de origen siguen bajo
`%APPDATA%\Hytale\data\pre-release\Saves`. Para transportar un mapa, comprime
un snapshot sin `logs`, `backup` ni `mods`, y guardalo como release artifact o
en almacenamiento externo.

Los outputs `.build`, `build`, `dist`, `target`, JARs y ZIPs tambien se ignoran.
Las releases de GitHub son el lugar adecuado para publicar binarios verificables.

Consulta [AGENTS.md](AGENTS.md) antes de retomar cambios con otro agente.
