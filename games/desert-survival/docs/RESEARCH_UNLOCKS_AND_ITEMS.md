# Investigaciones y objetos del MVP

Propuesta de diseño, 2026-11-14. El catálogo actual contiene 48 investigaciones
en cinco sectores y tres bandas: chatarra, cobre y hierro. **Todas las recetas
del árbol empiezan bloqueadas.** Cada investigación habilitará sus recetas,
no entregará automáticamente objetos. Las investigaciones
de coste cero también se activan pulsando su nodo. Se pueden investigar nodos
de una banda futura si se cumplen sus prerrequisitos y se dispone de Intel;
conseguir el material necesario será lo que impida fabricar el objeto antes de
visitar la zona correspondiente.

Los ingredientes, la distribución por zonas y las estaciones propuestos para
cada salida están en [Materiales, recetas y construcción](CRAFTING_MATERIALS_ZONES_AND_BUILDING.md).

**Estado real:** el prototipo de investigación guarda los desbloqueos y gasta
Intel, pero todavía no conecta esos desbloqueos con recetas. Solo existen como
prototipos jugables el destiltraje de tier 1 y el cutterray de tier 3, además
del flask y los nodos de cobre y hierro. Las demás entradas de las tablas son
objetos por diseñar o fabricar. Los identificadores internos de investigación
se conservan en código y en el guardado para no romper partidas; la interfaz y
esta lista muestran nombres.

## Disciplina del agua

| Banda | Investigación | Objetos que desbloquea | Implementación propuesta |
| --- | --- | --- | --- |
| Chatarra | Kit básico de exanguinación | Extractor improvisado y bolsa hemática pequeña vacía | Ambas recetas están implementadas en Research 0.2.6: 20 de chatarra para el extractor; 2 de plástico y 3 de fibra vegetal para la bolsa. La extracción de cadáveres queda pendiente; la bolsa llena de 250 ml es de prueba creativa. |
| Chatarra | Purificador de sangre | Purificador de sangre | Máquina colocable con entrada de sangre, agua en salida y tiempo de proceso; receta de conversión sencilla para el MVP. |
| Cobre | Destiltraje tier 1 | Pechera, capucha, guantes y botas de destiltraje tier 1 | Reutilizar el prototipo de cuatro piezas: 5 puntos de resistencia térmica cada una; la pechera lleva bolsillo de 500 ml y recupera 1 ml por cada 3 ml realmente perdidos. |
| Cobre | Literjon | Literjon; versión vacía | Recipiente portátil recargable de mayor capacidad que el flask; guardar el volumen de agua en los datos del objeto y transferir una dosis al usarlo. Capacidad exacta pendiente de balance. |
| Cobre | Cisterna | Cisterna | Bloque de base con inventario/depósito persistente de agua y acciones de llenar o retirar mediante recipientes. |
| Hierro | Destiltraje Kirab | Pechera, capucha, guantes y botas Kirab | Segundo set sobre el mismo sistema térmico; mantener bolsillo de 500 ml y aumentar la recuperación y resistencia por pieza. Cifras por equilibrar. |
| Hierro | Recolector de rocío | Recolector de rocío | Máquina colocable que acumula agua lentamente si cumple condiciones ambientales; transferirla a recipientes/cisterna. |
| Hierro | Extractor de sangre | Extractor de sangre mejorado | Variante de la herramienta básica que extrae más sangre por cadáver o reduce su tiempo de uso; una sola extracción sigue agotando la fuente. |
| Hierro | Saco de sangre | Saco de sangre grande; versión vacía | Contenedor portátil para varias unidades de sangre, utilizable como entrada de un purificador; ID reservado `Blood_Bag_Large_Empty_Prototype`. |
| Hierro | Purificador mejorado | Purificador de sangre mejorado | Máquina de mayor rendimiento o velocidad que acepta los mismos recursos y recipientes que la básica. |

