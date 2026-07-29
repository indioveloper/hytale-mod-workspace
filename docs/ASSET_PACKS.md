# Asset Packs

## Packs Canonicos

### `mods/asset-packs/blocks`

Pack fusionado de bloques tecnicos y de previsualizacion.

Incluye:

- Barrera: item unico `OrbGenesis_Barrier_Visible` con textura de contorno rojo
  sutil.
- Ghost outline rock stone: textura, icono e item
  `Ghost_Outline_Rock_Stone`.
- Marcas de conteo murales `OrbGenesis_Tally_Marks`, con cinco estados,
  interaccion ciclica y hitbox fina.
- Herramienta de generacion de textura `tools/make_outline_texture.py`.

Origen: `visible-barrier-block` y `ghost-outline-blocks`.

Nota sobre modo aventura: el bloque es visible para todos los modos. En
pre-release 0.6.8 los bloques no tienen una bandera de visibilidad por jugador
o por modo; `DrawType: "Empty"` (como la barrera nativa) los oculta para todos.
Para mostrar una guia a constructores y ocultarla a jugadores de aventura hay
que usar una entidad marcada `HiddenFromAdventurePlayers`, no un bloque.

### `mods/asset-packs/mechanisms`

Pack fusionado de props accionables/animados.

Incluye:

- Palanca real: `Real_Lever`, `Real_Lever_Wall`, modelo de pared y animaciones
  `Real_Lever_Wall_On/Off`.
- Botones de esencia: `Essence_Button_Ice`, `Life`, `Lightning` y `Void`, con
  iconos, texturas y modelo comun `Essence_Button.blockymodel`.

Origen: `real-lever-animation` y `essence-buttons`.

### `mods/asset-packs/raynor-npcs`

Mod/asset pack unificado de NPCs Raynor.

Incluye:

- Modelo custom `Raynor_Random_Human`.
- Roles `Raynor_Klops_Miner_Patrol`, `Raynor_Static_Guard`,
  `Raynor_Static_Human_Emoter` y `Raynor_Worker_Route`.
- Spawn markers para esos cuatro roles.
- Rol reutilizable `Nexus_Avatar_Sword_Runner`.
- Sonido `nexus_war_cry` y evento `SFX_Nexus_War_Cry`.
- Efectos de Trigger Volumes `SpawnNpcRandomModel`, `EquipNpcItem` y
  `RemoveOneItemInVolume`.
- Localizaciones `en-US` y `es-ES`.

Estado: actualizado a rango de pre-release 0.6.x; compilar y smoke testear en
0.6.8 antes de usarlo en mapas importantes.

### `mods/asset-packs/nexus-siege-props`

Pack de props para Nexus Siege. Contiene soportes de espada, braseros
encendidos/apagados, un mecanismo de braseros y una plataforma 1x5.

### Packs independientes conservados

- `ghost-outline-blocks`: distribucion independiente del bloque de contorno,
  ya integrado tambien en `blocks`.
- `visible-barrier-block`: distribucion independiente de la barrera visible,
  ya integrada tambien en `blocks`.

## Experiments

`experiments/asset-packs` no es instalable en bloque. Son snapshots de pruebas
con IDs y overrides potencialmente incompatibles entre si.

- `archerygame`: prefabs de arqueria por colores (`rojo`, `azul`, `verde`,
  `amarillo`) y variante organizada bajo `Server/Prefabs/archerygame/X`.
- `prefab-tests`: manifest minimo; no contiene assets utiles actualmente.
- `raynor-test-commands`: items de prueba (`Chest_A`, `Chest_AA`, `Lever`,
  `Soil_Hive_Command`, `Test_Block`) y prefab `caja`.
- `raynor-tests`: prefabs numericos `1`, `2`, `3` y `horno_on`.
- `tests-0.6.3`: prefabs `bat`, `montaneta`, `pickaxe` y variantes.
- `tests-0.6.4`: laboratorio grande con Flechazo, overrides de bow/shortbow,
  projectile configs, ghost outlines, ventana de pueblo con cristal, rubble,
  prefabs 2x2 y localizaciones.
- `tests-0.6.5`: continuacion de Flechazo, sonidos `countdown3sec` y
  `timer30sec`, prefabs de parkour/ores/secuencia de palancas y trigger effect.
- `tests-0.6.7`: prefabs de filas de roca/aqua para pruebas recientes.
- `trigger-assets`: dos efectos de Trigger Volumes (`a`, `b`) de laboratorio.
- `connect4`: assets editables del minijuego de FoxyCCA. No incluye la
  instancia, regiones, BSON ni backups que venian dentro del ZIP original.
- `infinity-pillars`: prefab `octogono` usado por la prueba de pilares.
- `legacy-essence-buttons`: distribucion independiente antigua de los botones
  de esencia, ya integrada en `mods/asset-packs/mechanisms`.
- `legacy-real-lever-animation`: distribucion independiente antigua de la
  palanca animada, ya integrada en `mods/asset-packs/mechanisms`.
- `legacy-nexus-siege-npcs`: primer pack de NPCs de Nexus Siege, reemplazado
  por `mods/asset-packs/raynor-npcs`.

Antes de promover algo desde `experiments`, copiar solo el asset concreto a un
pack canonico y revisar IDs, dependencias y overrides vanilla.
