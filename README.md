# Hytale Mod Workspace

Raynor Holograms se incluye en la suite publica experimental y sus scripts
de prueba/exportacion. La web ofrece release 0.8.3 y Update 7 pre.1
0.8.4-u7pre.1 por separado. No instalar las dos variantes simultaneamente.

Monorepo de desarrollo para los mods, asset packs y prototipos de Hytale de
OrbGenesis/Raynor. Reune las fuentes que hasta julio de 2026 estaban repartidas
entre workspaces de Codex, carpetas de staging y packs instalados localmente.

El repositorio contiene fuentes editables. No incluye JARs compilados, logs,
backups, instalaciones del juego, el Hytale Shared Source ni mundos completos.

## Contenido

### Mods Java

| Proyecto | Estado | Descripcion |
| --- | --- | --- |
| `mods/java/build-battle` | WIP jugable, v0.3.0 | Rondas solo/equipos, temas JSON, parcelas protegidas, creativo sin herramientas de mundo y votacion de construcciones. |
| `mods/java/configurable-mob-spawners` | WIP, v0.5.7 | Spawner compuesto con perfiles ponderados y variantes élite configurables, nameplate reforzado, avisos de aparición/proximidad, portada CMS1 guiada, preview NPC, vida, velocidad, escala física, equipo, loot, límite local de mobs vivos y oleadas controladas por Trigger Volumes mediante tags. |
| `mods/java/entity-motion-triggers` | Activo/prototipo, v1.3.3 | Efectos para crear, convertir y mover entidades, aplicar colision de plataforma y anclar particulas moviles. |
| `mods/java/more-triggers` | Activo/prototipo, v1.10.6 | Utilidades generales de Trigger Volumes, incluida la regla `NoMove`, `ExecuteCommand`, objetos aleatorios filtrados, prefabs aleatorios rotables con preview y reserva antisolapamiento, mensajes con tags, timer circular y senales repetitivas persistentes. |
| `mods/java/particle-shape-vfx` | Activo/prototipo, v0.1.2 | Efecto `SpawnParticleShape` para dibujar cubos, superficies esfericas y lineas de particulas con coordenadas exactas. |
| `mods/java/raynor-holograms` | MVP experimental, v0.8.3 | Hologramas persistentes de texto nativo o enriquecido, imagenes locales/remotas y GIF; una entidad por holograma, atlas compatible con Update 6, placeholders, billboard, UI validada y preparacion asincrona de GIF sin bloquear el mundo. |
| `mods/java/scoreboards` | Prototipo, v2.0.12 | Objectives editables y persistentes con comandos, UI nativa y control desde Trigger Volumes; compila en Update 6 y requiere smoke test en juego. |
| `mods/java/trigger-volume-effect-browser` | Prototipo experimental, v0.1.6 | Herramienta clon con creacion/seleccion vanilla y apertura mediante E, catalogo searchable, descripciones breves y filtros completos por categoria o mod; evita bindings a elementos inexistentes. |

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

Proyecto familiar independiente: `auxiliary/minecraft-bedrock/admin-menu` 1.2.0
es un add-on de Minecraft Bedrock con reloj, menus con iconos, efectos y
encantamientos. No participa en los builds de Hytale. Consulta su README.

`auxiliary/minecraft-bedrock/mob-buckets` 1.0.0 es otro add-on Bedrock
independiente: anade un cubo reutilizable para capturar y liberar criaturas
simples, con exclusiones de seguridad para mascotas, jefes y mobs con datos.

`auxiliary/minecraft-bedrock/diamond-walker` 1.0.2 anade el comando
`/function diamantes`, que activa por jugador la conversion en diamante del bloque
no vacio situado directamente bajo sus pies.

El [catalogo consolidado de efectos](docs/MORE_TRIGGERS_CONSOLIDATION.md)
detalla que registra cada mod despues de la reorganizacion.

`games/nexus-siege` contiene el proyecto del minijuego, documentacion, pruebas
y contratos de comportamiento. Los NPCs reutilizables viven en
`mods/asset-packs/raynor-npcs`.

`games/desert-survival` contiene la preproduccion del modo de supervivencia en
desierto: progresion de recursos, cutterrays, nodos minerales grandes y la
arquitectura prevista para supervivencia, bases y vehiculos. Su primer asset
jugable es un prototipo visual del cutterray industrial T3 para Hytale 0.7.x.

