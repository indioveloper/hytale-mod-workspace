# Handoff: Research UI y fallo de entrada en dune1

Fecha: 2026-11-12. El usuario ha pedido pausar este desarrollo y continuar
desde otro sitio. No instalar ni volver a probar variantes nuevas sin retomar
expresamente la tarea.

## Punto de entrada

1. Leer `AGENTS.md` y `README.md` de la raiz del workspace.
2. Leer `games/desert-survival/README.md` y
   `games/desert-survival/prototypes/research-tree/README.md`.
3. Leer `games/desert-survival/docs/RESEARCH_TREE_MVP.md` para el contrato
   de Intel y los IDs. Su descripcion de UI tabular esta desactualizada.
4. `games/desert-survival/docs/LOOT_AND_BLUEPRINTS_DRAFT.md` contiene un
   limite MVP antiguo que excluye el arbol. La decision posterior del usuario
   fue incluir Intel y el arbol en este prototipo.
5. `games/desert-survival/docs/RUNTIME_REVIEW_2026-11-10.md` fija Hytale
   estable 0.7.7. El runtime efectivo de las pruebas fue revision
   `12fc465604eecca402bbd479aafb423e0a1f0dc9`.

## Estado del trabajo

- Ultima combinacion que **se observo entrar** en `dune1`: Research 0.1.1
  + Vitals 0.2.2 (log cliente `2026-11-12_00-48-21_client.log`).
- Tras el ultimo fallo se retiro Research 0.1.5 del save y se restauro el JAR
  global Research 0.1.1. Vitals 0.2.2 sigue activo. No se ha hecho una
  nueva prueba de entrada despues de esa restauracion.
- Research 0.1.5 esta archivado en
  `UserData/Saves/dune1/mods/DesertSurvival-Research-0.1.5.jar.failed-ui`.
  Su codigo fuente permanece en el workspace. No esta activo.
- 0.1.5 incluye una propuesta de cinco sectores seleccionables, tres anillos
  concentricos (chatarra/cobre/hierro), un sector visible por vez y detalle
  del nodo seleccionado a la derecha. No se ha visto dentro del juego porque
  la conexion falla antes de abrir `/research`.
- Los 48 IDs de nodos, el componente persistente de Intel y los comandos
  `/research` no se deben renombrar a la ligera.
- Git contiene muchos cambios ajenos y `games/desert-survival` aparece como
  arbol sin seguimiento. Preservar todo; revisar `git status --short` antes
  de cualquier edicion. No usar carpetas antiguas de staging como fuente.

## Fallo reproducido y evidencia

El cliente muestra:

```text
Could not find document Hud/ReturnToHubButton.ui for Custom UI Append command.
```

- El `Append` proviene de `Hytale:CreativeHub`, concretamente
  `ReturnToHubButtonUI.send` del servidor base. El mundo `dune1` tiene
  `CreativeHub.StartupInstance=Defaults/CreativeHub` y el jugador conserva
  `CreativeHub.ParentHubWorldUuid`. No eliminar esa configuracion como
  supuesto arreglo: afectaria al flujo del mundo.
- El primer Research que fallo fue 0.1.2. Respecto de 0.1.1, modifico
  `ResearchTree.ui`, `ResearchNodeRow.ui` y anadio `IntelIcon.png` bajo
  `Common/UI/Custom/Pages/DesertResearch`.
- Se probaron Research 0.1.2, 0.1.3, 0.1.4 y 0.1.5, junto con variantes Vitals
  0.2.2 a 0.2.6. La hidratacion completa del arbol vanilla Custom UI, un
  unico JAR portador de UI, el renombrado `HUD` a `Hud`, la reconstruccion
  de cache del cliente y forzar una copia distinta de
  `ReturnToHubButton.ui` **no resolvieron** la entrada.
