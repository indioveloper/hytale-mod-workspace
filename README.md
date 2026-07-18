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
| `mods/java/player-trigger-tags` | Activo, v1.5.6 | Tags de jugador, condiciones y efectos de Trigger Volumes, y conversion de bloques en entidades. |
| `mods/java/entity-motion-triggers` | Activo/prototipo, v1.0.0 | Efectos de Trigger Volumes para mover entidades y aplicar/quitar colision de plataforma. |
| `mods/java/trigger-execute-command` | Actualizado, v1.1.0 | Efecto `ExecuteCommand` para lanzar comandos desde Trigger Volumes en pre-release 0.6.x. |
| `mods/java/chest-labels` | Prototipo, v0.1.0 | Nombres e iconos persistentes para cofres, con HUD y editor. |
| `mods/java/scoreboards` | Legacy unificado, Hytale 0.5 | Editor y seguimiento de scoreboards mediante UI nativa. Absorbe la variante local; requiere migracion antes de usarlo en 0.6.x. |

`games/nexus-siege` contiene el proyecto del minijuego, documentacion, pruebas
y contratos de comportamiento. Los NPCs reutilizables viven en
`mods/asset-packs/raynor-npcs`.

### Asset packs

`mods/asset-packs` contiene los packs independientes que siguen siendo utiles:

- `blocks`
- `mechanisms`
- `raynor-npcs`
- `nexus-siege-props`

`experiments/asset-packs` conserva prototipos y material de investigacion:
flechas con `SpawnTriggerVolume`, outlines, ventanas, prefabs, archery game,
pruebas Raynor y packs usados durante las migraciones 0.6.x. Pueden contener
overrides vanilla y no deben instalarse todos a la vez sin revisar sus IDs.
Consulta `docs/ASSET_PACKS.md` para el detalle de cada pack.

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

Ejemplo para Entity Motion Triggers:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\entity-motion-triggers `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName Entity_Motion_Triggers-1_0_0.jar
```

Ejemplo para Trigger Execute Command:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\trigger-execute-command `
  -SourceRoot src `
  -PackageRoot . `
  -ArtifactName Trigger_Execute_Command-1_1_0.jar
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
