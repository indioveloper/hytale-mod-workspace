# Propuesta de consolidacion de More Triggers

Revision realizada sobre los proyectos Java del workspace para decidir cuales
pueden distribuirse como un unico `OrbGenesis:More Triggers`.

## Conclusion

Todos los proyectos pueden compartir fisicamente un JAR, pero no conviene
fusionarlos todos en un unico plugin obligatorio. La opcion mas segura es
convertir `More Triggers` en el paquete general de utilidades de Trigger
Volumes y conservar como mods separados los sistemas de juego completos.

Los IDs publicos de efectos, condiciones y reglas deben conservarse. Los saves
guardan esos IDs; cambiar un nombre solo para reorganizar el codigo romperia
los Trigger Volumes existentes.

## Clasificacion

| Proyecto | Fusion | Recomendacion | Motivo principal |
| --- | --- | --- | --- |
| Circular Timer | Completada | Dentro de More Triggers | Es una utilidad de triggers y comandos sin datos persistentes. |
| Trigger Execute Command | Directa, riesgo bajo | Fusionar | Un solo efecto y la misma dependencia principal. |
| Player Trigger Tags | Viable, riesgo medio | Fusionar con migracion probada | Sus IDs de componente son explicitos y pueden conservarse, pero hay que validar tags ya guardadas. |
| Entity Motion Triggers | Viable, riesgo medio | Fusionar | Son cinco efectos de Trigger Volumes y dos sistemas ECS; encajan en el paquete general. |
| Scoreboards | Viable, riesgo alto | Mantener separado | Es un subsistema persistente de Objectives, con configuracion, assets dinamicos, comandos y UI propia. |
| Build Battle | Viable, riesgo alto | Mantener separado | Es logica de un modo de juego y obliga a depender de Builder Tools y de permisos especiales. |
| Map Selector | Viable tecnicamente | Mantener separado | No aporta efectos; es una aplicacion UI con mapas y asset packs externos. |
| Chest Labels | No integrar ahora | Mantener separado | No aporta efectos y su UI todavia es incompatible con pre-release 0.6.8. |

La fuente activa y empaquetada del timer esta dentro de
`mods/java/more-triggers`; el antiguo proyecto standalone no forma parte del
repositorio canonico.

## Paquete general recomendado

Una futura version mayor de More Triggers podria reunir:

- los siete efectos actuales;
- `ExecuteCommand`;
- `ModifyPlayerTag`, `ConvertBlocksToEntities` y `PlayerTagCondition`;
- los cinco efectos de Entity Motion Triggers.

El resultado seria un unico JAR de utilidades con 15 efectos, una condicion,
el comando `/timer` y los sistemas internos necesarios. Scoreboards, Build
Battle, Map Selector y Chest Labels seguirian siendo descargas independientes.

## Catalogo propuesto de utilidades

Los siete efectos que ya estan incluidos se documentan en
`mods/java/more-triggers/README.md`. Los siguientes son los candidatos a
integrar.

### ExecuteCommand

Ejecuta un comando cuando se dispara el Trigger Volume.

- `Command`: comando sin necesidad de `/` inicial.
- `Executor`: `SERVER` para consola o `PLAYER` para el jugador activador.
- Marcadores disponibles: `{player}`, `{uuid}`, `{x}`, `{y}` y `{z}`.

Ejemplo:

```text
give {player} Ingredient_Life_Essence 1
```

Debe reservarse a editores de confianza: con `SERVER` puede ejecutar cualquier
comando permitido a la consola.

### ModifyPlayerTag

Guarda una tag string persistente en el jugador activador.

- `Operation`: `SET`, `REMOVE`, `INCREMENT`, `TOGGLE` o `APPEND`.
- `TagKey`: nombre estable de la tag. Ejemplo: `arena_points`.
- `TagValue`: valor usado por `SET`, `INCREMENT` y `APPEND`.
- `DispatchMode`:
  - `NONE`: cambia el dato sin lanzar otro evento.
  - `CURRENT_VOLUME`: notifica el cambio al volumen actual.
  - `TAGGED_VOLUMES`: notifica a volumenes encontrados por tag y radio.
- `MatchKey`, `MatchValue`, `Radius` y `Center`: seleccionan los volumenes que
  reciben el evento cuando se usa `TAGGED_VOLUMES`.

Ejemplo: `INCREMENT`, `TagKey=arena_points`, `TagValue=1` suma un punto al
jugador.

### PlayerTagCondition

Condicion para decidir si un Trigger Volume continua segun una tag del jugador.

- `TagKey`: tag que se consulta.
- `Comparison`: `EQUALS`, `NOT_EQUALS`, `EXISTS`, `MISSING`, `GREATER_THAN`,
  `GREATER_OR_EQUAL`, `LESS_THAN` o `LESS_OR_EQUAL`.
- `TagValue`: valor esperado.
- `CaseSensitive`: exige coincidencia exacta de mayusculas y minusculas para
  comparaciones de texto.

Las comparaciones numericas solo pasan cuando ambos valores son numeros
validos.

### ConvertBlocksToEntities

Reemplaza cada bloque ordinario dentro del Trigger Volume por una entidad prop
con el item elegido.

- `Item`: item o item-bloque usado como modelo.
- `Collision`: `NONE`, `HARD` o `SOFT`.

