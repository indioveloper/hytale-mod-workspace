# Contexto para agentes

Este repositorio es el punto de entrada canonico para el desarrollo Hytale de
OrbGenesis/Raynor. Lee primero `README.md` y despues el README o handoff del
proyecto concreto. No uses las antiguas carpetas de staging del PC como fuente
de verdad.

## Estado de referencia

- Fecha del handoff: 2026-07-18.
- Runtime de trabajo: Hytale pre-release 0.6.8.
- Referencia de API: `HypixelStudios/hytale-shared-source`, rama `pre-release`.
- JDK usado en los builds recientes: Java 25.
- Save de smoke test en el PC de origen: `0.6.8`.
- El Shared Source, el servidor y los assets vanilla son dependencias externas y
  nunca deben copiarse al repo.

## Proyecto activo: Player Trigger Tags

Ruta: `mods/java/player-trigger-tags`.

Manifest actual: `OrbGenesis:Player Trigger Tags`, version `1.5.6`.

Funciones principales:

- Tags string persistentes en jugadores.
- Efecto `ModifyPlayerTag` y condicion `PlayerTagCondition`.
- `ApplyHorizontalPlatformMotion` se muestra como `Mover entidad`; mueve props
  en linea recta hacia coordenadas XYZ absolutas y puede volver en bucle.
- Efectos para aplicar o retirar colision de plataforma al jugador.
- `ConvertBlocksToEntities` convierte bloques dentro del Trigger Volume usando
  un item elegido. Los items con modelo usan `ModelComponent`; los items-bloque
  normales usan `BlockEntity`; otros items usan `ItemComponent`.
- La escala de `BlockEntity` debe ser `1.0` para conservar el tamano del bloque.
- La colision creada por la conversion se aplica dos ticks mas tarde mediante
  `PendingPlatformCollisionComponent/System`, porque el cliente debe recibir
  primero la geometria y el `NetworkId`.

La correccion de colision de v1.5.6 compila y esta desplegada, pero al crear este
handoff aun necesita confirmacion manual dentro del juego.

Reglas importantes de este mod:

- No cambies IDs internos de efectos existentes solo para renombrar su etiqueta;
  romperias Trigger Volumes guardados.
- Un efecto de movimiento representa un unico tramo recto. Las secuencias se
  programan apilando efectos y usando `Effect Delay` vanilla.
- Las plataformas solo necesitan colision con jugadores, no con NPCs.
- Las entidades seleccionadas por efectos deben seguir siendo `PropComponent`.
- El selector de items debe usar la fuente vanilla `Item`; filtrar por
  `Item.getModel()` deja fuera los items-bloque.

## Localizacion de Trigger Volumes

Los archivos `Server/Languages/*/server.lang` reciben automaticamente el
namespace `server.`. Por tanto, las claves deben comenzar por `customUI...`, no
por `server.customUI...`.

Cada campo nuevo debe tener como minimo:

- etiqueta en `en-US` y `es-ES`;
- tooltip util con significado, unidades y ejemplo cuando corresponda;
- placeholder para entradas de texto o numericas cuando ayude;
- etiquetas para todas las opciones de enums.

Ejecuta `tools/Test-PluginLocalization.ps1` antes de empaquetar.

## Compilacion y despliegue

Compila siempre contra el `HytaleServer.jar` de la version que se va a probar,
no contra clases copiadas ni contra un JAR antiguo. Usa `scripts/Build-JavaMod.ps1`
o reproduce sus pasos con `javac` y `jar`.

Antes de sustituir un mod instalado:

1. Comprueba que la compilacion termina sin errores. Los warnings de APIs
   deprecated deben registrarse, pero no confundirse con errores.
2. Ejecuta las pruebas disponibles en el proyecto.
3. Comprueba que no existe otra instalacion con el mismo `Group:Name` en mods
   globales y mods del save.
4. Copia el JAR y compara SHA-256 entre build e instalacion.
5. Reinicia o recarga la partida; sobrescribir el archivo no recarga las clases
   de un plugin ya iniciado.

En el PC original, Player Trigger Tags se desplegaba sobre:

```text
%APPDATA%\Hytale\data\pre-release\Saves\0.6.8\mods\Player_Trigger_Tags-1_1_1.jar
```

Se conserva ese nombre para evitar dejar un segundo JAR. No crees otro archivo
con el numero de version nuevo en la misma carpeta.

## Otros proyectos

- `mods/java/chest-labels`: mod funcional en fase de prototipo; conserva datos
  en componentes de bloques y usa UI custom.
- `games/nexus-siege`: workspace del minijuego. Su plugin `Triggers_NPCs` esta
  bajo `trigger-volumes/Triggers_NPCs`; los NPCs vanilla y sus contratos estan
  bajo `npcs/vanilla`.
- `mods/java/scoreboards`: codigo de 0.5.x. Revisar APIs, manifest y UI antes de
  intentar instalarlo en 0.6.x.
- `mods/java/legacy`: referencia para migrar funciones, no asumir compatibilidad.
- `mods/asset-packs`: packs independientes relativamente limpios.
- `experiments/asset-packs`: material de laboratorio. Puede sobreescribir IDs
  vanilla y combinar varias pruebas incompatibles; inspeccionar antes de usar.

## Higiene del repositorio

- No versionar JARs, ZIPs, clases, outputs, logs, backups o saves.
- No versionar `HytaleServer.jar`, assets extraidos ni el Shared Source.
- No borrar fuentes legacy solo porque esten duplicadas; primero confirma que la
  version activa contiene todas sus funciones.
- Mantener ASCII en codigo y scripts salvo que el archivo ya requiera Unicode.
- Actualizar este archivo y el README del proyecto cuando cambien IDs, version,
  procedimiento de build, ruta de despliegue o limitaciones conocidas.
- Tras crear una version de mod, comprobar especialmente plugin duplicado,
  traducciones sin resolver y que el selector de assets devuelve resultados.
