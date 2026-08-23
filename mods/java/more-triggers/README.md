# More Triggers

Coleccion de utilidades generales para Trigger Volumes. La version `1.10.2`
esta validada con Hytale `0.6.0-pre.13.1` e integra el antiguo mod Trigger
Execute Command.

## Efectos incluidos

| Nombre in-game (es-ES) | ID interno | Funcion |
| --- | --- | --- |
| Entregar objeto al azar | `GiveRandomItem` | Elige uniformemente un bloque, mueble, banco, arma o herramienta aptos para juego. |
| Pegar prefab al azar | `PasteRandomPrefab` | Elige y pega un prefab, con pesos opcionales, rotacion y preview. |
| Enviar mensaje con tags | `SendTagMessage` | Envia mensajes sustituyendo marcadores `{tag}`. |
| Mostrar titulo de evento con tags | `ShowTagEventTitle` | Muestra Event Titles sustituyendo marcadores `{tag}`. |
| Controlar timer circular | `ControlTimer` | Inicia, pausa, muestra, oculta o cancela el timer circular. |
| Controlar senal repetitiva | `ControlSignalLoop` | Inicia y controla bucles que envian `SignalReceived` aunque el activador abandone el volumen. |
| Ejecutar comando | `ExecuteCommand` | Ejecuta un comando como consola o jugador activador. |

## Reglas incluidas

| Nombre in-game (es-ES) | ID interno | Funcion |
| --- | --- | --- |
| No Move | `NoMove` | Cancela continuamente el movimiento dentro del volumen, con excepciones para jugadores y roles NPC. |

`RemoveEventTitle` ya no se registra: su codigo se conserva en
`mods/java/deprecated/more-triggers-retired-effects`.
`RandomTagSelection` se elimino completamente por decision de diseno.

## Entregar objeto al azar

El ID interno sigue siendo `GiveRandomItem`. Es el efecto al que se hace
referencia como "Spawn Items" cuando se habla de dar items aleatorios; no debe
confundirse con `SpawnItems` de Entity Motion Triggers, que crea una
entidad-prop visible en el mundo.

- `Quantity`: cantidad del item seleccionado.
- El pool incluye bloques de `Rocks`, `Wood`, `Metal`, `Cloth`, `Soils` y
  `Deco`; bancos y mobiliario; armas y herramientas normales.
- Se excluyen plantas y arboles, menas, fluidos, portales, variantes ocultas,
  assets de debug/prueba/spawn, bloques tecnicos, `BuilderTool` y selectores de
  bloques creativos. Una categoria permitida nunca anula una exclusion.
- `OverflowBehavior`:
  - `DROP_REMAINDER`: lo que no cabe cae al suelo.
  - `IGNORE_REMAINDER`: lo que no cabe se descarta.
  - `REQUIRE_FULL_STACK`: no entrega nada si no cabe la cantidad completa.

## Pegar prefab al azar

`PasteRandomPrefab` admite `Prefab1`, `Prefab2`, una lista adicional de
prefabs, pesos opcionales, posicion absoluta o relativa al volumen y particulas
vanilla del pegado. El campo `Yaw` rota horizontalmente el prefab completo
alrededor del eje Y con las cuatro orientaciones que admite la API:

- `None`: 0 grados; conserva la orientacion original y es el valor por defecto.
- `Ninety`: 90 grados en sentido horario.
- `OneEighty`: 180 grados.
- `TwoSeventy`: 270 grados en sentido horario.

Para configurarlo, edita el efecto `PasteRandomPrefab` en Trigger Volumes,
selecciona `Rotacion horizontal` / `Horizontal rotation` y elige una de esas
cuatro opciones. Los efectos guardados antes de 1.10.1 no contienen `Yaw`; al
cargarlos se usa `None`, por lo que mantienen exactamente su orientacion.
`PrefabUtil.paste` en Hytale 0.6.0-pre.13.1 solo expone esta rotacion cardinal
horizontal; pitch, roll y angulos arbitrarios no estan soportados.

El editor muestra tambien el boton vanilla `Mostrar preview` / `Ocultar
preview`. La preview usa el primer prefab configurado que pueda resolverse
(`Prefab1`, despues `Prefab2` y finalmente la lista adicional), para no cambiar
aleatoriamente mientras se edita. Esto solo afecta a la ayuda visual: al
ejecutarse, el efecto sigue seleccionando entre todos los prefabs y respetando
sus pesos. La posicion y el modo relativo/absoluto se exponen al inspector
vanilla para colocar correctamente la preview. En pre.13.1, el inspector
vanilla no aplica `Rotation` a los bloques de la preview ni siquiera para
`PastePrefabEffect`: la ayuda visual conserva la orientacion original, aunque
el `Yaw` configurado si se aplica al pegado real.

