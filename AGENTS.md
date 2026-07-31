# Contexto para agentes

Este repositorio es el punto de entrada canonico para el desarrollo Hytale de
OrbGenesis/Raynor. Lee primero `README.md` y despues el README o handoff del
proyecto concreto. No uses las antiguas carpetas de staging del PC como fuente
de verdad.

## Estado de referencia

- Fecha del handoff: 2026-07-18.
- Runtime de trabajo: Hytale pre-release 0.6.8.
- Referencia de API: `HypixelStudios/hytale-shared-source`, rama `pre-release`.
- JDK usado en los builds recientes: Java 25.
- Save de smoke test en el PC de origen: `0.6.8`.
- El Shared Source, el servidor y los assets vanilla son dependencias externas y
  nunca deben copiarse al repo.

## Proyecto activo: Player Trigger Tags

Ruta: `mods/java/player-trigger-tags`.

Manifest actual: `OrbGenesis:Player Trigger Tags`, version `1.5.6`.

Funciones principales:

- Tags string persistentes en jugadores.
- Efecto `ModifyPlayerTag` y condicion `PlayerTagCondition`.
- `ConvertBlocksToEntities` convierte bloques dentro del Trigger Volume usando
  un item elegido. Los items con modelo usan `ModelComponent`; los items-bloque
  normales usan `BlockEntity`; otros items usan `ItemComponent`.
- La escala de `BlockEntity` debe ser `1.0` para conservar el tamano del bloque.
- La colision creada por la conversion se aplica dos ticks mas tarde mediante
  `PendingPlatformCollisionComponent/System`, porque el cliente debe recibir
  primero la geometria y el `NetworkId`.

La correccion de colision de v1.5.6 compila y esta desplegada, pero al crear este
handoff aun necesita confirmacion manual dentro del juego.

Los efectos de movimiento y colision de plataformas se han separado a
`mods/java/entity-motion-triggers`.

Reglas importantes de este mod:

- No cambies IDs internos de efectos existentes solo para renombrar su etiqueta;
  romperias Trigger Volumes guardados.
- El selector de items debe usar la fuente vanilla `Item`; filtrar por
  `Item.getModel()` deja fuera los items-bloque.

## Localizacion de Trigger Volumes

Los archivos `Server/Languages/*/server.lang` reciben automaticamente el
namespace `server.`. Por tanto, las claves deben comenzar por `customUI...`, no
por `server.customUI...`.

Cada campo nuevo debe tener como minimo:

- etiqueta en `en-US` y `es-ES`;
- tooltip util con significado, unidades y ejemplo cuando corresponda;
- placeholder para entradas de texto o numericas cuando ayude;
- etiquetas para todas las opciones de enums.

Ejecuta `tools/Test-PluginLocalization.ps1` antes de empaquetar.

## Compilacion y despliegue

Compila siempre contra el `HytaleServer.jar` de la version que se va a probar,
no contra clases copiadas ni contra un JAR antiguo. Usa `scripts/Build-JavaMod.ps1`
o reproduce sus pasos con `javac` y `jar`.

Antes de sustituir un mod instalado:

1. Comprueba que la compilacion termina sin errores. Los warnings de APIs
   deprecated deben registrarse, pero no confundirse con errores.
2. Ejecuta las pruebas disponibles en el proyecto.
3. Comprueba que no existe otra instalacion con el mismo `Group:Name` en mods
   globales y mods del save.
4. Copia el JAR y compara SHA-256 entre build e instalacion.
5. Reinicia o recarga la partida; sobrescribir el archivo no recarga las clases
   de un plugin ya iniciado.

En el PC original, Player Trigger Tags se desplegaba sobre:

```text
%APPDATA%\Hytale\data\pre-release\Saves\0.6.8\mods\Player_Trigger_Tags-1_1_1.jar
```

Se conserva ese nombre para evitar dejar un segundo JAR. No crees otro archivo
con el numero de version nuevo en la misma carpeta.

## Otros proyectos

- `mods/java/build-battle`: prototipo `0.2.2`. `SuggestBuildTheme` abre una UI
  de una palabra y guarda en el propio volumen `theme_<palabra>=empty` y
  `points=0`. La regla `RestrictBuildBattleCreativeTools`, usada bajo
  `Always Active`, aplica una whitelist de Builder Tools por permisos e IDs de
  items y conserva el acceso a los Technical Block Sets. Desde 0.2.2 solo se
  registra como regla; el save `Trigger Volumes` se migro de `Effects` a
  `Rules`.
