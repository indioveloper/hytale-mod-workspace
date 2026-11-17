# Selector de construcción: investigación y propuesta

Fecha: 2026-11-14; prototipo actualizado 2026-11-15. El objetivo es elegir
suelos, paredes, puertas y módulos desde una sola herramienta, con una
experiencia cercana a la construcción por piezas de *Dune: Awakening*.
El prototipo aislado 0.3.0 en prototypes/build-selector/README.md incluye
catálogo, vista previa 3D y proyección en el mundo de siete prefabs. La
construcción de supervivencia completa sigue pendiente.

## Qué existe y qué está comprobado

- Hytale 0.7.7 incluye `PrefabStore` para encontrar prefabs de asset packs y
  `PrefabUtil` para pegarlos con rotación. También incluye una página nativa de
  navegador de prefabs (`PrefabPage`). El navegador creativo no es un catálogo
  de supervivencia: puede mostrar prefabs del servidor y de otros packs, sin
  coste ni investigación asociada.
- En este repositorio, `auxiliary/java/map-selector` demuestra una Custom UI
  con `PrefabPreviewComponent`: al pulsar un botón, carga un `BlockSelection` y
  envía `BuilderToolPrefabPreview` al cliente. La vista permite girar, acercar
  y desplazar el modelo. Es código auxiliar de la etapa 0.6.x, no un mod que
  debamos instalar en Desert Survival; hay que validar el patrón en 0.7.7.
- La UI actual de Research ya presenta botones cuadrados con imagen; otros
  mods del workspace usan `LayoutMode: TopScrolling` para listas largas. Esas
  piezas permiten construir el catálogo visual sin inventar un control nuevo.
- El inspector vanilla de Trigger Volumes usa los paquetes
  `ShowTriggerVolumePastePrefabPreview` y
  `HideTriggerVolumePastePrefabPreview` para una proyección privada por
  jugador. La 0.2.1 adopta ese mecanismo y recalcula la posición según la
  cámara en cada tick; falta confirmar su comportamiento visual dentro del juego.

## Controles durante la colocación

La 0.2.2 conserva la sesión y la pieza tras un pegado correcto. Las entradas
`Use`, `Ability1` y `Pick` del objeto activan, respectivamente, pieza siguiente,
pieza anterior y giro cardinal. La 0.2.3 anade `Ability2` para pasar a la
categoria siguiente y define `HudInputBindingEntry` en las seis interacciones
del plano. El componente vanilla `InputBindings` dibuja sus iconos de tecla y
etiquetas localizadas mientras el item esta equipado. El cliente decide las teclas físicas de esas
acciones y permite cambiarlas en sus ajustes. La API de assets del servidor
expone estas acciones, pero no una asignación obligatoria de Q/E ni un evento
de rueda independiente. La rueda cambia el slot de la hotbar, por lo que
interceptar ese cambio también capturaría la intención de cambiar de objeto.
Para una rueda de giro o Q/E dedicadas habrá que estudiar un componente de
cliente, además de mantener una alternativa para mandos y teclas remapeadas.
La 0.2.4 agrupa las secciones en Base y anade Maquinaria con fabricador de
supervivencia y reciclador de sangre colocables. Habilidades 1 y 2 recorren
las piezas de la categoria activa; 3 y 4 cambian de categoria. El chat anuncia
la categoria y pieza seleccionadas. Q/E/R son las teclas usuales de las tres
primeras habilidades; T requiere vincular Habilidad 4 en los controles del
cliente. Los dos prefabs nuevos aun no realizan procesos de fabricacion o agua.

La 0.3.0 usa un HUD fijo abajo a la izquierda, con iconos vanilla de raton
y de tecla, mientras se lleva el plano. `Ability1` y `Ability2` recorren las
piezas (Q/E por defecto); `Ability3` avanza de categoria (R); `Use` alterna
colocar/desmontar (F). Al cambiar pieza o girar no aparece mensaje en el chat.
El chat solo confirma colocacion, desmontaje o cambio de categoria, ademas de
avisos de error. La tecla fisica depende de la configuracion del cliente;
las pistas nativas del item muestran la asignacion real.