Con `HARD`, las entidades pueden convertirse en superficies sobre las que el
jugador se mantiene. El efecto elimina los bloques originales y crea entidades
persistentes, por lo que debe probarse sobre una copia del mapa.

### ApplyHorizontalPlatformMotion

Aplica movimiento a las entidades compatibles que se encuentren dentro del
volumen.

- `TargetX`, `TargetY`, `TargetZ`: destino o desplazamiento.
- `CoordinateMode`: `ABSOLUTE` usa coordenadas del mundo; `RELATIVE` desplaza
  cada entidad desde su posicion actual.
- `Speed`, `SpeedY`, `SpeedZ`: bloques por segundo en cada eje. Cero mantiene
  quieto ese eje.
- `LoopBack`: viaja al destino y vuelve continuamente.
- `DestroyAtDestination`: elimina la entidad al llegar por primera vez.
- `TurnDirection`: `NONE`, `LEFT` o `RIGHT`.
- `TurnAngle`: giro relativo de 0 a 180 grados completado durante el trayecto.

### StopHorizontalPlatformMotion

Detiene las entidades moviles dentro del volumen retirando su componente de
movimiento.

- `OnlyFirstMatch`: actua solo sobre la primera entidad compatible encontrada.

La entidad se queda en la posicion alcanzada al detenerla.

### ApplyPlayerPlatformCollision

Hace solidas para los jugadores las entidades compatibles dentro del volumen.

- `CollisionConfig`: normalmente `HardCollision`; puede usarse otro asset de
  `HitboxCollisionConfig`.
- `OnlyFirstMatch`: modifica solo la primera coincidencia.

Se usa antes o junto a `ApplyHorizontalPlatformMotion` para que una entidad
funcione como plataforma transportadora.

### RemovePlayerPlatformCollision

Retira la colision de jugador de las entidades compatibles dentro del volumen.

- `OnlyFirstMatch`: modifica solo la primera coincidencia.

### SpawnItems

Crea un item como prop persistente, no recogible y con escala, rotacion y
colision configurables.

- `Item`: asset `Item` que se mostrara.
- `X`, `Y`, `Z`: coordenadas o desplazamiento.
- `CoordinateMode`: `ABSOLUTE` o `RELATIVE_TO_VOLUME`.
- `RotationX`, `RotationY`, `RotationZ`: grados de rotacion.
- `Scale`: multiplicador de tamano; `1` es el tamano normal.
- `Collision`: `NONE`, `HARD` o `SOFT`.

La colision se aplica dos ticks despues del spawn para que el cliente reciba
primero la geometria y el `NetworkId`.

## Funciones que deben seguir separadas

### Scoreboards / Objectives

Efectos:

- `ControlScoreboard`: inicia, muestra, oculta, completa o cancela una
  Objective.
- `ModifyScoreboardTask`: fija, suma o resta el valor de una tarea.

Condiciones:

- `ScoreboardState`: comprueba estado activo, inactivo o completado.
- `ScoreboardTaskValue`: compara el valor actual de una tarea.

Tambien aporta `/scoreboard`, `/scoreboards`, un editor UI, definiciones
persistentes, tareas nativas y assets dinamicos. Fusionarlo exigiria migrar su
configuracion al directorio de More Triggers y haria obligatoria la dependencia
`Hytale:Objectives` para todo el paquete.

### Build Battle

- Efecto `SuggestBuildTheme`: abre una UI, valida una palabra y guarda
  `theme_<palabra>=empty` y `points=0` en el volumen.
- Regla `RestrictBuildBattleCreativeTools`: bajo `Always Active`, limita las
  Builder Tools permitidas dentro del plot y restaura permisos al salir.

Depende de `Hytale:BuilderTools`, manipula permisos e inventarios y conserva un
fichero de recuperacion. Es logica especifica de un modo de juego.

### Map Selector

No registra efectos. Aporta `/mapas`, una UI con previews nativas de prefabs,
seleccion en memoria y teletransporte a destinos configurados. Puede depender
de un asset pack de mapas externo.

### Chest Labels

No registra efectos. Aporta `/chestlabel`, datos persistentes en bloques, HUD
al apuntar a un contenedor y editor de nombre/icono. Su UI usa actualmente un
nodo no soportado por 0.6.8 y no debe distribuirse hasta portarla.

## Requisitos para una fusion segura

1. Mantener exactamente los IDs actuales de efectos, condiciones, reglas,
   tareas y componentes persistentes.
2. Convertir cada antiguo `JavaPlugin` en un modulo registrado por el main de
   More Triggers; un JAR con un manifest no inicia automaticamente varios mains.
3. Unificar las carpetas `Common` y `Server` sin sobrescribir assets vanilla.
4. Fusionar las claves `server.lang` de `en-US` y `es-ES` y ejecutar las pruebas
   de localizacion.
5. Migrar configuraciones y datos que dependan del directorio del plugin.
6. Hacer opcionales los modulos con dependencias especializadas o mantenerlos
   como JAR separado.
7. Retirar los JAR antiguos antes del primer arranque para evitar registros
   duplicados.
8. Probar un save existente con tags, plataformas y Trigger Volumes guardados,
   ademas de una instalacion limpia.