- En la prueba Vitals 0.2.6 el cliente registro una referencia descargada
  para `UI/Custom/Hud/ReturnToHubButton.ui` y aun asi rechazo el `Append`.
  Por tanto, la ausencia de transferencia por hash no explica el problema.
- En la prueba Research 0.1.5 el cliente registro la pagina de Research
  `UI/Custom/Pages/DesertResearch/ResearchTree.ui` antes de fallar en el
  boton vanilla. Log: `UserData/Logs/2026-11-12_21-11-25_client.log`.
- La sesion que entro y el ultimo fallo usaron la **misma** revision cliente
  y servidor 0.7.7. No atribuirlo a una actualizacion entre ambas sesiones.
- Las pruebas de compilacion, paquete y catalogo validan Java, rutas y 48
  nodos; **no** prueban la carga del parser Custom UI del cliente.

La causa exacta dentro del registro/parseo de Custom UI del cliente sigue
**sin identificar**. No afirmar que fusionar JARs, copiar el documento vanilla,
limpiar cache o cambiar su hash sea la solucion.

## Siguiente investigacion, sin alterar dune1

Partir de los JARs 0.1.1 y 0.1.2 archivados. Comparar las entradas de UI y
aislar **un cambio por variante** (imagen, `ResearchTree.ui`,
`ResearchNodeRow.ui`). Probar en una copia separada del save, con los mismos
mods y runtime. Comprobar primero si la combinacion restaurada entra ahora;
la ultima entrada positiva es historica, no posterior al rollback. Recoger
el log cliente completo y verificar si hay un error de parseo anterior al
`Append` de CreativeHub. Solo despues aplicar la interfaz radial.

El usuario habia reservado para si las pruebas del juego y pidio no abrir
launcher/partida. Obtener una nueva autorizacion explicita si se pretende
hacer el smoke test personalmente; no usar su save original como banco de
pruebas.

## Archivos y pruebas

- Fuente radial: `games/desert-survival/prototypes/research-tree/Common/UI/Custom/Pages/DesertResearch/ResearchTree.ui`,
  `ResearchRingNode.ui` y `src/gg/raynormods/desertsurvival/research/ResearchTreePage.java`.
- Datos/contrato: `ResearchCatalog.java`, `ResearchProgressComponent.java`,
  `ResearchService.java` y `manifest.json` en el mismo prototipo.
- Build: `scripts/Build-JavaMod.ps1` contra
  `%APPDATA%/Hytale/install/release/package/game/latest/Server/HytaleServer.jar`.
  El script ya comprueba que `javac` produzca clases: en un intento sin acceso
  al JAR, `javac` emitio una excepcion interna pero devolvio salida 0.
- Tests: `tools/Test-ResearchPrototype.ps1` y `tools/Test-Package.ps1`
  bajo `prototypes/research-tree`. 0.1.5 paso ambos, pero fallo el smoke test.
- Logs de servidor del ultimo fallo:
  `UserData/Saves/dune1/logs/2026-11-12_21-13-35_server.log`.

## Objetivo funcional pendiente

Cinco sectores, anillos de tiers 0/1/2, panel derecho del nodo elegido, sin
botones solapados. Investigar depende de Intel y prerrequisitos; alcanzar un
bioma/material limita el crafteo posterior, no la investigacion. Mantener
costes de cero o un Intel por nodo en el MVP y el pickup de Intel persistente.

## Diagnostico posterior (2026-11-12, noche)

Revision de los logs locales de cliente (`UserData/Logs`) y de servidor
(`Saves/dune1/logs`) sesion por sesion, cruzada con el contenido real de cada
JAR archivado:

