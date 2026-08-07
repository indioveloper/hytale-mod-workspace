# More Triggers

Coleccion de utilidades generales para Trigger Volumes. La version `1.9.1`
esta preparada para Hytale pre-release `0.6.x` e integra el antiguo mod Trigger
Execute Command.

## Efectos incluidos

| Nombre in-game (es-ES) | ID interno | Funcion |
| --- | --- | --- |
| Entregar objeto al azar | `GiveRandomItem` | Elige uniformemente un bloque, mueble, banco, arma o herramienta aptos para juego. |
| Pegar prefab al azar | `PasteRandomPrefab` | Elige y pega un prefab, con pesos opcionales. |
| Enviar mensaje con tags | `SendTagMessage` | Envia mensajes sustituyendo marcadores `{tag}`. |
| Mostrar titulo de evento con tags | `ShowTagEventTitle` | Muestra Event Titles sustituyendo marcadores `{tag}`. |
| Controlar timer circular | `ControlTimer` | Inicia, pausa, muestra, oculta o cancela el timer circular. |
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
vanilla del pegado.

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
  -ArtifactName More_Triggers-1_9_1.jar

.\mods\java\more-triggers\tools\Test-TimerMath.ps1
.\mods\java\more-triggers\tools\Test-RandomItemCandidateFilter.ps1
.\mods\java\more-triggers\tools\Test-Package.ps1 `
  -ArchivePath .\mods\java\more-triggers\.build\dist\More_Triggers-1_9_1.jar
```

El contorno del timer usa 61 frames PNG porque la UI 0.6.x no expone un
progreso radial nativo.
