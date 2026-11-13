# Compatibilidad de los JAR publicados con Hytale 0.6.6

Revision: 2026-09-17.

## Resultado

Los seis JAR de la rama estable que aparecen en
`website/raynor-mods/portal-config.js` siguen siendo compatibles con Hytale
estable `0.6.6`, revision
`ad11b033782bd8f5e715d2e8497d491f5780640c`.

La comprobacion uso los binarios publicados, no recompilaciones con el mismo
numero de version. Los seis manifests declaran `>=0.6.2 <0.7.0`, sus packs se
cargaron y cada plugin alcanzo el estado `Enabled` en un arranque aislado
`--bare` con los assets de 0.6.6:

| JAR publicado | SHA-256 |
| --- | --- |
| `ConfigurableMobSpawners-0.5.3.jar` | `5D508389AA88A2DA9490C0974634E37389C6754866A0A306C0A81B54DE7E2FB7` |
| `More_Triggers-1_10_5.jar` | `39F7FAB00752EE0D4E8F9F3D2669A59B224D7CE636858484FABB1F5EE0A17C84` |
| `Entity_Motion_Triggers-1_3_2.jar` | `577D1E744C092747E6CF0839E77CA23363BCC8898930CEE72DD929E16DE98964` |
| `Particle_Shape_VFX-0_1_1.jar` | `1E247C3877F54D12EDB2018BE04B64DA3B89A8D1E1B56130849F7D6A46F9AE3C` |
| `Scoreboards-2_0_11.jar` | `D300390F5F77153EE26E7B1F217622A5732B87C2E3B834FC8545ED5D005F445F` |
| `Raynor_Holograms-0.8.3.jar` | `9185F7A01BF9A2C1AF8B0CD66686E23AE9DFB3FA262058F0709BB61A7A36D927` |

El arranque bare termino con los errores vanilla ya conocidos de
`PermissionsModule` y `ServerManager`. No aparecieron errores de enlace Java,
manifests incompatibles, IDs duplicados ni fallos propios de estos plugins.

Ademas, `scripts/Test-ActiveJavaMods.ps1 -PublishedOnly` paso completo contra
el mismo servidor y `Assets.zip`. Esto recompila las fuentes mantenidas y
ejecuta localizacion, pruebas funcionales y contratos de paquete. El unico
aviso fue el uso de una API deprecated en `SpawnerTickSystem`; no fue un error
de compilacion.

## Hotfixes revisados

Las notas oficiales de Update 6 incorporaron despues de 0.6.2:

- `0.6.3` (31 de agosto): correcciones de render de NPC y UI social.
- `0.6.4` (7 de septiembre): entidades con modelos de bloque, rotacion de
  selecciones, ciclo de vida de `ClientTool` y fallback de iconos en `ItemGrid`.
- `0.6.5` (10 de septiembre): memoria de cliente/servidor y correcciones de
  Trigger Volumes disparados por NPC o proyectiles.
- `0.6.6` (14 de septiembre): estabilidad grafica y conexion/red.

Ninguno introduce una rotura de API para los seis JAR estables. Varias
correcciones benefician directamente a Entity Motion Triggers, More Triggers y
las interfaces que contienen previews de items.

Fuente oficial:
https://hytale.com/news/2026/8/hotfixes-update-6

## Pre-release de Update 7

La descarga `Raynor_Holograms-0.8.4-u7pre.1-Hytale-0.7.0-pre.1.jar` declara
`ServerVersion =0.7.0-pre.1`. Es una build fijada deliberadamente a Part 1 y no
debe anunciarse como compatible con la pre-release actual de Part 2 sin una
recompilacion y prueba contra ese runtime.

Part 2 retiro APIs como `WorldChunk.getWorld()` y cambio estructuras de red,
entre ellas `ShowEventTitle`. La instalacion local disponible durante esta
revision seguia siendo `0.7.0-pre.1`, por lo que no se hizo una afirmacion de
compatibilidad binaria con Part 2.

Fuente oficial:
https://hytale.com/news/2026/9/pre-release-patch-notes-update-7

## Limite de la comprobacion

El arranque aislado y la matriz automatica prueban carga, enlace, assets y los
contratos cubiertos por tests. Todavia hace falta un smoke test con cliente en
0.6.6 para validar visualmente Custom UI, previews, hologramas, prefabs,
plataformas, particulas y persistencia de scoreboards.
