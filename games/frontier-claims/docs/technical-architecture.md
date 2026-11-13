# Frontier Claims — Arquitectura técnica

Última actualización: 2026-09-05. Verificado contra Shared Source
`HypixelStudios/hytale-shared-source`, rama `pre-release`, commit local
`634880ce888e66922881597667fb87fbc0851e12` (ver `AGENTS.md` del workspace).

**Validado con `javap`** contra `HytaleServer.jar` de estable `0.6.3`,
revisión `ff802bf5a538f7e4b1df43a575c72f9d2bebb504` (manifest leído
directamente del JAR instalado en
`%APPDATA%\Hytale\install\release\package\game\latest\Server\HytaleServer.jar`,
coincide con la referencia de AGENTS.md). Todas las clases citadas en esta
sección existen en el JAR con el mismo paquete y las mismas firmas que en el
Shared Source `pre-release` de arriba:

- `InstancesPlugin.spawnInstance(String, World, Transform)` y la sobrecarga
  con `worldName` — idénticas.
- `InstancesPlugin.teleportPlayerToLoadingInstance`, `teleportPlayerToInstance`,
  `exitInstance`, `safeRemoveInstance` (las tres sobrecargas) — idénticas.
- `RemovalCondition` (interfaz, `shouldRemoveWorld`), `TimeoutCondition`,
  `IdleTimeoutCondition`, `WorldEmptyCondition` — mismos campos y métodos.
- `InstanceWorldConfig` — mismos 8 campos (`removalConditions`, `returnPoint`,
  `preventReconnection`, `discovery`, `respawnWhenTargeted`, `instanceKey`,
  `instanceName`, `documentDisplayOnRemoval`) y accesores.

Es decir: el sistema `Instances` en 0.6.3 estable es exactamente el descrito
en el Shared Source `pre-release` consultado — no hay drift entre ramas en
este módulo. El diseño de abajo puede darse por verificado a nivel de firma;
sigue pendiente la implementación real (nada de esto está codificado
todavía).

## 1. El sistema `Instances` ya resuelve el modelo de instancia por grupo

Módulo: `HytaleServer/builtin/Instances` (paquete
`com.hypixel.hytale.builtin.instances`). Es infraestructura vanilla, no algo
que haya que reinventar.

Piezas clave:

- `InstancesPlugin.spawnInstance(name, worldName, forWorld, returnPoint)` —
  copia un "instance asset" (un mundo con `instance.bson`) a una nueva copia
  de mundo y la carga. Si se pasa un `worldName`/`InstanceKey` fijo, spawns
  simultáneos convergen en la misma copia (útil para que un grupo de hasta 10
  jugadores entrando a la vez comparta una sola instancia, no una por
  jugador).
- `InstanceWorldConfig` — configuración por-mundo: `RemovalConditions[]`,
  `ReturnPoint`, `InstanceKey`, `Discovery` (título al descubrir),
  `RespawnWhenTargeted`, `DocumentDisplayOnRemoval` (UI mostrada mientras la
  instancia se cierra).
- `RemovalCondition` — interfaz con tres implementaciones vanilla:
  `TimeoutCondition` (cierra tras N segundos fijos desde la creación),
  `IdleTimeoutCondition` (cierra tras N segundos sin jugadores presentes) y
  `WorldEmptyCondition` (cierra en cuanto se vacía, una vez que al menos un
  jugador entró — ver hallazgo crítico abajo).

Archivos exactos:

```text
HytaleServer/builtin/Instances/src/main/java/com/hypixel/hytale/builtin/instances/InstancesPlugin.java
HytaleServer/builtin/Instances/src/main/java/com/hypixel/hytale/builtin/instances/config/InstanceWorldConfig.java
HytaleServer/builtin/Instances/src/main/java/com/hypixel/hytale/builtin/instances/config/InstanceDiscoveryConfig.java
HytaleServer/builtin/Instances/src/main/java/com/hypixel/hytale/builtin/instances/removal/RemovalCondition.java
HytaleServer/builtin/Instances/src/main/java/com/hypixel/hytale/builtin/instances/removal/TimeoutCondition.java
HytaleServer/builtin/Instances/src/main/java/com/hypixel/hytale/builtin/instances/removal/IdleTimeoutCondition.java
HytaleServer/builtin/Instances/src/main/java/com/hypixel/hytale/builtin/instances/removal/WorldEmptyCondition.java
```