## Mensajes y titulos con tags

`SendTagMessage` y `ShowTagEventTitle` resuelven marcadores como `{points}` a
partir de tags de este volumen, de los volumenes del evento o de volumenes
cercanos. Los destinatarios y el radio se configuran en el editor.

## Timer circular

`ControlTimer` mantiene un contador independiente por jugador. Admite
`START`, `PAUSE`, `RESUME`, `SHOW`, `HIDE` y `CANCEL`.

Comandos equivalentes:

```text
/timer start <segundos>
/timer pause
/timer resume
/timer hide
/timer show
/timer cancel
/timer status
```

## Senales repetitivas

`ControlSignalLoop` crea un bucle con estado propio del mundo. Al iniciarlo,
captura el volumen origen, el centro de busqueda, el actor y las tags de senal;
despues sigue enviando `SignalReceived` aunque el jugador salga del Trigger
Volume que lo activo. No necesita `On Tick` ni un volumen que cubra toda la
sala.

- `Action`: `START`, `STOP`, `PAUSE`, `RESUME` o `PULSE_NOW`.
- `LoopId`: nombre compartido por todas las acciones que controlan el mismo
  bucle. Es unico dentro de cada mundo.
- `IntervalSeconds`: cadencia; el minimo efectivo es 0.1 segundos.
- `FirstPulse`: primera senal inmediata o despues del primer intervalo.
- `StartBehavior`: ignora un segundo inicio, reinicia la cadencia existente o
  sustituye toda su configuracion.
- `DurationSeconds` y `MaxPulses`: limites automaticos; `0` significa sin
  limite.
- `MatchKey`, `MatchValue`, `Radius` y `Center`: seleccionan receptores igual
  que el efecto vanilla `SendSignal`. Sin `MatchKey`, la senal vuelve al
  volumen que inicio el bucle.
- `SignalKeys` y `SignalValues`: tags transportadas por `SignalReceived` para
  que el receptor pueda filtrarlas desde `EVENT`.
- `ContinueTagKey` y `ContinueTagValue`: condicion opcional comprobada
  continuamente en el volumen origen. Si deja de cumplirse, el bucle se para.

Ejemplo: en `On Enter`, usa `START` con `LoopId=room_a`, intervalo `5`, tag
objetivo `wave_receiver=room_a` y condicion `encounter_active=true`. Otro
efecto puede cambiar esa tag o ejecutar `STOP` con el mismo `LoopId`. El bucle
tambien termina si se desactiva o elimina el volumen que lo inicio.

Los bucles son deliberadamente temporales: no se restauran tras reiniciar el
servidor. Si un tick llega tarde, se envia una sola senal y comienza un nuevo
intervalo, sin rafagas de recuperacion.

## Ejecutar comando

`ExecuteCommand` conserva el ID y los campos del antiguo mod standalone.

- `Command`: comando con o sin `/`. Admite `{player}`, `{uuid}`, `{x}`, `{y}`
  y `{z}`.
- `Executor`: `SERVER` lo ejecuta como consola; `PLAYER`, como jugador
  activador.

No instales el antiguo Trigger Execute Command junto a More Triggers.

## No Move

`NoMove` es una regla y aparece en la seccion `Always Active` del Trigger
Volume. En cada tick asigna velocidad cero a las entidades situadas dentro del
volumen; al salir recuperan su movimiento normal sin necesitar otro efecto.

- `ExcludePlayers`: permite que los jugadores sigan moviendose.
- `ExcludedNpcRoles`: lista de assets `NpcRole` que conservaran su movimiento.
Las excepciones se comprueban por el ID estable del role NPC, no por el nombre
visible de la criatura.

El prototipo retirado `SetPlayerGravityView` y el diagnostico completo de sus
controles se conservan en `experiments/java/inverted-gravity-camera`; no se
registran ni se empaquetan con More Triggers.

## Build y validacion

```powershell
.\mods\java\more-triggers\tools\Test-PluginLocalization.ps1
.\mods\java\more-triggers\tools\Test-TagTemplateResolver.ps1
.\mods\java\more-triggers\tools\Test-NoMoveExceptionFilter.ps1

.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\more-triggers `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName More_Triggers-1_10_2.jar

.\mods\java\more-triggers\tools\Test-TimerMath.ps1
.\mods\java\more-triggers\tools\Test-SignalLoopSchedule.ps1
.\mods\java\more-triggers\tools\Test-RandomItemCandidateFilter.ps1
.\mods\java\more-triggers\tools\Test-Package.ps1 `
  -ArchivePath .\mods\java\more-triggers\.build\dist\More_Triggers-1_10_2.jar
```

El contorno del timer usa 61 frames PNG porque la UI 0.6.x no expone un
progreso radial nativo.