La consola de base coloca un claim de 40 x 40 x 100 centrado en el prefab.
Las piezas colocadas se registran en un archivo del mundo con propietario,
huella y coste temporal de basalto. Cobro: inventario, luego cofres cargados
del claim. Reembolso: cofre que ya contiene basalto, otros cofres con hueco,
inventario y saldo pendiente reclamable en `/desertbuild`. Los eventos normales
de colocar, romper, danar y usar bloques se bloquean a otros jugadores dentro
del claim. El prototipo aun no intercepta pegados creativos ni importa piezas
sin registro de versiones previas.

En 0.4.1, Q (`Ability1`) avanza por las piezas de la categoria actual. Si el
jugador esta agachado al pulsarla, la misma interaccion consulta
`MovementStatesComponent.crouching` y avanza de categoria, seleccionando su
primera pieza. El HUD explica ambos gestos. Esta via conserva el control en
el mod de servidor y evita depender de `Ability2`/`Ability3`, que en 0.7.7
se resuelven desde las ranuras de habilidades. El cambio requiere una prueba
en cliente para confirmar que el estado de movimiento llega antes del evento
de la herramienta.

Referencia: [interacciones de objetos en Hytale 0.7.7](https://docs.hytale.com/assets/item/items/),
[acciones de RootInteractions](https://docs.hytale.com/assets/item/rootinteractions/).

Referencias: [PrefabStore](https://docs.hytale.com/api/com/hypixel/hytale/server/core/prefab/PrefabStore),
[BuilderToolPrefabPreview](https://docs.hytale.com/api/com/hypixel/hytale/protocol/packets/buildertools/BuilderToolPrefabPreview),
[PrefabUtil](https://docs.hytale.com/api/com/hypixel/hytale/server/core/util/PrefabUtil),
[construcción y planos en Dune](https://duneawakening.com/news/building-in-dune-awakening-claim-your-piece-of-arrakis/).

## Pantalla propuesta

```text
┌──────────────────────────────────────────────────────────────────────┐
│ CONSTRUIR       [Suelos] [Paredes] [Puertas] [Techos] [Módulos]   × │
├───────────────────────┬───────────────────────┬──────────────────────┤
│ Buscar / filtrar      │ Vista 3D girable       │ Suelo de chatarra    │
│ [pieza] [pieza]       │ de la pieza elegida    │ 6 piedra + 2 chat.   │
│ [pieza] [pieza]       │                       │ Investigación: ...  │
│ [pieza] ...           │                       │ [COLOCAR]            │
│ ↕ desplazamiento     │                       │                      │
└───────────────────────┴───────────────────────┴──────────────────────┘
```

Las tarjetas muestran miniatura, nombre, coste resumido y estado: disponible,
sin materiales o pendiente de investigación. Un solo panel 3D actualiza la
pieza seleccionada; no se cargan decenas de previews 3D simultáneas. El panel
de detalle muestra el coste completo y la explicación del bloqueo. La selección
se recuerda por jugador mientras dure la sesión.

Cada entrada estructural apunta a un prefab real: el suelo, la pared o la
puerta pueden abarcar varias celdas y contener varios bloques. La vista 3D
muestra ese mismo prefab, y el servidor lo coloca como una pieza lógica. Las
miniaturas pueden ser PNG propios del pack, como los iconos de Research.

## Catálogo y colocación

El servidor debe publicar un catálogo cerrado de piezas autorizadas, con
nombre, categoría, imagen, tipo de colocación (`BLOCK` o `PREFAB`), asset,
prefab de vista previa, orientación permitida, coste y nodo de investigación.
No se debe generar el catálogo recorriendo todos los prefabs disponibles:
eso expondría salas de dungeon, estructuras de mapa y assets de otros packs.

| Categoría | Representación en el mundo | Motivo |
| --- | --- | --- |
| Cimientos, suelos, paredes, esquinas, marcos, puertas y techos | Prefabs de varios bloques | Cada sección se selecciona, orienta, cobra y coloca como una unidad de construcción, aunque ocupe varias celdas. |
| Máquinas y contenedores | Bloques funcionales propios, incluidos dentro de un prefab cuando proceda | Inventario, energía y estado persistente. |
| Habitaciones y puestos prediseñados | Prefabs de varias secciones | Colocar una composición completa desde un plano o desbloqueo. |

Ejemplo de escala inicial, aún pendiente de probar: suelo de 3 × 3 celdas,
pared de 3 de ancho × 3 de alto y marco con puerta de 3 × 3. Pueden combinar
bloques vanilla y bloques visuales propios. `Prefab` describe la **composición
y la colocación**; `BlockType` describe cada bloque que la compone. Para
retirar o reparar la sección completa habrá que guardar su origen, orientación,
huella e identidad de instancia en datos del servidor. Si alguien modifica
manualmente uno de sus bloques, el sistema debe detectar que la pieza ya no
está intacta y no borrar bloques ajenos al desmontarla.

Flujo: equipar herramienta de construcción → abrir catálogo → elegir pieza →
volver al mundo en modo de colocación → orientar y ajustar a cuadrícula →
confirmar. Antes de modificar el mundo, el servidor vuelve a comprobar
investigación, materiales, zona edificable, permisos de base, espacio libre y
rotación sobre **toda la huella**. El coste es por sección colocada, no por
bloque interno. El cambio y el consumo de materiales necesitan una operación
coordinada que no cobre si falla el pegado ni deje media sección colocada.
No debe usarse `FORCE` para sobrescribir bloques o entidades ajenos en el modo
supervivencia. La misma regla se aplica a las piezas simples y a los módulos.

La referencia de Dune distingue construcción pieza a pieza y planos de bases.
Aquí los planos guardados son una **función posterior**; el primer selector
solo ofrece piezas y un módulo prediseñado. Investigación desbloquea la
receta, y los recursos de cada zona determinan cuándo se puede construirla,
según `CRAFTING_MATERIALS_ZONES_AND_BUILDING.md`.

## Primer corte implementable

1. Catálogo con un suelo, una pared, un marco de puerta y una habitación
   pequeña. `/desertbuild` abre la preview 3D; al elegir ubicación cierra la
   página y equipa el plano de construcción. Falta smoke test de cliente.
2. Las cuatro secciones se pegan como prefabs de varios bloques en creativo,
   con giro cardinal, proyección en el mundo y rechazo de bloques ocupados.
   La retícula modular, costes, permisos y estado persistente de cada sección
   quedan pendientes.
3. Confirmar que la proyección nativa se actualiza y desaparece al cancelar,
   cambiar de herramienta o salir del mundo. El cliente muestra el fantasma
   con el paquete de Trigger Volumes; solo el clic de confirmación modifica el
   mundo.
4. Pegado de un módulo mayor con las mismas garantías. Probar fallo de
   colocación, giro, desmontaje y modificación manual de un bloque para
   asegurar que no se pierden recursos ni se borran construcciones ajenas.

## Riesgos de integración

El árbol Research ya mostró que un documento Custom UI inválido puede impedir
la entrada al mundo y acabar mostrando el error vanilla
`Hud/ReturnToHubButton.ui`. Antes de instalar el selector, validar los enums y
selectores contra la UI vanilla de la misma versión, pasar una prueba de
paquete y hacer un smoke test en un save aislado. Evitar otro JAR que publique
documentos Custom UI con rutas duplicadas; integrar la pantalla en el plugin
del modo cuando exista.
