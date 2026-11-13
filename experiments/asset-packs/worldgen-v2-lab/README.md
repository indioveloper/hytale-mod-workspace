# WorldGen V2 Lab

Laboratorio experimental para convertir una descripcion en lenguaje natural en
un bioma o mundo de Hytale WorldGen V2, probarlo como una instancia aislada y
volver con feedback para iterar.

Este proyecto esta fijado a `0.7.0-pre.1`. WorldGen V2 sigue en desarrollo y
los nombres, codecs y nodos pueden cambiar entre pre-releases. No debe
distribuirse como un pack estable ni instalarse en mundos importantes.

## Resultado inicial

La version `0.1.0` contiene una prueba minima llamada
`Raynor_Lab_Rolling_Hills`:

- un `WorldStructure` de un solo bioma;
- terreno de colinas con `SimplexNoise2D`;
- altura compartida mediante el framework decimal `Base`;
- hierba, cuatro bloques de tierra y piedra de fondo;
- una posicion de spawn explicita;
- una instancia creativa que no guarda chunks ni jugadores.

No incluye agua, cuevas ni props a proposito. Primero hay que demostrar que la
cadena completa carga y genera; despues cada sistema se incorpora de forma
aislada para que un fallo visual tenga una causa identificable.

## Jardines del Cielo

La version `0.3.0` anade una segunda iteracion de `Raynor_Sky_Gardens`, ya
centrada en romper la regularidad del primer prototipo y darle lectura de
bioma:

- tres poblaciones independientes de islas pequenas, medianas y grandes, con
  escalas no conmensurables, offsets, probabilidades y jitter propios;
- tamanos desde islotes de unos 12 bloques de ancho hasta masas cercanas a 100,
  con mucha mas separacion vertical y formas aplanadas de manera desigual;
- spawn elegido entre los mismos centros procedurales que crean las islas, sin
  una isla artificial reservada para aparecer;
- FitToHeightMap coloca a cada jugador sobre la superficie natural; la ventana
  finita de candidatos evita enumerar posiciones infinitas y permite en el
  futuro enmascararla por la region de este bioma al fusionarlo con otros;
- doble erosion 3D para romper siluetas, tres variantes de hierba, tres de
  subsuelo y piedra musgosa dispersa;
- algunas islas grandes incorporan una depresion y agua generada por el mismo
  campo de posiciones;
- pradera procedural de hierbas, flores y arbustos mediante bloques Manual y
  Locator; sigue sin existir ningun prefab, puente, esqueleto o landmark.

El spawn es determinista por jugador y seed, pero no esta fijado a una isla
especial. Para comparar esta revision hay que crear una instancia nueva: los
chunks de una prueba anterior no deben usarse como referencia visual.

### Altura infinita en Update 7

Update 7 incorpora la interfaz experimental `IWorldGen.Cubic`: el proveedor
documentado `CubicTest` genera por secciones y puede continuar indefinidamente
en X, Y y Z. Sin embargo, en `0.7.0-pre.1`, el adaptador `Handle` de
`HytaleGenerator` implementa todavia `IWorldGen`, no `IWorldGen.Cubic`, y su
salida usa la columna legacy de 10 secciones (Y 0..319).

Por tanto, los grafos de Positions y Density de WorldGen V2 son matematicamente
3D e ilimitados, pero el adaptador que los entrega al mundo aun solo solicita
esa franja vertical. Los Jardines del Cielo se han mantenido en la ruta V2 para
que sigan siendo editables como Biome. Un mundo realmente infinito en vertical
exige hoy un proveedor Java `IWorldGen.Cubic` propio, que no seria un Biome V2,
o esperar a que Hytale conecte `HytaleGenerator` con la ruta cubica.

## Modelo mental confirmado

WorldGen V2 es principalmente un sistema de assets, no un mod Java. La cadena
minima es:

