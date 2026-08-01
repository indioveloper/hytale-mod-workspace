# Map Selector

Proyecto auxiliar creado para ayudar a otro desarrollador. No forma parte de
los mods activos ni de las releases de OrbGenesis y no debe instalarse en los
mapas de desarrollo.

El mod para Hytale pre-release 0.6.x abre con `/mapas` una lista cerrada
de mapas y reutiliza el componente nativo `PrefabPreviewComponent`.

## Mapas iniciales

Los prefabs se resuelven desde los Asset Packs cargados, sin copiarlos al mod:

- `mapa 1.prefab.json`
- `mapa 2.prefab.json`

En la partida de desarrollo `test roguelike` ambos pertenecen al pack
`tests:tests`.

Al pulsar `IR AL MAPA`, la eleccion se conserva en memoria por UUID de jugador
y el jugador se teletransporta en el mundo actual a las coordenadas configuradas
en `MapDefinition`. El mod no pega el prefab ni modifica bloques. Otros sistemas
pueden consultar la seleccion con `MapSelectorPlugin#getSelectedMap(UUID)`.

Destinos placeholder actuales:

- `mapa 1`: `100, 100, 100`
- `mapa 2`: `200, 100, 200`

## Compilar

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\auxiliary\java\map-selector `
  -SourceRoot src `
  -PackageRoot . `
  -ArtifactName Map_Selector-0_1_1.jar
```

## Probar

Instala el JAR en los mods del save, reinicia la partida y ejecuta `/mapas`.
El Asset Pack que contiene los prefabs debe estar habilitado.
