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
  Punto de entrada para retomar: `games/desert-survival/STATUS.md` (tabla
  fuentes / instalado / probado y handoff breve). Cronologia en
  `DESERT_SURVIVAL_CONTINUATION_2026-11-12.md` en la raiz.
  `prototypes/desert-worldgen` 0.2.0: pack WorldGen V2 para 0.7.7 con la
  instancia `Raynor_Desert_Survival` (seed fija, guarda chunks): WorldStructure
  `NoiseRange` sobre la distancia deformada al origen, cuatro biomas por zona
  (refugio 0-260, dunas 260-560, canones 560-900, profundo 900-1400, `Void`
  fuera) generados por `tools/generate_biomes.py`, y nodos de recurso de
  Cutterray colocados por Props `Mesh2D` + `Column` (0.1.0/0.1.1 con
  `Locator` + `Manual` no colocaban nada; 0.1.0 ademas fallo porque Cutterray
  estaba desactivado en el save, ahora es dependencia del manifiesto). `Build-Pack.ps1`
  valida estructura y decodifica cada asset con los codecs del servidor
  (`Mesh.Jitter` maximo 0.5; `AssetManager` no se puede cargar fuera del
  servidor). 0.1.2 paso el smoke test (zonas y nodos visibles). 0.2.0 anade
  prefabs vanilla con prop `Prefab` (yaw aleatorio, `LoadEntities`): ruinas de
  arenisca blanca en zona 1 cada ~150 bloques, puestos `Encounter/Zone2/Tier1`
  cada ~320 y ruinas drywood rojas en zona 3; el test comprueba las carpetas
  en `Assets.zip`. ZIP 0.2.0 instalado en `UserData/Mods` con SHA-256
  `5C36289234DE1308B95400200132178F34CE5ACA1A6C1D920162C79EDAAEBE93`; se abre
  con `/instances`. Smoke test 2026-11-16 correcto: zonas, nodos y prefabs visibles.
  `games/desert-survival/companion/`: web estatica (sin dependencias) que
  replica la rueda del arbol de investigacion con paneo, zoom, detalle por
  nodo, recetas desbloqueadas y simulacion local; sus datos se generan con
  `companion/tools/Export-ResearchCatalog.ps1` desde el Java del prototipo
  (regenerar tras editar el catalogo). Sin despliegue.
  Cutterray T3 0.1.9 en fuentes: anade el nodo
  `Resource_Node_Fuel_Cells_Prototype` y el drop custom
  `Fuel_Cell_Prototype`, extraible en cinco segundos desde tier 1 con el mismo
  rendimiento por tier que cobre y hierro. Compilado contra 0.7.7; sin
  despliegue ni smoke test. `docs/WORLDGEN_DESERT_PLAN.md` propone cuatro
  biomas sobre terreno desértico procedural y distribucion de nodos por Props.
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
  Research 0.2.3 en fuentes: el aviso de prerrequisitos usa nombres visibles en
  vez de IDs internos. Research 0.2.4 en fuentes: cada nodo lista sus salidas
  (`ResearchUnlocks`), investigar y entrar ensenan esas recetas al modulo
  nativo de crafting (`CraftingPlugin.learnRecipe`, item id y
  `<item>_Recipe_Generated_0`), guardia `isOutputUnlocked` para maquinas, y
  primera pareja real `Ingredient_Scrap_Prototype` + `Weapon_Kindjal_T0_Prototype`
  con receta inline de campo y `KnowledgeRequired`. Instalado; el smoke test
  mostro la receta ausente del crafteo de campo. Research 0.2.5: la receta
  declara `BenchRequirement` `Fieldcraft`/`Tools` (sin el, ningun banco la
  registra), el item pasa a `Weapon_Scrap_Knife_Prototype` (Cuchillo de
  chatarra) y se anade `/research reset`. Instalado en `UserData/Mods`
  (0.2.4 archivado); smoke test pendiente. `docs/RESEARCH_UNLOCKS_AND_ITEMS.md`
  relaciona los 48 nodos con sus objetos y propuesta de implementacion.
  Research 0.2.5 liga la primera receta real al nodo `CO-0.2`; los demas
  objetos de la tabla aun no tienen receta. Compila y pasa tests de catalogo
  y paquete contra 0.7.7.
  `games/desert-survival/docs/CRAFTING_MATERIALS_ZONES_AND_BUILDING.md` es el
  borrador de costes por objeto, materiales por zona, estaciones y construccion
  basada en secciones prefab de varios bloques. Son cifras de diseno, no
  recetas implementadas.
  El prototipo build-selector 0.1.0 cargo en dune1, pero no encontro los
  prefabs por omitir `.prefab.json`: menu sin vista 3D y sin pegado. La 0.2.0
  corrige la ruta y, tras elegir una seccion, cierra el menu, equipa el plano
  de construccion y muestra un fantasma privado. La 0.2.0 instalado en dune1
  mostro el fantasma fijo, pero las interacciones fallaron: los JSON con
  `Type`/`Next` estaban en `RootInteractions` y el log mostro `Missing interaction`.
  La 0.2.1 los mueve a `Item/Interactions`, sincroniza el slot activo y actualiza
  el fantasma desde la camara cada tick. Clic izquierdo coloca y derecho cancela.
  Compila y pasa test de paquete contra 0.7.7; instalada en dune1/mods con
  SHA-256 igual al build y smoke test pendiente. No hay costes, bloqueos
  Research, permisos de base ni desmontaje persistente.
  La 0.2.2 en fuentes conserva pieza y fantasma tras colocar; mapea `Use` a
  siguiente, `Ability1` a anterior y `Pick` a giro. Q/E y rueda no son teclas
  asignables desde este plugin de servidor; los controles fisicos dependen del
  cliente. Compila y pasa test de paquete; instalada en dune1/mods con SHA-256
  igual al build, la 0.2.1 archivada y smoke test pendiente.
  Build Selector 0.2.3 en fuentes: `Ability2` cicla categorias y las seis
  acciones del plano usan `HudInputBindingEntry` para que el HUD vanilla
  `InputBindings` muestre iconos de las teclas configuradas. Al cancelar,
  vuelve al slot anterior. Compila y pasa test de paquete contra 0.7.7;
  instalada en dune1/mods con SHA-256 igual al build, la 0.2.2 archivada y
  smoke test pendiente.
  Build Selector 0.2.4 en fuentes: seis prefabs en dos categorias, incluidas
  maquinas placeholder colocables (fabricador de supervivencia y reciclador de
  sangre). Habilidades 1/2 ciclan piezas, 3/4 ciclan categorias y cada cambio
  anuncia categoria y pieza en el chat. Para Q/E/R/T, el cliente debe asignar
  Habilidad 4 a T; el plugin no configura teclas fisicas. Compila y pasa test
  de paquete contra 0.7.7; instalada en dune1/mods con SHA-256 igual al build,
  0.2.3 archivada y smoke test pendiente.
  Build Selector 0.3.0 en fuentes: HUD fijo inferior izquierdo con iconos de
  tecla vanilla; Q/E recorren piezas y R avanza de categoria. F alterna
  colocacion y desmontaje, y el chat solo anuncia colocacion, desmontaje y
  categoria. La consola de base crea un claim de 40x40x100 centrado en ella.
  Las piezas tienen registro persistente y coste temporal en bloques de
  basalto: cobra de inventario y cofres cargados del claim, reembolsa a cofres
  preferentes, inventario o saldo reclamable desde el menu. Protege los
  eventos normales de bloque frente a otros jugadores. No importa piezas
  colocadas con 0.2.4; no protege pegados directos de Builder Tools. Compila
  contra 0.7.7 y pasa test de paquete; instalada en `dune1/mods` con SHA-256
  `460CD76189650CC0175BCE092C56B3E8D81CF79C21E0966AAF23E7467F18712C`,
  igual al build. La 0.2.4 queda archivada; smoke test del usuario pendiente.
  Actualizacion 2026-11-15: Build Selector 0.6.0 en fuentes mueve el HUD al
  lado derecho, simplifica el selector y agrega generador con inventario de
  celulas y autonomia persistente, mas purificador de sangre con interfaz y
  conversion 250 ml de sangre a 50 ml de agua. La potencia del generador es
  fija (100 unidades prototipo), sin almacenamiento ni consumidores conectados.
  Research 0.2.6 agrega las recetas del extractor improvisado y la bolsa
  hematica pequena al nodo AG-0.1. La bolsa llena es un item creativo de
  prueba; la extraccion de cadaveres sigue pendiente. Ambas versiones compilan
  contra 0.7.7 y estan instaladas juntas (Research global, Build Selector en
  dune1/mods) con SHA-256 igual al build; smoke test del usuario pendiente.
  Actualizacion 2026-11-16: Build Selector 0.6.1 enlaza el bloque legacy
  `Desert_Water_Purifier_Prototype` con la misma UI de sangre, elimina el
  spam del chat del generador y muestra potencia/autonomia en una pagina
  propia. El generador activo alimenta el purificador en el mismo claim o
  dentro de 12 bloques sin claim, y el HUD de base muestra su potencia.
  Research 0.2.7 da al extractor improvisado un Parent de herramienta nativa
  y un icono placeholder distinto para corregir su ausencia del crafteo.
  Ambas versiones estan instaladas, Research global y Build Selector en
  dune1/mods. SHA-256: Research
  `C514E880B5E676533F5F4D20F29881BC7320D5AA796F5720E1930AC447835E48`,
  Build Selector
  `85C4BE6DC72EF3155FCF773421CBAA6E5AA96006E6A5BA631D92E34C5794CCE9`.
  La visibilidad de esa receta y las dos UIs requieren smoke test del usuario.
  Actualizacion 2026-11-16 (Build Selector 0.6.2): la 0.6.1
  mostro solo la pagina custom del generador, sin inventario. La 0.6.2
  vuelve a `Page.Bench` con `ContainerWindow` de cuatro ranuras. El
  selector desplaza una lista de maquinas que se muestran al investigar
  sus IDs: purificador AG-0.2, fabricador y generador CN-0.3,
  refinerias CN-0.4 y CN-0.5. Los dos ultimos bancos y el fabricador son
  prototipos sin procesamiento. Las recetas de bolsillo del purificador y
  generador se retiran; las herramientas improvisadas existentes siguen en
  Fieldcraft. Un filtro por jugador oculta recetas vanilla seleccionadas
  con `UpdateRecipes.Remove` y cancela su `CraftRecipeEvent.Pre`; no modifica
  el asset map global. Las piezas nuevas siguen cobrando basalto temporal.
  Compila y pasa test de paquete contra 0.7.7. Instalada en `dune1/mods`
  con SHA-256 `AE6BA4801923C5E95A5D37251DF8074D1C52987122DF621A6977474DB0EC55E5`,
  igual al build; la 0.6.1 esta archivada. Smoke test del usuario pendiente.
  Build Selector 0.3.1 en fuentes: reemplaza los iconos rotos del HUD con
  estilos y pictogramas del legend vanilla, muestra coste/disponibilidad de
  basalto en verde o rojo, escudo no instalado y energia 0:00. Dibuja al
  propietario el perimetro cian del claim y un contorno naranja del prefab
  registrado apuntado para desmontar usando paquetes privados de display de
  Trigger Volumes, sin crear volumenes funcionales. El resultado visual en
  cliente requiere smoke test. Compila contra 0.7.7, pasa test de paquete y
  esta instalada en `dune1/mods` con SHA-256
  `1B85EE45B5D6218A40B3127A01F1FF65107B6165D75F69E8C4C218B73BFB1574`,
  igual al build. La 0.3.0 esta archivada.
  Build Selector 0.4.0 en fuentes: Q recorre las siete piezas y cambia de
  categoria al cruzar el limite; E/R no enviaban Ability2/3 al item porque
  esas entradas corresponden a las ranuras de habilidad. El HUD de desmontaje
  es rojo y oculta la navegacion; se suprime la confirmacion de colocacion y
  desmontaje en chat. El estado de base vive en un HUD aparte, visible dentro
  del claim, con nombre del dueno, escudo y tiempo. La consola abre un
  contenedor nativo de cuatro ranuras para `Fuel_Cell_Prototype` de Cutterray
  0.1.9; consume una celula por cinco minutos de energia y persiste saldo y
  tiempo en el ledger. El escudo aun no altera los permisos del claim. Las
  consolas 0.3.1 se migran in situ al entrar en su claim. Compila y pasa la
  prueba de paquete contra 0.7.7; instalada en `dune1/mods` con SHA-256
  `BDF76B90C0B466A8D00769B84F308A5BC98D9AAAC024D77092BE68D0E841BE84`.
  Cutterray 0.1.9 esta activo en `UserData/Mods` con SHA-256
  `6587F8E59B33F00A9C799791FC2C7E6DFBBE4B71C94B23BB631FD5506386E962`.
  No hay Group:Name activos duplicados entre mods globales y `dune1`.
  Smoke test pendiente.
  Build Selector 0.4.1 corrige la navegacion sin tocar bindings del cliente:
  Q avanza solo dentro de la categoria actual y agacharse + Q llama a
  `cycleCategory` leyendo `MovementStatesComponent.crouching` durante la
  misma interaccion Ability1 que ya funciona. El HUD muestra ambos gestos.
  Compila y pasa test de paquete contra 0.7.7; smoke test pendiente.
  Build Selector 0.5.0 en fuentes: corrige la altura de ambos paneles HUD;
  al seleccionar el plano sin sesion inicia directamente con la consola y
  su perimetro fantasma naranja. Con el plano equipado cancela las
  interacciones con bloques, incluidos cofres y consola. Retirar una consola
  exige dos clics en diez segundos y muestra una advertencia roja; se eliminan
  del ledger las piezas del claim, pero sus bloques quedan fisicamente en el
  mundo sin proteccion ni derecho a reembolso. El sitio abandonado admite
  nueva consola en el mismo origen, incluso con cofres existentes. Compila
  y pasa test de paquete contra 0.7.7; instalada en `dune1/mods` con SHA-256
  `5EF818AD714BA0BF04840D69E41ABD9F12149F492C75D23C18559896408EA6A3`.
  La 0.4.0 queda archivada y la 0.4.1 no llego a instalarse. Smoke test pendiente.
  Build Selector 0.5.1 usa el icono vanilla `RowHintIconMouseMiddleClick`
  para girar, tras comprobar `ToolsLegendsCommon.ui`; el `HotkeyLabel`
  anterior se mostraba vacio/cortado. Compila, pasa test de paquete y esta
  instalada en `dune1/mods` con SHA-256
  `2C43322254558E860D7AF8ECA6A03DA09F14655FF217079589F5826EC651653F`.
  La 0.5.0 queda archivada; smoke test pendiente.
  Build Selector 0.5.2 sustituye agacharse + Q por clic derecho para cambiar
  de categoria en modo colocar; en desmontaje, clic derecho sale. Q avanza
  piezas. Muestra en rojo los perimetros de bases cercanas al apuntar con una
  consola nueva, junto al perimetro futuro naranja. El bloque central declara
  `Open_Container`, y la apertura del inventario de combustible activa la
  pagina nativa `Page.Bench` con la ventana. El plano sigue bloqueando el uso
  de bloques: interactuar con la consola con otro objeto o la mano vacia.
  Compila y pasa test de paquete contra 0.7.7. Instalada en `dune1/mods`
  con SHA-256 `C9A11D544496ABCB0777E4AF5F26D893DB99B724024931BC32F8A8F747703197`,
  igual al build; la 0.5.1 esta archivada. Smoke test pendiente.
  Build Selector 0.5.3 en fuentes: consola nativa de un bloque con claim
  registrado tras la colocacion, mas purificador de agua y generador de
  energia como bancos nativos vacios con modelos vanilla provisionales.
  El prefab antiguo de consola sigue en el paquete para saves existentes,
  pero sale del selector; el plano empieza con el suelo. La consola nativa
  se recupera como item al desmontarla. El panel de colocacion es marron
  para piezas de base y azul para maquinaria; cambiar de categoria no
  escribe en el chat. El generador coincide con `CN-0.3` de Research 0.2.5,
  pero aun carece de receta. El purificador de agua es distinto del
  purificador de sangre `AG-0.2`. Compila y pasa test de paquete contra
  0.7.7. Instalada en `dune1/mods` con SHA-256
  `396B0ABC66261856D88B120EDD721F59C5C52CD6C681092174D6207872D1D783`,
  igual al build; 0.5.2 archivada. Smoke test pendiente.
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
