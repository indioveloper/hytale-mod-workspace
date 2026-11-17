# Desert Survival Research: continuacion del 2026-11-12 (noche)

Estado operativo mas reciente: `games/desert-survival/docs/HANDOFF_2026-11-16.md`.
Las secciones siguientes conservan la cronologia y versiones anteriores.

Nota de traspaso escrita por el agente que retomo el proyecto tras el handoff
`games/desert-survival/docs/PROJECT_HANDOFF_INDEX_2026-11-12.md`. Resume que
se encontro, que se cambio, en que estado queda el PC y como seguir. Los
documentos de detalle estan enlazados al final; este archivo es el punto de
entrada para el siguiente agente.

## Resultado en una frase

El fallo de entrada a `dune1` esta resuelto, `/research` abre, y la interfaz
ha pasado de "un sector por vista con tres anillos" a "una rueda unica con
cinco sectores y tres bandas de tier" (Research 0.2.0), pendiente de revision
visual por el usuario.

## Que estaba pasando (diagnostico real)

El handoff del 12 de noviembre dejaba sin causa el error del cliente:

```text
Could not find document Hud/ReturnToHubButton.ui for Custom UI Append command.
```

Se cruzo cada sesion de `UserData/Logs/*_client.log` y
`UserData/Saves/dune1/logs/*_server.log` con el contenido real de cada JAR
archivado (`jars/*.replaced`, `*.failed-ui`). Conclusiones:

1. **Primera hipotesis, descartada.** Todas las builds fallidas llevaban
   `Common/UI/Custom/Pages/DesertResearch/IntelIcon.png`, copia byte a byte del
   icono del item Intel. Research 0.1.6 lo retiro y siguio fallando. La cache
   de assets del cliente es por SHA-256, pero el contenido duplicado no rompe
   la entrada. Se mantiene la comprobacion en el test como higiene.
2. **Causa confirmada.** El unico fichero comun a todas las builds fallidas
   (Research 0.1.2 a 0.1.6 y Vitals 0.2.3 a 0.2.6) y ausente de las que
   entraban era `ResearchNodeRow.ui` en su revision 0.1.2, que usa
   `HorizontalAlignment: Right`. En los 168 documentos vanilla de
   `Common/UI/Custom` ese enum solo vale `Start`, `Center` o `End`. Un
   documento con un valor que el cliente no reconoce anula **todo** su conjunto
   de documentos Custom UI, incluido el vanilla, y el error visible es el primer
   `Append` que envia el servidor (el boton de CreativeHub). Research 0.1.7 saco
   ese documento del paquete y la entrada funciono.
3. **Segundo fallo, ya en `/research`.** 0.1.7 desconectaba al abrir la pagina:
   `Selected element in CustomUI command was not found. Selector:
   #RingNodes[0] #RingNode.Anchor`. En el Shared Source
   (`TriggerVolumeInspectorPage`, `SelectOverrideRespawnPointPage`,
   `MemoriesPage`) el elemento raiz de un documento anadido con
   `append("#Lista", doc)` se direcciona como `#Lista[i]`, aunque tenga id
   propio. `#Lista[i] #Id` solo sirve para descendientes. Corregido en 0.1.8.
4. **Anillos descolocados.** La mascara vanilla
   `Common/CircularProgressBarMask.png` dibuja el anillo a 0.324 del tamano del
   control, asi que `@Size = 530` muestra un anillo de radio 172 y no 265.
   Calibrado en 0.1.9 y despues sustituido por la rueda unica en 0.2.0.

Reglas derivadas, ya anotadas en `AGENTS.md`:

- Antes de empaquetar un `.ui`, comprobar que cada valor de enum existe en
  algun documento vanilla. `tools/Test-Package.ps1` de Research lo hace para
  `HorizontalAlignment`, `VerticalAlignment` y `LayoutMode`; copiar ese bloque
  a otros mods con Custom UI.
- Direccionar el raiz de un documento anadido como `#Contenedor[i]`.
- El motor de Custom UI no tiene rotacion; las formas radiales se pintan como
  imagen y se colocan elementos encima por coordenadas.

## Cambios por version (todo en `games/desert-survival/prototypes/research-tree`)