El flask de agua de 500 ml ya existe en Vitals como recurso básico. El
[borrador de crafteos](CRAFTING_MATERIALS_ZONES_AND_BUILDING.md) propone una
receta inicial de Campo; la recarga desde fuentes de agua aún requiere diseño.
No requiere añadir otro nodo al árbol.

## Combate

| Banda | Investigación | Objetos que desbloquea | Implementación propuesta |
| --- | --- | --- | --- |
| Chatarra | Armadura de chatarra | Set de armadura de chatarra | Cuatro piezas equipables que heredan ranuras y animaciones de armadura nativa; una estadística defensiva compartida, sin clases ligera/pesada. |
| Chatarra | Cuchillo de chatarra | Cuchillo de chatarra | Arma cuerpo a cuerpo de hoja corta; usa la familia de interacción de dagas y una receta de 3 chatarra en Fieldcraft/Tools. |
| Chatarra | Pistola Maula tier 0 | Pistola Maula básica; munición básica | Arma de proyectil lenta con cargador pequeño; crear un proyectil/munición común para las tres Maula y graduar daño por tier. |
| Chatarra | Healkit tier 0 | Healkit básico | Consumible de curación con canalización corta, interrumpible al recibir daño; respetar siempre el límite de vida impuesto por hidratación. |
| Cobre | Armadura tier 1 | Set de armadura de cobre | Cuatro piezas sobre la misma familia de armadura; mayor defensa y receta con cobre procesado. |
| Cobre | Kindjal tier 1 | Kindjal de cobre | Misma interacción que el básico, con estadísticas y modelo de cobre. |
| Cobre | Pistola Maula tier 1 | Pistola Maula de cobre | Compartir proyectil y lógica de recarga con tier 0; mejorar daño/fiabilidad. |
| Cobre | Karpov 38 tier 1 | Karpov 38 básico; munición de rifle | Rifle de proyectil con alcance superior a la Maula y recarga propia. Su munición se comparte con el Karpov tier 2. |
| Hierro | Armadura tier 2 | Set de armadura de hierro | Cuatro piezas de defensa mayor, sin bifurcación ligera/pesada. |
| Hierro | Escudo Holtzman | Escudo Holtzman equipable | Efecto defensivo temporal con reserva de energía y recarga; la interacción detallada con proyectiles y melee queda para un pase de balance. |
| Hierro | Kindjal tier 2 | Kindjal de hierro | Evolución de la familia Kindjal con nuevo aspecto y estadísticas. |
| Hierro | Espada tier 2 | Espada de hierro | Arma de alcance y golpe mayores, a cambio de una cadencia menor que Kindjal. |
| Hierro | Pistola Maula tier 2 | Pistola Maula de hierro | Tercera variante de la misma familia de arma; conserva munición y controles. |
| Hierro | Karpov 38 tier 2 | Karpov 38 mejorado | Variante de rifle con el mismo tipo de munición y mejores estadísticas. |

## Construcción y fabricación

| Banda | Investigación | Objetos que desbloquea | Implementación propuesta |
| --- | --- | --- | --- |
| Chatarra | Power Pack básico | Power Pack básico | Batería portátil recargable para herramientas; primero definir capacidad, consumo y recarga comunes antes de exigir energía al cutterray. |
| Chatarra | Cutterray tier 1 | Cutterray tier 1 | Reutilizar el sistema de rayo y extracción de cinco segundos; limitarlo a chatarra y cobre, con rendimiento de tier 1. |
| Chatarra | Fabricador | Fabricador | Banco colocable que agrupa recetas investigadas; la receta de cada salida consulta el desbloqueo del jugador y sus ingredientes reales. |
| Chatarra | Refinería pequeña de mineral | Refinería pequeña de mineral | Bloque con entradas de mena y combustible/energía, proceso temporizado y salida de material refinado. |
| Chatarra | Refinería química pequeña | Refinería química pequeña | Misma base de máquina, con recetas químicas separadas: agua procesada, componentes y consumibles. |
| Cobre | Power Pack tier 2 | Power Pack tier 2 | Segunda batería con más capacidad; compatibilidad con herramientas anteriores. |
| Cobre | Cutterray tier 2 | Cutterray tier 2 | Misma lógica de extracción; añade hierro a los nodos válidos y aumenta rendimiento sin reducir los cinco segundos. |
| Cobre | Cofre | Cofre de base | Bloque contenedor persistente; reutilizar primero la interacción de inventario nativa y añadir permisos de base más adelante. |
| Hierro | Power Pack tier 3 | Power Pack tier 3 | Tercera batería, con capacidad superior y el mismo sistema de carga. |
| Hierro | Cutterray tier 3 | Cutterray tier 3 | Adaptar el prototipo ya jugable a receta y consumo de energía; añade aluminio y mantiene el rayo, los pulsos y la extracción de cinco segundos. |
| Hierro | Compactador estático | Compactador estático | Herramienta que transforma una zona pequeña de arena en superficie estable durante un tiempo; sin recompensa AQL. Definir permisos para no alterar bases ajenas. |
| Hierro | Estación de reparación | Estación de reparación | Banco colocable que consume componentes para restaurar durabilidad de equipo compatible, con coste proporcional al desgaste. |