| Sesion cliente | Research | Vitals | Resultado |
| --- | --- | --- | --- |
| 00-32-12 | 0.1.0 | 0.2.1 | entra |
| 00-48-21 | 0.1.1 | 0.2.2 | entra |
| 15-26-58 | 0.1.2 / 0.1.3 | 0.2.2 | falla `Hud/ReturnToHubButton.ui` |
| 15-41-24 | 0.1.3 | 0.2.4 (hidratado) | falla |
| 15-54-53 | 0.1.4 (sin UI) | 0.2.5 (hidratado + paginas Research) | falla |
| 16-01-34 | 0.1.4 (sin UI) | 0.2.6 (hidratado, incluye `ReturnToHubButton.ui`) | falla |
| 21-04-21 | 0.1.1 | 0.2.2 | **entra** (confirma el rollback) |
| 21-11-25 | 0.1.5 | 0.2.2 | falla |

El unico factor comun a todas las combinaciones que fallaron es el fichero
`Common/UI/Custom/Pages/DesertResearch/IntelIcon.png`, presente en Research
0.1.2, 0.1.3 y 0.1.5 y copiado tambien dentro de Vitals 0.2.5/0.2.6. Ese PNG
es identico byte a byte (SHA-256
`e0e068d762a4f7ed09916f1f3abd2050f5a04cca018cbb08a39f3ee7ac0dfa77`) a
`Common/Icons/Items/Raynor/Intel_Pickup_Prototype.png`. Ninguna combinacion
que entro contiene dos assets Common con el mismo contenido; los assets
vanilla tampoco. El log cliente muestra que su cache de assets es por hash
(`Added cached reference to asset <sha256>: <refs>, <ruta>`): en la sesion
15-54-53 el mismo hash aparece con contador `2` y una sola ruta, es decir, la
segunda ruta se pierde. El fallo aparece como "documento no encontrado" en el
primer `Append` que ejecuta el servidor (CreativeHub) porque el conjunto de
documentos Custom UI del cliente queda inutilizable, no porque falte el
documento vanilla.

Esto explica por que no sirvieron la hidratacion completa, el JAR unico, el
renombrado `HUD`/`Hud`, la limpieza de cache ni enviar una copia de
`ReturnToHubButton.ui`: todas esas variantes conservaban el duplicado.

### Research 0.1.6

- Retira `IntelIcon.png` del arbol `Common/UI/Custom` (la pagina radial 0.1.5
  nunca lo referenciaba).
- Dibuja los tres anillos con `$C.@CircularProgressBar` de `Common.ui` en vez
  de `CircularProgressBar` con `MaskTexturePath: "../../Common/..."`, una ruta
  relativa ascendente no verificada en el cliente.
- `tools/Test-Package.ps1` exige exactamente tres documentos `.ui`, rechaza
  cualquier fichero no `.ui` bajo `Common/UI/Custom` y falla si dos assets
  Common comparten SHA-256.
- Compila con `scripts/Build-JavaMod.ps1` contra el `HytaleServer.jar` 0.7.7
  instalado y pasa `Test-ResearchPrototype.ps1` y `Test-Package.ps1`.
- Artefacto: `prototypes/research-tree/.build/dist/DesertSurvival-Research-0.1.6.jar`.

Instalado el 2026-11-12 (noche) en `UserData/Mods/DesertSurvival-Research-0.1.6.jar`
(SHA-256 `b62ab2a6...58964c`, igual al build); 0.1.1 queda archivado como
`.jar.replaced` y Vitals 0.2.2 sin cambios. Pendiente: smoke test de entrada
en `dune1` y primera comprobacion visual de la UI radial con `/research`.
Regla derivada para todos los packs: nunca empaquetar dos assets Common con
los mismos bytes; si se necesita el mismo icono en dos rutas, variar el PNG.

### Resultado de 0.1.6 y variante 0.1.7

La sesion cliente `22-17-20` con Research 0.1.6 + Vitals 0.2.2 volvio a fallar
con el mismo error. El PNG duplicado queda descartado como causa (se mantiene
la comprobacion en el test por higiene).

