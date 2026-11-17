# Items y arbol de tecnologia: estado y pendientes (2026-11-15)

Documento histórico del 2026-11-15. Los nombres «Cutterray tier 1/2/3» de
las tablas originales ya no son la escala de diseño vigente: `CN-0.2` = T0,
`CN-1.2` = T1 y `CN-2.2` = T2; el item con ID T3 sigue siendo experimental.
Consultar [progresión actual](RESOURCE_PROGRESSION_RESEARCH.md) y
[STATUS](../STATUS.md) antes de usar la tabla como guía de implementación.

**Actualizacion posterior del mismo dia:** Research 0.2.5 ya implementa el
puente `ResearchUnlocks` -> `CraftingPlugin.learnRecipe`, lo sincroniza al
entrar y anade `/research reset`. `CO-0.2` ya tiene una receta real:
`Weapon_Scrap_Knife_Prototype`, 3 `Ingredient_Scrap_Prototype` en
`Fieldcraft/Tools`, con `KnowledgeRequired: true`. La consola de base,
el purificador de agua y el generador existen como bloques nativos en
Build Selector 0.5.3, instalado en `dune1` y pendiente de smoke test. El generador
figura entre las salidas futuras de `CN-0.3`; el purificador de agua no es
el purificador de sangre de `AG-0.2`. Los inventarios y pendientes de abajo
son el corte anterior a esas implementaciones.

Inventario hecho sobre el repo tal y como quedo tras las sesiones con ChatGPT
(Research 0.2.3, Vitals 0.2.6, Cutterray 0.1.9 y Build Selector 0.5.3 en
fuentes). Sirve para retomar la parte de **items y arbol** sin tocar la
interfaz de la rueda ni las herramientas de construccion, que quedaron a
medias en otra linea de trabajo.

## 1. Lo que existe hoy

### 1.1 Items definidos (16 JSON bajo `Server/Item/Items`)

| Prototipo | Item | Que es | Nodo del arbol que lo cubre |
| --- | --- | --- | --- |
| research-tree | `Intel_Pickup_Prototype` | Pickup de Intel; `Pickup` suma al componente persistente. Hereda `Potion_Template`. | Moneda del arbol, no es salida de nodo. |
| survival-vitals | `Water_Flask_Prototype` / `Water_Flask_Empty_Prototype` | Flask 500 ml; beber devuelve el vacio. Hereda `Potion_Template` / `Potion_Empty`, icono vanilla de pocion. | Fuera del arbol (recurso basico). |
| survival-vitals | `Armor_Stillsuit_Prototype` + `_T1_Head/_Hands/_Legs` | Set destiltraje tier 1: 5 de resistencia termica por pieza, bolsillo 500 ml en pechera. Heredan `Armor_Diving_Crude_*`. | `AG-1.1` Destiltraje tier 1 (completo como item). |
| cutterray-t3 | `Tool_Cutterray_T3_Prototype` | Herramienta jugable: rayo, extraccion de cinco segundos, modelo y textura propios. | `CN-2.2` Cutterray tier 3 (completo como item). |
| cutterray-t3 | `Resource_Node_Copper_Prototype` / `_Iron_` / `_Fuel_Cells_` | Nodos-bloque con modelo propio. Dropean `Ore_Copper` (vanilla), `Ore_Iron` (vanilla) y `Fuel_Cell_Prototype`. | Fuente de material, no son salidas. |
| cutterray-t3 | `Fuel_Cell_Prototype` | Ingrediente propio (icono y textura propios), combustible de la consola de base. | No esta en el arbol. |
| build-selector | `Desert_Build_Planner_Prototype` | Plano de construccion (hereda `Tool_Hammer_Crude`). | Fuera del arbol por decision de diseno. |
| build-selector | `Desert_Base_Console_Core_Prototype` | Consola de base; contenedor nativo de 4 ranuras para Fuel Cells. | No esta en el arbol. |
| build-selector | `Desert_Power_Generator_Prototype` | Banco placeholder, hereda `Bench_WorkBench`, categoria de crafteo `Processing`. | Cercano al "Generador basico" propuesto (receta auxiliar del Fabricador). |
| build-selector | `Desert_Water_Purifier_Prototype` | Banco placeholder, hereda `Bench_Alchemy`. | Cercano a `AG-0.2` Purificador de sangre. |