```text
Server/Instances/<id>/instance.bson
  -> WorldGen.Type = HytaleGenerator
  -> WorldGen.WorldStructure = <id>
     -> Server/HytaleGenerator/WorldStructures/<id>.json
        -> DefaultBiome / delimitadores de Biomes
           -> Server/HytaleGenerator/Biomes/<id>.json
              -> Terrain.Density
              -> MaterialProvider
              -> Props / PropDistribution / Positions
              -> EnvironmentProvider
              -> TintProvider
```

El `WorldStructure` decide que biomas aparecen, como se distribuyen, como
transicionan, donde se puede aparecer y que campos globales comparten. El
`Biome` decide la masa de terreno, los materiales, la decoracion, el ambiente y
el tint. Los assets separados de Density, Positions, Props y
PropDistributions permiten reutilizar subgrafos sin duplicarlos.

El orden de coste y complejidad recomendado es:

1. masa de terreno y spawn;
2. capas de materiales;
3. agua y cuevas;
4. props pequenos;
5. prefabs y landmarks con reglas de exclusion;
6. varios biomas y transiciones;
7. grafos de rios, caminos, cuevas coherentes o estructuras conectadas.

## Flujo prompt -> mundo -> feedback

1. El usuario describe el objetivo libremente o rellena
   [PROMPT_TEMPLATE.md](PROMPT_TEMPLATE.md).
2. Se separa la peticion en forma, materiales, ambiente, distribucion de props,
   landmarks, restricciones de jugabilidad y presupuesto de rendimiento.
3. Se decide si basta un `Biome`, si hace falta un `WorldStructure` multibioma
   o si una parte debe exportarse como Density/Positions/Prop independiente.
4. Se genera una revision del pack, se valida y se empaqueta. La seed del
   `instance.bson` se mantiene fija para comparar cambios equivalentes.
5. Se instala el ZIP o la carpeta y se abre la instancia desde `/instances`.
6. Se prueba con coordenadas y seed conocidas. Capturas y logs vuelven al chat.
7. El feedback se traduce a cambios concretos de amplitud, frecuencia,
   delimitadores, capas, densidad de props o reglas espaciales.
8. La decision y el resultado se anotan en
   [ITERATION_LOG.md](ITERATION_LOG.md).

Cambiar solo la seed no sustituye una comparacion controlada. Para evaluar una
modificacion del grafo se prueba primero con la misma seed; despues se prueban
varias seeds para detectar casos degenerados.

## Herramientas oficiales encontradas en este PC

En la instalacion pre-release actual existen:

```text
%APPDATA%\Hytale\install\pre-release\package\game\latest\Assets.zip
  Server/HytaleGenerator/...

%APPDATA%\Hytale\install\pre-release\package\game\latest\Client\NodeEditor\NodeEditor.exe
%APPDATA%\Hytale\install\pre-release\package\game\latest\Client\NodeEditor\Workspaces\HytaleGenerator Java
```

El workspace del editor describe los nodos y sus pines. `Assets.zip` contiene
los ejemplos ejecutables y plantillas `Basic`, `Default_Flat`, `Default_Void`,
ejemplos de props, erosion, rios y offsets vectoriales. Esas dependencias son
externas: no se copian al repositorio.

Para inventariar de nuevo la instalacion despues de una actualizacion:

```powershell
.\experiments\asset-packs\worldgen-v2-lab\tools\Inspect-InstalledWorldGenV2.ps1
```

El Shared Source canonico esta fuera del repo, en
`..\hytale-shared-source`. La implementacion V2 relevante vive bajo:

```text
HytaleServer/builtin/HytaleGenerator/src/main/java/
```

En particular, `AssetManager` confirma las rutas de los stores, y
`BiomeEditor.setupAssets` confirma la secuencia Biome -> WorldStructure ->
Instance. El comando `/worldgen2 create` (alias `/wg2 create`) abre esa UI,
crea por defecto el pack `User:Biomes`, clona las plantillas `Basic` y abre un
viewport temporal.

