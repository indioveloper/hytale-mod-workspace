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
| `mods/java/build-battle` | Prototipo, v0.2.2 | Sugerencias de temas y regla Always Active para restringir herramientas creativas por plot. |
| `mods/java/entity-motion-triggers` | Activo/prototipo, v1.3.0 | Efectos para crear, convertir y mover entidades, aplicar colision de plataforma y anclar particulas moviles. |
| `mods/java/map-selector` | Prototipo, v0.1.1 | Selector cerrado de mapas con previews 3D de prefabs y teletransporte mediante `/mapas`. |
| `mods/java/more-triggers` | Activo/prototipo, v1.6.0 | Utilidades generales de Trigger Volumes, incluido `ExecuteCommand`, objetos aleatorios, mensajes con tags y timer circular. |
| `mods/java/scoreboards` | Prototipo, v2.0.10 | Objectives editables y persistentes con comandos, UI nativa y control desde Trigger Volumes; compila en 0.6.8 y requiere smoke test en juego. |

`mods/java/deprecated` conserva Player Entity Tags, Chest Labels y el antiguo
Trigger Execute Command standalone. Son fuentes historicas desactivadas por
defecto y no deben incluirse en la distribucion activa.

El [catalogo consolidado de efectos](docs/MORE_TRIGGERS_CONSOLIDATION.md)
detalla que registra cada mod despues de la reorganizacion.

`games/nexus-siege` contiene el proyecto del minijuego, documentacion, pruebas
y contratos de comportamiento. Los NPCs reutilizables viven en
`mods/asset-packs/raynor-npcs`.

### Asset packs

`mods/asset-packs` contiene los packs independientes que siguen siendo utiles:

- `blocks`
- `mechanisms` (`OrbGenesis:OrbGenesis Mechanisms`, v1.0.5; incluye la piedra
  arrojadiza que crea una zona `NoBuild` temporal con perimetro de particulas)
- `raynor-npcs`
- `nexus-siege-props`
- `roguelike-prefabs` (`tests:tests`, usado por `test roguelike`)

`experiments/asset-packs` conserva prototipos y material de investigacion:
flechas con `SpawnTriggerVolume`, outlines, ventanas, prefabs, archery game,
pruebas Raynor y packs usados durante las migraciones 0.6.x. Pueden contener
overrides vanilla y no deben instalarse todos a la vez sin revisar sus IDs.
Consulta `docs/ASSET_PACKS.md` para el detalle de cada pack.

El resultado de la consolidacion de los mods Java de Trigger Volumes esta en
`docs/MORE_TRIGGERS_CONSOLIDATION.md`.

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

Ejemplo para More Triggers:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\more-triggers `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName More_Triggers-1_6_0.jar
```

Ejemplo para Entity Motion Triggers:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\entity-motion-triggers `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName Entity_Motion_Triggers-1_3_0.jar
```

Para Scoreboards, los assets estan en una raiz distinta:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\scoreboards `
  -SourceRoot src `
  -PackageRoot . `
  -AssetsRoot assets `
  -ArtifactName Scoreboards-2_0_10.jar
```

## Instalar y probar

Los mods pueden instalarse globalmente en `...\pre-release\mods` o solo en una
partida bajo `...\Saves\<save>\mods`. No instales simultaneamente dos JARs con
el mismo `Group:Name`: Hytale rechazara la conexion por plugin duplicado.

Para More Triggers, ejecuta siempre antes del despliegue:

```powershell
.\mods\java\more-triggers\tools\Test-PluginLocalization.ps1
.\mods\java\more-triggers\tools\Test-TagTemplateResolver.ps1
.\mods\java\more-triggers\tools\Test-TimerMath.ps1
```

Antes de migrar un save, retira los JARs standalone de Player Trigger Tags y
Trigger Execute Command para evitar registros duplicados. El nombre del archivo
no determina la version; la determina el `manifest.json` interior.

## Mundos y builds

Los mundos no se guardan directamente en el historial Git porque sus regiones
binarias son grandes y no generan diffs utiles. Los snapshots restaurables se
documentan bajo `snapshots/` y sus ZIPs se publican como GitHub Release
artifacts, sin `logs`, `backup`, `telemetry` ni `mods`.

Los outputs `.build`, `build`, `dist`, `target`, JARs y ZIPs tambien se ignoran.
Las releases de GitHub son el lugar adecuado para publicar binarios verificables.

Consulta [AGENTS.md](AGENTS.md) antes de retomar cambios con otro agente.

## Guias

- [Guia de Trigger Volumes](docs/trigger-volumes/index.html): introduccion para
  compartir con el equipo que son los TV y como pensarlos.
