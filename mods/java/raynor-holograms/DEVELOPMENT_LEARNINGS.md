# Aprendizajes de desarrollo: Raynor Holograms

Este documento recoge las decisiones técnicas y los problemas reproducidos al
desarrollar Raynor Holograms hasta la versión 0.8.3 para Hytale 0.6.2. Sirve
como handoff para continuar el proyecto sin depender del historial de chats.

## Principios de arquitectura

- El proyecto es una implementación *clean-room*. Los JAR de otros mods se
  usaron para estudiar capacidades visibles y contratos públicos, no para
  copiar su código, assets, formatos privados ni nombres internos.
- Un holograma persistente corresponde a una única entidad de red. El texto
  enriquecido puede producir muchos nodos dentro de un modelo, pero no crea una
  entidad, UUID o sistema de seguimiento por carácter.
- `Raynor_TextHologram` y `Raynor_ImageHologram` son los componentes
  serializables que identifican y persisten cada tipo de holograma.
- Los sistemas `TextIdentity` e `ImageIdentity` asignan un ID nuevo cuando la
  Entity Tool clona una entidad. La herramienta vanilla puede mover, rotar,
  escalar, clonar y borrar el holograma completo.

## Renderizado de texto

La versión 0.8 introduce tres modos:

- `AUTO`: usa `Nameplate` para texto sencillo y el renderer enriquecido cuando
  hay colores, gradientes o efectos.
- `LIVE`: fuerza texto nativo del cliente. Es la opción más nítida, pero no
  reproduce toda la composición visual.
- `RICH`: rasteriza el texto completo y lo publica como un único quad/modelo.

El parser acepta colores legacy (`&d`), hexadecimales, gradientes y los efectos
`glow`, `highlight/background`, `bold`, `shadow` y `outline`. También reconoce
sus alias españoles. Los límites defensivos son 4096 caracteres de fuente,
1024 glifos visibles, 32 líneas y 160 caracteres por línea.

Los placeholders implementados son `{online}`, `{players}`, `{world}`,
`{time}` y `{date}`; también se admiten sus variantes `%nombre%`. Se resuelven
en servidor, por lo que todos los jugadores ven el mismo contenido resuelto.

## El problema del atlas en Update 6

Update 6 prepara el atlas de texturas de entidades durante la entrada del
cliente. Añadir una PNG de entidad después de esa fase puede entregar los bytes
pero dejar el modelo muestreando una región transparente o negra. Este fue el
origen de los rectángulos negros y textos casi invisibles de las primeras
pruebas.

Se probaron dos estrategias:

1. Un atlas estático de paleta (`ColorAtlas`) y un atlas de medios transparente
   de partida (`MediaAtlas`), conocidos antes de `PrepareEntitiesAtlas`.
2. Para rellenar el atlas de medios en runtime, registrar el Common asset y emitir
   `RequestCommonAssetsRebuild` tras publicar cambios. Las texturas se rellenan
   a múltiplos de 32 y los modelos referencian regiones del atlas 2048x2048.

Si vuelve a aparecer un plano negro, hay que comprobar, en este orden: que la
textura está dentro del JAR o pack generado, que la ruta usada por el modelo
coincide exactamente, que sus dimensiones son válidas y que el cliente recibió
la reconstrucción del Common asset. Recargar solo las clases del plugin no
garantiza reconstruir el atlas del cliente.

## Imágenes y GIF

Las fuentes aceptadas son recursos `builtin:`, archivos bajo el directorio de
datos `images` mediante `local:` y URLs HTTP/HTTPS. La carga remota bloquea
direcciones privadas o locales para reducir SSRF, no sigue más de tres
redirecciones, usa timeouts y limita cada descarga a 10 MB.

El decoder limita el trabajo a 16 megapíxeles de entrada, 1024 px para imágenes
estáticas y 256 px para animaciones. Los GIF conservan los retardos por frame,
componen los métodos de disposición y se recortan a 48 frames. Los frames se
expresan mediante assets de partículas nativas con `FrameIndex`, evitando crear
una entidad distinta por frame. La caché de decodificación es LRU y conserva 32
entradas.

El pack de datos generado vive en el directorio del plugin y no debe
versionarse. `Samples/prueba.gif` sí debe viajar dentro del JAR porque se copia
al directorio local la primera vez que arranca el mod.

### Bloqueo al importar GIF desde un comando