| Version | Cambio | Resultado en `dune1` |
| --- | --- | --- |
| 0.1.6 | Retira `IntelIcon.png`; anillos con `$C.@CircularProgressBar`; `Test-Package.ps1` rechaza ficheros no `.ui` y contenido duplicado bajo `Common/`. | Sigue fallando la entrada. |
| 0.1.7 | `ResearchNodeRow.ui` sale del paquete (queda en `legacy/`, ya con `End`); el test rechaza enums ajenos a vanilla. | Entra. `/research` desconecta. |
| 0.1.8 | Selector `#RingNodes[i]` en `ResearchTreePage.java`. | `/research` abre; nodos fuera de los anillos y etiquetas truncadas. |
| 0.1.9 | Anillos redimensionados a los radios de los nodos; ids cortos; `ResearchSector.shortLabel()`; mensaje inicial. | Instalado y sustituido por 0.2.0 antes de probarse. |
| 0.2.0 | Rueda unica: `ResearchWheel.png` generada por `tools/Generate-ResearchWheel.ps1`; cinco cunas de 72 grados (orden del enum: Agua, Combate, Fabricacion, Exploracion, Vehiculos, empezando arriba); bandas tier 0/1/2 hasta r=150/225/288 con hueco 58; los 48 nodos a la vez con `ringAnchor(sector, tier, pos, n)` alternando 1, 2 o 3 radios por banda; nodos `$C.@SmallSecondaryTextButton` 68x24 con `nodeKey()` (primera palabra del nombre + digito de tier); sin botones de sector; panel derecho con rama, id, tier y coste. | Instalado. **Pendiente de revision visual.** |

Archivos tocados ademas del codigo y los `.ui`: `manifest.json` (0.2.0),
`README.md` del prototipo, `tools/Test-Package.ps1`, `legacy/README.md`,
`docs/RESEARCH_UI_HANDOFF_2026-11-12.md` (secciones "Diagnostico posterior" y
siguientes), `docs/PROJECT_HANDOFF_INDEX_2026-11-12.md` y `AGENTS.md` raiz.
`survival-vitals` no se ha tocado: su fuente sigue en 0.2.6 (estrategia
hidratada que fallo) mientras el JAR activo es 0.2.2.

## Estado del PC al cerrar

- `%APPDATA%/Hytale/UserData/Mods/DesertSurvival-Research-0.2.0.jar` activo,
  SHA-256 `073e1611...07ae71`, igual al build en
  `prototypes/research-tree/.build/dist/`.
- `Raynor_Desert_Survival_Vitals_Prototype-0_2_2.jar` activo, sin cambios.
- Archivados junto a ellos: Research 0.1.1 (`.replaced`), 0.1.6
  (`.failed-ui`), 0.1.7, 0.1.8 y 0.1.9 (`.replaced`). En
  `Saves/dune1/mods/` solo queda `0.1.5.jar.failed-ui`. Nunca renombrar dos a
  `.jar` a la vez: plugin duplicado.
- Runtime: Hytale estable 0.7.7, `HytaleServer.jar` en
  `%APPDATA%/Hytale/install/release/package/game/latest/Server/`.
- Git: `games/desert-survival/` sigue sin seguimiento y el arbol tiene muchos
  cambios ajenos; no se ha hecho commit de nada en esta sesion.
- La carpeta `output/desert-survival-handoff-2026-11-12-files/` es la copia de
  lectura del handoff; las fuentes canonicas son `games/desert-survival/`.

## Como seguir

Build, tests e instalacion (desde la raiz del workspace):

```powershell
.\scripts\Build-JavaMod.ps1 -ProjectPath .\games\desert-survival\prototypes\research-tree -ArtifactName DesertSurvival-Research-0.2.1.jar
```

```powershell
.\games\desert-survival\prototypes\research-tree\tools\Test-ResearchPrototype.ps1 -ServerJar "$env:APPDATA\Hytale\install\release\package\game\latest\Server\HytaleServer.jar"
```

```powershell
.\games\desert-survival\prototypes\research-tree\tools\Test-Package.ps1 -ArchivePath .\games\desert-survival\prototypes\research-tree\.build\dist\DesertSurvival-Research-0.2.1.jar
```

Instalar: renombrar el JAR activo a `.jar.replaced`, copiar el nuevo a
`UserData/Mods`, comparar SHA-256 y reiniciar la partida. El usuario hace el
smoke test en el juego y manda captura; no abrir el launcher por cuenta propia.