### Asset packs

`mods/asset-packs` contiene los packs independientes que siguen siendo utiles:

- `blocks`
- `metalwork-flat-roofs` (`RaynorMods:Metalwork Flat Roofs`, v0.1.3; copias Iron/Copper sin solapes, Void ornamentado y nueve
  tejados planos metalicos derivados de las texturas GPL de Metalwork)
- `mechanisms` (`OrbGenesis:OrbGenesis Mechanisms`, v1.1.2; incluye la piedra
  arrojadiza que crea una zona `NoBuild` temporal y requiere Particle Shape VFX)
- `raynor-npcs`
- `nexus-siege-props`
- `roguelike-prefabs` (`tests:tests`, usado por `test roguelike`)

`experiments/asset-packs` conserva prototipos y material de investigacion:
flechas con `SpawnTriggerVolume`, outlines, ventanas, prefabs, archery game,
pruebas Raynor y packs usados durante las migraciones 0.6.x. Pueden contener
overrides vanilla y no deben instalarse todos a la vez sin revisar sus IDs.
Consulta `docs/ASSET_PACKS.md` para el detalle de cada pack.

`experiments/asset-packs/worldgen-v2-lab` es el laboratorio reproducible para
WorldGen V2 de `0.7.0-pre.1`: incluye un mundo minimo de colinas, una instancia
efimera, plantilla de prompt, registro de iteraciones y pruebas/build propios.
No forma parte de las releases estables.

`experiments/java/inverted-gravity-camera` conserva el prototipo retirado de
camara/gravedad invertida y el handoff de sus problemas pendientes de control.

El resultado de la consolidacion de los mods Java de Trigger Volumes esta en
`docs/MORE_TRIGGERS_CONSOLIDATION.md`.

## Preparar otro PC

Requisitos:

- Hytale estable instalada (para estas versiones, `0.6.2` o posterior dentro de
  la linea `0.6.x`).
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

El JAR del servidor estable suele estar en:

```text
%APPDATA%\Hytale\install\release\package\game\latest\Server\HytaleServer.jar
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

La ultima validacion completa se hizo el 16 de septiembre de 2026 contra Hytale
estable `0.6.6`, revision `ad11b033782bd8f5e715d2e8497d491f5780640c`,
con Java 25. La matriz, los limites de esa validacion y los artefactos que se
pueden publicar estan en [Compatibilidad con Update 6](docs/UPDATE_6_COMPATIBILITY.md).

Ejemplo para More Triggers:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\more-triggers `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName More_Triggers-1_10_6.jar
```

Ejemplo para Entity Motion Triggers:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\entity-motion-triggers `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName Entity_Motion_Triggers-1_3_3.jar
```

Para Scoreboards, los assets estan en una raiz distinta:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\scoreboards `
  -SourceRoot src `
  -PackageRoot . `
  -AssetsRoot assets `
  -ArtifactName Scoreboards-2_0_12.jar
```

Los asset packs puros se empaquetan con rutas ZIP portables mediante:

```powershell
.\scripts\Build-AssetPack.ps1 `
  -ProjectPath .\mods\asset-packs\mechanisms `
  -ArtifactName OrbGenesis_Mechanisms-1_1_2.zip
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

## Revalidacion de mods publicos - 2026-09-03

Los cinco mods de `website/raynor-mods/portal-config.js` se han recompilado
contra release `0.6.3`. `scripts/Test-ActiveJavaMods.ps1 -PublishedOnly`
excluye Build Battle y Raynor NPCs; pasa el JAR estable tambien a los tests.
El build comun usa release por defecto; pre-release requiere `-ServerJar`.
Ver [informe y limites](docs/UPDATE_6_3_PUBLIC_MODS.md). La web y CurseForge
siguen con las versiones anteriores hasta publicar estos artefactos.

El 10 de septiembre de 2026, la misma matriz publicada, incluido Raynor
Holograms 0.8.3, volvio a pasar contra Hytale estable `0.6.4`, revision
`108a26d534805e68dcf0d00638914cb0db82555b`. Esta comprobacion no cambia las
versiones publicadas ni sustituye el smoke test dentro del juego.
