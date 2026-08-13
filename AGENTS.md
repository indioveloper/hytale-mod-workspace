# Contexto para agentes

Este repositorio es el punto de entrada canonico para el desarrollo Hytale de
OrbGenesis/Raynor. Lee primero `README.md` y despues el README o handoff del
proyecto concreto. No uses las antiguas carpetas de staging del PC como fuente
de verdad.

## Estado de referencia

- Fecha de la ultima validacion: 2026-08-11.
- Runtime de trabajo: Hytale pre-release `0.6.0-pre.11`, revision
  `00cf2e930ab404ea983cb709c3e0a6deb45fda7a`.
- Referencia de API: `HypixelStudios/hytale-shared-source`, rama `pre-release`.
- JDK usado en los builds recientes: Java 25.
- Save de smoke test en el PC de origen: `0.6.8`.
- El Shared Source, el servidor y los assets vanilla son dependencias externas y
  nunca deben copiarse al repo.

## Proyectos activos tras la consolidacion

- `mods/java/more-triggers`, version `1.9.1`: utilidades generales de Trigger
  Volumes. Integra `ExecuteCommand` y la regla `NoMove`; el antiguo mod
  standalone esta deprecated.
- `mods/java/entity-motion-triggers`, version `1.3.0`: crea, convierte y mueve
  entidades, gestiona colision de plataforma y ancla particulas moviles.
- `mods/java/particle-shape-vfx`, version `0.1.0`: registra
  `SpawnParticleShape` para cubos, superficies esfericas y lineas de
  particulas calculadas en coordenadas exactas.
- `mods/java/scoreboards`, version `2.0.10`: sistema independiente de
  Objectives y scoreboards.
- `mods/java/build-battle`, version `0.2.2`: logica independiente del modo de
  juego.
- `mods/java/configurable-mob-spawners`, version `0.3.0`: bloque solido minable
  con busqueda y preview NPC, vida/armadura, suelo automatico, loot por filas,
  importacion CMS1, modelo propio y VFX. Su tag persistente queda reservada
  para una futura integracion de senales con Trigger Volumes. Su artefacto hidrata
  el arbol `Common/UI/Custom` desde la `Assets.zip` usada para compilar, porque
  el cliente pre.11 pierde documentos vanilla al recibir un pack Custom UI
  parcial. Esos assets externos solo viven en `.build` y nunca se versionan.

`Player Entity Tags`, `Chest Labels` y el antiguo `Trigger Execute Command`
standalone se conservan bajo `mods/java/deprecated` y no deben distribuirse.

Reglas importantes:

- No cambies IDs internos de efectos, condiciones, reglas o componentes solo
  para renombrar su etiqueta; romperias Trigger Volumes o datos guardados.
- `ConvertBlocksToEntities` vive en Entity Motion Triggers. Los items con modelo
  usan `ModelComponent`; los items-bloque normales usan `BlockEntity`; otros
  items usan `ItemComponent`.
- La escala de `BlockEntity` debe ser `1.0` para conservar el tamano del bloque.
- La colision creada por la conversion se aplica dos ticks mas tarde mediante
  `PendingPlatformCollisionComponent/System`, porque el cliente debe recibir
  primero la geometria y el `NetworkId`.
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

Para validar todos los plugins Java activos contra el runtime local de
pre-release, ejecuta `scripts/Test-ActiveJavaMods.ps1`. La prueba de paquete de
More Triggers comprueba tambien que conserva el registro `ExecuteCommand`.

Los iconos del menu de mods no se declaran en `manifest.json`. El cliente
pre-release actual busca automaticamente `icon-256.png` en la raiz del JAR o
ZIP y exige que sea un PNG de 256x256 pixeles.

Durante la migracion, retira los JARs antiguos de Player Trigger Tags y Trigger
Execute Command antes de instalar los mods consolidados. No dejes dos plugins
que registren los mismos IDs.

## Otros proyectos