Si se cambian radios, colores o el hueco de la rueda, regenerar la imagen con
`tools/Generate-ResearchWheel.ps1` **y** actualizar `BAND_RADII`,
`WHEEL_CENTER_*` y los comentarios en `ResearchTreePage.java`; ambos describen
la misma geometria (grupo de 790x620, imagen de 580 px en (105, 20)).

Pendiente, por orden:

1. Revision visual de 0.2.0 con captura del usuario. Punto delicado: la cuna
   de Fabricacion en tier 0 tiene cinco nodos en la banda mas pequena.
2. Iconos por nodo en lugar del nombre clave (el usuario lo ha pedido para
   mas adelante). El hueco es el boton de 68x24; cambiar a `Group` con
   `Background` imagen cuando existan los assets.
3. Resaltar el nodo seleccionado y los investigados (ahora solo `*`).
4. Vitals: decidir si la fuente 0.2.6 vuelve a la estrategia de 0.2.2 (HUD
   propio sin hidratar vanilla), ya que la causa de los fallos no era la
   hidratacion sino el documento Research copiado dentro.
5. Pasar la comprobacion de enums de `Test-Package.ps1` a los demas mods con
   Custom UI (`configurable-mob-spawners`, `raynor-holograms`,
   `trigger-volume-effect-browser`).

## Lectura recomendada

1. `AGENTS.md` (raiz), parrafo `games/desert-survival`.
2. `games/desert-survival/docs/RESEARCH_UI_HANDOFF_2026-11-12.md`: evidencia
   sesion por sesion y cada variante probada.
3. `games/desert-survival/prototypes/research-tree/README.md`: loop de juego,
   comandos y estado de la interfaz.
4. `games/desert-survival/prototypes/research-tree/src/.../ResearchTreePage.java`
   y `Common/UI/Custom/Pages/DesertResearch/ResearchTree.ui`: la rueda.

## Continuacion: Research 0.2.1

- El usuario mostro que 0.2.0 abre `/research`, pero las etiquetas de nodos
  se solapan. 0.2.1 usa botones cuadrados de 32x32 con un PNG placeholder
  propio. Se ajustaron los radios de la banda interior; la comprobacion
  geometrica de las 48 cajas no encontro solapamientos.
- El objeto Intel recogido del suelo ya seguia la interaccion `Pickup` que
  incrementa el saldo y hace que el motor retire la entidad. Las copias
  insertadas directamente en inventario no pasaban por esa ruta. Ahora un
  `InventoryChangeEvent` retira esas copias y acredita su cantidad al saldo;
  en `PlayerReadyEvent` se consumen tambien copias heredadas en inventario.
- El panel `/research` y `/research status` ya no muestran Intel historico ni
  tier de material. El saldo disponible y el gasto por investigacion conservan
  su contrato persistente.
- El protocolo Custom UI 0.7.7 no expone coordenadas de arrastre al servidor.
  `TopScrolling` da desplazamiento vertical, pero no paneo libre de la rueda.
  La rueda sigue fija mientras se decide navegacion por regiones o una UI
  futura con soporte de arrastre.
- Build 0.2.1 y tests de catalogo y paquete pasaron contra el servidor estable
  0.7.7 instalado. `UserData/Mods/DesertSurvival-Research-0.2.1.jar` esta
  instalado con SHA-256 `D9A49A912EB79871BC0A31DE7391C006825F0617F1AB01A9DEA5F2C9F48B4294`,
  igual al build. 0.2.0 quedo como `.jar.replaced`; Vitals 0.2.2 sigue activo.
  Pendiente: smoke test en juego por el usuario. No se abrio el launcher.

## Continuacion: Research 0.2.2

- La rueda usa tres documentos de boton segun el estado calculado en el
  servidor: azul disponible (prerrequisitos e Intel suficientes), gris
  bloqueado y verde investigado. Los nodos bloqueados siguen siendo
  seleccionables para consultar su descripcion; el boton de investigar se
  desactiva. `ResearchLinks.png`, generado desde los IDs y prerrequisitos de
  `ResearchCatalog.java` por `tools/Generate-ResearchLinks.ps1`, muestra 37
  enlaces debajo de los botones. Los IDs persistentes no cambian.
- Combate se movio fuera del borde derecho de la rueda. Agua pasa a
  "Disciplina del Agua" y se corrigieron las tildes visibles. El detalle solo
  muestra el coste en Intel como metadato; se quitaron la lista de requisitos,
  la confirmacion "Nodo seleccionado" y la leyenda inferior de tiers.