En 0.8.2, `/holo createimage` llamaba a `ParticleHologramAssets.ensure()` desde
el hilo del mundo. Decodificar el GIF, escribir el spritesheet y ejecutar
`unregisterPack/registerPack` desde ese contexto produjo una espera circular
con el sistema de assets. El síntoma en el log fue que aparecía la recarga del
`.particlespawner` y, desde ese instante, el cliente dejaba de recibir paquetes.
La llamada nunca alcanzaba `store.addEntity()`.

Desde 0.8.3, el comando crea inmediatamente una entidad-ancla invisible. El
sistema inicia una única `CompletableFuture` transitoria por holograma usando
`HytaleServer.SCHEDULED_EXECUTOR`; allí se decodifica la imagen, se generan los
assets y se recarga el pack. El hilo del mundo solo consume el resultado ya
terminado y activa el asset de partículas después de una breve ventana de
entrega al cliente. Nunca se deben tomar o recargar locks de assets desde el
tick del mundo.

## Billboard y orientación

Los modos son `FIXED`, `BILLBOARD_Y` y `BILLBOARD`. Para entidades/modelos, el
servidor orienta el holograma hacia el jugador cargado más cercano; `BILLBOARD_Y`
solo cambia yaw y `BILLBOARD` puede ajustar también pitch. Un único objeto de
servidor no puede tener rotaciones diferentes para varios clientes. Los assets
de partículas usan la influencia billboard nativa cuando está disponible.

## Custom UI: fallos que desconectan al jugador

Custom UI aplica comandos contra selectores estrictos. Un `set` sobre un ID que
no existe en el documento cargado desconecta al cliente. El fallo de 0.8.0 fue:

`CustomUI Set command couldn't set value. Selector: #NativePreview.Text`

La fuente y el JAR sí declaraban `#NativePreview`; el cliente estaba reteniendo
una versión anterior de `HologramEditor.ui` que aún tenía `#TextPreview`. La
primera solución, 0.8.1, usa una ruta nueva,
`Pages/RaynorHolograms/HologramEditorV081.ui`, para invalidar esa caché.

El log del cliente mostró una segunda causa independiente en 0.8.1:
`No parameterless constructor defined for type 'System.String'`. En Update 6,
`Label.Text` exige un `System.String`; enviar un `Message` enriquecido hace que
el deserializador intente construir un string desde datos estructurados y
desconecte al jugador. Desde 0.8.2 la vista previa convierte el texto
enriquecido a texto plano antes del `set`. El renderer 3D continúa recibiendo y
aplicando las etiquetas completas.

Reglas prácticas:

- Versionar el nombre del documento cuando cambie su contrato de selectores.
- Reiniciar la partida/cliente después de sustituir el JAR.
- No usar `HorizontalAlignment: Left/Right` ni
  `VerticalAlignment: Top/Bottom` en `LabelStyle` de Update 6; los valores
  aceptados observados son más restrictivos.
- Ejecutar `tools/Test-HologramPackage.ps1`: extrae todos los selectores usados
  por `HologramEditorPage.java` y comprueba que existan en la UI empaquetada.
- Ejecutar también `tools/Test-PluginLocalization.ps1`; las claves de
  `server.lang` no llevan el prefijo `server.` porque el motor lo añade.

## Build y validación

Compilar siempre contra el `HytaleServer.jar` exacto de la versión objetivo:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\raynor-holograms `
  -SourceRoot src `
  -PackageRoot . `
  -ArtifactName Raynor_Holograms-0.8.3.jar `
  -ServerJar "$env:APPDATA\Hytale\install\release\package\game\latest\Server\HytaleServer.jar"
```

Después hay que ejecutar las dos pruebas PowerShell, compilar y ejecutar
`tests/java/.../RaynorHologramsSelfTest.java`, comprobar el contenido del JAR y
comparar SHA-256 con la copia instalada. Nunca deben coexistir dos JAR activos
con `RaynorMods:RaynorHolograms`.

## Límites y trabajo futuro

- La UI 0.8 es un editor de creación; recuperar el gestor completo de listado,
  edición, teletransporte y borrado requiere integrarlo con el nuevo modelo de
  datos sin reintroducir selectores obsoletos.
- La rotación billboard de modelo es compartida y depende del jugador más
  cercano, no es una orientación por cliente.
- Falta validar visualmente en juego el atlas dinámico con varias resoluciones,
  GIF largos, transparencias y reconexiones.
- Conviene añadir presupuesto global de memoria/CPU para muchos GIF simultáneos
  y persistir metadatos de error para diagnosticar fuentes que dejen de estar
  disponibles.
- Cualquier cambio de ID de componente o formato persistente necesita una
  migración explícita para no romper mundos guardados.