### Decisión de diseño derivada

- Cada partida de Frontier Claims = un **instance asset** (`Server/Instances/FrontierClaims/`
  con su `instance.bson`) generado una sola vez a mano/con worldgen (ver §3),
  y con los 80 POIs ya colocados como Trigger Volumes dentro de ese mundo
  plantilla.
- Al iniciar un grupo (comando o interacción en el HUB principal del server),
  se llama `spawnInstance("FrontierClaims", "frontier-claims-<partyId>", hubWorld, returnPoint)`
  para que ese grupo concreto obtenga su propia copia aislada.
- `RemovalConditions = [TimeoutCondition(7 días en segundos)]`. **No** añadir
  `WorldEmptyCondition`: ver hallazgo crítico.
- Opcionalmente un `IdleTimeoutCondition` con un umbral de varios días (no
  minutos) como red de seguridad para instancias realmente abandonadas, sin
  que penalice a alguien que se desconecta un rato dejando NPCs trabajando.

### Hallazgo crítico: `WorldEmptyCondition` mataría el farmeo offline

`WorldEmptyCondition.shouldRemoveWorld()` devuelve `true` en el primer tick en
que la instancia queda sin jugadores no-espectadores, siempre que ya haya
tenido al menos un jugador dentro (ver
`removal/WorldEmptyCondition.java:66-68`). Eso es exactamente lo contrario de
lo que pide el diseño (NPCs que farmean con el jugador desconectado, punto 3-4
de la idea original) — si se usara esta condición, la instancia (y sus NPCs,
minas, buffers) desaparecería en cuanto el último jugador del grupo cerrara
sesión. Por eso el diseño usa solo `TimeoutCondition` + opcionalmente
`IdleTimeoutCondition` con un umbral grande, nunca `WorldEmptyCondition`, para
este modo de juego.

### Aviso de cierre al jugador

`InstanceWorldConfig.documentDisplayOnRemoval` + el sistema vanilla
`hud/InstanceRemovalHudSystem.java` ya implementan un HUD de aviso mientras la
instancia se cierra. Reutilizar esto para el aviso de "la temporada termina
en X" en vez de construir un HUD de cuenta atrás desde cero.

## 2. Persistencia: qué vive dentro de la instancia y qué vive fuera

- **Dentro de la instancia** (se pierde al expirar, por decisión de diseño
  §13 del brief): propiedad de territorios, buffers de minas, NPCs
  contratados, progreso de mini-dungeons del día, racha diaria del jugador
  *para esa instancia concreta*.
- **Fuera de la instancia** (plugin-level, persistente): el ranking histórico
  de "poder acumulado" por jugador/temporada. Patrón: un `Storage` propio del
  plugin (siguiendo el patrón Service+Storage de `hytale-plugin` skill),
  clave `playerUUID`, escrito justo antes de que `TimeoutCondition` dispare el
  cierre — hay que engancharse al evento/momento de remoción de la instancia
  (`Universe.removeWorld` o el propio `RemovalSystem`) para capturar el
  snapshot final antes de que el mundo desaparezca, no después.
- El hook de monetización (`premiumBuffs`) vive en el modelo de datos del
  jugador dentro de la instancia (afecta a esa partida), no en el ranking
  histórico.

## 3. Generación procedural del mapa, con semilla compartida por temporada

Decisión cerrada con el usuario (revisada tras la primera pasada de diseño):
el mapa **no** se coloca a mano. Se genera con el generador procedural
clásico de Hytale (compatible con 0.6.3 estable, **no** WorldGen V2 — evita
por completo la limitación de `HytaleGenerator.Handle` en pre.1 documentada
antes). Reglas de semilla, decididas con el usuario:

- Todas las instancias que se abran **durante la misma semana/temporada**
  usan la **misma semilla** — mismo layout de POIs para todos los grupos, para
  que la competición sea justa entre partidas paralelas.
- Al llegar más de 10 jugadores, la cola automática (brief §2.1) simplemente
  abre otra copia de la misma plantilla (misma semilla) — más grupos, mismo
  mapa esa semana.
- Al terminar la temporada (7 días), la siguiente semana usa una **semilla
  nueva** — layout distinto, variedad entre temporadas.