- Build 0.2.2 y tests de catalogo y paquete pasan contra el servidor 0.7.7.
  `UserData/Mods/DesertSurvival-Research-0.2.2.jar` instalado con SHA-256
  `1401068D2812088E04961A6B738361C6EE1E08B5EC447F278139B58D7F8DA832`,
  igual al build. 0.2.1 se archivo como `.jar.replaced`; Vitals 0.2.2 sigue
  activo. Pendiente de smoke test visual del usuario; no se abrio el launcher.

## Continuacion 2026-11-15: bloque A, arbol -> recetas (Research 0.2.4)

Actualizacion 0.2.5: el primer smoke test mostro que la receta no aparecia en
el crafteo de campo. Causa: una receta sin `BenchRequirement` no se registra
en ningun banco; el campo exige `Id: Fieldcraft`, `Type: Crafting` y una
categoria (`Tools`, la unica que define vanilla y que el cliente conoce). El
item pasa a llamarse `Weapon_Scrap_Knife_Prototype` (Cuchillo de chatarra /
Scrap Knife) y el nodo `CO-0.2` muestra ese nombre; su id no cambia. Nuevo
`/research reset` para reiniciar Intel, nodos y recetas aprendidas del arbol.
Las herramientas de admin pendientes estan en
`docs/ITEMS_AND_TECH_TREE_STATUS_2026-11-15.md`, bloque F.

Inventario previo en `games/desert-survival/docs/ITEMS_AND_TECH_TREE_STATUS_2026-11-15.md`.

- `ResearchNode` tiene ahora `unlocks`: lista de IDs de item cuyas recetas
  ensena el nodo. `ResearchUnlocks.java` define la tabla de los 48 nodos
  (unas 75 salidas) con los nombres acordados; la mayoria de esos items no
  existen todavia y aprender su receta es inocuo. El test exige al menos una
  salida por nodo y ninguna salida con dos duenos.
- `ResearchService.research()` llama a `CraftingPlugin.learnRecipe` por cada
  salida; `syncKnownRecipes()` se ejecuta en `PlayerReadyEvent` para
  personajes con progreso anterior. Se aprenden dos IDs por salida: el del
  item (clave de `CraftingManager`) y `<item>_Recipe_Generated_0` (clave de
  `DiagramCraftingWindow` para recetas inline).
- `ResearchService.isOutputUnlocked(progress, itemId)` es la guardia para
  procesos de maquina que no pasen por el banco nativo. Items fuera del arbol
  devuelven true.
- Convencion de recetas: inline en el JSON del item (`"Recipe"`), con
  `KnowledgeRequired: true` y `BenchRequirement` explicito. Para crafteo de
  campo en 0.7.7, usar `Id: Fieldcraft`, `Type: Crafting` y categoria `Tools`;
  omitir ese requisito impidio que apareciera la primera receta en la prueba.
- Primera pareja real para la prueba de extremo a extremo, dentro del pack
  Research: `Ingredient_Scrap_Prototype` (chatarra, modelo y textura vanilla
  `Metal_Iron_Scrap` referenciados) y `Weapon_Scrap_Knife_Prototype` (hereda
  `Template_Weapon_Daggers`, modelo del punal de hueso, danos reducidos,
  receta Fieldcraft/Tools de 3 chatarra, `KnowledgeRequired: true`), nodo `CO-0.2`.
- Compila y pasa ambos tests contra 0.7.7. Instalado en
  `UserData/Mods/DesertSurvival-Research-0.2.4.jar` (SHA-256 `90886dbf...e3879`,
  igual al build) tras cerrar la partida; 0.2.2 archivado como `.jar.replaced`.
  Smoke test pendiente.

Smoke test propuesto para 0.2.5 en `dune1`:

1. `/give Ingredient_Scrap_Prototype 6` y abrir el crafteo de inventario: el
   Cuchillo de chatarra no debe aparecer o debe salir como desconocido.
2. `/research`, investigar `Cuchillo de chatarra` (gratis).
3. Volver al crafteo: fabricar el cuchillo con 3 chatarra.
4. Salir y volver a entrar: la receta sigue conocida (sincronizacion al entrar).

## Continuacion 2026-11-15 (noche): companion web, visor del arbol