- `mods/java/chest-labels`: mod funcional en fase de prototipo; conserva datos
  en componentes de bloques y usa UI custom.
- `mods/java/entity-motion-triggers`: v1.2.3. Efectos de movimiento de
  entidades, particulas ancladas y colision de plataforma extraidos de Player
  Trigger Tags. `AttachMovingParticles` usa particulas de modelo ligadas al
  `NetworkId`; `DetachEmittedParticles` debe quedar desactivado para que el
  sistema siga la entidad. Sus offsets de rotacion se envian en grados; no
  comparten la convencion en radianes de los paquetes de particulas de mundo.
  La clave legacy `OnlyFirstMatch` selecciona la entidad mas cercana al centro,
  no la primera devuelta por el indice espacial.
- `mods/java/map-selector`: prototipo `0.1.1` con comando `/mapas`, previews
  nativas de prefabs y destinos configurados en `MapDefinition`. Sus mapas
  iniciales dependen opcionalmente del asset pack local `tests:tests`.
- `mods/java/more-triggers`: version `1.4.0`. `GiveRandomItem` entrega al
  jugador activador un asset `Item` elegido uniformemente al azar. `SendTagMessage` y
  `ShowTagEventTitle` sustituyen `{tag}` desde una fuente elegible:
  `SELF`, `EVENT` o `RADIUS`; no cambian los IDs de los efectos vanilla.
  Tambien incluye el HUD circular `/timer` y el efecto `ControlTimer`; el
  contorno radial usa 61 frames PNG y su estado solo vive durante la sesion.
- `mods/java/trigger-execute-command`: efecto `ExecuteCommand` actualizado a
  rango 0.6.x; revisar en juego antes de usar comandos destructivos.
- `games/nexus-siege`: workspace del minijuego. Los NPCs reutilizables y
  efectos de Trigger Volumes para NPCs viven en `mods/asset-packs/raynor-npcs`;
  la carpeta `npcs/vanilla` conserva contratos y notas de diseno del juego.
- `mods/java/scoreboards`: prototipo `2.0.10` reconstruido para 0.6.8. Usa
  `ObjectiveDataStore`, assets dinamicos y la tarea `OrbGenesisManualCount`;
  permite editar definiciones mediante `/scoreboard`, controlar instancias con
  comandos y usar `ControlScoreboard`, `ModifyScoreboardTask`,
  `ScoreboardState` y `ScoreboardTaskValue` en Trigger Volumes. Compila y pasa
  las pruebas estaticas, pero aun requiere smoke test dentro del juego. Las
  mutaciones del asset store se encolan fuera del hilo del mundo para no
  bloquear el `ASSET_LOCK` durante eventos de UI. Los titulos y descripciones
  editables se publican al cliente como traducciones dinamicas antes de enviar
  los paquetes nativos de Objective.
- `mods/asset-packs`: packs independientes relativamente limpios.
- `mods/asset-packs/roguelike-prefabs`: pack `tests:tests` con los mapas y
  prefabs usados por `test roguelike` y por Map Selector.
- `experiments/asset-packs`: material de laboratorio. Puede sobreescribir IDs
  vanilla y combinar varias pruebas incompatibles; inspeccionar antes de usar.

## Higiene del repositorio

- No versionar JARs, ZIPs, clases, outputs, logs, backups o saves directamente
  en Git. Publicar los snapshots de saves y binarios como GitHub Release
  artifacts y documentarlos bajo `snapshots/`.
- No versionar `HytaleServer.jar`, assets extraidos ni el Shared Source.
- No borrar fuentes legacy solo porque esten duplicadas; primero confirma que la
  version activa contiene todas sus funciones.
- Mantener ASCII en codigo y scripts salvo que el archivo ya requiera Unicode.
- Actualizar este archivo y el README del proyecto cuando cambien IDs, version,
  procedimiento de build, ruta de despliegue o limitaciones conocidas.
- Tras crear una version de mod, comprobar especialmente plugin duplicado,
  traducciones sin resolver y que el selector de assets devuelve resultados.
