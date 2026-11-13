# Desert Survival Research: continuacion del 2026-11-12 (noche)

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