El kit básico de construcción y la pistola de construcción siguen fuera de este
catálogo hasta cerrar su diseño. El fabricador no presupone que esos objetos ya
existan.

## Exploración

| Banda | Investigación | Objetos que desbloquea | Implementación propuesta |
| --- | --- | --- | --- |
| Chatarra | Prismáticos | Prismáticos | Herramienta de uso mantenido que amplía visión y permite identificar un punto lejano; empezar por zoom sin marcador automático. |
| Chatarra | Luz portátil | Luz portátil | Objeto equipable/activable con luz vinculada al jugador y consumo del Power Pack básico. |
| Chatarra | Baliza de reaparición | Baliza de reaparición | Bloque que propone un punto de respawn personal tras activarse; alcance, coste y restricciones pendientes de la discusión específica. |
| Cobre | Cinturón suspensor pasivo | Cinturón suspensor | Accesorio equipable que reduce daño de caída mediante una sola regla de evento; sin vuelo ni salto especial en el MVP. |
| Cobre | Kit de topografía | Kit de topografía | Herramienta que registra un marcador personal de interés en el mapa; límites y modo de compartir quedan por diseñar. |

## Vehículos

| Banda | Investigación | Objetos que desbloquea | Implementación propuesta |
| --- | --- | --- | --- |
| Cobre | Moto de arena tier 1 | Moto de arena básica | Un objeto de despliegue crea una entidad montable persistente con dueño, salud y energía; empezar con conducción simple y sin pasajero. |
| Cobre | Pistola de recuperar vehículos | Pistola de recuperación | Herramienta sobre la moto propia para devolverla a una posición segura cercana o recogerla, con bloqueo durante combate y cooldown. |
| Hierro | Ruedas mejoradas | Módulo de ruedas | Pieza instalable en la moto que aumenta tracción y estabilidad; reemplaza el módulo anterior sin crear otra moto. |
| Hierro | Motor mejorado | Módulo de motor | Pieza instalable que mejora aceleración y velocidad a cambio de más consumo/temperatura. |
| Hierro | Disipador mejorado | Módulo de disipador | Pieza instalable que reduce calor generado por el motor. |
| Hierro | Módulo de maletero | Módulo de maletero | Pieza instalable que añade inventario persistente vinculado a la moto. |
| Hierro | Módulo de turbo | Módulo de turbo | Pieza instalable que permite un impulso breve con energía/cooldown; requiere motor mejorado también al instalarse. |

## Contrato de implementación propuesto

1. Un desbloqueo habilita una o varias recetas. Las piezas de un set comparten
   investigación, pero cada pieza es un item fabricable independiente. La
   munición pertenece al mismo desbloqueo que su primera arma; las variantes
   superiores pueden reutilizarla.