Revisando que fichero comparten **todas** las builds fallidas y ninguna de las
que entran: `ResearchNodeRow.ui` en su revision 0.1.2 (SHA-256 `207ba10b...`),
incluido en 0.1.2, 0.1.3, 0.1.5, 0.1.6 y copiado en Vitals 0.2.5/0.2.6. Esa
revision introdujo `HorizontalAlignment: Right`. En los 168 documentos vanilla
de `Common/UI/Custom` ese enum solo toma `Start`, `Center` y `End`; `Right`
no aparece en ninguno. La revision 0.1.1 de ese documento no usaba la
propiedad. Hipotesis: un documento Custom UI que el cliente no puede parsear
invalida el conjunto completo de documentos, y el primer `Append` vanilla
(CreativeHub) lo reporta como documento no encontrado.

Research 0.1.7:

- `ResearchNodeRow.ui` sale del paquete y se conserva en
  `prototypes/research-tree/legacy/` (ya con `End`). La pagina radial nunca
  lo anadia.
- El paquete lleva exactamente dos documentos: `ResearchTree.ui` y
  `ResearchRingNode.ui`. Todas sus propiedades y valores de enum existen en
  documentos vanilla.
- `tools/Test-Package.ps1` rechaza valores de `HorizontalAlignment`,
  `VerticalAlignment` y `LayoutMode` ausentes de vanilla, ademas de las
  comprobaciones de 0.1.6. Verificado que rechaza 0.1.5 y 0.1.6.
- Compila y pasa ambos tests. Instalado en
  `UserData/Mods/DesertSurvival-Research-0.1.7.jar` (SHA-256 `05f47bde...`);
  0.1.6 queda como `.jar.failed-ui`. Smoke test pendiente.

Si 0.1.7 tambien falla, el siguiente paso es bisecar `ResearchTree.ui`:
instalar una variante con la `ResearchTree.ui` exacta de 0.1.1 (hash
`d1b05663...`) junto a `ResearchRingNode.ui`, y despues ir restaurando bloques
de la version radial uno a uno.

### Resultado de 0.1.7

Entrada confirmada por el usuario y por el log cliente `22-17-20` (segundo
intento, 22:22) con Research 0.1.7 + Vitals 0.2.2. Causa cerrada:
`ResearchNodeRow.ui` 0.1.2 con `HorizontalAlignment: Right`. Combinacion
activa: `UserData/Mods/DesertSurvival-Research-0.1.7.jar` y
`Raynor_Desert_Survival_Vitals_Prototype-0_2_2.jar`. Pendiente: primera
revision visual de la UI radial con `/research` y, si procede, retomar la
estrategia de Vitals 0.2.6 sin copiar documentos Research dentro de Vitals.

### Primera apertura de /research (0.1.7) y Research 0.1.8

Con 0.1.7 el jugador entro en `dune1`, pero `/research` desconecto con
`Selected element in CustomUI command was not found. Selector: #RingNodes[0]
#RingNode.Anchor`. En el Shared Source (`TriggerVolumeInspectorPage`,
`SelectOverrideRespawnPointPage`, `MemoriesPage`) el elemento raiz de un
documento anadido con `append("#Lista", doc)` se direcciona como
`#Lista[i]`, aunque ese raiz tenga id propio; `#Lista[i] #Id` solo sirve para
descendientes. Research 0.1.8 usa `#RingNodes[i]` para Anchor, Text y el
evento. Compila, pasa tests y esta instalado (0.1.7 archivado como
`.replaced`). Pendiente: abrir `/research` y revisar visualmente la UI radial.

### Resultado de 0.1.8 (smoke test visual)

`/research` abre con Research 0.1.8 + Vitals 0.2.2: cabecera de Intel, cinco
sectores, tres anillos, panel derecho con nodo, estado y boton, y cierre.
Captura del usuario del 2026-11-12. Ajustes visuales pendientes observados:

- Los botones de nodo no caen sobre los anillos: el grupo `#RingNodes {}` sin
  Anchor no comparte el origen del grupo de anillos, asi que los nodos quedan
  desplazados hacia fuera y hacia arriba. Dar a `#RingNodes` el mismo Anchor
  que los anillos (o calcular `ringAnchor` desde el origen real).
