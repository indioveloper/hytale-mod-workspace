# Contexto para agentes

Actualizacion 2026-09-04: Raynor Holograms forma parte de la suite publica
(experimental). Release 0.8.3; rama codex/update7-pre-release: 0.8.4-u7pre.1,
ServerVersion =0.7.0-pre.1. Matrices y exportadores incluyen seis mods.
Web con dos descargas Holograms separadas; no instalar ambas juntas.

Este repositorio es el punto de entrada canonico para el desarrollo Hytale de
OrbGenesis/Raynor. Lee primero `README.md` y despues el README o handoff del
proyecto concreto. No uses las antiguas carpetas de staging del PC como fuente
de verdad.

## Estado de referencia

- Fecha de la ultima validacion: 2026-09-16.
- Runtime de trabajo y publicacion: Hytale estable `0.6.6` (Update 6), revision
  `ad11b033782bd8f5e715d2e8497d491f5780640c`.
- Referencia de API: `HypixelStudios/hytale-shared-source`, rama `pre-release`,
  commit local validado `634880ce888e66922881597667fb87fbc0851e12`.
- JDK usado en los builds recientes: Java 25.
- Save de smoke test en el PC de origen: `0.6.8`.
- El Shared Source, el servidor y los assets vanilla son dependencias externas y
  nunca deben copiarse al repo.

## Proyectos activos tras la consolidacion

- `mods/java/more-triggers`, version `1.10.6`: utilidades generales de Trigger
  Volumes. Integra `ExecuteCommand`, la regla `NoMove` y bucles de senales
  persistentes; el antiguo mod standalone esta deprecated.
- `mods/java/entity-motion-triggers`, version `1.3.3`: crea, convierte y mueve
  entidades, gestiona colision de plataforma y ancla particulas moviles.
- `mods/java/particle-shape-vfx`, version `0.1.2`: registra
  `SpawnParticleShape` para cubos, superficies esfericas y lineas de
  particulas calculadas en coordenadas exactas.
- `mods/java/raynor-holograms`, version `0.8.3` (MVP experimental): sistema
  clean-room de hologramas persistentes de texto nativo o enriquecido, imagenes
  locales/remotas y GIF. Cada holograma completo usa una unica entidad de red y
  sigue siendo seleccionable, movible, rotatable, escalable y clonable con la
  Entity Tool. Admite placeholders, alineacion y billboard fijo, horizontal o
  completo. Update 6 requiere atlas estaticos preparados al entrar o solicitar
  expresamente la reconstruccion del Common asset tras publicar el atlas de
  medios generado. La 0.8.1 evita documentos UI obsoletos usando
  `HologramEditorV081.ui`; la 0.8.2 corrige la desconexion restante haciendo que
  `#NativePreview.Text` reciba un `String` en vez de un `Message`, como exige
  Update 6. La prueba de paquete verifica selectores y este contrato de tipo.
  La 0.8.3 crea el ancla de imagen inmediatamente y prepara/recarga los assets
  de imagen o GIF en `HytaleServer.SCHEDULED_EXECUTOR`; el sistema del mundo
  activa la particula solo al completar esa fase, evitando bloquear el tick.
  `Samples/prueba.gif` debe incluirse en el JAR. Los detalles y limites viven en
  `mods/java/raynor-holograms/DEVELOPMENT_LEARNINGS.md`.
- `mods/java/scoreboards`, version `2.0.12`: sistema independiente de
  Objectives y scoreboards.
- `mods/java/trigger-volume-effect-browser`, version `0.1.6` (experimental):
  herramienta clon de Trigger Volumes con catalogo de efectos, condiciones y
  reglas. Conserva el `BuilderTool.Id=TriggerVolume`; como el cliente asigna E
  directamente a `/triggervolume effects`, sustituye de forma reversible ese
  subcomando y solo abre el inspector ampliado cuando el item clon esta en la
  mano. La herramienta vanilla mantiene su interfaz vanilla.
- `mods/java/build-battle`, version `0.3.0` (WIP jugable): rondas individuales
  o por equipos, temas JSON, parcelas protegidas, creativo sin Builder Tools,
  votacion de tema/construcciones y reinicio opcional desde prefab base.
