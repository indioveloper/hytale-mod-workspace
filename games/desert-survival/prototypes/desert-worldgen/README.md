# Desert Survival WorldGen (0.4.1)

Design reconciliation (2026-11-17): this pack has four finite zones and a
`Void` boundary, not the four planned outer rings or infinite deep desert.
Zone number is not cutterray tier. Aluminum, carbon, eritrite, jasmium,
titanium and stravidium nodes are not placed here. See
[world design](../../docs/WORLDGEN_DESERT_PLAN.md) and
[deep desert proposal](../../docs/DEEP_DESERT_WORLDGEN.md).

Asset pack WorldGen V2 para el runtime estable `0.7.7` que implementa los pasos
1 y 2 (y un primer corte del 3) de
[`docs/WORLDGEN_DESERT_PLAN.md`](../../docs/WORLDGEN_DESERT_PLAN.md): una
instancia aislada con cuatro zonas deserticas concentricas, terreno procedural
compartido, materiales por zona y nodos de recurso colocados por Props.

## Que contiene

| Archivo | Papel |
| --- | --- |
| `Server/Instances/Raynor_Desert_Survival/instance.bson` | Instancia creativa con seed fija `raynor-desert-survival-v1`, guarda chunks y jugadores. |
| `Server/HytaleGenerator/WorldStructures/Raynor_Desert_Survival.json` | `NoiseRange` con cinco rangos sobre la distancia al origen y spawn sobre la superficie natural cerca de (0, 0). |
| `Server/HytaleGenerator/Density/Raynor_Desert_Zone_Distance.json` | Campo de zona: `Distance` al origen (Y forzada a 0) mas ruido Simplex de escala 420 normalizado a +-70 bloques, para que las fronteras no sean circulos perfectos. |
| `Server/HytaleGenerator/Biomes/Raynor_Desert_Zone*.json` | Los cuatro biomas, generados por `tools/generate_biomes.py`. |

Zonas (distancia deformada al origen, en bloques):

| Zona | Rango | Relieve | Capas | Nodos |
| --- | --- | --- | --- | --- |
| 1 Refugio | 0-260 | dunas bajas (+-7) | arena blanca 2, grava blanca 2, arenisca blanca | celulas de combustible, celda 110; ruinas vanilla (campamentos, drywood y pozos de arenisca blanca) cada ~150 |
| 2 Dunas | 260-560 | crestas alargadas en X (+-12) y ondulacion amplia | arena 3, grava 1, arenisca | cobre celda 44, combustible celda 80 |
| 3 Canones | 560-900 | relieve suave con canales de ~30 bloques tallados por ruido en cresta | arena roja 1, arenisca roja 6, pizarra | hierro celda 46, cobre celda 70; bases enemigas T1/T2 cada ~390/~520 y ruinas drywood rojas cada ~140 |
| 4 Profundo | 900-1400 | relieve aspero de alta frecuencia (+-16) y erosion 3D con voladizos | arena cenicienta 1, basalto 5, piedra | hierro celda 40; bases enemigas T3/T4 cada ~620/~750 |
| Frontera | >1400 | bioma vanilla `Void` | - | - |

La superficie se asienta alrededor de `Base + 20` (Base = 100). Todos los
biomas usan `Env_Zone2_Deserts` y un tint propio. Los nodos son los bloques
`Resource_Node_*_Prototype` del prototipo Cutterray (dependencia declarada en el
manifiesto), colocados uno por celda de una malla con jitter 0.5 mediante un
prop `Column` con direccionalidad estatica y patron de suelo, igual que los
arbustos de `Desert1_Rocky` vanilla; el escaner `ColumnLinear` recorre la
columna de arriba abajo alrededor de Base. El mundo se genera en la ruta columnar
clasica (Y 0..319); no es cubico.

Los prefabs son carpetas de `Server/Prefabs` colocadas con un prop
`Prefab` (yaw aleatorio, patron "aire sobre cualquier bloque solido", escaner
de columna alrededor de Base, `LoadEntities` activo para que los puestos traigan
sus spawns de NPC), calcado de las chozas Feran de `Desert1_Oasis`. El puesto
`DesertSurvival/EnemyBase/Tier{1..4}` contiene las cuatro bases originales,
de 15x15, 21x21, 31x31 y 37x37 bloques (aproximadamente x1, x2, x4 y x6 de
superficie). T1 tiene cofres común y raro; T2 añade ultrarraro y una caja de
llave dorada junto a la entrada; T3 añade una caja que garantiza Intel; T4
incluye una bóveda techada
con puerta cerrada y cofre especial. La llave recogida desaparece del
inventario y se guarda en el personaje; abrir la puerta gasta una llave.
La puerta desbloqueada pasa a ser la puerta vanilla normal. Sus roles, Solaris,
recompensas, cofres y tablas de botín viven en este pack; el aggro, cadáveres
y la llave/puerta están en `prototypes/enemy-base` 0.3.1, dependencia declarada
del ZIP. La
distancia minima entre dos prefabs de la misma malla es aproximadamente
`celda * (1 - jitter)`. No hay regla de exclusion entre mallas distintas
(ruina contra puesto, prefab contra nodo). El prop Prefab no moldea el
terreno (`MoldingDirection: None`): en pendiente pueden quedar bordes volados.

