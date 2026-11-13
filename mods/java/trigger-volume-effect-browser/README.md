# Trigger Volume Effect Browser

Prototipo independiente que amplia el inspector vanilla de Trigger Volumes con
un catalogo de tipos registradas y busqueda por nombre o ID tecnico.

## Uso

1. Busca `Herramienta de Trigger Volumes ampliada` en las Builder Tools y
   equipala. Primary y Secondary conservan el comportamiento de la herramienta
   vanilla para crear, seleccionar y editar volumenes.
2. Con un Trigger Volume seleccionado, pulsa `E` (`Use`) para abrir el catalogo.
   `/triggerpalette` se conserva como acceso alternativo y de diagnostico.
3. En la pestana Effects, selecciona evento, entrada y destino en la columna
   izquierda.
4. Busca y anade el tipo desde el catalogo de la derecha.
5. El editor de propiedades vanilla se abre al anadir o seleccionar un efecto;
   usa `Volver a efectos disponibles` para regresar al catalogo.

El prototipo no sustituye `/triggervolume effects` ni sobrescribe ningun asset
vanilla. Lee en tiempo de ejecucion los registros de condiciones, efectos y
reglas, por lo que tambien muestra los tipos aportados por otros mods cargados.

## Estado 0.1.6

- Catalogo searchable en una columna.
- La cabecera del catalogo se inserta en la fila vanilla de las pestanas. Los
  controles permanecen dentro del panel derecho, sin offsets negativos que
  desplacen su area interactiva.
- Tres vistas: `Todos (A-Z)`, agrupacion por categoria y agrupacion por mod.
  Los botones de mod solo aparecen cuando esa fuente tiene tipos registrados.
- Compatible con Conditions, Success Effects, Rejection Effects y Always
  Active Rules.
- Reutiliza completamente el guardado y el editor de propiedades vanilla.
- Incluye una herramienta clon que conserva `BuilderTool.Id=TriggerVolume`; no
  sustituye `EditorTool_TriggerVolume` ni modifica su asset.
- La interfaz no se abre al equipar la herramienta. La tecla E de
  `BuilderTool.Id=TriggerVolume` ejecuta directamente el comando vanilla
  `/triggervolume effects`, por lo que el mod instala un puente reversible que
  abre el inspector ampliado solo cuando esta equipada la herramienta clon. La
  herramienta vanilla sigue abriendo el inspector vanilla.
- Corregido el alta desde una fila: el binding lee el ID tecnico visible, ya
  no solicita `@EffectType` al cliente. El ID viaja como dato literal del boton
  y se entrega despues al controlador vanilla.
- Los selectores `Por categoria` y `Por mod` usan una accion interna del
  navegador y dejan de reutilizar `ChangeTab` del inspector vanilla.
- Corregido el selector de los filtros dinamicos: el boton es la raiz del
  fragmento UI, no un hijo anidado. Esto evita la desconexion al entrar en las
  vistas por categoria o por mod.
- Los modos de listado comparten ahora la misma fila visual que las pestanas
  vanilla. Las categorias y fuentes reservan cuatro filas completas, sin scroll
  ni recortes, y cada tipo muestra una descripcion breve en lugar de su ID.
- Requiere smoke test dentro del juego. Si el enfoque funciona, el siguiente
  paso es convertir la lista en una rejilla de dos columnas, anadir categorias
  semanticas y estudiar una entrada integrada en el comando vanilla.

## Build

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\trigger-volume-effect-browser `
  -SourceRoot src `
  -PackageRoot . `
  -AssetsRoot assets `
  -ArtifactName Trigger-Volume-Effect-Browser-0_1_6.jar `
  -ServerJar "$env:APPDATA\Hytale\install\release\package\game\latest\Server\HytaleServer.jar"
```