- `mods/java/configurable-mob-spawners`, version `0.5.7` (WIP): bloque solido minable
  visible como `Spawner Block` en el creativo normal. Abre primero una portada
  CMS1 ligera con instrucciones, `Guardar y cerrar` y cancelacion sin cambios.
  Un bloque recien colocado no tiene rol y permanece apagado hasta guardar o
  importar una configuracion valida; los bloques existentes conservan su rol.
  La exportacion solo aparece en el editor completo, que carga el catalogo NPC
  y la preview 3D bajo demanda; conserva vida/armadura, suelo automatico, loot por filas,
  importacion CMS1, velocidad relativa y escala nativa de modelo, hitboxes y
  controladores de movimiento, modelo propio y VFX. Los elites refuerzan el
  nameplate y los avisos, pero no aplican actualmente el ModelVFX violeta; sus
  assets y el contexto para retomarlo viven en `ELITE_VFX_DEFERRED.md`. Sus tags persistentes identifican
  el bloque para `TriggerSpawnerWave` desde Trigger Volumes. No expone un
  interruptor manual: genera al cumplirse proximidad, luz y cadencia. Con radio
  de activacion `0`, desactiva la proximidad y solo responde a TriggerSpawnerWave;
  entonces el limite de mobs vivos cuenta todos los generados por ese bloque.
  Con radio mayor que cero, el limite cuenta solo los generados por ese bloque que
  permanezcan dentro de su radio de activacion. Su artefacto hidrata
  el arbol `Common/UI/Custom` desde la `Assets.zip` usada para compilar, porque
  el cliente pre.11 pierde documentos vanilla al recibir un pack Custom UI
  parcial. Esos assets externos solo viven en `.build` y nunca se versionan.
  Los elites usan un ModelVFX violeta oscuro propio, refuerzan su nameplate
  despues del comportamiento NPC y muestran un Event Title al entrar a 10
  bloques, con cooldown por jugador.
  CMS1 admite hasta 12 perfiles ponderados por spawner. Cada perfil puede tener
  una variante elite con probabilidad, multiplicadores, equipo y loot adicional;
  el editor completo multiperfil vive en la web y el editor del juego conserva
  los perfiles importados aunque solo exponga los campos basicos de Mob 1.
  Al entrar, cada jugador recibe en el chat la guia de instalacion y el enlace
  clicable al configurador. El boton `Copiar URL` prepara la URL en el chat para
  copiarla, ya que una Custom UI de servidor no accede al portapapeles cliente.
  `TriggerSpawnerWave` registra un efecto de Trigger Volumes que busca tags
  exactas entre spawners cargados. `SIGNAL_ONLY` permite raids bajo Tag Added o
  Signal Received sin cadencia ni proximidad, manteniendo luz, suelo y máximo
  de vivos. En `0.6.0-pre.13.1` y estable `0.6.2`, la consulta de luz usa los datos globales de la
  seccion del bloque porque `BlockChunk.getBlockLightIntensity` y `getSkyLight`
  fueron eliminados.

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

Para validar todos los plugins Java activos contra el runtime estable local,
ejecuta `scripts/Test-ActiveJavaMods.ps1`. Los manifests publicables usan
`>=0.6.2 <0.7.0`. La prueba de paquete de
More Triggers comprueba tambien que conserva el registro `ExecuteCommand`.

Los iconos del menu de mods no se declaran en `manifest.json`. El cliente
pre-release actual busca automaticamente `icon-256.png` en la raiz del JAR o
ZIP y exige que sea un PNG de 256x256 pixeles.

Durante la migracion, retira los JARs antiguos de Player Trigger Tags y Trigger
Execute Command antes de instalar los mods consolidados. No dejes dos plugins
que registren los mismos IDs.

## Otros proyectos

