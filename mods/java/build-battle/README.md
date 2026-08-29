# Build Battle

Mod Java para Hytale Update 6 estable que anade el efecto de Trigger Volume
`SuggestBuildTheme` y la regla segura de herramientas creativas
`RestrictBuildBattleCreativeTools`. El manifest acepta servidores
`>=0.6.2 <0.7.0`; la version `0.2.4` compila y se ha validado con el
runtime estable `0.6.2`.

Al ejecutar el efecto sobre un jugador se abre una interfaz que solicita una
sola palabra. Una sugerencia valida se normaliza a minusculas y se guarda en el
propio Trigger Volume que ejecuto el efecto:

- `theme_<palabra>` = `empty`
- `points` = `0`

Se aceptan letras y numeros, ademas de guion y guion bajo, con un maximo de 32
caracteres. Los espacios y las entradas vacias se rechazan.

## Configurar el Trigger Volume

Anade `SuggestBuildTheme` a los efectos del evento deseado. Para el caso
propuesto, configura el evento de colocacion de bloque del Trigger Volume. El
evento determina cuando se abre la interfaz; el mod no escucha globalmente
todas las colocaciones de bloques.

Para limitar las herramientas durante la fase de construccion, anade
`RestrictBuildBattleCreativeTools` en la seccion `Always Active` del plot.
Mientras el jugador permanezca dentro, el catalogo de Builder Tools queda
reducido a:

- Entity, Paint y Sculpt;
- Boulder, Cave, Mountain, Tentacle, Decoration, Forest, Grass, Hotsprings,
  Path, River y Spiral Brush;
- Noise, Revolve, Scatter, Tint y Layer.

Los bloques y los `Technical Block Sets` no son Builder Tools, por lo que la
regla no los oculta ni los retira. Al salir del volumen, los permisos y el
catalogo normales se restauran en el siguiente tick.

Desde la version 0.2.2 este tipo solo se registra como regla `Always Active` y
no aparece en la lista de efectos `On Success`.

La restriccion usa los permisos nativos del servidor para rechazar las
herramientas no autorizadas. Como varias Scripted Brushes comparten el permiso
interno `Paint`, el mod tambien filtra el catalogo por ID exacto y retira
Builder Tools no permitidas que el jugador intente conservar en sus
inventarios. Un fichero de recuperacion permite restaurar los permisos si el
servidor se cierra de forma inesperada.

## Dependencias

- `Hytale:TriggerVolumes`
- `Hytale:BuilderTools`

## Compilar

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\build-battle `
  -SourceRoot src `
  -PackageRoot . `
  -ArtifactName Build_Battle-0_2_4.jar
```

Antes de empaquetar o desplegar, ejecuta:

```powershell
.\mods\java\build-battle\tools\Test-PluginLocalization.ps1
```

Instala el mod sin duplicar su `Group:Name` y reinicia o recarga la partida
para que se registren las clases nuevas.