### 3.1 El pipeline real (verificado en el Shared Source, módulo `HytaleGenerator` clásico)

No es necesario WorldGen V2: el generador clásico ya tiene el pipeline
completo de colocación procedural de estructuras, el mismo que usa vanilla
para pueblos, dungeons, árboles y rocas:

```text
WorldStructureAsset (BasicWorldStructureAsset)
  └─ Biomes[]: BiomeRangeAsset (rango de una función Density → BiomeAsset)
  └─ DefaultBiome, Density (la función de ruido que reparte el espacio)
       └─ BiomeAsset
            └─ Props[]: PropRuntimeAsset
                 └─ PropDistributionAsset (dónde y con qué densidad)
                      └─ PositionProviderAsset (posiciones candidatas)
                      └─ PropAsset → PrefabPropAsset (qué prefab pegar)
```

Archivos exactos (todos bajo
`HytaleServer/builtin/HytaleGenerator/src/main/java/com/hypixel/hytale/builtin/hytalegenerator/`):

```text
assets/worldstructures/basic/BasicWorldStructureAsset.java
assets/biomes/BiomeAsset.java
assets/propdistribution/{ConstantPropDistributionAsset,PositionsPropDistributionAsset,...}.java
assets/props/prefabprop/PrefabPropAsset.java
```

**Hallazgo clave**: `VolumeEntry` tiene un campo `FromWorldGen` y
`TriggerVolumeManager` expone `removeWorldGenVolumesInChunk(long chunkIndex)`
(`TriggerVolumes/manager/{VolumeEntry,TriggerVolumeManager}.java`). Esto
confirma que una Trigger Volume **puede vivir dentro de un prefab** y
registrarse automáticamente cuando el generador lo pega en el mundo — es el
mismo mecanismo que usa un pueblo o dungeon vanilla. Consecuencia directa
para el diseño: **cada prefab de POI (mina de oro, mina de gemas, spawner,
recompensa, parcela, dungeon) lleva su propia Trigger Volume ya configurada
dentro del propio prefab** (tags `frontier_type`, `frontier_tier`, efectos
`ClaimTerritory`/`CollectMine`, etc. — ver `project-brief.md` §3), sin
necesidad de pegarla aparte a mano.

### 3.2 Diseño de zonas

Cada tipo de POI (mina de oro, mina de gemas, spawner de mobs, recompensa
inmediata, parcela construible, dungeon) se asigna a su propio "bioma" en el
`BasicWorldStructureAsset`, tallado por la función `Density` en regiones de
extensión similar (igual que vanilla reparte biomas de bosque/desierto/etc.),
y cada bioma coloca únicamente prefabs de su tipo de POI vía `Props`.

Pendiente de resolver en implementación (no bloquea el diseño):

- **El HUB central**: la función `Density` no garantiza por sí sola una
  región fija en el origen del mundo. Verificar si existe un nodo de
  `DensityAsset` de tipo "distancia al punto/radial" para tallar un anillo
  central de Hub rodeado por las 6 zonas de POI; si no existe, el Hub se
  coloca como una estructura fija independiente del bioma (framework/decor),
  no como un bioma más.
- **Conteo exacto de POIs (15/15/15/15/10/10)**: la colocación por
  `PropDistributionAsset` basada en densidad (`ConstantPropDistributionAsset`)
  es probabilística por chunk, no garantiza un total exacto. Alternativas:
  aceptar un total aproximado (variación entre semillas, coherente con que
  esto es procedural) o usar `PositionsPropDistributionAsset`/
  `AnchorPropDistributionAsset` con una lista de posiciones precalculada por
  semilla para fijar el total exacto. Decidir con datos del vertical slice.
- **Reglas de rotación de prefabs en worldgen**: la GUIA del workspace
  documenta un bug de rotación 90/270 al pegar prefabs **en runtime**
  (`PasteRandomPrefabEffect`, ver `docs/GUIA_DESARROLLO_MODS_HYTALE_PARA_MODELOS.md`
  §9). `PrefabPropAsset` pega en tiempo de generación, por una ruta de código
  distinta — no asumir que el mismo bug aplica; verificar con un prefab de
  prueba que tenga una Trigger Volume interna antes de construir los 6 tipos
  de POI reales.

### 3.3 Reseed entre temporadas

