# Consolidacion de More Triggers

Estado final de la reorganizacion, actualizado y compilado para Hytale
`0.6.0-pre.12`.

## Resultado

- `Trigger Execute Command` se fusiona con `OrbGenesis:More Triggers`.
- `Player Entity Tags` se aparta bajo `mods/java/deprecated/player-entity-tags`.
- `Chest Labels` se aparta bajo `mods/java/deprecated/chest-labels`.
- El antiguo mod standalone de Execute Command se conserva bajo
  `mods/java/deprecated/trigger-execute-command-standalone` solo como historial.
- `ConvertBlocksToEntities` pasa de Player Trigger Tags a Entity Motion
  Triggers, junto con su sistema de colision diferida.
- Entity Motion Triggers, Scoreboards y Build Battle permanecen como mods
  independientes.
- Map Selector se conserva bajo `auxiliary/java/map-selector` como codigo de
  apoyo para un proyecto externo; no forma parte del catalogo activo ni de la
  instalacion de desarrollo.
- `RemoveEventTitle` se retira a codigo deprecated y deja de registrarse.
- `RandomTagSelection` se elimina completamente.

No se ha cambiado ningun ID publico de efecto, condicion, regla o componente.
Los saves guardan esos IDs y un renombrado romperia Trigger Volumes existentes.

## Catalogo final por mod activo

### More Triggers 1.9.2

Efectos:

- `GiveRandomItem`: entrega un bloque de construccion, banco, objeto
  decorativo, arma o herramienta elegido al azar. Excluye plantas, arboles,
  menas, fluidos, portales, assets internos y herramientas creativas.
- `PasteRandomPrefab`: pega un prefab elegido al azar, con pesos opcionales.
- `SendTagMessage`: envia texto resolviendo marcadores `{tag}`.
- `ShowTagEventTitle`: muestra titulos resolviendo marcadores `{tag}`.
- `ControlTimer`: inicia, pausa, muestra, oculta o cancela el timer circular.
- `ExecuteCommand`: ejecuta un comando como servidor o jugador activador.
Reglas:

- `NoMove`: aparece bajo `Always Active` y cancela el movimiento dentro del
  volumen, con excepciones opcionales para jugadores y una lista de roles NPC.

No registra condiciones. Tambien aporta el comando `/timer`.
`SetPlayerGravityView` y `/gravityview` se retiraron del mod activo; el
prototipo 1.8.x se conserva bajo `experiments/java/inverted-gravity-camera`.

### Entity Motion Triggers 1.3.1

Efectos:

- `SpawnItems`: crea props persistentes a partir de items.
- `ConvertBlocksToEntities`: convierte los bloques del volumen en entidades.
- `ApplyHorizontalPlatformMotion`: mueve entidades a un destino absoluto o
  relativo.
- `StopHorizontalPlatformMotion`: detiene las entidades moviles del volumen.
- `ApplyPlayerPlatformCollision`: aplica colision de plataforma.
- `RemovePlayerPlatformCollision`: retira la colision de plataforma.
- `AttachMovingParticles`: ancla particulas que siguen el movimiento y giro de
  una entidad.

No registra condiciones ni reglas. Conserva los componentes persistentes y
temporales necesarios para movimiento y colision, incluido
`OrbGenesis_PendingPlatformCollision`.

### Particle Shape VFX 0.1.0

Efectos:

- `SpawnParticleShape`: dibuja las 12 aristas de un cubo, la superficie de una
  esfera o una linea entre dos puntos XYZ. Admite coordenadas absolutas o
  relativas al centro del Trigger Volume, densidad, escala, duracion y limite
  de puntos configurables.

No registra condiciones ni reglas. Sigue separado porque es una extension
generica de VFX y no depende de la logica de movimiento de entidades.

### Scoreboards 2.0.10

Efectos:

- `ControlScoreboard`: inicia, muestra, oculta, completa o cancela una
  Objective.
- `ModifyScoreboardTask`: fija, suma o resta el valor de una tarea.

Condiciones:

- `ScoreboardState`: comprueba si la Objective esta activa, inactiva o
  completada.
- `ScoreboardTaskValue`: compara el valor actual de una tarea.

Sigue separado porque mantiene Objectives persistentes, assets dinamicos,
comandos y UI propia.

### Build Battle 0.2.3

Efectos:

- `SuggestBuildTheme`: solicita una palabra y guarda las tags del tema y los
  puntos en el volumen.

Reglas:

- `RestrictBuildBattleCreativeTools`: limita las Builder Tools permitidas en el
  plot y restaura permisos al salir.

Sigue separado porque es logica especifica de un modo de juego y depende de
Builder Tools y permisos especiales.

## Codigo auxiliar

### Map Selector 0.1.1

No registra efectos, condiciones ni reglas. Aporta `/mapas`, previews de
prefabs y teletransporte a destinos configurados. Se conserva para consulta en
`auxiliary/java/map-selector`, pero no se distribuye con los mods activos.

## Codigo deprecated

### Efectos retirados de More Triggers

`RemoveEventTitle` se conserva como fuente historica bajo
`mods/java/deprecated/more-triggers-retired-effects`, pero no se registra ni se
distribuye. `RandomTagSelection` no se conserva y fue eliminado del codigo,
traducciones y documentacion activa.

### Player Entity Tags 1.5.6

Conserva como fuente historica, pero no se distribuye:

- efecto `ModifyPlayerTag`;
- condicion `PlayerTagCondition`;
- componente persistente `OrbGenesis_PlayerTriggerTags`.

`ConvertBlocksToEntities` ya no forma parte de este mod deprecated: su fuente
activa esta en Entity Motion Triggers.

### Chest Labels 0.1.0

No registra efectos de Trigger Volumes. Conserva el prototipo de `/chestlabel`,
datos persistentes de bloques, HUD y editor de nombre/icono. Su UI no esta
portada a pre-release 0.6.8.

### Trigger Execute Command standalone 1.1.0

Conserva el antiguo efecto `ExecuteCommand` solo como referencia. No debe
instalarse junto a More Triggers porque ambos registrarian el mismo ID.

## Reglas de instalacion y migracion

1. Instalar un unico JAR por cada `Group:Name`.
2. Retirar `Trigger Execute Command` antes de instalar More Triggers 1.9.2.
3. Retirar `Player Trigger Tags` antes de instalar Entity Motion Triggers 1.3.1
   si el save usa `ConvertBlocksToEntities`.
4. No instalar proyectos de `mods/java/deprecated`.
5. No instalar proyectos de `auxiliary` salvo para reproducir expresamente el
   proyecto externo al que pertenecen.
6. Reiniciar o recargar la partida despues de sustituir JARs; copiar el archivo
   no recarga clases ya iniciadas.
7. Probar sobre una copia del save los Trigger Volumes existentes y confirmar
   que no aparece ningun registro duplicado.
