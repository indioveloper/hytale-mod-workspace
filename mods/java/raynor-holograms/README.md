# Raynor Holograms

## Suite publica (2026-09-04)

Version release 0.8.3 recompilada contra 0.6.3. Disponible en el catalogo
Raynor Mods como experimental, junto a una descarga Alpha para Update 7 pre.1.
La variante 0.8.4-u7pre.1 vive en codex/update7-pre-release. No instalar ambas.
Incluido en scripts/Test-ActiveJavaMods.ps1 y scripts/Export-PublishedMods.ps1.

MVP experimental *clean-room* de hologramas persistentes para Hytale estable
0.6.2. La versión 0.8.3 admite texto nativo o enriquecido, imágenes integradas,
archivos locales, imágenes remotas y GIF animados. Cada holograma completo se
mantiene como una única entidad seleccionable con la Entity Tool vanilla.

## Cambios de 0.8.3

- Corrige la desconexión al abrir `/holograms` causada por una UI 0.7 retenida
  en caché: el editor usa ahora el documento versionado
  `HologramEditorV081.ui`.
- Añade una prueba de contrato entre todos los selectores utilizados por Java y
  los elementos realmente declarados en Custom UI.
- El empaquetado incluye `Samples/prueba.gif`, necesario para instalar el GIF
  de ejemplo en el primer arranque.
- Corrige la segunda causa de desconexión de `#NativePreview.Text`: en Update 6
  esa propiedad exige un `String`, no un objeto `Message`. La previsualización
  2D muestra el contenido sin etiquetas y el holograma 3D conserva los estilos.
- La decodificación de imágenes/GIF y la recarga de su pack generado se hacen
  en el ejecutor del servidor. El hilo del mundo crea primero un ancla invisible
  y activa la partícula cuando la preparación termina, evitando congelaciones.

## Funciones de 0.8

- Texto multilínea, alineación, escala, colores legacy/hex, gradientes, glow,
  sombra, resaltado, contorno y negrita.
- Modos de texto `AUTO`, `LIVE` y `RICH`.
- Placeholders `{online}`, `{players}`, `{world}`, `{time}` y `{date}`.
- Imágenes `builtin:`, `local:` y HTTP/HTTPS con límites de seguridad.
- GIF de hasta 48 frames conservando sus tiempos de reproducción.
- Orientación `FIXED`, `BILLBOARD_Y` o `BILLBOARD`.
- Componentes persistentes `Raynor_TextHologram` y
  `Raynor_ImageHologram`.

## Comandos

`/holo`, `/hologram` y `/holograms` son alias del mismo comando.

- Sin argumentos: abre el editor nativo.
- `create <id> <texto>`: crea texto persistente.
- `show <segundos> <texto>`: crea texto temporal con fundido.
- `createimage <id> <fuente> [tamaño]`: crea imagen o GIF.
- `list` e `info <id>`: inspeccionan hologramas cargados.
- `addline`, `setline` y `removeline`: modifican líneas.
- `billboard`, `align`, `textmode` y `scale`: cambian propiedades.
- `tphere <id>`: mueve el holograma delante del jugador.
- `remove <id>`: elimina el holograma.
- `images`: lista imágenes integradas y la carpeta local.
- `optimize`: vacía cachés temporales.

## Build

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\raynor-holograms `
  -SourceRoot src `
  -PackageRoot . `
  -ArtifactName Raynor_Holograms-0.8.3.jar `
  -ServerJar "$env:APPDATA\Hytale\install\release\package\game\latest\Server\HytaleServer.jar"
```

Validación estática:

```powershell
.\mods\java\raynor-holograms\tools\Test-PluginLocalization.ps1
.\mods\java\raynor-holograms\tools\Test-HologramPackage.ps1
```

La explicación de arquitectura, atlas, GIF, seguridad, Custom UI y límites se
encuentra en [DEVELOPMENT_LEARNINGS.md](DEVELOPMENT_LEARNINGS.md).
