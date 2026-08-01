# Asset Packs

## Packs Canonicos

### `mods/asset-packs/blocks`

Pack fusionado de bloques tecnicos y de previsualizacion.

Incluye:

- Barrera: item unico `OrbGenesis_Barrier_Visible` con textura de contorno rojo
  sutil.
- Ghost outline rock stone: textura, icono e item
  `Ghost_Outline_Rock_Stone`.
- Herramienta de generacion de textura `tools/make_outline_texture.py`.

Origen: `visible-barrier-block` y `ghost-outline-blocks`.

Nota sobre modo aventura: el bloque es visible para todos los modos. En
pre-release 0.6.8 los bloques no tienen una bandera de visibilidad por jugador
o por modo; `DrawType: "Empty"` (como la barrera nativa) los oculta para todos.
Para mostrar una guia a constructores y ocultarla a jugadores de aventura hay
que usar una entidad marcada `HiddenFromAdventurePlayers`, no un bloque.

### `mods/asset-packs/mechanisms`

Pack `OrbGenesis:OrbGenesis Mechanisms`, version `1.0.5`, con props
accionables/animados y mecanismos jugables.

Incluye:

- Palanca real: `Real_Lever`, `Real_Lever_Wall`, modelo de pared y animaciones
  `Real_Lever_Wall_On/Off`.
- Botones de esencia: `Essence_Button_Ice`, `Life`, `Lightning` y `Void`, con
  iconos, texturas y modelo comun `Essence_Button.blockymodel`.
- Piedra de no construccion: `OrbGenesis_NoBuild_Stone` crea al impactar un
  Trigger Volume de `5 x 5 x 5` durante 10 segundos. El evento
  `VOLUME_CREATE` inserta `NoBuild` mediante `ModifyRules SET` y ejecuta 12
  `PlayVfx` con el sistema vanilla `Beam_Heal_Red3`, uno por arista.

Origen: `real-lever-animation`, `essence-buttons` y desarrollo propio del
proyectil temporal.

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

Pack minimo de props para Nexus Siege. Ahora mismo contiene el prefab
`sword_stand.prefab.json`.

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

Antes de promover algo desde `experiments`, copiar solo el asset concreto a un
pack canonico y revisar IDs, dependencias y overrides vanilla.
