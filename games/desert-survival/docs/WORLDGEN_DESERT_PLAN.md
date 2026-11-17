# Borrador de generación del mapa desértico

Estado 2026-11-16: el pack `prototypes/desert-worldgen` 0.2.0 esta instalado
y el usuario confirmo en `dune2` las cuatro zonas, los nodos y los prefabs
vanilla. Este documento conserva el plan original; el estado operativo esta
en `../STATUS.md` y la implementacion en el README del prototipo. El
laboratorio `experiments/asset-packs/worldgen-v2-lab` esta fijado a
0.7.0-pre.1 y no se debe copiar sin validacion contra 0.7.7.

## Decisión propuesta

Crear una **instancia aislada** con `WorldGen.Type = HytaleGenerator` que apunte
a un `WorldStructure` propio. Su campo `Density` reparte cuatro biomas por
zonas diseñadas a mano: una distancia al centro o a puntos concretos, deformada
ligeramente con ruido para evitar fronteras circulares perfectas. Los
delimitadores `Biomes` eligen cada zona; `DefaultTransitionDistance` suaviza
sus bordes. El mapa puede conservar una seed fija y una zona inicial definida.

Los cuatro biomas comparten una función importada de terreno desértico como
base: dunas, roca y cañones procedurales. Cada bioma ajusta el relieve local,
las capas del `MaterialProvider`, el ambiente y las distribuciones de `Props`.
Así las zonas están diseñadas en posición y progresión, pero las formas
individuales siguen siendo procedurales.

| Zona | Relieve y materiales | Recursos iniciales propuestos |
| --- | --- | --- |
| 1 | Refugio, dunas bajas y ruinas | Chatarra; células de combustible escasas |
| 2 | Dunas abiertas y afloramientos | Cobre; células de combustible más frecuentes |
| 3 | Cañones y minas sombreadas | Hierro y componentes de bases enemigas |
| 4 | Desierto profundo y roca dura | Aluminio y materiales avanzados futuros |

La abundancia exacta de células de combustible queda pendiente de balance.
Los nodos de recurso actuales son bloques de un único anclaje con modelo grande.
La propuesta es colocarlos como `Manual` Props de un bloque, con posiciones
procedurales, `Locator` y `Pattern` para que se apoyen en suelo válido. Las
bases enemigas y otros espacios diseñados a mano usarían `Prefab` Props. Los
materiales del subsuelo se asignan en `MaterialProvider`; los nodos visibles se
colocan mediante Props para controlar separación, rareza y accesibilidad.

El `WorldStructure` por sí mismo no convierte un mundo infinito en un mapa
cerrado. Hay que definir una frontera de juego: una banda exterior hostil o
intransitable y un control de posición del servidor. No inferir límites solo
por el aspecto del terreno.

## Orden de implementación

1. Instancia de prueba con un único bioma desértico, terreno y spawn.
2. Cuatro biomas con la misma base de terreno y capas de materiales distintas.
3. Distribución de nodos por zona, empezando con cobre, hierro y células de
   combustible; comprobar que cada nodo se puede alcanzar y extraer.
4. Prefabs de ruinas y bases enemigas con reglas de distancia entre ellas.
5. Frontera del mapa, clima, calor, NPCs y puntos de interés.
6. Pruebas con la misma seed en chunks nuevos o `/viewport --radius 5`, y
   después con varias seeds para detectar distribuciones degeneradas.

Referencias de 0.7.7: [crear biomas e instancias](https://docs.hytale.com/creating-content/world-generation/how-to-edit-and-create-biomes/),
[World Structure](https://docs.hytale.com/hytale-generator/world-structure/),
[Density](https://docs.hytale.com/hytale-generator/density/),
[Props](https://docs.hytale.com/hytale-generator/prop/) y
[rendimiento](https://docs.hytale.com/creating-content/world-generation/performance-optimization/).