Nueva carpeta `games/desert-survival/companion/` (HTML/CSS/JS sin
dependencias). Replica la rueda de `/research` en el navegador con paneo,
zoom, panel de detalle (prerrequisitos, dependientes, recetas que desbloquea
y si el objeto ya tiene JSON en algun prototipo), busqueda, filtro por rama y
simulacion local de progreso en `localStorage`.

- Datos: `companion/tools/Export-ResearchCatalog.ps1` parsea
  `ResearchCatalog.java`, `ResearchUnlocks.java` y `ResearchSector.java` y
  escanea `prototypes/*/Server/Item/Items` y `server.lang`. Genera
  `companion/data/research-tree.json` y `.js` (global `window.RESEARCH_TREE`
  para abrir `index.html` desde disco). El Java sigue siendo la fuente de
  verdad; regenerar tras cambiar el catalogo.
- Cifras al generar: 48 nodos, 74 objetos desbloqueables, 7 con asset
  (destiltraje T1 x4, cuchillo de chatarra, generador, cutterray T3).
- Servir en local: `python -m http.server 8765 --directory
  games/desert-survival/companion`; hay entrada `desert-companion` en
  `.claude/launch.json` de la raiz.
- Probado en el navegador integrado: carga sin errores de consola, clic
  abre el panel, chips navegan, simulacion y reinicio funcionan, busqueda
  "cutterray" deja visibles CN-0.2, CN-1.2 y CN-2.2.
- Lecciones: con `setPointerCapture` en el SVG el evento `click` llega al
  SVG, no al nodo; la seleccion se resuelve en `pointerup` con
  `elementFromPoint`. El resaltado usa un unico estado (hover o seleccionado)
  recalculado entero para no depender del orden de eventos de raton.
- Pendiente: iconos por nodo, ficha de objeto con receta, progreso real del
  personaje, lore/NPCs/misiones y despliegue en Vercel.

En esa entrega no se habia tocado el mod ni la instalacion de `dune1`.

## Continuacion 2026-11-15: maquinaria y kit basico de sangre

- Research 0.2.6: `AG-0.1` desbloquea el extractor improvisado (20 de chatarra)
  y la bolsa hematica pequena vacia (2 de plastico y 3 de fibra vegetal).
  La bolsa llena de 250 ml existe como item creativo de prueba; la extraccion
  de cadaveres aun no esta programada. `AG-2.4` reserva ahora el ID de la
  futura bolsa grande, sin duplicar la receta pequena.
- Build Selector 0.6.0: controles de construccion al lado derecho para dejar
  libre el chat, selector sin boton de giro y accion `Seleccionar`. El
  generador abre cuatro ranuras filtradas a celulas, consume una cada cinco
  minutos, informa de la autonomia y publica una potencia fija de 100 unidades
  sin guardar el excedente. Todavia no hay consumidores de esa potencia.
  El purificador de sangre tiene UI propia: cada bolsa llena deposita 250 ml
  y crea 50 ml de agua; cada pulsacion de salida llena 250 ml de un flask.
  Estado de maquinas por posicion en `desert-machinery.properties` del mundo.
- Research y Build Selector compilan contra el mismo HytaleServer 0.7.7
  instalado (SHA-256 `6F87420B524162D27DC7CD734E5575850E5DBF5EB94B659F17D6CAC4DF932D4D`)
  y pasan los tests de paquete y catalogo. Research 0.2.6 instalado global
  (SHA-256 `5514BF9FB3249DCCBCD668CCAA4087C1C30A4FE328EFE311FFCA9BF579FCBABC`);
  Build Selector 0.6.0 instalado en `dune1/mods` (SHA-256
  `67C0689F76D85385131A2CB330AD60E1AE36AC2A9DD480BE99060957E6D7DAAB`).
  Las versiones anteriores quedaron archivadas. El usuario hace el smoke test;
  no se abrio el launcher ni la partida durante esta entrega.
- Exportador de companion ejecutado: 48 nodos, 75 objetos y 10 con asset.

## Continuacion 2026-11-16: purificador y generador

- Las capturas de la partida mostraron que el bloque de purificador anterior
  abria el banco vanilla, el generador solo anotaba combustible y llenaba el
  chat de avisos, y el extractor improvisado no aparecia en el crafteo.