2. Los assets de item y receta tendrán IDs propios. Se pueden heredar modelos,
   poses o interacciones vanilla sin modificar IDs de objetos vanilla. Para
   máquinas y vehículos, el objeto fabricado coloca el bloque o entidad que
   conserva su estado.
3. Cada receta declara `KnowledgeRequired: true` y un `BenchRequirement`
   concreto. Al investigar, el plugin registra para ese personaje los IDs de
   item de las salidas principales asociadas al nodo mediante la API nativa de
   crafting (`CraftingPlugin.learnRecipe`). Las variantes de proceso que no
   pasen por ese flujo también necesitarán un control de acceso en el servidor.
   Al entrar en un mundo, sincroniza también los desbloqueos ya guardados, para
   que los personajes existentes no pierdan sus recetas. Al confirmar el craft,
   el servidor vuelve a comprobar conocimiento, ingredientes, banco y espacio
   de salida. Una investigación futura puede estar comprada aunque falte hierro:
   su receta estará aprendida, pero no será fabricable sin ese material.
4. Los valores repetidos (resistencia térmica, defensa, energía, tier de corte,
   capacidad y eficiencia) irán en perfiles de datos por familia, no en una
   clase Java por objeto. Primero cerrar contratos de agua, energía y vehículos;
   después crear assets de las familias y probar una receta completa de extremo
   a extremo antes de rellenar las 48 investigaciones.