## Update 7: incompatibilidades que hay que vigilar

La pre-release `0.7.0-pre.1` cambio varios IDs de nodos Density:

- `SmallerThan` -> `LessThan`;
- `SmallerOrEquals` -> `LessOrEqual`;
- `Equals` -> `Equal`;
- `GreaterOrEquals` -> `GreaterOrEqual`;
- `Select` -> `Selector`.

Tambien elimino `2D` y `YFor2D` de `GradientWarp`. El reemplazo indicado es
una cadena `YOverride -> Cache -> GradientWarp`. Por eso cada pack generado se
fija a una version exacta de pre-release hasta revalidarlo.

## Validar y empaquetar

```powershell
.\experiments\asset-packs\worldgen-v2-lab\tools\Test-WorldGenV2Pack.ps1
.\experiments\asset-packs\worldgen-v2-lab\tools\Test-WorldGenV2Codec.ps1
.\experiments\asset-packs\worldgen-v2-lab\tools\Build-Pack.ps1
```

El ZIP se crea en `.build/dist`. La prueba actual es estructural: valida JSON,
referencias entre instancia, WorldStructure y Biome, el tipo
`HytaleGenerator`, y que el laboratorio no persiste chunks. La segunda prueba
compila un validador pequeno contra el `HytaleServer.jar` de la pre-release y
decodifica el Biome usando sus codecs reales. `WorldStructure.DefaultBiome` usa
un `ContainedAssetCodec` que solo puede resolverse dentro del AssetStore vivo;
por eso sus referencias se comprueban de forma estructural y se completan con
el smoke test dentro del juego. Solo el runtime puede confirmar la semantica
visual de los providers.

## Probar en Hytale

1. Cierra la partida antes de sustituir el pack.
2. Copia el ZIP a los mods globales de la pre-release o a `mods` de un save de
   prueba. No lo uses en un save importante.
3. Arranca la pre-release y habilita `RaynorLabs:WorldGen V2 Lab` si aparece
   desactivado.
4. Entra en un mundo creativo y ejecuta `/instances`.
5. Abre `Raynor_Lab_Rolling_Hills`.
6. Para comparar una revision, sal de la instancia, sustituye/reload el pack y
   crea una instancia nueva. No juzgues chunks conservados de una revision
   anterior.

Como alternativa visual, ejecuta `/worldgen2 create`, usa `Basic` como
plantilla y abre el Biome resultante desde el Content Creation menu o desde el
Node Editor. Ese camino es especialmente util para ajustar curvas a mano; el
pack de este laboratorio es mejor para revisiones reproducibles y diffs.

## Rendimiento

Los nodos no cuestan todos lo mismo. Antes de multiplicar props o aumentar
transiciones se prueba una ventana pequena. Update 6 incorporo prioridades y
concurrencia de generacion; `/worldgen2 concurrency <low> <normal> <high>` es
una herramienta de diagnostico, no una solucion para un grafo excesivamente
caro. Las transiciones grandes, los grafos, los escaneos amplios y la
composicion de muchas densidades deben medirse con varias seeds.

## Fuentes de referencia

- [Guia oficial para crear y editar biomas](https://pre-release.docs.hytale.com/creating-content/world-generation/how-to-edit-and-create-biomes/)
- [Referencia oficial de World Structure](https://pre-release.docs.hytale.com/hytale-generator/world-structure/)
- [Referencia oficial de assets HytaleGenerator](https://pre-release.docs.hytale.com/assets/hytalegenerator/worldstructures/)
- [Referencia oficial de Instances](https://pre-release.docs.hytale.com/assets/instances/)
- [Notas de la pre-release Update 7](https://hytale.com/news/2026/9/pre-release-patch-notes-update-7)
- [Update 6](https://hytale.com/news/2026/8/update-6-patch-notes)
- [El futuro de World Generation](https://hytale.com/news/2026/1/the-future-of-world-generation)