- `mods/java/build-battle`: WIP jugable `0.3.0`. `/buildbattle` gestiona lobby,
  rondas solo/equipos, votacion de temas JSON, construccion dentro de parcelas,
  visita/votacion de builds y resultados. Durante la construccion bloquea todas
  las Builder Tools; cada parcela puede restaurarse desde un prefab base que
  incluya el aire necesario para limpiar la ronda anterior. Conserva los IDs
  heredados `SuggestBuildTheme` y `RestrictBuildBattleCreativeTools`; el
  primero sigue guardando `theme_<palabra>=empty` y `points=0` en el volumen.
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
- `mods/java/more-triggers`: version `1.10.6`. `PasteRandomPrefab` permite yaw
  cardinal de 0, 90, 180 o 270 grados sin cambiar su ID ni los saves antiguos.
  Hereda de `PastePrefabEffect` y expone al inspector sus getters para
  reutilizar el boton vanilla Show/Hide Preview; previsualiza el primer prefab
  configurado que pueda resolverse. El inspector pre.13.1 coloca la preview,
  pero no renderiza su `Rotation`; el yaw si se aplica al pegado real. Desde
  1.10.3 pre-rota una copia completa antes de pegar para evitar el bug vanilla
  que desplaza entidades (incluidos Trigger Volumes) una celda al girar 90 o
  270 grados y omite la composicion de su orientacion. Desde 1.10.4 puede
  reservar la huella tridimensional ya rotada antes del pegado y evitar
  solapamientos entre salas del mismo `OccupancyGroup`; las reservas se guardan
  como Trigger Volumes tecnicos desactivados con la tag `procedural_room`.
  `GiveRandomItem` entrega al
  jugador activador un bloque, mueble, banco, arma o herramienta elegible y
  excluye plantas, assets internos y herramientas creativas. `SendTagMessage` y
  `ShowTagEventTitle` sustituyen `{tag}` desde una fuente elegible:
  `SELF`, `EVENT` o `RADIUS`; no cambian los IDs de los efectos vanilla.
  Tambien incluye el HUD circular `/timer` y el efecto `ControlTimer`; el
  contorno radial usa 61 frames PNG y su estado solo vive durante la sesion.
  `ControlSignalLoop` inicia y controla senales `SignalReceived` repetitivas
  por mundo, con intervalo, limites, condicion por tag del volumen origen y
  acciones `START`, `STOP`, `PAUSE`, `RESUME` y `PULSE_NOW`. El bucle persiste
  al salir el actor del volumen, pero no tras reiniciar el servidor.
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
- `games/desert-survival`: prototipos aislados del modo de supervivencia.
  Continuacion del 2026-11-12 (noche) y punto de entrada para retomar:
  `DESERT_SURVIVAL_CONTINUATION_2026-11-12.md` en la raiz.
  `research-tree` 0.1.5 tiene 48 nodos, Intel persistente y un borrador de UI
  radial con cinco sectores, tres anillos y detalle a la derecha. Investigar
  depende de Intel y prerrequisitos, no del material alcanzado. El ultimo par
  que entro correctamente en `dune1` fue Research 0.1.1 + Vitals 0.2.2 y sigue
  instalado; 0.1.2-0.1.4 junto con Vitals 0.2.3-0.2.6 provocaron desconexion
  por `Hud/ReturnToHubButton.ui` del CreativeHub vanilla. El nuevo 0.1.5
  compila y pasa tests de paquete/catalogo contra el servidor 0.7.7 instalado.
  El intento con 0.1.5 en `dune1` tambien desconecto al cliente por
  `Hud/ReturnToHubButton.ui`; se retiro y quedo archivado como `.jar.failed-ui`.
  El Research global 0.1.1 volvio a estar activo y Vitals 0.2.2 sigue activo.
  La UI radial queda en fuentes, sin despliegue. No declarar resuelto el fallo
  de entrada sin una prueba posterior a la restauracion.
  Diagnostico 2026-11-12 (noche): el log `21-04-21` confirma que la
  restauracion 0.1.1 + Vitals 0.2.2 entra en `dune1`. Research 0.1.6 retiro
  el PNG duplicado `IntelIcon.png` y siguio fallando, asi que el contenido
  duplicado no es la causa. El unico fichero comun a todas las builds que
  fallaron (0.1.2-0.1.6 y Vitals 0.2.5-0.2.6) es `ResearchNodeRow.ui` en su
  revision 0.1.2, que usa `HorizontalAlignment: Right`; vanilla solo usa
  `Start`, `Center` y `End`. Research 0.1.7 lo saca del paquete (queda en
  `legacy/`), usa `$C.@CircularProgressBar` para los anillos y su
  `Test-Package.ps1` rechaza enums ajenos a vanilla, ficheros no `.ui` bajo
  `Common/UI/Custom` y assets Common duplicados. Smoke test 2026-11-12 22:22: 0.1.7 + Vitals 0.2.2
  entra en `dune1`; es la combinacion activa en `UserData/Mods` (0.1.1 y 0.1.6
  archivados). Regla confirmada: un solo documento Custom UI con un valor que
  el cliente no reconoce anula todo el conjunto de documentos, incluido el
  vanilla, y el error visible es el primer `Append` vanilla. Con 0.1.7 `/research` desconecto por el selector
  `#RingNodes[0] #RingNode`: el raiz de un documento anadido se direcciona
  como `#Lista[i]`, sin repetir su id. Research 0.1.8 lo corrige: `/research`
  abre y es usable. 0.2.0 pasa a una rueda unica: imagen
  `ResearchWheel.png` generada por `tools/Generate-ResearchWheel.ps1` (sin
  rotacion en el motor UI), cinco cunas de 72 grados por rama, tres bandas
  por tier y los 48 nodos a la vez colocados por `ringAnchor`; instalado,
  revision visual pendiente.
  Research 0.2.1 en fuentes: Intel insertado directamente en inventario se
  consume y suma al saldo persistente; la rueda usa nodos cuadrados con imagen
  placeholder. Oculta Intel historico y tier de material. Compilado y validado
  contra 0.7.7, instalado en `UserData/Mods` con SHA-256 igual al build;
  pendiente de smoke test del usuario. 0.2.0 se archivo como `.jar.replaced`.
  Research 0.2.2 en fuentes: tres estados visuales de nodo (azul disponible,
  gris bloqueado, verde investigado), 37 enlaces de prerrequisitos dibujados
  desde `ResearchCatalog`, coste como unico metadato en el detalle, correccion
  de Combate y tildes, sin leyenda de tiers ni confirmacion de seleccion.
  Compila y pasa tests contra 0.7.7; instalado en `UserData/Mods`, pendiente
  de smoke test del usuario. 0.2.1 se archivo como `.jar.replaced`.