- Las etiquetas se truncan a `AG-...`: ancho 80 insuficiente para el id;
  mostrar solo el numero (`0.1`) o ampliar a ~110 px con FontSize menor.
- El anillo interior (tier 0, chatarra) es el mas pequeno y queda vacio por
  dentro; revisar radios para que los nodos de cada tier queden sobre su
  anillo y no entre dos.
- El pie `Selecciona un nodo.` aparece aunque ya hay nodo seleccionado;
  cambiar el feedback inicial.
- Boton `Hecho` deshabilitado para nodos investigados: correcto.

### Research 0.1.9: primer ajuste visual

- `Common/CircularProgressBarMask.png` (250 px) dibuja el anillo entre los
  radios 76 y 86 px, es decir, la linea central del anillo esta a 0.324 del
  tamano del control. Un `@Size = 530` muestra un anillo de radio 172, no 265.
- Radios de nodo ahora 82 / 160 / 238 (tier 0 / 1 / 2), centrados en
  (310, 285) del grupo de 620x570; anillos de tamano 254 / 494 / 734 con el
  mismo centro. El control exterior sobresale del grupo, pero su parte
  visible (radio 253) cabe dentro.
- Botones de nodo 80x36 con id corto (`1.2`, `1.2 *` si investigado).
- Botones de sector con `ResearchSector.shortLabel()` (`Agua`,
  `Fabricacion`...); el titulo del sector sigue usando `label()`.
- Mensaje inicial: "Pulsa un nodo de los anillos para ver su detalle.".
- Instalado en `UserData/Mods` (0.1.8 archivado). Pendiente revision visual.

### Research 0.2.0: rueda unica con cinco sectores

Peticion del usuario (boceto): un solo circulo dividido en cinco sectores, uno
por rama; los anillos concentricos marcan el tier; las 48 tecnologias visibles
a la vez; dentro de cada nodo el nombre clave (en el futuro, una imagen).

- El motor Custom UI no tiene rotacion, asi que la rueda es
  `Pages/DesertResearch/ResearchWheel.png` (580 px, radio exterior 288),
  generada por `tools/Generate-ResearchWheel.ps1` con System.Drawing: tres
  bandas translucidas (tier 0 naranja hasta r=150, tier 1 cian hasta 225,
  tier 2 gris hasta 288), cinco divisores desde -90 grados y hueco central
  de radio 58. Regenerar con el script si cambian radios o colores.
- `ResearchTreePage.ringAnchor(sector, tier, pos, n)` coloca cada nodo en el
  sector k (cuna que empieza en -90 + 72k grados, orden del enum
  `ResearchSector`: Agua, Combate, Fabricacion, Exploracion, Vehiculos) y en
  la banda de su tier, alternando 1, 2 o 3 radios segun el numero de nodos
  para que no se solapen. Centro (395, 310) del grupo de 790x620.
- Nodos: `$C.@SmallSecondaryTextButton` de 68x24 con `nodeKey()`: primera
  palabra del nombre (max 9 letras) mas el digito de tier si el nombre lo
  lleva (`Kindjal 1`). `*` si esta investigado. Placeholder hasta tener iconos.
- Se retiran los botones de sector y el titulo de sector; cinco etiquetas
  fijas rodean la rueda y el panel derecho muestra rama, id, tier y coste.
- `Test-Package.ps1` espera tres entradas bajo `Common/UI/Custom` (dos `.ui`
  y la imagen). La regla "solo .ui" de 0.1.6 se relaja para esa imagen: la
  hipotesis del PNG quedo descartada y vanilla usa PNG bajo `Pages`.
- Compila, pasa tests e instalado en `UserData/Mods` (0.1.9 archivado).
  Revision visual pendiente; la cuna de Fabricacion tier 0 (5 nodos) es la
  mas apretada.
