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
| `mods/java/build-battle` | Prototipo, v0.2.3 | Sugerencias de temas y regla Always Active para restringir herramientas creativas por plot. |
| `mods/java/configurable-mob-spawners` | MVP ampliado, v0.5.1 | Spawner compuesto con perfiles ponderados y variantes élite configurables, nombre reforzado, tinte ModelVFX, aviso por proximidad, portada CMS1 guiada, preview NPC, vida, velocidad, escala física, equipo, loot y límite local de mobs vivos. |
| `mods/java/entity-motion-triggers` | Activo/prototipo, v1.3.1 | Efectos para crear, convertir y mover entidades, aplicar colision de plataforma y anclar particulas moviles. |
| `mods/java/more-triggers` | Activo/prototipo, v1.10.1 | Utilidades generales de Trigger Volumes, incluida la regla `NoMove`, `ExecuteCommand`, objetos aleatorios filtrados, prefabs aleatorios rotables, mensajes con tags, timer circular y senales repetitivas persistentes. |
| `mods/java/particle-shape-vfx` | Activo/prototipo, v0.1.0 | Efecto `SpawnParticleShape` para dibujar cubos, superficies esfericas y lineas de particulas con coordenadas exactas. |
| `mods/java/scoreboards` | Prototipo, v2.0.10 | Objectives editables y persistentes con comandos, UI nativa y control desde Trigger Volumes; compila en pre.12 y requiere smoke test en juego. |

`mods/java/deprecated` conserva Player Entity Tags, Chest Labels y el antiguo
Trigger Execute Command standalone. Son fuentes historicas desactivadas por
defecto y no deben incluirse en la distribucion activa.

### Codigo auxiliar

`auxiliary/java/map-selector` conserva Map Selector 0.1.1, creado como ayuda
para el proyecto de otro desarrollador. No forma parte de los mods activos, no
se incluye en las releases de OrbGenesis y no debe instalarse en los mapas de
desarrollo.

`auxiliary/java/dungeon-core-pre11` conserva el port auxiliar de DungeonCore
1.3.7 a Hytale `0.6.0-pre.11`. Mantiene los IDs originales y requiere HyUI
0.9.8; no forma parte de los siete plugins Java activos del workspace.

El [catalogo consolidado de efectos](docs/MORE_TRIGGERS_CONSOLIDATION.md)
detalla que registra cada mod despues de la reorganizacion.

`games/nexus-siege` contiene el proyecto del minijuego, documentacion, pruebas
y contratos de comportamiento. Los NPCs reutilizables viven en
`mods/asset-packs/raynor-npcs`.

### Asset packs

`mods/asset-packs` contiene los packs independientes que siguen siendo utiles:

- `blocks`
- `mechanisms` (`OrbGenesis:OrbGenesis Mechanisms`, v1.1.1; incluye la piedra
  arrojadiza que crea una zona `NoBuild` temporal y requiere Particle Shape VFX)
- `raynor-npcs`
- `nexus-siege-props`
- `roguelike-prefabs` (`tests:tests`, usado por `test roguelike`)

`experiments/asset-packs` conserva prototipos y material de investigacion:
flechas con `SpawnTriggerVolume`, outlines, ventanas, prefabs, archery game,
pruebas Raynor y packs usados durante las migraciones 0.6.x. Pueden contener
overrides vanilla y no deben instalarse todos a la vez sin revisar sus IDs.
Consulta `docs/ASSET_PACKS.md` para el detalle de cada pack.

`experiments/java/inverted-gravity-camera` conserva el prototipo retirado de
camara/gravedad invertida y el handoff de sus problemas pendientes de control.

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

El script comun compila todos los `.java`, copia `manifest.json`, el icono
opcional `icon-256.png`, `Common` y `Server`, y genera el JAR bajo `.build/dist`.
El cliente de Hytale exige ese nombre exacto y una imagen PNG de 256x256; el
icono no se declara en el manifest.

La matriz completa de compatibilidad para los siete plugins Java activos se
ejecuta con:

```powershell
.\scripts\Test-ActiveJavaMods.ps1
```

La ultima validacion completa se hizo el 15 de agosto de 2026 contra
`0.6.0-pre.12.2` (`58a14e1362808d3b1bcffc0a02a1b5b9f8bfdcb2`) y Java 25.
El runtime local actual es `0.6.0-pre.13.1`
(`f0a85f20ac60b34232fa6b42d3585850bd959dde`); More Triggers 1.10.1 ya se
compila y valida contra esta version.

Ejemplo para More Triggers:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\more-triggers `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName More_Triggers-1_10_1.jar
```

Ejemplo para Entity Motion Triggers:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\entity-motion-triggers `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName Entity_Motion_Triggers-1_3_1.jar
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

Los asset packs puros se empaquetan con rutas ZIP portables mediante:

```powershell
.\scripts\Build-AssetPack.ps1 `
  -ProjectPath .\mods\asset-packs\mechanisms `
  -ArtifactName OrbGenesis_Mechanisms-1_1_1.zip
```

## Instalar y probar

Los mods pueden instalarse globalmente en `...\pre-release\mods` o solo en una
partida bajo `...\Saves\<save>\mods`. No instales simultaneamente dos JARs con
el mismo `Group:Name`: Hytale rechazara la conexion por plugin duplicado.

Para More Triggers, ejecuta siempre antes del despliegue:

```powershell
.\mods\java\more-triggers\tools\Test-PluginLocalization.ps1
.\mods\java\more-triggers\tools\Test-TagTemplateResolver.ps1
.\mods\java\more-triggers\tools\Test-NoMoveExceptionFilter.ps1
.\mods\java\more-triggers\tools\Test-RandomItemCandidateFilter.ps1
.\mods\java\more-triggers\tools\Test-TimerMath.ps1
.\mods\java\more-triggers\tools\Test-SignalLoopSchedule.ps1
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