- `mods/java/scoreboards`: prototipo `2.0.12` validado contra estable `0.6.2`. Usa
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
- `mods/asset-packs/metalwork-flat-roofs`, version `0.1.3`: Void y nuevas copias
  de Iron/Copper usan SeamSafe_Roof_Flat: pieza unica sin overhang ni tilt,
  huella 31.984x31.984 y altura 6; texturas vanilla referenciadas.
  Anade Void ornamentado,
  Raynor_Metal_Void_Roof_Flat, heredado de Metal_Iron_Roof_Flat con modelo
  vanilla referenciado y atlas propio opaco 160x352. Adamantite corrige
  las UV para usar solo los 13x13 pixeles opacos de Metalwork y stretch para
  ocupar 32x4x32 unidades. Las otras ocho variantes necesitan corregir sus UV.
  Instalado en Anima 0.6.X/mods. El pack anade nueve
  tejados planos metalicos (Adamantite, Cobalt, Gold, Mithril, Onyxium,
  Prisma, Refined Iron, Silver y Thorium). Cada item hereda el comportamiento
  de `Rock_Stone_Brick_Roof_Flat`, usa un modelo propio de bloque completo y
  reutiliza bajo GPL-3.0-only las texturas e iconos de Metalwork 1.0.0 de
  Lordimass. No copia modelos ni atlas vanilla; Bronze, Copper, Iron y Zinc se
  omiten porque ya existen en Hytale. Las recetas estan desactivadas y los
  bloques se obtienen desde `Blocks.Metal` en creativo.
- `mods/asset-packs/mechanisms`, version `1.1.2`: props accionables y la piedra
  `OrbGenesis_NoBuild_Stone`. Su volumen temporal usa `ModifyRules` con
  operacion `SET` en `VOLUME_CREATE` para insertar `NoBuild`, porque
  `SpawnTriggerVolume` 0.6.8 no copia directamente las reglas del asset. El
  volumen mide 10x10x10, retira `NoBuild` a los 10 segundos y usa
  `SpawnParticleShape` para mostrar fases roja y amarillo-naranja durante el
  bloqueo y una fase verde de desbloqueo en el segundo 10. Depende de Particle
  Shape VFX 0.1.1.