Ninguno de los 16 tiene receta: todos declaran `Recipe: null` o no lo
declaran, y no existe ningun fichero bajo `Server/Item/Recipes` en ningun
prototipo. Los materiales intermedios del borrador de crafteo (chatarra,
fibra, piedra, arena fina, lingotes, silicona, tejido filtrante, piezas
mecanicas, condensador, generador EMF, actuador Holtzman, sangre) **no
existen como items**. Solo hay `Ore_Copper` y `Ore_Iron` vanilla como drop.

### 1.2 Arbol de tecnologia

- `ResearchCatalog.java`: 48 nodos, 5 sectores, tiers 0/1/2, coste 0 o 1
  Intel, 37 prerrequisitos. IDs estables (`AG-0.1`...). Test de catalogo OK.
- `ResearchProgressComponent`: Intel disponible/historico, nodos
  desbloqueados, tier maximo. Persistente por personaje, esquema 1.
- `ResearchService.research()` valida y gasta; `addIntel()` acredita.
- `/research`, `/research status`, `/research grant`, `/research tier`.
- La rueda 0.2.3 muestra estados azul/gris/verde y enlaces dibujados.
- **No hay ninguna conexion entre un nodo investigado y una receta, item,
  estacion o permiso.** `ResearchNode` no tiene campo de salidas. Investigar
  solo cambia el componente.

### 1.3 Documentos de diseno ya escritos

- `RESEARCH_UNLOCKS_AND_ITEMS.md`: que objetos desbloquea cada nodo, via de
  fabricacion (Campo o Fabricador) y contrato de implementacion propuesto.
- `CRAFTING_MATERIALS_ZONES_AND_BUILDING.md`: 12 materiales con abreviatura,
  4 zonas, costes por receta de los 48 nodos y 9 estaciones a implementar.
- `RESEARCH_TREE_MVP.md`, `LOOT_AND_BLUEPRINTS_DRAFT.md`,
  `RESOURCE_PROGRESSION_RESEARCH.md`: contexto anterior.

### 1.4 Lo que ofrece el motor (comprobado en Shared Source 0.7.x)

- Recetas: JSON en `Server/Item/Recipes/**` con `Input`, `PrimaryOutput`,
  `Output`, `BenchRequirement` (`Type` + `Id`), `TimeSeconds` y
  `KnowledgeRequired`. Vanilla trae 410.
- `CraftingPlugin.learnRecipe(ref, recipeId, accessor)` anade el id al
  `PlayerConfigData.knownRecipes` del jugador y lo sincroniza al cliente.
- Ojo con la clave: `CraftingManager` comprueba el conocimiento por el
  **item id de la salida primaria**, mientras `DiagramCraftingWindow` lo hace
  por el **id de la receta**. Vanilla hace coincidir ambos: el fichero de
  receta se llama como el item que produce. Nuestras recetas deben seguir esa
  convencion y aprender ese mismo id.
- Bancos: un item con `BlockType.Crafting` y `Categories` propias define la
  estacion; `Desert_Power_Generator_Prototype` ya muestra el patron.

## 2. Pendiente, en orden propuesto

### Bloque A: cerrar el puente arbol -> recetas (sin assets nuevos)

1. Anadir a cada `ResearchNode` la lista de ids de salida (`unlocks`) sin
   cambiar los ids de nodo. Test: los 48 nodos tienen al menos una salida y
   ninguna salida pertenece a dos nodos.
2. Al investigar y en `PlayerReadyEvent`, llamar a `learnRecipe` por cada
   salida del nodo (sincroniza personajes con progreso antiguo).
3. Guardia de servidor para procesos que no pasen por el banco nativo
   (purificar, refinar, reparar), consultando `isUnlocked` del nodo dueno.
