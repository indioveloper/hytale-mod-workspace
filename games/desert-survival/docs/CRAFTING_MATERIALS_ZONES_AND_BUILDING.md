# Materiales, recetas y construcción — borrador MVP

Fecha: 2026-11-14. Este documento **propone costes**, no crea todavía assets de
receta ni cambia la economía de la partida. Se basa en la escala y los tipos de
ingrediente de *Dune: Awakening*, adaptados al árbol actual de 48
investigaciones. Las cifras iguales a Dune se señalan como referencia; todas
deben pasar por un balance con los rendimientos reales de los nodos del mod.
Las recetas del árbol empiezan bloqueadas y se aprenden al investigar. El
material y la estación se comprueban al fabricar; investigar no exige entrar
antes en una zona.

**Conciliación 2026-11-17:** los costes de abajo son hipótesis de balance y
no equivalen a recetas implementadas. WorldGen 0.4.1 solo coloca fuel cells,
cobre e hierro; no coloca aluminio, carbono, eritrita ni chatarra como nodo
propio. El [plan maestro](DESERT_SURVIVAL_MASTER_PLAN_2026-11-17.md) fija la
escala de cutterray T0 improvisado (cobre), T1 (hierro), T2
(carbono/eritrita), T3 (basalto/aluminio), T4–T6. Los nombres «cutterray tier
1/2/3» de la versión anterior eran etiquetas heredadas. La correspondencia
vigente conserva los IDs: `CN-0.2` = T0, `CN-1.2` = T1 y `CN-2.2` = T2.
El T3 requiere un nodo futuro posterior. Faltan las matrices exactas de
rendimiento/energía y las recetas reales.
Build Selector 0.6.2 revela estaciones investigadas en su selector, pero cobra
basalto vanilla temporal; el fabricador y las refinerías no procesan aún.

## Referencias y criterios

La wiki comunitaria documenta ejemplos de escala: extractor improvisado
**20 chatarra**, fabricador **75 chatarra**, refinería pequeña de mineral
**90 chatarra**, química pequeña **45 cobre**, cutterray Mk1 **6 cobre +
4 generadores EMF**, Karpov 38 **8 cobre + 2 piezas mecánicas** y cuerpo Kirab
**18 hierro + 15 tejido + 7 fibra**. Nuestra nomenclatura de tier no coincide
siempre con los Mk del juego. Son referencias de diseño, no un volcado literal
de recetas; una actualización de Dune podría cambiar sus cifras.

