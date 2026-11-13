# Build Battle

Mod Java para Hytale Update 6 estable. La version `0.3.0` ejecuta rondas
completas individuales o por equipos, con temas editables, parcelas protegidas,
votacion y resultados. El manifest acepta servidores `>=0.6.2 <0.7.0` y el
artefacto se compila contra el runtime estable `0.6.6`.

## Flujo de partida

1. Los jugadores entran al lobby con `/buildbattle join` o
   `/buildbattle jointeam <equipo>`.
2. Un administrador inicia modo individual o equipos. En equipos, los codigos
   iguales agrupan jugadores hasta `teamSize`; los huecos restantes se
   equilibran automaticamente.
3. Se muestran varias opciones elegidas al azar desde `themes.json`. Cada
   jugador puede cambiar su voto mientras la fase siga abierta.
4. El tema ganador inicia la construccion. Cada equipo aparece en su parcela,
   pasa a creativo y solo puede colocar o romper bloques dentro de sus limites.
   El mod retira y bloquea todas las Builder Tools de edicion del mundo.
5. Al terminar, los jugadores pasan a aventura y visitan todas las parcelas.
   En cada una puntuan de 1 a 5; no pueden votar su propia construccion.
6. Se anuncia la clasificacion por media y se restauran el modo de juego, la
   posicion y la rotacion originales de cada jugador.

La interfaz de votacion tambien tiene equivalentes por comando, utiles para
probar el flujo si una Custom UI no puede abrirse.

## Comandos

- `/bb join`: entra al lobby del mundo actual.
- `/bb jointeam <equipo>`: entra y solicita un equipo.
- `/bb team <equipo>`: cambia la preferencia antes de iniciar.
- `/bb leave`: abandona el lobby o la ronda.
- `/bb status`: muestra fase, tema, tiempo y equipo.
- `/bb votetheme <tema>`: vota una opcion exacta de tema.
- `/bb vote <1-5>`: puntua la parcela visible.
- `/bb startsolo`: inicia una ronda individual; requiere `hytale:WorldEditor`.
- `/bb startteams`: inicia una ronda por equipos; requiere `hytale:WorldEditor`.
- `/bb stop`: cancela y restaura a los participantes; requiere
  `hytale:WorldEditor`.
- `/bb reload`: recarga los dos JSON; requiere `hytale:WorldEditor`.

## Configuracion

En el primer arranque se crean `build-battle.json` y `themes.json` en el
directorio de datos del plugin. `themes.json` es una lista JSON de textos y se
puede modificar sin recompilar. Ejecuta `/bb reload` despues de editarlo.

`build-battle.json` controla:

- `minimumPlayers`, `teamSize` y `themeCandidates`;
- `themeVoteSeconds`, `buildSeconds`, `secondsPerBuildVote` y `resultsSeconds`;
- `plots`, con `id`, `world`, limites inclusivos `min`/`max`, punto `spawn`,
  punto `view` y `yaw` en radianes.

Cada parcela admite ademas `resetPrefab` y `resetOrigin`. Si `resetPrefab`
contiene la ruta de un prefab disponible, el mod lo pega en `resetOrigin` al
comenzar la construccion. La plantilla debe abarcar toda la parcela e incluir
el aire necesario para borrar la construccion anterior. Si se deja vacio, el
mod no modifica el terreno antes de la ronda. Ejemplo:

```json
{
  "id": "plot-1",
  "world": "default",
  "min": [-64, 0, -64],
  "max": [-1, 255, -1],
  "spawn": [-32.5, 65, -32.5],
  "view": [-32.5, 72, -72.5],
  "yaw": 0,
  "resetPrefab": "BuildBattle/Plots/EmptyPlot.prefab.json",
  "resetOrigin": [-64, 0, -64]
}
```

## Compatibilidad heredada

Se conservan los IDs persistentes `SuggestBuildTheme` y
`RestrictBuildBattleCreativeTools`. El primero mantiene la antigua UI de una
palabra para guardar `theme_<palabra>=empty` y `points=0` en el Trigger Volume.
La regla heredada puede usarse bajo `Always Active`; en 0.3.0 bloquea todas las
Builder Tools, igual que la fase de construccion administrada por el mod.

## Dependencias

- `Hytale:TriggerVolumes`
- `Hytale:BuilderTools`

## Compilar y validar

```powershell
.\mods\java\build-battle\tools\Test-PluginLocalization.ps1
.\mods\java\build-battle\tools\Test-Core.ps1
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\build-battle `
  -SourceRoot src `
  -PackageRoot . `
  -ArtifactName Build_Battle-0_3_0.jar
.\mods\java\build-battle\tools\Test-Package.ps1
```

Instala el JAR sin duplicar `OrbGenesis:Build Battle` y reinicia o recarga la
partida para que se registren las clases nuevas.
