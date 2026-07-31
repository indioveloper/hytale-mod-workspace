# More Triggers

Coleccion de efectos adicionales para los Trigger Volumes de Hytale. La
version `1.4.0` esta preparada para pre-release `0.6.x` e incluye un HUD de
timer circular controlable tanto por triggers como por comandos.

## Instalacion

1. Copia un solo JAR de `More Triggers` a la carpeta global `Mods` o a
   `Saves/<partida>/mods`.
2. Comprueba que no exista otra version con `Group:Name`
   `OrbGenesis:More Triggers`.
3. Reinicia la partida. Sustituir el archivo mientras el servidor esta abierto
   no recarga las clases ni los assets.

Dependencias:

- `Hytale:TriggerVolumes`
- `Hytale:EntityModule`

## Efectos incluidos

| Efecto | Para que sirve |
| --- | --- |
| `RemoveEventTitle` | Retira el Event Title mostrado al jugador activador. |
| `GiveRandomItem` | Entrega un item elegido al azar entre los assets `Item` cargados. |
| `RandomTagSelection` | Elige un valor al azar y lo escribe como tag de uno o varios Trigger Volumes. |
| `PasteRandomPrefab` | Elige y pega un prefab al azar, con pesos opcionales. |
| `SendTagMessage` | Envia mensajes sustituyendo marcadores `{tag}`. |
| `ShowTagEventTitle` | Muestra Event Titles y subtitulos sustituyendo marcadores `{tag}`. |
| `ControlTimer` | Inicia y controla un timer circular por jugador. |

### RemoveEventTitle

Oculta el Event Title actual del jugador que activa el volumen.

- `FadeOutDuration`: segundos que tarda en desaparecer. Ejemplo: `1.5`.

Uso habitual: ejecutarlo al terminar una ronda o al abandonar una zona para
retirar un titulo mostrado anteriormente.

### GiveRandomItem

Selecciona uniformemente un asset cargado de tipo `Item` y lo entrega al
jugador activador.

- `Quantity`: cantidad del item seleccionado.
- `OverflowBehavior`:
  - `DROP_REMAINDER`: lo que no cabe cae al suelo.
  - `IGNORE_REMAINDER`: lo que no cabe se descarta.
  - `REQUIRE_FULL_STACK`: no entrega nada si no cabe la cantidad completa.

Este efecto selecciona entre todos los items cargados, incluidos los aportados
por otros asset packs.

### RandomTagSelection

Elige un valor al azar de una lista y lo guarda como tag de Trigger Volume.

- `TagKey`: tag que se modificara. Ejemplo: `selected_room`.
- `TagValues`: valores separados por coma, punto y coma, barra vertical o
  salto de linea. Ejemplo: `lava, ice, forest`.
- Sin `MatchKey`, modifica solamente el volumen que ejecuta el efecto.
- `MatchKey` y `MatchValue`: filtro opcional para localizar otros volumenes.
- `Radius`: radio maximo de busqueda en bloques.
- `Center`: busca desde el volumen (`VOLUME`) o desde la entidad activadora
  (`ENTITY`).

Ejemplo: configura `TagKey=theme` y `TagValues=desert,forest,ice` al comenzar
una ronda. Otros efectos pueden leer después `{theme}`.

### PasteRandomPrefab

Elige un prefab cargado y lo pega en el mundo.

- `Prefab1` y `Prefab2`: dos slots directos con selector de assets.
- `Weight1` y `Weight2`: pesos de esos slots.
- `Prefabs`: lista adicional separada por coma, punto y coma, barra vertical o
  salto de linea. Cada entrada admite `ruta=peso` o `ruta:peso`.
- `UseWeights`: usa los pesos; desactivado significa probabilidad uniforme.
- `Position`: coordenada absoluta o desplazamiento XYZ.
- `AtVolumeOrigin`: al activarlo, `Position` se suma al origen del volumen.
- `ShowParticles`: muestra las particulas vanilla del pegado.

Ejemplo de lista ponderada:

```text
rooms/common=1; rooms/rare=0.2; rooms/boss=0.05
```

Los prefabs deben estar disponibles en un asset pack cargado.

### SendTagMessage

Envia texto directo y sustituye cada marcador `{nombre}` por el valor de la
tag correspondiente. Ejemplo: `Puntos: {points}`.

- `Message`: texto del mensaje.
- `Recipient`: jugador activador, jugador mas cercano, jugadores dentro del
  volumen o todos los jugadores del mundo actual.
- `TagSource`:
  - `SELF`: tags del volumen que contiene el efecto.
  - `EVENT`: tags de los volumenes implicados en el evento.
  - `RADIUS`: tags de volumenes cercanos a la posicion del evento.
- `TagRadius`: radio usado por `RADIUS`.

Si varios volumenes aportan la misma tag, gana el mas cercano al evento; el ID
del volumen desempata distancias iguales. Una tag inexistente se conserva como
`{nombre}` para hacer visible el error de configuracion.

### ShowTagEventTitle

Equivalente a un Event Title vanilla, pero con sustitucion de `{tag}` tanto en
el titulo como en el subtitulo.

- `PrimaryTitle` y `SecondaryTitle`: textos mostrados.
- `IsMajor`: usa la presentacion grande.
- `Icon`: asset ID opcional del icono.
- `Duration`, `FadeInDuration` y `FadeOutDuration`: tiempos en segundos.
- `TagSource` y `TagRadius`: funcionan igual que en `SendTagMessage`.

Se dirige al jugador que activa el Trigger Volume.

### ControlTimer

Muestra un contador digital dentro de un anillo que se vacia en sentido
horario. Cada jugador tiene un timer independiente.

- `Action`: `START`, `PAUSE`, `RESUME`, `SHOW`, `HIDE` o `CANCEL`.
- `DurationSeconds`: duracion usada por `START`, entre 1 y 359999 segundos.
- `Recipient`: jugador activador, jugador mas cercano, jugadores dentro del
  volumen o todos los jugadores del mundo actual.

`HIDE` oculta el HUD sin detener la cuenta. Al llegar a cero, muestra `00:00`
durante un segundo y se retira. El estado vive en memoria y no sobrevive a un
reinicio completo del servidor.

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

## Tags en mensajes y titulos

Los efectos vanilla construyen algunos textos libres como claves de
traduccion. En pre-release 0.6.8 eso impide resolver correctamente parametros
de tags. `SendTagMessage` y `ShowTagEventTitle` resuelven primero el texto en el
servidor y lo envian como texto directo.

Con tags `theme=volcano` y `points=42`:

```text
Theme {theme}: {points} points
```

produce:

```text
Theme volcano: 42 points
```

## Build y validacion

```powershell
.\mods\java\more-triggers\tools\Test-PluginLocalization.ps1
.\mods\java\more-triggers\tools\Test-TagTemplateResolver.ps1

.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\more-triggers `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName More_Triggers-1_4_0.jar

.\mods\java\more-triggers\tools\Test-TimerMath.ps1
.\mods\java\more-triggers\tools\Test-Package.ps1 `
  -ArchivePath .\mods\java\more-triggers\.build\dist\More_Triggers-1_4_0.jar
```

El contorno del timer usa 61 frames PNG porque la UI 0.6.x no expone un
progreso radial nativo. Se regeneran con
`tools/Generate-TimerRingAssets.ps1`.