- Build Selector 0.6.1 atiende tanto el ID antiguo como el nuevo purificador,
  abre una pagina de potencia y autonomia para el generador sin chat, y
  conecta el purificador a un generador activo del claim o a 12 bloques si
  esta fuera de una base. El HUD muestra la potencia producida; el excedente
  se disipa. El escudo de la consola usa esa energia antes de gastar sus
  propias celulas.
- Research 0.2.7 conserva los dos desbloqueos de AG-0.1 y da al extractor
  un Parent vanilla y un icono propio de placeholder. La receta queda
  pendiente de confirmacion visual en partida, como las UIs.
- Compilados contra el JAR 0.7.7 del save y tests de paquete y catalogo
  correctos. Instalados con Hytale cerrado, sin copias activas duplicadas:
  Research 0.2.7 global SHA-256
  `C514E880B5E676533F5F4D20F29881BC7320D5AA796F5720E1930AC447835E48`;
  Build Selector 0.6.1 en dune1/mods SHA-256
  `85C4BE6DC72EF3155FCF773421CBAA6E5AA96006E6A5BA631D92E34C5794CCE9`.
  Las versiones activas anteriores fueron archivadas; el usuario prueba el
  juego sin que este turno haya abierto el launcher.

## Continuacion 2026-11-16: recetas y catalogo de maquinas

- Captura del usuario: Build Selector 0.6.1 abre un panel custom de generador
  sin las ranuras nativas; las celulas no se pueden depositar. La 0.6.2
  restaura `Page.Bench` con `ContainerWindow`. La autonomia queda en el HUD
  de la base.
- El catalogo de construccion desplaza sus filas y anade purificador de
  sangre, generador, refineria de mineral y refineria quimica como prefabs de
  un bloque nativo. Cada entrada aparece al aprender su investigacion.
  El fabricador antiguo se oculta hasta CN-0.3 y el reciclador de bloques
  legacy sale del catalogo, conservando su ID para los saves.
- Generador y purificador dejan de ser recetas Fieldcraft. Las recetas de
  bolsillo existentes (extractor, bolsa, cuchillo) siguen en Fieldcraft.
  Cutterray T1, Maula T0 y luz personal aun carecen de asset final; sus
  futuras recetas quedan definidas como fabricacion manual inicial.
- Se ocultan por jugador y cancelan en servidor los crafteos vanilla de
  fogata, cama crude, antorcha crude, cofre pequeno, bancos vanilla,
  espada crude y todas las hachas y picos vanilla. Se usan paquetes
  `UpdateRecipes.Remove`, sin retirar recetas del registro global.
- Las refinerias/fabricador no procesan materiales todavia y cada nueva
  estacion cuesta un basalto temporal. Las pruebas de build y paquete de
  0.6.2 pasan contra 0.7.7; falta el smoke test del usuario.
- Build Selector 0.6.2 instalado en `dune1/mods` con Hytale cerrado. SHA-256
  build = instalado:
  `AE6BA4801923C5E95A5D37251DF8074D1C52987122DF621A6977474DB0EC55E5`.
  La 0.6.1 queda archivada como `.jar.replaced-20261116`. No se ha abierto
  el juego en este turno; el smoke test sigue pendiente.

## Continuacion 2026-11-16: mundo desertico con WorldGen V2

Nuevo pack `games/desert-survival/prototypes/desert-worldgen` (0.1.0), que
sigue `docs/WORLDGEN_DESERT_PLAN.md`. Detalles en su README.

- Cadena: `Server/Instances/Raynor_Desert_Survival/instance.bson`
  (`HytaleGenerator`, seed `raynor-desert-survival-v1`, guarda chunks) ->
  WorldStructure `Raynor_Desert_Survival` (`NoiseRange`) -> densidad exportada
  `Raynor_Desert_Zone_Distance` (Distance al origen + ruido +-70) -> cuatro
  biomas `Raynor_Desert_Zone{1..4}_*` y `Void` mas alla de 1400 bloques.
- El patron copia el portal Goblin vanilla (`WorldStructures/Portals_Goblins.json`
  + `Density/Portal_Goblin_Biome_Distance.json`), que es exactamente "zonas por
  distancia" en 0.7.7.
