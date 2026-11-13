# Raynor Cinema Watch Parties — spike 0.1.0

Prototipo inicial para estudiar watch parties sincronizadas en un servidor de
Hytale. Esta versión implementa el núcleo de sesión y los comandos, pero **no
reproduce todavía vídeo dentro del mundo**.

## Conclusión de viabilidad

La API pública de Hytale 0.6.3 no expone un WebView, navegador, iframe ni
reproductor de vídeo para Custom UI o para una textura del mundo. El Shared
Source se buscó expresamente y no apareció ninguna superficie web utilizable por
un plugin de servidor.

YouTube permite controlar su reproductor oficial mediante IFrame Player API,
pero sus políticas no permiten descargar, importar o cachear el audiovisual para
convertirlo en spritesheets. Por tanto, `yt-dlp` + `ffmpeg` no es una base
publicable apropiada para URLs de YouTube.

La arquitectura viable separa dos rutas:

1. **YouTube:** reproductor oficial IFrame en una página complementaria o futura
   superficie web oficial del cliente, sincronizado con el reloj del servidor.
2. **Pantalla 3D dentro de Hytale:** medios locales/directos sobre los que el
   administrador tenga derechos, convertidos previamente a segmentos de frames.

La segunda ruta puede aprovechar el pipeline de atlas/partículas de Raynor
Holograms, pero no es adecuada para retransmitir vídeos largos a alta resolución:
las reconstrucciones de assets, ancho de banda y memoria del cliente serían el
cuello de botella. También falta una API de audio espacial sincronizado.

## Qué implementa 0.1.0

- Normalización estricta de URLs HTTPS de YouTube (`watch`, `youtu.be`,
  `shorts`, `live` y `embed`).
- Sesiones aisladas por mundo.
- Reloj autoritativo monotónico derivado de epoch; no suma tiempo por tick.
- Estados `STOPPED`, `PAUSED` y `PLAYING`.
- Revisión incremental para que los clientes ignoren mensajes antiguos.
- Play, pause, seek, stop, status, list y remove.
- URL del reproductor oficial con el segundo inicial sincronizado.
- Ninguna descarga, cache o extracción del contenido audiovisual.

Comandos:

```text
/cinema create <id> <youtube-url>
/cinema play <id>
/cinema pause <id>
/cinema seek <id> <segundos>
/cinema stop <id>
/cinema status <id>
/cinema list
/cinema player <id>
/cinema remove <id>
```

Las sesiones son temporales en este spike y desaparecen al reiniciar el
servidor. Los comandos requieren el grupo `hytale:WorldEditor`.

## Protocolo de sincronización propuesto

El servidor debe publicar snapshots con:

```json
{
  "sessionId": "sala_1",
  "videoId": "M7lc1UVf-VE",
  "state": "PLAYING",
  "positionSeconds": 42.125,
  "serverMonotonicNanos": 123456789,
  "revision": 8
}
```

El cliente no recibe un frame por tick. Calcula la posición desde el snapshot y
su reloj local. Al recibir una revisión nueva:

- aplica play/pause;
- corrige con `seekTo` si el drift supera aproximadamente 500 ms;
- ignora revisiones antiguas;
- hace una resincronización periódica cada 5–10 segundos.

La reproducción del IFrame necesita una acción explícita del usuario por las
restricciones de autoplay de navegadores. Cada espectador debe pulsar Play en el
reproductor oficial al menos una vez.

## Pantalla 3D propuesta para medios propios

Pipeline experimental, fuera de 0.1.0:

1. El administrador coloca una entidad-pantalla 16:9 seleccionable.
2. Un proceso asíncrono valida el archivo y lo convierte en segmentos cortos.
3. Cada segmento produce un spritesheet con padding compatible con Update 6.
4. Los assets se preparan fuera del hilo del mundo.
5. El cliente recibe una reconstrucción de Common assets antes de activar el
   segmento.
6. El `FrameIndex` se calcula desde el mismo reloj autoritativo.
7. Se precarga el segmento siguiente y se libera el anterior con un presupuesto
   global de memoria.

Objetivo prudente para una primera prueba: 320x180 o 512x288, 8–12 FPS,
segmentos de 3–5 segundos y sin audio. No prometer 1080p/30 FPS con el sistema de
assets actual.

## Integración propuesta con Raynor Holograms

No se debe copiar el renderer. El siguiente paso sería exponer una API pública
pequeña y versionada desde Holograms:

```java
ScreenHandle createScreen(ScreenSpec spec);
void publishFrameSegment(ScreenHandle screen, FrameSegment segment);
void selectSegment(ScreenHandle screen, String segmentId, double offsetSeconds);
```

Cinema conservaría la sesión y Holograms sería responsable de la entidad,
selección, escala, atlas y entrega de assets. Las operaciones de decoder y pack
seguirían en `HytaleServer.SCHEDULED_EXECUTOR`, nunca en el tick del mundo.

## Build y prueba

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\experiments\java\raynor-cinema `
  -SourceRoot src `
  -PackageRoot . `
  -ArtifactName Raynor_Cinema-0.1.0.jar

.\experiments\java\raynor-cinema\tools\Test-SelfTest.ps1
```

El artefacto queda en:

```text
experiments/java/raynor-cinema/.build/dist/Raynor_Cinema-0.1.0.jar
```

No instalar ni publicar este spike como si ya proyectara vídeo. Primero hay que
elegir entre una experiencia YouTube oficial externa/futura superficie web y un
renderer limitado a medios propios dentro del mundo.
