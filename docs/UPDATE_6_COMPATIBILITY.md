# Compatibilidad con Hytale Update 6

Validacion realizada el 30 de agosto de 2026 para preparar las publicaciones de
Raynor Mods en CurseForge.

## Runtime de referencia

- Hytale estable `0.6.2` (Update 6, hotfix 2).
- Revision del servidor: `bb28fc642e147a39b1ce7e952903235e43f5afe8`.
- SHA-256 de `HytaleServer.jar`:
  `D1697D841D088FE38EC1789FBABAEE11ED0AF1DA2BD5146AB072345D55B33F86`.
- JDK: Java 25.
- Shared Source de consulta: rama `pre-release`, commit
  `634880ce888e66922881597667fb87fbc0851e12`.

El servidor, `Assets.zip`, los assets extraidos y el Shared Source son
dependencias externas y no se incluyen en Git.

## Releases preparadas

| Proyecto | Version | Estado en Update 6 |
| --- | --- | --- |
| Configurable Mob Spawners | `0.5.3` | WIP; compila y supera pruebas estaticas y de paquete. Conserva pendientes visuales y requiere smoke test de sus flujos de juego antes de declararlo estable. |
| More Triggers | `1.10.5` | Compila y supera localizacion, pruebas funcionales estaticas y prueba de paquete. |
| Entity Motion Triggers | `1.3.2` | Compila y supera sus pruebas estaticas. |
| Particle Shape VFX | `0.1.1` | Compila y supera prueba funcional y de paquete. |
| Build Battle | `0.2.4` | Compila y supera sus pruebas; sigue siendo prototipo. |
| Editable Objectives / Scoreboards | `2.0.11` | Compila y supera pruebas estaticas; el smoke test completo dentro del juego sigue pendiente. |
| Raynor NPCs | `1.1.1` | Compila y supera sus pruebas de contratos NPC. |

Los packs puros del catálogo también declaran la línea estable y se empaquetan
sin rutas Windows: Mechanisms `1.1.2`, Raynor Blocks `1.0.2`, Nexus Siege Props
`1.0.1` y Roguelike Prefabs `1.0.1`. Los dos últimos siguen siendo material
interno, no releases generales.

Todos los manifests Java publicables conservan sus `Group:Name` e IDs
persistentes y declaran `ServerVersion` como `>=0.6.2 <0.7.0`. La autoria
publica es `Raynor` y cada ficha enlaza al catalogo de Raynor Mods.

## Que significa la validacion

`scripts/Test-ActiveJavaMods.ps1` recompila los siete plugins contra el
`HytaleServer.jar` estable real y ejecuta todas las pruebas disponibles. Esto
detecta roturas de API, recursos omitidos, manifests inconsistentes,
localizaciones incompletas y regresiones cubiertas por los tests.

Además, un arranque aislado con los once artefactos del catálogo cargó los
cuatro packs y dejó los siete plugins Java en estado `Enabled`, sin IDs de
plugin duplicados. La opción global `--validate-assets` no se usa como criterio
de aprobación porque el propio `Assets.zip` estable falla la validación de sus
instancias cuando el servidor se ejecuta con `--bare`; ese fallo también se
reproduce fuera de los assets de Raynor.

No sustituye una prueba manual de todas las mecanicas en cliente y servidor.
Antes de subir cada archivo a CurseForge se debe comprobar en una instalacion
limpia de `0.6.2`:

1. que no exista otro JAR con el mismo `Group:Name`;
2. que el plugin se habilite sin errores y muestre su icono;
3. que sus comandos, UI y Trigger Volumes principales funcionen;
4. que un cliente pueda conectarse y reciba correctamente los assets;
5. que el SHA-256 del archivo probado sea el mismo que el publicado.

Los JARs generados viven en las carpetas `.build/dist` de cada proyecto y no se
versionan. Los binarios publicables deben adjuntarse a una release o subirse a
CurseForge; Git conserva unicamente fuentes, scripts, manifests y contexto.