El asset de instancia (`Server/Instances/FrontierClaims/instance.bson`, un
`WorldConfig` serializado) tiene un campo `Seed` (`long`), con
`getSeed()`/`setSeed(long)` verificados en
`CoreServer/.../universe/world/WorldConfig.java`. Mecanismo de reseed:

1. `spawnInstance("FrontierClaims", ...)` (brief §2.1, `technical-architecture.md`
   §1) copia los ficheros del asset — incluido su `instance.bson` con la
   semilla vigente — a cada copia nueva. Como no hace falta pregenerar chunks
   (el terreno se genera perezosamente igual que en exploración vanilla),
   copiar el asset es barato aunque nadie lo haya visitado nunca.
2. Todas las copias creadas mientras esa semilla siga vigente en el asset
   generan **el mismo layout de POIs**, de forma determinista e
   independiente entre copias — no hace falta compartir ni sincronizar nada
   entre instancias paralelas más allá de partir del mismo asset.
3. Al cerrar la temporada, antes de que se abra la primera instancia de la
   semana siguiente, un proceso (comando admin o tarea programada) reescribe
   `instance.bson` con `WorldConfig.setSeed(nuevaSemilla)` + `markChanged()`
   y lo persiste. **Nota**: `InstancesPlugin.loadInstanceAssetForEdit()`
   carga el asset con el guardado de configuración deliberadamente
   deshabilitado (comentario en el propio Javadoc: "changes to the
   configuration of the world will not save currently") — no sirve para esto.
   Reescribir la semilla necesita una vía distinta: lo más simple es una
   pequeña herramienta/comando que cargue el `instance.bson` con
   `WorldConfig.load(...)`, llame `setSeed(...)` + `markChanged()`, y lo
   vuelva a serializar a disco directamente con el codec, sin pasar por el
   flujo de edición en vivo. Verificar el método de guardado exacto de
   `WorldConfig` antes de implementar.

## 4. Qué NO existe todavía y hay que construir

Ninguno de estos tiene equivalente vanilla ni en los mods actuales del
workspace; son plugin nuevo:

- Ownership + economía de territorio sobre Trigger Volumes (coste, tokens,
  tiers) — more-triggers da la infraestructura de volúmenes y señales, no
  conceptos de propiedad ni gasto de recursos.
- Sistema de producción autónoma con buffer por mina (tick periódico, similar
  en espíritu a la cadencia de spawn de `configurable-mob-spawners` pero para
  generación pasiva de recursos, no de mobs).
- Lógica de NPC recolector que transfiere recursos entre el buffer de una
  mina y un almacén — los roles actuales de `raynor-npcs` (patrulla, guardia
  estática, emote) no hacen transferencia de recursos.
- Racha diaria + reparto de tokens (evento de "nuevo día" en la instancia,
  probablemente enganchado al ciclo día/noche del mundo o a un `TimeResource`
  propio, con un componente `PlayerSeasonProgress` por jugador y por
  instancia).
- Mini-dungeons propias con cooldown 1/día por jugador y por dungeon.
- El propio comando/interacción de entrada al evento que llama a
  `spawnInstance` y agrupa jugadores en un `party` de hasta 10.

## 5. Riesgos y validaciones pendientes antes de implementar

- ~~Confirmar con `javap` que `InstancesPlugin`, `InstanceWorldConfig`,
  `RemovalCondition` y sus implementaciones existen con la misma firma en
  0.6.3 estable~~ — **hecho el 2026-09-05**: validado contra
  `HytaleServer.jar` 0.6.3, revisión `ff802bf5a538f7e4b1df43a575c72f9d2bebb504`.
  Sin drift respecto al Shared Source `pre-release` citado. Ver §0.
- Revisar si `spawnInstance` y la copia de archivos de mundo tienen un coste
  de I/O relevante si se crean muchas instancias simultáneas en un servidor
  con muchos grupos activos a la vez (§ "Instance Creation" del Javadoc de
  `InstancesPlugin` ya avisa de que puede tardar con muchos chunks
  pregenerados — usar `teleportPlayerToLoadingInstance` con pantalla de
  carga, no bloquear al jugador).
- La composición de instancia ya está decidida (cola automática, ver el
  brief §2.1) — pendiente solo la implementación del comando/interacción de
  entrada que agrupa jugadores hasta llenar 10 o abrir una instancia nueva.