Los cofres son contenedores nativos de 18 o 36 huecos con `Droplist`: el sistema
Stash los llena una vez al crear el bloque y guarda el inventario en el mundo.
El común conserva el modelo desértico; el raro y la caja de llave usan el
cofre pequeño dorado de dungeon; el ultrarraro usa su versión doble; el
especial usa el cofre doble legendario, con gema y luz cian tenue.
Común: curas, fibra, flechas, Solaris, células y raciones especiadas. Raro:
añade siempre arma o armadura. Ultrarraro: añade además siempre equipo personal
o pieza de vehículo. Especial: mismo botín, triple de Solaris que ultrarraro
(135-240 frente a 45-80) y siempre un plano. Batería, suspensor, piezas de
vehículo y plano son objetos provisionales sin mecánica ni permisos de
fabricación aún. Intel y llave salen garantizados en sus cajas respectivas.

## Validar y empaquetar

```powershell
python -I games/desert-survival/prototypes/desert-worldgen/tools/generate_biomes.py
python -I games/desert-survival/prototypes/desert-worldgen/tools/generate_enemy_base.py
python -I games/desert-survival/prototypes/desert-worldgen/tools/generate_enemy_loot.py
python -I games/desert-survival/prototypes/desert-worldgen/tools/generate_solaris_icon.py
.\games\desert-survival\prototypes\desert-worldgen\tools\Build-Pack.ps1
```

`Build-Pack.ps1` ejecuta `Test-Package.ps1`, que comprueba la estructura (nombres,
rangos contiguos, referencias entre instancia, WorldStructure, biomas y densidad
exportada, y que los nodos existen en `cutterray-t3`) y despues compila
`ValidateDesertWorldGen.java` contra el `HytaleServer.jar` de release y decodifica
cada Biome, WorldStructure y Density con los codecs reales. Las referencias a
biomas del WorldStructure (`Biomes`, `DefaultBiome`) usan `ContainedAssetCodec` y
solo se resuelven dentro del juego, asi que se retiran del documento antes de
decodificar y se comprueban por nombre en la prueba estructural. El ZIP queda en
`.build/dist`.

Lecciones del codec 0.7.7: el `Jitter` del generador `Mesh` admite como maximo
0.5; cargar `AssetManager` fuera del servidor termina la JVM, por eso el
validador registra a mano los tipos que usa el pack (lista extraida del
inicializador estatico con `javap`).

## Instalar y abrir el mundo

1. Copiar `DesertSurvival-WorldGen-0.4.1.zip` a `%APPDATA%\Hytale\UserData\Mods`
   (o a `mods` de un save de prueba). Cutterray 0.1.9 debe estar activo, porque
   define los bloques de nodo. Instalar también
   `DesertSurvival-EnemyBase-0.3.1.jar` y Research 0.2.8.
2. Arrancar el juego y entrar en cualquier mundo creativo.
3. `/instances` y abrir `Raynor_Desert_Survival`. El spawn esta en la zona 1,
   sobre la superficie.
4. Para probar los nuevos puestos, abrir `Raynor_Desert_EnemyBase_Test` desde
   `/instances`: usa una seed nueva y habilita el spawn de NPC y los marcadores.
   Los chunks guardados de la instancia antigua no se regeneran.

Comprobaciones de la primera prueba: que la instancia aparece y carga, altura y
suavidad de la zona 1, visibilidad de las fronteras de zona (ir hacia +X hasta
~300 y ~600 bloques), que los nodos estan apoyados en el suelo y no flotan o
quedan enterrados, y que los canones de la zona 3 son transitables.

## Historial de pruebas

- 0.1.0 (save `New World`): zonas correctas, sin nodos. Causa: Cutterray
  desactivado en ese save; el log mostraba `Attempted to register a Solid
  Material with invalid name Resource_Node_...: using Unknown instead`.
- 0.1.1 (save `dune2`, Cutterray activo): zonas correctas, sin avisos de
  material y aun sin nodos. El prop `Locator` + `Manual` no coloca nada.
- 0.1.2 (save `dune2`, instancia nueva): nodos visibles. Confirmado 2026-11-16.
  Regla: para bloques sueltos usar `Column`; `Locator` + `Manual` no coloca.
- 0.2.0 (instancia nueva): ruinas en zona 1 y puestos en zona 3 visibles.
  Confirmado 2026-11-16. `Test-Package.ps1` comprueba que cada carpeta de
  prefab tenga `.prefab.json` en `Assets.zip`.
- 0.3.0 (fuentes): puesto original, dos roles propios, Solaris y ración
  especiada. Estructura WorldGen y codecs comprobados; spawn, combate, loot
  y apariencia pendientes de smoke test en juego.
- 0.4.0: cuatro tamaños de base, seis cofres con botín persistente, llave y
  puerta de bóveda. Validación de estructura, codecs y tiradas de botín pasada;
  pendiente de smoke test en chunks nuevos de `dune2`.
- 0.4.1: modelos distintos para raro, ultrarraro y especial; caja de llave
  visible en T2, T3 y T4. Los chunks ya generados conservan los bloques de
  la versión anterior: explorar nuevos chunks o crear una instancia nueva.
- Persiste el aviso `Couldn't find WorldStructure Framework Positions with
  name Spawns` aunque la lista exporte `Spawns`; el spawn funciona igual.

## Pendiente (orden del plan)

- Ruinas propias con loot de chatarra (`Ingredient_Scrap_Prototype`) en vez de
  los prefabs vanilla; exclusion entre prefabs y nodos.
- Frontera jugable real: `Void` solo corta el terreno; falta control de
  posicion o banda hostil.
- Balance de densidad de nodos y celulas de combustible; aluminio en zona 4.
- Clima, calor, NPCs y puntos de interes.
- Pruebas con varias seeds (`SeedOverride` de la instancia).