- `mods/asset-packs/roguelike-prefabs`: pack `tests:tests` con los mapas y
  prefabs usados por `test roguelike` y, de forma opcional, por el prototipo
  auxiliar Map Selector.
- `experiments/asset-packs`: material de laboratorio. Puede sobreescribir IDs
  vanilla y combinar varias pruebas incompatibles; inspeccionar antes de usar.
- `experiments/asset-packs/worldgen-v2-lab`: laboratorio WorldGen V2 `0.3.0`
  fijado a `0.7.0-pre.1`. La cadena canonica es Instance `HytaleGenerator` ->
  WorldStructure -> Biome -> Density/Material/Props. Incluye las instancias
  efimeras `Raynor_Lab_Rolling_Hills` y `Raynor_Sky_Gardens`; esta ultima usa
  tres poblaciones 3D independientes, spawn entre centros naturales, lagunas y
  vegetacion por bloques Manual, todavia sin prefabs. La interfaz experimental
  `IWorldGen.Cubic` ya permite proveedores
  con altura/profundidad infinitas, pero en pre.1 el `Handle` de
  `HytaleGenerator` aun implementa la ruta columnar `IWorldGen` (Y 0..319), por
  lo que un Biome V2 todavia no puede poblar un mundo cubico completo. Incluye
  brief de prompt, registro de iteraciones, inventario de la instalacion y
  validacion/build. Los assets V2 oficiales se consultan desde `Assets.zip`
  bajo `Server/HytaleGenerator` y el codigo desde
  `HytaleServer/builtin/HytaleGenerator`; nunca se copian al repo.

## Higiene del repositorio

- `auxiliary/minecraft-bedrock/admin-menu`, version `1.2.0`: add-on familiar de
  Minecraft Bedrock (no Hytale). Mantiene `raynor:admin_menu` y UUID de 1.0.0;
  ahora usa reloj, menus con iconos y botones +/-, catalogo de efectos beneficiosos
  infinitos sin particulas y encantamientos sin maldiciones de inventario/armadura.
  Los botones +/- aplican inmediatamente; nivel 0 retira el efecto o encantamiento. Los efectos
  instantaneos se repiten cada segundo mientras esten activados. Se distribuye
  como `.mcaddon` con BP y RP enlazados; compilar con su `tools/Build-Pack.ps1`.
  Requiere Bedrock 26.40, server 2.9.0 y server-ui 2.1.0. Pruebas automaticas
  correctas; smoke test de 1.2.0 pendiente. Ver su README para limites e instalacion.

- `auxiliary/minecraft-bedrock/mob-buckets`, version `1.0.0`: add-on Bedrock
  independiente con `raynor:mob_bucket`. Captura por interaccion y libera al usar
  el cubo; conserva el tipo, pero no el estado completo del mob. Excluye mascotas,
  criaturas con nombre o inventario, jugadores y jefes. Se distribuye como
  `.mcaddon` de un unico behavior pack; compilar con `tools/Build-Pack.ps1`.

- `auxiliary/minecraft-bedrock/diamond-walker`, version `1.0.2`: add-on Bedrock
  independiente. `/function diamantes` marca al ejecutor y el script alterna por
  jugador un camino que convierte
  el bloque no vacio directamente bajo sus pies en `minecraft:diamond_block`.
  El estado persiste como dynamic property y los cambios del terreno no se
  revierten. Se distribuye como `.mcaddon` de un unico behavior pack.

- La marca publica y la autoria de estos mods es `Raynor` / `Raynor Mods`.
  `OrbGenesis` identifica colaboraciones e IDs tecnicos historicos; no renombrar
  namespaces, paquetes ni assets persistentes hasta preparar una migracion
  compatible y separada.
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

## Revalidacion de mods publicos - 2026-09-03

Los cinco mods de `website/raynor-mods/portal-config.js` se han recompilado
contra release `0.6.3`. `scripts/Test-ActiveJavaMods.ps1 -PublishedOnly`
excluye Build Battle y Raynor NPCs; pasa el JAR estable tambien a los tests.
El build comun usa release por defecto; pre-release requiere `-ServerJar`.
Ver [informe y limites](docs/UPDATE_6_3_PUBLIC_MODS.md). La web y CurseForge
siguen con las versiones anteriores hasta publicar estos artefactos.