- `mods/java/build-battle`: prototipo `0.2.2`. `SuggestBuildTheme` abre una UI
  de una palabra y guarda en el propio volumen `theme_<palabra>=empty` y
  `points=0`. La regla `RestrictBuildBattleCreativeTools`, usada bajo
  `Always Active`, aplica una whitelist de Builder Tools por permisos e IDs de
  items y conserva el acceso a los Technical Block Sets. Desde 0.2.2 solo se
  registra como regla; el save `Trigger Volumes` se migro de `Effects` a
  `Rules`.
- `mods/java/entity-motion-triggers`: efectos de creacion, conversion y
  movimiento de entidades, colision de plataforma y particulas ancladas.
- `auxiliary/java/map-selector`: codigo auxiliar `0.1.1` creado para ayudar a
  otro desarrollador. Conserva `/mapas`, previews nativas de prefabs y destinos
  configurados en `MapDefinition`, pero no forma parte de las releases ni debe
  instalarse en los mapas de desarrollo de OrbGenesis.
- `auxiliary/java/dungeon-core-pre11`: port auxiliar de DungeonCore 1.3.7 a
  `0.6.0-pre.11`, publicado internamente como `1.3.8-pre11`. Mantiene
  `com.lol:DungeonCore` y todos sus IDs para poder abrir saves existentes.
  Requiere HyUI 0.9.8; la copia local `0.9.8-pre11` solo amplía el rango de
  compatibilidad del manifiesto después de comprobar que el plugin carga y se
  habilita. No forma parte de las releases propias de OrbGenesis.
- `mods/java/more-triggers`: version `1.9.1`. `GiveRandomItem` entrega al
  jugador activador un bloque, mueble, banco, arma o herramienta elegible y
  excluye plantas, assets internos y herramientas creativas. `SendTagMessage` y
  `ShowTagEventTitle` sustituyen `{tag}` desde una fuente elegible:
  `SELF`, `EVENT` o `RADIUS`; no cambian los IDs de los efectos vanilla.
  Tambien incluye el HUD circular `/timer` y el efecto `ControlTimer`; el
  contorno radial usa 61 frames PNG y su estado solo vive durante la sesion.
  Tambien registra `ExecuteCommand` con los IDs y campos del antiguo mod
  standalone. `NoMove` se registra como regla (no como efecto) para aparecer
  bajo `Always Active`; cancela la velocidad dentro del volumen y permite
  excluir jugadores y una lista de assets `NpcRole`.
- `experiments/java/inverted-gravity-camera`: conserva el codigo retirado de
  `SetPlayerGravityView`, `/gravityview` y la compensacion de movimiento de
  More Triggers 1.8.x. El handoff documenta los intentos y el bloqueo de control
  relativo a camara en Hytale 0.6.8. No forma parte del build activo.
- `mods/java/deprecated`: codigo historico no distribuible de Player Entity
  Tags, Chest Labels, Trigger Execute Command standalone y `RemoveEventTitle`.
  `RandomTagSelection` fue eliminado completamente y no debe reintroducirse.
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
- `mods/asset-packs/mechanisms`, version `1.1.1`: props accionables y la piedra
  `OrbGenesis_NoBuild_Stone`. Su volumen temporal usa `ModifyRules` con
  operacion `SET` en `VOLUME_CREATE` para insertar `NoBuild`, porque
  `SpawnTriggerVolume` 0.6.8 no copia directamente las reglas del asset. El
  volumen mide 10x10x10, retira `NoBuild` a los 10 segundos y usa
  `SpawnParticleShape` para mostrar fases roja y amarillo-naranja durante el
  bloqueo y una fase verde de desbloqueo en el segundo 10. Depende de Particle
  Shape VFX 0.1.0.
- `mods/asset-packs/roguelike-prefabs`: pack `tests:tests` con los mapas y
  prefabs usados por `test roguelike` y, de forma opcional, por el prototipo
  auxiliar Map Selector.
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
