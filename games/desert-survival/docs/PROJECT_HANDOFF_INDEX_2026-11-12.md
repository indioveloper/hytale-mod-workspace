# Desert Survival: indice de traspaso completo

Fecha: 2026-11-12. Desarrollo pausado a peticion del usuario. Este indice
acompaña a `RESEARCH_UI_HANDOFF_2026-11-12.md` y al ZIP portable
`output/desert-survival-handoff-2026-11-12.zip`. El ZIP es una copia de
lectura para traspaso, no una instalacion.

## Leer en este orden


0. `DESERT_SURVIVAL_CONTINUATION_2026-11-12.md` en la raiz del workspace:
   lo ocurrido despues de este handoff (causa del fallo, versiones 0.1.6 a
   0.2.0, estado del PC y pendientes). Leerlo antes que el resto.

1. `AGENTS.md` y `README.md` del workspace: instrucciones, higiene,
   build y advertencias sobre dependencias externas.
2. `games/desert-survival/README.md`: mapa general del proyecto.
3. `games/desert-survival/docs/RESEARCH_UI_HANDOFF_2026-11-12.md`:
   estado preciso del fallo, pruebas y rollback.
4. README de cada prototipo bajo `games/desert-survival/prototypes/`:
   `cutterray-t3`, `survival-vitals` y `research-tree`.

## Documentos de diseno incluidos

| Documento | Contenido |
| --- | --- |
| `docs/RESOURCE_PROGRESSION_RESEARCH.md` | Tiers de cutterray y recursos adoptados, con acero refinado y sin nodo natural. |
| `docs/RESOURCE_NODE_ARCHITECTURE.md` | Nodo hibrido: bloque ancla y entidad visual, extraccion de cinco segundos y modo analisis futuro. |
| `docs/LOOT_AND_BLUEPRINTS_DRAFT.md` | Capas de loot, esquemas esenciales/unicos y planos de base. Su nota de excluir el arbol del MVP es anterior a la decision de implementarlo. |
| `docs/RESEARCH_TREE_MVP.md` | 48 nodos, Intel, IDs y restricciones. Describe la antigua UI tabular; la fuente radial 0.1.5 es posterior. |
| `docs/RUNTIME_REVIEW_2026-11-10.md` | Instalacion Hytale 0.7.7 y referencia de pre-release. |
| `docs/RESEARCH_UI_HANDOFF_2026-11-12.md` | Diagnostico actual, archivos, pruebas y limites de certeza. |
| `assets/concepts/*.md` y `*.png` | Briefs e imagenes de cutterrays, rayos y nodos. |

No hay un GDD unico y cerrado dentro de esta carpeta. El proyecto combina
estos documentos y los README. Pilares acordados: mundo cerrado por biomas,
recursos, bases enemigas/dungeons, loot progresivo, construccion de bases,
calor/sed, tormentas, gusanos, vehiculos y herramientas propias. Facciones y
arbol general de mejoras quedaron fuera del primer MVP; el arbol de
investigacion de Intel se trabajo despues como prototipo especifico.

## Fuentes y assets incluidos

El ZIP contiene el arbol editable completo `games/desert-survival/`
exceptuando `.build/`, ademas de `AGENTS.md`, el `README.md` raiz y
`scripts/Build-JavaMod.ps1`. Incluye modelos, texturas, particulas, sonidos
declarativos, idiomas, codigo Java, manifests, tests y generadores de assets.
No contiene `HytaleServer.jar`, `Assets.zip`, Shared Source, cache, region
del mundo ni otras dependencias externas. Seguir sus rutas documentadas en
los README y compilar contra el runtime real.

## JARs incluidos en `jars/`

Se copian desde las instalaciones locales todos los JARs de los tres
prototipos Desert Survival, incluidos los sufijos `.replaced` y
`.failed-ui`. El archivo `jars/SHA256SUMS.txt` registra hashes y rutas
dentro del ZIP. Los nombres archivados se conservan para poder distinguir
versiones; renombrarlos a `.jar` sin retirar la variante activa produciria
un plugin duplicado.

| Prototipo | Activo en el PC al pausar | Variantes archivadas |
| --- | --- | --- |
| Research | `UserData/Mods/DesertSurvival-Research-0.1.1.jar` | 0.1.0, 0.1.2, 0.1.3, 0.1.4 globales; 0.1.5 fallida en `dune1/mods` |
| Vitals | `UserData/Mods/Raynor_Desert_Survival_Vitals_Prototype-0_2_2.jar` | 0.1.0, 0.1.1, 0.2.0, 0.2.1, 0.2.3 a 0.2.6 |
| Cutterray | `UserData/Mods/Raynor_Desert_Survival_Cutterray_T3_Prototype-0_1_8.jar` | 0.1.7 |

La carpeta del ZIP refleja ese estado, pero no lo activa en otro PC. Los
manifests de las fuentes actuales son Research 0.1.5 y Vitals 0.2.6; **no**
corresponden a las versiones activas. El README de Vitals 0.2.6 describe una
estrategia de UI que se probo y fallo para entrar a `dune1`. Para recuperar
el mundo, partir de los JARs activos indicados, no de un build automatico de
los manifests actuales.

## Evidencia y siguiente paso

La entrada con Research 0.1.1 + Vitals 0.2.2 se observo el 12 de noviembre
a las 00:48. Research 0.1.5 produjo otra desconexion por
`Hud/ReturnToHubButton.ui` de CreativeHub antes de abrir su menu. Se
restauro la combinacion anterior, pero no se volvio a probar tras el rollback.
Los logs originales estan bajo
`%APPDATA%/Hytale/UserData/Logs/` y
`%APPDATA%/Hytale/UserData/Saves/dune1/logs/`; no se incluyen en el ZIP
porque pueden contener informacion de cuenta, jugador y conexion.

Actualizacion 2026-11-12 (noche): la causa quedo identificada, ver la seccion
"Diagnostico posterior" de `RESEARCH_UI_HANDOFF_2026-11-12.md`. Research 0.1.7
entra en `dune1` (smoke test 2026-11-12 22:22); la causa era
`HorizontalAlignment: Right` en `ResearchNodeRow.ui`.

La investigacion pendiente debe partir de una copia de `dune1`, aislar
un cambio de UI a la vez desde Research 0.1.1 y verificar el arranque
antes de volver a modificar la partida principal. El usuario habia pedido
hacer el smoke test personalmente y no abrir launcher/partida; consultar de
nuevo antes de arrancar el cliente por cuenta propia.