- Biomas generados por `tools/generate_biomes.py` (base compartida: CurveMapper
  sobre BaseHeight Base con caida de 40 bloques + terminos de relieve por zona;
  materiales SpaceAndDepth; nodos `Resource_Node_*_Prototype` via
  Props `Positions: Mesh2D` + `Assignments: Constant` + `Locator` + `Manual`,
  patron Floor sobre los bloques de superficie de la zona, escaner
  `ColumnLinear` relativo a Base).
- Validacion: `tools/Test-Package.ps1` (estructura) + `ValidateDesertWorldGen.java`
  (codecs reales del `HytaleServer.jar` de release, registro manual de 57 tipos).
  Los 6 assets decodifican. Hallazgos: `Mesh.Jitter` > 0.5 falla la validacion
  del codec; `Class.forName(AssetManager)` termina la JVM en un proceso sin
  servidor; `Biomes`/`DefaultBiome` del WorldStructure solo se resuelven en un
  AssetStore vivo.
- Instalado: `UserData/Mods/DesertSurvival-WorldGen-0.1.0.zip`, SHA-256
  `720391823A04B3BE027398A34DDCD9C1476291E25E0E8D897E12D547AD9D7A57`, igual al
  build. No sustituye nada. Requiere Cutterray 0.1.9 activo (bloques de nodo).
- Smoke test del usuario: entrar en un mundo creativo, `/instances`, abrir
  `Raynor_Desert_Survival`; revisar altura/suavidad de la zona 1, fronteras
  hacia +X (~300 y ~600), nodos apoyados en el suelo, canones transitables.
- Si el pack no aparece o la instancia no carga, mirar el log del servidor por
  `Raynor_Desert` y por errores de `HytaleGenerator`; es la primera vez que un
  pack propio V2 se prueba contra 0.7.7 (el laboratorio estaba fijado a
  0.7.0-pre.1).

### Pruebas 2026-11-16 del mundo desertico

- 0.1.0 en `New World`: zonas bien, sin nodos. El log decia `invalid name
  Resource_Node_...: using Unknown` porque Cutterray estaba desactivado en la
  configuracion de ese save. 0.1.1 declara la dependencia en el manifiesto.
- 0.1.1 en `dune2` con Cutterray activo: zonas bien, sin avisos, sin nodos. El
  prop `Locator` + `Manual` no coloca. 0.1.2 pasa a `Column` con
  `Directionality: Static` + patron `Floor` + escaner `ColumnLinear`, calcado de
  los arbustos de `Desert1_Rocky`. Instalado (SHA-256 `7E707AA7...B0D0`),
  0.1.1 archivado. Smoke test 2026-11-16: nodos visibles en instancia nueva.
  Regla confirmada: bloques sueltos por Props con `Column`, no `Locator` +
  `Manual`.
- Siguiente del plan: prefabs de ruinas/bases, frontera jugable, balance de
  densidad de nodos y varias seeds.

### 2026-11-16: prefabs de ruinas y puestos (WorldGen 0.2.0)

- `generate_biomes.py` gana `prefab_props(paths, spacing, jitter, seed,
  load_entities)`: Mesh2D + Assignments Constant + prop `Prefab` con
  `WeightedPrefabPaths` (carpetas vanilla relativas a `Server/Prefabs`),
  `Directionality: Random` con patron Floor (origen aire, suelo no aire),
  escaner `ColumnLinear` sobre Base, `MoldingDirection: None`. Copia de las
  chozas Feran de `Assignments/Desert1/Desert1_Oasis_Feran_Huts.json`.
- Zona 1: `Monuments/Incidental/Sandstone/White/{Camp,Drywood}` y
  `!Wells/White`, celda 150, jitter 0.4. Zona 3:
  `Monuments/Encounter/Zone2/Tier1/Outpost` celda 320 jitter 0.3 (NPCs via
  LoadEntities) y `Incidental/Sandstone/Red/Drywood` celda 140.
- El validador registra `Prefab` y `Random`; decodificar un PrefabPropAsset
  fuera del juego funciona (no carga los ficheros en decode). El test
  estructural verifica que cada carpeta tenga `.prefab.json` en `Assets.zip`.
- Instalado 0.2.0 (SHA-256 `5C362892...BE93`), 0.1.2 archivado. Smoke test
  2026-11-16 correcto: ruinas en zona 1 y puestos en zona 3 visibles.
- Sin regla de exclusion entre mallas (prefab vs nodo, ruina vs puesto) ni
  loot de chatarra propio todavia.