La estructura de assets de items y recetas está documentada en las referencias
oficiales de [items](https://docs.hytale.com/assets/item/items/) y
[recetas](https://docs.hytale.com/assets/item/recipes/). El campo nativo
`KnowledgeRequired` no conecta por sí solo con el componente de investigación
de este prototipo; esa comprobación se debe implementar expresamente.

## Vía de fabricación de cada investigación

Esta matriz asigna una ruta a **cada receta propuesta**, sin suponer que las
máquinas o la herramienta de construcción ya estén implementadas. `Campo` es
la fabricación manual inicial; las herramientas improvisadas se hacen desde
el inventario con `Fieldcraft`. Las estaciones investigadas se incorporan al
selector de construcción. El fabricador es el futuro banco para equipo más
avanzado. La pistola de construcción y el kit básico siguen pendientes de
diseño y no son requisitos ocultos de estas recetas.

| Investigación de agua | Dónde se fabrica | Uso o transformación posterior |
| --- | --- | --- |
| Kit básico de exanguinación | Campo | Extrae sangre del cadáver. |
| Purificador de sangre | Selector de construcción | Aparece al investigarlo; procesa sangre en agua. |
| Destiltraje tier 1 | Fabricador, cuatro recetas | Se equipa; la pechera recicla agua. |
| Literjon | Fabricador | Se rellena desde una fuente o cisterna. |
| Cisterna | Fabricador | Se coloca; almacena agua. |
| Destiltraje Kirab | Fabricador, cuatro recetas | Se equipa. |
| Recolector de rocío | Fabricador | Se coloca; produce agua. |
| Extractor de sangre | Fabricador | Extrae sangre con mayor rendimiento. |
| Saco de sangre | Fabricador | Guarda sangre para purificadores. |
| Purificador mejorado | Fabricador | Se coloca; procesa sangre con mejor rendimiento. |

| Investigación de combate | Dónde se fabrica | Uso o transformación posterior |
| --- | --- | --- |
| Armadura de chatarra | Campo, una receta por pieza | Se equipa. |
| Cuchillo de chatarra | Campo (`Fieldcraft/Tools`) | Se equipa; receta ligada a `CO-0.2`. |
| Pistola Maula tier 0 | Campo; munición en Campo | Arma improvisada; munición reutilizada por Maula superiores. |
| Healkit tier 0 | Campo | Se consume respetando el tope de vida. |
| Armadura tier 1 | Fabricador, una receta por pieza | Se equipa. |
| Kindjal tier 1 | Fabricador | Se equipa. |
| Pistola Maula tier 1 | Fabricador | Usa munición Maula ya aprendida. |
| Karpov 38 tier 1 | Fabricador; munición de rifle en Fabricador | Su munición sirve también al Karpov superior. |
| Armadura tier 2 | Fabricador, una receta por pieza | Se equipa. |
| Escudo Holtzman | Fabricador | Se equipa y consume energía. |
| Kindjal tier 2 | Fabricador | Se equipa. |
| Espada tier 2 | Fabricador | Se equipa. |
| Pistola Maula tier 2 | Fabricador | Usa munición Maula ya aprendida. |
| Karpov 38 tier 2 | Fabricador | Usa munición de rifle ya aprendida. |

| Investigación de construcción | Dónde se fabrica | Uso o transformación posterior |
| --- | --- | --- |
| Power Pack básico | Campo | Se recarga en fuente de energía por definir. |
| Cutterray tier 1 | Campo | Herramienta improvisada; usa Power Pack y extrae chatarra y cobre. |
| Fabricador | Selector de construcción | Aparece al investigarlo; prototipo sin fabricación todavía. |
| Generador de energía | Selector de construcción | Aparece con Fabricador; consume células y entrega 100 de potencia. |
| Refinería pequeña de mineral | Selector de construcción | Aparece al investigarla; procesamiento pendiente. |
| Refinería química pequeña | Selector de construcción | Aparece al investigarla; procesamiento pendiente. |
| Power Pack tier 2 | Fabricador | Se recarga; compatible con herramientas anteriores. |
| Cutterray tier 2 | Fabricador | Usa Power Pack; añade hierro. |
| Cofre | Fabricador | Se coloca y almacena objetos. |
| Power Pack tier 3 | Fabricador | Se recarga; compatible con herramientas anteriores. |
| Cutterray tier 3 | Fabricador | Usa Power Pack; añade aluminio. |
| Compactador estático | Fabricador | Herramienta de uso sobre arena; no es una estación. |
| Estación de reparación | Fabricador | Se coloca; sus recetas reparan equipo en esa estación. |

| Investigación de exploración | Dónde se fabrica | Uso o transformación posterior |
| --- | --- | --- |
| Prismáticos | Campo | Se usan en mano. |
| Luz portátil | Campo | Se equipa/activa; usa energía. |
| Baliza de reaparición | Fabricador | Se coloca y vincula al personaje; reglas por cerrar. |
| Cinturón suspensor pasivo | Fabricador | Se equipa. |
| Kit de topografía | Fabricador | Se usa para marcar lugares; reglas por cerrar. |

| Investigación de vehículos | Dónde se fabrica | Uso o transformación posterior |
| --- | --- | --- |
| Moto de arena tier 1 | Fabricador | El objeto despliega la moto persistente. |
| Pistola de recuperar vehículos | Fabricador | Se usa sobre una moto propia. |
| Ruedas mejoradas | Fabricador | Se instala en moto propia parada. |
| Motor mejorado | Fabricador | Se instala en moto propia parada. |
| Disipador mejorado | Fabricador | Se instala en moto propia parada. |
| Módulo de maletero | Fabricador | Se instala en moto propia parada. |
| Módulo de turbo | Fabricador | Se instala en moto propia parada con motor mejorado. |

Las recetas de **proceso** (refinar mineral, purificar sangre, reparar) estarán
también bloqueadas al inicio. Se aprenden con la misma investigación que
desbloquea la máquina correspondiente y solo se ejecutan dentro de ella. Los
materiales refinados podrán tener varias recetas si más adelante cambia la
fuente o el rendimiento; cada variante debe figurar en el mismo registro de
desbloqueo.

Antes de integrar assets, crear un registro estable de investigación → salidas
con prueba de cobertura: ninguna receta propia puede quedar sin dueño, estación
o `KnowledgeRequired`. Una prueba de partida debe comprobar, para un personaje
nuevo y uno con progreso antiguo: receta invisible/bloqueada al inicio, aparición
tras investigar, imposibilidad de fabricar sin material o banco, y persistencia
tras reiniciar. La fabricación creativa mediante comandos no forma parte de esta
restricción de survival.