4. Prueba de extremo a extremo con un solo nodo y una receta real
   (propuesta: `CO-0.2` Kindjal tier 0, receta de 3 chatarra en Campo).

### Bloque B: materiales base (12 items + fuentes)

5. Crear los ingredientes `CH`, `FV`, `PC`, `AF`, `TF`, `PM`, `CE`, `GE`,
   `AH`, `SI` y los lingotes `CU`, `HI` (o reutilizar `Ingot_Copper` /
   `Ingot_Iron` vanilla si existen con la progresion deseada). Decidir si la
   "sangre" es item o volumen.
6. Fuentes: nodo de chatarra (hoy no hay), fibra en oasis, arena fina, y
   drop de los nodos actuales. Revisar que el cutterray entrega menas en
   cantidades coherentes con 4 menas -> 1 CU y 5 menas + agua -> 1 HI.
7. Recetas de componentes en Fabricador (TF, PM, CE, GE, AH) y de proceso en
   refinerias (CU, HI, SI). Todas con `KnowledgeRequired: true`.

### Bloque C: estaciones

8. Fabricador general (`CN-0.3`): banco propio con cinco categorias. Puede
   partir de `Desert_Power_Generator_Prototype` como plantilla de banco.
9. Refineria de mineral (`CN-0.4`) y quimica (`CN-0.5`), purificador de
   sangre (`AG-0.2`) y mejorado (`AG-2.5`), estacion de reparacion
   (`CN-2.4`), cisterna (`AG-1.3`) y recolector de rocio (`AG-2.2`).
   `Desert_Water_Purifier_Prototype` es un bloque de agua separado de `AG-0.2`.
10. Contrato de energia (Power Pack `CN-0.1/1.1/2.1`, generador, Fuel Cells
    de la consola) antes de exigir energia a cutterray, luz o escudo.

### Bloque D: items por sector (salidas que aun no existen)

| Sector | Ya existe | Falta por crear |
| --- | --- | --- |
| Agua (10 nodos) | Destiltraje tier 1 (4 piezas) | Kit de exanguinacion, purificador, literjon (+vacio), cisterna, set Kirab (4), recolector de rocio, extractor mejorado, saco de sangre (+vacio), purificador mejorado. |
| Combate (14 nodos) | Cuchillo de chatarra (0.2.4) | 3 sets de armadura (12 piezas), Kindjal x2, Maula x3 + dardos, Karpov x2 + cartuchos, espada, escudo Holtzman, healkit. |
| Fabricacion (12 nodos) | Cutterray tier 3 | Power Pack x3, cutterray tier 1 y 2, fabricador, 2 refinerias, cofre, compactador, estacion de reparacion. |
| Exploracion (5 nodos) | Nada | Prismaticos, luz portatil, baliza de reaparicion, cinturon suspensor, kit de topografia. |
| Vehiculos (7 nodos) | Nada | Moto (entidad montable persistente), pistola de recuperacion, 5 modulos instalables. |

Recuento: 48 nodos, 2 cubiertos por items jugables (`AG-1.1`, `CN-2.2`),
2 con placeholder de banco (`AG-0.2`, generador auxiliar), 44 sin item.
Salidas individuales estimadas: ~75 items (sets de 4 piezas, municiones y
versiones vacias incluidas), 0 creadas con receta.

### Bloque E: decisiones de diseno abiertas que bloquean assets

- Sangre: item apilable o volumen en datos del objeto (como el agua).
- Energia: capacidad, consumo y recarga comunes de los Power Pack.
- Vehiculo: entidad montable con dueno; no hay precedente en el workspace.
- Baliza de reaparicion y kit de topografia: reglas sin cerrar.
- Si `Ore_Copper`/`Ore_Iron` vanilla siguen siendo el drop o se crean menas
  propias para no mezclar con la progresion vanilla.

## 3. Como seguir sin pisar la otra linea

- Trabajar en `research-tree` (datos, servicio, recetas) y en un nuevo asset
  pack o prototipo `desert-items` para items y recetas; no tocar
  `build-selector` ni los `.ui` de la rueda salvo el detalle del nodo.