Fuentes de esos ejemplos: [extractor](https://awakening.wiki/Improvised_Blood_Extractor),
[fabricador](https://awakening.wiki/Module:Buildables/data/placeables/fabricators),
[refinerías](https://awakening.wiki/Buildables/en),
[cutterray](https://awakening.wiki/Cutteray_Mk1),
[Karpov](https://awakening.wiki/Karpov_38) y
[Kirab](https://awakening.wiki/Kirab_Stillsuit_Body).

## Materiales y zonas

La distribución siguiente es **propuesta para nuestro mapa**, no una afirmación
sobre la distribución de recursos de Dune. Las zonas 1–4 conservan los valores
provisionales de calor ya acordados: 1, 2, 0 y 3. La zona 3 puede ser un
cañón/mina sombreada: menos calor ambiental, enemigos y extracción más difíciles.

| Zona | Recurso exclusivo o principal | Recursos compartidos y fuentes secundarias | Función en la progresión |
| --- | --- | --- | --- |
| 1, refugio y ruinas (calor 1) | Chatarra recuperada y piedra común propuestas | Fibra vegetal, arena, agua escasa y piezas mecánicas propuestas; fuel cells colocadas actualmente | Equipo básico; cutterray improvisado T0 canónico. |
| 2, dunas de cobre (calor 2) | Nodos de cobre colocados | Chatarra, fibra y componentes eléctricos propuestos; fuel cells colocadas | Cobre refinado y preparación del cutterray T1 canónico. |
| 3, cañón/minas de hierro (calor 0) | Nodos de hierro colocados | Cobre colocado; otros componentes propuestos | Hierro y preparación de progresión posterior. |
| 4, roca dura (calor 3) | Hierro colocado; aluminio aún no | Bases T3/T4 colocadas; materiales avanzados futuros | Desafío del mapa actual, no equivalente al tier 4 de cutterray. |

Las zonas actuales están configuradas en biomas propios. Los depósitos
de chatarra no están implementados; los nodos jugables son cobre, hierro y
fuel cells. Ninguna receta inicial debe
depender de un drop aleatorio único: los componentes de dungeon aceleran el
progreso, pero tienen una vía de fabricación o una fuente garantizada.

### Vocabulario y rutas de procesado

Las abreviaturas de las tablas son solo para leerlas; no son IDs de assets.
Una unidad de mineral **no** equivale a una unidad de lingote.

| Abrev. | Material | Fuente y transformación propuesta |
| --- | --- | --- |
| `CH` | Chatarra | Restos y nodos de salvamento en zona 1; también ruinas posteriores. |
| `FV` | Fibra vegetal | Plantas de oasis y zonas protegidas, desde zona 1. |
| `PC` | Piedra común | Rocas desde zona 1; material estructural, sin cutterray. |
| `AF` | Arena fina | Dunas desde zona 2; se recoge a mano en poca cantidad o con compactador más adelante. |
| `CU` | Lingote de cobre | Refinería de mineral: **4 menas de cobre → 1 CU**, como la refinería pequeña de Dune. Mena exclusiva de zona 2. |
| `HI` | Lingote de hierro | Refinería: **5 menas de hierro + 25 ml de agua → 1 HI**, como la pequeña de Dune. Mena exclusiva de zona 3. |
| `SI` | Silicona | Refinería química: **5 AF + 50 ml de agua → 1 SI**, inspirada directamente en Dune. |
| `TF` | Tejido filtrante | Fabricador: **5 FV + 1 CH → 1 TF**. Sustituto MVP del micro-sandwich fabric; también puede aparecer en loot. |
| `PM` | Piezas mecánicas | Fabricador: **4 CH → 1 PM**; también en ruinas y bases. Esta receta se aprende con Fabricador. |
| `CE` | Condensador eléctrico | Fabricador: **2 CU + 1 SI → 1 CE**; también en bases de zona 2+. Equivalente simplificado del particle capacitor. |
| `GE` | Generador EMF | Fabricador: **2 CU + 1 PM + 1 SI → 1 GE**; también loot de zona 2+. |
| `AH` | Actuador Holtzman | Fabricador: **2 HI + 1 CE + 1 SI → 1 AH**; también recompensa garantizada en una base de zona 3. |
| `H2O` | Agua potable, en ml | Flask, literjon, purificador, recolector o cisterna; se consume por volumen, no por item lleno. |

Las recetas de componentes `TF`, `PM`, `CE`, `GE` y `AH` se aprenden con
**Fabricador**, que es gratuito pero requiere pulsar su nodo. Sus ingredientes
siguen bloqueando de forma natural los componentes posteriores. `SI` se aprende
con **Refinería química pequeña**; `CU` y `HI` con **Refinería pequeña de
mineral**. Para evitar un arranque circular, la refinería de mineral solo pide
chatarra, y el fabricador se ensambla en el inventario.

## Recetas sugeridas por investigación

Cada fila rinde **un objeto** salvo que indique lote o varias piezas. Las
cuatro piezas de un set se fabrican por separado. `Campo` significa
fabricación personal; `F` es Fabricador. Los procesos de agua y mineral se
ejecutan en su propia máquina. Los costes de edificios son por colocación.

### Disciplina del agua

| Investigación → salida | Coste propuesto | Dónde | Base de la adaptación |
| --- | --- | --- | --- |
| Kit básico de exanguinación → extractor | 20 CH | Campo | Igual al extractor improvisado de Dune. |
| Purificador de sangre → máquina | 70 CH | F | Igual al purificador básico de Dune. Su proceso consume sangre y entrega agua; rendimiento pendiente. |
| Destiltraje tier 1 → pechera | 8 CU + 3 TF + 8 FV | F | Dune usa 3 tejidos + 8 fibras en el cuerpo Scavenger; añadimos cobre para ligar nuestro tier a zona 2. |
| Destiltraje tier 1 → capucha | 4 CU + 1 TF + 3 FV | F | Proporción de la máscara Scavenger, con cobre añadido. |
| Destiltraje tier 1 → guantes | 3 CU + 1 TF + 3 FV | F | Pieza pequeña del mismo set. |
| Destiltraje tier 1 → botas | 4 CU + 1 TF + 3 FV | F | Proporción de las botas Scavenger, con cobre añadido. |
| Literjon → recipiente vacío de 1.000 ml | 14 CU + 2 TF | F | Coste y capacidad de la receta básica de Dune. |
| Cisterna → depósito de base | 24 CH + 8 CU + 4 SI | F | Adaptación de depósito fijo; requiere cobre y química. |
| Destiltraje Kirab → pechera | 18 HI + 15 TF + 7 FV | F | Igual al cuerpo Kirab de Dune; en el mod es pechera. |
| Destiltraje Kirab → capucha | 9 HI + 8 TF + 4 FV | F | Igual a la máscara Kirab. |
| Destiltraje Kirab → guantes | 6 HI + 6 TF + 3 FV | F | Igual a los guantes Kirab. |
| Destiltraje Kirab → botas | 8 HI + 6 TF + 3 FV | F | Igual a las botas Kirab. |
| Recolector de rocío → máquina | 14 HI + 6 SI + 4 CE | F | Propuesta: equipo de agua lento de zona 3, sin fuente infinita a alta velocidad. |
| Extractor de sangre → extractor mejorado | 10 HI + 4 GE + 3 SI | F | Propuesta: metal y electrónica en vez de una mejora gratuita del extractor básico. |
| Saco de sangre → recipiente vacío | 6 HI + 12 TF + 6 FV | F | Adaptación del saco mediano de Dune. |
| Purificador mejorado → máquina | 50 HI + 16 SI + 20 PM | F | Dune usa 50 hierro, 16 silicona y maquinaria compleja; PM sustituye esta última en MVP. |

El **flask de agua** ya existe como prototipo, pero no tiene investigación
propia. Propuesta de receta de arranque: `5 CH + 4 FV`, en Campo, capacidad
500 ml. Rellenarlo es una transferencia de agua, no volver a fabricar el flask.

### Combate

| Investigación → salida | Coste propuesto | Dónde | Base de la adaptación |
| --- | --- | --- | --- |
| Armadura de chatarra → pechera / capucha / guantes / botas | Respectivamente `12 CH + 8 FV` / `8 CH + 5 FV` / `6 CH + 4 FV` / `8 CH + 5 FV` | Campo | Set ligero de entrada; no hay armadura ligera/pesada separada. |
| Cuchillo de chatarra | 3 CH | Campo (`Fieldcraft/Tools`) | Receta real de `CO-0.2`, con conocimiento requerido. |
| Pistola Maula tier 0 → pistola | 15 CH | F | Igual a la Maula improvisada de Dune. |
| Pistola Maula tier 0 → 30 dardos ligeros | 5 CH | Campo o F | Escala del lote de dardos de Dune; sirve para todas las Maula del MVP. |
| Healkit tier 0 → vendaje básico | 5 FV | Campo | Consumible simple; sin materiales de zona posterior. |
| Armadura tier 1 → pechera / capucha / guantes / botas | `10 CU + 4 TF` / `6 CU + 2 TF` / `5 CU + 2 TF` / `6 CU + 2 TF` | F | Set de cobre simplificado. |
| Kindjal tier 1 → hoja de cobre | 4 CU + 3 TF + 3 FV | F | Dune usa 4 cobre y fibras compuestas para el Kindjal. |
| Pistola Maula tier 1 → pistola | 5 CU + 2 PM | F | Igual a la Maula básica de cobre de Dune. |
| Karpov 38 tier 1 → rifle | 8 CU + 2 PM | F | Igual al Karpov 38 básico de Dune. |
| Karpov 38 tier 1 → 20 cartuchos de rifle | 3 CU + 2 PM | F | Lote propuesto; munición independiente de la Maula. |
| Armadura tier 2 → pechera / capucha / guantes / botas | `14 HI + 5 TF` / `8 HI + 3 TF` / `6 HI + 2 TF` / `8 HI + 3 TF` | F | Mismo set único, ahora de hierro. |
| Escudo Holtzman → equipo | 15 HI + 10 CE + 10 AH | F | Escala del escudo Mk2 de Dune. |
| Kindjal tier 2 → hoja de hierro | 7 HI + 5 TF + 3 FV + 6 PM | F | Dune pide 7 hierro, fibras y piezas de hoja; PM simplifica las últimas. |
| Espada tier 2 → espada | 10 HI + 4 TF + 3 FV + 6 PM | F | Basada en la espada estándar de hierro. |
| Pistola Maula tier 2 → pistola | 5 HI + 7 PM + 3 SI | F | Simplificación de la Maula estándar de hierro. |
| Karpov 38 tier 2 → rifle | 9 HI + 4 PM + 3 SI | F | Simplificación del Karpov estándar de hierro. |

### Construcción y fabricación

| Investigación → salida | Coste propuesto | Dónde | Base de la adaptación |
| --- | --- | --- | --- |
| Power Pack básico → batería | 1 CH | Campo | Igual al improvisado de Dune; regeneración de energía por definir. |
| Cutterray improvisado T0 (`CN-0.2`) | 2 CH | Campo | Coste sugerido; acceso canónico a chatarra, granito y cobre. |
| Fabricador → banco | 75 CH | Campo; se coloca | Igual al fabricador general de Dune. |
| Refinería pequeña de mineral → máquina | 90 CH | F; se coloca | Igual a la pequeña de Dune. Sus procesos desbloquean CU y HI. |
| Refinería química pequeña → máquina | 45 CU | F; se coloca | Igual a la pequeña de Dune; habilita SI. |
| Power Pack tier 2 → batería | 5 CU + 3 CE | F | Dune usa 5 cobre + 3 capacitores para Mk1. |
| Cutterray T1 (`CN-1.2`) | 6 CU + 4 GE | F | Coste sugerido de Mk1; abre hierro. |
| Cofre → bloque contenedor | 12 CU + 4 PM | F; se coloca | Adaptación: se movió a cobre para el MVP; el cofre de Dune pide hierro. |
| Power Pack tier 3 → batería | 8 HI + 5 CE | F | Dune usa 8 hierro + 5 capacitores para Mk2. |
| Cutterray T2 (`CN-2.2`) | 10 HI + 7 GE | F | Coste sugerido de Mk2; abrirá carbono/eritrita. T3 para basalto/aluminio requiere otro nodo y coste aún sin definir. |
| Compactador estático → herramienta | 10 HI + 7 GE | F | Igual al compactador de Dune, pero sin recompensa AQL. |
| Estación de reparación → banco | 20 HI + 10 PM + 5 SI | F; se coloca | Propuesta para cerrar el circuito de mantenimiento. El coste de cada reparación depende del daño. |

### Exploración

| Investigación → salida | Coste propuesto | Dónde | Base de la adaptación |
| --- | --- | --- | --- |
| Prismáticos → herramienta | 15 CH | Campo | Igual a los prismáticos de Dune. |
| Luz portátil → lámpara | 15 CH | Campo | Escala del Glowtube de Dune. |
| Baliza de reaparición → bloque | 20 CH | Campo; se coloca | Igual al beacon de Dune; la regla de respawn sigue por diseñar. |
| Cinturón suspensor pasivo → accesorio | 8 CU + 3 CE | F | Escala del cinturón pasivo de Dune, simplificado a reducción de caída. |
| Kit de topografía → herramienta | 2 CU + 1 GE | F | Basado en el lanzador de sonda; aquí marca POI sin sondas complejas. |

### Vehículos

| Investigación → salida | Coste propuesto | Dónde | Base de la adaptación |
| --- | --- | --- | --- |
| Moto de arena tier 1 → moto desplegable | 34 CU + 12 PM + 6 CE + 4 SI | F | En Dune se fabrican chasis, motor, PSU y ruedas por separado; aquí se agrupan en un vehículo inicial. |
| Pistola de recuperar vehículos → herramienta | 6 CU + 2 CE | F | Igual a la herramienta de recuperación de Dune. |
| Ruedas mejoradas → par | 12 HI + 8 PM + 4 SI | F; instalar en moto | Adaptación de los treads de hierro. |
| Motor mejorado → módulo | 18 HI + 10 PM + 6 CE | F; instalar en moto | Más caro que el motor básico de cobre; mejor aceleración. |
| Disipador mejorado → módulo | 12 HI + 8 SI + 5 CE | F; instalar en moto | Sustituye la PSU/disipación de Dune por un módulo único. |
| Módulo de maletero → módulo | 23 HI + 15 PM | F; instalar en moto | Escala del inventario Mk2 de Dune. |
| Módulo de turbo → módulo | 23 HI + 15 CE + 15 AH | F; instalar en moto | Escala del booster Mk2 de Dune; exige motor mejorado. |

## Estaciones e infraestructura que hay que implementar

| Sistema | Función MVP | Desbloqueo y dependencia |
| --- | --- | --- |
| Fabricación personal (`Campo`) | Recetas iniciales y kit de arranque. No es bloque. | Disponible como interfaz de inventario; cada receta sigue bloqueada hasta investigar. |
| Fabricador general | Equipo, herramientas, piezas y componentes; categorías internas Agua, Combate, Construcción, Exploración y Vehículos. | Investigación gratuita Fabricador; ensamblaje con 75 CH en Campo. Un solo banco sustituye los cuatro fabricadores especializados de Dune durante el MVP. |
| Refinería pequeña de mineral | CU y HI a partir de mena; cola y consumo de agua cuando corresponde. | Investigación Refinería pequeña de mineral; 90 CH. |
| Refinería química pequeña | SI a partir de arena fina y agua; futuros compuestos. | Investigación Refinería química pequeña; 45 CU. |
| Purificador de sangre | Sangre → agua potable; depósito de entrada y salida. | Investigación Purificador de sangre; 70 CH. |
| Purificador mejorado | Misma transformación, mejor rendimiento/velocidad. | Investigación Purificador mejorado; requiere HI. |
| Estación de reparación | Reparar armas, herramientas, armadura y módulos con coste proporcional. | Investigación Estación de reparación; requiere HI. |
| Cisterna y recolector de rocío | Almacenamiento y producción lenta de agua. Son máquinas funcionales, aunque no bancos de crafteo generales. | Sus investigaciones respectivas. |
| Generador básico | Alimenta máquinas y recarga Power Packs dentro de la base; depósito de combustible. | Propuesta: receta auxiliar `45 CH` incluida al investigar Fabricador, sin nodo nuevo. Necesita contrato de energía antes de implementarse. |

Una futura estación de vehículos puede separar la fabricación y el ensamblaje
de módulos. Para el MVP, el **Fabricador general** produce la moto y sus piezas;
se instalan al interactuar con una moto propia parada. Esto evita introducir
un banco sin nodo ni receta de acceso. También evita exigir estaciones de
armaduras/armas especializadas antes de que exista suficiente contenido.

## Construcción de bases en Hytale

La referencia de Dune es construir pieza a pieza con herramienta y, después,
usar planos como proyección que aún requiere aportar cada material. [Funcom
describe ese flujo](https://duneawakening.com/news/building-in-dune-awakening-claim-your-piece-of-arrakis/).

| Opción | Ventaja | Problema para nuestro modo |
| --- | --- | --- |
| Prefab por sección de pared, suelo o puerta | Permite autorar una pieza de varios bloques y colocarla de una vez. | Exige registrar la instancia y validar toda su huella para girar, cobrar, proteger y retirar sin afectar a vecinos. |
| Bloques sueltos vanilla | Funciona ya con inventario, rotación y persistencia nativos. | Aspecto menos propio y paredes demasiado gruesas si solo se usan cubos completos. |
| Bloques propios con modelo y colisión | Integración nativa por celda; aspecto industrial, puertas interactivas, reparación y costes controlables. | Requiere modelos, texturas y variantes de esquina, borde, ventana y puerta. |
| Entidad por pieza | Animación y geometría libre. | Más estado, sincronización y coste de red; poco apropiada para miles de paredes. |

**Recomendación revisada:** el kit inicial usa **prefabs estructurales de varios
bloques**: cimiento, suelo, pared, esquina, marco, puerta, techo y escalera.
Cada entrada del selector coloca una sección completa; sus bloques internos
pueden ser vanilla o assets propios con `CustomModel`. Por ejemplo, un suelo
podría medir 3 × 3 celdas y una pared 3 × 3, sujeto a una prueba de encaje.
Hytale permite `CustomModel` en bloques y `PrefabUtil` para pegar estructuras.
[Referencia de bloques](https://docs.hytale.com/assets/item/block/blocks/),
[API de prefabs](https://docs.hytale.com/api/com/hypixel/hytale/server/core/util/PrefabUtil).

La **herramienta de construcción** será la interfaz de selección, vista previa,
rotación, colocación, retirada y reparación de esas secciones. Propuesta de
arranque: receta `20 CH` en Campo al investigar Fabricador, sin sumar un nodo
nuevo al árbol. Es una **propuesta pendiente de decisión**, porque antes se
había reservado el kit/pistola de construcción para discusión aparte. Hasta
tener la herramienta, los prefabs pueden probarse en un save aislado con las
herramientas creativas; eso no constituye aún el flujo de supervivencia. Las
máquinas y contenedores se colocan como bloques funcionales separados. Coste
inicial por sección para balance: cimiento `8 PC +
2 CH`, suelo `6 PC + 2 CH`, pared `4 PC + 2 CH`, puerta `4 CH + 2 PM` y techo
`5 PC + 2 CH`; versiones reforzadas añadirían CU o HI.

Las **habitaciones completas**, puestos avanzados, módulos prediseñados y
planos de base también usan prefabs, pero agrupan varias secciones. Un plano
guarda la disposición y crea una proyección; el servidor valida espacio,
permisos y materiales por sección antes de confirmar, sin duplicar recursos ni
borrar construcciones ajenas. El
prototipo existente `More Triggers/PasteRandomPrefab` demuestra pegado y giro de
salas, pero sus reservas y efectos son herramientas de mapa, no todavía un
sistema de construcción de supervivencia. Para el MVP bastan secciones
estructurales y uno o dos módulos predefinidos; copiar bases arbitrarias puede
esperar. El diseño del selector y el ciclo de vida de cada sección están en
[`BUILD_SELECTOR_RESEARCH.md`](BUILD_SELECTOR_RESEARCH.md).

## Validaciones antes de convertir el borrador en assets

1. Cerrar qué materiales serán items nuevos y cuáles reutilizarán recursos
   vanilla. No cambiar los IDs de prototipos existentes sin migración.
2. Medir chatarra/minuto, cobre/minuto e hierro/minuto con el cutterray y ajustar
   estos costes a tiempos objetivo. Los 75/90 de Dune pueden ser excesivos si
   hay pocos nodos en el mapa cerrado.
3. Crear un registro investigación → recetas → estación → material más alto.
   Rechazar en tests recetas propias sin investigación, sin banco o con un
   material de zona posterior al tier previsto, salvo excepción documentada.
4. Probar una cadena completa: chatarra → fabricador → refinería → cobre →
   cutterray T1 canónico → hierro, con personaje nuevo y progreso guardado.