- Mantener la regla de `Test-Package.ps1`: nada bajo `Common/UI/Custom`
  fuera de `.ui` y las imagenes ya aceptadas; enums solo vanilla.
- Cada receta nueva: fichero con el nombre del item de salida,
  `KnowledgeRequired: true`, `BenchRequirement` explicito, y entrada en la
  lista `unlocks` de su nodo.

### Bloque F: herramientas de admin y depuracion (anotado 2026-11-15)

Peticion del usuario tras el primer smoke test del bloque A: no habia forma
de reiniciar el progreso de investigacion dentro del juego. Research 0.2.5
anade `/research reset` (borra Intel, nodos y recetas aprendidas del arbol).
Pendiente un conjunto de admin coherente para todos los prototipos:

- `/research unlock <id|all>` y `/research lock <id>` para situar un
  personaje en un punto concreto del arbol sin jugar.
- `/research recipes` para listar las recetas que el modulo nativo considera
  conocidas y detectar desajustes entre arbol y crafting.
- Equivalentes en Vitals (fijar hidratacion, calor, exposicion) y Cutterray
  (dar nodos, forzar extraccion) y un `/desert status` que resuma todo.
- Permisos: estos comandos deben quedar tras un permiso de operador antes de
  cualquier build publica; hoy los `DEV:` estan abiertos.
- Registro: cada comando de admin deberia dejar linea en el log del servidor.

### Aprendido en el bloque A

- Una receta sin `BenchRequirement` no se registra en ningun banco y por
  tanto no aparece en el crafteo de campo. La fabricacion de campo exige
  `BenchRequirement: [{Id: "Fieldcraft", Type: "Crafting", Categories: [..]}]`.
  Vanilla solo define la categoria de campo `Tools`
  (`Server/Item/Category/Fieldcraft/Tools.json`); el cliente tiene las
  categorias de campo fijas, asi que de momento todas nuestras recetas de
  campo van en `Tools`.
- El conocimiento se guarda por id de item; el servidor envia al cliente
  solo las recetas registradas en bancos para ese item.

### Companion web (2026-11-15): en desarrollo

Experiencia online paralela al mod, sin las limitaciones de la Custom UI:
lore, enciclopedia de items, NPCs y arbol de tecnologia, y registro del
progreso de misiones del jugador. Items y NPCs enlazarian a la web.

Hechos ya comprobados para cuando se retome:

- El juego solo abre enlaces desde el chat: `Message.raw(url).link(url)`.
  Configurable Mob Spawners ya lo usa con su configurador. Una Custom UI no
  abre el navegador ni accede al portapapeles, asi que "el item muestra un
  enlace" sera "al interactuar, el servidor manda al chat un enlace clicable",
  con un token por jugador si la pagina debe ser personal.
- Hay web desplegada en Vercel (`website/raynor-mods`, HTML estatico con
  `portal-config.js`) que puede alojar el companion o servir de plantilla.
- Fuentes de verdad en el repo: `ResearchCatalog.java` (48 nodos),
  `ResearchUnlocks.java`, los JSON de items y los documentos de diseno. La
  web deberia generarse desde ellos para no mantener dos catalogos.
- Datos por personaje ya persistentes: Intel y nodos (Research), hidratacion
  (Vitals), claims y energia (Build Selector). Son la base del progreso.
- Preguntas abiertas antes de disenar: prioridad entre lore, enciclopedia y
  progreso; si la web es solo consulta o tambien actua sobre el juego; y si
  los usuarios son el grupo de prueba o jugadores anonimos (identidad).

Estado: primera entrega en `games/desert-survival/companion/` (ver su
README). Visor estatico del arbol con rueda identica a la del juego, paneo,
zoom, detalle por nodo con prerrequisitos, dependientes y recetas
desbloqueadas, marca de que objetos ya tienen JSON, busqueda, filtro por rama
y simulacion local de progreso. Los datos salen de
`companion/tools/Export-ResearchCatalog.ps1`, que parsea el Java del
prototipo; regenerar tras editar el catalogo. Sin despliegue todavia.
